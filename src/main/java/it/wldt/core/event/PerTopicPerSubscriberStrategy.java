/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.core.event;

import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *
 * Per-DT, per-topic, per-subscriber event delivery strategy.
 *
 * Delivery pipeline for each Digital Twin:
 *
 *   publishEvent()
 *     → TopicQueue[dtId][topic]            (LinkedBlockingQueue — lock-free put)
 *       → TopicReader thread               (one per topic per DT, blocked on take())
 *         → SubscriberQueue[subscriberId]  (PriorityBlockingQueue ordered by publish-time nanos)
 *           → SubscriberProcessor thread   (one per subscriber, blocked on take())
 *             → onEvent() callback
 *
 * Key properties:
 *
 *   Publisher isolation: publishEvent() is never blocked by subscriber processing time.
 *
 *   Subscriber isolation: each subscriber runs in its own thread — a slow subscriber
 *   cannot delay delivery to other subscribers (unlike PerDtQueuedStrategy where all
 *   subscribers share one consumer thread).
 *
 *   Temporal ordering: events from the same topic arrive at the subscriber queue in strict
 *   FIFO order (single topic-reader thread). Events from different topics are ordered by
 *   publish-time nanosecond timestamp + monotonic sequence number. This is best-effort for
 *   events published within the same nanosecond window from concurrent publisher threads,
 *   since different topic-reader threads enqueue concurrently.
 *
 *   Wildcard subscriptions: wildcards are resolved at topic-reader dispatch time, not at
 *   subscribe time. Topic queues for topics with only wildcard subscribers are created lazily
 *   on first publish.
 *
 * Thread count: O(topics × DTs) topic-reader threads + O(subscribers) subscriber-processor threads.
 */
public class PerTopicPerSubscriberStrategy implements WldtEventBusStrategy {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(PerTopicPerSubscriberStrategy.class);

    // Monotonic counter — tiebreaker for events with identical publish-time nanos
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    // [dtId → [exactTopic → LinkedBlockingQueue<EnqueuedEvent>]]
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, LinkedBlockingQueue<EnqueuedEvent>>>
            topicQueues = new ConcurrentHashMap<>();

    // [dtId → [exactTopic → single-thread topic-reader executor]]
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ExecutorService>>
            topicReaders = new ConcurrentHashMap<>();

    // [subscriberId → timestamp-ordered delivery queue]
    private final ConcurrentHashMap<String, PriorityBlockingQueue<TimestampedDelivery>>
            subscriberQueues = new ConcurrentHashMap<>();

    // [subscriberId → single-thread subscriber-processor executor]
    private final ConcurrentHashMap<String, ExecutorService>
            subscriberThreads = new ConcurrentHashMap<>();

    // [dtId → [filterType → subscriber list]]
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>>>
            subscriberMap = new ConcurrentHashMap<>();

    // ── Publish ───────────────────────────────────────────────────────────────

    @Override
    public void publishEvent(String dtId, String pubId, WldtEvent<?> event,
                             IWldtEventLogger eventLogger) throws EventBusException {

        if (dtId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");
        if (event == null || event.getType() == null || event.getType().isEmpty())
            throw new EventBusException(String.format(
                    "EventBus-publishEvent() -> Error: event = NULL or event-type (%s) is invalid !",
                    event != null ? event.getType() : "null"));

        if (eventLogger != null) eventLogger.logEventPublished(pubId, event);

        String topic = event.getType();
        long publishNanos = System.nanoTime();

        // No DT-level entry means subscribe() was never called for this DT — drop.
        ConcurrentHashMap<String, LinkedBlockingQueue<EnqueuedEvent>> dtQueues = topicQueues.get(dtId);
        if (dtQueues == null) return;

        LinkedBlockingQueue<EnqueuedEvent> queue = dtQueues.get(topic);
        if (queue == null) {
            // Topic queue not yet created — possible if only wildcard subscribers exist.
            // Drop if there are no subscribers at all for this DT.
            ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap = subscriberMap.get(dtId);
            if (dtMap == null || dtMap.isEmpty()) return;

            // Create topic queue + reader under class lock (double-checked).
            synchronized (this) {
                queue = dtQueues.get(topic);
                if (queue == null) {
                    queue = new LinkedBlockingQueue<>();
                    dtQueues.put(topic, queue);
                    topicReaders.get(dtId).put(topic, startTopicReader(dtId, topic, queue));
                }
            }
        }

        try {
            queue.put(new EnqueuedEvent(publishNanos, event, pubId, eventLogger));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventBusException(
                    "EventBus-publishEvent() -> Interrupted while enqueuing event for DT: " + dtId);
        }
    }

    // ── Subscribe ──────────────────────────────────────────────────────────────

    @Override
    public synchronized void subscribe(String dtId, String subId,
                                       WldtEventFilter filter, WldtEventListener listener,
                                       IWldtEventLogger eventLogger) throws EventBusException {

        if (dtId == null)
            throw new EventBusException("EventBus-subscribe() -> Error: digitalTwinId = NULL !");
        if (filter == null || listener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventListener = NULL !");

        // Ensure per-subscriber queue + processor thread exist (created once per unique subscriberId).
        final PriorityBlockingQueue<TimestampedDelivery> subQueue =
                subscriberQueues.computeIfAbsent(subId, k -> new PriorityBlockingQueue<>());
        subscriberThreads.computeIfAbsent(subId, k -> startSubscriberThread(subId, subQueue));

        // Ensure DT-level maps exist so publishEvent can find them.
        topicQueues.computeIfAbsent(dtId, k -> new ConcurrentHashMap<>());
        topicReaders.computeIfAbsent(dtId, k -> new ConcurrentHashMap<>());

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.computeIfAbsent(dtId, k -> new ConcurrentHashMap<>());

        for (String filterType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list =
                    dtMap.computeIfAbsent(filterType, k -> new CopyOnWriteArrayList<>());
            WldtSubscriberInfo info = new WldtSubscriberInfo(subId, listener);
            if (!list.contains(info)) {
                list.add(info);
                listener.onEventSubscribed(filterType);
                if (eventLogger != null) eventLogger.logClientSubscription(filterType, subId);

                // Eagerly create topic queue + reader for exact (non-wildcard) filters.
                // Wildcard filters are handled lazily at publishEvent() time.
                if (!filterType.endsWith(".*"))
                    ensureTopicReader(dtId, filterType);
            } else {
                logger.debug("Subscriber {} already registered for {}", subId, filterType);
            }
        }
    }

    // ── Unsubscribe ───────────────────────────────────────────────────────────

    @Override
    public synchronized void unSubscribe(String dtId, String subId,
                                         WldtEventFilter filter, WldtEventListener listener,
                                         IWldtEventLogger eventLogger) throws EventBusException {

        if (dtId == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: digitalTwinId = NULL !");
        if (filter == null || listener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventListener = NULL !");

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap = subscriberMap.get(dtId);
        if (dtMap == null) return;

        WldtSubscriberInfo info = new WldtSubscriberInfo(subId, listener);
        for (String filterType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list = dtMap.get(filterType);
            if (list != null && list.remove(info)) {
                listener.onEventUnSubscribed(filterType);
                if (eventLogger != null) eventLogger.logClientUnSubscription(filterType, subId);
            }
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    @Override
    public void shutdown() {
        topicReaders.values().forEach(dtReaders -> dtReaders.values().forEach(ExecutorService::shutdownNow));
        topicReaders.clear();
        topicQueues.clear();
        subscriberThreads.values().forEach(ExecutorService::shutdownNow);
        subscriberThreads.clear();
        subscriberQueues.clear();
        subscriberMap.clear();
    }

    // ── Topic-reader internals ────────────────────────────────────────────────

    private void ensureTopicReader(String dtId, String topic) {
        ConcurrentHashMap<String, LinkedBlockingQueue<EnqueuedEvent>> dtQueues = topicQueues.get(dtId);
        LinkedBlockingQueue<EnqueuedEvent> q = new LinkedBlockingQueue<>();
        if (dtQueues.putIfAbsent(topic, q) == null) {
            // We inserted the queue — start its reader.
            topicReaders.get(dtId).put(topic, startTopicReader(dtId, topic, q));
        }
    }

    private ExecutorService startTopicReader(String dtId, String topic,
                                             LinkedBlockingQueue<EnqueuedEvent> queue) {
        ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wldt-topic-" + dtId + "-" + topic);
            t.setDaemon(true);
            return t;
        });
        exec.submit(() -> topicReaderLoop(dtId, queue));
        return exec;
    }

    private void topicReaderLoop(String dtId, LinkedBlockingQueue<EnqueuedEvent> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EnqueuedEvent ee = queue.take();
                dispatchToSubscriberQueues(dtId, ee);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Resolves exact + wildcard subscribers for the event type, enqueues a
     * TimestampedDelivery into each matched subscriber's priority queue.
     * The dispatched set prevents double-delivery when a subscriber matches
     * both an exact and a wildcard entry.
     */
    private void dispatchToSubscriberQueues(String dtId, EnqueuedEvent ee) {
        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap = subscriberMap.get(dtId);
        if (dtMap == null) return;

        Set<String> dispatched = new HashSet<>();

        // Exact match
        CopyOnWriteArrayList<WldtSubscriberInfo> exact = dtMap.get(ee.event.getType());
        if (exact != null) {
            for (WldtSubscriberInfo info : exact) {
                enqueueForSubscriber(info, ee);
                dispatched.add(info.getId());
            }
        }

        // Wildcard match — skip the exact entry (already dispatched) and deduplicate per subscriber
        for (Map.Entry<String, CopyOnWriteArrayList<WldtSubscriberInfo>> entry : dtMap.entrySet()) {
            if (!entry.getKey().equals(ee.event.getType())
                    && WldtEventFilter.matchWildCardType(ee.event.getType(), entry.getKey())) {
                for (WldtSubscriberInfo info : entry.getValue()) {
                    if (dispatched.add(info.getId()))
                        enqueueForSubscriber(info, ee);
                }
            }
        }
    }

    private void enqueueForSubscriber(WldtSubscriberInfo info, EnqueuedEvent ee) {
        PriorityBlockingQueue<TimestampedDelivery> subQueue = subscriberQueues.get(info.getId());
        if (subQueue != null)
            subQueue.offer(new TimestampedDelivery(
                    ee.nanos, SEQUENCE.getAndIncrement(), ee.event, info, ee.publisherId, ee.eventLogger));
    }

    // ── Subscriber-processor internals ────────────────────────────────────────

    private ExecutorService startSubscriberThread(String subId,
                                                  PriorityBlockingQueue<TimestampedDelivery> queue) {
        ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wldt-sub-" + subId);
            t.setDaemon(true);
            return t;
        });
        exec.submit(() -> subscriberLoop(queue));
        return exec;
    }

    private void subscriberLoop(PriorityBlockingQueue<TimestampedDelivery> queue) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TimestampedDelivery td = queue.take();
                td.info.getEventListener().onEvent(td.event);
                if (td.eventLogger != null)
                    td.eventLogger.logEventForwarded(td.publisherId, td.info.getId(), td.event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ── Inner types ────────────────────────────────────────────────────────────

    /** Event wrapper placed into the per-topic queue by publishEvent(). */
    private static final class EnqueuedEvent {
        final long             nanos;       // System.nanoTime() at publishEvent() — ordering key
        final WldtEvent<?>     event;
        final String           publisherId;
        final IWldtEventLogger eventLogger;

        EnqueuedEvent(long nanos, WldtEvent<?> event, String publisherId, IWldtEventLogger eventLogger) {
            this.nanos       = nanos;
            this.event       = event;
            this.publisherId = publisherId;
            this.eventLogger = eventLogger;
        }
    }

    /**
     * Delivery item placed into a subscriber's PriorityBlockingQueue.
     * Ordered by publish-time nanos (ascending); sequence breaks ties to preserve
     * arrival order for events with identical timestamps.
     */
    private static final class TimestampedDelivery implements Comparable<TimestampedDelivery> {
        final long             nanos;
        final long             sequence;
        final WldtEvent<?>     event;
        final WldtSubscriberInfo info;
        final String           publisherId;
        final IWldtEventLogger eventLogger;

        TimestampedDelivery(long nanos, long sequence, WldtEvent<?> event,
                            WldtSubscriberInfo info, String publisherId, IWldtEventLogger eventLogger) {
            this.nanos       = nanos;
            this.sequence    = sequence;
            this.event       = event;
            this.info        = info;
            this.publisherId = publisherId;
            this.eventLogger = eventLogger;
        }

        @Override
        public int compareTo(TimestampedDelivery other) {
            int c = Long.compare(this.nanos, other.nanos);
            return c != 0 ? c : Long.compare(this.sequence, other.sequence);
        }
    }
}

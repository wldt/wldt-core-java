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

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *
 * Default event delivery strategy for the WLDT event bus.
 *
 * Combines a per-DT bounded queue for publisher isolation with per-subscriber
 * dedicated threads for subscriber isolation:
 *
 * Delivery pipeline per Digital Twin:
 *
 *   publishEvent()
 *     → LinkedBlockingQueue&lt;DeliveryTask&gt; [dtId]    (bounded if configured — backpressure path)
 *       → DT consumer thread(s)                     (configurable pool, drains the DT queue)
 *         → resolves exact + wildcard subscribers   (dispatched-set dedup, state read at delivery time)
 *         → fan-out → LinkedBlockingQueue&lt;SubscriberDelivery&gt; [subscriberId]
 *           → subscriber processor thread           (one per subscriber, blocked on take())
 *             → onEvent() callback
 *
 * Key properties:
 *
 *   Publisher isolation: publishEvent() returns immediately after queue.put(); never blocked
 *   by subscriber processing.
 *
 *   Subscriber isolation: each subscriber runs in its own dedicated thread — a slow subscriber
 *   cannot delay delivery to other subscribers.
 *
 *   FIFO ordering: events arrive at subscriber queues in the same order they were placed in the
 *   DT queue, so per-subscriber LinkedBlockingQueue suffices (no priority queue needed).
 *
 *   Backpressure: configurable bounded DT queue — put() blocks when full, giving the publisher
 *   natural back-pressure.
 *
 *   Subscriber state at delivery time: the DT consumer reads the subscriber map when processing
 *   each event, so a subscriber registered after publishEvent() but before the consumer picks up
 *   the task may receive that event.
 *
 *   Thread count: O(DTs) DT consumer threads + O(subscribers) subscriber-processor threads.
 *
 * Ordering:
 *   threadPoolSize = 1 (default) → strict FIFO per DT, guaranteed by single DT consumer
 *   threadPoolSize > 1           → concurrent DT dispatch, ordering not guaranteed
 */
public class DefaultEventBusStrategy implements WldtEventBusStrategy {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DefaultEventBusStrategy.class);

    private final int threadPoolSize;
    private final int queueCapacity;

    // [dtId → LinkedBlockingQueue<DeliveryTask>]
    private final ConcurrentHashMap<String, LinkedBlockingQueue<DeliveryTask>> dtQueues
            = new ConcurrentHashMap<>();

    // [dtId → ExecutorService (DT consumer thread pool)]
    private final ConcurrentHashMap<String, ExecutorService> dtConsumers
            = new ConcurrentHashMap<>();

    // [dtId → [filterType → CopyOnWriteArrayList<WldtSubscriberInfo>]]
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>>>
            subscriberMap = new ConcurrentHashMap<>();

    // [subscriberId → LinkedBlockingQueue<SubscriberDelivery>]
    private final ConcurrentHashMap<String, LinkedBlockingQueue<SubscriberDelivery>> subscriberQueues
            = new ConcurrentHashMap<>();

    // [subscriberId → ExecutorService (single subscriber-processor thread)]
    private final ConcurrentHashMap<String, ExecutorService> subscriberThreads
            = new ConcurrentHashMap<>();

    public DefaultEventBusStrategy() {
        this(1, Integer.MAX_VALUE);
    }

    public DefaultEventBusStrategy(int threadPoolSize) {
        this(threadPoolSize, Integer.MAX_VALUE);
    }

    public DefaultEventBusStrategy(int threadPoolSize, int queueCapacity) {
        if (threadPoolSize < 1) throw new IllegalArgumentException("threadPoolSize must be >= 1");
        if (queueCapacity  < 1) throw new IllegalArgumentException("queueCapacity must be >= 1");
        this.threadPoolSize = threadPoolSize;
        this.queueCapacity  = queueCapacity;
    }

    // ── Publish ───────────────────────────────────────────────────────────────

    @Override
    public void publishEvent(String digitalTwinId, String publisherId, WldtEvent<?> event,
                             IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");

        if (event == null || event.getType() == null || event.getType().isEmpty())
            throw new EventBusException(String.format(
                    "EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !",
                    event != null ? event.getType() : "null"));

        if (eventLogger != null)
            eventLogger.logEventPublished(publisherId, event);

        LinkedBlockingQueue<DeliveryTask> queue = dtQueues.get(digitalTwinId);
        if (queue == null) return;

        try {
            // put() blocks only when queueCapacity is bounded and the queue is full (backpressure).
            // With the default unbounded capacity this never blocks.
            queue.put(new DeliveryTask(event, publisherId, eventLogger));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventBusException(
                    "EventBus-publishEvent() -> Interrupted while enqueuing event for DT: " + digitalTwinId);
        }
    }

    // ── Subscribe / Unsubscribe ───────────────────────────────────────────────

    @Override
    public synchronized void subscribe(String digitalTwinId, String subscriberId,
                                       WldtEventFilter filter, WldtEventListener listener,
                                       IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-subscribe() -> Error: digitalTwinId = NULL !");

        if (filter == null || listener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventListener = NULL !");

        // Ensure per-subscriber queue + processor thread exist (created once per unique subscriberId).
        subscriberQueues.computeIfAbsent(subscriberId, k -> new LinkedBlockingQueue<>());
        subscriberThreads.computeIfAbsent(subscriberId, k -> startSubscriberThread(subscriberId));

        // Ensure DT queue + consumer exist before registering subscriptions so that
        // publishEvent() can always find the queue once dtMap is visible.
        dtQueues.computeIfAbsent(digitalTwinId, k -> new LinkedBlockingQueue<>(queueCapacity));
        dtConsumers.computeIfAbsent(digitalTwinId, this::startDtConsumers);

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.computeIfAbsent(digitalTwinId, k -> new ConcurrentHashMap<>());

        for (String filterType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list =
                    dtMap.computeIfAbsent(filterType, k -> new CopyOnWriteArrayList<>());
            WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
            if (!list.contains(info)) {
                list.add(info);
                listener.onEventSubscribed(filterType);
                if (eventLogger != null)
                    eventLogger.logClientSubscription(filterType, subscriberId);
            } else {
                logger.debug("Subscriber {} already registered for {}", subscriberId, filterType);
            }
        }
    }

    @Override
    public synchronized void unSubscribe(String digitalTwinId, String subscriberId,
                                         WldtEventFilter filter, WldtEventListener listener,
                                         IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: digitalTwinId = NULL !");

        if (filter == null || listener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventListener = NULL !");

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.get(digitalTwinId);
        if (dtMap == null) return;

        WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
        for (String filterType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list = dtMap.get(filterType);
            if (list != null && list.remove(info)) {
                listener.onEventUnSubscribed(filterType);
                if (eventLogger != null)
                    eventLogger.logClientUnSubscription(filterType, subscriberId);
            }
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    @Override
    public void shutdown() {
        dtConsumers.values().forEach(ExecutorService::shutdownNow);
        dtConsumers.clear();
        dtQueues.clear();
        subscriberThreads.values().forEach(ExecutorService::shutdownNow);
        subscriberThreads.clear();
        subscriberQueues.clear();
        subscriberMap.clear();
    }

    // ── DT consumer internals ─────────────────────────────────────────────────

    private ExecutorService startDtConsumers(String dtId) {
        LinkedBlockingQueue<DeliveryTask> queue = dtQueues.get(dtId);
        ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "wldt-bus-" + dtId);
            t.setDaemon(true);
            return t;
        });
        for (int i = 0; i < threadPoolSize; i++)
            pool.submit(() -> dtConsumerLoop(queue, dtId));
        return pool;
    }

    private void dtConsumerLoop(LinkedBlockingQueue<DeliveryTask> queue, String dtId) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DeliveryTask task = queue.take();
                dispatchToSubscriberQueues(dtId, task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Resolves exact + wildcard subscribers for the event, then fans out a
     * SubscriberDelivery into each matched subscriber's dedicated queue.
     * The dispatched set prevents double-delivery when a subscriber matches
     * both an exact filter and a wildcard filter.
     */
    private void dispatchToSubscriberQueues(String dtId, DeliveryTask task) {
        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.get(dtId);
        if (dtMap == null) return;

        Set<String> dispatched = new HashSet<>();

        // Exact match — subscriber state read at delivery time
        CopyOnWriteArrayList<WldtSubscriberInfo> exact = dtMap.get(task.event.getType());
        if (exact != null) {
            for (WldtSubscriberInfo info : exact) {
                enqueueForSubscriber(info, task);
                dispatched.add(info.getId());
            }
        }

        // Wildcard match — skip exact entry already dispatched, dedup per subscriber
        for (Map.Entry<String, CopyOnWriteArrayList<WldtSubscriberInfo>> entry : dtMap.entrySet()) {
            if (!entry.getKey().equals(task.event.getType())
                    && WldtEventFilter.matchWildCardType(task.event.getType(), entry.getKey())) {
                for (WldtSubscriberInfo info : entry.getValue()) {
                    if (dispatched.add(info.getId()))
                        enqueueForSubscriber(info, task);
                }
            }
        }
    }

    private void enqueueForSubscriber(WldtSubscriberInfo info, DeliveryTask task) {
        LinkedBlockingQueue<SubscriberDelivery> subQueue = subscriberQueues.get(info.getId());
        if (subQueue != null)
            subQueue.offer(new SubscriberDelivery(task.event, info, task.publisherId, task.eventLogger));
    }

    // ── Subscriber processor internals ────────────────────────────────────────

    private ExecutorService startSubscriberThread(String subId) {
        ExecutorService exec = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wldt-sub-" + subId);
            t.setDaemon(true);
            return t;
        });
        exec.submit(() -> subscriberLoop(subId));
        return exec;
    }

    private void subscriberLoop(String subId) {
        LinkedBlockingQueue<SubscriberDelivery> queue = subscriberQueues.get(subId);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SubscriberDelivery delivery = queue.take();
                delivery.info.getEventListener().onEvent(delivery.event);
                if (delivery.eventLogger != null)
                    delivery.eventLogger.logEventForwarded(delivery.publisherId, delivery.info.getId(), delivery.event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ── Inner types ───────────────────────────────────────────────────────────

    private static final class DeliveryTask {
        final WldtEvent<?>     event;
        final String           publisherId;
        final IWldtEventLogger eventLogger;

        DeliveryTask(WldtEvent<?> event, String publisherId, IWldtEventLogger eventLogger) {
            this.event       = event;
            this.publisherId = publisherId;
            this.eventLogger = eventLogger;
        }
    }

    private static final class SubscriberDelivery {
        final WldtEvent<?>     event;
        final WldtSubscriberInfo info;
        final String           publisherId;
        final IWldtEventLogger eventLogger;

        SubscriberDelivery(WldtEvent<?> event, WldtSubscriberInfo info,
                           String publisherId, IWldtEventLogger eventLogger) {
            this.event       = event;
            this.info        = info;
            this.publisherId = publisherId;
            this.eventLogger = eventLogger;
        }
    }
}

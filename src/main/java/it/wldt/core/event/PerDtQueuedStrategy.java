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
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Per-DT explicit-queue event delivery strategy.
 *
 * Each Digital Twin owns:
 *   - a dedicated {@link LinkedBlockingQueue} of {@link DeliveryTask}s
 *   - a fixed thread pool of consumer threads (default size 1) that block on
 *     {@code queue.take()} and dispatch events as they arrive
 *
 * Publishers call {@code queue.put()} and return immediately — they are never
 * blocked by subscriber execution.
 *
 * Subscriber state is read at delivery time (inside the consumer thread), so a
 * subscriber registered after {@code publishEvent()} but before the consumer
 * thread picks up the event will receive it.
 *
 * Ordering:
 *   threadPoolSize = 1 (default) → strict FIFO per DT, guaranteed by single consumer
 *   threadPoolSize > 1           → concurrent dispatch, ordering not guaranteed
 *
 * Backpressure:
 *   queueCapacity = Integer.MAX_VALUE (default) → unbounded, publishers never block
 *   queueCapacity = N                           → bounded; {@code put()} blocks when full
 */
public class PerDtQueuedStrategy implements WldtEventBusStrategy {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(PerDtQueuedStrategy.class);

    private final int threadPoolSize;
    private final int queueCapacity;

    private final ConcurrentHashMap<String, LinkedBlockingQueue<DeliveryTask>> queues
            = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ExecutorService> consumers
            = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>>>
            subscriberMap = new ConcurrentHashMap<>();

    public PerDtQueuedStrategy() {
        this(1, Integer.MAX_VALUE);
    }

    public PerDtQueuedStrategy(int threadPoolSize) {
        this(threadPoolSize, Integer.MAX_VALUE);
    }

    public PerDtQueuedStrategy(int threadPoolSize, int queueCapacity) {
        if (threadPoolSize < 1) throw new IllegalArgumentException("threadPoolSize must be >= 1");
        if (queueCapacity < 1)  throw new IllegalArgumentException("queueCapacity must be >= 1");
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

        LinkedBlockingQueue<DeliveryTask> queue = queues.get(digitalTwinId);
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
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        // Queue must exist before consumers start, and both before publishEvent() can enqueue.
        queues.computeIfAbsent(digitalTwinId, k -> new LinkedBlockingQueue<>(queueCapacity));
        consumers.computeIfAbsent(digitalTwinId, this::startConsumers);

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.computeIfAbsent(digitalTwinId, k -> new ConcurrentHashMap<>());

        for (String eventType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list =
                    dtMap.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());
            WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
            if (!list.contains(info)) {
                list.add(info);
                listener.onEventSubscribed(eventType);
                if (eventLogger != null)
                    eventLogger.logClientSubscription(eventType, subscriberId);
            } else {
                logger.debug("Subscriber {} already registered for {}", subscriberId, eventType);
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
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.get(digitalTwinId);
        if (dtMap == null) return;

        WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
        for (String eventType : filter) {
            CopyOnWriteArrayList<WldtSubscriberInfo> list = dtMap.get(eventType);
            if (list != null && list.remove(info)) {
                listener.onEventUnSubscribed(eventType);
                if (eventLogger != null)
                    eventLogger.logClientUnSubscription(eventType, subscriberId);
            }
        }
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    @Override
    public void shutdown() {
        consumers.values().forEach(ExecutorService::shutdownNow);
        consumers.clear();
        queues.clear();
        subscriberMap.clear();
    }

    // ── Consumer internals ────────────────────────────────────────────────────

    private ExecutorService startConsumers(String dtId) {
        LinkedBlockingQueue<DeliveryTask> queue = queues.get(dtId);
        ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "wldt-bus-" + dtId);
            t.setDaemon(true);
            return t;
        });
        for (int i = 0; i < threadPoolSize; i++)
            pool.submit(() -> consumerLoop(queue, dtId));
        return pool;
    }

    private void consumerLoop(LinkedBlockingQueue<DeliveryTask> queue, String dtId) {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DeliveryTask task = queue.take();
                dispatch(dtId, task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void dispatch(String dtId, DeliveryTask task) {
        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap =
                subscriberMap.get(dtId);
        if (dtMap == null) return;

        WldtEvent<?> event        = task.event;
        String publisherId        = task.publisherId;
        IWldtEventLogger evtLog   = task.eventLogger;

        // Exact match — subscriber state read at delivery time
        CopyOnWriteArrayList<WldtSubscriberInfo> exact = dtMap.get(event.getType());
        if (exact != null) {
            for (WldtSubscriberInfo info : exact) {
                info.getEventListener().onEvent(event);
                if (evtLog != null) evtLog.logEventForwarded(publisherId, info.getId(), event);
            }
        }

        // Wildcard match
        for (Map.Entry<String, CopyOnWriteArrayList<WldtSubscriberInfo>> entry : dtMap.entrySet()) {
            if (WldtEventFilter.matchWildCardType(event.getType(), entry.getKey())) {
                for (WldtSubscriberInfo info : entry.getValue()) {
                    info.getEventListener().onEvent(event);
                    if (evtLog != null) evtLog.logEventForwarded(publisherId, info.getId(), event);
                }
            }
        }
    }

    // ── DeliveryTask ──────────────────────────────────────────────────────────

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
}

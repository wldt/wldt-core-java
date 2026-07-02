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
 * Per-DT async event delivery strategy.
 *
 * Each Digital Twin gets an independent single-threaded executor that drains
 * its own FIFO event queue.  Publishers never block on subscriber execution —
 * publishEvent() returns after the dispatch task is enqueued.
 *
 * Ordering: strict FIFO per DT (single dispatcher thread per DT).
 * Concurrency: different DTs dispatch fully in parallel.
 * Subscribe / unSubscribe: serialized on the strategy instance (rare path).
 */
public class PerDtAsyncStrategy implements WldtEventBusStrategy {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(PerDtAsyncStrategy.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>>>
            subscriberMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, ExecutorService> executors = new ConcurrentHashMap<>();

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

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap = subscriberMap.get(digitalTwinId);
        if (dtMap == null) return;

        ExecutorService exec = executors.get(digitalTwinId);
        if (exec == null) return;

        // Snapshot subscriber lists in the publisher thread.
        // CopyOnWriteArrayList guarantees an atomic array snapshot via new ArrayList<>().
        CopyOnWriteArrayList<WldtSubscriberInfo> exactList = dtMap.get(event.getType());
        List<WldtSubscriberInfo> exactSnapshot =
                exactList != null ? new ArrayList<>(exactList) : Collections.emptyList();

        List<WldtSubscriberInfo> wildcardSnapshot = new ArrayList<>();
        for (Map.Entry<String, CopyOnWriteArrayList<WldtSubscriberInfo>> entry : dtMap.entrySet()) {
            if (WldtEventFilter.matchWildCardType(event.getType(), entry.getKey()))
                wildcardSnapshot.addAll(entry.getValue());
        }

        if (exactSnapshot.isEmpty() && wildcardSnapshot.isEmpty()) return;

        exec.submit(() -> {
            for (WldtSubscriberInfo info : exactSnapshot) {
                info.getEventListener().onEvent(event);
                if (eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, info.getId(), event);
            }
            for (WldtSubscriberInfo info : wildcardSnapshot) {
                info.getEventListener().onEvent(event);
                if (eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, info.getId(), event);
            }
        });
    }

    @Override
    public synchronized void subscribe(String digitalTwinId, String subscriberId,
                                       WldtEventFilter filter, WldtEventListener listener,
                                       IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-subscribe() -> Error: digitalTwinId = NULL !");

        if (filter == null || listener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        // Executor must be created BEFORE dtMap so publishEvent() never sees dtMap without executor.
        executors.computeIfAbsent(digitalTwinId, this::newDtExecutor);
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

        ConcurrentHashMap<String, CopyOnWriteArrayList<WldtSubscriberInfo>> dtMap = subscriberMap.get(digitalTwinId);
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

    @Override
    public void shutdown() {
        executors.values().forEach(ExecutorService::shutdownNow);
        executors.clear();
        subscriberMap.clear();
    }

    private ExecutorService newDtExecutor(String dtId) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "wldt-bus-" + dtId);
            t.setDaemon(true);
            return t;
        });
    }
}

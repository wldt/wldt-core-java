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

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Default synchronous implementation of WldtEventBusStrategy.
 * Preserves the original WldtEventBus behavior: all three operations are
 * serialized on a single instance-level lock, and onEvent() is called
 * in the publisher's thread while the lock is held.
 */
public class DefaultEventBusStrategy implements WldtEventBusStrategy {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DefaultEventBusStrategy.class);

    private final Map<String, SubscriptionDescriptor> subscriberMap = new HashMap<>();

    @Override
    public synchronized void publishEvent(String digitalTwinId, String publisherId, WldtEvent<?> event, IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");

        if (event == null || event.getType() == null || event.getType().isEmpty())
            throw new EventBusException(String.format(
                    "EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !",
                    event != null ? event.getType() : "null"));

        if (eventLogger != null)
            eventLogger.logEventPublished(publisherId, event);

        Optional<SubscriptionDescriptor> dtSubs = getSubscriptionForDigitalTwin(digitalTwinId);

        if (dtSubs.isPresent() &&
                dtSubs.get().containsKey(event.getType()) &&
                !dtSubs.get().get(event.getType()).isEmpty()) {
            dtSubs.get().get(event.getType()).forEach(info -> {
                info.getEventListener().onEvent(event);
                if (eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, info.getId(), event);
            });
        }

        if (dtSubs.isPresent()) {
            for (String subscriptionEventType : dtSubs.get().keySet()) {
                if (WldtEventFilter.matchWildCardType(event.getType(), subscriptionEventType))
                    dtSubs.get().get(subscriptionEventType).forEach(info -> {
                        info.getEventListener().onEvent(event);
                        if (eventLogger != null)
                            eventLogger.logEventForwarded(publisherId, info.getId(), event);
                    });
            }
        }
    }

    @Override
    public synchronized void subscribe(String digitalTwinId, String subscriberId, WldtEventFilter filter, WldtEventListener listener, IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-subscribe() -> Error: digitalTwinId = NULL !");

        if (filter == null || listener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        if (!subscriberMap.containsKey(digitalTwinId))
            subscriberMap.put(digitalTwinId, new SubscriptionDescriptor());

        for (String eventType : filter) {
            if (!subscriberMap.get(digitalTwinId).containsKey(eventType))
                subscriberMap.get(digitalTwinId).put(eventType, new ArrayList<>());

            WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
            if (!subscriberMap.get(digitalTwinId).get(eventType).contains(info)) {
                subscriberMap.get(digitalTwinId).get(eventType).add(info);
                listener.onEventSubscribed(eventType);
                if (eventLogger != null)
                    eventLogger.logClientSubscription(eventType, subscriberId);
            } else {
                logger.debug("Subscriber {} already registered for {}", subscriberId, eventType);
            }
        }
    }

    @Override
    public synchronized void unSubscribe(String digitalTwinId, String subscriberId, WldtEventFilter filter, WldtEventListener listener, IWldtEventLogger eventLogger) throws EventBusException {

        if (digitalTwinId == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: digitalTwinId = NULL !");

        if (filter == null || listener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        WldtSubscriberInfo info = new WldtSubscriberInfo(subscriberId, listener);
        for (String eventType : filter) {
            List<WldtSubscriberInfo> subscribers = subscriberMap.containsKey(digitalTwinId)
                    ? subscriberMap.get(digitalTwinId).get(eventType)
                    : null;
            if (subscribers != null && subscribers.contains(info)) {
                subscribers.remove(info);
                listener.onEventUnSubscribed(eventType);
                if (eventLogger != null)
                    eventLogger.logClientUnSubscription(eventType, subscriberId);
            }
        }
    }

    private Optional<SubscriptionDescriptor> getSubscriptionForDigitalTwin(String digitalTwinId) {
        return subscriberMap.containsKey(digitalTwinId)
                ? Optional.of(subscriberMap.get(digitalTwinId))
                : Optional.empty();
    }
}

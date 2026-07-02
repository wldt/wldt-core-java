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

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * WLDT Event Bus — public facade for publish/subscribe operations.
 * All dispatch behavior is delegated to the active WldtEventBusStrategy.
 * The default strategy preserves the original synchronous, lock-per-instance behavior.
 * A different strategy can be injected at runtime via setStrategy() without
 * changing any caller (PhysicalAdapter, DigitalAdapter, DigitalTwinWorker, etc.).
 */
public class WldtEventBus {

    private static volatile WldtEventBus instance = null;

    private volatile WldtEventBusStrategy strategy = new DefaultEventBusStrategy();

    private IWldtEventLogger eventLogger = null;

    private WldtEventBus() {}

    /**
     * Returns the singleton WldtEventBus instance (double-checked locking).
     */
    public static WldtEventBus getInstance() {
        if (instance == null) {
            synchronized (WldtEventBus.class) {
                if (instance == null)
                    instance = new WldtEventBus();
            }
        }
        return instance;
    }

    /**
     * Set the Event Logger for the current EventBus instance.
     */
    public void setEventLogger(IWldtEventLogger eventLogger) {
        this.eventLogger = eventLogger;
    }

    /**
     * Swap the dispatch strategy at runtime.
     * The caller is responsible for ensuring no in-flight operations when switching.
     */
    public void setStrategy(WldtEventBusStrategy strategy) {
        if (strategy == null)
            throw new IllegalArgumentException("strategy must not be null");
        WldtEventBusStrategy old = this.strategy;
        this.strategy = strategy;
        old.shutdown();
    }

    /**
     * Publish a new event on the event bus.
     */
    public void publishEvent(String digitalTwinId, String publisherId, WldtEvent<?> wldtEvent) throws EventBusException {
        strategy.publishEvent(digitalTwinId, publisherId, wldtEvent, eventLogger);
    }

    /**
     * Subscribe a client to the event bus.
     */
    public void subscribe(String digitalTwinId, String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException {
        strategy.subscribe(digitalTwinId, subscriberId, wldtEventFilter, wldtEventListener, eventLogger);
    }

    /**
     * Unsubscribe a client from the event bus.
     */
    public void unSubscribe(String digitalTwinId, String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException {
        strategy.unSubscribe(digitalTwinId, subscriberId, wldtEventFilter, wldtEventListener, eventLogger);
    }

    /**
     * Check whether eventType matches the wildcard filterType.
     * Delegates to WldtEventFilter.matchWildCardType — preserved for API compatibility.
     */
    public boolean matchWildCardType(String eventType, String filterType) {
        return WldtEventFilter.matchWildCardType(eventType, filterType);
    }
}

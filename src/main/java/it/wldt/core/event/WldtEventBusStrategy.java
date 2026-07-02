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
 * Strategy interface for WldtEventBus dispatch behavior.
 * Implementations define how events are delivered, subscriptions are managed,
 * and what concurrency model is used — without changing the public API of WldtEventBus.
 */
public interface WldtEventBusStrategy {

    void publishEvent(String digitalTwinId, String publisherId, WldtEvent<?> event, IWldtEventLogger eventLogger) throws EventBusException;

    void subscribe(String digitalTwinId, String subscriberId, WldtEventFilter filter, WldtEventListener listener, IWldtEventLogger eventLogger) throws EventBusException;

    void unSubscribe(String digitalTwinId, String subscriberId, WldtEventFilter filter, WldtEventListener listener, IWldtEventLogger eventLogger) throws EventBusException;

    /** Release any resources held by this strategy (e.g. executor threads). */
    default void shutdown() {}
}

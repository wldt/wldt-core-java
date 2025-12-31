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

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Default Event Logger to print of standard output all the received and sent event passing through the
 * WLDT Event Bus
 */
public class DefaultWldtEventLogger implements IWldtEventLogger {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DefaultWldtEventLogger.class);

    @Override
    public void logEventPublished(String publisherId, WldtEvent<?> wldtEvent) {
        if(wldtEvent != null)
            logger.debug("PUBLISHER [{}] -> PUBLISHED EVENT TYPE: {} Message: {}", publisherId, wldtEvent.getType(), wldtEvent);
        else
            logger.error("PUBLISHER [{}] -> NULL MESSAGE !", publisherId);
    }

    @Override
    public void logEventForwarded(String publisherId, String subscriberId, WldtEvent<?> wldtEvent) {
        if(wldtEvent != null)
            logger.debug("EVENT-BUS -> FORWARDED from PUBLISHER [{}] to SUBSCRIBER [{}] -> TOPIC: {} Message: {}", publisherId, subscriberId, wldtEvent.getType(), wldtEvent);
        else
            logger.error("EVENT-BUS FORWARDING from PUBLISHER [{}] to SUBSCRIBER [{}] -> NULL MESSAGE ! ", publisherId, subscriberId);
    }

    @Override
    public void logClientSubscription(String eventType, String subscriberId) {
        logger.debug("SUBSCRIBER [{}] -> Subscribed Correctly - Event Type: {}", subscriberId, eventType);
    }

    @Override
    public void logClientUnSubscription(String eventType, String subscriberId) {
        logger.debug("SUBSCRIBER [{}] -> UnSubscribed Correctly  - Event Type: {}", subscriberId, eventType);
    }
}

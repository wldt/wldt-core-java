/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
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

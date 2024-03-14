package it.wldt.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(DefaultWldtEventLogger.class);

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

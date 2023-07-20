package it.wldt.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultWldtEventLogger implements IWldtEventLogger {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWldtEventLogger.class);

    @Override
    public void logEventPublished(String publisherId, WldtEvent<?> wldtEvent) {
        if(wldtEvent != null)
            logger.info("PUBLISHER [{}] -> PUBLISHED EVENT TYPE: {} Message: {}", publisherId, wldtEvent.getType(), wldtEvent);
        else
            logger.error("PUBLISHER [{}] -> NULL MESSAGE !", publisherId);
    }

    @Override
    public void logEventForwarded(String publisherId, String subscriberId, WldtEvent<?> wldtEvent) {
        if(wldtEvent != null)
            logger.info("EVENT-BUS -> FORWARDED from PUBLISHER [{}] to SUBSCRIBER [{}] -> TOPIC: {} Message: {}", publisherId, subscriberId, wldtEvent.getType(), wldtEvent);
        else
            logger.error("EVENT-BUS FORWARDING from PUBLISHER [{}] to SUBSCRIBER [{}] -> NULL MESSAGE ! ", publisherId, subscriberId);
    }

    @Override
    public void logClientSubscription(String eventType, String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> Subscribed Correctly - Event Type: {}", subscriberId, eventType);
    }

    @Override
    public void logClientUnSubscription(String eventType, String subscriberId) {
        logger.info("SUBSCRIBER [{}] -> UnSubscribed Correctly  - Event Type: {}", subscriberId, eventType);
    }
}

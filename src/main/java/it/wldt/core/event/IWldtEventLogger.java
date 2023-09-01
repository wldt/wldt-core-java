package it.wldt.core.event;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Interface modeling a WLDT Event Logger to print of standard output all the received and sent event passing
 * through the WLDT Event Bus
 */
public interface IWldtEventLogger {

    public void logEventPublished(String publisherId, WldtEvent<?> wldtEvent);

    public void logEventForwarded(String publisherId, String subscriberId, WldtEvent<?> wldtEvent);

    public void logClientSubscription(String eventType, String subscriberId);

    public void logClientUnSubscription(String eventType, String subscriberId);

}

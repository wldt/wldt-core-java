package it.wldt.core.event;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Listener with the callback to receive notification for subscribe and unsubscribe notifications and
 * when a new WldtEvent has been delivered to a subscriber.
 */
public interface WldtEventListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onEvent(WldtEvent<?> wldtEvent);

}

package it.wldt.core.event;

public interface IWldtEventLogger {

    public void logEventPublished(String publisherId, WldtEvent<?> wldtEvent);

    public void logEventForwarded(String publisherId, String subscriberId, WldtEvent<?> wldtEvent);

    public void logClientSubscription(String eventType, String subscriberId);

    public void logClientUnSubscription(String eventType, String subscriberId);

}

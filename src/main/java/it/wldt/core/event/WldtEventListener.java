package it.wldt.core.event;

public interface WldtEventListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onEvent(WldtEvent<?> wldtEvent);

}

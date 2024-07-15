package it.wldt.core.event.observer;

import it.wldt.core.event.WldtEvent;

public interface IWldtEventObserverListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onStateEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent);

    public void onDigitalActionEvent(WldtEvent<?> wldtEvent);
}

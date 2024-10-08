package it.wldt.core.event.observer;

import it.wldt.core.event.WldtEvent;

/**
 * Interface for the WldtEventObserverListener allowing to listen to the events of the WldtEventObserver
 */
public interface IWldtEventObserverListener {

    public void onEventSubscribed(String eventType);

    public void onEventUnSubscribed(String eventType);

    public void onStateEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent);

    public void onDigitalActionEvent(WldtEvent<?> wldtEvent);

    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent);

    public void onLifeCycleEvent(WldtEvent<?> wldtEvent);

    public void onQueryRequestEvent(WldtEvent<?> wldtEvent);

    public void onQueryResultEvent(WldtEvent<?> wldtEvent);
}

package it.wldt.process.observer;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.observer.IWldtEventObserverListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestObserverListener implements IWldtEventObserverListener {

    private static final Logger logger = LoggerFactory.getLogger(TestObserverListener.class);

    // Physical Asset Events (Properties, Pad, Events)
    private Map<String, List<WldtEvent<?>>> physicalAssetEvents;

    // Physical Action Events
    private Map<String, List<WldtEvent<?>>> physicalActionEvents;

    // Digital Twin State Events
    private Map<String, List<WldtEvent<?>>> dtStateEvents;

    // Digital Action Events
    private Map<String, List<WldtEvent<?>>> digitalActionEvents;

    public TestObserverListener(){
        logger.info("TestObserverListener Constructor Called !");
        init();
    }

    private void init(){

        logger.info("TestObserverListener -> Initializing ...");

        this.physicalAssetEvents = new HashMap<>();
        this.physicalActionEvents = new HashMap<>();
        this.dtStateEvents = new HashMap<>();
        this.digitalActionEvents = new HashMap<>();
    }

    private void resetMetrics(){

        logger.info("TestObserverListener -> Resetting ...");

        this.physicalAssetEvents.clear();
        this.physicalActionEvents.clear();
        this.dtStateEvents.clear();
        this.digitalActionEvents.clear();

        init();
    }

    private void handleNewEvent(Map<String, List<WldtEvent<?>>> targetMap, WldtEvent<?> wldtEvent){

        if(wldtEvent == null || wldtEvent.getType() == null)
            logger.error("Error ! NULL or WRONG WLDT RECEIVED !");
        else{
            if(!targetMap.containsKey(wldtEvent.getType()))
                targetMap.put(wldtEvent.getType(), new ArrayList<>());

            targetMap.get(wldtEvent.getType()).add(wldtEvent);
        }
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("TestObserverListener Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("TestObserverListener UnSubscribed from: {}", eventType);
    }

    @Override
    public void onStateEvent(WldtEvent<?> wldtEvent) {
        handleNewEvent(this.dtStateEvents, wldtEvent);
    }

    @Override
    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent) {
        handleNewEvent(this.physicalAssetEvents, wldtEvent);
    }

    @Override
    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent) {
        handleNewEvent(this.physicalActionEvents, wldtEvent);
    }

    @Override
    public void onDigitalActionEvent(WldtEvent<?> wldtEvent) {
        handleNewEvent(this.digitalActionEvents, wldtEvent);
    }

    public Map<String, List<WldtEvent<?>>> getPhysicalAssetEvents() {
        return physicalAssetEvents;
    }

    public Map<String, List<WldtEvent<?>>> getPhysicalActionEvents() {
        return physicalActionEvents;
    }

    public Map<String, List<WldtEvent<?>>> getDtStateEvents() {
        return dtStateEvents;
    }

    public Map<String, List<WldtEvent<?>>> getDigitalActionEvents() {
        return digitalActionEvents;
    }

}

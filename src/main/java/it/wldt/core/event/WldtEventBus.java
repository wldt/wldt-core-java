package it.wldt.core.event;

import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WldtEventBus {

    private static final Logger logger = LoggerFactory.getLogger(WldtEventBus.class);

    private static WldtEventBus instance = null;
    private Map<String, List<WldtSubscriberInfo>> subscriberMap = null;

    private IWldtEventLogger eventLogger = null;

    private WldtEventBus(){
        this.subscriberMap = new HashMap<>();
    }

    public static WldtEventBus getInstance(){
        if(instance == null)
            instance = new WldtEventBus();
        return instance;
    }

    public void setEventLogger(IWldtEventLogger eventLogger){
        this.eventLogger = eventLogger;
    }

    public void publishEvent(String publisherId, WldtEvent<?> wldtEvent) throws EventBusException {
        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: SubscriberMap = NULL !");

        if(wldtEvent == null || wldtEvent.getType() == null || (wldtEvent.getType() != null && wldtEvent.getType().length() == 0))
            throw new EventBusException(String.format("EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !", wldtEvent != null ? wldtEvent.getType() : "null"));

        if(eventLogger != null)
            eventLogger.logEventPublished(publisherId, wldtEvent);

        if(this.subscriberMap.containsKey(wldtEvent.getType()) && this.subscriberMap.get(wldtEvent.getType()).size() > 0)
            this.subscriberMap.get(wldtEvent.getType()).forEach(wldtSubscriberInfo -> {
                wldtSubscriberInfo.getEventListener().onEvent(wldtEvent);

                if(eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, wldtSubscriberInfo.getId(), wldtEvent);
            });
    }

    public void subscribe(String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-subscribe() -> Error: SubscriberMap = NULL !");

        if(wldtEventFilter == null || wldtEventListener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        for(String eventType: wldtEventFilter) {
            //If required init the ArrayList for target eventyType
            if (!this.subscriberMap.containsKey(eventType))
                this.subscriberMap.put(eventType, new ArrayList<>());

            WldtSubscriberInfo newWldtSubscriberInfo = new WldtSubscriberInfo(subscriberId, wldtEventListener);

            if(!this.subscriberMap.get(eventType).contains(newWldtSubscriberInfo)) {

                this.subscriberMap.get(eventType).add(newWldtSubscriberInfo);
                wldtEventListener.onEventSubscribed(eventType);

                if(eventLogger != null)
                    eventLogger.logClientSubscription(eventType, subscriberId);
            }
            else
                logger.debug("Subscriber {} already registered for {}", subscriberId, eventType);
        }
    }

    public void unSubscribe(String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: SubscriberMap = NULL !");

        if(wldtEventFilter == null || wldtEventListener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        WldtSubscriberInfo wldtSubscriberInfo = new WldtSubscriberInfo(subscriberId, wldtEventListener);

        for(String eventType: wldtEventFilter) {
            if(this.subscriberMap.get(eventType) != null && this.subscriberMap.get(eventType).contains(wldtSubscriberInfo)) {
                this.subscriberMap.get(eventType).remove(wldtSubscriberInfo);
                wldtEventListener.onEventUnSubscribed(eventType);

                if(eventLogger != null)
                    eventLogger.logClientUnSubscription(eventType, subscriberId);
            }
        }
    }

}

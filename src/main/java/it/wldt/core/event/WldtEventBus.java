package it.wldt.core.event;

import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * WLDT Event Bus implementation exposing all the method to publish and subscribe to specific event on an active
 * instance of the WLDT Engine.
 */
public class WldtEventBus {

    private static final Logger logger = LoggerFactory.getLogger(WldtEventBus.class);

    private static WldtEventBus instance = null;

    private Map<String, SubscriptionDescriptor> subscriberMap = null;

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

    /**
     *
     * @param digitalTwinId
     * @return
     */
    private Optional<SubscriptionDescriptor> getSubscriptionForDigitalTwin(String digitalTwinId){
        if(this.subscriberMap.containsKey(digitalTwinId))
            return Optional.of(this.subscriberMap.get(digitalTwinId));
        else
            return Optional.empty();
    }

    public void publishEvent(String digitalTwinId, String publisherId, WldtEvent<?> wldtEvent) throws EventBusException {

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: SubscriberMap = NULL !");

        if(digitalTwinId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");

        if(wldtEvent == null || wldtEvent.getType() == null || (wldtEvent.getType() != null && wldtEvent.getType().length() == 0))
            throw new EventBusException(String.format("EventBus-publishEvent() -> Error: eventMessage = NULL or event-type (%s) is invalid !", wldtEvent != null ? wldtEvent.getType() : "null"));

        if(eventLogger != null)
            eventLogger.logEventPublished(publisherId, wldtEvent);

        Optional<SubscriptionDescriptor> digitalTwinSubscriptionOptional = getSubscriptionForDigitalTwin(digitalTwinId);

        // If there is a registered digital twin with that id and the target eventType among registered subscriptions
        if(this.subscriberMap.containsKey(digitalTwinId) &&
                digitalTwinSubscriptionOptional.isPresent() &&
                digitalTwinSubscriptionOptional.get().containsKey(wldtEvent.getType()) &&
                !digitalTwinSubscriptionOptional.get().get(wldtEvent.getType()).isEmpty()) {

            digitalTwinSubscriptionOptional.get().get(wldtEvent.getType()).forEach(wldtSubscriberInfo -> {
                wldtSubscriberInfo.getEventListener().onEvent(wldtEvent);
                if (eventLogger != null)
                    eventLogger.logEventForwarded(publisherId, wldtSubscriberInfo.getId(), wldtEvent);
            });
        }

        // Wildcard Check
        // If there is a registered digital twin with that id and the target eventType among registered subscriptions using wildcards
        if(this.subscriberMap.containsKey(digitalTwinId) &&
                digitalTwinSubscriptionOptional.isPresent()) {

            // Retrieve the list of Subscribers
            for(String subscriptionEventType : digitalTwinSubscriptionOptional.get().keySet()){
                   if(matchWildCardType(wldtEvent.getType(), subscriptionEventType))
                       digitalTwinSubscriptionOptional.get().get(subscriptionEventType).forEach(wldtSubscriberInfo -> {
                           wldtSubscriberInfo.getEventListener().onEvent(wldtEvent);
                           if (eventLogger != null)
                               eventLogger.logEventForwarded(publisherId, wldtSubscriberInfo.getId(), wldtEvent);
                       });
            }
        }
    }

    private boolean isWildCardType(String filterEventType){
        try{
            if(filterEventType != null){
                String[] eventTypeStructureArray = filterEventType.split("\\.");
                // If the last element of the topic is the multi level wild card
                return eventTypeStructureArray.length > 0 && eventTypeStructureArray[eventTypeStructureArray.length - 1].equals(WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE);
            }
            else
                logger.error("Error checking WildCard Type ! Event Type == NULL!");

            return false;

        }catch (Exception e){
            logger.error("Error checking WildCard Type ! Event Type: {} Error: {} ", filterEventType, e.getLocalizedMessage());
            return false;
        }
    }

    public boolean matchWildCardType(String eventType, String filterType){
        // If the filter type is a wild card filter
        if(isWildCardType(filterType)){
            // Remove ".*" from the filter to match the target event type
            String targetType = filterType.replace(String.format(".%s", WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE), "");
            return eventType.contains(targetType);
        }
        else
            return false;
    }

    public void subscribe(String digitalTwinId, String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-subscribe() -> Error: SubscriberMap = NULL !");

        if(digitalTwinId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");

        if(wldtEventFilter == null || wldtEventListener == null)
            throw new EventBusException("EventBus-subscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        // Check if the DT is registered on the subscription map
        if(!this.subscriberMap.containsKey(digitalTwinId))
            this.subscriberMap.put(digitalTwinId, new SubscriptionDescriptor());

        for(String eventType: wldtEventFilter) {

            //If required init the ArrayList for target eventyType of the target Digital Twin
            if (!this.subscriberMap.get(digitalTwinId).containsKey(eventType))
                this.subscriberMap.get(digitalTwinId).put(eventType, new ArrayList<>());

            WldtSubscriberInfo newWldtSubscriberInfo = new WldtSubscriberInfo(subscriberId, wldtEventListener);

            if(!this.subscriberMap.get(digitalTwinId).get(eventType).contains(newWldtSubscriberInfo)) {

                this.subscriberMap.get(digitalTwinId).get(eventType).add(newWldtSubscriberInfo);
                wldtEventListener.onEventSubscribed(eventType);

                if(eventLogger != null)
                    eventLogger.logClientSubscription(eventType, subscriberId);
            }
            else
                logger.debug("Subscriber {} already registered for {}", subscriberId, eventType);
        }
    }

    public void unSubscribe(String digitalTwinId, String subscriberId, WldtEventFilter wldtEventFilter, WldtEventListener wldtEventListener) throws EventBusException{

        if(this.subscriberMap == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: SubscriberMap = NULL !");

        if(digitalTwinId == null)
            throw new EventBusException("EventBus-publishEvent() -> Error: digitalTwinId = NULL !");

        if(wldtEventFilter == null || wldtEventListener == null)
            throw new EventBusException("EventBus-unSubscribe() -> Error: EventFilter = NULL or EventLister = NULL !");

        WldtSubscriberInfo wldtSubscriberInfo = new WldtSubscriberInfo(subscriberId, wldtEventListener);

        for(String eventType: wldtEventFilter) {
            if(this.subscriberMap.get(digitalTwinId).get(eventType) != null && this.subscriberMap.get(digitalTwinId).get(eventType).contains(wldtSubscriberInfo)) {
                this.subscriberMap.get(digitalTwinId).get(eventType).remove(wldtSubscriberInfo);
                wldtEventListener.onEventUnSubscribed(eventType);

                if(eventLogger != null)
                    eventLogger.logClientUnSubscription(eventType, subscriberId);
            }
        }
    }

}

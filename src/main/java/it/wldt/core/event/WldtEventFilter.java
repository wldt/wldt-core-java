package it.wldt.core.event;

import it.wldt.core.event.observer.WldtEventObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This method allows the creation of a new Filter with the list of event types used by a subscriber to
 * specify which events should be received. It can be used also to cancel a subscription and remove events types
 * that have been previously monitored.
 */
public class WldtEventFilter extends ArrayList<String> {

    private static final Logger logger = LoggerFactory.getLogger(WldtEventFilter.class);

    public boolean matchEventType(String eventType){

        // Check if the filter exactly match the target event type
        if(this.contains(eventType))
            return true;

        // If not check if there is a wildcard filter value to match with the target event type
        for(String filterEventType : this)
            if(matchWildCardType(eventType, filterEventType))
                return true;
        return false;
    }

    private static boolean isWildCardType(String filterEventType){
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

    public static boolean matchWildCardType(String eventType, String filterType){
        // If the filter type is a wild card filter
        if(isWildCardType(filterType)){
            // Remove ".*" from the filter to match the target event type
            String targetType = filterType.replace(String.format(".%s", WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE), "");
            return eventType.contains(targetType);
        }
        else
            return false;
    }
}

/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.core.event;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

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

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(WldtEventFilter.class);

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

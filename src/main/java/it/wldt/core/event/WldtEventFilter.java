/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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

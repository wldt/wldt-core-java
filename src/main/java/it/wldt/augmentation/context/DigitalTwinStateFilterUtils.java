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
package it.wldt.augmentation.context;

import it.wldt.core.state.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods applying the filters defined in an {@link AugmentationFunctionContextRequest}:
 * the trigger filter (which state updates are notified) and the content filter (which part of the
 * Digital Twin State is delivered).
 */
public final class DigitalTwinStateFilterUtils {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateFilterUtils.class);

    private DigitalTwinStateFilterUtils() {
    }

    /**
     * Checks if a state update must be notified according to the trigger filter of the context request.
     * @param contextRequest the context request of the function (null = no filter)
     * @param changeList the changes of the state update (may be null)
     * @return true if no trigger filter is defined, or at least one change matches at least one rule
     */
    public static boolean shouldTrigger(AugmentationFunctionContextRequest contextRequest, List<DigitalTwinStateChange> changeList) {
        if (contextRequest == null || !contextRequest.hasTriggerFilter())
            return true;

        if (changeList == null || changeList.isEmpty())
            return false;

        for (DigitalTwinStateChange change : changeList)
            for (StateChangeFilter rule : contextRequest.getStateChangeFilters())
                if (rule.matches(change))
                    return true;

        return false;
    }

    /**
     * Checks if an event notification must be delivered according to the event name filter of the context request.
     * @param contextRequest the context request of the function (null = no filter)
     * @param notification the notification
     * @return true if no event name filter is defined (null) or the event key is listed
     */
    public static boolean shouldDeliverEventNotification(AugmentationFunctionContextRequest contextRequest, DigitalTwinStateEventNotification<?> notification) {
        if (contextRequest == null || contextRequest.getEventNameFilter() == null)
            return true;

        return notification != null && contextRequest.getEventNameFilter().contains(notification.getDigitalEventKey());
    }

    /**
     * Applies the content filter to a state. Each category (properties, actions, events, relationships) is filtered
     * independently: a null name list keeps the whole category, an empty list removes it.
     * @param state the full state
     * @param contextRequest the context request of the function (null = no filter)
     * @return the same instance if no content filter is defined, otherwise a new partial state
     */
    public static DigitalTwinState filterState(DigitalTwinState state, AugmentationFunctionContextRequest contextRequest) {
        if (state == null || contextRequest == null || !contextRequest.hasContentFilter())
            return state;

        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        Map<String, DigitalTwinStateAction> actions = new HashMap<>();
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        Map<String, DigitalTwinStateRelationship<?>> relationships = new HashMap<>();

        try {
            List<String> propertyFilter = contextRequest.getPropertyNameFilter();
            if (state.getPropertyList().isPresent())
                for (DigitalTwinStateProperty<?> p : state.getPropertyList().get())
                    if (propertyFilter == null || propertyFilter.contains(p.getKey()))
                        properties.put(p.getKey(), p);

            List<String> actionFilter = contextRequest.getActionNameFilter();
            if (state.getActionList().isPresent())
                for (DigitalTwinStateAction a : state.getActionList().get())
                    if (actionFilter == null || actionFilter.contains(a.getKey()))
                        actions.put(a.getKey(), a);

            List<String> eventFilter = contextRequest.getEventNameFilter();
            if (state.getEventList().isPresent())
                for (DigitalTwinStateEvent e : state.getEventList().get())
                    if (eventFilter == null || eventFilter.contains(e.getKey()))
                        events.put(e.getKey(), e);

            List<String> relationshipFilter = contextRequest.getRelationshipNameFilter();
            if (state.getRelationshipList().isPresent())
                for (DigitalTwinStateRelationship<?> r : state.getRelationshipList().get())
                    if (relationshipFilter == null || relationshipFilter.contains(r.getName()))
                        relationships.put(r.getName(), r);

        } catch (Exception e) {
            logger.error("Error while filtering the Digital Twin State, the complete state is returned: {}", e.getMessage());
            return state;
        }

        return new DigitalTwinState(properties, actions, events, relationships);
    }
}

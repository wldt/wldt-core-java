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

import java.util.List;

/**
 * Utility methods applying the filters defined in an {@link AugmentationFunctionContextRequest}:
 * the trigger filter (which state values updates are notified) and the content filter (which part of the
 * Digital Twin State Values is delivered). The Digital Twin State Description is never filtered.
 */
public final class DigitalTwinStateFilterUtils {

    private DigitalTwinStateFilterUtils() {
    }

    /**
     * Checks if a state update must be notified according to the filters of the context request. The trigger filter
     * ({@link DigitalTwinStateChangeFilter} rules) and the content filter ({@link DigitalTwinStateFilter}) are
     * evaluated on each single change: the update is notified if at least one change satisfies both
     * (a missing filter accepts every change). Two different changes, one matching only the trigger filter and the
     * other only the content filter, do not trigger the update.
     * <p>
     * Without change information (null or empty list) the update is not notified if a trigger filter is defined,
     * and it is notified otherwise (the relevance for the content filter cannot be evaluated).
     * @param contextRequest the context request of the function (null = no filter)
     * @param changeList the changes of the state update (may be null)
     * @return true if the update must be notified
     */
    public static boolean shouldTrigger(AugmentationFunctionContextRequest contextRequest, List<DigitalTwinStateChange> changeList) {
        if (contextRequest == null)
            return true;

        boolean triggerFilter = contextRequest.hasTriggerFilter();
        boolean contentFilter = contextRequest.hasContentFilter();

        if (!triggerFilter && !contentFilter)
            return true;

        if (changeList == null || changeList.isEmpty())
            return !triggerFilter;

        DigitalTwinStateFilter stateFilter = contextRequest.toStateFilter();

        for (DigitalTwinStateChange change : changeList) {
            if (contentFilter && !stateFilter.accepts(change))
                continue;
            if (!triggerFilter || matchesAnyRule(contextRequest, change))
                return true;
        }

        return false;
    }

    private static boolean matchesAnyRule(AugmentationFunctionContextRequest contextRequest, DigitalTwinStateChange change) {
        for (DigitalTwinStateChangeFilter rule : contextRequest.getStateChangeFilters())
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
     * Applies the content filter of the context request to the values of the state. Each category (properties,
     * relationships, actions, events) is filtered independently: a null name list keeps the whole category, an
     * empty list removes it. The description is never filtered.
     * @param values the complete values
     * @param contextRequest the context request of the function (null = no filter)
     * @return the same instance if no content filter is defined, otherwise a new filtered values object
     */
    public static DigitalTwinStateValues filterValues(DigitalTwinStateValues values, AugmentationFunctionContextRequest contextRequest) {
        if (values == null || contextRequest == null || !contextRequest.hasContentFilter())
            return values;

        return applyFilter(values, contextRequest.toStateFilter());
    }

    /**
     * Applies a generic content filter to the values of the state. Each category (properties, relationships,
     * actions, events) is filtered independently: a null name list keeps the whole category, an empty list removes it.
     * @param values the complete values
     * @param filter the content filter (null = no filter)
     * @return the same instance if no filter is active, otherwise a new filtered values object
     */
    public static DigitalTwinStateValues applyFilter(DigitalTwinStateValues values, DigitalTwinStateFilter filter) {
        return values == null ? null : values.filter(filter);
    }
}

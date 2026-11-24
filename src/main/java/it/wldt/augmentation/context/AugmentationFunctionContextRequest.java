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

import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateChangeFilter;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateFilter;
import it.wldt.core.state.DigitalTwinStateValues;
import it.wldt.storage.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the request for the context of an Augmentation Function execution, allowing the specification
 * of observation preferences and query requests.
 * It provides options to observe the state of the system and event notifications, as well as the filters that select
 * which part of the Digital Twin State is delivered to the function and which state updates are notified.
 * Additionally, it allows the inclusion of a Query to retrieve necessary data from
 * the storage system during the execution of the augmentation function.
 * The class is designed to be flexible, enabling augmentation functions to customize their context based on their
 * specific needs and requirements.
 * <p>
 * <b>Filters:</b> two different, unrelated filters can be set (the Digital Twin State Description is never filtered):
 * <ul>
 *     <li>the <i>content filter</i> ({@link DigitalTwinStateFilter}, set with {@link #withStateFilter(DigitalTwinStateFilter)}):
 *     selects, by name, which properties, actions, events and relationships are delivered to the function. It is the same
 *     class used by the Digital Adapters;</li>
 *     <li>the <i>trigger filter</i> (one or more {@link DigitalTwinStateChangeFilter} rules, set with
 *     {@link #withStateChangeFilters(DigitalTwinStateChangeFilter...)}): selects which changes of the state notify the function.</li>
 * </ul>
 */
public class AugmentationFunctionContextRequest {

    /**
     * Boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * The default value is true, but it can be set to false if the augmentation function want to observe
     * only the event notifications and not the state of the system.
     */
    private boolean observeState;

    /**
    * Boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
    * The default value is true, but it can be set to false if the augmentation function want to observe
    * only the state of the system and not the event notifications.
    */
    private boolean observeEventNotifications;

    /**
     * Content filter: selects which part of the Digital Twin State values is delivered to the function (and, through
     * its event names, which event notifications). The default accepts everything.
     */
    private DigitalTwinStateFilter stateFilter = DigitalTwinStateFilter.all();

    /**
     * Trigger filter (stateful functions only): a state update is notified to the function only if at least one of
     * its changes matches at least one of these rules (and, if a content filter is defined, concerns an accepted resource). Null or empty (default) = every state update is notified.
     */
    private List<DigitalTwinStateChangeFilter> stateChangeFilters = null;

    /**
     * The Query associate to the augmentation function context,
     * it can be used by the augmentation function to query the storage system and retrieve the
     * data needed for the execution of the augmentation function.
     */
    private Query query = null;

    /**
     * If true, the handler executes the {@link #query} and delivers its result to the function
     * ({@code onQueryResultRefresh}) before every Digital Twin state update notified to the function.
     * It is disabled by default: it runs a synchronous query on the handler thread for each state update,
     * which can be expensive for heavy queries, and a function whose results modify the Digital Twin state can
     * end up in a feedback loop. When disabled, the function decides when to refresh the query result
     * by calling {@code refreshQueryResult()} itself.
     */
    private boolean refreshQueryOnStateUpdate = false;

    /**
     * Default constructor of the AugmentationFunctionContextRequest class.
     * In this configuration, the augmentation function will observe both the state of the system and the event notifications,
     * and it will not apply any filter on the observed state, event notifications and relationships.
     * Furthermore, the query request is set to null, so the augmentation function will
     * not query the storage system during the execution.
     */
    public AugmentationFunctionContextRequest(){
        this(true, true, null);
    }

    /**
     * Constructor of the AugmentationFunctionContextRequest class with the possibility to specify the query request.
     * In this configuration, the augmentation function will observe both the state of the system and the event notifications,
     * and it will not apply any filter on the observed state, event notifications and relationships.
     * The query request is set to the value passed as parameter, so the augmentation function will
     * be able to query the storage system during the execution using the specified query request.
     * @param query The Query associate to the augmentation function context.
     */
    public AugmentationFunctionContextRequest(Query query){
        this(true, true, query);
    }

    /**
     * Constructor of the AugmentationFunctionContextRequest class with the possibility to specify the observation configuration.
     * In this configuration, the augmentation function will observe the state of the system and the event notifications according to the values passed as parameters,
     * and it will not apply any filter on the observed state, event notifications and relationships.
     * The query request allows the augmentation function to query the storage system during the execution using the specified query request.
     * @param observeState Boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * @param observeEventNotifications Boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     * @param query The Query associate to the augmentation function context.
     */
    public AugmentationFunctionContextRequest(boolean observeState,
                                              boolean observeEventNotifications,
                                              Query query){
        this.observeState = observeState;
        this.observeEventNotifications = observeEventNotifications;
        this.query = query;
    }

    /**
     * @return true if the query is executed and its result delivered to the function before every state update
     * (default false)
     */
    public boolean isRefreshQueryOnStateUpdate() {
        return refreshQueryOnStateUpdate;
    }

    /**
     * Enable or disable the automatic execution of the query before every state update (disabled by default).
     * Only has effect if a query is set in this context request.
     * @param refreshQueryOnStateUpdate true to refresh the query result on every state update
     */
    public void setRefreshQueryOnStateUpdate(boolean refreshQueryOnStateUpdate) {
        this.refreshQueryOnStateUpdate = refreshQueryOnStateUpdate;
    }

    /**
     * Get the Query associate to the augmentation function context.
     * @return The Query associate to the augmentation function context.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Set the Query associate to the augmentation function context.
     * @param query The Query associate to the augmentation function context.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * Get the boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * @return The boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     */
    public boolean isObserveState() {
        return observeState;
    }

    /**
     * Set the boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * @param observeState The boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     */
    public void setObserveState(boolean observeState) {
        this.observeState = observeState;
    }

    /**
     * Set the boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     * @param observeEventNotifications The boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     */
    public void setObserveEventNotifications(boolean observeEventNotifications) {
        this.observeEventNotifications = observeEventNotifications;
    }

    /**
     * Get the boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     * @return The boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     */
    public boolean isObserveEventNotifications() {
        return observeEventNotifications;
    }

    /**
     * @return the content filter of this request (never null, it accepts everything if no filter has been set)
     */
    public DigitalTwinStateFilter getStateFilter() {
        return stateFilter;
    }

    /**
     * @return the names of the properties delivered to the function (null = all)
     */
    public List<String> getPropertyNameFilter() {
        return stateFilter.getPropertyNames();
    }

    /**
     * @return the names of the actions delivered to the function (null = all)
     */
    public List<String> getActionNameFilter() {
        return stateFilter.getActionNames();
    }

    /**
     * @return the names of the events / event notifications delivered to the function (null = all)
     */
    public List<String> getEventNameFilter() {
        return stateFilter.getEventNames();
    }

    /**
     * @return the names of the relationships delivered to the function (null = all)
     */
    public List<String> getRelationshipNameFilter() {
        return stateFilter.getRelationshipNames();
    }

    /**
     * @return the trigger rules (null or empty = every state update is notified)
     */
    public List<DigitalTwinStateChangeFilter> getStateChangeFilters() {
        return stateChangeFilters;
    }

    /**
     * Set the content filter: it selects which properties, actions, events and relationships (by name) are delivered
     * to the function. It has a double role: it selects the content delivered to the function and, for
     * stateful functions, a state update is notified only if at least one of its changes concerns an accepted resource
     * (when a trigger filter is also defined, the same change must satisfy both). The names of the events also select the
     * event notifications delivered to the function. The Digital Twin State Description is never filtered.
     * A new call replaces the previous filter.
     * @param stateFilter the content filter, null is equivalent to {@link DigitalTwinStateFilter#all()}
     * @return this request
     */
    public AugmentationFunctionContextRequest withStateFilter(DigitalTwinStateFilter stateFilter) {
        this.stateFilter = stateFilter != null ? stateFilter : DigitalTwinStateFilter.all();
        return this;
    }

    /**
     * Set the trigger rules: the function is notified only of state updates containing a change matching
     * at least one rule. Calling it without rules removes the filter.
     * @param rules the trigger rules
     * @return this request
     */
    public AugmentationFunctionContextRequest withStateChangeFilters(DigitalTwinStateChangeFilter... rules) {
        this.stateChangeFilters = (rules == null || rules.length == 0) ? null
                : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(rules)));
        return this;
    }

    /**
     * @return true if the content filter restricts at least one category (properties, actions, events, relationships)
     */
    public boolean hasContentFilter() {
        return stateFilter.isActive();
    }

    /**
     * @return true if a trigger filter is defined
     */
    public boolean hasTriggerFilter() {
        return stateChangeFilters != null && !stateChangeFilters.isEmpty();
    }

    /**
     * Checks if a state values update must be notified to the function according to the filters of this request.
     * The trigger filter ({@link DigitalTwinStateChangeFilter} rules) and the content filter ({@link DigitalTwinStateFilter}) are
     * evaluated on each single change: the update is notified if at least one change satisfies both
     * (a missing filter accepts every change). Two different changes, one matching only the trigger filter and the
     * other only the content filter, do not trigger the update.
     * <p>
     * Without change information (null or empty list) the update is not notified if a trigger filter is defined,
     * and it is notified otherwise (the relevance for the content filter cannot be evaluated).
     * @param changeList the changes of the state update (may be null)
     * @return true if the update must be notified
     */
    public boolean shouldTrigger(List<DigitalTwinStateChange> changeList) {

        boolean triggerFilter = hasTriggerFilter();
        boolean contentFilter = hasContentFilter();

        if (!triggerFilter && !contentFilter)
            return true;

        if (changeList == null || changeList.isEmpty())
            return !triggerFilter;

        for (DigitalTwinStateChange change : changeList) {
            if (contentFilter && !stateFilter.accepts(change))
                continue;
            if (!triggerFilter || matchesAnyRule(change))
                return true;
        }

        return false;
    }

    private boolean matchesAnyRule(DigitalTwinStateChange change) {
        for (DigitalTwinStateChangeFilter rule : stateChangeFilters)
            if (rule.matches(change))
                return true;
        return false;
    }

    /**
     * Checks if an event notification must be delivered to the function according to the event names of the
     * content filter.
     * @param notification the notification
     * @return true if the content filter does not restrict the events or the event key is listed
     */
    public boolean acceptsEventNotification(DigitalTwinStateEventNotification<?> notification) {
        if (stateFilter.getEventNames() == null)
            return true;

        return notification != null && stateFilter.acceptsEvent(notification.getDigitalEventKey());
    }

    /**
     * Applies the content filter of this request to the complete values of the state. Each category (properties,
     * relationships, actions, events) is filtered independently: a null name list keeps the whole category, an
     * empty list removes it. The description is never filtered.
     * @param values the complete values
     * @return the same instance if no content filter is defined, otherwise a new filtered values object
     */
    public DigitalTwinStateValues filterValues(DigitalTwinStateValues values) {
        return values == null ? null : values.filter(stateFilter);
    }

    @Override
    public String toString() {
        return "AugmentationFunctionContextRequest{" + "observeState=" + observeState +
                ", observeEventNotifications=" + observeEventNotifications +
                ", stateFilter=" + stateFilter +
                ", stateChangeFilters=" + stateChangeFilters +
                ", query=" + query +
                ", refreshQueryOnStateUpdate=" + refreshQueryOnStateUpdate +
                '}';
    }
}

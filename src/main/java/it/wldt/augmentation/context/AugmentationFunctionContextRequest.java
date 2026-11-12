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

import it.wldt.storage.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the request for the context of an Augmentation Function execution, allowing the specification
 * of observation preferences and query requests.
 * It provides options to observe the state of the system and event notifications, as well as filters for properties,
 * events, and relationships. Additionally, it allows the inclusion of a Query to retrieve necessary data from
 * the storage system during the execution of the augmentation function.
 * The class is designed to be flexible, enabling augmentation functions to customize their context based on their
 * specific needs and requirements.
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

    // Content filters: select which part of the Digital Twin State is delivered to the function.
    // For each category null = no filter (everything is delivered), an empty list = nothing is delivered.

    /**
     * Names of the properties delivered to the function. Null (default) = all the properties.
     */
    private List<String> propertyNameFilter = null;

    /**
     * Names of the actions delivered to the function. Null (default) = all the actions.
     */
    private List<String> actionNameFilter = null;

    /**
     * Names of the events delivered to the function, both in the state and as event notifications.
     * Null (default) = all the events and notifications.
     */
    private List<String> eventNameFilter = null;

    /**
     * Names of the relationships delivered to the function. Null (default) = all the relationships.
     */
    private List<String> relationshipNameFilter = null;

    /**
     * Trigger filter (stateful functions only): a state update is notified to the function only if at least one of
     * its changes matches at least one of these rules. Null or empty (default) = every state update is notified.
     */
    private List<StateChangeFilter> stateChangeFilters = null;

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
        this(true, true, null, null, null, null);
    }

    /**
     * Constructor of the AugmentationFunctionContextRequest class with the possibility to specify the observation
     * configuration and query request management.
     * @param observeState Boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * @param observeEventNotifications Boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     * @param propertyNameFilter List of property names to filter the observed state of the system. If the list is empty or null, all the properties are observed.
     * @param eventNameFilter List of event notifications names to filter the observed event notifications. If the list is empty or null, all the event notifications are observed.
     * @param relationshipNameFilter List of relationship names to filter the observed relationships. If the list is empty or null, all the relationships are observed.
     * @param query The Query associate to the augmentation function context, it can be used by the augmentation function to query the storage system and retrieve the data needed for the execution of the augmentation function.
     */
    private AugmentationFunctionContextRequest(boolean observeState,
                                              boolean observeEventNotifications,
                                              List<String> propertyNameFilter,
                                              List<String> eventNameFilter,
                                              List<String> relationshipNameFilter,
                                              Query query){
        this.observeState = observeState;
        this.observeEventNotifications = observeEventNotifications;
        this.propertyNameFilter = propertyNameFilter;
        this.eventNameFilter = eventNameFilter;
        this.relationshipNameFilter = relationshipNameFilter;
        this.query = query;
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
        this(true, true, null, null, null, query);
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
        this(observeState, observeEventNotifications, null, null, null, query);
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
     * @return the names of the properties delivered to the function (null = all)
     */
    public List<String> getPropertyNameFilter() {
        return propertyNameFilter;
    }

    /**
     * @return the names of the actions delivered to the function (null = all)
     */
    public List<String> getActionNameFilter() {
        return actionNameFilter;
    }

    /**
     * @return the names of the events / event notifications delivered to the function (null = all)
     */
    public List<String> getEventNameFilter() {
        return eventNameFilter;
    }

    /**
     * @return the names of the relationships delivered to the function (null = all)
     */
    public List<String> getRelationshipNameFilter() {
        return relationshipNameFilter;
    }

    /**
     * @return the trigger rules (null or empty = every state update is notified)
     */
    public List<StateChangeFilter> getStateChangeFilters() {
        return stateChangeFilters;
    }

    /**
     * Restrict the properties delivered to the function.
     * @param propertyNames the property names, null to remove the filter, empty to deliver no property
     * @return this request
     */
    public AugmentationFunctionContextRequest withPropertyNameFilter(String... propertyNames) {
        this.propertyNameFilter = copyNames(propertyNames);
        return this;
    }

    /**
     * Restrict the actions delivered to the function.
     * @param actionNames the action names, null to remove the filter, empty to deliver no action
     * @return this request
     */
    public AugmentationFunctionContextRequest withActionNameFilter(String... actionNames) {
        this.actionNameFilter = copyNames(actionNames);
        return this;
    }

    /**
     * Restrict the events (and the event notifications) delivered to the function.
     * @param eventNames the event names, null to remove the filter, empty to deliver no event
     * @return this request
     */
    public AugmentationFunctionContextRequest withEventNameFilter(String... eventNames) {
        this.eventNameFilter = copyNames(eventNames);
        return this;
    }

    /**
     * Restrict the relationships delivered to the function.
     * @param relationshipNames the relationship names, null to remove the filter, empty to deliver no relationship
     * @return this request
     */
    public AugmentationFunctionContextRequest withRelationshipNameFilter(String... relationshipNames) {
        this.relationshipNameFilter = copyNames(relationshipNames);
        return this;
    }

    /**
     * Set the trigger rules: the function is notified only of state updates containing a change matching
     * at least one rule. Calling it without rules removes the filter.
     * @param rules the trigger rules
     * @return this request
     */
    public AugmentationFunctionContextRequest withStateChangeFilters(StateChangeFilter... rules) {
        this.stateChangeFilters = (rules == null || rules.length == 0) ? null
                : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(rules)));
        return this;
    }

    /**
     * @return true if at least one content filter (property, action, event, relationship names) is defined
     */
    public boolean hasContentFilter() {
        return propertyNameFilter != null || actionNameFilter != null
                || eventNameFilter != null || relationshipNameFilter != null;
    }

    /**
     * @return true if a trigger filter is defined
     */
    public boolean hasTriggerFilter() {
        return stateChangeFilters != null && !stateChangeFilters.isEmpty();
    }

    private static List<String> copyNames(String[] names) {
        return names == null ? null : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(names)));
    }

    @Override
    public String toString() {
        return "AugmentationFunctionContextRequest{" + "observeState=" + observeState +
                ", observeEventNotifications=" + observeEventNotifications +
                ", propertyNameFilter=" + propertyNameFilter +
                ", actionNameFilter=" + actionNameFilter +
                ", eventNameFilter=" + eventNameFilter +
                ", relationshipNameFilter=" + relationshipNameFilter +
                ", stateChangeFilters=" + stateChangeFilters +
                ", query=" + query +
                ", refreshQueryOnStateUpdate=" + refreshQueryOnStateUpdate +
                '}';
    }
}

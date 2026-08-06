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

import it.wldt.storage.query.QueryRequest;

import java.util.List;

/**
 * This class represents the request for the context of an Augmentation Function execution, allowing the specification
 * of observation preferences and query requests.
 * It provides options to observe the state of the system and event notifications, as well as filters for properties,
 * events, and relationships. Additionally, it allows the inclusion of a QueryRequest to retrieve necessary data from
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

    // TODO At the moment this filter are not used but in the future they can be used to filter the observed
    //  state, event and relationship by name or type

    /**
     * List of property names to filter the observed state of the system.
     * If the list is empty or null, all the properties are observed.
     */
    private List<String> propertyNameFilter = null;

    /**
     * List of event notifications names to filter the observed event notifications.
     * If the list is empty or null, all the event notifications are observed.
     */
    private List<String> eventNameFilter = null;

    /**
     * List of relationship names to filter the observed relationships.
     * If the list is empty or null, all the relationships are observed.
     */
    private List<String> relationshipNameFilter = null;

    /**
     * The Query Request associate to the augmentation function context,
     * it can be used by the augmentation function to query the storage system and retrieve the
     * data needed for the execution of the augmentation function.
     */
    private QueryRequest queryRequest = null;

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
     * @param queryRequest The Query Request associate to the augmentation function context, it can be used by the augmentation function to query the storage system and retrieve the data needed for the execution of the augmentation function.
     */
    private AugmentationFunctionContextRequest(boolean observeState,
                                              boolean observeEventNotifications,
                                              List<String> propertyNameFilter,
                                              List<String> eventNameFilter,
                                              List<String> relationshipNameFilter,
                                              QueryRequest queryRequest){
        this.observeState = observeState;
        this.observeEventNotifications = observeEventNotifications;
        this.propertyNameFilter = propertyNameFilter;
        this.eventNameFilter = eventNameFilter;
        this.relationshipNameFilter = relationshipNameFilter;
        this.queryRequest = queryRequest;
    }

    /**
     * Constructor of the AugmentationFunctionContextRequest class with the possibility to specify the query request.
     * In this configuration, the augmentation function will observe both the state of the system and the event notifications,
     * and it will not apply any filter on the observed state, event notifications and relationships.
     * The query request is set to the value passed as parameter, so the augmentation function will
     * be able to query the storage system during the execution using the specified query request.
     * @param queryRequest The Query Request associate to the augmentation function context.
     */
    public AugmentationFunctionContextRequest(QueryRequest queryRequest){
        this(true, true, null, null, null, queryRequest);
    }

    /**
     * Constructor of the AugmentationFunctionContextRequest class with the possibility to specify the observation configuration.
     * In this configuration, the augmentation function will observe the state of the system and the event notifications according to the values passed as parameters,
     * and it will not apply any filter on the observed state, event notifications and relationships.
     * The query request allows the augmentation function to query the storage system during the execution using the specified query request.
     * @param observeState Boolean flag to specify if the augmentation function want to observe the state of the system during the execution.
     * @param observeEventNotifications Boolean flag to specify if the augmentation function want to observe the event notifications during the execution.
     * @param queryRequest The Query Request associate to the augmentation function context.
     */
    public AugmentationFunctionContextRequest(boolean observeState,
                                              boolean observeEventNotifications,
                                              QueryRequest queryRequest){
        this(true, true, null, null, null, queryRequest);
    }

    /**
     * Get the Query Request associate to the augmentation function context.
     * @return The Query Request associate to the augmentation function context.
     */
    public QueryRequest getQueryRequest() {
        return queryRequest;
    }

    /**
     * Set the Query Request associate to the augmentation function context.
     * @param queryRequest The Query Request associate to the augmentation function context.
     */
    public void setQueryRequest(QueryRequest queryRequest) {
        this.queryRequest = queryRequest;
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
     * Get the list of property names to filter the observed state of the system.
     * If the list is empty or null, all the properties are observed.
     * @return The list of property names to filter the observed state of the system.
     */
    public List<String> getPropertyNameFilter() {
        return propertyNameFilter;
    }

    /**
     * Get the list of event notifications names to filter the observed event notifications.
     * If the list is empty or null, all the event notifications are observed.
     * @return The list of event notifications names to filter the observed event notifications.
     */
    public List<String> getEventNameFilter() {
        return eventNameFilter;
    }

    /**
     * Get the list of relationship names to filter the observed relationships.
     * If the list is empty or null, all the relationships are observed.
     * @return The list of relationship names to filter the observed relationships.
     */
    public List<String> getRelationshipNameFilter() {
        return relationshipNameFilter;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionContextRequest{" + "observeState=" + observeState +
                ", observeEventNotifications=" + observeEventNotifications +
                ", propertyNameFilter=" + propertyNameFilter +
                ", eventNameFilter=" + eventNameFilter +
                ", relationshipNameFilter=" + relationshipNameFilter +
                ", queryRequest=" + queryRequest +
                '}';
    }
}

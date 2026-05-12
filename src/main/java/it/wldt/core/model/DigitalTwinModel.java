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
package it.wldt.core.model;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.augmentation.*;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.event.*;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.request.AugmentationFunctionRequestType;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.event.*;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.exception.EventBusException;
import it.wldt.exception.KernelException;
import it.wldt.adapter.physical.event.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.management.ResourceManager;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;
import it.wldt.storage.StorageManager;

import java.util.*;

import it.wldt.core.model.annotation.ShadowingFunction;
import it.wldt.core.model.annotation.ShadowingType;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Core class for Digital Twin modeling, event processing together with lifecycle and behavior management.
 * <p>
 * The {@code DigitalTwinModel} serves as the central component in shaping Digital Twin behavior,
 * managing the Digital Twin's execution, state, and interactions between physical and digital worlds.
 * It orchestrates the {@link ShadowingFunction} (modeled throudh a combination of dedicated methods)
 * that defines the DT's behavioral logic while coordinating core components including Physical Adapters,
 * Digital Adapters, State Management, and Storage.
 * Core Responsibilities:
 *
 * - Shadowing Function Orchestration: Manages the shadowing process (replication and
 *   digitalization) that keeps the Digital Twin State synchronized with the physical asset
 * - Physical Asset Description Management: Coordinates Physical Adapters and their description
 * - Digital Twin State Management: Maintains the canonical state representation
 * - Storage Management: Coordinates persistent storage of Digital Twin state evolution,
 *   events, and lifecycle data through the Storage Manager
 * - Resource Management: Handles Digital Twin resources, relationships, and their
 *   lifecycle through the Resource Manager
 * - Augmentation Manager: Handles Digital Twin Augmentation Functions through the evolution
 *   of DT life cycle and within its behavior
 * - Event Routing: Routes events between physical and digital sides through the Shadowing Function
 * - Lifecycle Management: Controls initialization, execution, and termination
 * - Augmentation Function Management: Manages augmentation function execution and results,
 *   allowing the Model to react to changes in the available Augmentation Functions and
 *   their execution results
 *
 * @see ShadowingFunction
 * @see DigitalTwinStateManager
 * @see StorageManager
 * @see ResourceManager
 * @see AugmentationManager
 */
public abstract class DigitalTwinModel implements WldtEventListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinModel.class);

    private String id = null;

    /**
     * Event Filter used to manage observer Augmentation Function
     */
    private WldtEventFilter augmentationFunctionResultEventsFilter = null;

    /**
     * Event Filter used to manage the subscription to the Physical Events
     */
    private WldtEventFilter physicalEventsFilter = null;

    /**
     * Reference to the Digital Twin State Manager
     */
    protected DigitalTwinStateManager digitalTwinStateManager = null;

    /**
     * Reference to the Storage Manager
     */
    protected StorageManager storageManager = null;

    /**
     * Reference to the Resource Manager
     */
    protected ResourceManager resourceManager = null;

    /**
     * Reference to the Shadowing Model Listener
     */
    private ShadowingModelListener shadowingModelListener;

    /**
     * Reference to the Augmentation Manager of the Digital Twin Engine,
     * used to execute Augmentation Functions from the Model
     */
    private AugmentationManager augmentationManager;

    /**
     * Reference to the Monitoring Interface in order to add metrics
     * related to the Models and its behavior
     */
    private MonitoringInterface monitoringInterface;

    /**
     * DT Model Metrics Namespace
     */
    private String metricsNamespace;

    /**
     * Default Constructor
     * @param id Unique Identifier of the Digital Twin Model
     */
    public DigitalTwinModel(String id){
        this.id = id;
        this.physicalEventsFilter = new WldtEventFilter();
        this.augmentationFunctionResultEventsFilter = new WldtEventFilter();
    }

    /**
     * Initialize the Digital Twin Model with the current Digital Twin State Manager
     * @param digitalTwinStateManager DigitalTwinStateManager instance
     */
    protected void init(DigitalTwinStateManager digitalTwinStateManager,
                        StorageManager storageManager,
                        ResourceManager resourceManager){
        this.digitalTwinStateManager = digitalTwinStateManager;
        this.storageManager = storageManager;
        this.resourceManager = resourceManager;

        // Notify Digital Twin Model implementation about the creation of the Model to let
        // it execute custom logic (e.g., observe specific Physical Asset Properties or Events, etc.)
        this.onCreate();
    }

    /**
     * TODO ...
     */
    private void handleMetricsRegistration() {

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){

            // Build metric namespace
            this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();

            // Register Counter Metric(s) - PA Property Variation
            this.monitoringInterface.registerMetric(new WldtCounter(this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - PA Property Variation
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));

            // Register Counter Metric(s) - PA Event Notification
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - PA Event Notification
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));

            // Register Counter Metric(s) - PA Relationship Created
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - PA Relationship Created
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));

            // Register Counter Metric(s) - PA Relationship DELETED
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - PA Relationship Created
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));

            // Register Counter Metric(s) - Digital Action Request
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - Digital Action Request
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));

            // Register Counter Metrics(s) - Augmentation Function Result
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_RESULT_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_RESULT_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function Result
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_RESULT_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));

            // Register Counter Metric(s) - Augmentation Function Registration
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REGISTERED_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REGISTERED_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function Registration
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REGISTERED_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));

            // Register Counter Metric(s) - Augmentation Function Un-Registration
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_UNREGISTERED_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_UNREGISTERED_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function Un-Registration
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_UNREGISTERED_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));

            // Register Counter Metric(s) - Augmentation Function Request
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REQUEST_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REQUEST_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function Request
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_REQUEST_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));

            // Register Counter Metric(s) - Augmentation Function Error
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_ERROR_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_ERROR_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function Error
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_ERROR_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));

            // Register Counter Metric(s) - Augmentation Function List Available
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_LIST_REGISTERED_SUCCESS_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_LIST_REGISTERED_ERROR_COUNT,
                    WldtMetricComponent.AUGMENTATION));

            // Register Execution Time Metrics - Augmentation Function List Available

            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinStateManager.getDigitalTwinId(),
                    this.metricsNamespace,
                    CoreMonitoringUtils.AF_LIST_REGISTERED_EXEC_TIME,
                    WldtMetricComponent.AUGMENTATION));
        }

    }

    /**
     * Method called by the Kernel to trigger specific logic to be executed at the
     * start of the Model
     */
    protected void start(){

        // Handle Metrics Registration for the DT Model
        handleMetricsRegistration();

        // Start handling Augmentation Functions (e.g., observe Augmentation Function Registration Events, etc.)
        this.startHandlingAugmentationFunction();

        // Notify Digital Twin Model implementation about the start of the Model to let it execute
        // custom logic (e.g., observe specific Physical Asset Properties or Events, etc.)
        this.onStart();

        // Notify the Model about already registered Augmentation Functions to let it execute
        // custom logic (e.g., execute specific Augmentation Functions, etc.)
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers())
            this.handleAugmentationFunctionListAvailable(augmentationFunctionHandler.getId(), augmentationFunctionHandler.getAllAugmentationFunctions());
    }

    /**
     * Method called by the Kernel to trigger specific logic to be executed at the stop of the Model
     */
    protected void stop(){
        // Stop handling Augmentation Functions (e.g., un-observe Augmentation Function Registration Events, etc.)
        this.stopHandlingAugmentationFunction();

        // Notify Digital Twin Model implementation about the stop of the Model to let it
        // execute custom logic (e.g., un-observe specific Physical Asset Properties or Events, etc.)
        this.onStop();
    }

    /**
     * Initialize the Digital Twin Model with the current Digital Twin State Manager
     * @param digitalTwinStateManager DigitalTwinStateManager instance
     */
    protected void init(DigitalTwinStateManager digitalTwinStateManager,
                        StorageManager storageManager,
                        ResourceManager resourceManager,
                        AugmentationManager augmentationManager){

        this.digitalTwinStateManager = digitalTwinStateManager;
        this.storageManager = storageManager;
        this.resourceManager = resourceManager;
        this.augmentationManager = augmentationManager;

        // Notify Digital Twin Model implementation about the creation of the Model to let
        // it execute custom logic (e.g., observe specific Physical Asset Properties or Events, etc.)
        this.onCreate();
    }

    /**
     * Method to handle the Augmentation Function related logic at the start of the Model
     */
    protected void startHandlingAugmentationFunction(){

        try{
            // Observe Augmentation Function Registration and Un-Registration Events to keep the Model
            // updated about the available Augmentation Functions
            this.observeAugmentationFunctionRegistrationEvents();

            // In this case since we have the Augmentation Manager -> Observe Aug. Function Results
            this.observeAugmentationFunctionResults();

            // Observe Augmentation Function Error Events
            this.observeAugmentationFunctionErrorEvents();

        }catch(Exception e){
            logger.error("Error Handling Augmentation Function Results ! Error: {}", e.getMessage());
        }
    }

    /**
     * Method to handle the Augmentation Function related logic at the stop of the Model
     */
    protected void stopHandlingAugmentationFunction(){

        try{
            // Un-Observe Augmentation Function Registration and Un-Registration Events to keep the Model
            // updated about the available Augmentation Functions
            this.unObserveAugmentationFunctionRegistrationEvents();

            // In this case since we have the Augmentation Manager -> Un-Observe Aug. Function Results
            this.unObserveAugmentationFunctionResults();

            // Un-Observe Augmentation Function Error Events
            this.unObserveAugmentationFunctionErrorEvents();

        }catch(Exception e){
            logger.error("Error Un-Handling Augmentation Function Results ! Error: {}", e.getMessage());
        }
    }

    /**
     * Build the Event Type to observe all the Augmentation Function Result Events related to the Model
     */
    private String buildResultWildCardEventType(){
        return String.format("%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE, WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE);
    }

    /**
     * Method to observe the Augmentation Function Registration and Un-Registration Events to keep the Model updated about the available Augmentation Functions
     */
    protected void observeAugmentationFunctionRegistrationEvents() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE);
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT_TYPE);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Observer for Augmentation Function Results Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);

    }

    /**
     * Method to un-observe the Augmentation Function Registration and Un-Registration Events to keep the Model updated about the available Augmentation Functions
     */
    protected void unObserveAugmentationFunctionRegistrationEvents() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE);
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT_TYPE);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Cancel Observation for Augmentation Function Results Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);

    }

    /**
     * Method to observe the Augmentation Function Result Events to keep the Model updated about the results of the executed Augmentation Functions
     */
    protected void observeAugmentationFunctionResults() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildResultWildCardEventType());

        //Save the adopted EventFilter
        this.augmentationFunctionResultEventsFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Observer for Augmentation Function Results Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);
    }

    /**
     * Method to un-observe the Augmentation Function Result Events to keep the Model updated about the results of the executed Augmentation Functions
     */
    protected void unObserveAugmentationFunctionResults() throws EventBusException, KernelException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildResultWildCardEventType());

        //Save the adopted EventFilter
        this.augmentationFunctionResultEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Cancel Observation for Augmentation Function Results Un-Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);
    }

    /**
     * Method to observe the Augmentation Function Error Events to keep the Model updated about the errors of the executed Augmentation Functions
     * @throws EventBusException If an error occurs during the event subscription
     */
    protected void observeAugmentationFunctionErrorEvents() throws EventBusException {
        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Observer for Augmentation Function Errors Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);
    }

    /**
     * Method to un-observe the Augmentation Function Error Events to keep the Model updated about the errors of the executed Augmentation Functions
     * @throws EventBusException If an error occurs during the event un-subscription
     */
    protected void unObserveAugmentationFunctionErrorEvents() throws EventBusException {
        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Observer for Augmentation Function Errors Unsubscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);
    }

    /**
     * Observe a single target Physical Asset Property
     * @param physicalAssetProperty the target PhysicalAssetProperty to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void observePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, KernelException {
        if(physicalAssetProperty == null)
            throw new KernelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(PhysicalAssetPropertyWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Observe a list of PhysicalAssetProperty
     * @param physicalAssetPropertyList List of PhysicalAssetProperty to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void observePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, KernelException {

        if(physicalAssetPropertyList == null)
            throw new KernelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetPropertyList)
            wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(PhysicalAssetPropertyWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

    }

    /**
     * Un-Observe a PhysicalAssetProperty
     * @param physicalAssetProperty PhysicalAssetProperty to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided PhysicalAssetProperty is NULL
     */
    protected void unObservePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, KernelException {

        if(physicalAssetProperty == null)
            throw new KernelException("Error ! NULL PhysicalProperty ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(PhysicalAssetPropertyWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Un-Observe a list of PhysicalAssetProperty
     * @param physicalAssetPropertyList List of PhysicalAssetProperty to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, KernelException {

        if(physicalAssetPropertyList == null)
            throw new KernelException("Error ! NULL PhysicalProperty List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetPropertyList)
            wldtEventFilter.add(PhysicalAssetPropertyWldtEvent.buildEventType(PhysicalAssetPropertyWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetProperty.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    ///////////////////// PHYSICAL ASSET EVENT OBSERVATION MANAGEMENT ////////////////////////////////

    /**
     * Observe a PhysicalAssetEvent
     * @param physicalAssetEvent PhysicalAssetEvent to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided PhysicalAssetEvent is NULL
     */
    protected void observePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, KernelException {
        if(physicalAssetEvent == null)
            throw new KernelException("Error ! NULL PhysicalAssetEvent ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(PhysicalAssetEventWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Observe a list of PhysicalAssetEvent
     * @param physicalAssetEventList List of PhysicalAssetEvent to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void observePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, KernelException {

        if(physicalAssetEventList == null)
            throw new KernelException("Error ! NULL PhysicalAssetEvent List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetEvent physicalAssetEvent : physicalAssetEventList)
            wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(PhysicalAssetEventWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

    }

    /**
     * Un-Observe a PhysicalAssetEvent
     * @param physicalAssetEvent PhysicalAssetEvent to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided PhysicalAssetEvent is NULL
     */
    protected void unObservePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, KernelException {

        if(physicalAssetEvent == null)
            throw new KernelException("Error ! NULL PhysicalAssetEvent ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(PhysicalAssetEventWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Un-Observe a list of PhysicalAssetEvent
     * @param physicalAssetEventList List of PhysicalAssetEvent to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, KernelException {

        if(physicalAssetEventList == null)
            throw new KernelException("Error ! NULL PhysicalAssetEvent List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetEvent physicalAssetEvent : physicalAssetEventList)
            wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(PhysicalAssetEventWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, physicalAssetEvent.getKey()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    ///////////////////// PHYSICAL ASSET RELATIONSHIP OBSERVATION MANAGEMENT ////////////////////////////////

    /**
     * Observe a PhysicalAssetRelationship
     * @param physicalAssetRelationship PhysicalAssetRelationship to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided PhysicalAssetRelationship is NULL
     */
    protected void observePhysicalAssetRelationship(PhysicalAssetRelationship<?> physicalAssetRelationship) throws EventBusException, KernelException {
        if(physicalAssetRelationship == null)
            throw new KernelException("Error ! NULL Physical Relationship ...");

        // Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceCreatedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceCreatedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceDeletedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceDeletedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /** Observe a list of PhysicalAssetRelationship
     * @param physicalAssetRelationships List of PhysicalAssetRelationship to observe
     * @throws EventBusException If an error occurs during the event subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void observePhysicalAssetRelationships(List<PhysicalAssetRelationship<?>> physicalAssetRelationships) throws KernelException, EventBusException {
        if(physicalAssetRelationships == null)
            throw new KernelException("Error ! NULL PhysicalAssetRelationship List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetRelationship<?> physicalAssetRelationship : physicalAssetRelationships){
            wldtEventFilter.add(PhysicalAssetRelationshipInstanceCreatedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceCreatedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));
            wldtEventFilter.add(PhysicalAssetRelationshipInstanceDeletedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceDeletedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));
        }

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Un-Observe a PhysicalAssetRelationship
     * @param physicalAssetRelationship PhysicalAssetRelationship to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided PhysicalAssetRelationship is NULL
     */
    protected void unObservePhysicalAssetRelationship(PhysicalAssetRelationship<?> physicalAssetRelationship) throws EventBusException, KernelException {

        if(physicalAssetRelationship == null)
            throw new KernelException("Error ! NULL PhysicalAssetRelationship ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceCreatedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceCreatedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceDeletedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceDeletedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));

        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Un-Observe a list of PhysicalAssetRelationship
     * @param physicalAssetRelationshipList List of PhysicalAssetRelationship to un-observe
     * @throws EventBusException If an error occurs during the event un-subscription
     * @throws KernelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetRelationships(List<PhysicalAssetRelationship<?>> physicalAssetRelationshipList) throws EventBusException, KernelException {

        if(physicalAssetRelationshipList == null)
            throw new KernelException("Error ! NULL PhysicalAssetEvent List ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(PhysicalAssetRelationship<?> relationship : physicalAssetRelationshipList){
            wldtEventFilter.add(PhysicalAssetEventWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceCreatedWldtEvent.EVENT_BASIC_TYPE, relationship.getName()));
            wldtEventFilter.add(PhysicalAssetRelationshipInstanceDeletedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceDeletedWldtEvent.EVENT_BASIC_TYPE, relationship.getName()));
        }

        //Save the adopted EventFilter
        this.physicalEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Observe all the Physical Events
     * @throws EventBusException If an error occurs during the event subscription
     */
    protected void observeDigitalActionEvents() throws EventBusException {
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        //wldtEventFilter.add(DigitalAdapter.DIGITAL_ACTION_EVENT);
        // Observe the Wildcard Event Type for Digital Action Event
        wldtEventFilter.add(WldtEventTypes.ALL_DIGITAL_ACTION_EVENT_TYPE);
        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Un-Observe all the Physical Events
     * @throws EventBusException If an error occurs during the event un-subscription
     */
    protected void unObserveDigitalActionEvents() throws EventBusException {
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        //wldtEventFilter.add(DigitalAdapter.DIGITAL_ACTION_EVENT);
        // Un-Observe the Wildcard Event Type for Digital Action Event
        wldtEventFilter.add(WldtEventTypes.ALL_DIGITAL_ACTION_EVENT_TYPE);
        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    /**
     * Publish a Physical Asset Action Event
     * @param actionKey Key of the action to publish
     * @param body Body of the action to publish
     * @param <T> Type of the action body
     * @throws EventBusException If an error occurs during the event publication
     */
    protected <T> void publishPhysicalAssetActionWldtEvent(String actionKey, T body) throws EventBusException {
        WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, new PhysicalAssetActionWldtEvent<>(actionKey, body));
    }

    /**
     * Method to refresh the result of a query for a specific Augmentation Function by providing the Augmentation Function id
     * @param augmentationFunctionId Id of the Augmentation Function to refresh the query result for
     */
    protected void refreshAugmentationFunctionQueryResult(String augmentationFunctionId) throws AugmentationFunctionException, EventBusException {
        logger.info("DigitalTwinModel -> Executing Query Request for Augmentation Function with id {} ...", augmentationFunctionId);

        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                logger.info("Executing Query Request for Augmentation Function with id {} from Augmentation Function Handler with id {} ...", augmentationFunctionId, augmentationFunctionHandler.getId());
                refreshAugmentationFunctionQueryResult(augmentationFunctionHandler.getId(), augmentationFunctionId);
                return;
            }
        }
    }

    /**
     * Method to refresh the result of a query for a specific Augmentation Function by providing the Augmentation Function Handler id and the Augmentation Function id
     * @param augmentationFunctionHandlerId Id of the Augmentation Function Handler of the Augmentation Function to refresh the query result for
     * @param augmentationFunctionId Id of the Augmentation Function to refresh the query result for
     * @throws AugmentationFunctionException If the provided Augmentation Function Handler id or Augmentation Function id are not valid or if the Augmentation Function is not of type STATEFUL
     * @throws EventBusException If an error occurs during the event publication to trigger the execution of the Augmentation Function
     */
    protected void refreshAugmentationFunctionQueryResult(String augmentationFunctionHandlerId, String augmentationFunctionId) throws AugmentationFunctionException, EventBusException {
        // Retrieve the Augmentation Function Handler with the specified id from the Augmentation Manager
        Optional<AugmentationFunctionHandler> augmentationFunctionHandlerOptional = this.augmentationManager.getAugmentationFunctionHandler(augmentationFunctionHandlerId);

        // Check if the Augmentation Function Handler is present, if not log a warning and return
        if(!augmentationFunctionHandlerOptional.isPresent()){
            logger.warn("Augmentation Function Handler with id {} is not registered in the Augmentation Manager of the Digital Twin Engine ! Cannot execute the Query Request for Augmentation Function with id {} ...", augmentationFunctionHandlerId, augmentationFunctionId);
            return;
        }

        // If the Augmentation Function Handler is present, retrieve it
        AugmentationFunctionHandler augmentationFunctionHandler = augmentationFunctionHandlerOptional.get();

        // Check if the Augmentation Function Handler has the Augmentation Function with the specified id, if not log a warning and return
        if(!augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent())
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Query Request for Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);

        // Retrieve the Augmentation Function with the specified id from the Augmentation Function Handler
        Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

        // Check if the Augmentation Function is present, if not log a warning and return
        if(!augmentationFunctionOptional.isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Query Request for Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Check the type of the augmentation function
        if(augmentationFunction.getType() != AugmentationFunctionType.STATEFUL)
            throw new AugmentationFunctionException(String.format("Error ! Invalid Augmentation Function Type provided for execution of the Query Request ! Expected: %s, Provided: %s", AugmentationFunctionType.STATEFUL, augmentationFunction.getType()));

        // Retrieve the Context Request for the augmentation function
        AugmentationFunctionContextRequest contextRequest = augmentationFunction.getContextRequest();

        if(contextRequest == null){
            logger.warn("Augmentation Function with id {} does not have a Context Request defined ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        if(contextRequest.getQueryRequest() == null) {
            logger.warn("Augmentation Function with id {} does not have a Query Request defined in the Context Request ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        // Create the correct Event Type to trigger the execution of the Augmentation Function
        String eventType = String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_QUERY_EXECUTION_BASE_TYPE, augmentationFunctionHandlerId, augmentationFunctionId);

        // Publish an event to trigger the execution of the Augmentation Function (Stateless) with the provided context
        WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, new WldtEvent<>(eventType));
    }

    /**
     * Method to execute a specific Stateless Augmentation Function by providing the Augmentation Function id. In this case the
     * context is automatically retrieved from the registered Augmentation Function and the execution is triggered without providing an explicit context.
     * This method is used for stateless augmentation function
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     */
    protected void executeAugmentationFunction(String augmentationFunctionId) throws EventBusException, AugmentationFunctionException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                executeAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, UUID.randomUUID().toString());
                return;
            }
        }

        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot execute the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to execute a specific Stateless Augmentation Function by providing the Augmentation Function id and a specific Augmentation
     * Function Request id. In this case the context is automatically retrieved from the registered Augmentation Function
     * and the execution is triggered without providing an explicit context. This method is used for stateless augmentation
     * function and allows to specify a custom Augmentation Function Request id to be used for the execution of the
     * Augmentation Function, otherwise a random UUID is generated and used as Augmentation Function Request id for the execution of the Augmentation Function
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for the execution of the Augmentation Function
     */
    protected void executeAugmentationFunction(String augmentationFunctionId, String augmentationFunctionRequestId) throws EventBusException, AugmentationFunctionException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                executeAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, augmentationFunctionRequestId);
                return;
            }
        }
        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot execute the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to execute a specific Stateless Augmentation Function by providing the Augmentation Function Handler id, the
     * Augmentation Function id and a specific Augmentation Function Request id. In this case the context is
     * automatically retrieved from the registered Augmentation Function and the execution is triggered without
     * providing an explicit context. This method is used for stateless augmentation function and allows to specify a
     * custom Augmentation Function Request id to be used for the execution of the Augmentation Function, otherwise a
     * random UUID is generated and used as Augmentation Function Request id for the execution of the Augmentation Function
     * @param augmentationFunctionHandlerId Id of the Augmentation Function Handler of the Augmentation Function to execute
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for the execution of the Augmentation Function
     */
    protected void executeAugmentationFunction(String augmentationFunctionHandlerId, String augmentationFunctionId, String augmentationFunctionRequestId) throws EventBusException, AugmentationFunctionException {

        // Retrieve the Augmentation Function Handler with the specified id from the Augmentation Manager
        Optional<AugmentationFunctionHandler> augmentationFunctionHandlerOptional = this.augmentationManager.getAugmentationFunctionHandler(augmentationFunctionHandlerId);

        // Check if the Augmentation Function Handler is present, if not log a warning and return
        if(!augmentationFunctionHandlerOptional.isPresent()){
            logger.warn("Augmentation Function Handler with id {} is not registered in the Augmentation Manager of the Digital Twin Engine ! Cannot execute the Augmentation Function with id {} ...", augmentationFunctionHandlerId, augmentationFunctionId);
            return;
        }

        // If the Augmentation Function Handler is present, retrieve it
        AugmentationFunctionHandler augmentationFunctionHandler = augmentationFunctionHandlerOptional.get();

        // Check if the Augmentation Function Handler has the Augmentation Function with the specified id, if not log a warning and return
        if(!augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent())
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);

        // Retrieve the Augmentation Function with the specified id from the Augmentation Function Handler
        Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

        // Check if the Augmentation Function is present, if not log a warning and return
        if(!augmentationFunctionOptional.isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }


        logger.info("Executing Augmentation Function with id {} from Augmentation Function Handler with id {} ...", augmentationFunctionId, augmentationFunctionHandler.getId());

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Check the type of the augmentation function
        if(augmentationFunction.getType() != AugmentationFunctionType.STATELESS)
            throw new AugmentationFunctionException(String.format("Error ! Invalid Augmentation Function Type provided for execution ! Expected: %s, Provided: %s", AugmentationFunctionType.STATELESS, augmentationFunction.getType()));

        // Retrieve the Context Request for the augmentation function
        AugmentationFunctionContextRequest contextRequest = augmentationFunction.getContextRequest();

        if(contextRequest == null){
            logger.warn("Augmentation Function with id {} does not have a Context Request defined ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        // Create an empty Augmentation Function Request to be then populated according to the Context Request and the augmentation function id
        AugmentationFunctionRequest augmentationFunctionRequest = new AugmentationFunctionRequest(augmentationFunctionRequestId, new AugmentationFunctionContext(), AugmentationFunctionRequestType.EXECUTE);

        // If the Context Request requires the Digital Twin State, retrieve it and set it in the context
        if(contextRequest.isObserveState())
            // TODO ... Check if the state to observe is the entire Digital Twin State or just a subset of it (e.g., specific properties or relationships) and retrieve only the required state information to set in the context
            augmentationFunctionRequest.getContext().setDigitalTwinState(this.digitalTwinStateManager.getDigitalTwinState());

        AugmentationFunctionExecuteWldtEvent augmentationFunctionExecuteWldtEvent = new AugmentationFunctionExecuteWldtEvent(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionRequest);

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionExecuteWldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {
            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Publish of Augmentation Function Request
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionExecuteWldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Augmentation Function Request
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_REQUEST_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * Method to start a specific Stateful Augmentation Function by providing the Augmentation Function id. In this case the context
     * is automatically retrieved from the registered Augmentation Function and the execution is triggered without providing
     * an explicit Augmentation Function Request Id. This method is used for stateful augmentation function
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     */
    protected void startAugmentationFunction(String augmentationFunctionId) throws EventBusException, AugmentationFunctionException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                startAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, UUID.randomUUID().toString());
                return;
            }
        }

        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot start the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to start a specific Stateful Augmentation Function by providing the Augmentation Function id and a specific Augmentation
     * Function Request id. In this case the context is automatically retrieved from the registered Augmentation Function
     * and the execution is triggered without providing an explicit context. This method is used for stateful augmentation
     * function and allows to specify a custom Augmentation Function Request id to be used for the execution of the
     * Augmentation Function.
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for the execution of the Augmentation Function
     */
    protected void startAugmentationFunction(String augmentationFunctionId, String augmentationFunctionRequestId) throws EventBusException, AugmentationFunctionException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                startAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, augmentationFunctionRequestId);
                return;
            }
        }

        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot start the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to start a specific Stateful Augmentation Function by providing the Augmentation Function Handler id, the
     * Augmentation Function id and a specific Augmentation Function Request id. In this case the context is
     * automatically retrieved from the registered Augmentation Function and the execution is triggered without
     * providing an explicit context. This method is used for stateful augmentation function and allows to specify a custom
     * Augmentation Function Request id to be used for the execution of the Augmentation Function.
     * @param augmentationFunctionHandlerId Id of the Augmentation Function Handler of the Augmentation Function to execute
     * @param augmentationFunctionId Id of the Augmentation Function to execute
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for the execution of the Augmentation Function
     */
    protected void startAugmentationFunction(String augmentationFunctionHandlerId, String augmentationFunctionId, String augmentationFunctionRequestId) throws EventBusException, AugmentationFunctionException {

        // Retrieve the Augmentation Function Handler with the specified id from the Augmentation Manager
        Optional<AugmentationFunctionHandler> augmentationFunctionHandlerOptional = this.augmentationManager.getAugmentationFunctionHandler(augmentationFunctionHandlerId);

        // Check if the Augmentation Function Handler is present, if not log a warning and return
        if(!augmentationFunctionHandlerOptional.isPresent()){
            logger.warn("Augmentation Function Handler with id {} is not registered in the Augmentation Manager of the Digital Twin Engine ! Cannot execute the Augmentation Function with id {} ...", augmentationFunctionHandlerId, augmentationFunctionId);
            return;
        }

        // If the Augmentation Function Handler is present, retrieve it
        AugmentationFunctionHandler augmentationFunctionHandler = augmentationFunctionHandlerOptional.get();

        // Check if the Augmentation Function Handler has the Augmentation Function with the specified id, if not log a warning and return
        if(!augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent())
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);

        // Retrieve the Augmentation Function with the specified id from the Augmentation Function Handler
        Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

        // Check if the Augmentation Function is present, if not log a warning and return
        if(!augmentationFunctionOptional.isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot execute the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }
        logger.info("Starting Augmentation Function with id {} from Augmentation Function Handler with id {} ...", augmentationFunctionId, augmentationFunctionHandler.getId());

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Check the type of the augmentation function
        if(augmentationFunction.getType() != AugmentationFunctionType.STATEFUL)
            throw new AugmentationFunctionException(String.format("Error ! Invalid Augmentation Function Type provided for execution ! Expected: %s, Provided: %s", AugmentationFunctionType.STATEFUL, augmentationFunction.getType()));

        if(((StatefulAugmentationFunction) augmentationFunction).isRunning()) {
            logger.info("Augmentation Function with id {} from Augmentation Function Handler with id {} is already running ! Nothing to do ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }

        // Retrieve the Context Request for the augmentation function
        AugmentationFunctionContextRequest contextRequest = augmentationFunction.getContextRequest();

        if(contextRequest == null){
            logger.warn("Augmentation Function with id {} does not have a Context Request defined ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        // Create a new Augmentation Function Request with the provided augmentation function request id and an empty context to be then populated according to the Context Request and the augmentation function id
        AugmentationFunctionRequest augmentationFunctionRequest = new AugmentationFunctionRequest(augmentationFunctionRequestId, new AugmentationFunctionContext(), AugmentationFunctionRequestType.START);

        // If the Context Request requires the Digital Twin State, retrieve it and set it in the context
        if(contextRequest.isObserveState())
            // TODO ... Check if the state to observe is the entire Digital Twin State or just a subset of it (e.g., specific properties or relationships) and retrieve only the required state information to set in the context
            augmentationFunctionRequest.getContext().setDigitalTwinState(this.digitalTwinStateManager.getDigitalTwinState());

        AugmentationFunctionStartWldtEvent augmentationFunctionStartWldtEvent = new AugmentationFunctionStartWldtEvent(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionRequest);

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionStartWldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {
            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Publish of Augmentation Function Request
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionStartWldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Augmentation Function Request
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_REQUEST_EXEC_TIME,
                        startMs);
            }
        }

    }

    /**
     * Method to stop a specific Stateful Augmentation Function by providing the Augmentation Function id. In this case
     * the context is automatically retrieved from the registered Augmentation Function and the execution is triggered
     * without providing an explicit context. This method is used for stateful augmentation function
     * @param augmentationFunctionId Id of the Augmentation Function to stop
     */
    protected void stopAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException, EventBusException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                stopAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, UUID.randomUUID().toString());
                return;
            }
        }

        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot stop the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to stop a specific Stateful Augmentation Function by providing the Augmentation Function id and a specific Augmentation
     * Function Request id. In this case the context is automatically retrieved from the registered Augmentation Function
     * and the execution is triggered without providing an explicit context. This method is used for stateful augmentation
     * function and allows to specify a custom Augmentation Function Request id to be used for the execution of the
     * Augmentation Function.
     * @param augmentationFunctionId Id of the Augmentation Function to stop
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for stopping the Augmentation Function
     */
    protected void stopAugmentationFunction(String augmentationFunctionId, String augmentationFunctionRequestId) throws AugmentationFunctionException, EventBusException {
        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                stopAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId, augmentationFunctionRequestId);
                return;
            }
        }

        logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot stop the Augmentation Function ...", augmentationFunctionId);
    }

    /**
     * Method to stop a specific Stateful Augmentation Function by providing the Augmentation Function Handler id, the
     * Augmentation Function id and a specific Augmentation Function Request id. In this case the context is automatically
     * retrieved from the registered Augmentation Function and the execution is triggered without providing an explicit context.
     * This method is used for stateful augmentation function and allows to specify a custom Augmentation Function Request
     * id to be used for the execution of the Augmentation Function.
     * @param augmentationFunctionHandlerId Id of the Augmentation Function Handler of the Augmentation Function to stop
     * @param augmentationFunctionId Id of the Augmentation Function to stop
     * @param augmentationFunctionRequestId Id of the Augmentation Function Request to use for stopping the Augmentation Function
     */
    protected void stopAugmentationFunction(String augmentationFunctionHandlerId, String augmentationFunctionId, String augmentationFunctionRequestId) throws AugmentationFunctionException, EventBusException {
        // Retrieve the Augmentation Function Handler with the specified id from the Augmentation Manager
        Optional<AugmentationFunctionHandler> augmentationFunctionHandlerOptional = this.augmentationManager.getAugmentationFunctionHandler(augmentationFunctionHandlerId);

        // Check if the Augmentation Function Handler is present, if not log a warning and return
        if(!augmentationFunctionHandlerOptional.isPresent()){
            logger.warn("Augmentation Function Handler with id {} is not registered in the Augmentation Manager of the Digital Twin Engine ! Cannot stop the Augmentation Function with id {} ...", augmentationFunctionHandlerId, augmentationFunctionId);
            return;
        }

        // If the Augmentation Function Handler is present, retrieve it
        AugmentationFunctionHandler augmentationFunctionHandler = augmentationFunctionHandlerOptional.get();

        // Check if the Augmentation Function Handler has the Augmentation Function with the specified id, if not log a warning and return
        if(!augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent())
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot stop the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);

        // Retrieve the Augmentation Function with the specified id from the Augmentation Function Handler
        Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

        // Check if the Augmentation Function is present, if not log a warning and return
        if(!augmentationFunctionOptional.isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot stop the Augmentation Function ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }
        logger.info("Stopping Augmentation Function with id {} from Augmentation Function Handler with id {} ...", augmentationFunctionId, augmentationFunctionHandler.getId());

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Check the type of the augmentation function
        if(augmentationFunction.getType() != AugmentationFunctionType.STATEFUL)
            throw new AugmentationFunctionException(String.format("Error ! Invalid Augmentation Function Type provided for execution ! Expected: %s, Provided: %s", AugmentationFunctionType.STATEFUL, augmentationFunction.getType()));

        if(!((StatefulAugmentationFunction) augmentationFunction).isRunning()) {
            logger.info("Augmentation Function with id {} from Augmentation Function Handler with id {} is not running ! Nothing to do ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return;
        }

        // Retrieve the Context Request for the augmentation function
        AugmentationFunctionContextRequest contextRequest = augmentationFunction.getContextRequest();

        if(contextRequest == null){
            logger.warn("Augmentation Function with id {} does not have a Context Request defined ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        // Create a new Augmentation Function Request with the provided augmentation function request id and an empty context to be then populated according to the Context Request and the augmentation function id
        AugmentationFunctionRequest augmentationFunctionRequest = new AugmentationFunctionRequest(augmentationFunctionRequestId, new AugmentationFunctionContext(), AugmentationFunctionRequestType.STOP);

        // If the Context Request requires the Digital Twin State, retrieve it and set it in the context
        if(contextRequest.isObserveState())
            // TODO ... Check if the state to observe is the entire Digital Twin State or just a subset of it (e.g., specific properties or relationships) and retrieve only the required state information to set in the context
            augmentationFunctionRequest.getContext().setDigitalTwinState(this.digitalTwinStateManager.getDigitalTwinState());

        AugmentationFunctionStopWldtEvent augmentationFunctionStopWldtEvent = new AugmentationFunctionStopWldtEvent(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionRequest);

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionStopWldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {
            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Publish of Augmentation Function Request
                WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, augmentationFunctionStopWldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("Publish Event Function Error Publishing Augmentation Function Request: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REQUEST_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Augmentation Function Request
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_REQUEST_EXEC_TIME,
                        startMs);
            }
        }
    }

    public boolean isStatefulAugmentationFunctionRunning(String augmentationFunctionId) {
            // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
            for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
                // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes check if it is running
                if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                    return isStatefulAugmentationFunctionRunning(augmentationFunctionHandler.getId(), augmentationFunctionId);
                }
            }

            logger.warn("Augmentation Function with id {} is not registered in any of the Augmentation Function Handlers registered in the Augmentation Manager of the Digital Twin Engine ! Cannot check if the Augmentation Function is running ...", augmentationFunctionId);
            return false;
    }

    public boolean isStatefulAugmentationFunctionRunning(String augmentationFunctionHandlerId, String augmentationFunctionId) {
        // Retrieve the Augmentation Function Handler with the specified id from the Augmentation Manager
        Optional<AugmentationFunctionHandler> augmentationFunctionHandlerOptional = this.augmentationManager.getAugmentationFunctionHandler(augmentationFunctionHandlerId);

        // Check if the Augmentation Function Handler is present, if not log a warning and return false
        if(!augmentationFunctionHandlerOptional.isPresent()){
            logger.warn("Augmentation Function Handler with id {} is not registered in the Augmentation Manager of the Digital Twin Engine ! Cannot check if the Augmentation Function with id {} is running ...", augmentationFunctionHandlerId, augmentationFunctionId);
            return false;
        }

        // If the Augmentation Function Handler is present, retrieve it
        AugmentationFunctionHandler augmentationFunctionHandler = augmentationFunctionHandlerOptional.get();

        // Check if the Augmentation Function Handler has the Augmentation Function with the specified id, if not log a warning and return false
        if(!augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot check if the Augmentation Function is running ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return false;
        }

        // Retrieve the Augmentation Function with the specified id from the Augmentation Function Handler
        Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

        // Check if the Augmentation Function is present, if not log a warning and return false
        if(!augmentationFunctionOptional.isPresent()) {
            logger.warn("Augmentation Function with id {} is not registered in the Augmentation Function Handler with id {} ! Cannot check if the Augmentation Function is running ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return false;
        }

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Check the type of the augmentation function
        if(augmentationFunction.getType() != AugmentationFunctionType.STATEFUL) {
            logger.warn("Augmentation Function with id {} from Augmentation Function Handler with id {} is not a stateful augmentation function ! Cannot check if it is running ...", augmentationFunctionId, augmentationFunctionHandlerId);
            return false;
        }

        return ((StatefulAugmentationFunction) augmentationFunction).isRunning();
    }



    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Digital Twin Model -> Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Digital Twin Model -> Unsubscribed from: {}", eventType);
    }

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handlePhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                onPhysicalAssetPropertyVariation(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Physical Asset Property Variation Event (implemented by the developer)
                onPhysicalAssetPropertyVariation(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handlePhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                onPhysicalAssetEventNotification(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Physical Asset Event Notitication (implemented by the developer)
                onPhysicalAssetEventNotification(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetEventNotification Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handlePhysicalAssetRelationshipInstanceCreatedEvent(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                onPhysicalAssetRelationshipEstablished(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                onPhysicalAssetRelationshipEstablished(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetRelationshipEstablished Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * Callback method invoked when results from Augmentation Functions are received.
     * In this case the default implementation does nothing, but specific Digital Twin Models can override this method
     * to handle the results of Augmentation Functions executions.
     * <p>
     * This method is not abstract because not all Digital Twin Models may need to handle Augmentation Function results,
     * so it provides a default implementation that can be optionally overridden
     * by specific Digital Twin Models that need to process Augmentation Function results.
     * <p>
     * This is different compared with method associated to the Shadowing Functions since they are
     * mandatory for all the Digital Twin Models and they define the core behavior of the shadowing process,
     * so they are defined as abstract methods without default implementation
     * to force all the Digital Twin Models to provide their own implementation of the shadowing behavior.
     *
     * @param augmentationFunctionHandlerId the id of the Augmentation Function Handler that executed the Augmentation Function
     * @param augmentationFunctionId the id of the executed Augmentation Function
     * @param augmentationFunctionResults the list of results from executed Augmentation Functions
     */
    private void handleAugmentationFunctionResultEvent(String augmentationFunctionHandlerId, String augmentationFunctionId, AugmentationFunctionResultList augmentationFunctionResults) {
        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                this.onAugmentationFunctionResultEvent(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionResults);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionResultEvent Function Error Observing Augmentation Function Result: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Physical Asset Property Variation Event (implemented by the developer)
                this.onAugmentationFunctionResultEvent(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionResults);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_RESULT_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionResultEvent Function Error Observing Augmentation Function Result: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_RESULT_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_RESULT_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * TODO Check ...
     * Callback method invoked when a new Augmentation Function has been registered, and it is available to be used.
     * <b>IMPORTANT NOTE 1</b>: Notification of the availability of augmentation functions are possible only if the Digital Twin is
     * on the Sync state through its lifecycle. If the DT is not sync it means that its state is not consistent and so
     * the execution of Augmentation function is not feasible. that are registered at the creation of the Digital Twin.
     * For those Augmentation Functions registered one the Digital Twin is created they will be notified step
     * by step at the first Synchronization phase of the Digital Twin lifecycle.
     * On the other hand, if an Augmentation Function is registered after the creation of the Digital Twin, it will be
     * immediately notified as available only if the Digital Twin is currently in the Sync state,
     * otherwise it will not be notified until the next Synchronization phase of the Digital Twin lifecycle.
     * <b>IMPORTANT NOTE 2</b>: Since the evolution of the Digital Twin lifecycle is not predictable, the availability of
     * Augmentation Functions is not predictable as well, so the Digital Twin Model should be able to handle the
     * availability of Augmentation Functions at any time during its lifecycle and handle potential duplicated callbacks
     * and notifications of the availability of the same Augmentation Function multiple times
     * (e.g., if an Augmentation Function is registered while the Digital Twin is in Sync, then it will be notified as available,
     * then if the Digital Twin goes out of sync and then back to sync, it will be notified again as available).
     * This behavior is implemented in this way to ensure that the Digital Twin Model is always aware of the availability of Augmentation Functions and can handle it accordingly,
     * <b>IMPORTANT NOTE 3</b>: At any time the Digital Twin Model can check the currently available Augmentation Functions
     * by querying the Augmentation Manager, so it can always be aware of the currently available Augmentation Functions
     * even if it misses some notifications of their availability of if the developer decides to not handle the in a
     * different and custom way within the Digital Twin Behavior.
     * This method is not abstract because not all Digital Twin Models may need to handle the availability of Augmentation Functions,
     * so it provides a default implementation that can be optionally overridden
     * by specific Digital Twin Models that need to process Augmentation Function availability.
     * @param handlerId the id of the Augmentation Function Handler that registered the Augmentation Function
     * @param augmentationFunction the Augmentation Function that became available
     */
    private void handleAugmentationFunctionNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                this.onAugmentationNewFunctionAvailable(handlerId, augmentationFunction);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationNewFunctionAvailable Function Error Observing Augmentation Function Available: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Augmentation Function Available (implemented by the developer)
                this.onAugmentationNewFunctionAvailable(handlerId, augmentationFunction);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REGISTERED_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationNewFunctionAvailable Function Error Observing Augmentation Function Available: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_REGISTERED_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_REGISTERED_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * Callback method invoked when an Augmentation Function has been unregistered, and it is no more available to be used.
     * This method is not abstract because not all Digital Twin Models may need to handle the unavailability of Augmentation Functions,
     * so it provides a default implementation that can be optionally overridden
     * by specific Digital Twin Models that need to process Augmentation Function unavailability.
     * @param handlerId the id of the Augmentation Function Handler that unregistered the Augmentation Function
     * @param augmentationFunction the Augmentation Function that became unavailable
     */
    private void handleAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                this.onAugmentationFunctionUnAvailable(handlerId, augmentationFunction);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionUnAvailable Function Error Observing Augmentation Function Unavailable: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Augmentation Function UnAvailable (implemented by the developer)
                this.onAugmentationFunctionUnAvailable(handlerId, augmentationFunction);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_UNREGISTERED_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionUnAvailable Function Error Observing Augmentation Function Unavailable: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_UNREGISTERED_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_UNREGISTERED_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * Method to handle the availability of a list of Augmentation Functions, for example when a new Augmentation Function
     * Handler is registered with a list of Augmentation Functions or when the Digital Twin is created and it is notified
     * of the currently available Augmentation Functions that are registered at the creation of the Digital Twin.
     * @param handlerId the id of the Augmentation Function Handler that registered the Augmentation Functions
     * @param augmentationFunctionList the list of Augmentation Functions that became available
     */
    private void handleAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList) {
        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                this.onAugmentationFunctionListAvailable(handlerId, augmentationFunctionList);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionListAvailable Function Error Observing Augmentation Function List Available: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Augmentation Function List Available (implemented by the developer)
                this.onAugmentationFunctionListAvailable(handlerId, augmentationFunctionList);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_LIST_REGISTERED_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionListAvailable Function Error Observing Augmentation Function List Available: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_LIST_REGISTERED_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_LIST_REGISTERED_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * This method is called when an error occurs during the execution of an Augmentation Function. It provides a callback
     * to handle such errors, allowing specific Digital Twin Models to implement custom error handling logic for Augmentation
     * Function executions. This method is not abstract because not all Digital Twin Models may need to handle Augmentation
     * Function errors, so it provides a default implementation that can be optionally overridden by specific Digital
     * Twin Models that need to process Augmentation Function errors.
     * @param handlerId the id of the Augmentation Function Handler that executed the Augmentation Function that caused the error
     * @param augmentationFunctionError the error that occurred during the execution of the Augmentation Function
     * @param functionId the id of the Augmentation Function that caused the error
     */
    private void handleAugmentationFunctionError(String handlerId, String functionId, AugmentationFunctionError augmentationFunctionError) {
        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                this.onAugmentationFunctionError(handlerId, functionId, augmentationFunctionError);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionError Function Error Observing Augmentation Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the Augmentation Function Error (implemented by the developer)
                this.onAugmentationFunctionError(handlerId, functionId, augmentationFunctionError);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_ERROR_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onAugmentationFunctionError Function Error Observing Augmentation Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AF_ERROR_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.AF_ERROR_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handlePhysicalAssetRelationshipInstanceDeletedEvent(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                onPhysicalAssetRelationshipDeleted(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                onPhysicalAssetRelationshipDeleted(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetRelationshipDeleted Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_EXEC_TIME,
                        startMs);
            }
        }
    }

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handleDigitalActionEvent(DigitalActionWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            try {
                onDigitalActionEvent(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                onDigitalActionEvent(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_EXEC_TIME,
                        startMs);
            }
        }
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        logger.info("Shadowing Function -> Received Event: {} Class: {}", wldtEvent, wldtEvent.getClass());

        // TODO Re-write all the following checks with Event Filters & Wildcard instead of Class Instances

        if(wldtEvent instanceof PhysicalAssetPropertyWldtEvent)
            handlePhysicalAssetPropertyVariation((PhysicalAssetPropertyWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetEventWldtEvent)
            handlePhysicalAssetEventNotification((PhysicalAssetEventWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceCreatedWldtEvent)
            //onPhysicalAssetRelationshipEstablished((PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) wldtEvent);
            handlePhysicalAssetRelationshipInstanceCreatedEvent((PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceDeletedWldtEvent)
            //onPhysicalAssetRelationshipDeleted((PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) wldtEvent);
            handlePhysicalAssetRelationshipInstanceDeletedEvent((PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof DigitalActionWldtEvent<?>)
            //onDigitalActionEvent((DigitalActionWldtEvent<?>) wldtEvent);
            handleDigitalActionEvent((DigitalActionWldtEvent<?>) wldtEvent);

        //if(wldtEvent.getType().equals(DigitalAdapter.DIGITAL_ACTION_EVENT))
        //    onDigitalActionEvent((DigitalActionWldtEvent<?>) wldtEvent.getBody());

        // If the event is an Augmentation Function Result Event, then handle it with the correct callback
        // checks also the body of the event to avoid processing events with the correct topic but
        // with a wrong body (e.g., not containing an AugmentationFunctionResult)
        // TODO Move the check about event list size and type inside the if to at least log a warning in case of wrong event body instead of just ignoring the event
        if(wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE)
                && (wldtEvent.getBody() instanceof List)
                && !((List<?>) wldtEvent.getBody()).isEmpty()
                && ((List<?>) wldtEvent.getBody()).get(0) instanceof AugmentationFunctionResult){

            // Extract Augmentation Handler and Augmentation Function id from the event type
            String[] eventTypeParts = wldtEvent.getType().split("\\.");
            String augmentationFunctionHandlerId = eventTypeParts[4];
            String augmentationFunctionId = eventTypeParts[5];

            // Call the callback to handle the Augmentation Function Result Event with the correct body type
            this.handleAugmentationFunctionResultEvent(augmentationFunctionHandlerId, augmentationFunctionId, (AugmentationFunctionResultList) wldtEvent.getBody());
        }

        // Handle Augmentation Function Registration Events with the correct callback and body type
        if(wldtEvent.getType().equals(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE)
                && wldtEvent instanceof AugmentationFunctionRegistrationWldtEvent){

            // Cast the event body to the correct type
            AugmentationFunctionRegistrationWldtEvent augmentationFunctionRegistrationEvent = (AugmentationFunctionRegistrationWldtEvent) wldtEvent;

            // Extract the Augmentation Function Handler id from the event body
            String augmentationFunctionHandlerId = augmentationFunctionRegistrationEvent.getAugmentationHandlerId();

            // Extract the Augmentation Function from the event body
            AugmentationFunction augmentationFunction = augmentationFunctionRegistrationEvent.getBody();

            // Call the callback to handle the Augmentation Function Registration Event with the correct body type
            this.handleAugmentationFunctionNewFunctionAvailable(augmentationFunctionHandlerId, augmentationFunction);
        }

        // Handle Augmentation Function Un-Registration Events with the correct callback and body type
        if(wldtEvent.getType().equals(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT_TYPE)
                && wldtEvent instanceof AugmentationFunctionUnRegistrationWldtEvent){

            // Cast the event body to the correct type
            AugmentationFunctionUnRegistrationWldtEvent augmentationFunctionUnRegistrationEvent = (AugmentationFunctionUnRegistrationWldtEvent) wldtEvent;

            // Extract the Augmentation Function Handler id from the event body
            String augmentationFunctionHandlerId = augmentationFunctionUnRegistrationEvent.getAugmentationHandlerId();

            // Extract the Augmentation Function from the event body
            AugmentationFunction augmentationFunction = augmentationFunctionUnRegistrationEvent.getBody();

            // Call the callback to handle the Augmentation Function Un-Registration Event with the correct body type
            this.handleAugmentationFunctionUnAvailable(augmentationFunctionHandlerId, augmentationFunction);
        }

        // Handle Augmentation Function Error Events with the correct callback and body type
        if(wldtEvent.getType().equals(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE)
                && wldtEvent instanceof AugmentationFunctionErrorWldtEvent){

            // Cast the event body to the correct type
            AugmentationFunctionErrorWldtEvent augmentationFunctionErrorWldtEvent = (AugmentationFunctionErrorWldtEvent) wldtEvent;

            // Extract the Augmentation Function Handler id from the event body
            String augmentationFunctionHandlerId = augmentationFunctionErrorWldtEvent.getAugmentationHandlerId();

            // Extract the Augmentation Function id from the event type
            String augmentationFunctionId = augmentationFunctionErrorWldtEvent.getAugmentationFunctionId();

            // Extract the Augmentation Function id from the event body
            AugmentationFunctionError augmentationFunctionError = augmentationFunctionErrorWldtEvent.getBody();

            // Call the callback to handle the Augmentation Function Un-Registration Event with the correct body type
            this.handleAugmentationFunctionError(augmentationFunctionHandlerId, augmentationFunctionId, augmentationFunctionError);
        }

    }

    abstract protected void onCreate();

    abstract protected void onStart();

    abstract protected void onStop();

    abstract protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    abstract protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);

    abstract protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription);

    // ========================================
    // SHADOWING FUNCTION CALLBACKS
    // ========================================
    // The following abstract methods define the shadowing behavior of the Digital Twin.
    // Each method is annotated with @ShadowingFunction to provide formal metadata
    // about its role in the shadowing process.

    /**
     * Callback method invoked when a physical asset property changes.
     * <p>
     * This shadowing function is responsible for processing property variations
     * received from Physical Adapters and updating the Digital Twin State accordingly.
     * The implementation defines how the DT reacts to property changes, which may include:
     * <ul>
     *   <li>Updating corresponding DT state properties</li>
     *   <li>Triggering derived computations or aggregations</li>
     *   <li>Generating events for Digital Adapters</li>
     *   <li>Applying business logic or validation rules</li>
     * </ul>
     *
     * @param physicalPropertyEventMessage the event containing property variation details
     */
    @ShadowingFunction(
            value = ShadowingType.PHYSICAL_PROPERTY_VARIATION,
            description = "Processes physical asset property changes and updates DT state"
    )
    abstract protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage);

    /**
     * Callback method invoked when a physical asset emits an event notification.
     * <p>
     * This shadowing function processes event notifications from the physical world,
     * allowing the DT to react to asynchronous occurrences on the physical asset.
     *
     * @param physicalAssetEventWldtEvent the event notification from the physical asset
     */
    @ShadowingFunction(
            value = ShadowingType.PHYSICAL_EVENT_NOTIFICATION,
            description = "Handles event notifications emitted by the physical asset"
    )
    abstract protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent);

    /**
     * Callback method invoked when a relationship is established on the physical asset.
     * <p>
     * This shadowing function manages the creation of relationships in the DT state,
     * mirroring the relationship structure of the physical asset.
     *
     * @param physicalAssetRelationshipWldtEvent the relationship establishment event
     */
    @ShadowingFunction(
            value = ShadowingType.PHYSICAL_RELATIONSHIP_ESTABLISHED,
            description = "Mirrors relationship creation from physical asset to DT state"
    )
    abstract protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent);

    /**
     * Callback method invoked when a relationship is deleted from the physical asset.
     * <p>
     * This shadowing function manages the removal of relationships in the DT state,
     * maintaining consistency with the physical asset's relationship model.
     *
     * @param physicalAssetRelationshipWldtEvent the relationship deletion event
     */
    @ShadowingFunction(
            value = ShadowingType.PHYSICAL_RELATIONSHIP_DELETED,
            description = "Removes relationships from DT state when deleted on physical asset"
    )
    abstract protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent);

    /**
     * Callback method invoked when a digital action is requested from external applications.
     * <p>
     * This shadowing function processes action requests originating from Digital Adapters
     * and determines how to handle them, which may include:
     * <ul>
     *   <li>Validating action parameters against DT state</li>
     *   <li>Forwarding actions to appropriate Physical Adapters</li>
     *   <li>Executing DT-side computations before physical execution</li>
     *   <li>Rejecting invalid or unsafe action requests</li>
     * </ul>
     *
     * @param digitalActionWldtEvent the digital action request event
     */
    @ShadowingFunction(
            value = ShadowingType.DIGITAL_ACTION_REQUEST,
            description = "Processes digital action requests and coordinates execution with physical asset"
    )
    abstract protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent);

    /**
     * Callback method invoked when an error occurs during the execution of an Augmentation Function.
     * <p>
     * This shadowing function allows the DT to handle errors from Augmentation Function executions,
     * which may involve logging, state updates, or triggering compensating actions.
     * @param handlerId the id of the Augmentation Function Handler that executed the Augmentation Function that caused the error
     * @param functionId the id of the Augmentation Function that caused the error
     * @param augmentationFunctionError the error that occurred during the execution of the Augmentation Function
     */
    @ShadowingFunction(
            value = ShadowingType.AUGMENTATION_FUNCTION_ERROR,
            description = "Handles errors that occur during the execution of Augmentation Functions"
    )
    abstract protected void onAugmentationFunctionError(String handlerId, String functionId, AugmentationFunctionError augmentationFunctionError);

    /**
     * Callback method invoked when results from Augmentation Functions are received.
     * <p>
     * This shadowing function processes the results of Augmentation Function executions, allowing the DT to integrate
     * new information derived from the physical asset or to trigger further computations or actions based on those results.
     * @param augmentationFunctionHandlerId the id of the Augmentation Function Handler that executed the Augmentation Function
     * @param augmentationFunctionId the id of the executed Augmentation Function
     * @param augmentationFunctionResults the list of results from executed Augmentation Functions
     */
    @ShadowingFunction(
            value = ShadowingType.AUGMENTATION_FUNCTION_RESULT,
            description = "Processes results from executed Augmentation Functions"
    )
    abstract protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId, String augmentationFunctionId, AugmentationFunctionResultList augmentationFunctionResults);

    /**
     * Callback method invoked when a new Augmentation Function becomes available for execution.
     * <p>
     * This shadowing function allows the DT to react to the availability of new Augmentation Functions that can be executed,
     * which may involve:
     * <ul>
     *   <li>Checking if the new function is relevant to the current DT state</li>
     *   <li>Triggering the execution of the new function if it is relevant</li>
     *   <li>Updating internal models or state to reflect the new capabilities provided by the available function</li>
     * </ul>
     * @param handlerId the id of the Augmentation Function Handler that registered the Augmentation Function
     * @param augmentationFunction the Augmentation Function that became available
     */
    @ShadowingFunction(
            value = ShadowingType.AUGMENTATION_FUNCTION_AVAILABLE,
            description = "Handles the availability of new Augmentation Functions that can be executed by the Digital Twin"
    )
    abstract protected void onAugmentationNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction);

    /**
     * Callback method invoked when an Augmentation Function becomes unavailable for execution.
     * <p>
     * This shadowing function allows the DT to handle the unavailability of Augmentation Functions that can be executed, which may involve:
     * <ul>
     *   <li>Checking if the unavailable function is relevant to the current DT state</li>
     *   <li>Handling the unavailability of the function, for example by updating internal models
     *   or state to reflect the loss of capabilities provided by the unavailable function</li>
     * @param handlerId the id of the Augmentation Function Handler that unregistered the Augmentation Function
     * @param augmentationFunction the Augmentation Function that became unavailable
     */
    @ShadowingFunction(
            value = ShadowingType.AUGMENTATION_FUNCTION_UNAVAILABLE,
            description = "Handles the unavailability of Augmentation Functions that can be executed by the Digital Twin"
    )
    abstract protected void onAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction);

    /**
     * Callback method invoked when a list of Augmentation Functions becomes available for execution.
     * <p>
     * This shadowing function allows the DT to react to the availability of a list of Augmentation Functions that can be executed, which may involve:
     * <ul>
     *   <li>Checking if the new functions are relevant to the current DT state</li>
     *   <li>Triggering the execution of the new functions if they are relevant</li>
     *   <li>Updating internal models or state to reflect the new capabilities provided by the available functions</li>
     * </ul>
     * @param handlerId the id of the Augmentation Function Handler that registered the Augmentation Functions
     * @param augmentationFunctionList the list of Augmentation Functions that became available
     */
    @ShadowingFunction(
            value = ShadowingType.AUGMENTATION_FUNCTION_LIST_AVAILABLE,
            description = "Handles the availability of new Augmentation Functions that can be executed by the Digital Twin"
    )
    abstract protected void onAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WldtEventFilter getPhysicalEventsFilter() {
        return physicalEventsFilter;
    }

    public ShadowingModelListener getShadowingModelListener() {
        return shadowingModelListener;
    }

    public void setShadowingModelListener(ShadowingModelListener shadowingModelListener) {
        this.shadowingModelListener = shadowingModelListener;
    }

    /**
     * Notify the Shadowing Model Listener that the Shadowing Model is synchronized with the Physical Asset
     */
    protected void notifyShadowingSync(){
        // Notify the Shadowing Model Listener that the Shadowing Model is synchronized with the Physical Asset
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingSync(digitalTwinStateManager.getDigitalTwinState());
    }

    /**
     * Notify the Shadowing Model Listener that the Shadowing Model is out of sync with the Physical Asset
     */
    protected void notifyShadowingOutOfSync(){
        // Notify the Shadowing Model Listener that the Shadowing Model is out of sync with the Physical Asset
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingOutOfSync(digitalTwinStateManager.getDigitalTwinState());
    }

    /**
     * TODO ...
     * @return
     */
    public MonitoringInterface getMonitoringInterface() {
        return monitoringInterface;
    }

    /**
     * TODO ...
     */
    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {

        // Check if the monitoring interface is not null
        if(monitoringInterface != null) {

            // Set the Monitoring Interface Reference to the DT Model
            this.monitoringInterface = monitoringInterface;

            // If the Digital Twin State Manager is not null set the reference to the Monitoring Interface
            if (this.digitalTwinStateManager != null)
                this.digitalTwinStateManager.setMonitoringInterface(monitoringInterface);
        }
        else
            logger.error("Cannot set Monitoring Interface to null for Digital Twin Model with id: {}", this.id);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalTwinModel that = (DigitalTwinModel) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelFunction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", physicalEventsFilter=").append(physicalEventsFilter);
        sb.append('}');
        return sb.toString();
    }

}

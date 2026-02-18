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
import it.wldt.augmentation.event.AugmentationFunctionRegistrationWldtEvent;
import it.wldt.core.event.*;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.EventBusException;
import it.wldt.exception.KernelException;
import it.wldt.adapter.physical.event.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.management.ResourceManager;
import it.wldt.storage.StorageManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
 *
 * <h3>Core Responsibilities</h3>
 * <ul>
 *   <li><b>Shadowing Function Orchestration</b>: Manages the shadowing process (replication and
 *       digitalization) that keeps the Digital Twin State synchronized with the physical asset</li>
 *   <li><b>Physical Asset Description Management</b>: Coordinates Physical Adapters and their descriptions</li>
 *   <li><b>Digital Twin State Management</b>: Maintains the canonical state representation</li>
 *   <li><b>Storage Management</b>: Coordinates persistent storage of Digital Twin state evolution,
 *       events, and lifecycle data through the Storage Manager</li>
 *   <li><b>Resource Management</b>: Handles Digital Twin resources, relationships, and their
 *       lifecycle through the Resource Manager</li>
 *   <li><b>Augmentation Manager</b>: Handles Digital Twin Augmentation Functions through the evolution of DT life cycle
 *   and within its behavior.</li>
 *   <li><b>Event Routing</b>: Routes events between physical and digital sides through the Shadowing Function</li>
 *   <li><b>Lifecycle Management</b>: Controls initialization, execution, and termination</li>
 * </ul>
 *
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

        try{
            // In this case since we have the Augmentation Manager -> Observe Aug. Function Results
            this.observeAugmentationFunctionResults();
        }catch(Exception e){
            logger.error("Error Observing Augmentation Function Results ! Error: {}", e.getMessage());
        }
    }

    /**
     * TODO ...
     */
    private String buildResultWildCardEventType(){
        return String.format("%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE, WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE);
    }

    /**
     * TODO ...
     */
    protected void observeAugmentationFunctionRegistrationEvents() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE);
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Observer for Augmentation Function Results Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);

    }

    /**
     * TODO ...
     */
    protected void unObserveAugmentationFunctionRegistrationEvents() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE);
        wldtEventFilter.add(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);

        logger.info("Cancel Observation for Augmentation Function Results Subscribed ! Digital Twin Model ID: {}, Event Filter: {}", this.id, wldtEventFilter);

    }

    /**
     * TODO ...
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
     * TODO ...
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
     * TODO
     */
    protected void executeAugmentationFunction(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws EventBusException {
        // Publish an event to trigger the execution of the Augmentation Function (Stateless) with the provided context
        WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, new WldtEvent<>(augmentationFunctionId, augmentationFunctionContext));
    }

    /**
     * TODO In this case the context is automatically retrieved from the registered Augmentation Function and the
     * execution is triggered without providing an explicit context
     * (e.g., for stateful augmentation functions that manage their own context internally)
     */
    protected void executeAugmentationFunction(String augmentationFunctionId) throws EventBusException {

        logger.info("DigitalTwinModel -> Executing Augmentation Function with id {} ...", augmentationFunctionId);

        // Iterate over all the registered Augmentation Function Handlers Map to find the Augmentation Function with the specified id
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()){
            // Check if the current Augmentation Function Handler has the Augmentation Function with the specified id, if yes execute it
            if(augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId).isPresent()){
                logger.info("Executing Augmentation Function with id {} from Augmentation Function Handler with id {} ...", augmentationFunctionId, augmentationFunctionHandler.getId());
                executeAugmentationFunction(augmentationFunctionHandler.getId(), augmentationFunctionId);
                return;
            }
        }
    }

    /**
     * TODO In this case the context is automatically retrieved from the registered Augmentation Function and the
     * execution is triggered without providing an explicit context
     * (e.g., for stateful augmentation functions that manage their own context internally)
     */
    protected void executeAugmentationFunction(String augmentationFunctionHandlerId, String augmentationFunctionId) throws EventBusException {

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

        // If the Augmentation Function is present, retrieve it
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        // Retrieve the Context Request for the augmentation function
        AugmentationFunctionContextRequest contextRequest = augmentationFunction.getContextRequest();

        if(contextRequest == null){
            logger.warn("Augmentation Function with id {} does not have a Context Request defined ! Nothing to execute ...", augmentationFunctionId);
            return;
        }

        // Create an empty Augmentation Context to be then populated according to the Context Request
        AugmentationFunctionContext augmentationFunctionContext = new AugmentationFunctionContext();

        // If the Context Request requires the Digital Twin State, retrieve it and set it in the context
        if(contextRequest.isObserveState())
            augmentationFunctionContext.setDigitalTwinState(this.digitalTwinStateManager.getDigitalTwinState());

        // If the context request has a query request, execute the query on the Digital Twin's storage and set the query result in the context
        if(contextRequest.getQueryRequest() != null){
            // TODO ... Fix with Query Management
        }

        // Create the correct Event Type to trigger the execution of the Augmentation Function
        String eventType = String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTION_BASE_TYPE, augmentationFunctionHandlerId, augmentationFunctionId);

        // Publish an event to trigger the execution of the Augmentation Function (Stateless) with the provided context
        WldtEventBus.getInstance().publishEvent(this.digitalTwinStateManager.getDigitalTwinId(), this.id, new WldtEvent<>(eventType, augmentationFunctionContext));

    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Digital Twin Model -> Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Digital Twin Model -> Unsubscribed from: {}", eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        logger.info("Shadowing Function -> Received Event: {} Class: {}", wldtEvent, wldtEvent.getClass());

        // TODO Re-write all the following checks with Event Filters & Wildcard instead of Class Instances

        if(wldtEvent instanceof PhysicalAssetPropertyWldtEvent)
            onPhysicalAssetPropertyVariation((PhysicalAssetPropertyWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetEventWldtEvent)
            onPhysicalAssetEventNotification((PhysicalAssetEventWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceCreatedWldtEvent)
            onPhysicalAssetRelationshipEstablished((PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceDeletedWldtEvent)
            onPhysicalAssetRelationshipDeleted((PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof DigitalActionWldtEvent<?>)
            onDigitalActionEvent((DigitalActionWldtEvent<?>) wldtEvent);

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
            onAugmentationFunctionResultEvent(augmentationFunctionHandlerId, augmentationFunctionId, (List<AugmentationFunctionResult<?>>) wldtEvent.getBody());
        }

        // TODO Check
        if(wldtEvent.getType().equals(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE)
                && wldtEvent.getBody() instanceof AugmentationFunctionRegistrationWldtEvent){

            // Cast the event body to the correct type
            AugmentationFunctionRegistrationWldtEvent augmentationFunctionRegistrationEvent = (AugmentationFunctionRegistrationWldtEvent) wldtEvent.getBody();

            // Extract the Augmentation Function Handler id from the event body
            String augmentationFunctionHandlerId = augmentationFunctionRegistrationEvent.getAugmentationHandlerId();

            // Extract the Augmentation Function from the event body
            AugmentationFunction augmentationFunction = augmentationFunctionRegistrationEvent.getBody();

            // Call the callback to handle the Augmentation Function Registration Event with the correct body type
            onAugmentationFunctionAvailable(augmentationFunctionHandlerId, augmentationFunction);
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
     * @param augmentationFunctionResult    the list of results from executed Augmentation Functions
     */
    protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId, String augmentationFunctionId, List<AugmentationFunctionResult<?>> augmentationFunctionResult){
        // Default implementation does nothing, can be overridden by specific
        // Digital Twin Models to handle Augmentation Function results

        logger.info("Default Implementation -> Nothing to do with: Augmentation Function Result Event: {}", augmentationFunctionResult);
    }

    /**
     * Callback method invoked when an Augmentation Function has been registered, and it is available to be used.
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
    protected void onAugmentationFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        // Default implementation does nothing, can be overridden by specific
        // Digital Twin Models to handle Augmentation Function results

        logger.info("Default Implementation -> Nothing to do with: Augmentation Function Available: {} for Handler", augmentationFunction, handlerId);
    }

    /**
     * Callback method invoked when an Augmentation Function has been unregistered, and it is no more available to be used.
     * This method is not abstract because not all Digital Twin Models may need to handle the unavailability of Augmentation Functions,
     * so it provides a default implementation that can be optionally overridden
     * by specific Digital Twin Models that need to process Augmentation Function unavailability.
     * @param handlerId the id of the Augmentation Function Handler that unregistered the Augmentation Function
     * @param augmentationFunction the Augmentation Function that became unavailable
     */
    protected void onAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        // Default implementation does nothing, can be overridden by specific
        // Digital Twin Models to handle Augmentation Function results

        logger.info("Default Implementation -> Nothing to do with: Augmentation Function UnAvailable: {} for Handler", augmentationFunction, handlerId);
    }

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

        // Now that the Digital Twin is Synchronized we can notify the Digital Twin Model about the available
        // and registered Augmentation Function for each Handler in order to allow the Digital Twin Model to execute
        // them when needed
        notifyInitialAugmentationFunctionsAvailable();
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
     * Notify the Digital Twin Model implemented by the developer about the available and registered
     * Augmentation Function for each Handler in order to allow the Digital Twin Model to execute them when needed.
     * This method is typically called at the first Synchronization phase of the Digital Twin lifecycle to notify
     * the availability of all the Augmentation Functions that are registered at the creation of the Digital Twin,
     * and then it can be called every time a new Augmentation Function
     * is registered while the Digital Twin is in Sync to notify its availability immediately.
     */
    private void notifyInitialAugmentationFunctionsAvailable(){
        try{
            // Now that the Digital Twin is Synchronized we can notify the Digital Twin Model about the available
            // and registered Augmentation Function for each Handler in order to allow the Digital Twin Model to execute
            // them when needed
            if(this.augmentationManager != null
                    && this.augmentationManager.getAllAugmentationFunctionHandlers() != null
                    && !this.augmentationManager.getAllAugmentationFunctionHandlers().isEmpty()) {

                logger.debug("Notifying Digital Twin Model about the available Augmentation Function Handlers for the Digital Twin Model Engine ...");

                // Iterate over all the registered Augmentation Function Handlers and notify the Digital Twin Model
                // about the available Augmentation Functions for each Handler
                for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationManager.getAllAugmentationFunctionHandlers()) {
                    for(AugmentationFunction augmentationFunction : augmentationFunctionHandler.getAllAugmentationFunctions()) {
                        this.onAugmentationFunctionAvailable(augmentationFunctionHandler.getId(), augmentationFunction);
                    }
                }
            }
        }catch(Exception e){
            logger.error("Error while notifying the availability of Augmentation Functions to the Digital Twin Model: {}", e.getMessage());
        }

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

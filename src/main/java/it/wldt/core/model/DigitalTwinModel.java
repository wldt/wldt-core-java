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
import it.wldt.core.model.annotation.ShadowingFunction;
import it.wldt.core.model.annotation.ShadowingType;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Core class for Digital Twin modeling, event processing together with lifecycle and behavior management.
 * The {@code DigitalTwinModel} serves as the central component in shaping Digital Twin behavior,
 * managing the Digital Twin's execution, state, and interactions between physical and digital worlds.
 * It orchestrates the {@link ShadowingFunction} (modeled throudh a combination of dedicated methods)
 * that defines the DT's behavioral logic while coordinating core components including Physical Adapters,
 * Digital Adapters, State Management, and Storage.
 * Core Responsibilities:
 * - Shadowing Function Orchestration: Manages the shadowing process (replication and digitalization) that keeps the Digital Twin State synchronized with the physical asset
 * - Physical Asset Description Management: Coordinates Physical Adapters and their descriptions
 * - Digital Twin State Management: Maintains the canonical state representation
 * - Storage Management: Coordinates persistent storage of Digital Twin state evolution, events, and lifecycle data through the Storage Manager
 * - Resource Management: Handles Digital Twin resources, relationships, and their lifecycle through the Resource Manager
 * - Event Routing: Routes events between physical and digital sides through the Shadowing Function
 * - Lifecycle Management: Controls initialization, execution, and termination
 *
 * @see ShadowingFunction
 * @see DigitalTwinStateManager
 * @see StorageManager
 * @see ResourceManager
 */
public abstract class DigitalTwinModel implements WldtEventListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinModel.class);

    private String id = null;

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
     * Default Constructor
     * @param id Unique Identifier of the Digital Twin Model
     */
    public DigitalTwinModel(String id){
        this.id = id;
        this.physicalEventsFilter = new WldtEventFilter();
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
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingSync(digitalTwinStateManager.getDigitalTwinState());
    }

    /**
     * Notify the Shadowing Model Listener that the Shadowing Model is out of sync with the Physical Asset
     */
    protected void notifyShadowingOutOfSync(){
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingOutOfSync(digitalTwinStateManager.getDigitalTwinState());
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

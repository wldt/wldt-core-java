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
package it.wldt.core.model;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.core.event.*;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.adapter.physical.event.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.management.ResourceManager;
import it.wldt.storage.StorageManager;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class implement the shadowing process (also known as replication of digitalization) responsible to keep the
 * Digital Twin State synchronized with that of the corresponding physical resource
 * according to what is defined by the Model. It handles:
 *  - Physical Asset Description Management
 *  - Digital Twin State Management
 *  - Life Cycle Management
 *  - Incoming and outgoing events of both Physical and Digital Adapters
 */
public abstract class ShadowingFunction implements WldtEventListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(ShadowingFunction.class);

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
     * @param id Unique Identifier of the Shadowing Model Function
     */
    public ShadowingFunction(String id){
        this.id = id;
        this.physicalEventsFilter = new WldtEventFilter();
    }

    /**
     * Initialize the Shadowing Model Function with the current Digital Twin State Manager
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
     *
     * @param physicalAssetProperty
     * @throws EventBusException
     * @throws ModelException
     */
    protected void observePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, ModelException {
        if(physicalAssetProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void observePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, ModelException {

        if(physicalAssetPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

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
     * @throws ModelException If the provided PhysicalAssetProperty is NULL
     */
    protected void unObservePhysicalAssetProperty(PhysicalAssetProperty<?> physicalAssetProperty) throws EventBusException, ModelException {

        if(physicalAssetProperty == null)
            throw new ModelException("Error ! NULL PhysicalProperty ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetProperties(List<PhysicalAssetProperty<?>> physicalAssetPropertyList) throws EventBusException, ModelException {

        if(physicalAssetPropertyList == null)
            throw new ModelException("Error ! NULL PhysicalProperty List ...");

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
     * @throws ModelException If the provided PhysicalAssetEvent is NULL
     */
    protected void observePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, ModelException {
        if(physicalAssetEvent == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void observePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, ModelException {

        if(physicalAssetEventList == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent List ...");

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
     * @throws ModelException If the provided PhysicalAssetEvent is NULL
     */
    protected void unObservePhysicalAssetEvent(PhysicalAssetEvent physicalAssetEvent) throws EventBusException, ModelException {

        if(physicalAssetEvent == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetEvents(List<PhysicalAssetEvent> physicalAssetEventList) throws EventBusException, ModelException {

        if(physicalAssetEventList == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent List ...");

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
     * @throws ModelException If the provided PhysicalAssetRelationship is NULL
     */
    protected void observePhysicalAssetRelationship(PhysicalAssetRelationship<?> physicalAssetRelationship) throws EventBusException, ModelException {
        if(physicalAssetRelationship == null)
            throw new ModelException("Error ! NULL Physical Relationship ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void observePhysicalAssetRelationships(List<PhysicalAssetRelationship<?>> physicalAssetRelationships) throws ModelException, EventBusException {
        if(physicalAssetRelationships == null)
            throw new ModelException("Error ! NULL PhysicalAssetRelationship List ...");

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
     * @throws ModelException If the provided PhysicalAssetRelationship is NULL
     */
    protected void unObservePhysicalAssetRelationship(PhysicalAssetRelationship<?> physicalAssetRelationship) throws EventBusException, ModelException {

        if(physicalAssetRelationship == null)
            throw new ModelException("Error ! NULL PhysicalAssetRelationship ...");

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
     * @throws ModelException If the provided list is NULL
     */
    protected void unObservePhysicalAssetRelationships(List<PhysicalAssetRelationship<?>> physicalAssetRelationshipList) throws EventBusException, ModelException {

        if(physicalAssetRelationshipList == null)
            throw new ModelException("Error ! NULL PhysicalAssetEvent List ...");

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
        logger.info("Shadowing Model Function -> Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Shadowing Model Function -> Unsubscribed from: {}", eventType);
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

    abstract protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage);

    abstract protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent);

    abstract protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent);

    abstract protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent);

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
        ShadowingFunction that = (ShadowingFunction) o;
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

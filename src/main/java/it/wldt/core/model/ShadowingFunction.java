package it.wldt.core.model;


import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.adapter.physical.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class implement the shadowing process (also known as replication of digitalization) responsible to keep the
 * Digital Twin State synchronized with that of the corresponding physical resource
 * according to what is defined by the Model. It handles:
 *  - Physical Asset Description Management
 *  - Digital Twin State Management
 *  - Life Cycle Management
 *  - Incoming and outgoing events of both Physical and Digital Adapters
 */
public abstract class ShadowingFunction implements WldtEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ShadowingFunction.class);

    private String id = null;

    private WldtEventFilter physicalEventsFilter = null;

    protected DigitalTwinStateManager digitalTwinStateManager = null;

    private ShadowingModelListener shadowingModelListener;

    public ShadowingFunction(String id){
        this.id = id;
        this.physicalEventsFilter = new WldtEventFilter();
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
     *
     * @param physicalAssetPropertyList
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetProperty
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetPropertyList
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetEvent
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetEventList
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetEvent
     * @throws EventBusException
     * @throws ModelException
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
     *
     * @param physicalAssetEventList
     * @throws EventBusException
     * @throws ModelException
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

    protected void observePhysicalAssetRelationship(PhysicalAssetRelationship<?> physicalAssetRelationship) throws EventBusException, ModelException {
        if(physicalAssetRelationship == null)
            throw new ModelException("Error ! NULL Physical Relationship ...");

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceCreatedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceCreatedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));
        wldtEventFilter.add(PhysicalAssetRelationshipInstanceDeletedWldtEvent.buildEventType(PhysicalAssetRelationshipInstanceDeletedWldtEvent.EVENT_BASIC_TYPE, physicalAssetRelationship.getName()));

        //Save the adopted EventFilter
        this.physicalEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

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

    protected void observeDigitalActionEvents() throws EventBusException {
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalAdapter.DIGITAL_ACTION_EVENT);
        WldtEventBus.getInstance().subscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

    protected void unObserveDigitalActionEvents() throws EventBusException {
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalAdapter.DIGITAL_ACTION_EVENT);
        WldtEventBus.getInstance().unSubscribe(this.digitalTwinStateManager.getDigitalTwinId(), this.id, wldtEventFilter, this);
    }

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

        logger.info("Shadowing Model Function -> Received Event: {} Class: {}", wldtEvent, wldtEvent.getClass());

        if(wldtEvent instanceof PhysicalAssetPropertyWldtEvent)
            onPhysicalAssetPropertyVariation((PhysicalAssetPropertyWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetEventWldtEvent)
            onPhysicalAssetEventNotification((PhysicalAssetEventWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceCreatedWldtEvent)
            onPhysicalAssetRelationshipEstablished((PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) wldtEvent);

        if(wldtEvent instanceof PhysicalAssetRelationshipInstanceDeletedWldtEvent)
            onPhysicalAssetRelationshipDeleted((PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) wldtEvent);

        if(wldtEvent.getType().equals(DigitalAdapter.DIGITAL_ACTION_EVENT))
            onDigitalActionEvent((DigitalActionWldtEvent<?>) wldtEvent.getBody());

    }

    protected void init(DigitalTwinStateManager digitalTwinStateManager){
        this.digitalTwinStateManager = digitalTwinStateManager;
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

    protected void notifyShadowingSync(){
        if(getShadowingModelListener() != null)
            getShadowingModelListener().onShadowingSync(digitalTwinStateManager.getDigitalTwinState());
    }

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

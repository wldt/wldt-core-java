package it.wldt.adapter.digital;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.state.*;
import it.wldt.core.worker.WldtWorker;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class defines the core capabilities and responsibilities of a Digital Adapter in the Digital Twin's Instance
 * A Digital Adapter is the architectural component in charge of handling the interaction of the Digital Twin
 * with the external digital world exposing information coming from the Digital Twin State and receiving information and
 * action from other digital applications or twins.
 *
 * This class can be extended in order to create custom Digital Adapter shaping specific behaviours of the twin and
 * allowing a simplified interaction with the external digital world.
 *
 * Multiple Digital Adapters can be active at the same time on the Digital Twin with the aim to handle different
 * interaction with the digital layer according to the nature of the twin and its operation context.
 */
public abstract class DigitalAdapter<C> extends WldtWorker implements WldtEventListener, LifeCycleListener {

    public static final String DIGITAL_ACTION_EVENT = "da.digital.action.event";

    private static final Logger logger = LoggerFactory.getLogger(DigitalAdapter.class);

    private String id = null;

    private C configuration;

    private WldtEventFilter statePropertiesWldtEventFilter = null;

    private WldtEventFilter stateActionsWldtEventFilter = null;

    private WldtEventFilter stateEventsAvailabilityWldtEventFilter = null;

    private WldtEventFilter stateTargetPropertyWldtEventsFilter = null;

    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    private WldtEventFilter stateRelationshipsWldtEventFilter;

    private final Map<String, WldtEventFilter> stateTargetRelationshipWldtEventFilter = new HashMap<>();

    private boolean observeDigitalTwinState = true;

    protected DigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    private final WldtEventListener digitalTwinStateWldtEventListener = new WldtEventListener() {
        @Override
        public void onEventSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEventUnSubscribed(String eventType) {
            //TODO Implement
        }

        @Override
        public void onEvent(WldtEvent<?> wldtEvent) {

            logger.debug("{} - Digital Adapter - Received Event: {}", getId(), wldtEvent);

            //DT State Events Management
            if(wldtEvent != null
                    && wldtEvent.getType().equals(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType())
                    && wldtEvent.getBody() != null
                    && (wldtEvent.getBody() instanceof DigitalTwinState)){

                //Retrieve DT's State Update
                DigitalTwinState newDigitalTwinState = (DigitalTwinState)wldtEvent.getBody();

            }

            ///////// DT STATE EVENTS NOTIFICATION MANAGEMENT ///////////
            if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEventNotification)) {
                DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();
                onDigitalTwinStateEventNotificationReceived(digitalTwinStateEventNotification);
            }

        }
    };

    private DigitalAdapter(){}

    public DigitalAdapter(String id, C configuration){
        this.id = id;
        this.configuration = configuration;
    }

    public DigitalAdapter(String id, boolean observeDigitalTwinState){
        this.id = id;
        this.observeDigitalTwinState = observeDigitalTwinState;
    }


    //////////////////////// PROPERTIES OBSERVATION ///////////////////////////////////////////////////////////////////
    /**
     * Enable the observation of all the Digital Twin State properties, when they are created, updated and deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateProperties() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of all the Digital Twin State properties, when they are created, updated and deleted.
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateProperties() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Enable the observation of a specific list of Digital Twin State properties, when they are updated and/or deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @param propertyList
     * @throws EventBusException
     */
    protected void observeTargetDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String propertyKey : propertyList) {
            wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
            wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);

    }

    /**
     * Cancel the observation of a target list of properties
     * @throws EventBusException
     */
    protected void unObserveTargetDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String propertyKey : propertyList) {
            wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
            wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));
        }

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of a single Digital Twin State properties, when it is updated and/or deleted.
     * With respect to properties an update contains the new value and no additional observations are required
     * @param propertyKey
     * @throws EventBusException
     */
    protected void observeDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
        wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target property
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinProperty(String propertyKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
        wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));

        if(stateTargetPropertyWldtEventsFilter == null)
            this.stateTargetPropertyWldtEventsFilter = new WldtEventFilter();

        this.stateTargetPropertyWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// ACTIONS OBSERVATION /////////////////////////////////////////////////////////
    /**
     * Enable the observation of available Digital Twin State Actions.
     * Callbacks will be received when an action is enabled, updated or disable.
     * The update of an action is associated to the variation of its signature and declaration and it is not associated
     * to any attached payload or value.
     *
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateActionsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_ENABLED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_DISABLED);

        //Save the adopted EventFilter
        this.stateActionsWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of Digital Twin State Actions
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateActionsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_ENABLED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_ACTION_DISABLED);

        //Save the adopted EventFilter
        this.stateActionsWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    protected <T> void publishDigitalActionWldtEvent(String actionKey, T body) throws EventBusException {
        WldtEvent<DigitalActionWldtEvent<T>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, new DigitalActionWldtEvent<>(actionKey, body));
        WldtEventBus.getInstance().publishEvent(this.id, notification);
    }

    protected void publishDigitalActionWldtEvent(DigitalActionWldtEvent<?> actionWldtEvent) throws EventBusException {
        WldtEvent<DigitalActionWldtEvent<?>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, actionWldtEvent);
        WldtEventBus.getInstance().publishEvent(this.id, notification);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// EVENTS OBSERVATION //////////////////////////////////////////////////////////
    /**
     * Enable the observation of available Digital Twin State Events.
     * Callbacks will be received when an event is registered, updated or unregistered.
     * The update of an event is associated to the variation of its signature and declaration and it is not associated
     * to any attached payload or value.
     *
     * @throws EventBusException
     */
    protected void observeDigitalTwinStateEventsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_REGISTERED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_REGISTRATION_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_UNREGISTERED);

        //Save the adopted EventFilter
        this.stateEventsAvailabilityWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of Digital Twin State Events
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinStateEventsAvailability() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_REGISTERED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_REGISTRATION_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_EVENT_UNREGISTERED);

        //Save the adopted EventFilter
        this.stateEventsAvailabilityWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Enable the observation of the notification associated to a specific list of Digital Twin State events.
     * With respect to event a notification contains the new associated value
     * @param eventsList
     * @throws EventBusException
     */
    protected void observeDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter = new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    /**
     * Cancel the observation of a target list of properties
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of the notification associated to a single Digital Twin State event.
     * With respect to event a notification contains the new associated value
     * @param eventKey
     * @throws EventBusException
     */
    protected void observeDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target event
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(digitalTwinState.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.id, wldtEventFilter, this);
    }

    //////////////////////// RELATIONSHIPS OBSERVATION //////////////////////////////////////////////////////////

    protected void observeDigitalTwinRelationshipsAvailability() throws EventBusException {
        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_RELATIONSHIP_CREATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_RELATIONSHIP_DELETED);

        //Save the adopted EventFilter
        this.stateRelationshipsWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    protected void unObserveDigitalTwinRelationshipsAvailability() throws EventBusException {
        WldtEventBus.getInstance().unSubscribe(this.id, stateRelationshipsWldtEventFilter, digitalTwinStateWldtEventListener);
    }

    protected void observeDigitalTwinRelationship(String relationshipName) throws EventBusException {
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(digitalTwinState.getRelationshipInstanceCreatedWldtEventMessageType(relationshipName));
        wldtEventFilter.add(digitalTwinState.getRelationshipInstanceDeletedWldtEventMessageType(relationshipName));

        this.stateTargetRelationshipWldtEventFilter.put(relationshipName, wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, digitalTwinStateWldtEventListener);
    }

    protected void unObserveDigitalTwinRelationship(String relationshipName) throws EventBusException {
        WldtEventFilter relationshipFilter = this.stateTargetRelationshipWldtEventFilter.remove(relationshipName);
        if(relationshipFilter != null)
            WldtEventBus.getInstance().unSubscribe(this.id, relationshipFilter, digitalTwinStateWldtEventListener);
    }

    protected void observeDigitalTwinRelationships(List<String> relationshipList){
        relationshipList.forEach(s -> {
            try {
                observeDigitalTwinRelationship(s);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
        });
    }

    protected void unObserveDigitalTwinRelationships(List<String> relationshipList){
        relationshipList.forEach(s -> {
            try {
                unObserveDigitalTwinRelationship(s);
            } catch (EventBusException e) {
                e.printStackTrace();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners
     * when there is an issue in the binding with the Digital Asset.
     *
     * @param errorMessage
     */
    protected void notifyDigitalAdapterUnBound(String errorMessage){
        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), errorMessage);
    }

    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners when
     * the adapter is ready to work and correctly bound to the associated external digital services.
     *
     */
    protected void notifyDigitalAdapterBound() {
        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterBound(getId());
    }

    //////////////////////// DIGITAL TWIN STATE UPDATE  //////////////////////////////////////////////////////////
    abstract protected void onStateUpdate(DigitalTwinState newDigitalTwinState,
                                          DigitalTwinState previousDigitalTwinState,
                                          ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList);


    //////////////////////// EVENTS NOTIFICATION CALLBACK /////////////////////////////////////////////////////
    abstract protected void onDigitalTwinStateEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


    //////////////////////// ADAPTER CALLBACKS /////////////////////////////////////////////////////
    public abstract void onAdapterStart();

    public abstract void onAdapterStop();


    //////////////////////// DT CALLBACKS /////////////////////////////////////////////////////
    public abstract void onDigitalTwinSync(DigitalTwinState digitalTwinState);

    public abstract void onDigitalTwinUnSync(DigitalTwinState digitalTwinState);

    public abstract void onDigitalTwinCreate();

    public abstract void onDigitalTwinStart();

    public abstract void onDigitalTwinStop();

    public abstract void onDigitalTwinDestroy();

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{
            onAdapterStart();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            unObserveDigitalTwinStateProperties();
            onAdapterStop();
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), null);
        }catch (Exception e){
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), e.getLocalizedMessage());
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public void setConfiguration(C configuration) {
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public DigitalAdapterListener getDigitalAdapterListener() {
        return digitalAdapterListener;
    }

    public void setDigitalAdapterListener(DigitalAdapterListener digitalAdapterListener) {
        this.digitalAdapterListener = digitalAdapterListener;
    }

    public DigitalAdapterLifeCycleListener getDigitalAdapterLifeCycleListener() {
        return digitalAdapterLifeCycleListener;
    }

    public void setDigitalAdapterLifeCycleListener(DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener) {
        this.digitalAdapterLifeCycleListener = digitalAdapterLifeCycleListener;
    }

    public void removeDigitalAdapterLifeCycleListener(){
        this.digitalAdapterLifeCycleListener = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalAdapter that = (DigitalAdapter) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("UnSubscribed from: {}", eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {
        if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)){
            DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) wldtEvent.getBody();
            if(wldtEvent.getType().equals(digitalTwinState.getPropertyCreatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStateChangePropertyCreated(digitalTwinStateProperty);
            else if(wldtEvent.getType().equals(digitalTwinState.getPropertyUpdatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyUpdated(digitalTwinStateProperty);
            else if(wldtEvent.getType().equals(digitalTwinState.getPropertyDeletedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                onStatePropertyDeleted(digitalTwinStateProperty);
            else
                logger.error(String.format("Digital Adapter (%s) -> observeDigitalTwinProperties: Error received type %s that is not matching", id, wldtEvent.getType()));
        }
    }

    @Override
    public void onSync(IDigitalTwinStateManager digitalTwinState) {

        logger.info("Digital Adapter ({}) Received DT onSync callback ! Ready to start ...", this.id);

        this.digitalTwinState = digitalTwinState;

        onDigitalTwinSync(digitalTwinState);

        try{
            if(observeDigitalTwinState) {
                observeDigitalTwinStateProperties();
                observeDigitalTwinStateActionsAvailability();
                observeDigitalTwinStateEventsAvailability();
                observeDigitalTwinRelationshipsAvailability();
            }
        }catch (Exception e){
            logger.error(String.format("Digital Adapter (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(IDigitalTwinStateManager digitalTwinState) {
        logger.debug("Digital Adapter ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinUnSync(digitalTwinState);
        this.digitalTwinState = null;
    }

    @Override
    public void onCreate() {
        onDigitalTwinCreate();
    }

    @Override
    public void onStart() {
        onDigitalTwinStart();
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalAdapterBound(adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalAdapterUnBound(adapterId, errorMessage);
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap,errorMessage);
    }

    @Override
    public void onStop() {
        onDigitalTwinStop();
    }

    @Override
    public void onDestroy() {
        onDigitalTwinDestroy();
    }
}

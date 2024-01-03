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
import it.wldt.exception.WldtDigitalTwinStateEventException;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

    protected DigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    private DigitalAdapter() {
    }

    public DigitalAdapter(String id, C configuration) {
        super();
        this.id = id;
        this.configuration = configuration;
    }

    public DigitalAdapter(String id) {
        super();
        this.id = id;
    }


    //////////////////////// PROPERTIES OBSERVATION ///////////////////////////////////////////////////////////////////
    /**
     * Enable the observation of all the Digital Twin State and any of its variations in terms of Properties, Actions,
     * Events and Relationships.
     * @throws EventBusException
     */
    protected void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of all the Digital Twin State variations and updates.
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }


    /////////////////////////////// DIGITAL ACTION EVENT MANAGEMENT ///////////////////////////////////////////////////

    /**
     *
     * Publish an event associated to a Digital Action performed on the Digital Adapter and that should be forwarded
     * to the DT's core
     *
     * @param actionKey
     * @param body
     * @param <T>
     * @throws EventBusException
     */
    protected <T> void publishDigitalActionWldtEvent(String actionKey, T body) throws EventBusException {
        WldtEvent<DigitalActionWldtEvent<T>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, new DigitalActionWldtEvent<>(actionKey, body));
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, notification);
    }

    /**
     * Publish an event associated to a Digital Action performed on the Digital Adapter and that should be forwarded
     * to the DT's core
     * @param actionWldtEvent
     * @throws EventBusException
     */
    protected void publishDigitalActionWldtEvent(DigitalActionWldtEvent<?> actionWldtEvent) throws EventBusException {
        WldtEvent<DigitalActionWldtEvent<?>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, actionWldtEvent);
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, notification);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// EVENTS OBSERVATION //////////////////////////////////////////////////////////
    /**
     * Enable the observation of available Digital Twin State Events Notifications.
     * @throws EventBusException
     */
    protected void observeAllDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState) throws EventBusException, WldtDigitalTwinStateEventException {

        if(digitalTwinState != null && digitalTwinState.getEventList().isPresent()){
            this.observeDigitalTwinEventsNotifications(digitalTwinState.getEventList().get().stream().map(DigitalTwinStateEvent::getKey).collect(Collectors.toList()));
        }
        else
            throw new WldtDigitalTwinStateEventException("Error observing All DT Event Notifications ! Provided DT State = null !");
    }

    /**
     * Cancel the observation of Digital Twin State Events Notifications
     * @throws EventBusException
     */
    protected void unObserveAllDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState) throws EventBusException, WldtDigitalTwinStateEventException {

        if(digitalTwinState != null && digitalTwinState.getEventList().isPresent()){
            this.unObserveDigitalTwinEventsNotifications(digitalTwinState.getEventList().get().stream().map(DigitalTwinStateEvent::getKey).collect(Collectors.toList()));
        }
        else
            throw new WldtDigitalTwinStateEventException("Error observing All DT Event Notifications ! Provided DT State = null !");
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
            wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter = new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a target list of properties
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
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

        wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target event
     * @throws EventBusException
     */
    protected void unObserveDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
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
    abstract protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


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
            unObserveDigitalTwinState();
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

        logger.debug("{} - Digital Adapter - Received Event: {}", getId(), wldtEvent);

        //DT State Events Management
        if(wldtEvent != null
                && wldtEvent.getType().equals(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType())
                && wldtEvent.getBody() != null
                && (wldtEvent.getBody() instanceof DigitalTwinState)){

            //Retrieve DT's State Update
            DigitalTwinState newDigitalTwinState = (DigitalTwinState)wldtEvent.getBody();
            DigitalTwinState previsousDigitalTwinState = null;
            ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList = null;
            Optional<?> prevDigitalTwinStateOptional = wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE);
            Optional<?> digitalTwinStateChangeListOptional = wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST);

            if(prevDigitalTwinStateOptional.isPresent() && prevDigitalTwinStateOptional.get() instanceof DigitalTwinState)
                previsousDigitalTwinState = (DigitalTwinState) prevDigitalTwinStateOptional.get();

            if(digitalTwinStateChangeListOptional.isPresent())
                digitalTwinStateChangeList = (ArrayList<DigitalTwinStateChange>) digitalTwinStateChangeListOptional.get();

            onStateUpdate(newDigitalTwinState, previsousDigitalTwinState, digitalTwinStateChangeList);
        }

        ///////// DT STATE EVENTS NOTIFICATION MANAGEMENT ///////////
        if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEventNotification)) {
            DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();
            logger.debug("Received Event Notification: {}", digitalTwinStateEventNotification);
            onEventNotificationReceived(digitalTwinStateEventNotification);
        }

    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {

        try{

            logger.info("Digital Adapter ({}) Received DT onSync callback ! Ready to start ...", this.id);

            this.digitalTwinState = digitalTwinState;

            //Notify about the first available Digital Twin State
            onDigitalTwinSync(digitalTwinState);

            //By default, the Digital Adapter observer all the variation on the DT State
            observeDigitalTwinState();

        }catch (Exception e){
            logger.error(String.format("Digital Adapter (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
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

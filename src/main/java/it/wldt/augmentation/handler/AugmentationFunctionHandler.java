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
package it.wldt.augmentation.handler;

import it.wldt.adapter.digital.DigitalAdapterLifeCycleListener;
import it.wldt.adapter.digital.DigitalAdapterListener;
import it.wldt.augmentation.event.AugmentationFunctionRegistrationWldtEvent;
import it.wldt.augmentation.event.AugmentationFunctionUnRegistrationWldtEvent;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.listener.AugmentationLifeCycleListener;
import it.wldt.augmentation.listener.StatefulAugmentationResultListener;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.core.event.*;
import it.wldt.core.state.*;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authors: Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 12/02/2026
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * TODO WRITE ..
 */
public abstract class AugmentationFunctionHandler extends DigitalTwinWorker implements StatefulAugmentationResultListener, WldtEventListener, AugmentationLifeCycleListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationFunctionHandler.class);

    private String id = null;

    private WldtEventFilter stateVariationWldtEventFilter = null;

    private WldtEventFilter augmentationFunctionWldtEventFilter = null;

    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    protected DigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    // Query Executor to send query to the storage layer in both synchronous and asynchronous way
    protected QueryExecutor queryExecutor = null;

    /**
     * Constructor of the AugmentationFunctionHandler class.
     * It is protected to allow the extension of the class and the creation of custom Augmentation Managers.
     */
    private AugmentationFunctionHandler() {
        super();
    }

    /**
     * Constructor of the AugmentationFunctionHandler class with the id of the Manager.
     * Receives the id of the Augmentation Function Manager and set it as the id of the Manager.
     * @param id the id of the Augmentation Manager
     */
    public AugmentationFunctionHandler(String id) {
        this();
        this.id = id;
    }

    /**
     * This method allow the registration for the event associated to the target Handler
     * @param baseType
     * @return
     */
    private String buildHandlerWildCardEventType(String baseType){
        return String.format("%s.%s.%s", baseType, this.id, WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE);
    }

    /**
     * This method allow the registration for the event associated to the target Handler
     * @param baseType
     * @return
     */
    private String buildHandlerEventType(String baseType){
        return String.format("%s.%s", baseType, this.id);
    }

    /**
     * TODO ...
     * @param resultList
     */
    private void notifyAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList) throws EventBusException {

        // Build the Event Type for the Augmentation Function Result associated to the target Handler and the target Augmentation Function Id
        String eventType = String.format("%s.%s", buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE), augmentationFunctionId);

        // Create the Event associated to the Augmentation Function Result
        WldtEvent<List<AugmentationFunctionResult<?>>> augmentationFunctionResultEvent = new WldtEvent<>(
                eventType,
                resultList
        );

        // Publish the Event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, augmentationFunctionResultEvent);

    }

    /**
     * Enable the observation of all the Digital Twin State and any of its variations in terms of Properties, Actions,
     * Events and Relationships.
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeAugmentationFunctionEvents() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTION_BASE_TYPE));

        //Save the adopted EventFilter
        this.augmentationFunctionWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of all the Digital Twin State variations and updates.
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveAugmentationFunctionEvents() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTION_BASE_TYPE));

        //Save the adopted EventFilter
        this.augmentationFunctionWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of all the Digital Twin State and any of its variations in terms of Properties, Actions,
     * Events and Relationships.
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Save the adopted EventFilter
        this.stateVariationWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of all the Digital Twin State variations and updates.
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Save the adopted EventFilter
        this.stateVariationWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of available Digital Twin State Events Notifications.
     * @param digitalTwinState the Digital Twin State to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
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
     * @param digitalTwinState the Digital Twin State to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
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
     * @param eventsList the list of events to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
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
     * @param eventsList the list of events to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
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
     * @param eventKey the key of the event to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
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
     * @param eventKey the key of the event to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
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
     * @param errorMessage the error message to be notified to the listeners
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

    // ========================================
    // Augmentation Function Management Methods
    // ========================================
    // The following abstract methods define the methods for the management, registration and unregistration
    // of augmentation functions through their Descriptions. The implementation of these methods is left to the
    // concrete implementation of the Augmentation Function Manager

    /**
     * TODO ...
     * @param augmentationFunction
     * @throws EventBusException
     */
    private void notifyAugmentationFunctionRegistered(AugmentationFunction augmentationFunction) throws EventBusException {
        // Create the Event associated to the registration of the Augmentation Function
        AugmentationFunctionRegistrationWldtEvent event = new AugmentationFunctionRegistrationWldtEvent(this.id, augmentationFunction);

        // Notify the registration of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, event);
    }

    /**
     * TODO ...
     * @param augmentationFunction
     * @throws EventBusException
     */
    private void notifyAugmentationFunctionUnRegistered(AugmentationFunction augmentationFunction) throws EventBusException {

        // Create the Event associated to the unregistration of the Augmentation Function
        AugmentationFunctionUnRegistrationWldtEvent event = new AugmentationFunctionUnRegistrationWldtEvent(this.id, augmentationFunction);

        // Notify the unregistration of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, event);
    }

    /**
     * TODO ...
     * @param augmentationFunction
     * @throws AugmentationFunctionException
     */
    public void registerAugmentationFunction(AugmentationFunction augmentationFunction) throws AugmentationFunctionException {
        // This method is public and not abstract since it can contain common logic for the registration of the
        // Augmentation Function, while the handler is protected and abstract since it contains the specific logic
        // for the registration of the Augmentation Function that can be different for each

        try{

            // If the Augmentation Function is Stateful, set the listener for the result of the Augmentation Function to
            // the current Handler to allow the notification of the result of the function execution
            if(augmentationFunction.getType().equals(AugmentationFunctionType.STATEFUL) && augmentationFunction instanceof StatefulAugmentationFunction)
                ((StatefulAugmentationFunction) augmentationFunction).setResultListener(this);

            // Call the handler for the registration of the Augmentation Function
            handleAugmentationFunctionRegistration(augmentationFunction);

            // Notify the registration of the Augmentation Function publishing the associated event on the EventBus
            notifyAugmentationFunctionRegistered(augmentationFunction);

        } catch (Exception e){
            e.printStackTrace();
            throw new AugmentationFunctionException(String.format("Error registering Augmentation Function with id %s: %s", augmentationFunction.getId(), e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    public void unRegisterAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {
        // This method is public and not abstract since it can contain common logic for the unregistration of the
        // Augmentation Function, while the handler is protected and abstract since it contains the specific logic
        // for the unregistration of the Augmentation Function that can be different for each

        // Retrieve the Augmentation Function associated to the received id to be able to publish it in the unregistration event, if not present create a new one with only the id
        Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

        // If the Augmentation Function is not registered throw an exception since it cannot be unregistered
        if(!augmentationFunctionOptional.isPresent())
            throw new AugmentationFunctionException(String.format("Error unregistering Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));

        // Get the Augmentation Function to be unregistered
        AugmentationFunction augmentationFunction = augmentationFunctionOptional.get();

        try{
            // Call the handler for the unregistration of the Augmentation Function
            handleAugmentationFunctionUnRegistration(augmentationFunctionId);

            // Notify the unregistration of the Augmentation Function publishing the associated event on the EventBus
            notifyAugmentationFunctionUnRegistered(augmentationFunction);

        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error unregistering Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    abstract public Optional<AugmentationFunction> getAugmentationFunction(String augmentationFunctionId);

    /**
     * TODO ...
     * @param augmentationFunction
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionRegistration(AugmentationFunction augmentationFunction) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionUnRegistration(String augmentationFunctionId) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param augmentationFunctionContext
     * @throws AugmentationFunctionException
     */
    public void startAugmentationFunction(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException {
        try{
            // Call the handler for the start of the Augmentation Function
            handleAugmentationFunctionStart(augmentationFunctionId, augmentationFunctionContext);
        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error starting Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param augmentationFunctionContext
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionStart(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    public void stopAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {
        try{
            // Call the handler for the stop of the Augmentation Function
            handleAugmentationFunctionStop(augmentationFunctionId);
        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error stopping Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }


    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionStop(String augmentationFunctionId) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param augmentationFunctionContext
     * @throws AugmentationFunctionException
     */
    public void executeAugmentationFunction(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException {
        try{
            // Call the handler for the execution of the Augmentation Function
            List<AugmentationFunctionResult<?>> resultList = handleAugmentationFunctionExecution(augmentationFunctionId, augmentationFunctionContext);

            // Notify through and event the result of the augmentation function execution
            notifyAugmentationFunctionResult(augmentationFunctionId, resultList);

        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param augmentationFunctionContext
     * @throws AugmentationFunctionException
     */
    abstract protected List<AugmentationFunctionResult<?>> handleAugmentationFunctionExecution(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @return
     */
    abstract public List<AugmentationFunction> getAllAugmentationFunctions();

    //////////////////////// DIGITAL TWIN STATE UPDATE  //////////////////////////////////////////////////////////
    abstract protected void onStateUpdate(DigitalTwinState newDigitalTwinState,
                                          DigitalTwinState previousDigitalTwinState,
                                          ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList);


    //////////////////////// EVENTS NOTIFICATION CALLBACK /////////////////////////////////////////////////////
    abstract protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


    //////////////////////// ADAPTER CALLBACKS /////////////////////////////////////////////////////
    public abstract void onManagerStart();

    public abstract void onManagerStop();


    //////////////////////// DT CALLBACKS /////////////////////////////////////////////////////
    public abstract void onDigitalTwinLifeCycleSync(DigitalTwinState digitalTwinState);

    public abstract void onDigitalTwinLifeCycleUnSync(DigitalTwinState digitalTwinState);

    public abstract void onDigitalTwinLifeCycleCreate();

    public abstract void onDigitalTwinLifeCycleStart();

    public abstract void onDigitalTwinLifeCycleStop();

    public abstract void onDigitalTwinLifeCycleDestroy();

    public abstract void onDigitalTwinLifeCycleBound();

    public abstract void onDigitalTwinLifeCycleUnBound();

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{

            // Init the Query Executor
            if(this.queryExecutor == null)
                this.queryExecutor = new QueryExecutor(this.digitalTwinId, String.format("query-executor-%s", this.id));

            // Once started the handler is ready to observe augmentation function events
            observeAugmentationFunctionEvents();

            // Notify the handler implementation about the start of the Manager to allow it to execute
            // custom logics at the start of the Manager
            onManagerStart();

        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            unObserveDigitalTwinState();
            unObserveAugmentationFunctionEvents();
            onManagerStop();
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), null);
        }catch (Exception e){
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), e.getLocalizedMessage());
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
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
        AugmentationFunctionHandler that = (AugmentationFunctionHandler) o;
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

        logger.debug("{} - Augmentation Manager - Received Event: {}", getId(), wldtEvent);

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

        ////////// AUGMENTATION FUNCTION EXECUTION EVENTS MANAGEMENT ///////////
        if(wldtEvent != null
                && wldtEvent.getType() != null
                && (wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTION_BASE_TYPE))
                && wldtEvent.getBody() != null
                && (wldtEvent.getBody() instanceof AugmentationFunctionContext)){

            // Retrieve Augmentation Function Execution Context
            AugmentationFunctionContext augmentationFunctionContext = (AugmentationFunctionContext) wldtEvent.getBody();

            // Extract the Augmentation Function Id from the Event Type after the base type and the handler id
            // Substring after the base type and the handler id considering the '.' as separator
            String augmentationFunctionId = wldtEvent.getType().substring(buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTION_BASE_TYPE).length() + 1);

            logger.info("Received Augmentation Function Execution Event for function with id {} and context: {}", augmentationFunctionId, augmentationFunctionContext);

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error executing Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            }
            else {
                try {

                    // Check if the Augmentation Function is Stateless since it is an execution request, if not log the error and skip the execution
                    if(!isAugmentationFunctionExecutionRequestValid(augmentationFunctionOptional.get(), augmentationFunctionContext)){
                        logger.warn(String.format("Error executing Augmentation Function with id %s is not valid !", augmentationFunctionId));
                    } else{
                        logger.info("Executing Augmentation Function with id {} ...", augmentationFunctionId);
                        executeAugmentationFunction(augmentationFunctionId, augmentationFunctionContext);
                    }
                } catch (AugmentationFunctionException e) {
                    logger.error(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
                }
            }
        }
    }

    /**
     * TODO
     */
    private boolean isAugmentationFunctionExecutionRequestValid(AugmentationFunction augmentationFunction,
                                                                 AugmentationFunctionContext augmentationFunctionContext) {

        // Validate that the Augmentation Function and the Context are not null
        if(augmentationFunction == null || augmentationFunctionContext == null) {
            logger.error("Invalid Augmentation Function Request ! Augmentation Function and Context cannot be null !");
            return false;
        }

        // Validate that the Augmentation Function is Stateless since it is an execution request
        if(!augmentationFunction.getType().equals(AugmentationFunctionType.STATELESS)){
            logger.error(String.format("Invalid Augmentation Function Request for function with id %s: Augmentation Function is not Stateless, execution request is not allowed !", augmentationFunction.getId()));
            return false;
        }

        // The request is valid
        return true;
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param resultList
     */
    @Override
    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList) {
        try{
            logger.info("Received result for Stateful Augmentation Function with id {}: {}", augmentationFunctionId, resultList);

            // Notify the result of the execution of the Stateful Augmentation Function through an event on the EventBus
            notifyAugmentationFunctionResult(augmentationFunctionId, resultList);

        } catch (Exception e){
            logger.error(String.format("Error while notifying result for Stateful Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {

        try{

            logger.info("Augmentation Manager ({}) Received DT onSync callback ! Ready to start ...", this.id);

            this.digitalTwinState = digitalTwinState;

            //Notify about the first available Digital Twin State
            onDigitalTwinLifeCycleSync(digitalTwinState);

            //By default, the Augmentation Manager observer all the variation on the DT State
            observeDigitalTwinState();

        }catch (Exception e){
            logger.error(String.format("Augmentation Manager (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
        logger.debug("Augmentation Manager ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinLifeCycleUnSync(digitalTwinState);
        this.digitalTwinState = null;
    }

    @Override
    public void onCreate() {
        onDigitalTwinLifeCycleCreate();
    }

    @Override
    public void onStart() {
        onDigitalTwinLifeCycleStart();
    }

    @Override
    public void onDigitalTwinBound() { onDigitalTwinLifeCycleBound(); }

    @Override
    public void onDigitalTwinUnBound() { onDigitalTwinLifeCycleUnBound(); }

    @Override
    public void onStop() {
        onDigitalTwinLifeCycleStop();
    }

    @Override
    public void onDestroy() {
        onDigitalTwinLifeCycleDestroy();
    }
}

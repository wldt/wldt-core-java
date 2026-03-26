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
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.event.*;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.listener.AugmentationLifeCycleListener;
import it.wldt.augmentation.listener.StatefulAugmentationListener;
import it.wldt.augmentation.listener.StatelessAugmentationListener;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
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
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Authors: Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 12/02/2026
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * TODO WRITE ..
 */
public abstract class AugmentationFunctionHandler extends DigitalTwinWorker implements StatelessAugmentationListener, StatefulAugmentationListener, WldtEventListener, AugmentationLifeCycleListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationFunctionHandler.class);

    private String id = null;

    private WldtEventFilter stateVariationWldtEventFilter = null;

    private WldtEventFilter augmentationFunctionWldtEventFilter = null;

    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    protected DigitalTwinState digitalTwinState = null;

    private DigitalAdapterListener digitalAdapterListener;

    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    public HashMap<String, AugmentationFunction> augmentationFunctionHashMap;

    // Query Executor to send query to the storage layer in both synchronous and asynchronous way
    protected QueryExecutor queryExecutor = null;

    /**
     * Constructor of the AugmentationFunctionHandler class.
     * It is protected to allow the extension of the class and the creation of custom Augmentation Managers.
     */
    private AugmentationFunctionHandler() {
        super();

        this.augmentationFunctionHashMap = new HashMap<>();
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

        // Create the Event associated to the result of the Augmentation Function
        AugmentationFunctionResultWldtEvent augmentationFunctionResultWldtEvent = new AugmentationFunctionResultWldtEvent(this.id, augmentationFunctionId, resultList);

        // Notify the result of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, augmentationFunctionResultWldtEvent);
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
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_QUERY_EXECUTION_BASE_TYPE));

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
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE));
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.AUGMENTATION_FUNCTION_QUERY_EXECUTION_BASE_TYPE));

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
     * TODO ...
     * @throws EventBusException
     */
    protected void observerAllDigitalTwinEventsNotification() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE));

        //Save the adopted EventFilter
        this.augmentationFunctionWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);

    }

    /**
     * TODO ...
     * @throws EventBusException
     */
    protected void unObserverAllDigitalTwinEventsNotification() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(buildHandlerWildCardEventType(WldtEventTypes.ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE));

        //Save the adopted EventFilter
        this.augmentationFunctionWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);

    }

    /**
     * Enable the observation of available Digital Twin State Events Notifications.
     * @param digitalTwinState the Digital Twin State to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState) throws EventBusException, WldtDigitalTwinStateEventException {

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
    protected void unObserveDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState) throws EventBusException, WldtDigitalTwinStateEventException {

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
     * TODO
     * @param augmentationFunctionError
     * @throws EventBusException
     */
    private void notifyAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) throws EventBusException {

        // Create the Event associated to the error of the Augmentation Function
        AugmentationFunctionErrorWldtEvent event = new AugmentationFunctionErrorWldtEvent(this.id, augmentationFunctionId, augmentationFunctionError);

        // Notify the error of the Augmentation Function publishing the associated event on the EventBus
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
                ((StatefulAugmentationFunction) augmentationFunction).setStatefulAugmentationListener(this);

            // If the Augmentation Function is Stateless, set the listener for the error notification of the Augmentation Function to
            // the current Handler to allow the notification of the error of the function execution
            if(augmentationFunction.getType().equals(AugmentationFunctionType.STATELESS) && augmentationFunction instanceof StatelessAugmentationFunction)
                ((StatelessAugmentationFunction) augmentationFunction).setStatelessAugmentationListener(this);

            if(this.augmentationFunctionHashMap.containsKey(augmentationFunction.getId()))
                throw new AugmentationFunctionException(String.format("Error registering Augmentation Function with id %s: Augmentation Function with the same id already registered !", augmentationFunction.getId()));
            // Add the Augmentation Function to the list of the managed Augmentation Functions by the Handler
            this.augmentationFunctionHashMap.put(augmentationFunction.getId(), augmentationFunction);

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

        // Remove the Augmentation Function from the list of the managed Augmentation Functions by the Handler
        this.augmentationFunctionHashMap.remove(augmentationFunctionId);

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
     * TODO
     * @param augmentationFunctionId
     * @return
     */
    public Optional<AugmentationFunction> getAugmentationFunction(String augmentationFunctionId) {

        if (!augmentationFunctionHashMap.containsKey(augmentationFunctionId)) {
            return Optional.empty();
        }

        // Return the augmentation function
        return Optional.ofNullable(augmentationFunctionHashMap.get(augmentationFunctionId));
    }

    /**
     * TODO
     * @return
     */
    public List<AugmentationFunction> getAllAugmentationFunctions() {
        return new ArrayList<>(augmentationFunctionHashMap.values());
    }

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
     * @param augmentationFunctionRequest
     * @throws AugmentationFunctionException
     */
    public void startAugmentationFunction(String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try{
            // Check if the augmentation function is registered
            if (!augmentationFunctionHashMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatefulAugmentationFunction
            if (augmentationFunctionHashMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATEFUL || !(augmentationFunctionHashMap.get(augmentationFunctionId) instanceof StatefulAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateful Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatefulAugmentationFunction
            StatefulAugmentationFunction statefulAugmentationFunction = (StatefulAugmentationFunction) augmentationFunctionHashMap.get(augmentationFunctionId);

            // Call the handler for the start of the Augmentation Function
            handleAugmentationFunctionStart(statefulAugmentationFunction, augmentationFunctionRequest);
        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error starting Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param statefulAugmentationFunction
     * @param augmentationFunctionRequest
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionStart(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    public void stopAugmentationFunction(String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try{

            // Check if the augmentation function is registered
            if (!augmentationFunctionHashMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatefulAugmentationFunction
            if (augmentationFunctionHashMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATEFUL || !(augmentationFunctionHashMap.get(augmentationFunctionId) instanceof StatefulAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateful Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatefulAugmentationFunction
            StatefulAugmentationFunction statefulAugmentationFunction = (StatefulAugmentationFunction) augmentationFunctionHashMap.get(augmentationFunctionId);

            // Call the handler for the stop of the Augmentation Function
            handleAugmentationFunctionStop(statefulAugmentationFunction, augmentationFunctionRequest);
        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error stopping Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }


    /**
     * TODO ...
     * @param statefulAugmentationFunction
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionStop(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @param augmentationFunctionRequest
     * @throws AugmentationFunctionException
     */
    public void executeAugmentationFunction(String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try{

            // Check if the augmentation function is registered
            if (!augmentationFunctionHashMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatelessAugmentationFunction
            if (augmentationFunctionHashMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATELESS || !(augmentationFunctionHashMap.get(augmentationFunctionId) instanceof StatelessAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateless Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatelessAugmentationFunction
            StatelessAugmentationFunction statelessAugmentationFunction = (StatelessAugmentationFunction) augmentationFunctionHashMap.get(augmentationFunctionId);

            // Call the handler for the execution of the Augmentation Function
            List<AugmentationFunctionResult<?>> resultList = handleAugmentationFunctionExecution(statelessAugmentationFunction, augmentationFunctionRequest);

            // Notify through and event the result of the augmentation function execution
            notifyAugmentationFunctionResult(augmentationFunctionId, resultList);

        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * TODO ...
     * @param statelessAugmentationFunction
     * @param augmentationFunctionRequest
     * @throws AugmentationFunctionException
     */
    abstract protected List<AugmentationFunctionResult<?>> handleAugmentationFunctionExecution(StatelessAugmentationFunction statelessAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException;

    /**
     * TODO
     * @param augmentationFunctionId
     * @param queryResult
     */
    public void executeAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, QueryRequest queryRequest, QueryResult<?> queryResult) {
        try {
            StatefulAugmentationFunction statefulAugmentationFunction = null;
            if (augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getType() == AugmentationFunctionType.STATEFUL && augmentationFunctionHashMap.get(augmentationFunctionId) instanceof StatefulAugmentationFunction) {
                statefulAugmentationFunction = (StatefulAugmentationFunction) augmentationFunctionHashMap.get(augmentationFunctionId);
            } else {
                logger.error(String.format("Augmentation Function with id %s is not registered or not a Stateful Augmentation Function.", augmentationFunctionId));
                return;
            }

            handleAugmentationFunctionQueryResultRefresh(statefulAugmentationFunction, queryRequest, queryResult);
        } catch (AugmentationFunctionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * TODO
     * @param statefulAugmentationFunction
     * @param queryResult
     * @throws AugmentationFunctionException
     */
    abstract protected void handleAugmentationFunctionQueryResultRefresh(StatefulAugmentationFunction statefulAugmentationFunction, QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException;

    /**
     * TODO
     * @param statefulAugmentationFunctions
     * @param newDigitalTwinState
     * @param previousDigitalTwinState
     * @param digitalTwinStateChangeList
     */
    abstract protected void onStateUpdate(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList);


    /**
     * TODO
     * @param statefulAugmentationFunctions
     * @param digitalTwinStateEventNotification
     */
    abstract protected void onEventNotificationReceived(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


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

        ///////// NEW DT STATE UPDATE MANAGEMENT ///////////
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

            ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions = this.augmentationFunctionHashMap.values().stream().filter(augmentationFunction -> augmentationFunction.getType() == AugmentationFunctionType.STATEFUL && augmentationFunction instanceof StatefulAugmentationFunction)
                    .map(augmentationFunction -> (StatefulAugmentationFunction) augmentationFunction)
                    .collect(Collectors.toCollection(ArrayList::new));

            onStateUpdate(statefulAugmentationFunctions, newDigitalTwinState, previsousDigitalTwinState, digitalTwinStateChangeList);
        }

        ///////// DT STATE EVENTS NOTIFICATION MANAGEMENT ///////////
        if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEventNotification)) {
            DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();
            logger.debug("Received Event Notification: {}", digitalTwinStateEventNotification);

            ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions = this.augmentationFunctionHashMap.values().stream().filter(augmentationFunction -> augmentationFunction.getType() == AugmentationFunctionType.STATEFUL && augmentationFunction instanceof StatefulAugmentationFunction)
                    .map(augmentationFunction -> (StatefulAugmentationFunction) augmentationFunction)
                    .collect(Collectors.toCollection(ArrayList::new));

            onEventNotificationReceived(statefulAugmentationFunctions, digitalTwinStateEventNotification);
        }

        ////////// STATELESS AUGMENTATION FUNCTION EXECUTION EVENTS MANAGEMENT ///////////
        if(wldtEvent != null
                && wldtEvent.getType() != null
                && (wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE))
                && wldtEvent.getBody() != null
                && (wldtEvent.getBody() instanceof AugmentationFunctionRequest)){

            // Retrieve Augmentation Function Execution Request
            AugmentationFunctionRequest augmentationFunctionRequest = (AugmentationFunctionRequest) wldtEvent.getBody();

            // Extract the Augmentation Function Id from the Event Type after the base type and the handler id
            // Substring after the base type and the handler id considering the '.' as separator
            String augmentationFunctionId = wldtEvent.getType().substring(buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE).length() + 1);

            logger.info("Received Augmentation Function Execution Event for function with id {} and request: {}", augmentationFunctionId, augmentationFunctionRequest);

            if(augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null &&
            augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                try {
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                    QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                    augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                } catch (Exception e) {
                    logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                }
            } else {
                logger.warn(String.format("Error executing query for Augmentation Function with id %s: Augmentation Function not found or Query Request not available in the context !", augmentationFunctionId));
            }

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error executing Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            }
            else {
                try {

                    // Check if the Augmentation Function is Stateless since it is an execution request, if not log the error and skip the execution
                    if(!isAugmentationFunctionExecutionRequestValid(augmentationFunctionOptional.get(), augmentationFunctionRequest)){
                        logger.warn(String.format("Error executing Augmentation Function with id %s is not valid !", augmentationFunctionId));
                    } else{
                        logger.info("Executing Augmentation Function with id {} ...", augmentationFunctionId);
                        executeAugmentationFunction(augmentationFunctionId, augmentationFunctionRequest);
                    }
                } catch (AugmentationFunctionException e) {
                    logger.error(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
                }
            }
        }

        ////////// STATEFUL AUGMENTATION FUNCTION START EVENTS MANAGEMENT ///////////
        if(wldtEvent != null
                && wldtEvent.getType() != null
                && (wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE))
                && wldtEvent.getBody() != null
                && (wldtEvent.getBody() instanceof AugmentationFunctionRequest)){

            // Retrieve Augmentation Function Execution Context
            AugmentationFunctionRequest augmentationFunctionRequest = (AugmentationFunctionRequest) wldtEvent.getBody();

            // Extract the Augmentation Function Id from the Event Type after the base type and the handler id
            // Substring after the base type and the handler id considering the '.' as separator
            String augmentationFunctionId = wldtEvent.getType().substring(buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE).length() + 1);

            logger.info("Received Augmentation Function Execution Event for function with id {} and request: {}", augmentationFunctionId, augmentationFunctionRequest);

            if(augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null &&
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                try {
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                    QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                    augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                } catch (Exception e) {
                    logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                }
            } else {
                logger.warn(String.format("Error executing query for Augmentation Function with id %s: Augmentation Function not found or Query Request not available in the context !", augmentationFunctionId));
            }

            logger.info("Received Augmentation Function Start Event for function with id {} and context: {}", augmentationFunctionId, augmentationFunctionRequest);

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error starting Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            }
            else {
                try {

                    // Check if the Augmentation Function is Stateful since it is an execution request, if not log the error and skip the execution
                    if(!isAugmentationFunctionStartRequestValid(augmentationFunctionOptional.get(), augmentationFunctionRequest)){
                        logger.warn(String.format("Error starting Augmentation Function with id %s is not valid !", augmentationFunctionId));
                    } else{
                        logger.info("Executing Augmentation Function with id {} ...", augmentationFunctionId);
                        startAugmentationFunction(augmentationFunctionId, augmentationFunctionRequest);
                    }
                } catch (AugmentationFunctionException e) {
                    logger.error(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
                }
            }
        }

        ////////// STATEFUL AUGMENTATION FUNCTION STOP EVENTS MANAGEMENT ///////////
        if(wldtEvent != null
                && wldtEvent.getType() != null
                && (wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE))
                && wldtEvent.getBody() != null
                && (wldtEvent.getBody() instanceof AugmentationFunctionRequest)){

            // Retrieve Augmentation Function Execution Context
            AugmentationFunctionRequest augmentationFunctionRequest = (AugmentationFunctionRequest) wldtEvent.getBody();

            // Extract the Augmentation Function Id from the Event Type after the base type and the handler id
            // Substring after the base type and the handler id considering the '.' as separator
            String augmentationFunctionId = wldtEvent.getType().substring(buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE).length() + 1);

            logger.info("Received Augmentation Function Stop Event for function with id {} and request: {}", augmentationFunctionId, augmentationFunctionRequest);

            if(augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null &&
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                try {
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                    QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                    augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                } catch (Exception e) {
                    logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                }
            } else {
                logger.warn(String.format("Error executing query for Augmentation Function with id %s: Augmentation Function not found or Query Request not available in the context !", augmentationFunctionId));
            }

            logger.info("Received Augmentation Function Stop Event for function with id {} and context: {}", augmentationFunctionId, augmentationFunctionRequest);

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error stopping Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            }
            else {
                try {

                    // Check if the Augmentation Function is Stateful since it is an execution request, if not log the error and skip the execution
                    if(!isAugmentationFunctionStartRequestValid(augmentationFunctionOptional.get(), augmentationFunctionRequest)){
                        logger.warn(String.format("Error stopping Augmentation Function with id %s is not valid !", augmentationFunctionId));
                    } else{
                        logger.info("Stopping Augmentation Function with id {} ...", augmentationFunctionId);
                        stopAugmentationFunction(augmentationFunctionId, augmentationFunctionRequest);
                    }
                } catch (AugmentationFunctionException e) {
                    logger.error(String.format("Error stopping Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
                }
            }
        }

        ////////// STATEFUL AUGMENTATION FUNCTION QUERY EXECUTION EVENTS MANAGEMENT ///////////
        if(wldtEvent != null
                && wldtEvent.getType() != null
                && (wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_QUERY_EXECUTION_BASE_TYPE))
                && wldtEvent.getBody() != null
                && wldtEvent.getBody() == null) {


            // Extract the Augmentation Function Id from the Event Type after the base type and the handler id
            // Substring after the base type and the handler id considering the '.' as separator
            String augmentationFunctionId = wldtEvent.getType().substring(buildHandlerEventType(WldtEventTypes.AUGMENTATION_FUNCTION_QUERY_EXECUTION_BASE_TYPE).length() + 1);

            logger.info("Received Augmentation Function Execution of Query Request Event for function with id {}", augmentationFunctionId);

            QueryResult<?> queryResult = null;
            QueryRequest queryRequest = null;

            if(augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null &&
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                try {
                    queryRequest = augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest();
                    queryRequest.setRequestTimestampMs(System.currentTimeMillis());
                    queryRequest.setRequestId(UUID.randomUUID().toString());
                    queryResult = this.queryExecutor.syncQueryExecute(queryRequest);
                } catch (Exception e) {
                    logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                }
            } else {
                logger.warn(String.format("Error executing query for Augmentation Function with id %s: Augmentation Function not found or Query Request not available in the context !", augmentationFunctionId));
            }

            logger.info("Received Augmentation Function Execution of Query Request Event for function with id {} and query result: {}", augmentationFunctionId, queryResult);

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error executing Query Request on Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            }
            else {
                logger.info("Executing Query Result Update on Augmentation Function with id {} ...", augmentationFunctionId);
                executeAugmentationFunctionQueryResultRefresh(augmentationFunctionId, queryRequest, queryResult);
            }
        }
    }

    /**
     * TODO
     */
    private boolean isAugmentationFunctionExecutionRequestValid(AugmentationFunction augmentationFunction,
                                                                 AugmentationFunctionRequest augmentationFunctionRequest) {

        // Validate that the Augmentation Function and the Context are not null
        if(augmentationFunction == null || augmentationFunctionRequest == null || augmentationFunctionRequest.getContext() == null) {
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
     * TODO
     */
    private boolean isAugmentationFunctionStartRequestValid(AugmentationFunction augmentationFunction,
                                                                AugmentationFunctionRequest augmentationFunctionRequest) {

        // Validate that the Augmentation Function and the Context are not null
        if(augmentationFunction == null || augmentationFunctionRequest == null || augmentationFunctionRequest.getContext() == null) {
            logger.error("Invalid Augmentation Function Request ! Augmentation Function and Context cannot be null !");
            return false;
        }

        // Validate that the Augmentation Function is Stateless since it is an execution request
        if(!augmentationFunction.getType().equals(AugmentationFunctionType.STATEFUL)){
            logger.error(String.format("Invalid Augmentation Function Request for function with id %s: Augmentation Function is not Stateful, start request is not allowed !", augmentationFunction.getId()));
            return false;
        }

        // The request is valid
        return true;
    }

    @Override
    public void onStatelessAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) {
        try {
            // Log the error
            logger.error(String.format("Error in Stateless Augmentation Function with id %s: %s", augmentationFunctionId, augmentationFunctionError.getMessage()));

            notifyAugmentationFunctionError(augmentationFunctionId, augmentationFunctionError);
        } catch (Exception e) {
            logger.error(String.format("Error while handling error notification for Stateless Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
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

    /**
     * TODO
     * @param augmentationFunctionId
     * @param augmentationFunctionError
     */
    @Override
    public void onStatefulAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) {
        try {
            // Log the error
            logger.error(String.format("Error in Stateful Augmentation Function with id %s: %s", augmentationFunctionId, augmentationFunctionError.getMessage()));

            notifyAugmentationFunctionError(augmentationFunctionId, augmentationFunctionError);
        } catch (Exception e) {
            logger.error(String.format("Error while handling error notification for Stateful Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onStatefulAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, QueryRequest queryRequest) {
        try{
            logger.info("Received query result refresh request for Stateful Augmentation Function with id {} and query request: {}", augmentationFunctionId, queryRequest);

            if(augmentationFunctionHashMap.containsKey(augmentationFunctionId) && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null &&
                    augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                try {
                    QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(queryRequest);
                    executeAugmentationFunctionQueryResultRefresh(augmentationFunctionId, queryRequest, queryResult);
                } catch (Exception e) {
                    logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                }
            }
             else {
                logger.warn(String.format("Error refreshing query result of Stateful Augmentation Function with id %s: Augmentation Function not found or Query Request not available in the context !", augmentationFunctionId));
            }
        } catch (Exception e){
            logger.error(String.format("Error while notifying query result refresh for Stateful Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
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

            // By default, observer all the State Event Notifications
            observerAllDigitalTwinEventsNotification();

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

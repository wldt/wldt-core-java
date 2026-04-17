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
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
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
 * This class represents the Augmentation Function Handler in the WLDT framework. It is responsible for managing the
 * lifecycle of augmentation functions, including their registration, unregistration, and execution. The handler
 * listens for events related to augmentation functions and digital twin state changes, allowing it to react
 * accordingly and manage the execution of augmentation functions based on the current state of the digital twin.
 * It also provides methods for notifying listeners about the results of augmentation function executions and any
 * errors that may occur during their execution. The Augmentation Function Handler serves as a central component for
 * orchestrating the execution of augmentation functions and ensuring that they are properly managed within the context
 * of the digital twin's state and events.
 */
public abstract class AugmentationFunctionHandler extends DigitalTwinWorker implements StatefulAugmentationListener, WldtEventListener, AugmentationLifeCycleListener {

    /**
     * Logger instance for logging messages related to the Augmentation Function Handler. This logger is used to log
     * important information, errors, and debug messages that can help in monitoring the behavior of the handler and
     * diagnosing issues that may arise during its operation.
     */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationFunctionHandler.class);

    /**
     * The unique identifier of the Augmentation Function Handler.
     */
    private String id = null;

    /**
     * The filter used for subscribing to the events related to the variations of the Digital Twin State.
     */
    private WldtEventFilter stateVariationWldtEventFilter = null;

    /**
     * The filter used for subscribing to the events related to the execution of the Augmentation Functions, including
     * their start, stop, execution and query execution events.
     */
    private WldtEventFilter augmentationFunctionWldtEventFilter = null;

    /**
     * The filter used for subscribing to the events related to the notification of the Digital Twin State Events.
     */
    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    /**
     * The current state of the Digital Twin associated to the Handler. This state is updated based on the events received
     */
    protected DigitalTwinState digitalTwinState = null;

    /**
     * The listener for the events related to the binding and unbinding of the Digital Adapter associated to the Handler.
     * This listener allows the Handler to be notified when the Digital Adapter is correctly bound to the associated
     * external digital services and ready to work, or when there is an issue in the binding with the Digital Asset.
     */
    private DigitalAdapterListener digitalAdapterListener;

    /**
     * The listener for the lifecycle events of the Digital Adapter associated to the Handler. This listener allows the
     * Handler to be notified about the events of the Digital Adapter.
     */
    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    /**
     * The HashMap that contains the registered Augmentation Functions managed by the Handler, where the key is the id
     * of the Augmentation Function and the value is the instance of the Augmentation Function. This HashMap allows for
     * efficient management and retrieval of the registered Augmentation Functions, enabling the Handler to easily
     * access and manage the functions based on their unique identifiers. It serves as a central repository for all
     * the Augmentation Functions that are currently registered and managed by the Handler, facilitating their execution
     * and lifecycle management within the context of the digital twin's state and events.
     */
    public HashMap<String, AugmentationFunction> augmentationFunctionHashMap;

    /**
     * The Query Executor used for executing queries on the storage system. This component allows the Handler to perform
     * queries on the underlying storage system to retrieve data that may be necessary for the execution of augmentation functions.
     */
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
     * @param baseType the base type of the event to build the target event type for the registration
     * @return the target event type for the registration of the event associated to the target Handler
     */
    private String buildHandlerWildCardEventType(String baseType){
        return String.format("%s.%s.%s", baseType, this.id, WldtEventTypes.MULTI_LEVEL_WILDCARD_VALUE);
    }

    /**
     * This method allow the registration for the event associated to the target Handler
     * @param baseType the base type of the event to build the target event type for the registration
     * @return the target event type for the registration of the event associated to the target Handler
     */
    private String buildHandlerEventType(String baseType){
        return String.format("%s.%s", baseType, this.id);
    }

    /**
     * This method allows the Augmentation Function Handler to notify active listeners about the results of the execution
     * of an Augmentation Function. It creates an event associated to the result of the Augmentation Function and publishes
     * it on the EventBus, allowing all the active listeners that are subscribed to the event to receive the notification
     * and react accordingly.
     * @param resultList the list of results produced by the execution of the Augmentation Function to be notified to the listeners
      * @throws EventBusException Thrown if there is an error in the EventBus publishing process, such as issues with
     * the event bus system or problems with the event creation. This exception allows for proper error handling and
     * management of any issues that may arise during the notification process.
     */
    private void notifyAugmentationFunctionResult(String augmentationFunctionId, AugmentationFunctionResultList resultList) throws EventBusException {

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
     * Enable the observation of all the notifications related to any Digital Twin State Event, regardless of the
     * specific event associated to the notification.
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
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
     * Cancel the observation of all the notifications related to any Digital Twin State Event, regardless of the
     * specific event associated to the notification.
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
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
     * This method is responsible for handling the specific logic for the registration of an Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the
     * registerAugmentationFunction method after adding the Augmentation Function to the list of the managed Augmentation
     * Functions by the Handler, allowing to implement any specific logic for the registration of the Augmentation Function,
     * such as the interaction with external services or components, or any other specific operation that needs to be
     * performed during the registration of the Augmentation Function.
     * @param augmentationFunction the Augmentation Function to be registered, encapsulated in an instance of {@link AugmentationFunction}
     * @throws EventBusException Thrown if there is an error in the EventBus publishing process.
     */
    private void notifyAugmentationFunctionRegistered(AugmentationFunction augmentationFunction) throws EventBusException {
        // Create the Event associated to the registration of the Augmentation Function
        AugmentationFunctionRegistrationWldtEvent event = new AugmentationFunctionRegistrationWldtEvent(this.id, augmentationFunction);

        // Notify the registration of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, event);
    }

    /**
     * This method is responsible for handling the specific logic for the unregistration of an Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the unRegisterAugmentationFunction
     * method after removing the Augmentation Function from the list of the managed Augmentation Functions by the Handler,
     * allowing to implement any specific logic for the unregistration of the Augmentation Function, such as the interaction
     * with external services or components, or any other specific operation that needs to be performed during the
     * unregistration of the Augmentation Function.
     * @param augmentationFunction the Augmentation Function to be unregistered, encapsulated in an instance of {@link AugmentationFunction}
     * @throws EventBusException Thrown if there is an error in the EventBus publishing process.
     */
    private void notifyAugmentationFunctionUnRegistered(AugmentationFunction augmentationFunction) throws EventBusException {

        // Create the Event associated to the unregistration of the Augmentation Function
        AugmentationFunctionUnRegistrationWldtEvent event = new AugmentationFunctionUnRegistrationWldtEvent(this.id, augmentationFunction);

        // Notify the unregistration of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, event);
    }

    /**
     * This method is responsible for handling the specific logic for the notification of an error of an Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the execution methods of
     * the Augmentation Functions when there is an error during their execution, allowing to implement any specific
     * logic for the notification of the error of the Augmentation Function, such as the interaction with external
     * services or components, or any other specific operation that needs to be performed during the notification of
     * the error of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function that produced the error during its execution
     * @throws EventBusException Thrown if there is an error in the EventBus publishing process.
     */
    private void notifyAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) throws EventBusException {

        // Create the Event associated to the error of the Augmentation Function
        AugmentationFunctionErrorWldtEvent event = new AugmentationFunctionErrorWldtEvent(this.id, augmentationFunctionId, augmentationFunctionError);

        // Notify the error of the Augmentation Function publishing the associated event on the EventBus
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, event);
    }

    /**
     * This method allows to register an Augmentation Function to the Handler. It adds the Augmentation Function to
     * the list of the managed Augmentation Functions by the Handler and calls the handler for the registration of the
     * Augmentation Function to allow the implementation of any specific logic for the registration of the Augmentation Function.
     * @param augmentationFunction the Augmentation Function to be registered, encapsulated in an instance of {@link AugmentationFunction}
     * @throws AugmentationFunctionException Thrown if there is an error during the registration of the Augmentation Function.
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
     * This method allows to unregister an Augmentation Function from the Handler. It removes the Augmentation Function from
     * the list of the managed Augmentation Functions by the Handler and calls the handler for the unregistration of
     * the Augmentation Function to allow the implementation of any specific logic for the unregistration of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be unregistered
     * @throws AugmentationFunctionException Thrown if there is an error during the unregistration of the Augmentation Function.
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
     * This method allows to retrieve an Augmentation Function based on its id. It checks if the Augmentation Function is present
     * in the list of the managed Augmentation Functions by the Handler and returns it if present, otherwise it returns an empty Optional.
     * @param augmentationFunctionId the id of the Augmentation Function to be retrieved
     * @return an Optional containing the Augmentation Function if it is present in the list of the managed Augmentation Functions by the Handler, otherwise an empty Optional
     */
    public Optional<AugmentationFunction> getAugmentationFunction(String augmentationFunctionId) {

        if (!augmentationFunctionHashMap.containsKey(augmentationFunctionId)) {
            return Optional.empty();
        }

        // Return the augmentation function
        return Optional.ofNullable(augmentationFunctionHashMap.get(augmentationFunctionId));
    }

    /**
     * This method allows to retrieve the list of all the registered Augmentation Functions managed by the Handler.
     * It returns a list containing all the Augmentation Functions that are currently registered and managed by the
     * Handler, allowing to have an overview of all the available functions that can be executed.
     * @return a list containing all the Augmentation Functions that are currently registered and managed by the Handler
     */
    public List<AugmentationFunction> getAllAugmentationFunctions() {
        return new ArrayList<>(augmentationFunctionHashMap.values());
    }

    /**
     * This method is responsible for handling the specific logic for the registration of an Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the registerAugmentationFunction
     * method after adding the Augmentation Function to the list of the managed Augmentation Functions by the Handler,
     * allowing to implement any specific logic for the registration of the Augmentation Function.
     * @param augmentationFunction the Augmentation Function to be registered, encapsulated in an instance of {@link AugmentationFunction}
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the registration of the Augmentation Function.
     */
    abstract protected void handleAugmentationFunctionRegistration(AugmentationFunction augmentationFunction) throws AugmentationFunctionException;

    /**
     * This method is responsible for handling the specific logic for the unregistration of an Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the unRegisterAugmentationFunction
     * method after removing the Augmentation Function from the list of the managed Augmentation Functions by the Handler,
     * allowing to implement any specific logic for the unregistration of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be unregistered
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the unregistration of the Augmentation Function.
     */
    abstract protected void handleAugmentationFunctionUnRegistration(String augmentationFunctionId) throws AugmentationFunctionException;

    /**
     * This method allows to start the execution of a Stateful Augmentation Function based on its id. It checks if the
     * Augmentation Function is registered and if it is of the correct type for the execution, then it calls the handler
     * for the start of the Augmentation Function to allow the implementation of any specific logic for the start of the
     * execution of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be executed
     * @param augmentationFunctionRequest the request associated to the execution of the Augmentation Function,
     *                                    containing all the necessary information for the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the start of the execution of the Augmentation Function.
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
     * This method is responsible for handling the specific logic for the start of the execution of a Stateful Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the startAugmentationFunction
     * method after checking the registration and the type of the Augmentation Function, allowing to implement any specific
     * logic for the start of the execution of the Augmentation Function.
     * @param statefulAugmentationFunction the Stateful Augmentation Function to be executed, encapsulated in an instance of {@link StatefulAugmentationFunction}
     * @param augmentationFunctionRequest the request associated to the execution of the Augmentation Function, containing all the necessary information for the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the start of the execution of the Augmentation Function.
     */
    abstract protected void handleAugmentationFunctionStart(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException;

    /**
     * This method allows to stop the execution of a Stateful Augmentation Function based on its id. It checks if the
     * Augmentation Function is registered and if it is of the correct type for the execution, then it calls the handler
     * for the stop of the Augmentation Function to allow the implementation of any specific logic for the stop of the execution of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be stopped
     * @param augmentationFunctionRequest the request associated to the stop of the execution of the Augmentation Function,
     *                                    containing all the necessary information for the stop of the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the stop of the execution of the Augmentation Function.
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
     * This method is responsible for handling the specific logic for the stop of the execution of a Stateful Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the stopAugmentationFunction method
     * after checking the registration and the type of the Augmentation Function, allowing to implement any specific logic
     * for the stop of the execution of the Augmentation Function.
     * @param statefulAugmentationFunction the Stateful Augmentation Function to be stopped, encapsulated in an instance of {@link StatefulAugmentationFunction}
     * @param request the request associated to the stop of the execution of the Augmentation Function, containing all
     *                the necessary information for the stop of the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the stop of the execution of the Augmentation Function.
     */
    abstract protected void handleAugmentationFunctionStop(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * This method allows to execute a Stateless Augmentation Function based on its id. It checks if the Augmentation
     * Function is registered and if it is of the correct type for the execution, then it calls the handler for the
     * execution of the Augmentation Function to allow the implementation of any specific logic for the execution of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be executed
     * @param augmentationFunctionRequest the request associated to the execution of the Augmentation Function, containing all the necessary information for the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the execution of the Augmentation Function.
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
            AugmentationFunctionResultList resultList = handleAugmentationFunctionExecution(statelessAugmentationFunction, augmentationFunctionRequest);

            if(resultList.hasError()) {
                logger.warn(String.format("Execution of Augmentation Function with id %s produced an error: %s", augmentationFunctionId, resultList.getAugmentationFunctionError().getMessage()));
                notifyAugmentationFunctionError(augmentationFunctionId, resultList.getAugmentationFunctionError());
                return;
            }

            // Notify through and event the result of the augmentation function execution
            notifyAugmentationFunctionResult(augmentationFunctionId, resultList);

        } catch (Exception e){
            throw new AugmentationFunctionException(String.format("Error executing Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * This method is responsible for handling the specific logic for the execution of a Stateless Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the executeAugmentationFunction
     * method after checking the registration and the type of the Augmentation Function, allowing to implement any specific
     * logic for the execution of the Augmentation Function.
     * @param statelessAugmentationFunction the Stateless Augmentation Function to be executed, encapsulated in an instance of {@link StatelessAugmentationFunction}
     * @param augmentationFunctionRequest the request associated to the execution of the Augmentation Function, containing all the necessary information for the execution of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the execution of the Augmentation Function.
     */
    abstract protected AugmentationFunctionResultList handleAugmentationFunctionExecution(StatelessAugmentationFunction statelessAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException;

    /**
     * This method allows to execute a refresh of the result of a Stateful Augmentation Function based on its id. It checks
     * if the Augmentation Function is registered and if it is of the correct type for the execution, then it calls the
     * handler for the refresh of the result of the Augmentation Function to allow the implementation of any specific
     * logic for the refresh of the result of the Augmentation Function.
     * @param augmentationFunctionId the id of the Augmentation Function to be refreshed
     * @param queryResult the result associated to the refresh of the execution of the Augmentation Function, containing all the necessary information for the refresh of the result of the function.
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
     * This method is responsible for handling the specific logic for the refresh of the result of a Stateful Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the executeAugmentationFunctionQueryResultRefresh
     * method after checking the registration and the type of the Augmentation Function, allowing to implement any specific
     * logic for the refresh of the result of the Augmentation Function.
     * @param statefulAugmentationFunction the Stateful Augmentation Function to be refreshed, encapsulated in an instance of {@link StatefulAugmentationFunction}
     * @param queryResult the result associated to the refresh of the execution of the Augmentation Function, containing all the necessary information for the refresh of the result of the function.
     * @throws AugmentationFunctionException Thrown if there is an error during the handling of the refresh of the result of the Augmentation Function.
     */
    abstract protected void handleAugmentationFunctionQueryResultRefresh(StatefulAugmentationFunction statefulAugmentationFunction, QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException;

    /**
     * This method is responsible for handling the specific logic for the update of the state of a Stateful Augmentation Function that
     * can be different for each concrete implementation of the Handler. It is called within the onStateUpdate method after
     * checking the registration and the type of the Augmentation Function, allowing to implement any specific logic for
     * the update of the state of the Augmentation Function.
     * @param statefulAugmentationFunctions the list of the Stateful Augmentation Functions whose state needs to be updated, encapsulated in an instance of {@link StatefulAugmentationFunction}
     * @param newDigitalTwinState the new state of the Digital Twin associated to the update of the state of the Stateful Augmentation Functions, encapsulated in an instance of {@link DigitalTwinState}
     * @param previousDigitalTwinState the previous state of the Digital Twin associated to the update of the state of the Stateful Augmentation Functions, encapsulated in an instance of {@link DigitalTwinState}
     * @param digitalTwinStateChangeList the list of the changes that occurred in the state of the Digital Twin associated to the update of the state of the Stateful Augmentation Functions, encapsulated in a list of {@link DigitalTwinStateChange}
     */
    abstract protected void onStateUpdate(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList);


    /**
     * This method is responsible for handling the specific logic for the notification of an event related to the state
     * of the Digital Twin that can be different for each concrete implementation of the Handler. It is called within
     * the onEvent method when there is an event related to the state of the Digital Twin, allowing to implement any
     * specific logic for the notification of the event related to the state of the Digital Twin.
     * @param statefulAugmentationFunctions the list of the Stateful Augmentation Functions that need to be notified about the event related to the state of the Digital Twin, encapsulated in an instance of {@link StatefulAugmentationFunction}
     * @param digitalTwinStateEventNotification the event related to the state of the Digital Twin that needs to be notified, encapsulated in an instance of {@link DigitalTwinStateEventNotification}
     */
    abstract protected void onEventNotificationReceived(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


    //////////////////////// ADAPTER CALLBACKS /////////////////////////////////////////////////////

    /**
     * Called when the Augmentation Function Handler Manager is started. Implementations can override this method
     * to execute custom initialization logic at the start of the Manager.
     */
    public abstract void onManagerStart();

    /**
     * Called when the Augmentation Function Handler Manager is stopped. Implementations can override this method
     * to execute custom cleanup logic at the stop of the Manager.
     */
    public abstract void onManagerStop();


    //////////////////////// DT CALLBACKS /////////////////////////////////////////////////////

    /**
     * Called when the Digital Twin transitions to a synchronized state. Provides the current Digital Twin State
     * to the implementation so that it can react accordingly.
     * @param digitalTwinState the current state of the Digital Twin at the moment of synchronization
     */
    public abstract void onDigitalTwinLifeCycleSync(DigitalTwinState digitalTwinState);

    /**
     * Called when the Digital Twin transitions to an unsynchronized state. Provides the last known Digital Twin State
     * to the implementation so that it can react accordingly.
     * @param digitalTwinState the last known state of the Digital Twin before losing synchronization
     */
    public abstract void onDigitalTwinLifeCycleUnSync(DigitalTwinState digitalTwinState);

    /**
     * Called when the Digital Twin is created. Implementations can override this method
     * to execute custom logic at the creation of the Digital Twin.
     */
    public abstract void onDigitalTwinLifeCycleCreate();

    /**
     * Called when the Digital Twin is started. Implementations can override this method
     * to execute custom logic at the start of the Digital Twin.
     */
    public abstract void onDigitalTwinLifeCycleStart();

    /**
     * Called when the Digital Twin is stopped. Implementations can override this method
     * to execute custom logic at the stop of the Digital Twin.
     */
    public abstract void onDigitalTwinLifeCycleStop();

    /**
     * Called when the Digital Twin is destroyed. Implementations can override this method
     * to execute custom cleanup logic at the destruction of the Digital Twin.
     */
    public abstract void onDigitalTwinLifeCycleDestroy();

    /**
     * Called when the Digital Twin is bound to its physical asset counterpart. Implementations can override
     * this method to execute custom logic when the binding is established.
     */
    public abstract void onDigitalTwinLifeCycleBound();

    /**
     * Called when the Digital Twin is unbound from its physical asset counterpart. Implementations can override
     * this method to execute custom logic when the binding is removed.
     */
    public abstract void onDigitalTwinLifeCycleUnBound();
    
    /**
     * Called by the WLDT framework when this worker is started. Initializes the query executor, subscribes to
     * augmentation function events, and notifies the handler implementation via {@link #onManagerStart()}.
     * @throws WldtRuntimeException Thrown if any error occurs during the start-up of the worker.
     */
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
    
    /**
     * Called by the WLDT framework when this worker is stopped. Unsubscribes from the Digital Twin state and
     * augmentation function events, notifies the handler implementation via {@link #onManagerStop()}, and
     * notifies the digital adapter listener about the unbound state.
     * @throws WldtRuntimeException Thrown if any error occurs during the shutdown of the worker.
     */
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

    /**
     * This method returns the id of the Handler, which is a unique identifier that distinguishes it from other Handlers.
     * @return the id of the Handler, represented as a String.
     */
    public String getId() {
        return id;
    }

    /**
     * This method sets the id of the Handler, which is a unique identifier that distinguishes it from other Handlers.
     * It takes a String as a parameter, which represents the id to be assigned to the Handler.
     * @param id the id to be assigned to the Handler, represented as a String.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * This method returns the listener associated to the Digital Adapter, which is responsible for handling events and
     * interactions related to the Digital Twin.
     * @return the DigitalAdapterListener associated to the Digital Adapter, which is responsible for handling events and interactions related to the Digital Twin, represented as an instance of {@link DigitalAdapterListener}.
     */
    public DigitalAdapterListener getDigitalAdapterListener() {
        return digitalAdapterListener;
    }

    /**
     * This method sets the listener associated to the Digital Adapter, which is responsible for handling events and interactions related to the Digital Twin.
     * It takes an instance of {@link DigitalAdapterListener} as a parameter, which represents the listener to be assigned to the Digital Adapter for handling events and interactions related to the Digital Twin.
     * @param digitalAdapterListener the DigitalAdapterListener to be assigned to the Digital Adapter for handling events and interactions related to the Digital Twin, represented as an instance of {@link DigitalAdapterListener}.
     */
    public void setDigitalAdapterListener(DigitalAdapterListener digitalAdapterListener) {
        this.digitalAdapterListener = digitalAdapterListener;
    }

    /**
     * Gets the listener associated to the lifecycle of the Digital Adapter, which is responsible for handling events and interactions related to the lifecycle of the Digital Twin.
     * @return the DigitalAdapterLifeCycleListener associated to the lifecycle of the Digital Adapter, which is responsible for handling events and interactions related to the lifecycle of the Digital Twin, represented as an instance of {@link DigitalAdapterLifeCycleListener}.
     */
    public DigitalAdapterLifeCycleListener getDigitalAdapterLifeCycleListener() {
        return digitalAdapterLifeCycleListener;
    }

    /**
     * Sets the listener associated to the lifecycle of the Digital Adapter, which is responsible for handling events and interactions related to the lifecycle of the Digital Twin.
     * @param digitalAdapterLifeCycleListener the DigitalAdapterLifeCycleListener to be assigned to the lifecycle of the Digital Adapter for handling events and interactions related to the lifecycle of the Digital Twin, represented as an instance of {@link DigitalAdapterLifeCycleListener}.
     */
    public void setDigitalAdapterLifeCycleListener(DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener) {
        this.digitalAdapterLifeCycleListener = digitalAdapterLifeCycleListener;
    }

    /**
     * This method removes the listener associated to the lifecycle of the Digital Adapter, which is responsible for handling events and interactions related to the lifecycle of the Digital Twin, by setting it to null.
     */
    public void removeDigitalAdapterLifeCycleListener(){
        this.digitalAdapterLifeCycleListener = null;
    }

    /**
     * Checks equality between this handler and another object based on the handler id.
     * @param o the object to compare with this handler
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AugmentationFunctionHandler that = (AugmentationFunctionHandler) o;
        return id.equals(that.id);
    }

    /**
     * Returns the hash code of this handler based on its id.
     * @return the hash code of the handler
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Called when the handler is successfully subscribed to the given event type on the EventBus.
     * @param eventType the event type to which the handler has subscribed
     */
    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Subscribed to: {}", eventType);
    }

    /**
     * Called when the handler is successfully unsubscribed from the given event type on the EventBus.
     * @param eventType the event type from which the handler has unsubscribed
     */
    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("UnSubscribed from: {}", eventType);
    }

    /**
     * Central entry point for all events received from the EventBus. Dispatches each event to the appropriate
     * internal handler based on its type, including Digital Twin state updates, state event notifications,
     * and augmentation function execution/start/stop/query-refresh events.
     * @param wldtEvent the event received from the EventBus, encapsulated in an instance of {@link WldtEvent}
     */
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

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error executing Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            } else {
                if(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                    try {
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                        QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                        augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                    } catch (Exception e) {
                        logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                    }
                } else {
                    logger.debug(String.format("Not executing query for Augmentation Function with id %s: Augmentation Function Query Request not available in the context !", augmentationFunctionId));
                }
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

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error starting Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            } else {
                if(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                    try {
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                        QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                        augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                    } catch (Exception e) {
                        logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                    }
                } else {
                    logger.debug(String.format("Not executing query for Augmentation Function with id %s: Augmentation Function Query Request not available in the context !", augmentationFunctionId));
                }
                try {
                    // Check if the Augmentation Function is Stateful since it is an execution request, if not log the error and skip the execution
                    if(!isAugmentationFunctionStartRequestValid(augmentationFunctionOptional.get(), augmentationFunctionRequest)){
                        logger.warn(String.format("Error starting Augmentation Function with id %s is not valid !", augmentationFunctionId));
                    } else{
                        logger.info("Starting Augmentation Function with id {} ...", augmentationFunctionId);
                        startAugmentationFunction(augmentationFunctionId, augmentationFunctionRequest);
                    }
                } catch (AugmentationFunctionException e) {
                    logger.error(String.format("Error starting Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
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

            logger.info("Received Augmentation Function Stop Event for function with id {} and context: {}", augmentationFunctionId, augmentationFunctionRequest);

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error stopping Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            } else {
                if(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                    try {
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                        QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                        augmentationFunctionRequest.getContext().setQueryResult(queryResult);
                    } catch (Exception e) {
                        logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                    }
                } else {
                    logger.debug(String.format("Not executing query for Augmentation Function with id %s: Augmentation Function Query Request not available in the context !", augmentationFunctionId));
                }
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

            // Retrieve the Augmentation Function associated to the received id
            Optional<AugmentationFunction> augmentationFunctionOptional = this.getAugmentationFunction(augmentationFunctionId);

            // If the Augmentation Function is not present log the error and skip the execution
            if(!augmentationFunctionOptional.isPresent()){
                logger.warn(String.format("Error executing Query Request on Augmentation Function with id %s: Augmentation Function not found !", augmentationFunctionId));
            } else {
                QueryResult<?> queryResult = null;

                if(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest() != null && augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest() != null) {
                    try {
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
                        augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
                        queryResult = this.queryExecutor.syncQueryExecute(augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest());
                    } catch (Exception e) {
                        logger.error("Error executing query for Augmentation Function with id {}: {}", augmentationFunctionId, e.getLocalizedMessage());
                    }
                } else {
                    logger.warn(String.format("Error executing query for Augmentation Function with id %s: Augmentation Function Query Request not available in the context !", augmentationFunctionId));
                    return;
                }
                logger.info("Executing Query Result Update on Augmentation Function with id {} ...", augmentationFunctionId);
                executeAugmentationFunctionQueryResultRefresh(augmentationFunctionId, augmentationFunctionHashMap.get(augmentationFunctionId).getContextRequest().getQueryRequest(), queryResult);
            }
        }
    }

    /**
     * This method checks the validity of an Augmentation Function Execution Request by validating that the Augmentation 
     * Function is not null, that the Context of the request is not null and that the type of the Augmentation Function 
     * is Stateless since it is an execution request. If any of these conditions is not met, the method returns false 
     * indicating that the request is not valid, otherwise it returns true.
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
     * This method checks the validity of an Augmentation Function Start Request by validating that the Augmentation 
     * Function is not null, that the Context of the request is not null and that the type of the Augmentation Function 
     * is Stateful since it is a start request. If any of these conditions is not met, the method returns false 
     * indicating that the request is not valid, otherwise it returns true.
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

    /**
     * Called when a Stateful Augmentation Function produces a result from its execution. Logs the result and
     * notifies all active listeners by publishing a result event on the EventBus.
     * @param augmentationFunctionId the id of the Stateful Augmentation Function that produced the result
     * @param resultList the list of results produced by the Augmentation Function execution
     */
    @Override
    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, AugmentationFunctionResultList resultList) {
        try{
            logger.info("Received result for Stateful Augmentation Function with id {}: {}", augmentationFunctionId, resultList);

            if(resultList.hasError()) {
                logger.warn(String.format("Stateful Augmentation Function with id %s produced an error result: %s", augmentationFunctionId, resultList.getAugmentationFunctionError().getMessage()));
                notifyAugmentationFunctionError(augmentationFunctionId, resultList.getAugmentationFunctionError());
                return;
            }

            // Notify the result of the execution of the Stateful Augmentation Function through an event on the EventBus
            notifyAugmentationFunctionResult(augmentationFunctionId, resultList);

        } catch (Exception e){
            logger.error(String.format("Error while notifying result for Stateful Augmentation Function with id %s: %s", augmentationFunctionId, e.getLocalizedMessage()));
        }
    }

    /**
     * Called when a Stateful Augmentation Function requests a refresh of its query result context. Executes the
     * provided query request synchronously and delegates the result to
     * {@link #executeAugmentationFunctionQueryResultRefresh(String, QueryRequest, QueryResult)}.
     * @param augmentationFunctionId the id of the Stateful Augmentation Function requesting the query refresh
     * @param queryRequest the query request to be executed for refreshing the context of the Augmentation Function
     */
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

    /**
     * Called by the WLDT framework when the Digital Twin transitions to a synchronized state. Stores the current
     * state, notifies the handler implementation, and subscribes to Digital Twin state and event notifications.
     * @param digitalTwinState the current state of the Digital Twin at the moment of synchronization
     */
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

    /**
     * Called by the WLDT framework when the Digital Twin transitions to an unsynchronized state. Notifies the
     * handler implementation and clears the locally stored Digital Twin state.
     * @param digitalTwinState the last known state of the Digital Twin before losing synchronization
     */
    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
        logger.debug("Augmentation Manager ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinLifeCycleUnSync(digitalTwinState);
        this.digitalTwinState = null;
    }

    /**
     * Called by the WLDT framework when the Digital Twin is created. Delegates to {@link #onDigitalTwinLifeCycleCreate()}.
     */
    @Override
    public void onCreate() {
        onDigitalTwinLifeCycleCreate();
    }

    /**
     * Called by the WLDT framework when the Digital Twin is started. Delegates to {@link #onDigitalTwinLifeCycleStart()}.
     */
    @Override
    public void onStart() {
        onDigitalTwinLifeCycleStart();
    }

    /**
     * Called by the WLDT framework when the Digital Twin is bound to its physical asset. Delegates to {@link #onDigitalTwinLifeCycleBound()}.
     */
    @Override
    public void onDigitalTwinBound() { onDigitalTwinLifeCycleBound(); }

    /**
     * Called by the WLDT framework when the Digital Twin is unbound from its physical asset. Delegates to {@link #onDigitalTwinLifeCycleUnBound()}.
     */
    @Override
    public void onDigitalTwinUnBound() { onDigitalTwinLifeCycleUnBound(); }

    /**
     * Called by the WLDT framework when the Digital Twin is stopped. Delegates to {@link #onDigitalTwinLifeCycleStop()}.
     */
    @Override
    public void onStop() {
        onDigitalTwinLifeCycleStop();
    }

    /**
     * Called by the WLDT framework when the Digital Twin is destroyed. Delegates to {@link #onDigitalTwinLifeCycleDestroy()}.
     */
    @Override
    public void onDestroy() {
        onDigitalTwinLifeCycleDestroy();
    }
}

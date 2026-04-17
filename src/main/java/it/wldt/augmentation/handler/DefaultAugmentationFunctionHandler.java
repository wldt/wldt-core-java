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

import it.wldt.augmentation.function.*;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the {@link AugmentationFunctionHandler} class. This class provides a basic implementation
 * of the methods defined in the abstract class, allowing for the registration, unregistration, execution, and management
 * of augmentation functions. It also handles the notification of state updates and event notifications to active
 * stateful augmentation functions. This default implementation can be extended or customized as needed to provide
 * specific behavior for different types of augmentation functions or use cases.
 */
public class DefaultAugmentationFunctionHandler extends AugmentationFunctionHandler {

    /**
     * Logger instance for logging messages related to the AugmentationFunctionHandler. This logger is used to log
     * important events and errors that occur during the registration, unregistration, execution, and management of
     * augmentation functions, as well as during the notification of state updates and event notifications to active
     * stateful augmentation functions. The logger helps in tracking the behavior of the handler and diagnosing issues
     * that may arise during its operation.
     */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationFunctionHandler.class);

    /**
     * Constructor of the AugmentationFunctionHandler class with the id of the Manager.
     * Receives the id of the Augmentation Function Manager and set it as the id of the Manager.
     *
     * @param id the id of the Augmentation Manager
     */
    public DefaultAugmentationFunctionHandler(String id) {
        super(id);
    }

    /**
     * Logs a debug message confirming the registration of the given augmentation function.
     * @param augmentationFunction the augmentation function that has been registered
     */
    @Override
    protected void handleAugmentationFunctionRegistration(AugmentationFunction augmentationFunction) {
        logger.debug("Registering augmentation function with id {} and name {}", augmentationFunction.getId(), augmentationFunction.getName());
    }

    /**
     * Logs a debug message confirming the unregistration of the augmentation function with the given id.
     * @param augmentationFunctionId the id of the augmentation function that has been unregistered
     */
    @Override
    protected void handleAugmentationFunctionUnRegistration(String augmentationFunctionId) {
        logger.debug("Unregistering augmentation function with id {}", augmentationFunctionId);
    }

    /**
     * Handles the start of a Stateful Augmentation Function by delegating to
     * {@link StatefulAugmentationFunction#handleStart(AugmentationFunctionRequest)}.
     * @param statefulAugmentationFunction the Stateful Augmentation Function to start
     * @param augmentationFunctionRequest the request containing the context and parameters for the start
     * @throws AugmentationFunctionException Thrown if an error occurs while starting the augmentation function
     */
    @Override
    protected void handleAugmentationFunctionStart(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try{
            // Start the augmentation function
            statefulAugmentationFunction.handleStart(augmentationFunctionRequest);

        }catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while starting augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
        }

    }

    /**
     * Handles the stop of a Stateful Augmentation Function by delegating to
     * {@link StatefulAugmentationFunction#handleStop(AugmentationFunctionRequest)}.
     * @param statefulAugmentationFunction the Stateful Augmentation Function to stop
     * @param augmentationFunctionRequest the request containing the context and parameters for the stop
     * @throws AugmentationFunctionException Thrown if an error occurs while stopping the augmentation function
     */
    @Override
    protected void handleAugmentationFunctionStop(StatefulAugmentationFunction statefulAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try {
            // Stop the augmentation function
            statefulAugmentationFunction.handleStop(augmentationFunctionRequest);

        } catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while stopping augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
        }
    }

    /**
     * Handles the execution of a Stateless Augmentation Function by delegating to
     * {@link StatelessAugmentationFunction#handleRun(AugmentationFunctionRequest)}.
     * @param statelessAugmentationFunction the Stateless Augmentation Function to execute
     * @param augmentationFunctionRequest the request containing the context and parameters for the execution
     * @return the list of results produced by the execution of the augmentation function
     * @throws AugmentationFunctionException Thrown if an error occurs while executing the augmentation function
     */
    @Override
    protected AugmentationFunctionResultList handleAugmentationFunctionExecution(StatelessAugmentationFunction statelessAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try {
            // Execute the augmentation function
            return statelessAugmentationFunction.handleRun(augmentationFunctionRequest);

        } catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while executing augmentation function with id %s: %s", statelessAugmentationFunction.getId(), e.getMessage()));
        }
    }

    /**
     * Handles the refresh of the query result context for a Stateful Augmentation Function by delegating to
     * {@link StatefulAugmentationFunction#onQueryResultRefresh(QueryRequest, QueryResult)}.
     * @param statefulAugmentationFunction the Stateful Augmentation Function whose context needs to be refreshed
     * @param queryRequest the query request that was executed to obtain the new result
     * @param queryResult the result of the query execution to pass to the augmentation function
     * @throws AugmentationFunctionException Thrown if an error occurs while refreshing the query result
     */
    @Override
    protected void handleAugmentationFunctionQueryResultRefresh(StatefulAugmentationFunction statefulAugmentationFunction, QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException {
        try {
            statefulAugmentationFunction.onQueryResultRefresh(queryRequest, queryResult);
        } catch (AugmentationFunctionException e) {
            throw new AugmentationFunctionException(String.format("Error while refreshing query result to augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
        }
    }

    /**
     * Notifies all active Stateful Augmentation Functions about a Digital Twin state update, so that they can
     * update their internal state and produce new results if needed.
     * @param statefulAugmentationFunctions the list of active Stateful Augmentation Functions to notify
     * @param newDigitalTwinState the new state of the Digital Twin after the update
     * @param previousDigitalTwinState the previous state of the Digital Twin before the update
     * @param digitalTwinStateChangeList the list of changes that occurred in the Digital Twin state
     */
    @Override
    protected void onStateUpdate(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {
        // The handler should notify active Stateful Augmentation Function about the state update,
        // so that they can update their internal state and generate new results if needed
        for(StatefulAugmentationFunction statefulAugmentationFunction : statefulAugmentationFunctions) {
            try {
                statefulAugmentationFunction.onStateUpdate(newDigitalTwinState);
            } catch (AugmentationFunctionException e) {
                // Log the error and continue with the next augmentation function
               logger.error(String.format("Error while notifying state update to augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
            }
        }
    }

    /**
     * Notifies all active Stateful Augmentation Functions about a Digital Twin state event notification, so that
     * they can update their internal state and produce new results if needed.
     * @param statefulAugmentationFunctions the list of active Stateful Augmentation Functions to notify
     * @param digitalTwinStateEventNotification the event notification received from the Digital Twin state
     */
    @Override
    protected void onEventNotificationReceived(ArrayList<StatefulAugmentationFunction> statefulAugmentationFunctions, DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        // The handler should notify active Stateful Augmentation Function about the State Event Notification update,
        // so that they can update their internal state and generate new results if needed
        for(StatefulAugmentationFunction statefulAugmentationFunction : statefulAugmentationFunctions) {
            try {
                statefulAugmentationFunction.onEventNotificationReceived(digitalTwinStateEventNotification);
            } catch (AugmentationFunctionException e) {
                // Log the error and continue with the next augmentation function
                logger.error(String.format("Error while notifying event notification to augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
            }
        }
    }

    /**
     * Default empty implementation. Override to add custom logic when the Manager is started.
     */
    @Override
    public void onManagerStart() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Manager is stopped.
     */
    @Override
    public void onManagerStop() {

    }

    /**
     * Default empty implementation. Override to react when the Digital Twin becomes synchronized.
     * @param digitalTwinState the current state of the Digital Twin at synchronization
     */
    @Override
    public void onDigitalTwinLifeCycleSync(DigitalTwinState digitalTwinState) {

    }

    /**
     * Default empty implementation. Override to react when the Digital Twin loses synchronization.
     * @param digitalTwinState the last known state of the Digital Twin before losing synchronization
     */
    @Override
    public void onDigitalTwinLifeCycleUnSync(DigitalTwinState digitalTwinState) {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is created.
     */
    @Override
    public void onDigitalTwinLifeCycleCreate() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is started.
     */
    @Override
    public void onDigitalTwinLifeCycleStart() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is stopped.
     */
    @Override
    public void onDigitalTwinLifeCycleStop() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is destroyed.
     */
    @Override
    public void onDigitalTwinLifeCycleDestroy() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is bound to its physical asset.
     */
    @Override
    public void onDigitalTwinLifeCycleBound() {

    }

    /**
     * Default empty implementation. Override to add custom logic when the Digital Twin is unbound from its physical asset.
     */
    @Override
    public void onDigitalTwinLifeCycleUnBound() {

    }
}

package it.wldt.augmentation.handler;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.*;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
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
import java.util.Map;
import java.util.Optional;

public class DefaultAugmentationFunctionHandler extends AugmentationFunctionHandler {

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

    @Override
    protected void handleAugmentationFunctionRegistration(AugmentationFunction augmentationFunction) {
        logger.debug("Registering augmentation function with id {} and name {}", augmentationFunction.getId(), augmentationFunction.getName());
    }

    @Override
    protected void handleAugmentationFunctionUnRegistration(String augmentationFunctionId) {
        logger.debug("Unregistering augmentation function with id {}", augmentationFunctionId);
    }

    /**
     * TODO ...
     * @param statefulAugmentationFunction
     * @throws AugmentationFunctionException
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
     * TODO ...
     * @param statefulAugmentationFunction
     * @throws AugmentationFunctionException
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
     * TODO ...
     *
     * @param statelessAugmentationFunction
     * @param augmentationFunctionRequest
     * @return
     * @throws AugmentationFunctionException
     */
    @Override
    protected List<AugmentationFunctionResult<?>> handleAugmentationFunctionExecution(StatelessAugmentationFunction statelessAugmentationFunction, AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        try {
            // Execute the augmentation function
            return statelessAugmentationFunction.handleRun(augmentationFunctionRequest);

        } catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while executing augmentation function with id %s: %s", statelessAugmentationFunction.getId(), e.getMessage()));
        }
    }

    @Override
    protected void handleAugmentationFunctionQueryResultRefresh(StatefulAugmentationFunction statefulAugmentationFunction, QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException {
        try {
            statefulAugmentationFunction.onQueryResultRefresh(queryRequest, queryResult);
        } catch (AugmentationFunctionException e) {
            throw new AugmentationFunctionException(String.format("Error while refreshing query result to augmentation function with id %s: %s", statefulAugmentationFunction.getId(), e.getMessage()));
        }
    }

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

    @Override
    public void onManagerStart() {

    }

    @Override
    public void onManagerStop() {

    }

    @Override
    public void onDigitalTwinLifeCycleSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinLifeCycleUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinLifeCycleCreate() {

    }

    @Override
    public void onDigitalTwinLifeCycleStart() {

    }

    @Override
    public void onDigitalTwinLifeCycleStop() {

    }

    @Override
    public void onDigitalTwinLifeCycleDestroy() {

    }

    @Override
    public void onDigitalTwinLifeCycleBound() {

    }

    @Override
    public void onDigitalTwinLifeCycleUnBound() {

    }
}

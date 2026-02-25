package it.wldt.augmentation.handler;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.function.*;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultAugmentationFunctionHandler extends AugmentationFunctionHandler {

    private Map<String, AugmentationFunction> augmentationFunctionMap;

    /**
     * Constructor of the AugmentationFunctionHandler class with the id of the Manager.
     * Receives the id of the Augmentation Function Manager and set it as the id of the Manager.
     *
     * @param id the id of the Augmentation Manager
     */
    public DefaultAugmentationFunctionHandler(String id) {
        super(id);

        // Initialize the augmentation function map
        this.augmentationFunctionMap = new java.util.HashMap<>();
    }

    @Override
    protected void handleAugmentationFunctionRegistration(AugmentationFunction augmentationFunction) throws AugmentationFunctionException {

        // Check if the augmentation function is already registered
        if (augmentationFunctionMap.containsKey(augmentationFunction.getId())) {
            throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is already registered.", augmentationFunction.getId()));
        }

        // Register the augmentation function
        augmentationFunctionMap.put(augmentationFunction.getId(), augmentationFunction);
    }

    @Override
    public Optional<AugmentationFunction> getAugmentationFunction(String augmentationFunctionId) {

        if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
            return Optional.empty();
        }

        // Return the augmentation function
        return Optional.ofNullable(augmentationFunctionMap.get(augmentationFunctionId));
    }

    @Override
    protected void handleAugmentationFunctionUnRegistration(String augmentationFunctionId) throws AugmentationFunctionException {
        // Check if the augmentation function is registered
        if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
            throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
        }

        // Unregister the augmentation function
        augmentationFunctionMap.remove(augmentationFunctionId);
    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    @Override
    protected void handleAugmentationFunctionStart(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException {

        try{

            // Check if the augmentation function is registered
            if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatefulAugmentationFunction
            if (augmentationFunctionMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATEFUL || !(augmentationFunctionMap.get(augmentationFunctionId) instanceof StatefulAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateful Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatefulAugmentationFunction
            StatefulAugmentationFunction statefulAugmentationFunction = (StatefulAugmentationFunction) augmentationFunctionMap.get(augmentationFunctionId);

            // Start the augmentation function
            statefulAugmentationFunction.start(augmentationFunctionContext);

        }catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while starting augmentation function with id %s: %s", augmentationFunctionId, e.getMessage()));
        }

    }

    /**
     * TODO ...
     * @param augmentationFunctionId
     * @throws AugmentationFunctionException
     */
    @Override
    protected void handleAugmentationFunctionStop(String augmentationFunctionId) throws AugmentationFunctionException {

        try {

            // Check if the augmentation function is registered
            if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatefulAugmentationFunction
            if (augmentationFunctionMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATEFUL || !(augmentationFunctionMap.get(augmentationFunctionId) instanceof StatefulAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateful Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatefulAugmentationFunction
            StatefulAugmentationFunction statefulAugmentationFunction = (StatefulAugmentationFunction) augmentationFunctionMap.get(augmentationFunctionId);

            // Stop the augmentation function
            statefulAugmentationFunction.stop(new AugmentationFunctionContext());

        } catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while stopping augmentation function with id %s: %s", augmentationFunctionId, e.getMessage()));
        }
    }

    /**
     * TODO ...
     *
     * @param augmentationFunctionId
     * @param augmentationFunctionContext
     * @return
     * @throws AugmentationFunctionException
     */
    @Override
    protected List<AugmentationFunctionResult<?>> handleAugmentationFunctionExecution(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException {

        try {

            // Check if the augmentation function is registered
            if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
            }

            // Check if the Augmentation Function is of the correct type for the execution and the instance of StatelessAugmentationFunction
            if (augmentationFunctionMap.get(augmentationFunctionId).getType() != AugmentationFunctionType.STATELESS || !(augmentationFunctionMap.get(augmentationFunctionId) instanceof StatelessAugmentationFunction)) {
                throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not a Stateless Augmentation Function and cannot be executed with this method.", augmentationFunctionId));
            }

            // Cast the augmentation function to StatelessAugmentationFunction
            StatelessAugmentationFunction statelessAugmentationFunction = (StatelessAugmentationFunction) augmentationFunctionMap.get(augmentationFunctionId);

            // Execute the augmentation function
            return statelessAugmentationFunction.run(augmentationFunctionContext);

        } catch (Exception e) {
            throw new AugmentationFunctionException(String.format("Error while executing augmentation function with id %s: %s", augmentationFunctionId, e.getMessage()));
        }
    }

    @Override
    public List<AugmentationFunction> getAllAugmentationFunctions() {
        return new ArrayList<>(augmentationFunctionMap.values());
    }

    @Override
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

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

package it.wldt.augmentation;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;

import java.util.ArrayList;
import java.util.Map;

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
    }

    @Override
    protected void registerAugmentationFunction(AugmentationFunction augmentationFunction) throws AugmentationFunctionException {

        // Check if the augmentation function is already registered
        if (augmentationFunctionMap.containsKey(augmentationFunction.getId())) {
            throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is already registered.", augmentationFunction.getId()));
        }

        // Register the augmentation function
        augmentationFunctionMap.put(augmentationFunction.getId(), augmentationFunction);
    }

    @Override
    protected void unRegisterAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {
        // Check if the augmentation function is registered
        if (!augmentationFunctionMap.containsKey(augmentationFunctionId)) {
            throw new AugmentationFunctionException(String.format("Augmentation Function with id %s is not registered.", augmentationFunctionId));
        }

        // Unregister the augmentation function
        augmentationFunctionMap.remove(augmentationFunctionId);
    }

    @Override
    protected void startAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {

    }

    @Override
    protected void stopAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {

    }

    @Override
    protected void executeAugmentationFunction(String augmentationFunctionId, AugmentationFunctionContext augmentationFunctionContext) throws AugmentationFunctionException {

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
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }
}

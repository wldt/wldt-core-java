package it.wldt.augmentation;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;

import java.util.ArrayList;
import java.util.Map;

public class DefaultAugmentationManager extends AugmentationManager {

    private Map<String, AugmentationFunction> augmentationFunctionMap;

    /**
     * Constructor of the AugmentationManager class with the id of the Manager.
     * Receives the id of the Augmentation Function Manager and set it as the id of the Manager.
     *
     * @param id the id of the Augmentation Manager
     */
    public DefaultAugmentationManager(String id) {
        super(id);
    }

    @Override
    protected void registerAugmentationFunction(AugmentationFunction augmentationFunction) throws AugmentationFunctionException {

    }

    @Override
    protected void unRegisterAugmentationFunction(String augmentationFunctionId) throws AugmentationFunctionException {

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

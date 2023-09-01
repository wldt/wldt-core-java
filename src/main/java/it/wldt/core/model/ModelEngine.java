package it.wldt.core.model;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.ModelFunctionException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.core.state.IDigitalTwinState;
import it.wldt.core.worker.WldtWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This a fundamental core component responsible to handle the Model associated to the DT instance
 * maintaining its internal state and executing/coordinating its shadowing function
 */
public class ModelEngine extends WldtWorker implements LifeCycleListener {

    private static final Logger logger = LoggerFactory.getLogger(ModelEngine.class);

    private IDigitalTwinState digitalTwinState = null;

    private Map<String, StateModelFunction> modelFunctionMap;

    private ShadowingModelFunction shadowingModelFunction;

    public ModelEngine(IDigitalTwinState digitalTwinState, ShadowingModelFunction shadowingModelFunction) throws ModelException, EventBusException {

        this.digitalTwinState = digitalTwinState;
        this.modelFunctionMap = new HashMap<>();

        if(shadowingModelFunction != null){
            //Init the Shadowing Model Function with the current Digital Twin State and call the associated onCreate method
            this.shadowingModelFunction = shadowingModelFunction;
            this.shadowingModelFunction.init(digitalTwinState);
            this.shadowingModelFunction.onCreate();
        }
        else {
            logger.error("MODEL ENGINE ERROR ! Shadowing Model Function = NULL !");
            throw new ModelException("Error ! Provided ShadowingModelFunction == Null !");
        }
    }

    /**
     *
     * @param stateModelFunction
     * @throws ModelException
     */
    public void addStateModelFunction(StateModelFunction stateModelFunction, boolean observeState, List<String> observePropertyList) throws ModelException, EventBusException, ModelFunctionException {
        if(stateModelFunction == null || stateModelFunction.getId() == null)
            throw new ModelException("Error ! ModelFunction = Null or ModelFunction-Id = Null !");

        stateModelFunction.init(this.digitalTwinState);

        this.modelFunctionMap.put(stateModelFunction.getId(), stateModelFunction);

        this.modelFunctionMap.get(stateModelFunction.getId()).onAdded();

        if(observeState)
            this.modelFunctionMap.get(stateModelFunction.getId()).observeDigitalTwinState();

        if(observePropertyList != null && observePropertyList.size() > 0)
            this.modelFunctionMap.get(stateModelFunction.getId()).observeDigitalTwinProperties(observePropertyList);
    }

    /**
     *
     * @param modelFunctionId
     * @throws ModelException
     */
    public void removeStateModelFunction(String modelFunctionId) throws ModelException {
        if(modelFunctionId == null || !modelFunctionMap.containsKey(modelFunctionId))
            throw new ModelException(String.format("Error ! Provided modelFunctionId(%s) invalid or not found !", modelFunctionId));

        this.modelFunctionMap.get(modelFunctionId).onRemoved();
        this.modelFunctionMap.remove(modelFunctionId);
    }

    @Override
    public void onWorkerStop() {

        logger.info("Stopping Model Engine ....");

        //Stop Shadowing Function
        if(this.shadowingModelFunction != null)
            this.shadowingModelFunction.onStop();

        //Remove all the stored State Model Function
        for (Map.Entry<String, StateModelFunction> entry : this.modelFunctionMap.entrySet())
            try{
                removeStateModelFunction(entry.getKey());
            }catch (Exception e){
                logger.error("Error Removing State Model Function: {}", e.getLocalizedMessage());
            }

        logger.info("Model Engine Correctly Stopped !");
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try {
            this.shadowingModelFunction.onStart();
        } catch (Exception e) {
            String errorMessage = String.format("Shadowing Function Error Observing Physical Event: %s", e.getLocalizedMessage());
            logger.error(errorMessage);
            throw new WldtRuntimeException(errorMessage);
        }
    }

    @Override
    public void onCreate() {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onCreate()");
    }

    @Override
    public void onStart() {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onCreate()");
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterBound({})", adapterId);
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterBindingUpdate()");
        this.shadowingModelFunction.onPhysicalAdapterBidingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterBound({})", adapterId);
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDigitalAdapterBound({})", adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDigitalAdapterUnBound({})", adapterId);
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDigitalTwinBound()");
        this.shadowingModelFunction.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDigitalTwinUnBound()");
        this.shadowingModelFunction.onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    @Override
    public void onSync(IDigitalTwinState digitalTwinState) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onUnSync(IDigitalTwinState digitalTwinState) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onUnSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onStop() {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onStop()");
    }

    @Override
    public void onDestroy() {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDestroy()");
    }
}

/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.core.model;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.WldtWorkerException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.management.ResourceManager;
import it.wldt.storage.StorageManager;
import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This a fundamental core component responsible to handle the Model associated to the DT instance
 * maintaining its internal state and executing/coordinating its shadowing function
 */
public class DigitalTwinModel extends DigitalTwinWorker implements LifeCycleListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinModel.class);

    private static final String MODEL_ENGINE_PUBLISHER_ID = "model_engine";

    private String digitalTwinId = null;

    private final ShadowingFunction shadowingFunction;

    /**
     * Digital Twin Model Constructor
     * @param digitalTwinId Digital Twin ID
     * @param digitalTwinStateManager Digital Twin State Manager
     * @param shadowingFunction Shadowing Function to be executed by the Model
     * @param storageManager Storage Manager to be used by the Model
     * @throws ModelException Model Exception
     * @throws WldtWorkerException Wldt Worker Exception
     */
    public DigitalTwinModel(String digitalTwinId,
                            DigitalTwinStateManager digitalTwinStateManager,
                            ShadowingFunction shadowingFunction,
                            StorageManager storageManager,
                            ResourceManager resourceManager) throws ModelException, WldtWorkerException {

        super();

        if(digitalTwinId == null)
            throw new ModelException("Error ! Digital Twin ID cannot be NULL !");
        else
            this.digitalTwinId = digitalTwinId;

        if(shadowingFunction != null){

            //Init the Shadowing Model Function with the current Digital Twin State and call the associated onCreate method
            this.shadowingFunction = shadowingFunction;
            this.shadowingFunction.init(digitalTwinStateManager, storageManager, resourceManager);
            this.shadowingFunction.onCreate();
        }
        else {
            logger.error("MODEL ENGINE ERROR ! Shadowing Model Function = NULL !");
            throw new ModelException("Error ! Provided ShadowingFunction == Null !");
        }
    }


    @Override
    public void onWorkerStop() {

        logger.info("Stopping Model Engine ....");

        //Stop Shadowing Function
        if(this.shadowingFunction != null)
            this.shadowingFunction.onStop();

        logger.info("Model Engine Correctly Stopped !");
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try {
            this.shadowingFunction.onStart();
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
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onStart()");
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterBound({})", adapterId);
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterBindingUpdate()");
        this.shadowingFunction.onPhysicalAdapterBidingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onPhysicalAdapterUnBound({})", adapterId);
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
        this.shadowingFunction.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onDigitalTwinUnBound()");
        this.shadowingFunction.onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {
        logger.debug("ModelEngine-Listener-DT-LifeCycle: onSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
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

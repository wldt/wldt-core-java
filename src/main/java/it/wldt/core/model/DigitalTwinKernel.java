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
package it.wldt.core.model;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.augmentation.AugmentationFunctionHandler;
import it.wldt.augmentation.AugmentationManager;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.KernelException;
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
public class DigitalTwinKernel extends DigitalTwinWorker implements LifeCycleListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinKernel.class);

    private final DigitalTwinModel digitalTwinModel;

    /**
     * Digital Twin Kernel Constructor
     * @param digitalTwinId Digital Twin ID
     * @param digitalTwinStateManager Digital Twin State Manager
     * @param digitalTwinModel Shadowing Function to be executed by the Kernel
     * @param storageManager Storage Manager to be used by the Kernel
     * @throws KernelException Kernel Exception
     * @throws WldtWorkerException Wldt Worker Exception
     */
    public DigitalTwinKernel(String digitalTwinId,
                             DigitalTwinStateManager digitalTwinStateManager,
                             DigitalTwinModel digitalTwinModel,
                             StorageManager storageManager,
                             ResourceManager resourceManager,
                             AugmentationManager augmentationManager) throws KernelException, WldtWorkerException {

        super();

        if(digitalTwinId == null)
            throw new KernelException("Error ! Digital Twin ID cannot be NULL !");
        else
            this.digitalTwinId = digitalTwinId;

        if(digitalTwinModel != null){

            //Init the Shadowing Function with the current Digital Twin State and call the associated onCreate method
            this.digitalTwinModel = digitalTwinModel;

            // Old init without the Augmentation Manager of the Digital Twin Engine
            //this.digitalTwinModel.init(digitalTwinStateManager, storageManager, resourceManager);
            // Init with the Augmentation Manager of the Digital Twin Engine
            this.digitalTwinModel.init(digitalTwinStateManager,
                 storageManager,
                 resourceManager,
                 augmentationManager);
        }
        else {
            logger.error("KERNEL ERROR ! Shadowing Function = NULL !");
            throw new KernelException("Error ! Provided DigitalTwinModel == Null !");
        }
    }


    @Override
    public void onWorkerStop() {

        logger.info("Stopping Kernel ....");

        //Stop Shadowing Function
        if(this.digitalTwinModel != null)
            // Notify Digital Twin Model the stop of the Kernel and consequently of the operation of the Model
            this.digitalTwinModel.stop();

        logger.info("Kernel Correctly Stopped !");
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try {
            // Notify Digital Twin Model the start of the Kernel and consequently of the operation of the Model
            this.digitalTwinModel.start();
        } catch (Exception e) {
            String errorMessage = String.format("Shadowing Function Error Observing Physical Event: %s", e.getLocalizedMessage());
            logger.error(errorMessage);
            throw new WldtRuntimeException(errorMessage);
        }
    }

    @Override
    public void onCreate() {
        logger.debug("Kernel-Listener-DT-LifeCycle: onCreate()");
    }

    @Override
    public void onStart() {
        logger.debug("Kernel-Listener-DT-LifeCycle: onStart()");
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onPhysicalAdapterBound({})", adapterId);
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onPhysicalAdapterBindingUpdate()");
        this.digitalTwinModel.onPhysicalAdapterBidingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onPhysicalAdapterUnBound({})", adapterId);
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onDigitalAdapterBound({})", adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onDigitalAdapterUnBound({})", adapterId);
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onDigitalTwinBound()");
        this.digitalTwinModel.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onDigitalTwinUnBound()");
        this.digitalTwinModel.onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
        logger.debug("Kernel-Listener-DT-LifeCycle: onUnSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onStop() {
        logger.debug("Kernel-Listener-DT-LifeCycle: onStop()");
    }

    @Override
    public void onDestroy() {
        logger.debug("Kernel-Listener-DT-LifeCycle: onDestroy()");
    }

    @Override
    public String getDigitalTwinId() {
        return digitalTwinId;
    }
}

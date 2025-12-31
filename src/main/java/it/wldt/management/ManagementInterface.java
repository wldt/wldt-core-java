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
package it.wldt.management;

import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.List;

/**
 * ManagementInterface is an abstract class that extends DigitalTwinWorker
 * and implements IResourceManagerObserver to manage resources in a Digital Twin environment.
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public abstract class ManagementInterface extends DigitalTwinWorker implements IResourceManagerObserver {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(ManagementInterface.class);

    protected ResourceManager resourceManager;

    public ManagementInterface() {
        super();
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{

            logger.info("Starting Management Interface...");

            if (this.resourceManager == null) {
                logger.error("ResourceManager is not set. Please set it before starting the Management Interface.");
                throw new WldtRuntimeException("ResourceManager is not set. Please set it before starting the Management Interface.");
            }

            onStart(this.resourceManager.getResourceList());
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            logger.info("Stopping Management Interface...");
            onStop();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * Sets the ResourceManager for this Management Interface.
     * This method is typically called by Digital Twin before starting the Management Interface.
     *
     * @param resourceManager the ResourceManager instance to set
     */
    public void setResourceManager(ResourceManager resourceManager) {
        // Ensure that the ResourceManager is not null
        if (resourceManager == null) {
            throw new IllegalArgumentException("ResourceManager cannot be null");
        }
        else {
            // Set the ResourceManager for this Management Interface
            this.resourceManager = resourceManager;

            // Register this Management Interface as an observer of the ResourceManager
            this.resourceManager.addObserver(this);
        }
    }

    @Override
    public void onManagerResourceAdded(String resourceId) {
        this.onResourceAdded(resourceId);
    }

    @Override
    public void onManagerResourceRemoved(String resourceId) {
        this.onResourceRemoved(resourceId);
    }

    @Override
    public void onManagerResourceUpdated(String resourceId) {
        this.onResourceUpdated(resourceId);
    }

    @Override
    public void onManagerResourceListCleared() {
        this.onResourceListCleared();
    }

    /**
     * Callback when the Management Interface is started.
     * @param resources the list of resources that are being managed and that have been already registered.
     */
    abstract protected void onStart(List<ManagedResource<?, ?, ?>> resources);

    /**
     * Callback when the Management Interface has been stopped.
     */
    abstract protected void onStop();


    /**
     * Callback when a resource is added on the Resource Manager to and the Management Interface should handle it
     * @param resourceId the unique identifier of the added resource
     */
    abstract protected void onResourceAdded(String resourceId);

    /**
     * Callback when a resource has been updated on the Resource Manager and the Management Interface should handle it
     * @param resourceId the unique identifier of the updated resource
     */
    abstract protected void onResourceUpdated(String resourceId);

    /**
     * Callback when a resource has been removed and the Management Interface should handle it
     * @param resourceId the unique identifier of the removed resource
     */
    abstract protected void onResourceRemoved(String resourceId);

    /**
     * Callback when the resource list has been cleared on the Resource Manager
     * and the Management Interface should handle it
     */
    abstract protected void onResourceListCleared();
}

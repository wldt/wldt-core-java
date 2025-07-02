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
package it.wldt.management;

import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.List;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 30/05/2025 - 22:22
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
     */
    abstract protected void onResourceAdded(String resourceId);

    /**
     * Callback when a resource has been updated on the Resource Manager and the Management Interface should handle it
     */
    abstract protected void onResourceUpdated(String resourceId);

    /**
     * Callback when a resource has been removed and the Management Interface should handle it
     */
    abstract protected void onResourceRemoved(String resourceId);

    /**
     * Callback when the resource list has been cleared on the Resource Manager
     * and the Management Interface should handle it
     */
    abstract protected void onResourceListCleared();
}

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
package it.wldt.augmentation;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.listener.AugmentationLifeCycleListener;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtUpDownCounter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The AugmentationManager class is responsible for managing the augmentation functions and their handlers for a specific
 * digital twin. It maintains a map of augmentation function handlers, each identified by a unique ID, and provides methods 
 * to add, remove, and retrieve these handlers. The AugmentationManager also implements the LifeCycleListener interface to 
 * listen to the life cycle events of the digital twin and forward them to the registered augmentation function handlers.
 * Additionally, it manages the execution of the augmentation function handlers using an ExecutorService, allowing for 
 * concurrent execution of multiple handlers while respecting a defined thread pool size limit.
 */
public class AugmentationManager implements LifeCycleListener {

    /**
     * Logger instance for logging information, warnings, and errors related to the AugmentationManager's operations.
     */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationManager.class);

    /**
     * Number of Thread limit for DT Augmentation Managers
     */
    private static final int AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT = 5;

    /**
     * The ID of the digital twin associated with this augmentation manager.
     */
    private final String digitalTwinId;

    /**
     * Map to store the augmentation function handlers, where the key is the handler's unique ID and the value is the handler instance.
     */
    private Map<String, AugmentationFunctionHandler> augmentationFunctionHandlerMap;

    /**
     * Data Structure to keep track of the status of Augmentation Managers
     */
    private Map<String, Boolean> augmentationManagerStatusMap = null;

    /**
     * List of Life Cycle Listener for the current Digital Twin
     */
    private List<AugmentationLifeCycleListener> augmentationFunctionlifeCycleListenerList = null;

    /**
     * Executor Service for Augmentation Function Handlers
     */
    private ExecutorService augmentationFunctionHandlerExecutor = null;

    /**
     * The Monitoring Interface used to record handler-count metrics for the Augmentation Manager.
     */
    private MonitoringInterface monitoringInterface = null;

    /**
     * The namespace used to register and retrieve metrics.
     */
    private String metricsNamespace = null;

    /**
     * Constructor for the AugmentationManager class, initializing the digital twin ID and the necessary data structures 
     * for managing augmentation function handlers and life cycle listeners.
     * @param digitalTwinId The ID of the digital twin associated with this augmentation manager.
     */
    public AugmentationManager(String digitalTwinId) {

        // Set the digital twin id for this augmentation manager
        this.digitalTwinId = digitalTwinId;

        // Initialize the augmentation function handler map
        this.augmentationFunctionHandlerMap = new HashMap<>();

        // Initialize the augmentation manager status map
        this.augmentationManagerStatusMap = new HashMap<>();

        // Initialize the life cycle listener list
        this.augmentationFunctionlifeCycleListenerList = new ArrayList<>();
    }

    /**
     * Sets the Monitoring Interface to be used by this manager for recording handler-count metrics.
     * Follows the same pattern as PhysicalAdapter/DigitalAdapter: registers metrics immediately and propagates
     * to any handlers already added before this call.
     * @param monitoringInterface the MonitoringInterface instance to be used for metrics tracking
     */
    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {
        if (monitoringInterface != null) {
            this.monitoringInterface = monitoringInterface;
            handleMetricsRegistration();
            for (AugmentationFunctionHandler handler : augmentationFunctionHandlerMap.values())
                handler.setMonitoringInterface(monitoringInterface);
        }
    }

    /**
     * Registers the AUGMENTATION_FUNCTION_HANDLER_COUNT metric and sets its initial value based on the handlers
     * already present. Idempotent — does nothing if the namespace is already initialised.
     */
    private void handleMetricsRegistration() {
        if (this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
            return;
        if (this.metricsNamespace != null)
            return;
        this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();
        this.monitoringInterface.registerMetric(new WldtUpDownCounter(this.digitalTwinId, this.metricsNamespace, CoreMonitoringUtils.AUGMENTATION_FUNCTION_HANDLER_COUNT, WldtMetricComponent.AUGMENTATION));
        if (!augmentationFunctionHandlerMap.isEmpty())
            this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.AUGMENTATION_FUNCTION_HANDLER_COUNT, augmentationFunctionHandlerMap.size());
    }

    /**
     * Adds a life cycle listener to the list.
     *
     * @param listener The life cycle listener to be added.
     */
    public void addLifeCycleListener(AugmentationLifeCycleListener listener){
        if(listener != null && this.augmentationFunctionlifeCycleListenerList != null && !this.augmentationFunctionlifeCycleListenerList.contains(listener))
            this.augmentationFunctionlifeCycleListenerList.add(listener);
    }

    /**
     * Removes a life cycle listener from the list.
     *
     * @param listener The life cycle listener to be removed.
     */
    public void removeLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.augmentationFunctionlifeCycleListenerList != null)
            this.augmentationFunctionlifeCycleListenerList.remove(listener);
    }

    /**
     * Adds an augmentation function handler to the augmentation manager, ensuring that the handler is not already
     * registered and that the total number of handlers does not exceed the defined thread pool size limit. The method 
     * also sets the digital twin ID for the handler, initializes its bound status, and registers it as a life cycle listener for the digital twin.
     * @param augmentationFunctionHandler The augmentation function handler to be added to the augmentation manager.
     * @throws AugmentationFunctionException if the augmentation function handler is already registered or if the handler limit has been reached.
     * @throws WldtWorkerException if there is an error while registering the augmentation function handler as a life cycle listener for the digital twin.
     */
    public void addAugmentationFunctionHandler(AugmentationFunctionHandler augmentationFunctionHandler) throws AugmentationFunctionException, WldtWorkerException {

        if(augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandler.getId())){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler with id %s is already registered.", augmentationFunctionHandler.getId()));
        }

        if(augmentationFunctionHandlerMap.size() >= AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler limit of %d reached. Cannot register more handlers.", AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT));
        }

        // Set the Digital Twin ID for the Augmentation Manager
        augmentationFunctionHandler.setDigitalTwinId(this.digitalTwinId);

        // Propagate monitoring interface to the handler if already configured
        if (this.monitoringInterface != null)
            augmentationFunctionHandler.setMonitoringInterface(this.monitoringInterface);

        //Save BoundStatus to False. It will be changed through a call back by the adapter
        this.augmentationManagerStatusMap.put(augmentationFunctionHandler.getId(), false);

        //Save the Model Engine as Digital Twin Life Cycle Listener
        addLifeCycleListener(augmentationFunctionHandler);

        // Register the augmentation function handler
        augmentationFunctionHandlerMap.put(augmentationFunctionHandler.getId(), augmentationFunctionHandler);

        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION) && metricsNamespace != null)
            monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AUGMENTATION_FUNCTION_HANDLER_COUNT);

        logger.debug("New Augmentation Function Handler ({}) Added to the Worker List ! Augmentation Manager - Worker List Size: {}",
                augmentationFunctionHandler.getClass().getName(),
                this.augmentationFunctionHandlerMap.size());
    }

    /**
     * Removes an augmentation function handler from the augmentation manager, ensuring that the handler is currently registered.
     * @param augmentationFunctionHandlerId The unique ID of the augmentation function handler to be removed from the augmentation manager.
     * @throws AugmentationFunctionException if the augmentation function handler with the specified ID is not registered in the augmentation manager.
     */
    public void removeAugmentationFunctionHandler(String augmentationFunctionHandlerId) throws AugmentationFunctionException {
        if(!augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandlerId)){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler with id %s is not registered.", augmentationFunctionHandlerId));
        }

        // Unregister the augmentation function handler
        augmentationFunctionHandlerMap.remove(augmentationFunctionHandlerId);

        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION) && metricsNamespace != null)
            monitoringInterface.decreaseCounter(metricsNamespace, CoreMonitoringUtils.AUGMENTATION_FUNCTION_HANDLER_COUNT);
    }

    /**
     * Retrieves an augmentation function handler from the augmentation manager based on its unique ID, returning an 
     * Optional that may contain the handler if it is registered, or be empty if it is not found.
     * @param augmentationFunctionHandlerId The unique ID of the augmentation function handler to be retrieved from the augmentation manager.
     * @return An Optional containing the augmentation function handler if it is registered in the augmentation manager, or an empty Optional if it is not found.
     */
    public Optional<AugmentationFunctionHandler> getAugmentationFunctionHandler(String augmentationFunctionHandlerId) {

        if(!augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandlerId)){
            return Optional.empty();
        }

        // Return the augmentation function handler
        return Optional.ofNullable(augmentationFunctionHandlerMap.get(augmentationFunctionHandlerId));
    }

    /**
     * Returns the map of all registered augmentation function handlers, where the key is the handler's unique ID
     * and the value is the handler instance.
     * @return the map of augmentation function handlers currently registered in the augmentation manager
     */
    public Map<String, AugmentationFunctionHandler> getAugmentationFunctionHandlerMap() {
        return augmentationFunctionHandlerMap;
    }

    /**
     * Returns a list of all registered augmentation function handlers.
     * @return a list containing all augmentation function handlers currently registered in the augmentation manager
     */
    public List<AugmentationFunctionHandler> getAllAugmentationFunctionHandlers(){
        return new ArrayList<>(augmentationFunctionHandlerMap.values());
    }

    /**
     * This method returns a map of augmentation functions with the specified id from all augmentation function handlers.
     *
     * @param augmentationFunctionId The id of the augmentation function to be retrieved.
     * @return A map of augmentation functions with the specified id from all augmentation function handlers.
     */
    public Map<String, AugmentationFunction> getAugmentationFunctionWithId(String augmentationFunctionId) {

        Map<String, AugmentationFunction> augmentationFunctionMap = new HashMap<>();

        // Search for the augmentation function with the specified id in all augmentation function handlers
        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationFunctionHandlerMap.values()){

            // Get the augmentation function with the specified id from the current augmentation function handler
            Optional<AugmentationFunction> augmentationFunctionOptional = augmentationFunctionHandler.getAugmentationFunction(augmentationFunctionId);

            // If the augmentation function is present, add it to the result map
            augmentationFunctionOptional.ifPresent(augmentationFunction -> augmentationFunctionMap.put(augmentationFunctionId, augmentationFunction));
        }

        return augmentationFunctionMap;

    }

    /**
     * Starts the augmentation manager by executing all registered augmentation function handlers using an ExecutorService. 
     * The method logs the number of handlers being started and their respective classes. If there are no handlers 
     * registered, it logs that the augmentation manager will not be started.
     */
    public void startAugmentationManager(){

        logger.info("Starting {} Augmentation Function Handlers for Digital Twin: {}",
                this.augmentationFunctionHandlerMap.size(),
                this.digitalTwinId);


        if(this.augmentationFunctionHandlerMap.isEmpty()){
            logger.info("No Augmentation Function Handler to start for Digital Twin: {}. Augmentation Manager will not be started.", this.digitalTwinId);
            return;
        }
        //Init PhysicalAdapter Executor
        augmentationFunctionHandlerExecutor = Executors.newFixedThreadPool(this.augmentationFunctionHandlerMap.size());

        this.augmentationFunctionHandlerMap.values().forEach(augmentationFunctionHandler -> {
            logger.info("Executing Augmentation Function Handler: {}", augmentationFunctionHandler.getClass());
            augmentationFunctionHandlerExecutor.execute(augmentationFunctionHandler);
        });
    }

    /**
     * Stops the augmentation manager by shutting down the ExecutorService and invoking the onWorkerStop method on all 
     * registered augmentation function handlers. The method logs the number of handlers being stopped and their respective
     * classes. If there are no handlers registered, it logs that the augmentation manager is not running and will not be stopped.
     * @throws WldtRuntimeException if there is an error while stopping the augmentation function handlers or shutting down the ExecutorService.
     */
    public void stopAugmentationManager() throws WldtRuntimeException {

        if(this.augmentationFunctionHandlerMap.isEmpty()) {
            logger.info("No Augmentation Function Handler to stop for Digital Twin: {}. Augmentation Manager is not running.", this.digitalTwinId);
            return;
        }

        logger.info("Stopping {} Augmentation Function Handlers for Digital Twin: {}",
                this.augmentationFunctionHandlerMap.size(),
                this.digitalTwinId);

        //Stop and Notify Physical Adapters
        this.augmentationFunctionHandlerExecutor.shutdownNow();
        this.augmentationFunctionHandlerExecutor = null;

        for(AugmentationFunctionHandler augmentationFunctionHandler : this.augmentationFunctionHandlerMap.values()) {
            logger.info("Stopping Augmentation Function Handler: {}", augmentationFunctionHandler.getClass());
            augmentationFunctionHandler.onWorkerStop();
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationManager{");
        sb.append("augmentationFunctionHandlerMap=").append(augmentationFunctionHandlerMap);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Forwards the {@code onCreate} life cycle event to all registered augmentation life cycle listeners.
     */
    @Override
    public void onCreate() {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onCreate();
            }
        }
    }

    /**
     * Forwards the {@code onStart} life cycle event to all registered augmentation life cycle listeners.
     */
    @Override
    public void onStart() {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onStart();
            }
        }
    }

    /**
     * Not relevant for augmentation functions. This event is ignored.
     * @param adapterId the ID of the physical adapter that has been bound
     * @param physicalAssetDescription the description of the physical asset associated with the adapter
     */
    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        // Augmentation Function are not interested in this type of event and its granularity
    }

    /**
     * Not relevant for augmentation functions. This event is ignored.
     * @param adapterId the ID of the physical adapter whose binding has been updated
     * @param physicalAssetDescription the updated description of the physical asset associated with the adapter
     */
    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        // Augmentation Function are not interested in this type of event and its granularity
    }

    /**
     * Not relevant for augmentation functions. This event is ignored.
     * @param adapterId the ID of the physical adapter that has been unbound
     * @param physicalAssetDescription the description of the physical asset associated with the adapter
     * @param errorMessage the error message describing the reason for the unbinding, if any
     */
    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        // Augmentation Function are not interested in this type of event and its granularity
    }

    /**
     * Not relevant for augmentation functions. This event is ignored.
     * @param adapterId the ID of the digital adapter that has been bound
     */
    @Override
    public void onDigitalAdapterBound(String adapterId) {
        // Augmentation Function are not interested in this type of event and its granularity
    }

    /**
     * Not relevant for augmentation functions. This event is ignored.
     * @param adapterId the ID of the digital adapter that has been unbound
     * @param errorMessage the error message describing the reason for the unbinding, if any
     */
    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        // Augmentation Function are not interested in this type of event and its granularity
    }

    /**
     * Forwards the {@code onDigitalTwinBound} life cycle event to all registered augmentation life cycle listeners.
     * @param adaptersPhysicalAssetDescriptionMap the map of physical asset descriptions for all bound adapters
     */
    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onDigitalTwinBound();
            }
        }
    }

    /**
     * Forwards the {@code onDigitalTwinUnBound} life cycle event to all registered augmentation life cycle listeners.
     * @param adaptersPhysicalAssetDescriptionMap the map of physical asset descriptions for all unbound adapters
     * @param errorMessage the error message describing the reason for the unbinding, if any
     */
    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onDigitalTwinUnBound();
            }
        }
    }

    /**
     * Forwards the {@code onSync} life cycle event and the current Digital Twin state to all registered
     * augmentation life cycle listeners.
     * @param digitalTwinState the current state of the Digital Twin at the moment of synchronization
     */
    @Override
    public void onSync(DigitalTwinState digitalTwinState) {

        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onSync(digitalTwinState);
            }
        }
    }

    /**
     * Forwards the {@code onUnSync} life cycle event and the last known Digital Twin state to all registered
     * augmentation life cycle listeners.
     * @param digitalTwinState the last known state of the Digital Twin before losing synchronization
     */
    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onUnSync(digitalTwinState);
            }
        }
    }

    /**
     * Forwards the {@code onStop} life cycle event to all registered augmentation life cycle listeners.
     */
    @Override
    public void onStop() {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onStop();
            }
        }
    }

    /**
     * Forwards the {@code onDestroy} life cycle event to all registered augmentation life cycle listeners.
     */
    @Override
    public void onDestroy() {
        // Forward the notification to the life cycle listeners
        if(this.augmentationFunctionlifeCycleListenerList != null) {
            for (AugmentationLifeCycleListener lifeCycleListener : this.augmentationFunctionlifeCycleListenerList) {
                lifeCycleListener.onDestroy();
            }
        }
    }
}

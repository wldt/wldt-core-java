package it.wldt.augmentation;

import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AugmentationManager {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationManager.class);

    /**
     * Number of Thread limit for DT Augmentation Managers
     */
    private static final int AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT = 5;

    private final String digitalTwinId;

    private Map<String, AugmentationFunctionHandler> augmentationFunctionHandlerMap;

    /**
     * Data Structure to keep track of the status of Augmentation Managers
     */
    private Map<String, Boolean> augmentationManagerStatusMap = null;

    /**
     * List of Life Cycle Listener for the current Digital Twin
     */
    private List<LifeCycleListener> lifeCycleListenerList = null;

    /**
     * Executor Service for Augmentation Function Handlers
     */
    private ExecutorService augmentationFunctionHandlerExecutor = null;

    public AugmentationManager(String digitalTwinId) {

        // Set the digital twin id for this augmentation manager
        this.digitalTwinId = digitalTwinId;

        // Initialize the augmentation function handler map
        this.augmentationFunctionHandlerMap = new HashMap<>();

        // Initialize the augmentation manager status map
        this.augmentationManagerStatusMap = new HashMap<>();

        // Initialize the life cycle listener list
        this.lifeCycleListenerList = new ArrayList<>();
    }

    /**
     * Adds a life cycle listener to the list.
     *
     * @param listener The life cycle listener to be added.
     */
    public void addLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null && !this.lifeCycleListenerList.contains(listener))
            this.lifeCycleListenerList.add(listener);
    }

    /**
     * Removes a life cycle listener from the list.
     *
     * @param listener The life cycle listener to be removed.
     */
    public void removeLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null)
            this.lifeCycleListenerList.remove(listener);
    }

    public void addAugmentationFunctionHandler(AugmentationFunctionHandler augmentationFunctionHandler) throws AugmentationFunctionException, WldtWorkerException {

        if(augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandler.getId())){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler with id %s is already registered.", augmentationFunctionHandler.getId()));
        }

        if(augmentationFunctionHandlerMap.size() >= AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler limit of %d reached. Cannot register more handlers.", AUGMENTATION_MANAGER_THREAD_POOL_SIZE_LIMIT));
        }

        // Set the Digital Twin ID for the Augmentation Manager
        augmentationFunctionHandler.setDigitalTwinId(this.digitalTwinId);

        //Save BoundStatus to False. It will be changed through a call back by the adapter
        this.augmentationManagerStatusMap.put(augmentationFunctionHandler.getId(), false);

        //Save the Model Engine as Digital Twin Life Cycle Listener
        addLifeCycleListener(augmentationFunctionHandler);

        // Register the augmentation function handler
        augmentationFunctionHandlerMap.put(augmentationFunctionHandler.getId(), augmentationFunctionHandler);

        logger.debug("New Augmentation Function Handler ({}) Added to the Worker List ! Augmentation Manager - Worker List Size: {}",
                augmentationFunctionHandler.getClass().getName(),
                this.augmentationFunctionHandlerMap.size());
    }

    public void removeAugmentationFunctionHandler(String augmentationFunctionHandlerId) throws AugmentationFunctionException {
        if(!augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandlerId)){
            throw new AugmentationFunctionException(String.format("Augmentation Function Handler with id %s is not registered.", augmentationFunctionHandlerId));
        }

        // Unregister the augmentation function handler
        augmentationFunctionHandlerMap.remove(augmentationFunctionHandlerId);
    }

    public Optional<AugmentationFunctionHandler> getAugmentationFunctionHandler(String augmentationFunctionHandlerId) {

        if(!augmentationFunctionHandlerMap.containsKey(augmentationFunctionHandlerId)){
            return Optional.empty();
        }

        // Return the augmentation function handler
        return Optional.ofNullable(augmentationFunctionHandlerMap.get(augmentationFunctionHandlerId));
    }

    public Map<String, AugmentationFunctionHandler> getAugmentationFunctionHandlerMap() {
        return augmentationFunctionHandlerMap;
    }

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

    public void startAugmentationManager(){

        logger.info("Starting {} Augmentation Function Handlers for Digital Twin: {}",
                this.augmentationFunctionHandlerMap.size(),
                this.digitalTwinId);

        //Init PhysicalAdapter Executor
        augmentationFunctionHandlerExecutor = Executors.newFixedThreadPool(this.augmentationFunctionHandlerMap.size());

        this.augmentationFunctionHandlerMap.values().forEach(augmentationFunctionHandler -> {
            logger.info("Executing Augmentation Function Handler: {}", augmentationFunctionHandler.getClass());
            augmentationFunctionHandlerExecutor.execute(augmentationFunctionHandler);
        });
    }

    public void stopAugmentationManager() throws WldtRuntimeException {

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
}

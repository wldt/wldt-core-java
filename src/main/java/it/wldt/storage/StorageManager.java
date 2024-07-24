package it.wldt.storage;

import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.WldtRuntimeException;

import java.util.HashMap;
import java.util.Map;

public abstract class StorageManager extends DigitalTwinWorker {

    // Map containing the storage types for a DT
    private Map<String, IWldtStorage> storageMap;

    // If true the storage manager will observe state events and receive callback to handle the storage
    private boolean observeStateEvents;

    // If true the storage manager will observe physical asset events and receive callback to handle the storage
    private boolean observerPhysicalAssetEvents;

    // If true the storage manager will observe physical asset action events and receive callback to handle the storage
    private boolean observerPhysicalAssetActionEvents;

    // If true the storage manager will observe physical asset description events and receive callback to handle the storage
    private boolean observePhysicalAssetDescriptionEvents;

    // If true the storage manager will observe digital action events and receive callback to handle the storage
    private boolean observerDigitalActionEvents;

    // If true the storage manager will observe life cycle events and receive callback to handle the storage
    private boolean observeLifeCycleEvents;

    public StorageManager(String storageId, IWldtStorage defaultStorage, boolean observeAllEvents){
        this(new HashMap<String, IWldtStorage>() {{
                 put(storageId, defaultStorage);
             }}, observeAllEvents,
                observeAllEvents,
                observeAllEvents,
                observeAllEvents,
                observeAllEvents,
                observeAllEvents);
    }

    public StorageManager(Map<String, IWldtStorage> storageMap){
        this(storageMap,
                false,
                false,
                false,
                false,
                false,
                false);
    }

    public StorageManager(){
        this(false,
                false,
                false,
                false,
                false,
                false);
    }

    /**
     * Default constructor for the StorageManager class
     * @param observeStateEvents If true the storage manager will observe state events and receive callback to handle the storage
     * @param observerPhysicalAssetEvents If true the storage manager will observe physical asset events and receive callback to handle the storage
     * @param observerPhysicalAssetActionEvents If true the storage manager will observe physical asset action events and receive callback to handle the storage
     * @param observePhysicalAssetDescriptionEvents If true the storage manager will observe physical asset description events and receive callback to handle the storage
     * @param observerDigitalActionEvents If true the storage manager will observe digital action events and receive callback to handle the storage
     * @param observeLifeCycleEvents If true the storage manager will observe life cycle events and receive callback to handle the storage
     */
    public StorageManager(boolean observeStateEvents,
                          boolean observerPhysicalAssetEvents,
                          boolean observerPhysicalAssetActionEvents,
                          boolean observePhysicalAssetDescriptionEvents,
                          boolean observerDigitalActionEvents,
                          boolean observeLifeCycleEvents) {
        this(new HashMap<>(),
                observeStateEvents,
                observerPhysicalAssetEvents,
                observerPhysicalAssetActionEvents,
                observePhysicalAssetDescriptionEvents,
                observerDigitalActionEvents,
                observeLifeCycleEvents);
    }

    /**
     * Constructor for the StorageManager class
     * @param storageMap Map containing the storage types for a DT
     * @param observeStateEvents If true the storage manager will observe state events and receive callback to handle the storage
     * @param observerPhysicalAssetEvents If true the storage manager will observe physical asset events and receive callback to handle the storage
     * @param observerPhysicalAssetActionEvents If true the storage manager will observe physical asset action events and receive callback to handle the storage
     * @param observePhysicalAssetDescriptionEvents If true the storage manager will observe physical asset description events and receive callback to handle the storage
     * @param observerDigitalActionEvents If true the storage manager will observe digital action events and receive callback to handle the storage
     * @param observeLifeCycleEvents If true the storage manager will observe life cycle events and receive callback to handle the storage
     */
    public StorageManager(Map<String, IWldtStorage> storageMap, boolean observeStateEvents, boolean observerPhysicalAssetEvents, boolean observerPhysicalAssetActionEvents, boolean observePhysicalAssetDescriptionEvents, boolean observerDigitalActionEvents, boolean observeLifeCycleEvents) {
        this.storageMap = storageMap;
        this.observeStateEvents = observeStateEvents;
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
        this.observerDigitalActionEvents = observerDigitalActionEvents;
        this.observeLifeCycleEvents = observeLifeCycleEvents;
    }

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker starts.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker start logic.
     */
    @Override
    public void onWorkerStart() throws WldtRuntimeException {

    }

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker stops.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker stop logic.
     */
    @Override
    public void onWorkerStop() throws WldtRuntimeException {

    }
}

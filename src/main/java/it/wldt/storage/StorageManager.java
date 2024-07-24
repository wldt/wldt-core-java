package it.wldt.storage;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetWldtEvent;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.observer.IWldtEventObserverListener;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public abstract class StorageManager extends DigitalTwinWorker implements IWldtEventObserverListener {

    private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);

    private static final String STORAGE_MANAGER_OBSERVER_ID = "storage_manager";

    // Map containing the storage types for a DT
    private Map<String, IWldtStorage> storageMap;

    // If true the storage manager will observe state events and receive callback to handle the storage
    private boolean observeStateEvents = false;

    // If true the storage manager will observe physical asset events and receive callback to handle the storage
    private boolean observerPhysicalAssetEvents = false;

    // If true the storage manager will observe physical asset action events and receive callback to handle the storage
    private boolean observerPhysicalAssetActionEvents = false;

    // If true the storage manager will observe physical asset description events and receive callback to handle the storage
    private boolean observePhysicalAssetDescriptionEvents = false;

    // If true the storage manager will observe digital action events and receive callback to handle the storage
    private boolean observerDigitalActionEvents = false;

    // If true the storage manager will observe life cycle events and receive callback to handle the storage
    private boolean observeLifeCycleEvents = false;

    // The WldtEventObserver instance
    private WldtEventObserver wldtEventObserver;

    // The default storage id
    private String defaultStorageId;

    /**
     * Default constructor for the StorageManager class
     */
    public StorageManager(){
        this.storageMap = new HashMap<>();
    }

    /**
     * Constructor for the StorageManager class
     * @param storageMap Map containing the storage types for a DT
     */
    public StorageManager(Map<String, IWldtStorage> storageMap){
        this(storageMap,
                false,
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
    public StorageManager(Map<String, IWldtStorage> storageMap,
                          boolean observeStateEvents,
                          boolean observerPhysicalAssetEvents,
                          boolean observerPhysicalAssetActionEvents,
                          boolean observePhysicalAssetDescriptionEvents,
                          boolean observerDigitalActionEvents,
                          boolean observeLifeCycleEvents) {

        this.storageMap = storageMap;
        this.observeStateEvents = observeStateEvents;
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
        this.observerDigitalActionEvents = observerDigitalActionEvents;
        this.observeLifeCycleEvents = observeLifeCycleEvents;
    }

    /**
     * Observe all the events types for the Storage Class
     * @param observeAllEvents If true the storage manager will observe all the events and receive callback to handle the storage
     * @return
     */
    public StorageManager observeAllEvents(boolean observeAllEvents) {
        this.observeStateEvents = observeAllEvents;
        this.observerPhysicalAssetEvents = observeAllEvents;
        this.observerPhysicalAssetActionEvents = observeAllEvents;
        this.observePhysicalAssetDescriptionEvents = observeAllEvents;
        this.observerDigitalActionEvents = observeAllEvents;
        this.observeLifeCycleEvents = observeAllEvents;
        return this;
    }

    /**
     * Observe the state events for the Storage Class
     * @param observeStateEvents If true the storage manager will observe state events and receive callback to handle the storage
     * @return
     */
    public StorageManager observeStateEvents(boolean observeStateEvents) {
        this.observeStateEvents = observeStateEvents;
        return this;
    }

    /**
     * Observe the physical asset events for the Storage Class
     * @param observerPhysicalAssetEvents If true the storage manager will observe physical asset events and receive callback to handle the storage
     * @return
     */
    public StorageManager observerPhysicalAssetEvents(boolean observerPhysicalAssetEvents) {
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
        return this;
    }

    /**
     * Observe the physical asset action events for the Storage Class
     * @param observerPhysicalAssetActionEvents If true the storage manager will observe physical asset action events and receive callback to handle the storage
     * @return
     */
    public StorageManager observerPhysicalAssetActionEvents(boolean observerPhysicalAssetActionEvents) {
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
        return this;
    }

    /**
     * Observe the physical asset description events for the Storage Class
     * @param observePhysicalAssetDescriptionEvents If true the storage manager will observe physical asset description events and receive callback to handle the storage
     * @return
     */
    public StorageManager observePhysicalAssetDescriptionEvents(boolean observePhysicalAssetDescriptionEvents) {
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
        return this;
    }

    /**
     * Observe the digital action events for the Storage Class
     * @param observerDigitalActionEvents If true the storage manager will observe digital action events and receive callback to handle the storage
     * @return
     */
    public StorageManager observerDigitalActionEvents(boolean observerDigitalActionEvents) {
        this.observerDigitalActionEvents = observerDigitalActionEvents;
        return this;
    }

    /**
     * Observe the life cycle events for the Storage Class
     * @param observeLifeCycleEvents If true the storage manager will observe life cycle events and receive callback to handle the storage
     * @return
     */
    public StorageManager observeLifeCycleEvents(boolean observeLifeCycleEvents) {
        this.observeLifeCycleEvents = observeLifeCycleEvents;
        return this;
    }

    /**
     * Check if the observation of any type is active
     * @return True if the observation of any type is active
     */
    private boolean isObservationActive(){
        return observeStateEvents || observerPhysicalAssetEvents || observerPhysicalAssetActionEvents || observePhysicalAssetDescriptionEvents || observerDigitalActionEvents || observeLifeCycleEvents;
    }

    /**
     * Save the storage in the storage map
     * @param storageId The storage id
     * @param storage The storage object
     */
    public StorageManager putStorage(String storageId, IWldtStorage storage, boolean isDefault){
        this.storageMap.put(storageId, storage);

        if(isDefault)
            this.defaultStorageId = storageId;

        return this;
    }

    /**
     * Save the storage in the storage map
     * @param storageId The storage id
     * @param storage The storage object
     */
    public StorageManager putStorage(String storageId, IWldtStorage storage){
        return this.putStorage(storageId, storage, false);
    }

    /**
     * Get the storage from the storage map
     * @param storageId The storage id
     * @return The storage object
     */
    public IWldtStorage getStorage(String storageId) {
        return this.storageMap.get(storageId);
    }

    /**
     * Remove the storage from the storage map
     * @param storageId The storage id
     */
    public void removeStorage(String storageId) {
        // Check if is the default storage and avoid to remove it
        if(this.defaultStorageId != null && this.defaultStorageId.equals(storageId))
            logger.error("Cannot remove the default storage !");
        else
            this.storageMap.remove(storageId);
    }

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker starts.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker start logic.
     */
    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try {

            // Check if the default storage is present and if the observation of any type is active
            if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && isObservationActive()){
                logger.info("Initializing the WldtEventObserver for the StorageManager ...");
                this.wldtEventObserver = new WldtEventObserver(this.digitalTwinId, STORAGE_MANAGER_OBSERVER_ID, this);

                if(observeStateEvents)
                    wldtEventObserver.observeStateEvents();

                if(observerPhysicalAssetEvents)
                    wldtEventObserver.observePhysicalAssetEvents();

                if(observerPhysicalAssetActionEvents)
                    wldtEventObserver.observePhysicalAssetActionEvents();

                if(observePhysicalAssetDescriptionEvents)
                    wldtEventObserver.observePhysicalAssetDescriptionEvents();

                if(observerDigitalActionEvents)
                    wldtEventObserver.observeDigitalActionEvents();

                if(observeLifeCycleEvents)
                    wldtEventObserver.observeLifeCycleEvents();

            }
            else
                logger.info("No events to observe for the StorageManager ... Working in Manual mode");
        }catch (Exception e){
            logger.error("Error initializing the StorageManager WldtEventObserver ! Error: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker stops.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker stop logic.
     */
    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try {
            // Check if the default storage is present and if the observation of any type is active
            if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && isObservationActive()){

                logger.info("Stopping the WldtEventObserver for the StorageManager ...");

                this.wldtEventObserver = new WldtEventObserver(this.digitalTwinId, STORAGE_MANAGER_OBSERVER_ID, this);

                if(observeStateEvents)
                    wldtEventObserver.unObserveLifeCycleEvents();

                if(observerPhysicalAssetEvents)
                    wldtEventObserver.unObservePhysicalAssetEvents();

                if(observerPhysicalAssetActionEvents)
                    wldtEventObserver.unObservePhysicalAssetActionEvents();

                if(observePhysicalAssetDescriptionEvents)
                    wldtEventObserver.unObservePhysicalAssetDescriptionEvents();

                if(observerDigitalActionEvents)
                    wldtEventObserver.unObserveDigitalActionEvents();

                if(observeLifeCycleEvents)
                    wldtEventObserver.unObserveLifeCycleEvents();

            }
            else
                logger.info("Nothing to stop for the StorageManager ... Worked in Manual mode");
        }catch (Exception e){
            logger.error("Error stopping the StorageManager WldtEventObserver ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Storage Manager - Subscribed to the event type: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("Storage Manager - Unsubscribed from the event type: {}", eventType);
    }

    @Override
    public void onStateEvent(WldtEvent<?> wldtEvent) {
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId)){
            IWldtStorage storage = this.storageMap.get(defaultStorageId);
            //storage.saveDigitalTwinState(wldtEvent.getDigitalTwinState(), wldtEvent.getDigitalTwinStateChangeList());
        }
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetWldtEvent is received.
     *
     * @param wldtEvent The PhysicalAssetWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent) {
        // Check if the default storage is present and if the event is a PhysicalAssetWldtEvent
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && wldtEvent instanceof PhysicalAssetWldtEvent<?>){
                IWldtStorage storage = this.storageMap.get(defaultStorageId);
                storage.savePhysicalAssetEvent((PhysicalAssetWldtEvent<?>) wldtEvent);
        }
        else
            logger.error("Error saving the PhysicalAssetWldtEvent ! The default storage is not present or the event is not a PhysicalAssetWldtEvent !");
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetActionWldtEvent is received.
     *
     * @param wldtEvent The PhysicalAssetActionWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent) {
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && wldtEvent instanceof PhysicalAssetActionWldtEvent<?>){
            IWldtStorage storage = this.storageMap.get(defaultStorageId);
            storage.savePhysicalAssetActionEvent(wldtEvent);
        }
    }

    @Override
    public void onDigitalActionEvent(WldtEvent<?> wldtEvent) {
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && wldtEvent instanceof DigitalActionWldtEvent<?>){
            IWldtStorage storage = this.storageMap.get(defaultStorageId);
            storage.saveDigitalActionEvent(wldtEvent);
        }
    }

    @Override
    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent) {
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && wldtEvent != null && wldtEvent.getBody() instanceof PhysicalAssetDescription){
            IWldtStorage storage = this.storageMap.get(defaultStorageId);
            storage.savePhysicalAssetDescription(wldtEvent);
        }
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a LifeCycleEvent is received.
     *
     * @param wldtEvent The LifeCycleEvent received by the observer.
     */
    @Override
    public void onLifeCycleEvent(WldtEvent<?> wldtEvent) {
        if(defaultStorageId != null && this.storageMap.containsKey(defaultStorageId) && wldtEvent != null && wldtEvent.getBody() instanceof String){
            IWldtStorage storage = this.storageMap.get(defaultStorageId);
            storage.saveLifeCycleState(wldtEvent.getCreationTimestamp(), LifeCycleState.valueOf((String) wldtEvent.getBody()));
        }
    }
}

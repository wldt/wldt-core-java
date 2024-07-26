package it.wldt.storage;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.*;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.core.event.observer.IWldtEventObserverListener;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.StorageException;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StorageManager extends DigitalTwinWorker implements IWldtEventObserverListener {

    private static final Logger logger = LoggerFactory.getLogger(StorageManager.class);

    private static final String STORAGE_MANAGER_OBSERVER_ID = "storage_manager";

    // Map containing the storage types for a DT
    private Map<String, WldtStorage> storageMap;

    // The WldtEventObserver instance
    private WldtEventObserver wldtEventObserver;

    /**
     * Default constructor for the StorageManager class
     */
    public StorageManager(String digitalTwinId){
        this.digitalTwinId = digitalTwinId;
        this.storageMap = new HashMap<>();
    }

    /**
     * Save the storage in the storage map
     * @param storage The storage object
     */
    public StorageManager putStorage(WldtStorage storage) throws StorageException {

        if(storage == null || storage.getStorageId() == null)
            throw new StorageException("The storage object or the storage id cannot be null !");

        this.storageMap.put(storage.getStorageId(), storage);
        return this;
    }

    /**
     * Get the storage from the storage map
     * @param storageId The storage id
     * @return The storage object
     */
    public WldtStorage getStorage(String storageId) {
        return this.storageMap.get(storageId);
    }

    /**
     * Remove the storage from the storage map
     * @param storageId The storage id
     */
    public void removeStorage(String storageId) throws StorageException {
        // Check if is the default storage and avoid to remove it
        if(this.storageMap.containsKey(storageId))
            this.storageMap.remove(storageId);
        else
            throw new StorageException("The storage id is not present in the storage map !");
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
            logger.info("Initializing the WldtEventObserver for the StorageManager ...");

            this.wldtEventObserver = new WldtEventObserver(this.digitalTwinId, STORAGE_MANAGER_OBSERVER_ID, this);

            wldtEventObserver.observeStateEvents();
            wldtEventObserver.observePhysicalAssetEvents();
            wldtEventObserver.observePhysicalAssetActionEvents();
            wldtEventObserver.observePhysicalAssetDescriptionEvents();
            wldtEventObserver.observeDigitalActionEvents();
            wldtEventObserver.observeLifeCycleEvents();

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
            if(wldtEventObserver != null){

                logger.info("Stopping the WldtEventObserver for the StorageManager ...");
                wldtEventObserver.unObserveStateEvents();
                wldtEventObserver.unObservePhysicalAssetEvents();
                wldtEventObserver.unObservePhysicalAssetActionEvents();
                wldtEventObserver.unObservePhysicalAssetDescriptionEvents();
                wldtEventObserver.unObserveDigitalActionEvents();
                wldtEventObserver.unObserveLifeCycleEvents();

            }
            else
                logger.info("Nothing to stop for the StorageManager ... Observer not initialized !");
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

        // Check if the event is a DigitalTwinState
        if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof DigitalTwinState){

            DigitalTwinState currentState = (DigitalTwinState)wldtEvent.getBody();
            DigitalTwinState previousState = null;
            List<DigitalTwinStateChange> stateChangeList = null;

            // Check if the previous state is present in the event metadata
            if(wldtEvent.getMetadata() != null
                    && wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE).isPresent()
                    && wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE).get() instanceof DigitalTwinState)
                previousState = (DigitalTwinState) wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE).get();

            // Check if the state change list is present in the event metadata
            if(wldtEvent.getMetadata() != null
                    && wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST).isPresent()
                    && wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST).get() instanceof List<?>) {

                List<?> list = (List<?>) wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST).get();

                boolean allElementsAreDigitalTwinStateChange = list.stream().allMatch(element -> element instanceof DigitalTwinStateChange);

                if (allElementsAreDigitalTwinStateChange)
                    stateChangeList = (List<DigitalTwinStateChange>) list;
                else
                    logger.error("Warning saving the DigitalTwinState ! The state change list is not a list of DigitalTwinStateChange !");
            }

            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                WldtStorage storage = entry.getValue();
                if(storage != null && storage.isObserveStateEvents())
                    storage.saveDigitalTwinState(currentState, stateChangeList);
            }

        }
        else
            logger.error("Error saving the DigitalTwinState ! The event is not a DigitalTwinState !");

    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetWldtEvent is received.
     *
     * @param event The PhysicalAssetWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetEvent(WldtEvent<?> event) {
        // Check if the event is a PhysicalAssetWldtEvent
        if(event != null && event.getBody() != null && event.getType() != null && event instanceof PhysicalAssetWldtEvent<?>)
            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                WldtStorage storage = entry.getValue();

                if (storage != null && storage.isObserverPhysicalAssetEvents()){

                    // Save the PhysicalAsset Property Variation
                    if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_PROPERTY_VARIATION_EVENT_TYPE) && event.getBody() instanceof PhysicalAssetPropertyWldtEvent<?>){
                        PhysicalAssetPropertyWldtEvent<?> propertyVariationEvent = (PhysicalAssetPropertyWldtEvent<?>) event;
                        storage.savePhysicalAssetPropertyVariation(propertyVariationEvent.getCreationTimestamp(),
                                propertyVariationEvent.getPhysicalPropertyId(),
                                propertyVariationEvent.getBody(),
                                propertyVariationEvent.getMetadata());
                    }

                    // Save the PhysicalAsset Event
                    if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_EVENT_NOTIFICATION_EVENT_TYPE) && event.getBody() instanceof PhysicalAssetEventWldtEvent<?>){
                        PhysicalAssetEventWldtEvent<?> physicalEvent = (PhysicalAssetEventWldtEvent<?>) event;
                        storage.savePhysicalAssetActionEvent(physicalEvent.getCreationTimestamp(),
                                physicalEvent.getPhysicalEventKey(),
                                physicalEvent.getBody(),
                                physicalEvent.getMetadata());
                    }

                    // Save the PhysicalAsset Relationship Instance Created
                    if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_TYPE) && event.getBody() instanceof PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>){
                        PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalEvent = (PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) event;
                        PhysicalAssetRelationshipInstance<?> relationshipInstance = (PhysicalAssetRelationshipInstance)physicalEvent.getBody();
                        storage.savePhysicalAssetRelationshipInstanceCreatedEvent(physicalEvent.getCreationTimestamp(),
                                relationshipInstance);
                    }

                    // Save the PhysicalAsset Relationship Instance Deleted
                    if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_TYPE) && event.getBody() instanceof PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>){
                        PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalEvent = (PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) event;
                        PhysicalAssetRelationshipInstance<?> relationshipInstance = (PhysicalAssetRelationshipInstance)physicalEvent.getBody();
                        storage.savePhysicalAssetRelationshipInstanceDeletedEvent(physicalEvent.getCreationTimestamp(),
                                relationshipInstance);
                    }


                }
            }
        else
            logger.error("Error saving the PhysicalAssetWldtEvent ! The event is not a PhysicalAssetWldtEvent !");
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetActionWldtEvent is received.
     *
     * @param wldtEvent The PhysicalAssetActionWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent) {

        // Check if the event is a PhysicalAssetActionWldtEvent
        if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent instanceof PhysicalAssetActionWldtEvent<?>)

            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {

                WldtStorage storage = entry.getValue();

                // Save the PhysicalAssetActionWldtEvent
                if (storage != null && storage.isObserverPhysicalAssetActionEvents()) {
                    PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent = (PhysicalAssetActionWldtEvent<?>) wldtEvent;
                    storage.savePhysicalAssetActionEvent(physicalAssetActionWldtEvent.getCreationTimestamp(),
                            physicalAssetActionWldtEvent.getActionKey(),
                            physicalAssetActionWldtEvent.getBody(),
                            physicalAssetActionWldtEvent.getMetadata());
                }
            }
            else
                logger.error("Error saving the PhysicalAssetActionWldtEvent ! The event is not a PhysicalAssetActionWldtEvent !");
    }

    @Override
    public void onDigitalActionEvent(WldtEvent<?> wldtEvent) {

        // Check if the event is a PhysicalAssetActionWldtEvent
        if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent instanceof DigitalActionWldtEvent<?>)
            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {

                WldtStorage storage = entry.getValue();

                // Save the DigitalActionWldtEvent
                if (storage != null && storage.isObserverDigitalActionEvents()) {
                    DigitalActionWldtEvent<?> digitalActionWldtEvent = (DigitalActionWldtEvent<?>) wldtEvent;
                    storage.saveDigitalActionEvent(
                            digitalActionWldtEvent.getCreationTimestamp(),
                            digitalActionWldtEvent.getActionKey(),
                            digitalActionWldtEvent.getBody(),
                            digitalActionWldtEvent.getMetadata());
                }
            }
        else
            logger.error("Error saving the DigitalActionWldtEvent ! The event is not a DigitalActionWldtEvent !");
    }

    @Override
    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent) {

        // Check if the event is a PhysicalAssetActionWldtEvent
        if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof PhysicalAssetDescription)

            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                WldtStorage storage = entry.getValue();
                if (storage != null && storage.isObservePhysicalAssetDescriptionEvents()) {

                    // Get the event timestamp and adapter id
                    long timestamp = wldtEvent.getCreationTimestamp();
                    String adapterId = null;

                    // Check if the adapter id is present in the event metadata
                    if(wldtEvent.getMetadata(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_EVENT_METADATA_ADAPTER_ID).isPresent())
                        adapterId = (String) wldtEvent.getMetadata(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_EVENT_METADATA_ADAPTER_ID).get();

                    // Save the PhysicalAssetDescription Event
                    if (wldtEvent.getType().equals(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_AVAILABLE))
                        storage.savePhysicalAssetDescriptionAvailable(timestamp, adapterId, (PhysicalAssetDescription) wldtEvent.getBody());
                    if (wldtEvent.getType().equals(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_UPDATED))
                        storage.savePhysicalAssetDescriptionUpdated(timestamp, adapterId, (PhysicalAssetDescription) wldtEvent.getBody());
                }
            }
        else
            logger.error("Error saving the PhysicalAssetDescription Event ! The event body is not a PhysicalAssetDescription !");
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a LifeCycleEvent is received.
     *
     * @param wldtEvent The LifeCycleEvent received by the observer.
     */
    @Override
    public void onLifeCycleEvent(WldtEvent<?> wldtEvent) {

        // Check if the event is a PhysicalAssetActionWldtEvent
        if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof String)
            // Find Storage interested to the target event
            for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                WldtStorage storage = entry.getValue();
                if (storage != null && storage.isObserveLifeCycleEvents())
                    storage.saveLifeCycleState(wldtEvent.getCreationTimestamp(), LifeCycleState.valueOf((String) wldtEvent.getBody()));
            }
        else
            logger.error("Error saving the LifeCycleEvent Event ! The event body is not a String !");
    }
}
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
package it.wldt.storage;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.*;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.event.*;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.event.EventManager;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.core.event.observer.IWldtEventObserverListener;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.StorageException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.storage.query.DefaultQueryManager;
import it.wldt.storage.query.QueryManager;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Authors:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * The StorageManager class is a class that represents the storage manager for a DigitalTwin.
 * It is responsible for managing the storage of the data related to the DigitalTwin.
 * The StorageManager class is an observer of the WldtEventBus, and it is able to save the data in the storage.
 * The StorageManager class is also a DigitalTwinWorker, and it is able to start and stop the storage manager.
 */
public class StorageManager extends DigitalTwinWorker implements IWldtEventObserverListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageManager.class);

    private static final String STORAGE_MANAGER_EVENTBUS_CLIENT_ID = "storage_manager";

    private QueryManager queryManager;

    // Map containing the storage types for a DT
    private Map<String, WldtStorage> storageMap;

    // The WldtEventObserver instance
    private WldtEventObserver wldtEventObserver;

    private MonitoringInterface monitoringInterface;

    /**
     * Default constructor for the StorageManager class
     */
    public StorageManager(String digitalTwinId){

        // Digital Twin Id
        this.digitalTwinId = digitalTwinId;

        // New Storage Map to save WldtStorage to be handled by the manager
        this.storageMap = new HashMap<>();

        // Set the Default Query Manager (can be updated through the setQueryManager method)
        this.queryManager = new DefaultQueryManager(this.digitalTwinId);

        logger.info("StorageManager created for the DigitalTwin with id: {}", digitalTwinId);
    }

    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {
        if (monitoringInterface != null) {
            this.monitoringInterface = monitoringInterface;
            if (this.queryManager != null)
                this.queryManager.setMonitoringInterface(monitoringInterface);
            for (WldtStorage storage : storageMap.values())
                storage.setMonitoringInterface(this.digitalTwinId, monitoringInterface);
        }
    }

    /**
     * Save the storage in the storage map
     * @param storage The storage object
     */
    public StorageManager putStorage(WldtStorage storage) throws StorageException {

        if(storage == null || storage.getStorageId() == null)
            throw new StorageException("The storage object or the storage id cannot be null !");

        this.storageMap.put(storage.getStorageId(), storage);
        if (this.monitoringInterface != null)
            storage.setMonitoringInterface(this.digitalTwinId, this.monitoringInterface);
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

            this.wldtEventObserver = new WldtEventObserver(this.digitalTwinId, STORAGE_MANAGER_EVENTBUS_CLIENT_ID, this);

            wldtEventObserver.observeStateEvents();
            wldtEventObserver.observePhysicalAssetEvents();
            wldtEventObserver.observePhysicalAssetActionEvents();
            wldtEventObserver.observePhysicalAssetDescriptionEvents();
            wldtEventObserver.observeAugmentationFunctionEvents();
            wldtEventObserver.observeDigitalActionEvents();
            wldtEventObserver.observeLifeCycleEvents();

            // Observe the Query Request
            wldtEventObserver.observeStorageQueryRequestEvents();

            logger.info("WldtEventObserver for the StorageManager initialized !");

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
                wldtEventObserver.unObserveAugmentationFunctionEvents();
                wldtEventObserver.unObserveLifeCycleEvents();

                // Cancel the observation for query Request
                wldtEventObserver.unObserveStorageQueryRequestEvents();

                // Clear all the references to Storage Instances
                this.storageMap.clear();

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

        try{
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
                    if(storage != null && storage.isObserveStateEvents()) {
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveDigitalTwinState(currentState, stateChangeList);
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_EXEC_TIME, startMs);
                            logger.error("Error saving DigitalTwinState to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }

            }
            else if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof DigitalTwinStateEventNotification){
                DigitalTwinStateEventNotification<?> eventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();

                // Find Storage interested to the target event
                for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                    WldtStorage storage = entry.getValue();
                    if(storage != null && storage.isObserveStateEvents()) {
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveDigitalTwinStateEventNotification(eventNotification);
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME, startMs);
                            logger.error("Error saving DigitalTwinStateEventNotification to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }
            }
            else
                logger.error("Error saving the DigitalTwinState ! The event is not a DigitalTwinState !");
        }catch (Exception e){
            logger.error("Error saving the DigitalTwinState ! Error: {}", e.getLocalizedMessage());
        }

    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetWldtEvent is received.
     *
     * @param event The PhysicalAssetWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetEvent(WldtEvent<?> event) {
        try{
            // Check if the event is a PhysicalAssetWldtEvent
            if(event != null && event.getBody() != null && event.getType() != null && event instanceof PhysicalAssetWldtEvent<?>)
                // Find Storage interested to the target event
                for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                    WldtStorage storage = entry.getValue();

                    if (storage != null && storage.isObserverPhysicalAssetEvents()){

                        // Save the PhysicalAsset Property Variation
                        if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_PROPERTY_VARIATION_EVENT_TYPE) && event instanceof PhysicalAssetPropertyWldtEvent<?>){
                            PhysicalAssetPropertyWldtEvent<?> propertyVariationEvent = (PhysicalAssetPropertyWldtEvent<?>) event;
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.savePhysicalAssetPropertyVariation(new PhysicalAssetPropertyVariation(propertyVariationEvent.getCreationTimestamp(),
                                        propertyVariationEvent.getPhysicalPropertyId(),
                                        propertyVariationEvent.getBody(),
                                        propertyVariationEvent.getMetadata()));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                                logger.error("Error saving PhysicalAssetPropertyVariation to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }

                        // Save the PhysicalAsset Event
                        if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_EVENT_NOTIFICATION_EVENT_TYPE) && event instanceof PhysicalAssetEventWldtEvent<?>){
                            PhysicalAssetEventWldtEvent<?> physicalEvent = (PhysicalAssetEventWldtEvent<?>) event;
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.savePhysicalAssetEventNotification(new PhysicalAssetEventNotification(physicalEvent.getCreationTimestamp(),
                                        physicalEvent.getPhysicalEventKey(),
                                        physicalEvent.getBody(),
                                        physicalEvent.getMetadata()));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                                logger.error("Error saving PhysicalAssetEventNotification to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }

                        // Save the PhysicalAsset Relationship Instance Created
                        if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_TYPE) && event instanceof PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>){
                            PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalEvent = (PhysicalAssetRelationshipInstanceCreatedWldtEvent<?>) event;
                            PhysicalAssetRelationshipInstance<?> relationshipInstance = physicalEvent.getBody();
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.savePhysicalAssetRelationshipInstanceCreatedNotification(new PhysicalRelationshipInstanceVariation(physicalEvent.getCreationTimestamp(),
                                        relationshipInstance));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                                logger.error("Error saving RelationshipInstanceCreated to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }

                        // Save the PhysicalAsset Relationship Instance Deleted
                        if(WldtEventBus.getInstance().matchWildCardType(event.getType(), WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_TYPE) && event instanceof PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>){
                            PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalEvent = (PhysicalAssetRelationshipInstanceDeletedWldtEvent<?>) event;
                            PhysicalAssetRelationshipInstance<?> relationshipInstance = physicalEvent.getBody();
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.savePhysicalAssetRelationshipInstanceDeletedNotification(new PhysicalRelationshipInstanceVariation(physicalEvent.getCreationTimestamp(),
                                        relationshipInstance));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                                logger.error("Error saving RelationshipInstanceDeleted to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }
                    }
                }
            else
                logger.error("Error saving the PhysicalAssetWldtEvent ! The event is not a PhysicalAssetWldtEvent !");
        }catch (Exception e){
            logger.error("Error saving the PhysicalAssetWldtEvent ! Error: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a PhysicalAssetActionWldtEvent is received.
     *
     * @param wldtEvent The PhysicalAssetActionWldtEvent received by the observer.
     */
    @Override
    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent) {
        try {
            // Check if the event is a PhysicalAssetActionWldtEvent
            if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent instanceof PhysicalAssetActionWldtEvent<?>)

                // Find Storage interested to the target event
                for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {

                    WldtStorage storage = entry.getValue();

                    // Save the PhysicalAssetActionWldtEvent
                    if (storage != null && storage.isObserverPhysicalAssetActionEvents()) {
                        PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent = (PhysicalAssetActionWldtEvent<?>) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.savePhysicalAssetActionRequest(new PhysicalAssetActionRequest(physicalAssetActionWldtEvent.getCreationTimestamp(),
                                    physicalAssetActionWldtEvent.getActionKey(),
                                    physicalAssetActionWldtEvent.getBody(),
                                    physicalAssetActionWldtEvent.getMetadata()));
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, startMs);
                            logger.error("Error saving PhysicalAssetActionRequest to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }
            else
                logger.error("Error saving the PhysicalAssetActionWldtEvent ! The event is not a PhysicalAssetActionWldtEvent !");
        }catch (Exception e){
            logger.error("Error saving the PhysicalAssetActionWldtEvent ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void onDigitalActionEvent(WldtEvent<?> wldtEvent) {

        try{
            // Check if the event is a PhysicalAssetActionWldtEvent
            if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent instanceof DigitalActionWldtEvent<?>)
                // Find Storage interested to the target event
                for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {

                    WldtStorage storage = entry.getValue();

                    // Save the DigitalActionWldtEvent
                    if (storage != null && storage.isObserverDigitalActionEvents()) {
                        DigitalActionWldtEvent<?> digitalActionWldtEvent = (DigitalActionWldtEvent<?>) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveDigitalActionRequest(new DigitalActionRequest(
                                    digitalActionWldtEvent.getCreationTimestamp(),
                                    digitalActionWldtEvent.getActionKey(),
                                    digitalActionWldtEvent.getBody(),
                                    digitalActionWldtEvent.getMetadata()));
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME, startMs);
                            logger.error("Error saving DigitalActionRequest to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }
            else
                logger.error("Error saving the DigitalActionWldtEvent ! The event is not a DigitalActionWldtEvent !");
        }catch (Exception e){
            logger.error("Error saving the DigitalActionWldtEvent ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent) {
        try{
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
                        if (wldtEvent.getType().equals(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_AVAILABLE)) {
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.saveNewPhysicalAssetDescriptionNotification(new PhysicalAssetDescriptionNotification(
                                        timestamp, adapterId, (PhysicalAssetDescription) wldtEvent.getBody()));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME, startMs);
                                logger.error("Error saving new PAD to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }
                        if (wldtEvent.getType().equals(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_UPDATED)) {
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.saveUpdatedPhysicalAssetDescriptionNotification(new PhysicalAssetDescriptionNotification(
                                        timestamp, adapterId, (PhysicalAssetDescription) wldtEvent.getBody()));
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME, startMs);
                                logger.error("Error saving updated PAD to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }
                    }
                }
            else
                logger.error("Error saving the PhysicalAssetDescription Event ! The event body is not a PhysicalAssetDescription !");
        }catch (Exception e){
            logger.error("Error saving the PhysicalAssetDescription Event ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void onAugmentationFunctionEvent(WldtEvent<?> wldtEvent) {
        try {
            for(Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                WldtStorage storage = entry.getValue();
                if(storage != null && storage.isObserveAugmentationFunctionEvents()) {

                    // Check if the event is a AugmentationFunctionStartEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE) && wldtEvent.getBody() instanceof AugmentationFunctionRequest) {
                        AugmentationFunctionStartWldtEvent augmentationFunctionStartWldtEvent = (AugmentationFunctionStartWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionRequest(augmentationFunctionStartWldtEvent.getAugmentationFunctionId(),
                                    augmentationFunctionStartWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionStartWldtEvent.getBody());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionRequest (start) to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }

                    // Check if the event is a AugmentationFunctionStopEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE) && wldtEvent.getBody() instanceof AugmentationFunctionRequest) {
                        AugmentationFunctionStopWldtEvent augmentationFunctionStopWldtEvent = (AugmentationFunctionStopWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionRequest(augmentationFunctionStopWldtEvent.getAugmentationFunctionId(),
                                    augmentationFunctionStopWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionStopWldtEvent.getBody());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionRequest (stop) to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }

                    // Check if the event is a AugmentationFunctionExecuteEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE) && wldtEvent.getBody() instanceof AugmentationFunctionRequest) {
                        AugmentationFunctionExecuteWldtEvent augmentationFunctionExecuteWldtEvent = (AugmentationFunctionExecuteWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionRequest(augmentationFunctionExecuteWldtEvent.getAugmentationFunctionId(),
                                    augmentationFunctionExecuteWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionExecuteWldtEvent.getBody());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionRequest (execute) to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }

                    // Check if the event is a AugmentationFunctionResultEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE) && wldtEvent.getBody() instanceof List<?>) {
                        AugmentationFunctionResultWldtEvent augmentationFunctionResultWldtEvent = (AugmentationFunctionResultWldtEvent) wldtEvent;
                        AugmentationFunctionResultList results = augmentationFunctionResultWldtEvent.getBody();
                        for(AugmentationFunctionResult<?> result : results) {
                            long startMs = System.currentTimeMillis();
                            try {
                                storage.saveAugmentationFunctionResult(augmentationFunctionResultWldtEvent.getAugmentationFunctionId(),
                                        augmentationFunctionResultWldtEvent.getAugmentationHandlerId(),
                                        result);
                                storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            } catch (StorageException e) {
                                storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                                logger.error("Error saving AugmentationFunctionResult to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                            }
                        }
                    }

                    // Check if the event is a AugmentationFunctionErrorEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE) && wldtEvent.getBody() instanceof AugmentationFunctionError) {
                        AugmentationFunctionErrorWldtEvent augmentationFunctionErrorWldtEvent = (AugmentationFunctionErrorWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionError(augmentationFunctionErrorWldtEvent.getAugmentationFunctionId(),
                                    augmentationFunctionErrorWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionErrorWldtEvent.getBody());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionError to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }

                    // Check if the event is a AugmentationFunctionRegistrationEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE) && wldtEvent.getBody() instanceof AugmentationFunction) {
                        AugmentationFunctionRegistrationWldtEvent augmentationFunctionRegistrationWldtEvent = (AugmentationFunctionRegistrationWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionRegistration(augmentationFunctionRegistrationWldtEvent.getBody().getId(),
                                    augmentationFunctionRegistrationWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionRegistrationWldtEvent.getBody().getType());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionRegistration to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }

                    // Check if the event is a AugmentationFunctionUnRegistrationEvent
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getType().startsWith(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT_TYPE) && wldtEvent.getBody() instanceof AugmentationFunction) {
                        AugmentationFunctionUnRegistrationWldtEvent augmentationFunctionUnRegistrationWldtEvent = (AugmentationFunctionUnRegistrationWldtEvent) wldtEvent;
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveAugmentationFunctionUnregistration(augmentationFunctionUnRegistrationWldtEvent.getBody().getId(),
                                    augmentationFunctionUnRegistrationWldtEvent.getAugmentationHandlerId(),
                                    augmentationFunctionUnRegistrationWldtEvent.getBody().getType());
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, startMs);
                            logger.error("Error saving AugmentationFunctionUnregistration to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error saving the AugmentationFunction Event ! Error: {}", e.getLocalizedMessage());
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
        try{
            // Check if the event is a PhysicalAssetActionWldtEvent
            if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof String)
                // Find Storage interested to the target event
                for (Map.Entry<String, WldtStorage> entry : storageMap.entrySet()) {
                    WldtStorage storage = entry.getValue();
                    if (storage != null && storage.isObserveLifeCycleEvents()) {
                        long startMs = System.currentTimeMillis();
                        try {
                            storage.saveLifeCycleState(new LifeCycleStateVariation(wldtEvent.getCreationTimestamp(), LifeCycleState.fromValue((String) wldtEvent.getBody())));
                            storage.notifyWriteSuccess(CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_SUCCESS_COUNT, CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_EXEC_TIME, startMs);
                        } catch (StorageException e) {
                            storage.notifyWriteError(CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_ERROR_COUNT, CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_EXEC_TIME, startMs);
                            logger.error("Error saving LifeCycleState to storage {}: {}", entry.getKey(), e.getLocalizedMessage());
                        }
                    }
                }
            else
                logger.error("Error saving the LifeCycleEvent Event ! The event body is not a String !");
        }catch (Exception e){
            logger.error("Error saving the LifeCycleEvent Event ! Error: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Method to be implemented by subclasses.
     * Defines the logic to be executed when a QueryRequest is received.
     *
     * @param wldtEvent The QueryRequest received by the observer.
     */
    @Override
    public void onQueryRequestEvent(WldtEvent<?> wldtEvent) {
        try{
            // Check if the event is a QueryRequest
            if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof QueryRequest) {
                QueryRequest queryRequest = (QueryRequest) wldtEvent.getBody();
                logger.info("Query Request Event Received ! Request: {}", queryRequest);

                if (this.queryManager != null) {
                    QueryResult<?> queryResult = this.queryManager.handleQuery(queryRequest, this.storageMap);
                    EventManager.publishStorageQueryResult(this.digitalTwinId, STORAGE_MANAGER_EVENTBUS_CLIENT_ID, queryResult);
                } else
                    logger.error("Error handling the QueryRequest Event ! The QueryManager is not set !");
            }
            else
                logger.error("Error handling the QueryRequest Event ! The event body is not a QueryRequest !");
        }catch (Exception e){
            logger.error("Error handling the QueryRequest Event ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public void onQueryResultEvent(WldtEvent<?> wldtEvent) {
        // Result of a query request are not used by the StorageManager
    }

    /**
     * The method set the query manager for the StorageManager
     * @param queryManager The query manager to be set
     */
    public void setQueryManager(QueryManager queryManager) throws StorageException {
        if(queryManager != null)
            this.queryManager = queryManager;
        else
            throw new StorageException("The query manager cannot be null !");
    }

    /**
     * Check if a target Storage Id is available in the Storage Manager
     */
    public boolean isStorageAvailable(String storageId){
        return this.storageMap.containsKey(storageId);
    }

    /**
     * Return the list of id of the WldtStorage in the StorageManager
     * @return The list of id of the WldtStorage in the StorageManager
     */
    public List<String> getStorageIdList() {
        return new ArrayList<>(this.storageMap.keySet());
    }
}

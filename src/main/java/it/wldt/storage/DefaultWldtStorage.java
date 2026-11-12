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
import it.wldt.adapter.physical.*;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.StorageException;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.model.StorageRecord;
import it.wldt.storage.model.StorageStats;
import it.wldt.storage.model.StorageStatsRecord;
import it.wldt.storage.model.augmentation.*;
import it.wldt.storage.model.digital.DigitalActionRequestRecord;
import it.wldt.storage.model.lifecycle.LifeCycleVariationRecord;
import it.wldt.storage.model.physical.*;
import it.wldt.storage.model.state.DigitalTwinStateEventNotificationRecord;
import it.wldt.storage.model.state.DigitalTwinStateRecord;
import it.wldt.storage.query.inmemory.InMemoryQueryEvaluator;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResult;
import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 29/03/2024
 * Default implementation of the {@code IWldtStorage} interface. This class provides a simple storage solution for digital twin states,
 * digital twin state changes, physical asset events, and digital twin events. The class provides ONLY a memory based
 * approach for storage using ArrayLists and HashMaps and more advanced solution should be implemented for production
 * oriented Digital Twins for examples using external storage and memorization solutions.
 */
public class DefaultWldtStorage extends WldtStorage {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageManager.class);

    // Instance variables for storing digital twin states, state changes, physical asset events, and digital twin events
    private final List<Map.Entry<Long, DigitalTwinStateRecord>> digitalTwinStateList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, DigitalTwinStateEventNotificationRecord>> digitalStateEventNotificationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalAssetActionRequestRecord>> physicalActionRequestList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalAssetEventNotificationRecord>> physicalEventNotificationsList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, DigitalActionRequestRecord>> digitalActionRequestList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalAssetDescriptionNotificationRecord>> newPhysicalAssetDescriptionNotificationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalAssetDescriptionNotificationRecord>> updatedPhysicalAssetDescriptionNotificationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalAssetPropertyVariationRecord>> physicalAssetPropertyVariationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalRelationshipInstanceVariationRecord>> physicalRelationshipInstanceCreatedList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, PhysicalRelationshipInstanceVariationRecord>> physicalRelationshipInstanceDeletedList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, LifeCycleVariationRecord>> lifeCycleStateList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, AugmentationFunctionErrorRecord>> augmentationFunctionErrorList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, AugmentationFunctionRegistrationRecord>> augmentationFunctionRegistrationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, AugmentationFunctionUnregistrationRecord>> augmentationFunctionUnregistrationList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, AugmentationFunctionRequestRecord>> augmentationFunctionRequestList = Collections.synchronizedList(new ArrayList<>());
    private final List<Map.Entry<Long, AugmentationFunctionResultRecord>> augmentationFunctionResultList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Default Constructor
     */
    public DefaultWldtStorage(String storageId) {
        super(storageId);

        try{
            init();
        }catch (Exception e){
            logger.error("Error during storage initialization: {}", e.getMessage());
        }
    }

    /**
     * Constructor with the possibility to enable/disable the observation of all events
     * @param storageId the id of the storage instance
     * @param observeAll if true the storage manager will observe all events and receive callback to handle the storage
     */
    public DefaultWldtStorage(String storageId, boolean observeAll){
        super(storageId, observeAll);

        try{
            init();
        }catch (Exception e){
            logger.error("Error during storage initialization: {}", e.getMessage());
        }
    }

    /**
     * Constructor with the possibility to enable/disable the observation of specific events
     * @param storageId the id of the storage instance
     * @param observeStateEvents if true the storage manager will observe state events and receive callback to handle the storage
     * @param observerPhysicalAssetEvents if true the storage manager will observe physical asset events and receive callback to handle the storage
     * @param observerPhysicalAssetActionEvents if true the storage manager will observe physical asset action events and receive callback to handle the storage
     * @param observePhysicalAssetDescriptionEvents if true the storage manager will observe physical asset description events and receive callback to handle the storage
     * @param observerDigitalActionEvents if true the storage manager will observe digital action events and receive callback to handle the storage
     * @param observeLifeCycleEvents if true the storage manager will observe life cycle events and receive callback to handle the storage
     */
    public DefaultWldtStorage(String storageId,
                          boolean observeStateEvents,
                          boolean observerPhysicalAssetEvents,
                          boolean observerPhysicalAssetActionEvents,
                          boolean observePhysicalAssetDescriptionEvents,
                          boolean observerDigitalActionEvents,
                          boolean observeLifeCycleEvents,
                          boolean observeAugmentationFunctionEvents){
        super(storageId,
                observeStateEvents,
                observerPhysicalAssetEvents,
                observerPhysicalAssetActionEvents,
                observePhysicalAssetDescriptionEvents,
                observerDigitalActionEvents,
                observeLifeCycleEvents,
                observeAugmentationFunctionEvents);

        try{
            init();
        }catch (Exception e){
            logger.error("Error during storage initialization: {}", e.getMessage());
        }
    }

    /**
     * Initialize the WLDT Storage
     */
    @Override
    protected void init() throws StorageException{

        logger.info("Initializing Default WLDT Storage...");

        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
    }

    /**
     * Clear the WLDT Storage canceling all the stored data and information
     */
    @Override
    public void clear(){

        logger.info("Clearing Default WLDT Storage...");

        this.digitalTwinStateList.clear();
        this.digitalStateEventNotificationList.clear();
        this.physicalActionRequestList.clear();
        this.physicalEventNotificationsList.clear();
        this.digitalActionRequestList.clear();
        this.newPhysicalAssetDescriptionNotificationList.clear();
        this.updatedPhysicalAssetDescriptionNotificationList.clear();
        this.physicalAssetPropertyVariationList.clear();
        this.physicalRelationshipInstanceCreatedList.clear();
        this.physicalRelationshipInstanceDeletedList.clear();
        this.lifeCycleStateList.clear();
        this.augmentationFunctionErrorList.clear();
        this.augmentationFunctionRequestList.clear();
        this.augmentationFunctionResultList.clear();
        this.augmentationFunctionRegistrationList.clear();
        this.augmentationFunctionUnregistrationList.clear();
    }

    /**
     * Save a new computed instance of the DT State in the Storage together with the list of the changes with respect
     * to the previous state
     * @param digitalTwinState associated to the new computed Digital State to be stored
     * @param digitalTwinStateChangeList the list of state changes associated to the new Digital State that has to be stored
     * @throws IllegalArgumentException  if digitalTwinState is null
     */
    @Override
    public void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws StorageException, IllegalArgumentException {
        if (digitalTwinState == null) {
            throw new IllegalArgumentException("Digital twin state cannot be null.");
        }
        add(digitalTwinStateList, digitalTwinState.getEvaluationInstant().toEpochMilli(), new DigitalTwinStateRecord(digitalTwinState, digitalTwinStateChangeList));
    }

    /**
     * Save the Digital Twin State Event Notification
     *
     * @param digitalTwinStateEventNotification the Digital Twin State Event Notification to be saved
     */
    @Override
    public void saveDigitalTwinStateEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws StorageException {
        if(digitalTwinStateEventNotification == null)
            throw new StorageException("Digital Twin State Event Notification cannot be null.");

        // Save the Digital Twin State Event Notification
        add(digitalStateEventNotificationList, digitalTwinStateEventNotification.getTimestamp(),
                new DigitalTwinStateEventNotificationRecord(
                        digitalTwinStateEventNotification.getDigitalEventKey(),
                        digitalTwinStateEventNotification.getBody(),
                        digitalTwinStateEventNotification.getTimestamp()));
    }

    /**
     * Save the LifeCycleState of the Digital Twin
     * @param lifeCycleStateVariation the LifeCycleState of the Digital Twin to be saved
     */
    @Override
    public void saveLifeCycleState(LifeCycleStateVariation lifeCycleStateVariation) {
        // Implement this method using the variable lifeCycleStateList
        add(lifeCycleStateList, lifeCycleStateVariation.getTimestamp(),
                new LifeCycleVariationRecord(lifeCycleStateVariation.getLifeCycleState(),
                        lifeCycleStateVariation.getTimestamp()));

    }

    /**
     * Save the Physical Asset Event Notification
     *
     * @param physicalAssetEventNotification the Physical Asset Event Notification to be saved
     */
    @Override
    public void savePhysicalAssetEventNotification(PhysicalAssetEventNotification physicalAssetEventNotification) throws StorageException {
        if(physicalAssetEventNotification == null)
            throw new StorageException("Physical Asset Event Notification cannot be null.");

        add(physicalEventNotificationsList, physicalAssetEventNotification.getTimestamp(),
                new PhysicalAssetEventNotificationRecord(
                        physicalAssetEventNotification.getTimestamp(),
                        physicalAssetEventNotification.getEventkey(),
                        physicalAssetEventNotification.getBody(),
                        physicalAssetEventNotification.getMetadata()));
    }

    /**
     * Save Physical Asset Action Request
     *
     * @param physicalAssetActionRequest the Physical Asset Action Request to be saved
     */
    @Override
    public void savePhysicalAssetActionRequest(PhysicalAssetActionRequest physicalAssetActionRequest) throws StorageException {
        if(physicalAssetActionRequest == null)
            throw new StorageException("Physical Asset Action Request cannot be null.");

        add(physicalActionRequestList, physicalAssetActionRequest.getRequestTimestamp(),
                new PhysicalAssetActionRequestRecord(
                        physicalAssetActionRequest.getRequestTimestamp(),
                        physicalAssetActionRequest.getActionkey(),
                        physicalAssetActionRequest.getRequestBody(),
                        physicalAssetActionRequest.getRequestMetadata()));
    }

    /**
     * Save a Digital Action Request
     *
     * @param digitalActionRequest the Digital Action Request to be saved
     */
    @Override
    public void saveDigitalActionRequest(DigitalActionRequest digitalActionRequest) throws StorageException {
        if(digitalActionRequest == null)
            throw new StorageException("Digital Action Request cannot be null.");

        add(digitalActionRequestList, digitalActionRequest.getRequestTimestamp(),
                new DigitalActionRequestRecord(
                        digitalActionRequest.getRequestTimestamp(),
                        digitalActionRequest.getActionkey(),
                        digitalActionRequest.getRequestBody(),
                        digitalActionRequest.getRequestMetadata()));
    }

    /**
     * Save a new Physical Asset Description Available
     *
     * @param physicalAssetDescriptionNotification the Physical Asset Description Notification to be saved
     */
    @Override
    public void saveNewPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException {
        if(physicalAssetDescriptionNotification == null)
            throw new StorageException("Physical Asset Description Notification cannot be null.");

        add(newPhysicalAssetDescriptionNotificationList, physicalAssetDescriptionNotification.getNotificationTimestamp(),
                new PhysicalAssetDescriptionNotificationRecord(
                        physicalAssetDescriptionNotification.getNotificationTimestamp(),
                        physicalAssetDescriptionNotification.getAdapterId(),
                        physicalAssetDescriptionNotification.getPhysicalAssetDescription()));
    }

    /**
     * Save the updated Physical Asset Description
     * @param physicalAssetDescriptionNotification the updated Physical Asset Description
     */
    @Override
    public void saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException {
        if(physicalAssetDescriptionNotification == null)
            throw new StorageException("Physical Asset Description Notification cannot be null.");

        add(updatedPhysicalAssetDescriptionNotificationList, physicalAssetDescriptionNotification.getNotificationTimestamp(),
                new PhysicalAssetDescriptionNotificationRecord(
                        physicalAssetDescriptionNotification.getNotificationTimestamp(),
                        physicalAssetDescriptionNotification.getAdapterId(),
                        physicalAssetDescriptionNotification.getPhysicalAssetDescription()));
    }

    /**
     * Save the Physical Asset Property Variation
     *
     * @param physicalAssetPropertyVariation the Physical Asset Property Variation to be saved
     */
    @Override
    public void savePhysicalAssetPropertyVariation(PhysicalAssetPropertyVariation physicalAssetPropertyVariation) throws StorageException {
        if(physicalAssetPropertyVariation == null)
            throw new StorageException("Physical Asset Property Variation cannot be null.");

        add(physicalAssetPropertyVariationList, physicalAssetPropertyVariation.getTimestamp(),
                new PhysicalAssetPropertyVariationRecord(
                        physicalAssetPropertyVariation.getTimestamp(),
                        physicalAssetPropertyVariation.getPropertykey(),
                        physicalAssetPropertyVariation.getBody(),
                        physicalAssetPropertyVariation.getVariationMetadata()));
    }

    /**
     * Save the Physical Asset Relationship Instance Created Event
     *
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    @Override
    public void savePhysicalAssetRelationshipInstanceCreatedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException {
        if(physicalRelationshipInstanceVariation == null)
            throw new StorageException("Physical Relationship Instance Variation cannot be null.");

        Map<String, Object> metadata = new HashMap<>();
        if(physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getMetadata().isPresent())
            metadata = physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getMetadata().get();

        add(physicalRelationshipInstanceCreatedList, physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                new PhysicalRelationshipInstanceVariationRecord(
                        physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getKey(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getTargetId(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getName(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getType(),
                        metadata));
    }

    /**
     * Save the Physical Asset Relationship Instance Updated Event
     *
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    @Override
    public void savePhysicalAssetRelationshipInstanceDeletedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException {
        if(physicalRelationshipInstanceVariation == null)
            throw new StorageException("Physical Relationship Instance Variation cannot be null.");

        Map<String, Object> metadata = new HashMap<>();
        if(physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getMetadata().isPresent())
            metadata = physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getMetadata().get();

        add(physicalRelationshipInstanceDeletedList, physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                new PhysicalRelationshipInstanceVariationRecord(
                    physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getKey(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getTargetId(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getName(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getType(),
                    metadata));
        }

    // Records are kept in insertion order together with their storage timestamp. A list is used instead of a map keyed
    // by timestamp because several events can share the same millisecond and none of them must be lost.
    private static <T extends StorageRecord> void add(List<Map.Entry<Long, T>> target, long timestamp, T record) {
        target.add(new AbstractMap.SimpleImmutableEntry<>(timestamp, record));
    }

    private StorageStatsRecord getStorageStatsFromList(List<? extends Map.Entry<Long, ? extends StorageRecord>> target){
        StorageStatsRecord storageStatsRecord = new StorageStatsRecord();
        synchronized (target) {
            storageStatsRecord.setRecordCount(target.size());
            long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
            for (Map.Entry<Long, ? extends StorageRecord> entry : target) {
                min = Math.min(min, entry.getKey());
                max = Math.max(max, entry.getKey());
            }
            if (!target.isEmpty()) {
                storageStatsRecord.setRecordStartTimestampMs(min);
                storageStatsRecord.setRecordEndTimestampMs(max);
            }
        }
        return storageStatsRecord;
    }

    /**
     * Save the Augmentation Function Error
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the error
     * @param augmentationFunctionId the id of the Augmentation Function associated to the error
     * @param augmentationFunctionError the Augmentation Function Error to be saved
     * @throws StorageException if the provided Augmentation Function Error is null
     */
    @Override
    public void saveAugmentationFunctionError(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionError augmentationFunctionError) throws StorageException {
        if(augmentationFunctionError == null)
            throw new StorageException("Augmentation Function Error cannot be null.");

        add(augmentationFunctionErrorList, augmentationFunctionError.getTimestamp(),
                new AugmentationFunctionErrorRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionError.getErrorId(),
                        augmentationFunctionError.getAugmentationFunctionRequestId(),
                        augmentationFunctionError.getErrorType(),
                        augmentationFunctionError.getMessage(),
                        augmentationFunctionError.getMetadata()));

    }

    /**
     * Save the Augmentation Function Request
     * @param augmentationFunctionId the id of the Augmentation Function associated to the request
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the request
     * @param augmentationFunctionRequest the Augmentation Function Request to be saved
     * @throws StorageException if the provided Augmentation Function Request is null
     */
    @Override
    public void saveAugmentationFunctionRequest(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionRequest augmentationFunctionRequest) throws StorageException {
        if(augmentationFunctionRequest == null)
            throw new StorageException("Augmentation Function Request cannot be null.");

        add(augmentationFunctionRequestList, augmentationFunctionRequest.getTimestamp(),
                new AugmentationFunctionRequestRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionRequest.getRequestId(),
                        augmentationFunctionRequest.getContext(),
                        augmentationFunctionRequest.getType()));
    }

    /**
     * Save the Augmentation Function Result
     * @param augmentationFunctionId the id of the Augmentation Function associated to the result
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the result
     * @param augmentationFunctionResult the Augmentation Function Result to be saved
     * @throws StorageException if the provided Augmentation Function Result is null
     */
    @Override
    public void saveAugmentationFunctionResult(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionResult<?> augmentationFunctionResult) throws StorageException {
        if(augmentationFunctionResult == null)
            throw new StorageException("Augmentation Function Result cannot be null.");

        add(augmentationFunctionResultList, augmentationFunctionResult.getTimestamp(),
                new AugmentationFunctionResultRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionResult.getRequest().getRequestId(),
                        augmentationFunctionResult.getKey(),
                        augmentationFunctionResult.getType(),
                        augmentationFunctionResult.getValue(),
                        augmentationFunctionResult.getAugmentationFunctionResultMetrics(),
                        augmentationFunctionResult.getMetadata()));
    }

    /**
     * Save the Augmentation Function Registration
     * @param augmentationFunctionId the id of the Augmentation Function associated to the registration
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the registration
     * @param augmentationFunctionType the type of the Augmentation Function associated to the registration
     * @throws StorageException if any of the provided parameters is null
     */
    @Override
    public void saveAugmentationFunctionRegistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType) throws StorageException {
        if(augmentationFunctionId == null || augmentationFunctionHandlerId == null || augmentationFunctionType == null)
            throw new StorageException("Augmentation Function Registration parameters cannot be null.");

        add(augmentationFunctionRegistrationList, System.currentTimeMillis(),
                new AugmentationFunctionRegistrationRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionType));
    }

    /**
     * Save the Augmentation Function Unregistration
     * @param augmentationFunctionId the id of the Augmentation Function associated to the unregistration
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the unregistration
     * @param augmentationFunctionType the type of the Augmentation Function associated to the unregistration
     * @throws StorageException if any of the provided parameters is null
     */
    @Override
    public void saveAugmentationFunctionUnregistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType) throws StorageException {
        if(augmentationFunctionId == null || augmentationFunctionHandlerId == null || augmentationFunctionType == null)
            throw new StorageException("Augmentation Function Unregistration parameters cannot be null.");

        add(augmentationFunctionUnregistrationList, System.currentTimeMillis(),
                new AugmentationFunctionUnregistrationRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionType));
    }

    /**
     * Execute the query over the in-memory records through the {@link InMemoryQueryEvaluator}
     * @param query the query to execute
     * @return the query result
     */
    @Override
    public QueryResult<?> execute(Query query) throws StorageException {

        try {

            switch (query.getResource()) {
                case DIGITAL_TWIN_STATE:
                    return InMemoryQueryEvaluator.evaluate(query, digitalTwinStateList);
                case DIGITAL_TWIN_STATE_EVENT_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, digitalStateEventNotificationList);
                case PHYSICAL_ASSET_PROPERTY_VARIATION:
                    return InMemoryQueryEvaluator.evaluate(query, physicalAssetPropertyVariationList);
                case PHYSICAL_ASSET_EVENT_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, physicalEventNotificationsList);
                case PHYSICAL_ACTION_REQUEST:
                    return InMemoryQueryEvaluator.evaluate(query, physicalActionRequestList);
                case DIGITAL_ACTION_REQUEST:
                    return InMemoryQueryEvaluator.evaluate(query, digitalActionRequestList);
                case NEW_PAD_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, newPhysicalAssetDescriptionNotificationList);
                case UPDATED_PAD_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, updatedPhysicalAssetDescriptionNotificationList);
                case PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, physicalRelationshipInstanceCreatedList);
                case PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION:
                    return InMemoryQueryEvaluator.evaluate(query, physicalRelationshipInstanceDeletedList);
                case AUGMENTATION_FUNCTION_ERROR:
                    return InMemoryQueryEvaluator.evaluate(query, augmentationFunctionErrorList);
                case AUGMENTATION_FUNCTION_REQUEST:
                    return InMemoryQueryEvaluator.evaluate(query, augmentationFunctionRequestList);
                case AUGMENTATION_FUNCTION_RESULT:
                    return InMemoryQueryEvaluator.evaluate(query, augmentationFunctionResultList);
                case AUGMENTATION_FUNCTION_REGISTRATION:
                    return InMemoryQueryEvaluator.evaluate(query, augmentationFunctionRegistrationList);
                case AUGMENTATION_FUNCTION_UNREGISTRATION:
                    return InMemoryQueryEvaluator.evaluate(query, augmentationFunctionUnregistrationList);
                case LIFE_CYCLE_EVENT:
                    return InMemoryQueryEvaluator.evaluate(query, lifeCycleStateList);
                case STORAGE_STATS:
                    return QueryResult.success(query, Collections.singletonList(getStorageStats()));
                default:
                    return QueryResult.failure(query, "Unsupported query resource: " + query.getResource());
            }

        } catch (Exception e) {
            return QueryResult.failure(query, e.getMessage());
        }
    }

    /**
     * Retrieve and returns storage statistics in terms of the number of stored records for each type and the
     * associated time range of the stored records (start and end timestamp)
     * @return the storage statistics
     * @throws StorageException if an error occurs while retrieving the storage statistics
     */
    @Override
    public StorageStats getStorageStats() throws StorageException {
        try {
            // Set the StorageStateRecords in the final class
            StorageStats storageStats = new StorageStats();
            storageStats.setStateVariationStats(getStorageStatsFromList(digitalTwinStateList));
            storageStats.setLifeCycleVariationStats(getStorageStatsFromList(lifeCycleStateList));
            storageStats.setPhysicalAssetPropertyVariationStats(getStorageStatsFromList(physicalAssetPropertyVariationList));
            storageStats.setPhysicalAssetEventNotificationStats(getStorageStatsFromList(physicalEventNotificationsList));
            storageStats.setPhysicalAssetActionRequestStats(getStorageStatsFromList(physicalActionRequestList));
            storageStats.setDigitalActionRequestStats(getStorageStatsFromList(digitalActionRequestList));
            storageStats.setNewPhysicalAssetDescriptionNotificationStats(getStorageStatsFromList(newPhysicalAssetDescriptionNotificationList));
            storageStats.setUpdatedPhysicalAssetDescriptionNotificationStats(getStorageStatsFromList(updatedPhysicalAssetDescriptionNotificationList));
            storageStats.setPhysicalRelationshipInstanceCreatedNotificationStats(getStorageStatsFromList(physicalRelationshipInstanceCreatedList));
            storageStats.setPhysicalRelationshipInstanceDeletedNotificationStats(getStorageStatsFromList(physicalRelationshipInstanceDeletedList));
            storageStats.setAugmentationFunctionErrorStats(getStorageStatsFromList(augmentationFunctionErrorList));
            storageStats.setAugmentationFunctionRequestStats(getStorageStatsFromList(augmentationFunctionRequestList));
            storageStats.setAugmentationFunctionResultStats(getStorageStatsFromList(augmentationFunctionResultList));
            storageStats.setAugmentationFunctionRegistrationStats(getStorageStatsFromList(augmentationFunctionRegistrationList));
            storageStats.setAugmentationFunctionUnregistrationStats(getStorageStatsFromList(augmentationFunctionUnregistrationList));

            return storageStats;
        }catch (Exception e){
            throw new StorageException("Error while retrieving the storage statistics: " + e.getMessage());
        }
    }

}

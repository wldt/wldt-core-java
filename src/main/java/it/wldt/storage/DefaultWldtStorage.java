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
    private Map<Long, DigitalTwinStateRecord> digitalTwinStateMap;
    private Map<Long, DigitalTwinStateEventNotificationRecord> digitalStateEventNotificationMap;
    private Map<Long, PhysicalAssetActionRequestRecord> physicalActionRequestMap;
    private Map<Long, PhysicalAssetEventNotificationRecord> physicalEventNotificationsMap;
    private Map<Long, DigitalActionRequestRecord> digitalActionRequestMap;
    private Map<Long, PhysicalAssetDescriptionNotificationRecord> newPhysicalAssetDescriptionNotificationMap;
    private Map<Long, PhysicalAssetDescriptionNotificationRecord> updatedPhysicalAssetDescriptionNotificationMap;
    private Map<Long, PhysicalAssetPropertyVariationRecord> physicalAssetPropertyVariationMap;
    private Map<Long, PhysicalRelationshipInstanceVariationRecord> physicalRelationshipInstanceCreatedMap;
    private Map<Long, PhysicalRelationshipInstanceVariationRecord> physicalRelationshipInstanceDeletedMap;
    private Map<Long, LifeCycleVariationRecord> lifeCycleStateMap;
    private Map<Long, AugmentationFunctionErrorRecord> augmentationFunctionErrorMap;
    private Map<Long, AugmentationFunctionRegistrationRecord> augmentationFunctionRegistrationMap;
    private Map<Long, AugmentationFunctionUnregistrationRecord> augmentationFunctionUnregistrationMap;
    private Map<Long, AugmentationFunctionRequestRecord> augmentationFunctionRequestMap;
    private Map<Long, AugmentationFunctionResultRecord> augmentationFunctionResultMap;

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

        this.digitalTwinStateMap = new HashMap<>();
        this.digitalStateEventNotificationMap= new HashMap<>();
        this.physicalActionRequestMap = new HashMap<>();
        this.physicalEventNotificationsMap = new HashMap<>();
        this.digitalActionRequestMap = new HashMap<>();
        this.newPhysicalAssetDescriptionNotificationMap = new HashMap<>();
        this.updatedPhysicalAssetDescriptionNotificationMap = new HashMap<>();
        this.physicalAssetPropertyVariationMap = new HashMap<>();
        this.physicalRelationshipInstanceCreatedMap = new HashMap<>();
        this.physicalRelationshipInstanceDeletedMap = new HashMap<>();
        this.lifeCycleStateMap = new HashMap<>();
        this.augmentationFunctionErrorMap = new HashMap<>();
        this.augmentationFunctionRequestMap = new HashMap<>();
        this.augmentationFunctionResultMap = new HashMap<>();
        this.augmentationFunctionRegistrationMap = new HashMap<>();
        this.augmentationFunctionUnregistrationMap = new HashMap<>();
    }

    /**
     * Clear the WLDT Storage canceling all the stored data and information
     */
    @Override
    public void clear(){

        logger.info("Clearing Default WLDT Storage...");

        this.digitalTwinStateMap.clear();
        this.digitalStateEventNotificationMap.clear();
        this.physicalActionRequestMap.clear();
        this.physicalEventNotificationsMap.clear();
        this.digitalActionRequestMap.clear();
        this.newPhysicalAssetDescriptionNotificationMap.clear();
        this.updatedPhysicalAssetDescriptionNotificationMap.clear();
        this.physicalAssetPropertyVariationMap.clear();
        this.physicalRelationshipInstanceCreatedMap.clear();
        this.physicalRelationshipInstanceDeletedMap.clear();
        this.lifeCycleStateMap.clear();
        this.augmentationFunctionErrorMap.clear();
        this.augmentationFunctionRequestMap.clear();
        this.augmentationFunctionResultMap.clear();
        this.augmentationFunctionRegistrationMap.clear();
        this.augmentationFunctionUnregistrationMap.clear();
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
        digitalTwinStateMap.put(digitalTwinState.getEvaluationInstant().toEpochMilli(), new DigitalTwinStateRecord(digitalTwinState, digitalTwinStateChangeList));
    }

    /**
     * Returns the latest computed Digital Twin State of the target Digital Twin instance
     * @return the latest computed Digital Twin State
     */
    @Override
    public Optional<DigitalTwinStateRecord> getLastDigitalTwinState() {

        if (digitalTwinStateMap.isEmpty()) {
            return Optional.empty();
        }
        long lastTimestamp = Collections.max(digitalTwinStateMap.keySet());
        return Optional.ofNullable(digitalTwinStateMap.get(lastTimestamp));
    }

    /**
     * Returns the number of computed and stored Digital Twin States
     * @return The number of computed Digital Twin States
     */
    @Override
    public int getDigitalTwinStateCount() {
        return digitalTwinStateMap.size();
    }

    /**
     * Retrieves a list of DigitalTwinState objects within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of DigitalTwinState objects representing the state of the digital twin within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    @Override
    public List<DigitalTwinStateRecord> getDigitalTwinStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        if (startTimestampMs > endTimestampMs) {
            throw new IllegalArgumentException("Start timestamp cannot be greater than end timestamp.");
        }
        List<DigitalTwinStateRecord> result = new ArrayList<>();
        for (Map.Entry<Long, DigitalTwinStateRecord> entry : digitalTwinStateMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Retrieves a list of Digital Twin states within the specified range of indices.
     *
     * @param startIndex the index of the first digital twin state to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last digital twin state to retrieve (inclusive)
     * @return a list of digital twin states within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<DigitalTwinStateRecord> getDigitalTwinStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalTwinStateMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }
        List<DigitalTwinStateRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(digitalTwinStateMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(digitalTwinStateMap.get(timestamps.get(i)));
        }
        return result;
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
        this.digitalStateEventNotificationMap.put(digitalTwinStateEventNotification.getTimestamp(),
                new DigitalTwinStateEventNotificationRecord(
                        digitalTwinStateEventNotification.getDigitalEventKey(),
                        digitalTwinStateEventNotification.getBody(),
                        digitalTwinStateEventNotification.getTimestamp()));
    }

    /**
     * Get the number of Digital Twin State Event Notification
     *
     * @return the number of Digital Twin State Event Notification
     */
    @Override
    public int getDigitalTwinStateEventNotificationCount() throws StorageException {
        return this.digitalStateEventNotificationMap.size();
    }

    /**
     * Get the Digital Twin State Event Notification in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Digital Twin State Event Notification in the specified time range
     */
    @Override
    public List<DigitalTwinStateEventNotificationRecord> getDigitalTwinStateEventNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException {
        List<DigitalTwinStateEventNotificationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, DigitalTwinStateEventNotificationRecord> entry : digitalStateEventNotificationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Digital Twin State Event Notification in the specified range of indices
     *
     * @param startIndex the index of the first Digital Twin State Event Notification to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Digital Twin State Event Notification to retrieve (inclusive)
     * @return a list of Digital Twin State Event Notification within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<DigitalTwinStateEventNotificationRecord> getDigitalTwinStateEventNotificationInRange(int startIndex, int endIndex) throws StorageException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalStateEventNotificationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<DigitalTwinStateEventNotificationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(digitalStateEventNotificationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(digitalStateEventNotificationMap.get(timestamps.get(i)));
        }
        return result;
    }

    /**
     * Save the LifeCycleState of the Digital Twin
     * @param lifeCycleStateVariation the LifeCycleState of the Digital Twin to be saved
     */
    @Override
    public void saveLifeCycleState(LifeCycleStateVariation lifeCycleStateVariation) {
        // Implement this method using the variable lifeCycleStateMap
        lifeCycleStateMap.put(lifeCycleStateVariation.getTimestamp(),
                new LifeCycleVariationRecord(lifeCycleStateVariation.getLifeCycleState(),
                        lifeCycleStateVariation.getTimestamp()));

    }

    /**
     * Get the last LifeCycleState of the Digital Twin
     *
     * @return the last LifeCycleState of the Digital Twin
     */
    @Override
    public LifeCycleVariationRecord getLastLifeCycleState() throws StorageException {
        return lifeCycleStateMap.isEmpty() ? null : lifeCycleStateMap.get(Collections.max(lifeCycleStateMap.keySet()));
    }

    /**
     * Get the number of LifeCycleState of the Digital Twin
     * @return the number of LifeCycleState of the Digital Twin
     */
    @Override
    public int getLifeCycleStateCount() {
        return lifeCycleStateMap.size();
    }

    /**
     * Get the last LifeCycleState of the Digital Twin
     * @param startTimestampMs
     * @param endTimestampMs
     * @return the last LifeCycleState of the Digital Twin
     */
    @Override
    public List<LifeCycleVariationRecord> getLifeCycleStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<LifeCycleVariationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, LifeCycleVariationRecord> entry : lifeCycleStateMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the LifeCycleState of the Digital Twin in the specified range of indices
     *
     * @param startIndex the index of the first LifeCycleState to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last LifeCycleState to retrieve (inclusive)
     * @return a list of LifeCycleState within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<LifeCycleVariationRecord> getLifeCycleStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= lifeCycleStateMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<LifeCycleVariationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(lifeCycleStateMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(lifeCycleStateMap.get(timestamps.get(i)));
        }
        return result;
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

        this.physicalEventNotificationsMap.put(physicalAssetEventNotification.getTimestamp(),
                new PhysicalAssetEventNotificationRecord(
                        physicalAssetEventNotification.getTimestamp(),
                        physicalAssetEventNotification.getEventkey(),
                        physicalAssetEventNotification.getBody(),
                        physicalAssetEventNotification.getMetadata()));
    }

    /**
     * Get the number of Physical Asset Event Notification
     *
     * @return the number of Physical Asset Event Notification
     */
    @Override
    public int getPhysicalAssetEventNotificationCount() throws StorageException {
        return this.physicalEventNotificationsMap.size();
    }

    /**
     * Get the Physical Asset Event Notification in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Physical Asset Event Notification in the specified time range
     */
    @Override
    public List<PhysicalAssetEventNotificationRecord> getPhysicalAssetEventNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException {
        List<PhysicalAssetEventNotificationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetEventNotificationRecord> entry : physicalEventNotificationsMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Physical Asset Event Notification in the specified range of indices
     *
     * @param startIndex the index of the first Physical Asset Event Notification to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Event Notification to retrieve (inclusive)
     * @return a list of Physical Asset Event Notification within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetEventNotificationRecord> getPhysicalAssetEventNotificationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalEventNotificationsMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetEventNotificationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalEventNotificationsMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalEventNotificationsMap.get(timestamps.get(i)));
        }
        return result;
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

        this.physicalActionRequestMap.put(physicalAssetActionRequest.getRequestTimestamp(),
                new PhysicalAssetActionRequestRecord(
                        physicalAssetActionRequest.getRequestTimestamp(),
                        physicalAssetActionRequest.getActionkey(),
                        physicalAssetActionRequest.getRequestBody(),
                        physicalAssetActionRequest.getRequestMetadata()));
    }

    /**
     * Get the number of Physical Asset Action Request
     * @return the number of Physical Asset Action Request
     */
    @Override
    public int getPhysicalAssetActionRequestCount() {
        return this.physicalActionRequestMap.size();
    }

    /**
     * Get the Physical Asset Action Request in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Physical Asset Action Request in the specified time range
     */
    @Override
    public List<PhysicalAssetActionRequestRecord> getPhysicalAssetActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetActionRequestRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetActionRequestRecord> entry : physicalActionRequestMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Physical Asset Action Request in the specified range of indices
     *
     * @param startIndex the index of the first Physical Asset Action Request to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Action Request to retrieve (inclusive)
     * @return a list of Physical Asset Action Request within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetActionRequestRecord> getPhysicalAssetActionRequestInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalActionRequestMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetActionRequestRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalActionRequestMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalActionRequestMap.get(timestamps.get(i)));
        }
        return result;
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

        this.digitalActionRequestMap.put(digitalActionRequest.getRequestTimestamp(),
                new DigitalActionRequestRecord(
                        digitalActionRequest.getRequestTimestamp(),
                        digitalActionRequest.getActionkey(),
                        digitalActionRequest.getRequestBody(),
                        digitalActionRequest.getRequestMetadata()));
    }

    /**
     * Get the number of Digital Action Request Stored
     *
     * @return the number of Digital Action Request
     */
    @Override
    public int getDigitalActionRequestCount() {
        return this.digitalActionRequestMap.size();
    }

    /**
     * Get the Digital Action Request in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Digital Action Request in the specified time range
     */
    @Override
    public List<DigitalActionRequestRecord> getDigitalActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<DigitalActionRequestRecord> result = new ArrayList<>();
        for (Map.Entry<Long, DigitalActionRequestRecord> entry : digitalActionRequestMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Digital Action Request in the specified range of indices
     *
     * @param startIndex the index of the first Digital Action Request to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Digital Action Request to retrieve (inclusive)
     * @return a list of Digital Action Request within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<DigitalActionRequestRecord> getDigitalActionRequestInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalActionRequestMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<DigitalActionRequestRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(digitalActionRequestMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(digitalActionRequestMap.get(timestamps.get(i)));
        }
        return result;
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

        this.newPhysicalAssetDescriptionNotificationMap.put(physicalAssetDescriptionNotification.getNotificationTimestamp(),
                new PhysicalAssetDescriptionNotificationRecord(
                        physicalAssetDescriptionNotification.getNotificationTimestamp(),
                        physicalAssetDescriptionNotification.getAdapterId(),
                        physicalAssetDescriptionNotification.getPhysicalAssetDescription()));
    }

    /**
     * Get the number of New Physical Asset Description Notifications available
     *
     * @return the number of Physical Asset Description Notifications available
     */
    @Override
    public int getNewPhysicalAssetDescriptionNotificationCount() {
        return this.newPhysicalAssetDescriptionNotificationMap.size();
    }

    /**
     * Get the New Physical Asset Description Available in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of New Physical Asset Description Available in the specified time range
     */
    @Override
    public List<PhysicalAssetDescriptionNotificationRecord> getNewPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetDescriptionNotificationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetDescriptionNotificationRecord> entry : newPhysicalAssetDescriptionNotificationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the New Physical Asset Description Available in the specified range of indices
     *
     * @param startIndex the index of the first New Physical Asset Description to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last New Physical Asset Description to retrieve (inclusive)
     * @return a list of New Physical Asset Description within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetDescriptionNotificationRecord> getNewPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= newPhysicalAssetDescriptionNotificationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetDescriptionNotificationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(newPhysicalAssetDescriptionNotificationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(newPhysicalAssetDescriptionNotificationMap.get(timestamps.get(i)));
        }
        return result;
    }

    /**
     * Save the updated Physical Asset Description
     * @param physicalAssetDescriptionNotification the updated Physical Asset Description
     */
    @Override
    public void saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException {
        if(physicalAssetDescriptionNotification == null)
            throw new StorageException("Physical Asset Description Notification cannot be null.");

        this.updatedPhysicalAssetDescriptionNotificationMap.put(physicalAssetDescriptionNotification.getNotificationTimestamp(),
                new PhysicalAssetDescriptionNotificationRecord(
                        physicalAssetDescriptionNotification.getNotificationTimestamp(),
                        physicalAssetDescriptionNotification.getAdapterId(),
                        physicalAssetDescriptionNotification.getPhysicalAssetDescription()));
    }

    /**
     * Get the number of Updated Physical Asset Description
     *
     * @return the number of Updated Physical Asset Description
     */
    @Override
    public int getUpdatedPhysicalAssetDescriptionNotificationCount() {
        return this.updatedPhysicalAssetDescriptionNotificationMap.size();
    }

    /**
     * Get the Updated Physical Asset Description in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Updated Physical Asset Description in the specified time range
     */
    @Override
    public List<PhysicalAssetDescriptionNotificationRecord> getUpdatedPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetDescriptionNotificationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetDescriptionNotificationRecord> entry : updatedPhysicalAssetDescriptionNotificationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Updated Physical Asset Description in the specified range of indices
     *
     * @param startIndex the index of the first Updated Physical Asset Description to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Updated Physical Asset Description to retrieve (inclusive)
     * @return a list of Updated Physical Asset Description within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetDescriptionNotificationRecord> getUpdatedPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= updatedPhysicalAssetDescriptionNotificationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetDescriptionNotificationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(updatedPhysicalAssetDescriptionNotificationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(updatedPhysicalAssetDescriptionNotificationMap.get(timestamps.get(i)));
        }
        return result;
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

        this.physicalAssetPropertyVariationMap.put(physicalAssetPropertyVariation.getTimestamp(),
                new PhysicalAssetPropertyVariationRecord(
                        physicalAssetPropertyVariation.getTimestamp(),
                        physicalAssetPropertyVariation.getPropertykey(),
                        physicalAssetPropertyVariation.getBody(),
                        physicalAssetPropertyVariation.getVariationMetadata()));
    }

    /**
     * Get the number of Physical Asset Property Variation
     *
     * @return the number of Physical Asset Property Variation
     */
    @Override
    public int getPhysicalAssetPropertyVariationCount() throws StorageException{
        return this.physicalAssetPropertyVariationMap.size();
    }

    /**
     * Get the Physical Asset Property Variation in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Physical Asset Property Variation in the specified time range
     */
    @Override
    public List<PhysicalAssetPropertyVariationRecord> getPhysicalAssetPropertyVariationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetPropertyVariationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetPropertyVariationRecord> entry : physicalAssetPropertyVariationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Physical Asset Property Variation in the specified range of indices
     *
     * @param startIndex the index of the first Physical Asset Property Variation to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Property Variation to retrieve (inclusive)
     * @return a list of Physical Asset Property Variation within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetPropertyVariationRecord> getPhysicalAssetPropertyVariationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalAssetPropertyVariationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetPropertyVariationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalAssetPropertyVariationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalAssetPropertyVariationMap.get(timestamps.get(i)));
        }
        return result;
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

        this.physicalRelationshipInstanceCreatedMap.put(physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                new PhysicalRelationshipInstanceVariationRecord(
                        physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getKey(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getTargetId(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getName(),
                        physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getType(),
                        metadata));
    }

    /**
     * Get the number of Physical Asset Relationship Instance Created Event
     *
     * @return the number of Physical Asset Relationship Instance Created Event
     */
    @Override
    public int getPhysicalAssetRelationshipInstanceCreatedNotificationCount() {
        return this.physicalRelationshipInstanceCreatedMap.size();
    }

    /**
     * Get the Physical Asset Relationship Instance Created Event in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Physical Asset Relationship Instance Created Event in the specified time range
     */
    @Override
    public List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceCreatedNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalRelationshipInstanceVariationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalRelationshipInstanceVariationRecord> entry : physicalRelationshipInstanceCreatedMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Physical Asset Relationship Instance Created Event in the specified range of indices
     *
     * @param startIndex the index of the first Physical Asset Property Variation to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Property Variation to retrieve (inclusive)
     * @return a list of Physical Asset Relationship Instance Created Event within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceCreatedNotificationInRange(int startIndex, int endIndex) throws IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalRelationshipInstanceCreatedMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalRelationshipInstanceVariationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalRelationshipInstanceCreatedMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalRelationshipInstanceCreatedMap.get(timestamps.get(i)));
        }
        return result;
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

        this.physicalRelationshipInstanceDeletedMap.put(physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                new PhysicalRelationshipInstanceVariationRecord(
                    physicalRelationshipInstanceVariation.getNotificationTimestamp(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getKey(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getTargetId(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getName(),
                    physicalRelationshipInstanceVariation.getPhysicalAssetRelationshipInstance().getRelationship().getType(),
                    metadata));
        }

    /**
     * Get the number of Physical Asset Relationship Instance Updated Event
     *
     * @return the number of Physical Asset Relationship Instance Updated Event
     */
    @Override
    public int getPhysicalAssetRelationshipInstanceDeletedNotificationCount() {
        return this.physicalRelationshipInstanceDeletedMap.size();
    }

    /**
     * Get the Physical Asset Relationship Instance Updated Event in the specified time range
     *
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs   the end timestamp of the time range
     * @return the list of Physical Asset Relationship Instance Updated Event in the specified time range
     */
    @Override
    public List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceDeletedNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalRelationshipInstanceVariationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalRelationshipInstanceVariationRecord> entry : physicalRelationshipInstanceDeletedMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Physical Asset Relationship Instance Updated Event in the specified range of indices
     *
     * @param startIndex the index of the first Physical Asset Relationship Instance Updated Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Relationship Instance Updated Event to retrieve (inclusive)
     * @return a list of Physical Asset Relationship Instance Updated Event within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceDeletedNotificationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalRelationshipInstanceDeletedMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalRelationshipInstanceVariationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalRelationshipInstanceDeletedMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalRelationshipInstanceDeletedMap.get(timestamps.get(i)));
        }
        return result;
    }

    /**
     * Get the number stored information of the target Map
     * @return th StorageStatsRecord associated to the target Map
     */
    private StorageStatsRecord getStorageStatsFromMap(Map<Long, ? extends StorageRecord> targetMap){

        StorageStatsRecord storageStatsRecord = new StorageStatsRecord();
        storageStatsRecord.setRecordCount(targetMap.size());

        if(!targetMap.isEmpty()){
            List<Long> timestamps = new ArrayList<>(targetMap.keySet());
            storageStatsRecord.setRecordStartTimestampMs(Collections.min(timestamps));
            storageStatsRecord.setRecordEndTimestampMs(Collections.max(timestamps));
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

        this.augmentationFunctionErrorMap.put(augmentationFunctionError.getTimestamp(),
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
     * Get the number of stored Augmentation Function Error
     * @return the number of stored Augmentation Function Error
     */
    @Override
    public int getAugmentationFunctionErrorCount() {
        return this.augmentationFunctionErrorMap.size();
    }

    /**
     * Get the Augmentation Function Error in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Augmentation Function Error in the specified time range
     * @throws IllegalArgumentException if the start timestamp is greater than the end timestamp
     */
    @Override
    public List<AugmentationFunctionErrorRecord> getAugmentationFunctionErrorsInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<AugmentationFunctionErrorRecord> result = new ArrayList<>();
        for (Map.Entry<Long, AugmentationFunctionErrorRecord> entry : augmentationFunctionErrorMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Augmentation Function Error in the specified range of indices
     * @param startIndex the index of the first error occurred during the execution of the Augmentation Functions to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last error occurred during the execution of the Augmentation Functions to retrieve (inclusive)
     * @return a list of Augmentation Function Error within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    @Override
    public List<AugmentationFunctionErrorRecord> getAugmentationFunctionErrorsInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= augmentationFunctionErrorMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<AugmentationFunctionErrorRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(augmentationFunctionErrorMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(augmentationFunctionErrorMap.get(timestamps.get(i)));
        }
        return result;
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

        this.augmentationFunctionRequestMap.put(augmentationFunctionRequest.getTimestamp(),
                new AugmentationFunctionRequestRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionRequest.getRequestId(),
                        augmentationFunctionRequest.getContext(),
                        augmentationFunctionRequest.getType()));
    }

    /**
     * Get the number of stored Augmentation Function Request
     * @return the number of stored Augmentation Function Request
     */
    @Override
    public int getAugmentationFunctionRequestCount() {
        return this.augmentationFunctionRequestMap.size();
    }

    /**
     * Get the Augmentation Function Request in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Augmentation Function Request in the specified time range
     * @throws IllegalArgumentException if the start timestamp is greater than the end timestamp
     */
    @Override
    public List<AugmentationFunctionRequestRecord> getAugmentationFunctionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<AugmentationFunctionRequestRecord> result = new ArrayList<>();
        for (Map.Entry<Long, AugmentationFunctionRequestRecord> entry : augmentationFunctionRequestMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Augmentation Function Request in the specified range of indices
     * @param startIndex the index of the first Augmentation Function Request to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Augmentation Function Request to retrieve (inclusive)
     * @return a list of Augmentation Function Request within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    @Override
    public List<AugmentationFunctionRequestRecord> getAugmentationFunctionRequestInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= augmentationFunctionRequestMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<AugmentationFunctionRequestRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(augmentationFunctionRequestMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(augmentationFunctionRequestMap.get(timestamps.get(i)));
        }
        return result;
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

        this.augmentationFunctionResultMap.put(augmentationFunctionResult.getTimestamp(),
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
     * Get the number of stored Augmentation Function Result
     * @return the number of stored Augmentation Function Result
     */
    @Override
    public int getAugmentationFunctionResultCount() {
        return this.augmentationFunctionResultMap.size();
    }

    /**
     * Get the Augmentation Function Result in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Augmentation Function Result in the specified time range
     * @throws IllegalArgumentException if the start timestamp is greater than the end timestamp
     */
    @Override
    public List<AugmentationFunctionResultRecord> getAugmentationFunctionResultInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<AugmentationFunctionResultRecord> result = new ArrayList<>();
        for (Map.Entry<Long, AugmentationFunctionResultRecord> entry : augmentationFunctionResultMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Augmentation Function Result in the specified range of indices
     * @param startIndex the index of the first Augmentation Function Result to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Augmentation Function Result to retrieve (inclusive)
     * @return a list of Augmentation Function Result within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    @Override
    public List<AugmentationFunctionResultRecord> getAugmentationFunctionResultInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= augmentationFunctionResultMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<AugmentationFunctionResultRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(augmentationFunctionResultMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(augmentationFunctionResultMap.get(timestamps.get(i)));
        }
        return result;
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

        this.augmentationFunctionRegistrationMap.put(System.currentTimeMillis(),
                new AugmentationFunctionRegistrationRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionType));
    }

    /**
     * Get the number of stored Augmentation Function Registration
     * @return the number of stored Augmentation Function Registration
     */
    @Override
    public int getAugmentationFunctionRegistrationCount() {
        return this.augmentationFunctionRegistrationMap.size();
    }

    /**
     * Get the Augmentation Function Registration in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Augmentation Function Registration in the specified time range
     * @throws IllegalArgumentException if the start timestamp is greater than the end timestamp
     */
    @Override
    public List<AugmentationFunctionRegistrationRecord> getAugmentationFunctionRegistrationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<AugmentationFunctionRegistrationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, AugmentationFunctionRegistrationRecord> entry : augmentationFunctionRegistrationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Augmentation Function Registration in the specified range of indices
     * @param startIndex the index of the first Augmentation Function Registration to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Augmentation Function Registration to retrieve (inclusive)
     * @return a list of Augmentation Function Registration within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    @Override
    public List<AugmentationFunctionRegistrationRecord> getAugmentationFunctionRegistrationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= augmentationFunctionRegistrationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<AugmentationFunctionRegistrationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(augmentationFunctionRegistrationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(augmentationFunctionRegistrationMap.get(timestamps.get(i)));
        }
        return result;
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

        this.augmentationFunctionUnregistrationMap.put(System.currentTimeMillis(),
                new AugmentationFunctionUnregistrationRecord(
                        augmentationFunctionId,
                        augmentationFunctionHandlerId,
                        augmentationFunctionType));
    }

    /**
     * Get the number of stored Augmentation Function Unregistration
     * @return the number of stored Augmentation Function Unregistration
     */
    @Override
    public int getAugmentationFunctionUnregistrationCount() {
        return this.augmentationFunctionUnregistrationMap.size();
    }

    /**
     * Get the Augmentation Function Unregistration in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Augmentation Function Unregistration in the specified time range
     * @throws IllegalArgumentException if the start timestamp is greater than the end timestamp
     */
    @Override
    public List<AugmentationFunctionUnregistrationRecord> getAugmentationFunctionUnregistrationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<AugmentationFunctionUnregistrationRecord> result = new ArrayList<>();
        for (Map.Entry<Long, AugmentationFunctionUnregistrationRecord> entry : augmentationFunctionUnregistrationMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get the Augmentation Function Unregistration in the specified range of indices
     * @param startIndex the index of the first Augmentation Function Unregistration to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Augmentation Function Unregistration to retrieve (inclusive)
     * @return a list of Augmentation Function Unregistration within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    @Override
    public List<AugmentationFunctionUnregistrationRecord> getAugmentationFunctionUnregistrationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= augmentationFunctionUnregistrationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<AugmentationFunctionUnregistrationRecord> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(augmentationFunctionUnregistrationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(augmentationFunctionUnregistrationMap.get(timestamps.get(i)));
        }
        return result;
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
            storageStats.setStateVariationStats(getStorageStatsFromMap(digitalTwinStateMap));
            storageStats.setLifeCycleVariationStats(getStorageStatsFromMap(lifeCycleStateMap));
            storageStats.setPhysicalAssetPropertyVariationStats(getStorageStatsFromMap(physicalAssetPropertyVariationMap));
            storageStats.setPhysicalAssetEventNotificationStats(getStorageStatsFromMap(physicalEventNotificationsMap));
            storageStats.setPhysicalAssetActionRequestStats(getStorageStatsFromMap(physicalActionRequestMap));
            storageStats.setDigitalActionRequestStats(getStorageStatsFromMap(digitalActionRequestMap));
            storageStats.setNewPhysicalAssetDescriptionNotificationStats(getStorageStatsFromMap(newPhysicalAssetDescriptionNotificationMap));
            storageStats.setUpdatedPhysicalAssetDescriptionNotificationStats(getStorageStatsFromMap(updatedPhysicalAssetDescriptionNotificationMap));
            storageStats.setPhysicalRelationshipInstanceCreatedNotificationStats(getStorageStatsFromMap(physicalRelationshipInstanceCreatedMap));
            storageStats.setPhysicalRelationshipInstanceDeletedNotificationStats(getStorageStatsFromMap(physicalRelationshipInstanceDeletedMap));
            storageStats.setAugmentationFunctionErrorStats(getStorageStatsFromMap(augmentationFunctionErrorMap));
            storageStats.setAugmentationFunctionRequestStats(getStorageStatsFromMap(augmentationFunctionRequestMap));
            storageStats.setAugmentationFunctionResultStats(getStorageStatsFromMap(augmentationFunctionResultMap));
            storageStats.setAugmentationFunctionRegistrationStats(getStorageStatsFromMap(augmentationFunctionRegistrationMap));
            storageStats.setAugmentationFunctionUnregistrationStats(getStorageStatsFromMap(augmentationFunctionUnregistrationMap));

            return storageStats;
        }catch (Exception e){
            throw new StorageException("Error while retrieving the storage statistics: " + e.getMessage());
        }
    }

}

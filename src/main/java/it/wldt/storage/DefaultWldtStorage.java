package it.wldt.storage;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.digital.event.DigitalWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetActionRequest;
import it.wldt.adapter.physical.PhysicalAssetDescriptionNotification;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.adapter.physical.PhysicalRelationshipInstanceVariation;
import it.wldt.adapter.physical.event.PhysicalAssetWldtEvent;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.exception.StorageException;

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

    // Instance variables for storing digital twin states, state changes, physical asset events, and digital twin events
    private Map<Long, DigitalTwinState> digitalTwinStateMap;
    private Map<Long, List<DigitalTwinStateChange>> stateChangeMap;
    private Map<Long, PhysicalAssetActionRequest> physicalActionRequestMap;
    private Map<Long, DigitalActionRequest> digitalActionRequestMap;
    private Map<Long, PhysicalAssetDescriptionNotification> newPhysicalAssetDescriptionNotificationMap;
    private Map<Long, PhysicalAssetDescriptionNotification> updatedPhysicalAssetDescriptionNotificationMap;
    private Map<Long, PhysicalAssetPropertyVariation> physicalAssetPropertyVariationMap;
    private Map<Long, PhysicalRelationshipInstanceVariation> physicalRelationshipInstanceCreatedMap;
    private Map<Long, PhysicalRelationshipInstanceVariation> physicalRelationshipInstanceDeletedMap;
    private Map<Long, LifeCycleState> lifeCycleStateMap;

    /**
     * Constructs a new DefaultWldtStorage object with empty storage containers.
     */
    public DefaultWldtStorage(String storageId) {
        super(storageId);
        this.digitalTwinStateMap = new HashMap<>();
        this.stateChangeMap = new HashMap<>();
        this.physicalActionRequestMap = new HashMap<>();
        this.digitalActionRequestMap = new HashMap<>();
        this.newPhysicalAssetDescriptionNotificationMap = new HashMap<>();
        this.updatedPhysicalAssetDescriptionNotificationMap = new HashMap<>();
        this.physicalAssetPropertyVariationMap = new HashMap<>();
        this.physicalRelationshipInstanceCreatedMap = new HashMap<>();
        this.physicalRelationshipInstanceDeletedMap = new HashMap<>();
        this.lifeCycleStateMap = new HashMap<>();
    }

    ////////////////////////////////////// Digital Twin State Management //////////////////////////////////////////////

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
        digitalTwinStateMap.put(digitalTwinState.getEvaluationInstant().toEpochMilli(), digitalTwinState);
        stateChangeMap.put(digitalTwinState.getEvaluationInstant().toEpochMilli(), digitalTwinStateChangeList);
    }

    /**
     * Returns the latest computed Digital Twin State of the target Digital Twin instance
     * @return the latest computed Digital Twin State
     */
    @Override
    public Optional<DigitalTwinState> getLastDigitalTwinState() {

        if (digitalTwinStateMap.isEmpty()) {
            return Optional.empty();
        }
        long lastTimestamp = Collections.max(digitalTwinStateMap.keySet());
        return Optional.ofNullable(digitalTwinStateMap.get(lastTimestamp));
    }

    /**
     * Retrieves a list of Digital Twin state changes associated with the given digital twin state that characterized
     * is computation and the transition from the previous state.
     *
     * @param digitalTwinState the digital twin state for which to retrieve the state changes
     * @return a list of digital twin state changes associated with the given digital twin state
     */
    @Override
    public List<DigitalTwinStateChange> getDigitalTwinStateChangeList(DigitalTwinState digitalTwinState) {
        if (digitalTwinState == null || !stateChangeMap.containsKey(digitalTwinState.getEvaluationInstant().toEpochMilli())) {
            return Collections.emptyList();
        }
        return stateChangeMap.get(digitalTwinState.getEvaluationInstant().toEpochMilli());
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
    public List<DigitalTwinState> getDigitalTwinStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        if (startTimestampMs > endTimestampMs) {
            throw new IllegalArgumentException("Start timestamp cannot be greater than end timestamp.");
        }
        List<DigitalTwinState> result = new ArrayList<>();
        for (Map.Entry<Long, DigitalTwinState> entry : digitalTwinStateMap.entrySet()) {
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
    public List<DigitalTwinState> getDigitalTwinStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalTwinStateMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }
        List<DigitalTwinState> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(digitalTwinStateMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(digitalTwinStateMap.get(timestamps.get(i)));
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
        lifeCycleStateMap.put(lifeCycleStateVariation.getTimestamp(), lifeCycleStateVariation.getLifeCycleState());

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
    public Map<Long, LifeCycleState> getLifeCycleStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        Map<Long, LifeCycleState> result = new HashMap<>();
        for (Map.Entry<Long, LifeCycleState> entry : lifeCycleStateMap.entrySet()) {
            long timestamp = entry.getKey();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.put(timestamp, entry.getValue());
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
    public Map<Long, LifeCycleState> getLifeCycleStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {

        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= lifeCycleStateMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        Map<Long, LifeCycleState> result = new HashMap<>();
        List<Long> timestamps = new ArrayList<>(lifeCycleStateMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.put(timestamps.get(i), lifeCycleStateMap.get(timestamps.get(i)));
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

        this.physicalActionRequestMap.put(physicalAssetActionRequest.getRequestTimestamp(), physicalAssetActionRequest);
    }

    /**
     * Get the number of Physical Asset Action Request
     * @return the number of Physical Asset Action Request
     */
    @Override
    public int getPhysicalAssetActionEventCount() {
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
    public List<PhysicalAssetActionRequest> getPhysicalAssetActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetActionRequest> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetActionRequest> entry : physicalActionRequestMap.entrySet()) {
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
    public List<PhysicalAssetActionRequest> getPhysicalAssetActionRequestInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalActionRequestMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetActionRequest> result = new ArrayList<>();
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
    public void saveDigitalActionEvent(DigitalActionRequest digitalActionRequest) throws StorageException {
        if(digitalActionRequest == null)
            throw new StorageException("Digital Action Request cannot be null.");

        this.digitalActionRequestMap.put(digitalActionRequest.getRequestTimestamp(), digitalActionRequest);
    }

    /**
     * Get the number of Digital Action Request Stored
     *
     * @return the number of Digital Action Request
     */
    @Override
    public int getDigitalActionEventCount() {
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
    public List<DigitalActionRequest> getDigitalActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<DigitalActionRequest> result = new ArrayList<>();
        for (Map.Entry<Long, DigitalActionRequest> entry : digitalActionRequestMap.entrySet()) {
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
    public List<DigitalActionRequest> getDigitalActionRequestInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalActionRequestMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<DigitalActionRequest> result = new ArrayList<>();
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

        this.newPhysicalAssetDescriptionNotificationMap.put(physicalAssetDescriptionNotification.getNotificationTimestamp(), physicalAssetDescriptionNotification);
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
    public List<PhysicalAssetDescriptionNotification> getNewPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetDescriptionNotification> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetDescriptionNotification> entry : newPhysicalAssetDescriptionNotificationMap.entrySet()) {
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
    public List<PhysicalAssetDescriptionNotification> getNewPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= newPhysicalAssetDescriptionNotificationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetDescriptionNotification> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(newPhysicalAssetDescriptionNotificationMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(newPhysicalAssetDescriptionNotificationMap.get(timestamps.get(i)));
        }
        return result;
    }

    /**
     * Save the updated Physical Asset Description
     *
     * @param physicalAssetDescriptionNotification
     * @return the number of Physical Asset Description Available
     */
    @Override
    public void saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException {
        if(physicalAssetDescriptionNotification == null)
            throw new StorageException("Physical Asset Description Notification cannot be null.");

        this.updatedPhysicalAssetDescriptionNotificationMap.put(physicalAssetDescriptionNotification.getNotificationTimestamp(), physicalAssetDescriptionNotification);
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
    public List<PhysicalAssetDescriptionNotification> getUpdatedPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetDescriptionNotification> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetDescriptionNotification> entry : updatedPhysicalAssetDescriptionNotificationMap.entrySet()) {
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
    public List<PhysicalAssetDescriptionNotification> getUpdatedPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= updatedPhysicalAssetDescriptionNotificationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetDescriptionNotification> result = new ArrayList<>();
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

        this.physicalAssetPropertyVariationMap.put(physicalAssetPropertyVariation.getTimestamp(), physicalAssetPropertyVariation);
    }

    /**
     * Get the number of Physical Asset Property Variation
     *
     * @return the number of Physical Asset Property Variation
     */
    @Override
    public int getPhysicalAssetPropertyVariationCount() {
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
    public List<PhysicalAssetPropertyVariation> getPhysicalAssetPropertyVariationInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalAssetPropertyVariation> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalAssetPropertyVariation> entry : physicalAssetPropertyVariationMap.entrySet()) {
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
    public List<PhysicalAssetPropertyVariation> getPhysicalAssetPropertyVariationInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalAssetPropertyVariationMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalAssetPropertyVariation> result = new ArrayList<>();
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
    public void savePhysicalAssetRelationshipInstanceCreatedEvent(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException {
        if(physicalRelationshipInstanceVariation == null)
            throw new StorageException("Physical Relationship Instance Variation cannot be null.");

        this.physicalRelationshipInstanceCreatedMap.put(physicalRelationshipInstanceVariation.getNotificationTimestamp(), physicalRelationshipInstanceVariation);
    }

    /**
     * Get the number of Physical Asset Relationship Instance Created Event
     *
     * @return the number of Physical Asset Relationship Instance Created Event
     */
    @Override
    public int getPhysicalAssetRelationshipInstanceCreatedEventCount() {
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
    public List<PhysicalRelationshipInstanceVariation> getPhysicalAssetRelationshipInstanceCreatedEventInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalRelationshipInstanceVariation> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalRelationshipInstanceVariation> entry : physicalRelationshipInstanceCreatedMap.entrySet()) {
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
    public List<PhysicalRelationshipInstanceVariation> getPhysicalAssetRelationshipInstanceCreatedEventInRange(int startIndex, int endIndex) throws IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalRelationshipInstanceCreatedMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalRelationshipInstanceVariation> result = new ArrayList<>();
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
    public void savePhysicalAssetRelationshipInstanceDeletedEvent(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException {
        if(physicalRelationshipInstanceVariation == null)
            throw new StorageException("Physical Relationship Instance Variation cannot be null.");

        this.physicalRelationshipInstanceDeletedMap.put(physicalRelationshipInstanceVariation.getNotificationTimestamp(), physicalRelationshipInstanceVariation);
    }

    /**
     * Get the number of Physical Asset Relationship Instance Updated Event
     *
     * @return the number of Physical Asset Relationship Instance Updated Event
     */
    @Override
    public int getPhysicalAssetRelationshipInstanceDeletedEventCount() {
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
    public List<PhysicalRelationshipInstanceVariation> getPhysicalAssetRelationshipInstanceDeletedEventInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        List<PhysicalRelationshipInstanceVariation> result = new ArrayList<>();
        for (Map.Entry<Long, PhysicalRelationshipInstanceVariation> entry : physicalRelationshipInstanceDeletedMap.entrySet()) {
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
    public List<PhysicalRelationshipInstanceVariation> getPhysicalAssetRelationshipInstanceDeletedEventInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalRelationshipInstanceDeletedMap.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }

        List<PhysicalRelationshipInstanceVariation> result = new ArrayList<>();
        List<Long> timestamps = new ArrayList<>(physicalRelationshipInstanceDeletedMap.keySet());
        for (int i = startIndex; i <= endIndex; i++) {
            result.add(physicalRelationshipInstanceDeletedMap.get(timestamps.get(i)));
        }
        return result;
    }

    @Override
    public void clear(){
        this.digitalTwinStateMap.clear();
        this.stateChangeMap.clear();
        this.physicalActionRequestMap.clear();
        this.digitalActionRequestMap.clear();
        this.newPhysicalAssetDescriptionNotificationMap.clear();
        this.updatedPhysicalAssetDescriptionNotificationMap.clear();
        this.physicalAssetPropertyVariationMap.clear();
        this.physicalRelationshipInstanceCreatedMap.clear();
        this.physicalRelationshipInstanceDeletedMap.clear();
        this.lifeCycleStateMap.clear();
    }

}

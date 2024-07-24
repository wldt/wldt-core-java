package it.wldt.storage;

import it.wldt.adapter.digital.event.DigitalWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetWldtEvent;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;

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
public class DefaultWldtStorage implements IWldtStorage{

    // Instance variables for storing digital twin states, state changes, physical asset events, and digital twin events
    private Map<Long, DigitalTwinState> digitalTwinStateMap;
    private Map<Long, List<DigitalTwinStateChange>> stateChangeMap;
    private List<PhysicalAssetWldtEvent<?>> physicalAssetEvents;
    private List<DigitalWldtEvent<?>> digitalTwinEvents;
    private Map<Long, LifeCycleState> lifeCycleStateMap;

    /**
     * Constructs a new DefaultWldtStorage object with empty storage containers.
     */
    public DefaultWldtStorage() {
        digitalTwinStateMap = new HashMap<>();
        stateChangeMap = new HashMap<>();
        physicalAssetEvents = new ArrayList<>();
        digitalTwinEvents = new ArrayList<>();
        lifeCycleStateMap = new HashMap<>();
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
    public void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws IllegalArgumentException {
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

    ////////////////////////////////////// Physical Asset Events Management //////////////////////////////////////////////

    /**
     * Saves a physical event generated from a physical adapter and associated with a physical counterpart of the
     * Digital Twin.
     *
     * @param physicalAssetWldtEvent the physical asset event to save
     * @throws IllegalArgumentException if the provided physical asset event is invalid or null
     */
    @Override
    public void savePhysicalAssetEvent(PhysicalAssetWldtEvent<?> physicalAssetWldtEvent) throws IllegalArgumentException {
        if (physicalAssetWldtEvent == null) {
            throw new IllegalArgumentException("Physical asset event cannot be null.");
        }
        physicalAssetEvents.add(physicalAssetWldtEvent);
    }

    /**
     * Returns the number of events generated events from Physical Assets associated to the Digital Twin
     * @return The number of received Physical Asset Events
     */
    @Override
    public int getPhysicalAssetEventsCount() {
        return physicalAssetEvents.size();
    }

    /**
     * Retrieves a list of Physical Assets Events within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of Physical Asset Events within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    @Override
    public List<PhysicalAssetWldtEvent<?>> getPhysicalAssetEventsInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        if (startTimestampMs > endTimestampMs) {
            throw new IllegalArgumentException("Start timestamp cannot be greater than end timestamp.");
        }
        List<PhysicalAssetWldtEvent<?>> result = new ArrayList<>();
        for (PhysicalAssetWldtEvent<?> event : physicalAssetEvents) {
            long timestamp = event.getCreationTimestamp();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Retrieves a list of Physical Assets Events within the specified range of indices.
     *
     * @param startIndex the index of the first Physical Asset Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Event to retrieve (inclusive)
     * @return a list of Physical Asset Events within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<PhysicalAssetWldtEvent<?>> getPhysicalAssetEventsInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= physicalAssetEvents.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }
        return physicalAssetEvents.subList(startIndex, endIndex + 1);
    }

    ////////////////////////////////////// Digital Twin Events Management //////////////////////////////////////////////

    /**
     * Saves a digital event generated from the Digital Twin and sent to Digital Adapters or Augmentation Functions
     *
     * @param digitalWldtEvent the digital twin event to save
     * @throws IllegalArgumentException if the provided digital twin events is invalid or null
     */
    @Override
    public void saveDigitalTwinEvent(DigitalWldtEvent<?> digitalWldtEvent) throws IllegalArgumentException {
        if (digitalWldtEvent == null) {
            throw new IllegalArgumentException("Digital twin event cannot be null.");
        }
        digitalTwinEvents.add(digitalWldtEvent);
    }

    /**
     * Returns the number of events generated Digital Twin events
     * @return The number of generated Digital Twin Events
     */
    @Override
    public int getDigitalTwinEventsCount() {
        return digitalTwinEvents.size();
    }

    /**
     * Retrieves a list of Digital Twin Events within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of Digital Twin objects representing the generated events within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    @Override
    public List<DigitalWldtEvent<?>> getDigitalTwinEventsInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException {
        if (startTimestampMs > endTimestampMs) {
            throw new IllegalArgumentException("Start timestamp cannot be greater than end timestamp.");
        }
        List<DigitalWldtEvent<?>> result = new ArrayList<>();
        for (DigitalWldtEvent<?> event : digitalTwinEvents) {
            long timestamp = event.getCreationTimestamp();
            if (timestamp >= startTimestampMs && timestamp <= endTimestampMs) {
                result.add(event);
            }
        }
        return result;
    }

    /**
     * Retrieves a list of Digital Twin Events within the specified range of indices.
     *
     * @param startIndex the index of the first Digital Twin Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Digital Twin Event to retrieve (inclusive)
     * @return a list of Digital Twin Events within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    @Override
    public List<DigitalWldtEvent<?>> getDigitalTwinEventsInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
            throw new IllegalArgumentException("Invalid index range.");
        }
        if (endIndex >= digitalTwinEvents.size()) {
            throw new IndexOutOfBoundsException("End index out of bounds.");
        }
        return digitalTwinEvents.subList(startIndex, endIndex + 1);
    }

    /**
     * Save the LifeCycleState of the Digital Twin
     *
     * @param lifeCycleState
     */
    @Override
    public void saveLifeCycleState(long timestamp, LifeCycleState lifeCycleState) {
        // Implement this method using the variable lifeCycleStateMap
        lifeCycleStateMap.put(timestamp, lifeCycleState);

    }

    /**
     * Get the number of LifeCycleState of the Digital Twin
     *
     * @return the number of LifeCycleState of the Digital Twin
     */
    @Override
    public int getLifeCycleStateCount() {
        return lifeCycleStateMap.size();
    }

    /**
     * Get the last LifeCycleState of the Digital Twin
     *
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

    @Override
    public void clear(){
        digitalTwinStateMap.clear();
        stateChangeMap.clear();
        physicalAssetEvents.clear();
        digitalTwinEvents.clear();
        lifeCycleStateMap.clear();
    }

    public Map<Long, DigitalTwinState> getDigitalTwinStateMap() {
        return digitalTwinStateMap;
    }

    public void setDigitalTwinStateMap(Map<Long, DigitalTwinState> digitalTwinStateMap) {
        this.digitalTwinStateMap = digitalTwinStateMap;
    }

    public Map<Long, List<DigitalTwinStateChange>> getStateChangeMap() {
        return stateChangeMap;
    }

    public void setStateChangeMap(Map<Long, List<DigitalTwinStateChange>> stateChangeMap) {
        this.stateChangeMap = stateChangeMap;
    }

    public List<PhysicalAssetWldtEvent<?>> getPhysicalAssetEvents() {
        return physicalAssetEvents;
    }

    public void setPhysicalAssetEvents(List<PhysicalAssetWldtEvent<?>> physicalAssetEvents) {
        this.physicalAssetEvents = physicalAssetEvents;
    }

    public List<DigitalWldtEvent<?>> getDigitalTwinEvents() {
        return digitalTwinEvents;
    }

    public void setDigitalTwinEvents(List<DigitalWldtEvent<?>> digitalTwinEvents) {
        this.digitalTwinEvents = digitalTwinEvents;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultWldtStorage{");
        sb.append("digitalTwinStateMap=").append(digitalTwinStateMap);
        sb.append(", stateChangeMap=").append(stateChangeMap);
        sb.append(", physicalAssetEvents=").append(physicalAssetEvents);
        sb.append(", digitalTwinEvents=").append(digitalTwinEvents);
        sb.append('}');
        return sb.toString();
    }
}

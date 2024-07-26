package it.wldt.storage;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 29/03/2024
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Defines an Interface allowing the Digital Twin developer to implement its internal storage system for the Digital Twin instance.
 * The interface defines methods for the management of:
 *      i) Digital Twin State storage and retrieval;
 *      ii) Digital Twin State Change List storage and retrieval;
 *      iii) Received Physical Events;
 *      iv) Generated Digital Events;
 */
public abstract class WldtStorage {

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

    // Id of the storage instance
    private String storageId = null;

    /**
     * Default Constructor
     */
    public WldtStorage(String storageId) {
        this.storageId = storageId;
    }

    /**
     * Constructor with the possibility to enable/disable the observation of all events
     * @param storageId the id of the storage instance
     * @param observeAll if true the storage manager will observe all events and receive callback to handle the storage
     */
    public WldtStorage(String storageId, boolean observeAll){
        this(storageId);
        this.observeStateEvents = true;
        this.observerPhysicalAssetEvents = true;
        this.observerPhysicalAssetActionEvents = true;
        this.observePhysicalAssetDescriptionEvents = true;
        this.observerDigitalActionEvents = true;
        this.observeLifeCycleEvents = true;
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
    public WldtStorage(String storageId,
                       boolean observeStateEvents,
                       boolean observerPhysicalAssetEvents,
                       boolean observerPhysicalAssetActionEvents,
                       boolean observePhysicalAssetDescriptionEvents,
                       boolean observerDigitalActionEvents,
                       boolean observeLifeCycleEvents){
        this(storageId);

        this.observeStateEvents = observeStateEvents;
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
        this.observerDigitalActionEvents = observerDigitalActionEvents;
        this.observeLifeCycleEvents = observeLifeCycleEvents;
    }

    public boolean isObserveStateEvents() {
        return observeStateEvents;
    }

    public void setObserveStateEvents(boolean observeStateEvents) {
        this.observeStateEvents = observeStateEvents;
    }

    public boolean isObserverPhysicalAssetEvents() {
        return observerPhysicalAssetEvents;
    }

    public void setObserverPhysicalAssetEvents(boolean observerPhysicalAssetEvents) {
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
    }

    public boolean isObserverPhysicalAssetActionEvents() {
        return observerPhysicalAssetActionEvents;
    }

    public void setObserverPhysicalAssetActionEvents(boolean observerPhysicalAssetActionEvents) {
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
    }

    public boolean isObservePhysicalAssetDescriptionEvents() {
        return observePhysicalAssetDescriptionEvents;
    }

    public void setObservePhysicalAssetDescriptionEvents(boolean observePhysicalAssetDescriptionEvents) {
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
    }

    public boolean isObserverDigitalActionEvents() {
        return observerDigitalActionEvents;
    }

    public void setObserverDigitalActionEvents(boolean observerDigitalActionEvents) {
        this.observerDigitalActionEvents = observerDigitalActionEvents;
    }

    public boolean isObserveLifeCycleEvents() {
        return observeLifeCycleEvents;
    }

    public void setObserveLifeCycleEvents(boolean observeLifeCycleEvents) {
        this.observeLifeCycleEvents = observeLifeCycleEvents;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    ////////////////////////////////////// Digital Twin State Management //////////////////////////////////////////////

    /**
     * Save a new computed instance of the DT State in the Storage together with the list of the changes with respect
     * to the previous state
     * @param digitalTwinState associated to the new computed Digital State to be stored
     * @param digitalTwinStateChangeList the list of state changes associated to the new Digital State that has to be stored
     * @throws IllegalArgumentException  if digitalTwinState is null
     */
    public abstract void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws IllegalArgumentException;

    /**
     * Returns the latest computed Digital Twin State of the target Digital Twin instance
     * @return the latest computed Digital Twin State
     */
    public abstract Optional<DigitalTwinState> getLastDigitalTwinState();

    /**
     * Retrieves a list of Digital Twin state changes associated with the given digital twin state that characterized
     * is computation and the transition from the previous state.
     *
     * @param digitalTwinState the digital twin state for which to retrieve the state changes
     * @return a list of digital twin state changes associated with the given digital twin state
     */
    public abstract List<DigitalTwinStateChange> getDigitalTwinStateChangeList(DigitalTwinState digitalTwinState);

    /**
     * Returns the number of computed and stored Digital Twin States
     * @return The number of computed Digital Twin States
     */
    public abstract int getDigitalTwinStateCount();

    /**
     * Retrieves a list of DigitalTwinState objects within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of DigitalTwinState objects representing the state of the digital twin within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    public abstract List<DigitalTwinState> getDigitalTwinStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException;

    /**
     * Retrieves a list of Digital Twin states within the specified range of indices.
     *
     * @param startIndex the index of the first digital twin state to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last digital twin state to retrieve (inclusive)
     * @return a list of digital twin states within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    public abstract List<DigitalTwinState> getDigitalTwinStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException;


    /**
     * Save the LifeCycleState of the Digital Twin
     * @param lifeCycleState
     */
    public abstract void saveLifeCycleState(long timestamp, LifeCycleState lifeCycleState);

    /**
     * Get the number of LifeCycleState of the Digital Twin
     * @return the number of LifeCycleState of the Digital Twin
     */
    public abstract int getLifeCycleStateCount();

    /**
     * Get the last LifeCycleState of the Digital Twin
     * @return the last LifeCycleState of the Digital Twin
     */
    public abstract Map<Long, LifeCycleState> getLifeCycleStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException;


    public abstract void savePhysicalAssetActionEvent(long timestamp, String actionKey, Object actionBody, Map<String, Object> metadata);

    public abstract int getPhysicalAssetActionEventCount();

    public abstract void saveDigitalActionEvent(long timestamp, String actionKey, Object actionBody, Map<String, Object> metadata);

    public abstract void savePhysicalAssetDescriptionAvailable(long timestamp, String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public abstract void savePhysicalAssetDescriptionUpdated(long timestamp, String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public abstract void savePhysicalAssetPropertyVariation(long creationTimestamp, String physicalPropertyId, Object body, Map<String, Object> metadata);

    public abstract void savePhysicalAssetRelationshipInstanceCreatedEvent(long creationTimestamp, PhysicalAssetRelationshipInstance<?> relationshipInstance);

    public abstract void savePhysicalAssetRelationshipInstanceDeletedEvent(long creationTimestamp, PhysicalAssetRelationshipInstance<?> relationshipInstance);

    /**
     * Clear all the store information in the WLDT Storage
     */
    public abstract void clear();

}

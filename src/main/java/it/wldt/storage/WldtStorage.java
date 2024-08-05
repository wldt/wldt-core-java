package it.wldt.storage;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.physical.*;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.StorageException;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.storage.model.digital.DigitalActionRequestRecord;
import it.wldt.storage.model.lifecycle.LifeCycleVariationRecord;
import it.wldt.storage.model.physical.*;
import it.wldt.storage.model.state.DigitalTwinStateEventNotificationRecord;
import it.wldt.storage.model.state.DigitalTwinStateRecord;

import java.util.List;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 29/03/2024
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Defines an Interface allowing the Digital Twin developer to implement its internal storage system for the Digital Twin instance.
 * The interface defines methods for the management of:
 *      - Digital Twin State storage and retrieval with the associated change list;
 *      - Generated State Digital Events;
 *      - Life Cycle State storage and retrieval;
 *      - Physical Asset Description storage and retrieval;
 *      - Physical Asset Property Variation storage and retrieval;
 *      - Physical Asset Relationship Instance storage and retrieval;
 *      - Digital Action Request storage and retrieval;
 *      - Physical Asset Action Request storage and retrieval;
 *      - Physical Asset Event Notification storage and retrieval;
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
    protected WldtStorage(String storageId) {
        this.storageId = storageId;
    }

    /**
     * Constructor with the possibility to enable/disable the observation of all events
     * @param storageId the id of the storage instance
     * @param observeAll if true the storage manager will observe all events and receive callback to handle the storage
     */
    protected WldtStorage(String storageId, boolean observeAll){
        this(storageId);
        this.observeStateEvents = observeAll;
        this.observerPhysicalAssetEvents = observeAll;
        this.observerPhysicalAssetActionEvents = observeAll;
        this.observePhysicalAssetDescriptionEvents = observeAll;
        this.observerDigitalActionEvents = observeAll;
        this.observeLifeCycleEvents = observeAll;
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
    protected WldtStorage(String storageId,
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

    /**
     * Save a new computed instance of the DT State in the Storage together with the list of the changes with respect
     * to the previous state
     * @param digitalTwinState associated to the new computed Digital State to be stored
     * @param digitalTwinStateChangeList the list of state changes associated to the new Digital State that has to be stored
     * @throws IllegalArgumentException  if digitalTwinState is null
     */
    public abstract void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws StorageException, IllegalArgumentException;

    /**
     * Returns the latest computed Digital Twin State of the target Digital Twin instance
     * @return the latest computed Digital Twin State
     */
    public abstract Optional<DigitalTwinStateRecord> getLastDigitalTwinState() throws StorageException;

    /**
     * Returns the number of computed and stored Digital Twin States
     * @return The number of computed Digital Twin States
     */
    public abstract int getDigitalTwinStateCount() throws StorageException;

    /**
     * Retrieves a list of DigitalTwinState objects within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of DigitalTwinState objects representing the state of the digital twin within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    public abstract List<DigitalTwinStateRecord> getDigitalTwinStateInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Retrieves a list of Digital Twin states within the specified range of indices.
     *
     * @param startIndex the index of the first digital twin state to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last digital twin state to retrieve (inclusive)
     * @return a list of digital twin states within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    public abstract List<DigitalTwinStateRecord> getDigitalTwinStateInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save the Digital Twin State Event Notification
     * @param digitalTwinStateEventNotification the Digital Twin State Event Notification to be saved
     */
    public abstract void saveDigitalTwinStateEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws StorageException;

    /**
     * Get the number of Digital Twin State Event Notification
     * @return the number of Digital Twin State Event Notification
     */
    public abstract int getDigitalTwinStateEventNotificationCount() throws StorageException;

    /**
     * Get the Digital Twin State Event Notification in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Digital Twin State Event Notification in the specified time range
     */
    public abstract List<DigitalTwinStateEventNotificationRecord> getDigitalTwinStateEventNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Digital Twin State Event Notification in the specified range of indices
     * @param startIndex the index of the first Digital Twin State Event Notification to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Digital Twin State Event Notification to retrieve (inclusive)
     * @return a list of Digital Twin State Event Notification within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<DigitalTwinStateEventNotificationRecord> getDigitalTwinStateEventNotificationInRange(int startIndex, int endIndex) throws StorageException, IllegalArgumentException;

    /**
     * Save the LifeCycleState of the Digital Twin
     * @param lifeCycleStateVariation the LifeCycleStateVariation to be saved
     */
    public abstract void saveLifeCycleState(LifeCycleStateVariation lifeCycleStateVariation) throws StorageException;

    /**
     * Get the last LifeCycleState of the Digital Twin
     * @return the last LifeCycleState of the Digital Twin
     */
    public abstract LifeCycleVariationRecord getLastLifeCycleState() throws StorageException;

    /**
     * Get the number of LifeCycleState of the Digital Twin
     * @return the number of LifeCycleState of the Digital Twin
     */
    public abstract int getLifeCycleStateCount() throws StorageException;

    /**
     * Get the last LifeCycleState of the Digital Twin
     * @return the last LifeCycleState of the Digital Twin
     */
    public abstract List<LifeCycleVariationRecord> getLifeCycleStateInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the LifeCycleState of the Digital Twin in the specified range of indices
     * @param startIndex the index of the first LifeCycleState to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last LifeCycleState to retrieve (inclusive)
     * @return a list of LifeCycleState within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<LifeCycleVariationRecord> getLifeCycleStateInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save the Physical Asset Event Notification
     * @param physicalAssetEventNotification the Physical Asset Event Notification to be saved
     */
    public abstract void savePhysicalAssetEventNotification(PhysicalAssetEventNotification physicalAssetEventNotification) throws StorageException;

    /**
     * Get the number of Physical Asset Event Notification
     * @return the number of Physical Asset Event Notification
     */
    public abstract int getPhysicalAssetEventNotificationCount() throws StorageException;

    /**
     * Get the Physical Asset Event Notification in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Physical Asset Event Notification in the specified time range
     */
    public abstract List<PhysicalAssetEventNotificationRecord> getPhysicalAssetEventNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Physical Asset Event Notification in the specified range of indices
     * @param startIndex the index of the first Physical Asset Event Notification to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Physical Asset Event Notification to retrieve (inclusive)
     * @return a list of Physical Asset Event Notification within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalAssetEventNotificationRecord> getPhysicalAssetEventNotificationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save Physical Asset Action Request
     * @param physicalAssetActionRequest the Physical Asset Action Request to be saved
     */
    public abstract void savePhysicalAssetActionRequest(PhysicalAssetActionRequest physicalAssetActionRequest) throws StorageException;

    /**
     * Get the number of Physical Asset Action Request
     * @return the number of Physical Asset Action Request
     */
    public abstract int getPhysicalAssetActionRequestCount() throws StorageException;

    /**
     * Get the Physical Asset Action Request in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Physical Asset Action Request in the specified time range
     */
    public abstract List<PhysicalAssetActionRequestRecord> getPhysicalAssetActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Physical Asset Action Request in the specified range of indices
     * @param startIndex the index of the first Physical Asset Action Request to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Physical Asset Action Request to retrieve (inclusive)
     * @return a list of Physical Asset Action Request within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalAssetActionRequestRecord> getPhysicalAssetActionRequestInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save a Digital Action Request
     * @param digitalActionRequest the Digital Action Request to be saved
     */
    public abstract void saveDigitalActionRequest(DigitalActionRequest digitalActionRequest) throws StorageException;

    /**
     * Get the number of Digital Action Request Stored
     * @return the number of Digital Action Request
     */
    public abstract int getDigitalActionRequestCount() throws StorageException;

    /**
     * Get the Digital Action Request in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Digital Action Request in the specified time range
     */
    public abstract List<DigitalActionRequestRecord> getDigitalActionRequestInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Digital Action Request in the specified range of indices
     * @param startIndex the index of the first Digital Action Request to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Digital Action Request to retrieve (inclusive)
     * @return a list of Digital Action Request within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<DigitalActionRequestRecord> getDigitalActionRequestInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save a new Physical Asset Description Available
     * @param physicalAssetDescriptionNotification the Physical Asset Description Notification to be saved
     */
    public abstract void saveNewPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException;

    /**
     * Get the number of New Physical Asset Description Notifications available
     * @return the number of Physical Asset Description Notifications available
     */
    public abstract int getNewPhysicalAssetDescriptionNotificationCount() throws StorageException;

    /**
     * Get the New Physical Asset Description Available in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of New Physical Asset Description Available in the specified time range
     */
    public abstract List<PhysicalAssetDescriptionNotificationRecord> getNewPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the New Physical Asset Description Available in the specified range of indices
     * @param startIndex the index of the first New Physical Asset Description to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last New Physical Asset Description to retrieve (inclusive)
     * @return a list of New Physical Asset Description within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalAssetDescriptionNotificationRecord> getNewPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save the updated Physical Asset Description
     * @return the number of Physical Asset Description Available
     */
    public abstract void saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException;

    /**
     * Get the number of Updated Physical Asset Description
     * @return the number of Updated Physical Asset Description
     */
    public abstract int getUpdatedPhysicalAssetDescriptionNotificationCount() throws StorageException;

    /**
     * Get the Updated Physical Asset Description in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Updated Physical Asset Description in the specified time range
     */
    public abstract List<PhysicalAssetDescriptionNotificationRecord> getUpdatedPhysicalAssetDescriptionNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Updated Physical Asset Description in the specified range of indices
     * @param startIndex the index of the first Updated Physical Asset Description to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Updated Physical Asset Description to retrieve (inclusive)
     * @return a list of Updated Physical Asset Description within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalAssetDescriptionNotificationRecord> getUpdatedPhysicalAssetDescriptionNotificationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save the Physical Asset Property Variation
     * @param physicalAssetPropertyVariation the Physical Asset Property Variation to be saved
     */
    public abstract void savePhysicalAssetPropertyVariation(PhysicalAssetPropertyVariation physicalAssetPropertyVariation) throws StorageException;

    /**
     * Get the number of Physical Asset Property Variation
     * @return the number of Physical Asset Property Variation
     */
    public abstract int getPhysicalAssetPropertyVariationCount() throws StorageException;

    /**
     * Get the Physical Asset Property Variation in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Physical Asset Property Variation in the specified time range
     */
    public abstract List<PhysicalAssetPropertyVariationRecord> getPhysicalAssetPropertyVariationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Physical Asset Property Variation in the specified range of indices
     * @param startIndex the index of the first Physical Asset Property Variation to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Physical Asset Property Variation to retrieve (inclusive)
     * @return a list of Physical Asset Property Variation within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalAssetPropertyVariationRecord> getPhysicalAssetPropertyVariationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Save the Physical Asset Relationship Instance Created Event
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    public abstract void savePhysicalAssetRelationshipInstanceCreatedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException;

    /**
     * Get the number of Physical Asset Relationship Instance Created Event
     * @return the number of Physical Asset Relationship Instance Created Event
     */
    public abstract int getPhysicalAssetRelationshipInstanceCreatedNotificationCount() throws StorageException;

    /**
     * Get the Physical Asset Relationship Instance Created Event in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Physical Asset Relationship Instance Created Event in the specified time range
     */
    public abstract List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceCreatedNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Physical Asset Relationship Instance Created Event in the specified range of indices
     * @param startIndex the index of the first Physical Asset Property Variation to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Physical Asset Property Variation to retrieve (inclusive)
     * @return a list of Physical Asset Relationship Instance Created Event within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceCreatedNotificationInRange(int startIndex, int endIndex) throws StorageException, IllegalArgumentException;

    /**
     * Save the Physical Asset Relationship Instance Updated Event
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    public abstract void savePhysicalAssetRelationshipInstanceDeletedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException;

    /**
     * Get the number of Physical Asset Relationship Instance Updated Event
     * @return the number of Physical Asset Relationship Instance Updated Event
     */
    public abstract int getPhysicalAssetRelationshipInstanceDeletedNotificationCount() throws StorageException;

    /**
     * Get the Physical Asset Relationship Instance Updated Event in the specified time range
     * @param startTimestampMs the start timestamp of the time range
     * @param endTimestampMs the end timestamp of the time range
     * @return the list of Physical Asset Relationship Instance Updated Event in the specified time range
     */
    public abstract List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceDeletedNotificationInTimeRange(long startTimestampMs, long endTimestampMs) throws StorageException, IllegalArgumentException;

    /**
     * Get the Physical Asset Relationship Instance Updated Event in the specified range of indices
     * @param startIndex the index of the first Physical Asset Relationship Instance Updated Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex the index of the last Physical Asset Relationship Instance Updated Event to retrieve (inclusive)
     * @return a list of Physical Asset Relationship Instance Updated Event within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException if startIndex is greater than endIndex
     */
    public abstract List<PhysicalRelationshipInstanceVariationRecord> getPhysicalAssetRelationshipInstanceDeletedNotificationInRange(int startIndex, int endIndex) throws StorageException, IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Initialize the WLDT Storage
     */
    protected abstract void init() throws StorageException;

    /**
     * Clear all the store information in the WLDT Storage
     */
    protected abstract void clear() throws StorageException;

}

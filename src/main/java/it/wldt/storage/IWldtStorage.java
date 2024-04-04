package it.wldt.storage;

import it.wldt.adapter.digital.event.DigitalWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetWldtEvent;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import java.util.List;
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
public interface IWldtStorage {

    ////////////////////////////////////// Digital Twin State Management //////////////////////////////////////////////

    /**
     * Save a new computed instance of the DT State in the Storage together with the list of the changes with respect
     * to the previous state
     * @param digitalTwinState associated to the new computed Digital State to be stored
     * @param digitalTwinStateChangeList the list of state changes associated to the new Digital State that has to be stored
     * @throws IllegalArgumentException  if digitalTwinState is null
     */
    public void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws IllegalArgumentException;

    /**
     * Returns the latest computed Digital Twin State of the target Digital Twin instance
     * @return the latest computed Digital Twin State
     */
    public Optional<DigitalTwinState> getLastDigitalTwinState();

    /**
     * Retrieves a list of Digital Twin state changes associated with the given digital twin state that characterized
     * is computation and the transition from the previous state.
     *
     * @param digitalTwinState the digital twin state for which to retrieve the state changes
     * @return a list of digital twin state changes associated with the given digital twin state
     */
    public List<DigitalTwinStateChange> getDigitalTwinStateChangeList(DigitalTwinState digitalTwinState);

    /**
     * Returns the number of computed and stored Digital Twin States
     * @return The number of computed Digital Twin States
     */
    public int getDigitalTwinStateCount();

    /**
     * Retrieves a list of DigitalTwinState objects within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of DigitalTwinState objects representing the state of the digital twin within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    public List<DigitalTwinState> getDigitalTwinStateInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException;

    /**
     * Retrieves a list of Digital Twin states within the specified range of indices.
     *
     * @param startIndex the index of the first digital twin state to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last digital twin state to retrieve (inclusive)
     * @return a list of digital twin states within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    public List<DigitalTwinState> getDigitalTwinStateInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException;

    ////////////////////////////////////// Physical Asset Event Management //////////////////////////////////////////////
    /**
     * Saves a physical event generated from a physical adapter and associated with a physical counterpart of the
     * Digital Twin.
     *
     * @param physicalAssetWldtEvent the physical asset event to save
     * @throws IllegalArgumentException if the provided physical asset event is invalid or null
     */
    public void savePhysicalAssetEvent(PhysicalAssetWldtEvent<?> physicalAssetWldtEvent) throws IllegalArgumentException;

    /**
     * Returns the number of events generated events from Physical Assets associated to the Digital Twin
     * @return The number of received Physical Asset Events
     */
    public int getPhysicalAssetEventsCount();

    /**
     * Retrieves a list of Physical Assets Events within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of Physical Asset Events within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    public List<PhysicalAssetWldtEvent<?>> getPhysicalAssetEventsInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException;

    /**
     * Retrieves a list of Physical Assets Events within the specified range of indices.
     *
     * @param startIndex the index of the first Physical Asset Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Physical Asset Event to retrieve (inclusive)
     * @return a list of Physical Asset Events within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    public List<PhysicalAssetWldtEvent<?>> getPhysicalAssetEventsInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException;

    ////////////////////////////////////// Digital Twin Events Management //////////////////////////////////////////////
    /**
     * Saves a digital event generated from the Digital Twin and sent to Digital Adapters or Augmentation Functions
     *
     * @param digitalWldtEvent the digital twin event to save
     * @throws IllegalArgumentException if the provided digital twin events is invalid or null
     */
    public void saveDigitalTwinEvent(DigitalWldtEvent<?> digitalWldtEvent) throws IllegalArgumentException;

    /**
     * Returns the number of events generated Digital Twin events
     * @return The number of generated Digital Twin Events
     */
    public int getDigitalTwinEventsCount();

    /**
     * Retrieves a list of Digital Twin Events within the specified time range.
     *
     * @param startTimestampMs The start timestamp (in milliseconds) of the time range.
     * @param endTimestampMs   The end timestamp (in milliseconds) of the time range.
     * @return A list of Digital Twin objects representing the generated events within the specified time range.
     * @throws IllegalArgumentException If the start timestamp is greater than the end timestamp.
     */
    public List<DigitalWldtEvent<?>> getDigitalTwinEventsInTimeRange(long startTimestampMs, long endTimestampMs) throws IllegalArgumentException;

    /**
     * Retrieves a list of Digital Twin Events within the specified range of indices.
     *
     * @param startIndex the index of the first Digital Twin Event to retrieve (inclusive). Starting index is 0.
     * @param endIndex   the index of the last Digital Twin Event to retrieve (inclusive)
     * @return a list of Digital Twin Events within the specified index range
     * @throws IndexOutOfBoundsException if the startIndex or endIndex is out of bounds
     * @throws IllegalArgumentException  if startIndex is greater than endIndex
     */
    public List<DigitalWldtEvent<?>> getDigitalTwinEventsInRange(int startIndex, int endIndex) throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Clear all the store information in the WLDT Storage
     */
    public void clear();
}

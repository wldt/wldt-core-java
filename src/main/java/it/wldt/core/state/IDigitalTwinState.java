package it.wldt.core.state;

import it.wldt.exception.*;

import java.util.List;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This Interface defines and structures the internal Digital Twin's State management in terms of:
 * - Properties
 * - Actions
 * - Events
 * - Relationships
 */
public interface IDigitalTwinState {

    /////////////// PROPERTY MANAGEMENT ////////////////////////////

    /**
     * Checks if a target Property Key is already available in the current Digital Twin's State
     * @param propertyKey
     * @return
     * @throws WldtDigitalTwinStatePropertyException
     */
    public boolean containsProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    /**
     * Retrieves if present the target DigitalTwinStateProperty by Key
     * @param propertyKey
     * @return
     * @throws WldtDigitalTwinStatePropertyException
     */
    public Optional<DigitalTwinStateProperty<?>> getProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException;

    /**
     * Loads the list of available Properties (described by the class DigitalTwinStateProperty) available on the Digital Twin's State
     * @return
     * @throws WldtDigitalTwinStatePropertyException
     */
    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException;

    /**
     * Allows the creation of a new Property on the Digital Twin's State through the class DigitalTwinStateProperty
     * @param dtStateProperty
     * @throws WldtDigitalTwinStatePropertyException
     * @throws WldtDigitalTwinStatePropertyConflictException
     * @throws WldtDigitalTwinStatePropertyBadRequestException
     */
    public void createProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException;

    /**
     * Retrieves if present the target DigitalTwinStateProperty by Key
     *
     * @param propertyKey
     * @return
     * @throws WldtDigitalTwinStatePropertyException
     * @throws WldtDigitalTwinStatePropertyBadRequestException
     * @throws WldtDigitalTwinStatePropertyNotFoundException
     */
    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    /**
     * Updates the target property using the DigitalTwinStateProperty and the associated Property Key field
     * @param dtStateProperty
     * @throws WldtDigitalTwinStatePropertyException
     * @throws WldtDigitalTwinStatePropertyBadRequestException
     * @throws WldtDigitalTwinStatePropertyNotFoundException
     */
    public void updateProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    /**
     * Deletes the target property identified by the specified key
     * @param propertyKey
     * @throws WldtDigitalTwinStatePropertyException
     * @throws WldtDigitalTwinStatePropertyBadRequestException
     * @throws WldtDigitalTwinStatePropertyNotFoundException
     */
    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException;

    /**
     * Defines the type associated to the target Event Message for Property Creation
     * @param propertyKey
     * @return
     */
    public String getPropertyCreatedWldtEventMessageType(String propertyKey);

    /**
     * Defines the type associated to the target Event Message for Property Update
     * @param propertyKey
     * @return
     */
    public String getPropertyUpdatedWldtEventMessageType(String propertyKey);

    /**
     * Defines the type associated to the target Event Message for Property Deletion
     * @param propertyKey
     * @return
     */
    public String getPropertyDeletedWldtEventMessageType(String propertyKey);

    //////////////////////////////////////////////////////////////

    /////////////// ACTION MANAGEMENT ////////////////////////////

    /**
     * Checks if a Digital Twin State Action with the specified key is correctly registered
     * @param actionKey
     * @return
     * @throws WldtDigitalTwinStateActionException
     */
    public boolean containsAction(String actionKey) throws WldtDigitalTwinStateActionException;

    /**
     * Loads the target DigitalTwinStateAction by key
     * @param actionKey
     * @return
     * @throws WldtDigitalTwinStateActionException
     */
    public Optional<DigitalTwinStateAction> getAction(String actionKey) throws WldtDigitalTwinStateActionException;

    /**
     * Gets the list of available Actions registered on the Digital Twin's State
     * @return
     * @throws WldtDigitalTwinStateActionException
     */
    public Optional<List<DigitalTwinStateAction>> getActionList() throws WldtDigitalTwinStateActionException;

    /**
     * Enables and registers the target Action described through an instance of the DigitalTwinStateAction class
     * @param digitalTwinStateAction
     * @throws WldtDigitalTwinStateActionException
     * @throws WldtDigitalTwinStateActionConflictException
     */
    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException;

    /**
     * Update the already registered target Action described through an instance of the DigitalTwinStateAction class
     * @param digitalTwinStateAction
     * @throws WldtDigitalTwinStateActionException
     * @throws WldtDigitalTwinStateActionNotFoundException
     */
    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

    /**
     * Disables and unregisters the target Action described through an instance of the DigitalTwinStateAction class
     * @param actionKey
     * @throws WldtDigitalTwinStateActionException
     * @throws WldtDigitalTwinStateActionNotFoundException
     */
    public void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException;

    //////////////////////////////////////////////////////////////

    /////////////// EVENT MANAGEMENT ////////////////////////////

    /**
     * Check if a Digital Twin State Event with the specified key is correctly registered
     * @param eventKey
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public boolean containsEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the description of a registered Digital Twin State Event according to its Key
     * @param eventKey
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public Optional<DigitalTwinStateEvent> getEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the list of existing and registered Digital Twin State Events
     * @return
     * @throws WldtDigitalTwinStateEventException
     */
    public Optional<List<DigitalTwinStateEvent>> getEventList() throws WldtDigitalTwinStateEventException;

    /**
     * Register a new Digital Twin State Event
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateEventException
     * @throws WldtDigitalTwinStateEventConflictException
     */
    public void registerEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException, WldtDigitalTwinStateEventConflictException;

    /**
     * Update the registration and signature of an existing Digital Twin State Event
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateEventException
     */
    public void updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException;

    /**
     * Un-register a Digital Twin State Event
     * @param eventKey
     * @throws WldtDigitalTwinStateEventException
     */
    public void unRegisterEvent(String eventKey) throws WldtDigitalTwinStateEventException;

    /**
     * Return the Event Type associated to Digital Twin State Event Notifications
     * @param eventKey
     * @return
     */
    public String getEventNotificationWldtEventMessageType(String eventKey);

    /**
     * Method to notify the occurrence of the target Digital Twin State Event
     * @param digitalTwinStateEventNotification
     */
    public void notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws WldtDigitalTwinStateEventNotificationException;

    //////////////////////////////////////////////////////////////

    /////////////// RELATIONSHIP MANAGEMENT ////////////////////////////

    /**
     * Checks if a Relationship Name is already available in the current Digital Twin's State
     * @param relationshipName
     * @return
     */
    boolean containsRelationship(String relationshipName);

    /**
     * Creates a new Relationships (described by the class DigitalTwinStateRelationship) in the Digital Twin's State
     * @param relationship
     * @throws WldtDigitalTwinStateRelationshipException
     */
    void createRelationship(DigitalTwinStateRelationship<?> relationship) throws WldtDigitalTwinStateRelationshipException;

    /**
     * Adds a new Relationship instance described through the class DigitalTwinStateRelationshipInstance and identified through its name
     * @param name
     * @param instance
     * @throws WldtDigitalTwinStateRelationshipException
     */
    void addRelationshipInstance(String name, DigitalTwinStateRelationshipInstance<?> instance) throws WldtDigitalTwinStateRelationshipException;

    /**
     * Loads the list of existing relationships on the Digital Twin's State through a list of DigitalTwinStateRelationship
     * @return
     */
    Optional<List<DigitalTwinStateRelationship<?>>> getRelationshipList();

    /**
     * Gets a target Relationship identified through its name and described through the class DigitalTwinStateRelationship
     * @param name
     * @return
     */
    Optional<DigitalTwinStateRelationship<?>> getRelationship(String name);

    /**
     * Deletes a target Relationship identified through its name
     * @param name
     * @throws WldtDigitalTwinStateRelationshipException
     */
    void deleteRelationship(String name) throws WldtDigitalTwinStateRelationshipException;

    /**
     * Deletes the target Relationship Instance using relationship name and instance Key
     * @param relationshipName
     * @param instanceKey
     * @throws WldtDigitalTwinStateRelationshipException
     */
    void deleteRelationshipInstance(String relationshipName, String instanceKey) throws WldtDigitalTwinStateRelationshipException;

    /**
     * Returns the Event Type associated to Digital Twin State Relationship Instance Creation Notifications
     * @param relationshipName
     * @return
     */
    String getRelationshipInstanceCreatedWldtEventMessageType(String relationshipName);

    /**
     * Returns the Event Type associated to Digital Twin State Relationship Instance Delition Notifications
     * @param relationshipName
     * @return
     */
    String getRelationshipInstanceDeletedWldtEventMessageType(String relationshipName);

}

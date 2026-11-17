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
package it.wldt.core.state;

import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.time.Instant;
import java.util.*;

/**
 * Represents the state of a digital twin, including properties, actions, events, and relationships.
 * Manages the lifecycle and operations on these components.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class DigitalTwinState {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinState.class);

    // Maps to store properties, actions, events, and relationships
    private Map<String, DigitalTwinStateProperty<?>> properties;
    private Map<String, DigitalTwinStateAction> actions;
    private Map<String, DigitalTwinStateEvent> events;
    private Map<String, DigitalTwinStateRelationship<?>> relationships;

    // Last invocation of each action and last notification of each event (by key). They are updated outside of the
    // state transactions and, for this reason, they are shared (not copied) between a state and the states derived
    // from it, so that an update done while a transaction is open is not lost on commit.
    private Map<String, Instant> lastActionInvocations;
    private Map<String, Instant> lastEventNotifications;

    // Timestamp representing the evaluation instant of the digital twin state
    private Instant evaluationInstant;

    /**
     * Default constructor initializes maps and sets the evaluation instant to the current timestamp.
     */
    public DigitalTwinState() {
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
        this.relationships = new HashMap<>();
        this.lastActionInvocations = new java.util.concurrent.ConcurrentHashMap<>();
        this.lastEventNotifications = new java.util.concurrent.ConcurrentHashMap<>();
        this.evaluationInstant = Instant.now();
    }

    /**
     * Parameterized constructor to set properties, actions, events, and relationships, and initialize the evaluation instant.
     *
     * @param properties      Map of properties
     * @param actions         Map of actions
     * @param events          Map of events
     * @param relationships   Map of relationships
     */
    public DigitalTwinState(Map<String, DigitalTwinStateProperty<?>> properties,
                            Map<String, DigitalTwinStateAction> actions,
                            Map<String, DigitalTwinStateEvent> events,
                            Map<String, DigitalTwinStateRelationship<?>> relationships) {
        this.properties = properties;
        this.actions = actions;
        this.events = events;
        this.relationships = relationships;
        this.lastActionInvocations = new java.util.concurrent.ConcurrentHashMap<>();
        this.lastEventNotifications = new java.util.concurrent.ConcurrentHashMap<>();
        this.evaluationInstant = Instant.now();
    }

    /**
     * Constructor to build a new DT State from a previous one.
     * The evaluationInstant will be assigned to now and values are copied
     *
     * @param sourceDigitalTwinState The Source DT State to copy
     */
    public DigitalTwinState(DigitalTwinState sourceDigitalTwinState) {
        this();
        this.properties.putAll(sourceDigitalTwinState.properties);
        this.actions.putAll(sourceDigitalTwinState.actions);
        this.events.putAll(sourceDigitalTwinState.events);
        this.relationships.putAll(sourceDigitalTwinState.relationships);
        // Shared on purpose, see the field comment
        this.lastActionInvocations = sourceDigitalTwinState.lastActionInvocations;
        this.lastEventNotifications = sourceDigitalTwinState.lastEventNotifications;
    }

    //////////////////////////// PROPERTY MANAGEMENT //////////////////////////////////////////////////////////

    /**
     * Checks if the digital twin state contains a property with the given key.
     *
     * @param propertyKey The key of the property to check.
     * @return True if the property is present, false otherwise.
     * @throws WldtDigitalTwinStatePropertyException If an exception occurs during the check.
     */
    public boolean containsProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException {
        try {

            if (this.properties == null || this.properties.isEmpty())
                return false;

            return this.properties.containsKey(propertyKey);

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves the property with the given key.
     *
     * @param propertyKey The key of the property to retrieve.
     * @return An Optional containing the property if found, empty otherwise.
     * @throws WldtDigitalTwinStatePropertyException If an exception occurs during the retrieval.
     */
    public Optional<DigitalTwinStateProperty<?>> getProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException {
        try {

            if (this.properties == null || this.properties.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(this.properties.get(propertyKey));

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves a list of all properties associated with the digital twin state.
     *
     * This method returns an Optional containing a List of DigitalTwinStateProperty objects if properties
     * are present. If the properties map is null or empty, it returns an empty Optional.
     *
     * @return An Optional containing a List of DigitalTwinStateProperty objects if properties are present,
     *         empty Optional otherwise.
     * @throws WldtDigitalTwinStatePropertyException If an exception occurs during the retrieval of properties.
     *         The exception wraps the original cause and provides a descriptive error message.
     */
    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException {

        try {

            if (this.properties == null || this.properties.isEmpty())
                return Optional.empty();

            return Optional.of(new ArrayList<DigitalTwinStateProperty<?>>(this.properties.values()));

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    protected void createProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DigitalTwinStateManager: Properties Map = Null !");

        if (dtStateProperty == null || dtStateProperty.getKey() == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: provided property: %s -> propertyKey and/or property = Null !", dtStateProperty));

        if (this.properties.containsKey(dtStateProperty.getKey()))
            throw new WldtDigitalTwinStatePropertyConflictException(String.format("DigitalTwinStateManager: property with Key: %s already existing ! Conflict !", dtStateProperty.getKey()));

        try {
            this.properties.put(dtStateProperty.getKey(), dtStateProperty);

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    
    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DigitalTwinStateManager: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DigitalTwinStateManager: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DigitalTwinStateManager: property with Key: %s not found !", propertyKey));

        //TODO Check the field exposed ? Is it really useful ?
        if (!this.properties.get(propertyKey).isReadable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: property with Key: %s not readable !", propertyKey));

        try{
            if (this.properties.get(propertyKey) != null)
                return Optional.of(this.properties.get(propertyKey));
            else
                return Optional.empty();
        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }

    }


    protected void updateProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DigitalTwinStateManager: Properties Map = Null !");

        if (dtStateProperty == null || dtStateProperty.getKey() == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: provided property: %s -> propertyKey and/or property = Null !", dtStateProperty));

        if (!this.properties.containsKey(dtStateProperty.getKey()))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DigitalTwinStateManager: property with Key: %s not found !", dtStateProperty.getKey()));

        if (!this.properties.get(dtStateProperty.getKey()).isWritable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: property with Key: %s not writable !", dtStateProperty.getKey()));

        try {
            this.properties.put(dtStateProperty.getKey(), dtStateProperty);
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    protected void updatePropertyValue(String propertyKey, Object propertyValue) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DigitalTwinStateManager: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: provided propertyKey: %s -> propertyKey and/or property = Null !", propertyKey));

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DigitalTwinStateManager: property with Key: %s not found !", propertyKey));

        if (!this.properties.get(propertyKey).isWritable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DigitalTwinStateManager: property with Key: %s not writable !", propertyKey));

        try {
            DigitalTwinStateProperty<?> currentProperty = this.properties.get(propertyKey);
            //Validate Value Type & Update Property Value
            if(currentProperty == null)
                throw new Exception(String.format("Wrong Type between Property Value Type: %s and the provided new value type: %s", currentProperty.getValue().getClass(), propertyValue.getClass()));
            else if(currentProperty.getValue().getClass().equals(propertyValue.getClass())) {
                currentProperty.setValueObject(propertyValue);
                this.properties.put(propertyKey, currentProperty);
            }
            else
                throw new Exception(String.format("Wrong Type between Property Value Type: %s and the provided new value type: %s", currentProperty.getValue().getClass(), propertyValue.getClass()));
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    protected void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DigitalTwinStateManager: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DigitalTwinStateManager: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DigitalTwinStateManager: property with Key: %s not found !", propertyKey));

        try{
            DigitalTwinStateProperty<?> originalValue = this.properties.get(propertyKey);
            this.properties.remove(propertyKey);
        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }


    //////////////////////////// ACTION MANAGEMENT //////////////////////////////////////////////////////////

    
    public boolean containsAction(String actionKey) throws WldtDigitalTwinStateActionException {
        try {
            return this.actions != null
                    && !this.actions.isEmpty()
                    && this.actions.containsKey(actionKey);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    
    public Optional<DigitalTwinStateAction> getAction(String actionKey) throws WldtDigitalTwinStateActionException {
        try {
            return this.containsAction(actionKey) ? Optional.of(this.actions.get(actionKey)) : Optional.empty();
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    
    public Optional<List<DigitalTwinStateAction>> getActionList() throws WldtDigitalTwinStateActionException {
        try{
            return this.actions == null
                    || this.actions.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(this.actions.values()));
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    
    protected void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: digitalTwinStateAction or its Key = Null !");

        if (this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionConflictException(String.format("DigitalTwinStateManager: action with Key: %s already existing ! Conflict !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
        } catch (Exception e) {
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }


    protected void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: digitalTwinStateAction or its Key = Null !");

        if (!this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DigitalTwinStateManager: Action with Key: %s not found !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }

    }


    protected void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: Action Map = Null !");

        if (actionKey == null)
            throw new WldtDigitalTwinStateActionException("DigitalTwinStateManager: digitalTwinStateAction Key = Null !");

        if (!this.actions.containsKey(actionKey))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DigitalTwinStateManager: Action with Key: %s not found !", actionKey));

        try {
            DigitalTwinStateAction originalValue = this.actions.get(actionKey);
            this.actions.remove(actionKey);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    //////////////////////////// EVENT MANAGEMENT //////////////////////////////////////////////////////////

    
    public boolean containsEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return false;

            return this.events.containsKey(eventKey);

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    
    public Optional<DigitalTwinStateEvent> getEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(this.events.get(eventKey));

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    
    public Optional<List<DigitalTwinStateEvent>> getEventList() throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return Optional.empty();

            return Optional.of(new ArrayList<DigitalTwinStateEvent>(this.events.values()));

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }


    protected void registerEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException, WldtDigitalTwinStateEventConflictException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DigitalTwinStateManager: Events Map = Null !");

        if (digitalTwinStateEvent == null || digitalTwinStateEvent.getKey() == null)
            throw new WldtDigitalTwinStateEventException(String.format("DigitalTwinStateManager: provided event: %s -> eventKey and/or event = Null !", digitalTwinStateEvent));

        if (this.events.containsKey(digitalTwinStateEvent.getKey()))
            throw new WldtDigitalTwinStateEventConflictException(String.format("DigitalTwinStateManager: event with Key: %s already existing ! Conflict !", digitalTwinStateEvent.getKey()));

        try {
            this.events.put(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);
        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }


    protected void updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DigitalTwinStateManager: Events Map = Null !");

        if (digitalTwinStateEvent == null || digitalTwinStateEvent.getKey() == null)
            throw new WldtDigitalTwinStateEventException(String.format("DigitalTwinStateManager: provided event: %s -> eventKey and/or property = Null !", digitalTwinStateEvent));

        if (!this.events.containsKey(digitalTwinStateEvent.getKey()))
            throw new WldtDigitalTwinStateEventException(String.format("DigitalTwinStateManager: event with Key: %s not found !", digitalTwinStateEvent.getKey()));

        try {
            this.events.put(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);
        }catch (Exception e){
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }


    protected void unRegisterEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DigitalTwinStateManager: Events Map = Null !");

        if (eventKey == null)
            throw new WldtDigitalTwinStateEventException("DigitalTwinStateManager: eventKey = Null !");

        if (!this.events.containsKey(eventKey))
            throw new WldtDigitalTwinStateEventException(String.format("DigitalTwinStateManager: event with Key: %s not found !", eventKey));

        try{

            DigitalTwinStateEvent originalValue = this.events.get(eventKey);
            this.events.remove(eventKey);
        }catch (Exception e){
            throw  new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    //////////////////////////// RELATIONSHIP MANAGEMENT //////////////////////////////////////////////////////////
    
    public boolean containsRelationship(String relationshipName) {
        return relationshipName != null && !relationshipName.isEmpty() && this.relationships.containsKey(relationshipName);
    }

    public boolean containsRelationshipInstance(String relationshipName, String instanceKey) {
        return instanceKey != null && !instanceKey.isEmpty() && this.relationships.containsKey(relationshipName) && this.relationships.get(relationshipName).containsInstance(instanceKey);
    }


    protected void createRelationship(DigitalTwinStateRelationship<?> relationship) throws WldtDigitalTwinStateRelationshipException {
        if(relationship == null || relationship.getName() == null || relationship.getName().isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinStateRelationship cannot be null or have empty or null name");
        this.relationships.put(relationship.getName(), relationship);
    }


    protected void addRelationshipInstance(String name, DigitalTwinStateRelationshipInstance<?> instance) throws WldtDigitalTwinStateRelationshipException {
        if(name == null || name.isEmpty() || instance == null)
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState provided relationship name or instance are null");
        if(containsRelationship(name)){
            this.relationships.get(name).addInstance(instance);
        } else throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error adding instance because specified relationship does not exist");
    }

    
    public Optional<List<DigitalTwinStateRelationship<?>>> getRelationshipList() {
        return this.relationships.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(this.relationships.values()));
    }

    
    public Optional<DigitalTwinStateRelationship<?>> getRelationship(String name) {
        return containsRelationship(name) ? Optional.of(this.relationships.get(name)) : Optional.empty();
    }


    protected void deleteRelationship(String name) throws WldtDigitalTwinStateRelationshipException {
        if(name == null || name.isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error deleting relationship because name is null or empty");
        Optional<DigitalTwinStateRelationship<?>> relationship = getRelationship(name);
        relationship.ifPresent(r -> {
            this.relationships.remove(name);
        });
    }


    protected void deleteRelationshipInstance(String relationshipName, String instanceKey) throws WldtDigitalTwinStateRelationshipException {
        if(relationshipName == null || relationshipName.isEmpty() || instanceKey == null || instanceKey.isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error deleting relationship instance because name or instanceKey are null or empty");
        getRelationship(relationshipName).ifPresent(relationship -> {
            if(relationship.containsInstance(instanceKey)) {
                DigitalTwinStateRelationshipInstance<?> instance = relationship.getInstance(instanceKey);
                relationship.removeInstance(instanceKey);
            }
        });
    }

    //////////////////////////// DESCRIPTION & VALUES VIEWS ////////////////////////////////////////////////////////

    /**
     * Records the instant of the last invocation of an action. It does not require a state transaction and does
     * not produce a state update.
     * @param actionKey the key of the invoked action
     * @param instant the instant of the invocation
     */
    protected void recordActionInvocation(String actionKey, Instant instant) {
        if (actionKey != null && instant != null)
            this.lastActionInvocations.put(actionKey, instant);
    }

    /**
     * Records the instant of the last notification of an event. It does not require a state transaction and does
     * not produce a state update.
     * @param eventKey the key of the notified event
     * @param instant the instant of the notification
     */
    protected void recordEventNotification(String eventKey, Instant instant) {
        if (eventKey != null && instant != null)
            this.lastEventNotifications.put(eventKey, instant);
    }

    /**
     * @return the instant of the last invocation of the action, or empty if it has never been invoked
     * @param actionKey the key of the action
     */
    public Optional<Instant> getLastActionInvocation(String actionKey) {
        return Optional.ofNullable(this.lastActionInvocations.get(actionKey));
    }

    /**
     * @return the instant of the last notification of the event, or empty if it has never been notified
     * @param eventKey the key of the event
     */
    public Optional<Instant> getLastEventNotification(String eventKey) {
        return Optional.ofNullable(this.lastEventNotifications.get(eventKey));
    }

    /**
     * Builds a partial state limited to the resources accepted by the filter. Each category (properties, actions,
     * events, relationships) is filtered independently: a null name list keeps the whole category, an empty list
     * removes it. The resources are shared with this state (not copied). The partial state keeps the evaluation
     * instant and the last action invocations / event notifications of this state.
     * @param filter the content filter (null = no filter)
     * @return this same instance if no filter is active, otherwise a new partial state
     */
    public DigitalTwinState filter(DigitalTwinStateFilter filter) {

        if (filter == null || !filter.isActive())
            return this;

        Map<String, DigitalTwinStateProperty<?>> filteredProperties = new HashMap<>();
        for (DigitalTwinStateProperty<?> p : safe(this.properties))
            if (filter.acceptsProperty(p.getKey()))
                filteredProperties.put(p.getKey(), p);

        Map<String, DigitalTwinStateAction> filteredActions = new HashMap<>();
        for (DigitalTwinStateAction a : safe(this.actions))
            if (filter.acceptsAction(a.getKey()))
                filteredActions.put(a.getKey(), a);

        Map<String, DigitalTwinStateEvent> filteredEvents = new HashMap<>();
        for (DigitalTwinStateEvent e : safe(this.events))
            if (filter.acceptsEvent(e.getKey()))
                filteredEvents.put(e.getKey(), e);

        Map<String, DigitalTwinStateRelationship<?>> filteredRelationships = new HashMap<>();
        for (DigitalTwinStateRelationship<?> r : safe(this.relationships))
            if (filter.acceptsRelationship(r.getName()))
                filteredRelationships.put(r.getName(), r);

        DigitalTwinState filtered = new DigitalTwinState(filteredProperties, filteredActions, filteredEvents, filteredRelationships);
        filtered.lastActionInvocations = this.lastActionInvocations;
        filtered.lastEventNotifications = this.lastEventNotifications;
        filtered.evaluationInstant = this.evaluationInstant;
        return filtered;
    }

    /**
     * Extracts the structure (no values) of the complete state.
     * @return the description of the state
     */
    public DigitalTwinStateDescription getDescription() {
        return getDescription(DigitalTwinStateFilter.all());
    }

    /**
     * Extracts the structure (no values) of the resources accepted by the filter.
     * @param filter the content filter (null = no filter)
     * @return the description of the state
     */
    public DigitalTwinStateDescription getDescription(DigitalTwinStateFilter filter) {

        DigitalTwinStateFilter f = filter != null ? filter : DigitalTwinStateFilter.all();

        List<DigitalTwinStateDescription.PropertyDescription> propertyDescriptions = new ArrayList<>();
        for (DigitalTwinStateProperty<?> p : safe(this.properties))
            if (f.acceptsProperty(p.getKey()))
                propertyDescriptions.add(new DigitalTwinStateDescription.PropertyDescription(
                        p.getKey(), p.getType(), p.getContentType(), p.isReadable(), p.isWritable(), p.isExposed()));

        List<DigitalTwinStateDescription.ActionDescription> actionDescriptions = new ArrayList<>();
        for (DigitalTwinStateAction a : safe(this.actions))
            if (f.acceptsAction(a.getKey()))
                actionDescriptions.add(new DigitalTwinStateDescription.ActionDescription(
                        a.getKey(), a.getType(), a.getContentType(), a.isExposed()));

        List<DigitalTwinStateDescription.EventDescription> eventDescriptions = new ArrayList<>();
        for (DigitalTwinStateEvent e : safe(this.events))
            if (f.acceptsEvent(e.getKey()))
                eventDescriptions.add(new DigitalTwinStateDescription.EventDescription(
                        e.getKey(), e.getType(), e.getContentType()));

        List<DigitalTwinStateDescription.RelationshipDescription> relationshipDescriptions = new ArrayList<>();
        for (DigitalTwinStateRelationship<?> r : safe(this.relationships))
            if (f.acceptsRelationship(r.getName()))
                relationshipDescriptions.add(new DigitalTwinStateDescription.RelationshipDescription(r.getName(), r.getType()));

        return new DigitalTwinStateDescription(this.evaluationInstant,
                propertyDescriptions, actionDescriptions, eventDescriptions, relationshipDescriptions);
    }

    /**
     * Extracts a snapshot of the values of the complete state.
     * @return the values of the state
     */
    public DigitalTwinStateValues getValues() {
        return getValues(DigitalTwinStateFilter.all());
    }

    /**
     * Extracts a snapshot of the values of the resources accepted by the filter: the properties and the
     * relationships instances, plus the last invocation of the accepted actions and the last notification of the
     * accepted events (only if they ever happened).
     * @param filter the content filter (null = no filter)
     * @return the values of the state
     */
    public DigitalTwinStateValues getValues(DigitalTwinStateFilter filter) {

        DigitalTwinStateFilter f = filter != null ? filter : DigitalTwinStateFilter.all();

        Map<String, Object> propertyValues = new HashMap<>();
        for (DigitalTwinStateProperty<?> p : safe(this.properties))
            if (f.acceptsProperty(p.getKey()))
                propertyValues.put(p.getKey(), p.getValue());

        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationshipInstances = new HashMap<>();
        for (DigitalTwinStateRelationship<?> r : safe(this.relationships))
            if (f.acceptsRelationship(r.getName()))
                relationshipInstances.put(r.getName(), r.getInstances());

        Map<String, Instant> actionInvocations = new HashMap<>();
        for (DigitalTwinStateAction a : safe(this.actions)) {
            Instant last = this.lastActionInvocations.get(a.getKey());
            if (last != null && f.acceptsAction(a.getKey()))
                actionInvocations.put(a.getKey(), last);
        }

        Map<String, Instant> eventNotifications = new HashMap<>();
        for (DigitalTwinStateEvent e : safe(this.events)) {
            Instant last = this.lastEventNotifications.get(e.getKey());
            if (last != null && f.acceptsEvent(e.getKey()))
                eventNotifications.put(e.getKey(), last);
        }

        return new DigitalTwinStateValues(this.evaluationInstant,
                propertyValues, relationshipInstances, actionInvocations, eventNotifications);
    }

    private static <T> Collection<T> safe(Map<String, T> map) {
        return map == null ? Collections.<T>emptyList() : new ArrayList<>(map.values());
    }

    public Instant getEvaluationInstant() {
        return evaluationInstant;
    }

    protected void setEvaluationInstant(Instant evaluationInstant) {
        this.evaluationInstant = evaluationInstant;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinState{");
        sb.append("properties=").append(properties);
        sb.append(", actions=").append(actions);
        sb.append(", events=").append(events);
        sb.append(", relationships=").append(relationships);
        sb.append(", evaluationInstant=").append(evaluationInstant);
        sb.append('}');
        return sb.toString();
    }
}

package it.wldt.core.state;

import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Default implementation of the DigitalTwinState as implementation of the reference Interface IDigitalTwinState.
 * Developers can use this class or customize the implemented behaviours through their implementation of the
 * target IDigitalTwinState interface.
 */
public class DefaultDigitalTwinState implements IDigitalTwinState {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDigitalTwinState.class);

    public static final String DT_STATE_PUBLISHER_ID = "dt-state-publisher";
    public static final String DT_STATE_SUBSCRIBER_ID = "dt-state-subscriber";

    private static final String DT_STATE_PROPERTY_BASE_TOPIC = "dt.state.property";
    private static final String DT_STATE_ACTION_BASE_TOPIC = "dt.state.action";
    private static final String DT_STATE_EVENT_BASE_TOPIC = "dt.state.event";
    private static final String DT_STATE_RELATIONSHIP_BASE_TOPIC = "dt.state.relationship";
    public static final String CREATED_STRING  = "created";
    public static final String UPDATED_STRING  = "updated";
    public static final String DELETED_STRING  = "deleted";
    public static final String ENABLED_STRING  = "enabled";
    public static final String DISABLED_STRING = "disabled";
    public static final String REGISTER_STRING  = "registered";
    public static final String UNREGISTER_STRING = "unregistered";
    public static final String NOTIFICATION_STRING = "notification";
    public static final String INSTANCE_STRING = "instance";

    public static final String DT_STATE_PROPERTY_CREATED = DT_STATE_PROPERTY_BASE_TOPIC + "." + CREATED_STRING;
    public static final String DT_STATE_PROPERTY_UPDATED = DT_STATE_PROPERTY_BASE_TOPIC + "." + UPDATED_STRING;
    public static final String DT_STATE_PROPERTY_DELETED = DT_STATE_PROPERTY_BASE_TOPIC + "." + DELETED_STRING;

    public static final String DT_STATE_ACTION_ENABLED = DT_STATE_ACTION_BASE_TOPIC + "." + ENABLED_STRING;
    public static final String DT_STATE_ACTION_UPDATED = DT_STATE_ACTION_BASE_TOPIC + "." + UPDATED_STRING;
    public static final String DT_STATE_ACTION_DISABLED = DT_STATE_ACTION_BASE_TOPIC + "." + DISABLED_STRING;

    public static final String DT_STATE_EVENT_REGISTERED = DT_STATE_EVENT_BASE_TOPIC + "." + REGISTER_STRING;
    public static final String DT_STATE_EVENT_REGISTRATION_UPDATED = DT_STATE_EVENT_BASE_TOPIC + "." + UPDATED_STRING;
    public static final String DT_STATE_EVENT_UNREGISTERED = DT_STATE_EVENT_BASE_TOPIC + "." + UNREGISTER_STRING;
    public static final String DT_STATE_EVENT_NOTIFICATION = DT_STATE_EVENT_BASE_TOPIC + "." + NOTIFICATION_STRING;

    public static final String DT_STATE_RELATIONSHIP_CREATED = DT_STATE_RELATIONSHIP_BASE_TOPIC + "." + CREATED_STRING;
    public static final String DT_STATE_RELATIONSHIP_DELETED = DT_STATE_RELATIONSHIP_BASE_TOPIC + "." + DELETED_STRING;

    public static final String DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY = "dt.state.property.metadata.key";
    public static final String DT_STATE_ACTION_METADATA_KEY_PROPERTY_KEY = "dt.state.action.metadata.key";
    public static final String DT_STATE_EVENT_METADATA_KEY_EVENT_KEY = "dt.state.event.metadata.key";
    public static final String DT_STATE_RELATIONSHIP_METADATA_KEY = "dt.state.relationship.metadata.key";

    private final Map<String, DigitalTwinStateProperty<?>> properties;

    private final Map<String, DigitalTwinStateAction> actions;

    private final Map<String, DigitalTwinStateEvent> events;

    private final Map<String, DigitalTwinStateRelationship<?>> relationships;

    public DefaultDigitalTwinState() {
        this.properties = new HashMap<>();
        this.actions = new HashMap<>();
        this.events = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    //////////////////////////// PROPERTY MANAGEMENT //////////////////////////////////////////////////////////

    @Override
    public boolean containsProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException {
        try {

            if (this.properties == null || this.properties.isEmpty())
                return false;

            return this.properties.containsKey(propertyKey);

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<DigitalTwinStateProperty<?>> getProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException {
        try {

            if (this.properties == null || this.properties.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(this.properties.get(propertyKey));

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<List<DigitalTwinStateProperty<?>>> getPropertyList() throws WldtDigitalTwinStatePropertyException {

        try {

            if (this.properties == null || this.properties.isEmpty())
                return Optional.empty();

            return Optional.of(new ArrayList<DigitalTwinStateProperty<?>>(this.properties.values()));

        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void createProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyBadRequestException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (dtStateProperty == null || dtStateProperty.getKey() == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: provided property: %s -> propertyKey and/or property = Null !", dtStateProperty));

        if (this.properties.containsKey(dtStateProperty.getKey()))
            throw new WldtDigitalTwinStatePropertyConflictException(String.format("DefaultDigitalTwinState: property with Key: %s already existing ! Conflict !", dtStateProperty.getKey()));

        try {
            this.properties.put(dtStateProperty.getKey(), dtStateProperty);

            notifyPropertyCreated(dtStateProperty.getKey(), dtStateProperty);
        } catch (Exception e) {
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<DigitalTwinStateProperty<?>> readProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        //TODO Check the field exposed ? Is it really useful ?
        if (!this.properties.get(propertyKey).isReadable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: property with Key: %s not readable !", propertyKey));

        try{
            if (this.properties.get(propertyKey) != null)
                return Optional.of(this.properties.get(propertyKey));
            else
                return Optional.empty();
        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }

    }

    @Override
    public void updateProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (dtStateProperty == null || dtStateProperty.getKey() == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: provided property: %s -> propertyKey and/or property = Null !", dtStateProperty));

        if (!this.properties.containsKey(dtStateProperty.getKey()))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", dtStateProperty.getKey()));

        if (!this.properties.get(dtStateProperty.getKey()).isWritable())
            throw new WldtDigitalTwinStatePropertyBadRequestException(String.format("DefaultDigitalTwinState: property with Key: %s not writable !", dtStateProperty.getKey()));

        try {
            this.properties.put(dtStateProperty.getKey(), dtStateProperty);
            notifyPropertyUpdated(dtStateProperty.getKey(), dtStateProperty);
        }catch (Exception e){
            throw new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    @Override
    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        if (this.properties == null)
            throw new WldtDigitalTwinStatePropertyException("DefaultDigitalTwinState: Properties Map = Null !");

        if (propertyKey == null)
            throw new WldtDigitalTwinStatePropertyBadRequestException("DefaultDigitalTwinState: propertyKey = Null !");

        if (!this.properties.containsKey(propertyKey))
            throw new WldtDigitalTwinStatePropertyNotFoundException(String.format("DefaultDigitalTwinState: property with Key: %s not found !", propertyKey));

        try{
            DigitalTwinStateProperty<?> originalValue = this.properties.get(propertyKey);
            this.properties.remove(propertyKey);

            notifyPropertyDeleted(propertyKey, originalValue);

        }catch (Exception e){
            throw  new WldtDigitalTwinStatePropertyException(e.getLocalizedMessage());
        }
    }

    private void notifyPropertyCreated(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {

            WldtEvent<DigitalTwinStateProperty<?>> wldtEvent = new WldtEvent<>(DT_STATE_PROPERTY_CREATED);
            wldtEvent.setBody(digitalTwinStateProperty);
            wldtEvent.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyUpdated(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {

            //Publish the event for state observer
            WldtEvent<DigitalTwinStateProperty<?>> eventStateMessage = new WldtEvent<>(DT_STATE_PROPERTY_UPDATED);
            eventStateMessage.setBody(digitalTwinStateProperty);
            eventStateMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

            //Publish the event for property observers
            WldtEvent<DigitalTwinStateProperty<?>> eventPropertyMessage = new WldtEvent<>(getPropertyUpdatedWldtEventMessageType(propertyKey));
            eventPropertyMessage.setBody(digitalTwinStateProperty);
            eventPropertyMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventPropertyMessage);

        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyUpdated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyPropertyDeleted(String propertyKey, DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        try {
            //Publish the event for state observer
            WldtEvent<DigitalTwinStateProperty<?>> eventStateMessage = new WldtEvent<>(DT_STATE_PROPERTY_DELETED);
            eventStateMessage.setBody(digitalTwinStateProperty);
            eventStateMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

            //Publish the event for property observers
            WldtEvent<DigitalTwinStateProperty<?>> eventPropertyMessage = new WldtEvent<>(getPropertyDeletedWldtEventMessageType(propertyKey));
            eventPropertyMessage.setBody(digitalTwinStateProperty);
            eventPropertyMessage.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, propertyKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventPropertyMessage);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    @Override
    public String getPropertyCreatedWldtEventMessageType(String propertyKey) {
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, CREATED_STRING);
    }

    @Override
    public String getPropertyUpdatedWldtEventMessageType(String propertyKey){
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, UPDATED_STRING);
    }

    @Override
    public String getPropertyDeletedWldtEventMessageType(String propertyKey) {
        return String.format("%s.%s.%s", DT_STATE_PROPERTY_BASE_TOPIC, propertyKey, DELETED_STRING);
    }


    //////////////////////////// ACTION MANAGEMENT //////////////////////////////////////////////////////////

    @Override
    public boolean containsAction(String actionKey) throws WldtDigitalTwinStateActionException {
        try {
            return this.actions != null && !this.actions.isEmpty() && this.actions.containsKey(actionKey);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<DigitalTwinStateAction> getAction(String actionKey) throws WldtDigitalTwinStateActionException {
        try {
            return this.containsAction(actionKey) ? Optional.of(this.actions.get(actionKey)) : Optional.empty();
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<List<DigitalTwinStateAction>> getActionList() throws WldtDigitalTwinStateActionException {
        try{
            return this.actions == null || this.actions.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(this.actions.values()));
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    @Override
    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionConflictException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction or its Key = Null !");

        if (this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionConflictException(String.format("DefaultDigitalTwinState: action with Key: %s already existing ! Conflict !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
            notifyActionEnabled(digitalTwinStateAction);
        } catch (Exception e) {
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    @Override
    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (digitalTwinStateAction == null || digitalTwinStateAction.getKey() == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction or its Key = Null !");

        if (!this.actions.containsKey(digitalTwinStateAction.getKey()))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DefaultDigitalTwinState: Action with Key: %s not found !", digitalTwinStateAction.getKey()));

        try {
            this.actions.put(digitalTwinStateAction.getKey(), digitalTwinStateAction);
            notifyActionUpdated(digitalTwinStateAction);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }

    }

    @Override
    public void disableAction(String actionKey) throws WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        if (this.actions == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: Action Map = Null !");

        if (actionKey == null)
            throw new WldtDigitalTwinStateActionException("DefaultDigitalTwinState: digitalTwinStateAction Key = Null !");

        if (!this.actions.containsKey(actionKey))
            throw new WldtDigitalTwinStateActionNotFoundException(String.format("DefaultDigitalTwinState: Action with Key: %s not found !", actionKey));

        try {
            DigitalTwinStateAction originalValue = this.actions.get(actionKey);
            this.actions.remove(actionKey);
            notifyActionDisabled(originalValue);
        }catch (Exception e){
            throw new WldtDigitalTwinStateActionException(e.getLocalizedMessage());
        }
    }

    private void notifyActionEnabled(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            WldtEvent<DigitalTwinStateAction> wldtEvent = new WldtEvent<>(DT_STATE_ACTION_ENABLED);
            wldtEvent.setBody(digitalTwinStateAction);
            wldtEvent.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyActionUpdated(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            WldtEvent<DigitalTwinStateAction> wldtEvent = new WldtEvent<>(DT_STATE_ACTION_UPDATED);
            wldtEvent.setBody(digitalTwinStateAction);
            wldtEvent.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyActionDisabled(DigitalTwinStateAction digitalTwinStateAction) {
        try {
            WldtEvent<DigitalTwinStateAction> wldtEvent = new WldtEvent<>(DT_STATE_ACTION_DISABLED);
            wldtEvent.setBody(digitalTwinStateAction);
            wldtEvent.putMetadata(DT_STATE_PROPERTY_METADATA_KEY_PROPERTY_KEY, digitalTwinStateAction.getKey());
            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);
        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    //////////////////////////// EVENT MANAGEMENT //////////////////////////////////////////////////////////

    @Override
    public boolean containsEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return false;

            return this.events.containsKey(eventKey);

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<DigitalTwinStateEvent> getEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return Optional.empty();

            return Optional.ofNullable(this.events.get(eventKey));

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public Optional<List<DigitalTwinStateEvent>> getEventList() throws WldtDigitalTwinStateEventException {

        try {

            if (this.events == null || this.events.isEmpty())
                return Optional.empty();

            return Optional.of(new ArrayList<DigitalTwinStateEvent>(this.events.values()));

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public void registerEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException, WldtDigitalTwinStateEventConflictException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DefaultDigitalTwinState: Events Map = Null !");

        if (digitalTwinStateEvent == null || digitalTwinStateEvent.getKey() == null)
            throw new WldtDigitalTwinStateEventException(String.format("DefaultDigitalTwinState: provided event: %s -> eventKey and/or event = Null !", digitalTwinStateEvent));

        if (this.events.containsKey(digitalTwinStateEvent.getKey()))
            throw new WldtDigitalTwinStateEventConflictException(String.format("DefaultDigitalTwinState: event with Key: %s already existing ! Conflict !", digitalTwinStateEvent.getKey()));

        try {
            this.events.put(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);

            notifyEventRegistered(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);

        } catch (Exception e) {
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public void updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DefaultDigitalTwinState: Events Map = Null !");

        if (digitalTwinStateEvent == null || digitalTwinStateEvent.getKey() == null)
            throw new WldtDigitalTwinStateEventException(String.format("DefaultDigitalTwinState: provided event: %s -> eventKey and/or property = Null !", digitalTwinStateEvent));

        if (!this.events.containsKey(digitalTwinStateEvent.getKey()))
            throw new WldtDigitalTwinStateEventException(String.format("DefaultDigitalTwinState: event with Key: %s not found !", digitalTwinStateEvent.getKey()));

        try {

            this.events.put(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);

            notifyEventRegistrationUpdated(digitalTwinStateEvent.getKey(), digitalTwinStateEvent);

        }catch (Exception e){
            throw new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public void unRegisterEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        if (this.events == null)
            throw new WldtDigitalTwinStateEventException("DefaultDigitalTwinState: Events Map = Null !");

        if (eventKey == null)
            throw new WldtDigitalTwinStateEventException("DefaultDigitalTwinState: eventKey = Null !");

        if (!this.events.containsKey(eventKey))
            throw new WldtDigitalTwinStateEventException(String.format("DefaultDigitalTwinState: event with Key: %s not found !", eventKey));

        try{

            DigitalTwinStateEvent originalValue = this.events.get(eventKey);
            this.events.remove(eventKey);
            notifyEventUnregistered(originalValue);

        }catch (Exception e){
            throw  new WldtDigitalTwinStateEventException(e.getLocalizedMessage());
        }
    }

    @Override
    public String getEventNotificationWldtEventMessageType(String eventKey) {
        return String.format("%s.%s.%s", DT_STATE_EVENT_BASE_TOPIC, NOTIFICATION_STRING, eventKey);
    }

    private void notifyEventRegistered(String eventKey, DigitalTwinStateEvent digitalTwinStateEvent) {

        try {

            WldtEvent<DigitalTwinStateEvent> wldtEvent = new WldtEvent<>(DT_STATE_EVENT_REGISTERED);
            wldtEvent.setBody(digitalTwinStateEvent);
            wldtEvent.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, eventKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyEventRegistered() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }

    }

    private void notifyEventRegistrationUpdated(String eventKey, DigitalTwinStateEvent digitalTwinStateEvent) {
        try {

            //Publish the event for state observer
            WldtEvent<DigitalTwinStateEvent> eventStateMessage = new WldtEvent<>(DT_STATE_EVENT_REGISTRATION_UPDATED);
            eventStateMessage.setBody(digitalTwinStateEvent);
            eventStateMessage.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, eventKey);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

        } catch (Exception e) {
            logger.error("notifyEventUpdated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyEventUnregistered(DigitalTwinStateEvent digitalTwinStateEvent) {

        try {
            //Publish the event for state observer
            WldtEvent<DigitalTwinStateEvent> eventStateMessage = new WldtEvent<>(DT_STATE_EVENT_UNREGISTERED);
            eventStateMessage.setBody(digitalTwinStateEvent);
            eventStateMessage.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, digitalTwinStateEvent.getKey());

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, eventStateMessage);

        } catch (Exception e) {
            logger.error("notifyStateListenersPropertyDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Method to notify the occurrence of the target Digital Twin State Event
     * @param digitalTwinStateEventNotification
     */
    @Override
    public void notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws WldtDigitalTwinStateEventNotificationException{

        try {
            //Publish the event for state observer
            WldtEvent<DigitalTwinStateEventNotification<?>> notificationEvent = new WldtEvent<>(getEventNotificationWldtEventMessageType(digitalTwinStateEventNotification.getDigitalEventKey()));
            notificationEvent.setBody(digitalTwinStateEventNotification);
            notificationEvent.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, digitalTwinStateEventNotification.getDigitalEventKey());

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, notificationEvent);

        } catch (Exception e) {
            logger.error("notifyDigitalTwinStateEvent() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
            throw new WldtDigitalTwinStateEventNotificationException(e.getLocalizedMessage());
        }

    }

    @Override
    public boolean containsRelationship(String relationshipName) {
        return relationshipName != null && !relationshipName.isEmpty() && this.relationships.containsKey(relationshipName);
    }

    @Override
    public void createRelationship(DigitalTwinStateRelationship<?> relationship) throws WldtDigitalTwinStateRelationshipException {
        if(relationship == null || relationship.getName() == null || relationship.getName().isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinStateRelationship cannot be null or have empty or null name");
        this.relationships.put(relationship.getName(), relationship);
        notifyRelationshipCreated(relationship.getName(), relationship);
    }

    @Override
    public void addRelationshipInstance(String name, DigitalTwinStateRelationshipInstance<?> instance) throws WldtDigitalTwinStateRelationshipException {
        if(name == null || name.isEmpty() || instance == null)
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState provided relationship name or instance are null");
        if(containsRelationship(name)){
            this.relationships.get(name).addInstance(instance);
            notifyRelationshipEstablished(name, instance);
        } else throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error adding instance because specified relationship does not exist");
    }

    @Override
    public Optional<List<DigitalTwinStateRelationship<?>>> getRelationshipList() {
        return this.relationships.isEmpty() ? Optional.empty() : Optional.of(new ArrayList<>(this.relationships.values()));
    }

    @Override
    public Optional<DigitalTwinStateRelationship<?>> getRelationship(String name) {
        return containsRelationship(name) ? Optional.of(this.relationships.get(name)) : Optional.empty();
    }

    @Override
    public void deleteRelationship(String name) throws WldtDigitalTwinStateRelationshipException {
        if(name == null || name.isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error deleting relationship because name is null or empty");
        Optional<DigitalTwinStateRelationship<?>> relationship = getRelationship(name);
        relationship.ifPresent(r -> {
            this.relationships.remove(name);
            notifyRelationshipDeleted(name, r);
        });
    }

    @Override
    public void deleteRelationshipInstance(String relationshipName, String instanceKey) throws WldtDigitalTwinStateRelationshipException {
        if(relationshipName == null || relationshipName.isEmpty() || instanceKey == null || instanceKey.isEmpty())
            throw new WldtDigitalTwinStateRelationshipException("DigitalTwinState: Error deleting relationship instance because name or instanceKey are null or empty");
        getRelationship(relationshipName).ifPresent(relationship -> {
            if(relationship.containsInstance(instanceKey)) {
                DigitalTwinStateRelationshipInstance<?> instance = relationship.getInstance(instanceKey);
                relationship.removeInstance(instanceKey);
                notifyRelationshipInstanceDeleted(relationshipName, instance);
            }
        });
    }

    @Override
    public String getRelationshipInstanceCreatedWldtEventMessageType(String relationshipName) {
        return String.format("%s.%s.%s.%s", DT_STATE_RELATIONSHIP_BASE_TOPIC, relationshipName, INSTANCE_STRING , CREATED_STRING);
    }

    @Override
    public String getRelationshipInstanceDeletedWldtEventMessageType(String relationshipName) {
        return String.format("%s.%s.%s.%s", DT_STATE_RELATIONSHIP_BASE_TOPIC, relationshipName, INSTANCE_STRING , DELETED_STRING);
    }

    private void notifyRelationshipCreated(String relationshipName, DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {
        try {

            WldtEvent<DigitalTwinStateRelationship<?>> wldtEvent = new WldtEvent<>(DT_STATE_RELATIONSHIP_CREATED);
            wldtEvent.setBody(digitalTwinStateRelationship);
            wldtEvent.putMetadata(DT_STATE_RELATIONSHIP_METADATA_KEY, relationshipName);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyRelationshipCreated() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyRelationshipEstablished(String relationshipName, DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance){
        try {

            WldtEvent<DigitalTwinStateRelationshipInstance<?>> wldtEvent = new WldtEvent<>(getRelationshipInstanceCreatedWldtEventMessageType(relationshipName));
            wldtEvent.setBody(digitalTwinStateRelationshipInstance);
            wldtEvent.putMetadata(DT_STATE_RELATIONSHIP_METADATA_KEY, relationshipName);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyRelationshipEstablished() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyRelationshipDeleted(String relationshipName, DigitalTwinStateRelationship<?> digitalTwinStateRelationship){
        try {

            WldtEvent<DigitalTwinStateRelationship<?>> wldtEvent = new WldtEvent<>(DT_STATE_RELATIONSHIP_DELETED);
            wldtEvent.setBody(digitalTwinStateRelationship);
            wldtEvent.putMetadata(DT_STATE_RELATIONSHIP_METADATA_KEY, relationshipName);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyRelationshipDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    private void notifyRelationshipInstanceDeleted(String relationshipName, DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance){
        try {
            WldtEvent<DigitalTwinStateRelationshipInstance<?>> wldtEvent = new WldtEvent<>(getRelationshipInstanceDeletedWldtEventMessageType(relationshipName));
            wldtEvent.setBody(digitalTwinStateRelationshipInstance);
            wldtEvent.putMetadata(DT_STATE_RELATIONSHIP_METADATA_KEY, relationshipName);

            WldtEventBus.getInstance().publishEvent(DT_STATE_PUBLISHER_ID, wldtEvent);

        } catch (Exception e) {
            logger.error("notifyRelationshipInstanceDeleted() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultDigitalTwinState{");
        sb.append("properties=").append(properties);
        sb.append(", actions=").append(actions);
        sb.append(", events=").append(events);
        sb.append(", relationships=").append(relationships);
        sb.append('}');
        return sb.toString();
    }
}

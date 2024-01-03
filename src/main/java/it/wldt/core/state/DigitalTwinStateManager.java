package it.wldt.core.state;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Default implementation of the DigitalTwinState as implementation of the reference Interface IDigitalTwinStateManager.
 * Developers can use this class or customize the implemented behaviours through their implementation of the
 * target IDigitalTwinStateManager interface.
 */
public class DigitalTwinStateManager {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinStateManager.class);

    public static final String DT_STATE_PUBLISHER_ID = "dt-state-publisher";

    private static final String DT_STATE_UPDATE_MESSAGE_TYPE = "dt.state.update";

    public static final String DT_STATE_UPDATE_METADATA_PREVIOUS_STATE = "dt.state.update.metadata.previous_state";

    public static final String DT_STATE_UPDATE_METADATA_CHANGE_LIST = "dt.state.update.metadata.change_list";

    private static final String DT_STATE_EVENT_BASE_TYPE = "dt.state.event";

    private static final String NOTIFICATION_STRING = "notification";

    private static final String DT_STATE_EVENT_METADATA_KEY_EVENT_KEY = "dt.state.event.metadata.key";

    private DigitalTwinState digitalTwinState = null;

    private DigitalTwinStateTransaction digitalTwinStateTransaction = null;

    private boolean isEditing = false;

    private String digitalTwinId = null;

    private DigitalTwinStateManager(){

    }

    public DigitalTwinStateManager(String digitalTwinId) throws WldtDigitalTwinStateException {

        if(digitalTwinId == null)
            throw new WldtDigitalTwinStateException("Error ! Impossible to create a WldtWorker with a NULL Digital Twin Id !");

        this.digitalTwinId = digitalTwinId;
        this.digitalTwinState = new DigitalTwinState();
    }

    /**
     * Start Digital Twin State Transaction to handle DT changes keeping track of variations.
     * Changes will be added to the transaction and applied only after calling the method commit.
     */
    public void startStateTransaction(){
        logger.info("Starting Digital Twin State edit ...");
        this.isEditing = true;
        this.digitalTwinStateTransaction = new DigitalTwinStateTransaction(digitalTwinState);
    }

    /**
     * Rollback an already started transaction to the Digital Twin State that we had at the beginning of the
     * transaction. All required changes will be removed and the state will be recovered to the previous one.
     * After that the Transaction status is still in edit mode and new changes can be added and then commited.
     * @throws WldtDigitalTwinStateException
     */
    public void rollbackTransaction() throws WldtDigitalTwinStateException {

        if(!this.isEditing)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        this.digitalTwinStateTransaction.clean();
    }

    /**
     * Apply added changes to the Digital Twin State, update the DT'State accordingly and then notify Digital Adapters
     * about the state's variation
     * @throws WldtDigitalTwinStateException
     */
    public void commitStateTransaction() throws WldtDigitalTwinStateException {

        if(digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to commit Digital Twin State Transaction without properly starting it ! Call start() and then commit() to apply changes");

        logger.info("Committing Digital Twin State ({}) Changes ...", this.digitalTwinStateTransaction);

        //Apply Digital Twin State Changes
        this.digitalTwinStateTransaction.handleStateChanges();

        //Update Digital Twin State with the result of the transaction
        this.digitalTwinState = this.digitalTwinStateTransaction.getEndDigitalTwinState();

        logger.info("Digital Twin State Transaction Committed ! New Digital Twin State: {}", this.digitalTwinState);

        //Notify New Digital Twin State
        notifyDigitalTwinStateUpdate();

        //Reset Transaction Status & Flag
        this.isEditing = false;
        this.digitalTwinStateTransaction = null;

    }

    /**
     * Notify a successful update of the Digital Twin State providing the resulting transaction, together with
     * the previous State, the new one and the list of applied changes
     */
    private void notifyDigitalTwinStateUpdate() throws WldtDigitalTwinStateException {
        try {

            //If the State Transaction is null
            if(this.digitalTwinStateTransaction == null)
                throw new WldtDigitalTwinStateException("Error DigitalTwinStateTransaction = null ! Nothing to notify");

            //If the State Transaction is correct
            if(this.digitalTwinStateTransaction.isCommitted()
                    && this.digitalTwinStateTransaction.getStartDigitalTwinState() != null
                    && this.digitalTwinStateTransaction.getEndDigitalTwinState() != null){

                //Create the new notification event
                WldtEvent<DigitalTwinState> wldtEvent = new WldtEvent<>(getStatusUpdatesWldtEventMessageType());

                //Add the new DT State in the event body
                wldtEvent.setBody(this.digitalTwinStateTransaction.getEndDigitalTwinState());

                //Add as event metadata both the original DT State before the transaction and the list of applied changes
                wldtEvent.putMetadata(DT_STATE_UPDATE_METADATA_PREVIOUS_STATE, this.digitalTwinStateTransaction.getStartDigitalTwinState());
                wldtEvent.putMetadata(DT_STATE_UPDATE_METADATA_CHANGE_LIST, this.digitalTwinStateTransaction.getDigitalTwinStateChangeList());

                //Publish the event on the WLDT event bus
                WldtEventBus.getInstance().publishEvent(this.digitalTwinId, DT_STATE_PUBLISHER_ID, wldtEvent);

            } else
                throw new WldtDigitalTwinStateException("Invalid DigitalTwinStateTransaction ! Missing commit or null starting or final state");

        } catch (Exception e) {
            throw new WldtDigitalTwinStateException(String.format("Error notifying DT State Update ! Error: %s", e.getLocalizedMessage()));
        }
    }

    /**
     * Returns the WLDT Event Type to subscribe in order to receive notification about state update
     * @return the WLDT Event Type to subscribe in order to receive notification about state update
     */
    public static String getStatusUpdatesWldtEventMessageType() {
        return String.format("%s", DT_STATE_UPDATE_MESSAGE_TYPE);
    }

    /**
     * Method to notify the occurrence of the target Digital Twin State Event
     * @param digitalTwinStateEventNotification
     */
    public void notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws WldtDigitalTwinStateEventNotificationException{

        try {
            //Publish the event for state observer
            WldtEvent<DigitalTwinStateEventNotification<?>> notificationEvent = new WldtEvent<>(getEventNotificationWldtEventMessageType(digitalTwinStateEventNotification.getDigitalEventKey()));
            notificationEvent.setBody(digitalTwinStateEventNotification);
            notificationEvent.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, digitalTwinStateEventNotification.getDigitalEventKey());

            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, DT_STATE_PUBLISHER_ID, notificationEvent);

        } catch (Exception e) {
            logger.error("notifyDigitalTwinStateEvent() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
            throw new WldtDigitalTwinStateEventNotificationException(e.getLocalizedMessage());
        }

    }

    /**
     * Returns the WLDT Event Type to subscribe in order to receive notification about an event generated by the DT
     * @return the WLDT Event Type to subscribe in order to receive notification about an event generated by the DT
     */
    public static String getEventNotificationWldtEventMessageType(String eventKey) {
        return String.format("%s.%s.%s", DT_STATE_EVENT_BASE_TYPE, NOTIFICATION_STRING, eventKey);
    }

    /**
     * Manage a new DT State Transaction request associated to the CREATION of a new PROPERTY.
     * The change will be applied after calling the commit() method.
     * @param dtStateProperty
     * @throws WldtDigitalTwinStateException
     */
    public void createProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_ADD,
                    DigitalTwinStateChange.ResourceType.PROPERTY,
                    dtStateProperty));
        } catch (Exception e){
            String errorMsg = String.format("Exception creating new property ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the UPDATE of a PROPERTY.
     * The change will be applied after calling the commit() method.
     * @param dtStateProperty
     * @throws WldtDigitalTwinStateException
     */
    public void updateProperty(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE,
                    DigitalTwinStateChange.ResourceType.PROPERTY,
                    dtStateProperty));
        } catch (Exception e){
            String errorMsg = String.format("Exception updating property ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a DT State Transaction request associated to the UPDATE of a PROPERTY VALUE.
     * The Property class is used as parameter but only property's key and value will be used in the update procedure.
     * If it is required to update the structure a Property, use the method updateProperty().
     * The change will be applied after calling the commit() method.
     * @param dtStateProperty
     * @throws WldtDigitalTwinStateException
     */
    public void updatePropertyValue(DigitalTwinStateProperty<?> dtStateProperty) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                    DigitalTwinStateChange.ResourceType.PROPERTY_VALUE,
                    dtStateProperty));
        } catch (Exception e){
            String errorMsg = String.format("Exception updating property ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the DELETE of a PROPERTY.
     * The change will be applied after calling the commit() method.
     * @param propertyKey
     * @throws WldtDigitalTwinStateException
     */
    public void deleteProperty(String propertyKey) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{

            Optional<DigitalTwinStateProperty<?>> optionalDigitalTwinStateProperty = this.digitalTwinState.getProperty(propertyKey);

            if(optionalDigitalTwinStateProperty.isPresent())
                this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                        DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.PROPERTY,
                        optionalDigitalTwinStateProperty.get()));
            else
                throw new WldtDigitalTwinStateException(String.format("Property with Key:%s Not Found !", propertyKey));
        } catch (Exception e){
            String errorMsg = String.format("Exception deleting property ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the CREATION of an ACTION.
     * The change will be applied after calling the commit() method.
     * @param digitalTwinStateAction
     * @throws WldtDigitalTwinStateException
     */
    public void enableAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_ADD,
                    DigitalTwinStateChange.ResourceType.ACTION,
                    digitalTwinStateAction));
        } catch (Exception e){
            String errorMsg = String.format("Exception creating new action ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the UPDATE of an ACTION.
     * The change will be applied after calling the commit() method.
     * @param digitalTwinStateAction
     * @throws WldtDigitalTwinStateException
     */
    public void updateAction(DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE,
                    DigitalTwinStateChange.ResourceType.ACTION,
                    digitalTwinStateAction));
        } catch (Exception e){
            String errorMsg = String.format("Exception updating action ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the DELETE of an ACTION.
     * The change will be applied after calling the commit() method.
     * @param actionKey
     * @throws WldtDigitalTwinStateException
     */
    public void disableAction(String actionKey) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{

            Optional<DigitalTwinStateAction> optionalDigitalTwinStateAction = this.digitalTwinState.getAction(actionKey);

            if(optionalDigitalTwinStateAction.isPresent())
                this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                        DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.ACTION,
                        optionalDigitalTwinStateAction.get()));
            else
                throw new WldtDigitalTwinStateException(String.format("Action with Key:%s Not Found !", actionKey));

        } catch (Exception e){
            String errorMsg = String.format("Exception disabling action ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }


    /**
     * Manage a new DT State Transaction request associated to the CREATE of an EVENT.
     * The change will be applied after calling the commit() method.
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateException
     */
    public void registerEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_ADD,
                    DigitalTwinStateChange.ResourceType.EVENT,
                    digitalTwinStateEvent));
        } catch (Exception e){
            String errorMsg = String.format("Exception creating new event ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the UPDATE of an EVENT.
     * The change will be applied after calling the commit() method.
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateException
     */
    public void updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE,
                    DigitalTwinStateChange.ResourceType.EVENT,
                    digitalTwinStateEvent));
        } catch (Exception e){
            String errorMsg = String.format("Exception updating event ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the DELETE of an EVENT.
     * The change will be applied after calling the commit() method.
     * @param eventKey
     * @throws WldtDigitalTwinStateException
     */
    public void unRegisterEvent(String eventKey) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{

            Optional<DigitalTwinStateEvent> optionalDigitalTwinStateEvent = this.digitalTwinState.getEvent(eventKey);

            if(optionalDigitalTwinStateEvent.isPresent())
                this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                        DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.EVENT,
                        optionalDigitalTwinStateEvent.get()));
            else
                throw new WldtDigitalTwinStateException(String.format("Event with Key:%s Not Found !", eventKey));

        } catch (Exception e){
            String errorMsg = String.format("Exception deleting event ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the CREATE of a RELATIONSHIP.
     * The change will be applied after calling the commit() method.
     * @param relationship
     * @throws WldtDigitalTwinStateException
     */
    public void createRelationship(DigitalTwinStateRelationship<?> relationship) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_ADD,
                    DigitalTwinStateChange.ResourceType.RELATIONSHIP,
                    relationship));
        } catch (Exception e){
            String errorMsg = String.format("Exception creating new relationship ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the CREATE of a RELATIONSHIP INSTANCE.
     * The change will be applied after calling the commit() method.
     * @param instance
     * @throws WldtDigitalTwinStateException
     */
    public void addRelationshipInstance(DigitalTwinStateRelationshipInstance<?> instance)throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_ADD,
                    DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE,
                    instance));
        } catch (Exception e){
            String errorMsg = String.format("Exception creating new relationship instance ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the DELETE of a RELATIONSHIP.
     * The change will be applied after calling the commit() method.
     * @param name
     * @throws WldtDigitalTwinStateException
     */
    public void deleteRelationship(String name) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{

            Optional<DigitalTwinStateRelationship<?>> optionalDigitalTwinStateRelationship = this.digitalTwinState.getRelationship(name);

            if(optionalDigitalTwinStateRelationship.isPresent())
                this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                        DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.RELATIONSHIP,
                        optionalDigitalTwinStateRelationship.get()));
            else
                throw new WldtDigitalTwinStateException(String.format("Event with Name:%s Not Found !", name));

        } catch (Exception e){
            String errorMsg = String.format("Exception deleting relationship ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    /**
     * Manage a new DT State Transaction request associated to the DELETE of a RELATIONSHIP INSTANCE.
     * The change will be applied after calling the commit() method.
     *
     * @param relationshipName
     * @param instanceKey
     * @throws WldtDigitalTwinStateException
     */
    public void deleteRelationshipInstance(String relationshipName, String instanceKey) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to edit Digital Twin State without start a transaction ! Call start() and then commit() to apply changes");

        try{

            Optional<DigitalTwinStateRelationship<?>> optionalDigitalTwinStateRelationship = this.digitalTwinState.getRelationship(relationshipName);

            if(optionalDigitalTwinStateRelationship.isPresent()) {

                DigitalTwinStateRelationship<?> digitalTwinStateRelationship = optionalDigitalTwinStateRelationship.get();

                if(digitalTwinStateRelationship.getInstance(instanceKey) != null)
                    this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                            DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                            DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE,
                            digitalTwinStateRelationship.getInstance(instanceKey)));
                else
                    throw new WldtDigitalTwinStateException(String.format("Relationship Instance with Key:%s and Relationship Name:%s Not Found !", instanceKey, relationshipName));
            }
            else
                throw new WldtDigitalTwinStateException(String.format("Relationship with Name:%s Not Found !", relationshipName));

        } catch (Exception e){
            String errorMsg = String.format("Exception deleting relationship ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current Digital Twin State
     * @return
     */
    public DigitalTwinState getDigitalTwinState() {
        return digitalTwinState;
    }

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

}

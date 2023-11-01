package it.wldt.core.state;

import it.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 18/10/2023 - 16:02
 */
public class DigitalTwinStateTransaction {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinStateTransaction.class);

    /**
     * Current Digital Twin State before the beginning of the Transaction
     */
    private DigitalTwinState startDigitalTwinState = null;

    /**
     * New Digital Twin State with the applied changes at the end of the transaction
     */
    private DigitalTwinState endDigitalTwinState = null;

    private boolean isCommitted = false;

    /**
     * List of Digital Twin State Changes
     */
    private ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList = null;

    private DigitalTwinStateTransaction(){}

    /**
     * A new Digital Twin State Transaction is created starting from the current Digital Twin State
     * @param startDigitalTwinState
     */
    public DigitalTwinStateTransaction(DigitalTwinState startDigitalTwinState) {
        this.startDigitalTwinState = startDigitalTwinState;
        this.endDigitalTwinState = startDigitalTwinState;
        this.digitalTwinStateChangeList = new ArrayList<>();
    }

    /**
     * Reset the current transaction, cleaning the list of available changes and recovering the DT'State to the
     * original one passed in the constructor.
     */
    public void clean(){
        this.digitalTwinStateChangeList.clear();
        this.endDigitalTwinState = this.startDigitalTwinState;
    }

    /**
     * Add a new DT State Change request that will be applied within the method handleStateChanges().
     * @param digitalTwinStateChange
     * @throws WldtDigitalTwinStateException
     */
    public void addStateChange(DigitalTwinStateChange digitalTwinStateChange) throws WldtDigitalTwinStateException {

        if(isCommitted)
            throw new WldtDigitalTwinStateException("Digital Twin State Transaction already Committed !");

        if(digitalTwinStateChange == null)
            throw new WldtDigitalTwinStateException("Impossible to handleStateChange() -> TransactionDigitalTwinStateChange = null !");

        this.digitalTwinStateChangeList.add(digitalTwinStateChange);
    }

    /**
     * Apply all the added changes to compute the new Digital Twin State updating also its evaluation Instant.
     * @throws WldtDigitalTwinStateException
     */
    public void handleStateChanges() throws WldtDigitalTwinStateException {

        try {

            //Apply Digital Twin State Changes added to the Transaction
            for(DigitalTwinStateChange digitalTwinStateChange : this.digitalTwinStateChangeList){
                if(digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.PROPERTY) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateProperty)
                    handlePropertyChange(digitalTwinStateChange.getOperation(), (DigitalTwinStateProperty)digitalTwinStateChange.getResource());
                else if(digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.EVENT) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateEvent)
                    handleEventChange(digitalTwinStateChange.getOperation(), (DigitalTwinStateEvent)digitalTwinStateChange.getResource());
                else if(digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.ACTION) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateAction)
                    handleActionChange(digitalTwinStateChange.getOperation(), (DigitalTwinStateAction)digitalTwinStateChange.getResource());
                else if(digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.RELATIONSHIP) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateRelationship)
                    handleRelationshipChange(digitalTwinStateChange.getOperation(), (DigitalTwinStateRelationship)digitalTwinStateChange.getResource());
                else if(digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateRelationshipInstance)
                    handleRelationshipInstanceChange(digitalTwinStateChange.getOperation(), (DigitalTwinStateRelationshipInstance)digitalTwinStateChange.getResource());
            }

            //Update Digital Twin State evaluation Instant
            this.endDigitalTwinState.setEvaluationInstant(Instant.now());
        } catch (Exception e){
            String errorMsg = String.format("Exception handling Digital Twin State Transaction Changes ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
        }

    }

    /**
     * Handle a Property Change on the new Digital Twin State
     * @param operation
     * @param digitalTwinStateProperty
     * @throws WldtDigitalTwinStatePropertyException
     * @throws WldtDigitalTwinStatePropertyBadRequestException
     * @throws WldtDigitalTwinStatePropertyConflictException
     * @throws WldtDigitalTwinStatePropertyNotFoundException
     */
    private void handlePropertyChange(DigitalTwinStateChange.Operation operation, DigitalTwinStateProperty digitalTwinStateProperty) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {
        if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_ADD))
            this.endDigitalTwinState.createProperty(digitalTwinStateProperty);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE))
            this.endDigitalTwinState.deleteProperty(digitalTwinStateProperty.getKey());
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_UPDATE))
            this.endDigitalTwinState.updateProperty(digitalTwinStateProperty);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE))
            this.endDigitalTwinState.updatePropertyValue(digitalTwinStateProperty.getKey(), digitalTwinStateProperty.getValue());
        else
            throw new WldtDigitalTwinStatePropertyException(String.format("Wrong DigitalTwinStateChange Operation Provided: %s", operation));
    }

    /**
     * Handle an Event Change on the new Digital Twin State
     * @param operation
     * @param digitalTwinStateEvent
     * @throws WldtDigitalTwinStateEventConflictException
     * @throws WldtDigitalTwinStateEventException
     */
    private void handleEventChange(DigitalTwinStateChange.Operation operation, DigitalTwinStateEvent digitalTwinStateEvent) throws WldtDigitalTwinStateEventConflictException, WldtDigitalTwinStateEventException {
        if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_ADD))
            this.endDigitalTwinState.registerEvent(digitalTwinStateEvent);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE))
            this.endDigitalTwinState.unRegisterEvent(digitalTwinStateEvent.getKey());
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_UPDATE))
            this.endDigitalTwinState.updateRegisteredEvent(digitalTwinStateEvent);
        else
            throw new WldtDigitalTwinStateEventException(String.format("Wrong DigitalTwinStateChange Operation Provided: %s", operation));
    }

    /**
     * Handle an Action Change on the new Digital Twin State
     * @param operation
     * @param digitalTwinStateAction
     * @throws WldtDigitalTwinStateActionConflictException
     * @throws WldtDigitalTwinStateActionException
     * @throws WldtDigitalTwinStateActionNotFoundException
     */
    private void handleActionChange(DigitalTwinStateChange.Operation operation, DigitalTwinStateAction digitalTwinStateAction) throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {
        if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_ADD))
            this.endDigitalTwinState.enableAction(digitalTwinStateAction);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE))
            this.endDigitalTwinState.disableAction(digitalTwinStateAction.getKey());
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_UPDATE))
            this.endDigitalTwinState.updateAction(digitalTwinStateAction);
        else
            throw new WldtDigitalTwinStateActionException(String.format("Wrong DigitalTwinStateChange Operation Provided: %s", operation));
    }

    /**
     * Handle a Relationship Change on the new Digital Twin State
     * @param operation
     * @param digitalTwinStateRelationship
     * @throws WldtDigitalTwinStateRelationshipException
     */
    private void handleRelationshipChange(DigitalTwinStateChange.Operation operation, DigitalTwinStateRelationship digitalTwinStateRelationship) throws WldtDigitalTwinStateRelationshipException {
        if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_ADD))
            this.endDigitalTwinState.createRelationship(digitalTwinStateRelationship);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE))
            this.endDigitalTwinState.deleteRelationship(digitalTwinStateRelationship.getName());
        else
            throw new WldtDigitalTwinStateRelationshipException(String.format("Wrong DigitalTwinStateChange Operation Provided: %s", operation));
    }

    /**
     * Handle a Relationship Instance Change on the new Digital Twin State
     * @param operation
     * @param digitalTwinStateRelationshipInstance
     * @throws WldtDigitalTwinStateRelationshipException
     */
    private void handleRelationshipInstanceChange(DigitalTwinStateChange.Operation operation, DigitalTwinStateRelationshipInstance digitalTwinStateRelationshipInstance) throws WldtDigitalTwinStateRelationshipException {
        if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_ADD))
            this.endDigitalTwinState.addRelationshipInstance(digitalTwinStateRelationshipInstance.getRelationshipName(), digitalTwinStateRelationshipInstance);
        else if(operation.equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE))
            this.endDigitalTwinState.deleteRelationshipInstance(digitalTwinStateRelationshipInstance.getRelationshipName(), digitalTwinStateRelationshipInstance.getKey());
        else
            throw new WldtDigitalTwinStateRelationshipException(String.format("Wrong DigitalTwinStateChange Operation Provided: %s", operation));
    }

    /**
     * Returns the current Digital Twin State
     * @return
     */
    public DigitalTwinState getStartDigitalTwinState() {
        return startDigitalTwinState;
    }

    /**
     * Returns the new Digital Twin State.
     * Values will be updated only after the application of the listed changes.
     * @return
     */
    public DigitalTwinState getEndDigitalTwinState() {
        return endDigitalTwinState;
    }

    /**
     * Returns if the Transaction has been correctly commited and applied
     * @return
     */
    public boolean isCommitted() {
        return isCommitted;
    }

    public ArrayList<DigitalTwinStateChange> getDigitalTwinStateChangeList() {
        return new ArrayList<>(digitalTwinStateChangeList);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinStateTransaction{");
        sb.append("startDigitalTwinState=").append(startDigitalTwinState);
        sb.append(", endDigitalTwinState=").append(endDigitalTwinState);
        sb.append(", isCommitted=").append(isCommitted);
        sb.append(", digitalTwinStateChangeList=").append(digitalTwinStateChangeList);
        sb.append('}');
        return sb.toString();
    }
}

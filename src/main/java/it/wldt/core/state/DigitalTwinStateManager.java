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

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;

import java.time.Instant;
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

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateManager.class);

    public static final String DT_STATE_PUBLISHER_ID = "dt-state-publisher";

    /**
     * Metadata key of the description update event: the description (complete) that was valid before the update
     */
    public static final String DT_STATE_DESCRIPTION_UPDATE_METADATA_PREVIOUS_DESCRIPTION = "dt.state.description.update.metadata.previous_description";

    /**
     * Metadata key of the description update event: the list of the Digital Twin State Changes that alter the structure
     */
    public static final String DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST = "dt.state.description.update.metadata.change_list";

    /**
     * Metadata key of the values update event: the previous values of the resources touched by the update
     * (only the things that are changed, associated to the id of the description they belonged to)
     */
    public static final String DT_STATE_VALUES_UPDATE_METADATA_PREVIOUS_VALUES = "dt.state.values.update.metadata.previous_values";

    /**
     * Metadata key of the values update event: the list of the Digital Twin State Changes that alter the values
     */
    public static final String DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST = "dt.state.values.update.metadata.change_list";

    private static final String DT_STATE_EVENT_BASE_TYPE = "dt.state.event";

    private static final String NOTIFICATION_STRING = "notification";

    private static final String DT_STATE_EVENT_METADATA_KEY_EVENT_KEY = "dt.state.event.metadata.key";

    private DigitalTwinState digitalTwinState = null;

    private DigitalTwinStateTransaction digitalTwinStateTransaction = null;

    private boolean isEditing = false;

    /**
     * Creation timestamp (epoch milliseconds) of this manager, first part of every description id
     */
    private final long creationTimestamp;

    /**
     * Sequence number of the current description, second part of the description id. It starts from 0 and is
     * increased every time a committed transaction really changes the structure of the state
     */
    private long descriptionSeq = 0;

    /**
     * Event notifications requested during the current transaction: they are published at commit time
     */
    private final List<DigitalTwinStateEventNotification<?>> pendingEventNotifications = new ArrayList<>();

    private String digitalTwinId = null;

    /**
     * Reference to the Monitoring Interface in order to add metrics
     * related to computation of the Digital Twin State
     */
    private MonitoringInterface monitoringInterface;

    /**
     * State Manager Metrics Namespace
     */
    private String metricsNamespace;

    private DigitalTwinStateManager(){
        this.creationTimestamp = System.currentTimeMillis();
    }

    public DigitalTwinStateManager(String digitalTwinId) throws WldtDigitalTwinStateException {

        if(digitalTwinId == null)
            throw new WldtDigitalTwinStateException("Error ! Impossible to create a DigitalTwinWorker with a NULL Digital Twin Id !");

        this.digitalTwinId = digitalTwinId;
        this.creationTimestamp = System.currentTimeMillis();
        this.digitalTwinState = new DigitalTwinState();
        this.digitalTwinState.setDescriptionId(buildDescriptionId());
    }

    /**
     * Builds the id of the current description: {@code <creationTimestamp>_<numSeq>}
     */
    private String buildDescriptionId() {
        return String.format("%d_%d", this.creationTimestamp, this.descriptionSeq);
    }

    /**
     * Start Digital Twin State Transaction to handle DT changes keeping track of variations.
     * Changes will be added to the transaction and applied only after calling the method commit.
     */
    public void startStateTransaction(){
        logger.info("Starting Digital Twin State edit ...");
        this.isEditing = true;
        this.pendingEventNotifications.clear();
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
        this.pendingEventNotifications.clear();
    }

    /**
     * Apply added changes to the Digital Twin State, update the DT'State accordingly and then notify Digital Adapters
     * about the state's variation
     * @throws WldtDigitalTwinStateException
     */
    private void handleCommitStateTransaction() throws WldtDigitalTwinStateException {

        if(digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to commit Digital Twin State Transaction without properly starting it ! Call start() and then commit() to apply changes");

        logger.debug("Committing Digital Twin State: Start State: {} Changes: {}", this.digitalTwinStateTransaction.getStartDigitalTwinState(), this.digitalTwinStateTransaction.getDigitalTwinStateChangeList());

        // Snapshot of the description and of the values before the application of the changes: the resources of the
        // start state are shared with the end state (shallow copy), so they must be extracted before the changes
        DigitalTwinState startState = this.digitalTwinStateTransaction.getStartDigitalTwinState();
        DigitalTwinStateDescription previousDescription = startState.getDescription();
        DigitalTwinStateValues previousValues = startState.getValues();

        //Apply Digital Twin State Changes
        this.digitalTwinStateTransaction.handleStateChanges();

        DigitalTwinState endState = this.digitalTwinStateTransaction.getEndDigitalTwinState();
        List<DigitalTwinStateChange> changes = this.digitalTwinStateTransaction.getDigitalTwinStateChangeList();

        // Structural comparison: a new description (and a new id) exists only if the structure really changed
        boolean descriptionChanged = !previousDescription.isSameStructureAs(endState.getDescription());
        if(descriptionChanged) {
            this.descriptionSeq++;
            endState.setDescriptionId(buildDescriptionId());
        }

        //Update Digital Twin State with the result of the transaction
        this.digitalTwinState = endState;

        logger.info("Digital Twin State Transaction Committed ! New Digital Twin State: {}", this.digitalTwinState);

        //Notify the variations: description (if changed), values (if any value changed) and event notifications
        List<DigitalTwinStateEventNotification<?>> notifications = new ArrayList<>(this.pendingEventNotifications);
        this.pendingEventNotifications.clear();

        //Reset Transaction Status & Flag
        this.isEditing = false;
        this.digitalTwinStateTransaction = null;

        notifyDigitalTwinStateUpdate(endState, previousDescription, previousValues, changes, descriptionChanged, notifications);
    }

    /**
     * Apply added changes to the Digital Twin State, update the DT'State accordingly and then notify Digital Adapters
     * about the state's variation. This version of the method add also metrics computation if
     * the Monitoring Interface is correctly configured on the DT State Manager.
     * @throws WldtDigitalTwinStateException
     */
    public void commitStateTransaction() throws WldtDigitalTwinStateException {

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){
            handleCommitStateTransaction();
        }
        else {
            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                handleCommitStateTransaction();

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DT_STATE_COMPUTATION_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("commitStateTransaction Function Error: %s", e.getLocalizedMessage());
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DT_STATE_COMPUTATION_ERROR_COUNT);

                // Propagate the Exception after updating the error counter metric
                throw new WldtDigitalTwinStateException(errorMessage);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.DT_STATE_COMPUTATION_EXEC_TIME,
                        startMs);
            }
        }

    }

    /**
     * Publishes the events related to a committed transaction, in this order:
     * <ol>
     *     <li>the description update event, only if the structure of the state really changed: body = the complete
     *     new description, metadata = previous description and changes that alter the structure</li>
     *     <li>the values update event, only if the transaction contains at least one change of the values: body =
     *     only the values that are changed, metadata = the previous values of the same resources and the changes that
     *     alter the values</li>
     *     <li>the notifications of the Digital Twin State Events requested within the transaction</li>
     * </ol>
     */
    private void notifyDigitalTwinStateUpdate(DigitalTwinState endState,
                                              DigitalTwinStateDescription previousDescription,
                                              DigitalTwinStateValues previousValues,
                                              List<DigitalTwinStateChange> changes,
                                              boolean descriptionChanged,
                                              List<DigitalTwinStateEventNotification<?>> notifications) throws WldtDigitalTwinStateException {
        try {

            // DESCRIPTION UPDATE
            if(descriptionChanged) {

                List<DigitalTwinStateChange> descriptionChanges = DigitalTwinStateFilter.all().descriptionChanges(changes);

                WldtEvent<DigitalTwinStateDescription> descriptionEvent = new WldtEvent<>(getStateDescriptionUpdateWldtEventMessageType());
                descriptionEvent.setBody(endState.getDescription());
                descriptionEvent.putMetadata(DT_STATE_DESCRIPTION_UPDATE_METADATA_PREVIOUS_DESCRIPTION, previousDescription);
                descriptionEvent.putMetadata(DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(descriptionChanges));

                WldtEventBus.getInstance().publishEvent(this.digitalTwinId, DT_STATE_PUBLISHER_ID, descriptionEvent);
            }

            // VALUES UPDATE
            List<DigitalTwinStateChange> valueChanges = DigitalTwinStateFilter.all().valueChanges(changes);

            if(!valueChanges.isEmpty()) {

                DigitalTwinStateValues[] delta = buildValuesDelta(endState, previousValues, valueChanges);

                WldtEvent<DigitalTwinStateValues> valuesEvent = new WldtEvent<>(getStateValuesUpdateWldtEventMessageType());
                valuesEvent.setBody(delta[0]);
                valuesEvent.putMetadata(DT_STATE_VALUES_UPDATE_METADATA_PREVIOUS_VALUES, delta[1]);
                valuesEvent.putMetadata(DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(valueChanges));

                WldtEventBus.getInstance().publishEvent(this.digitalTwinId, DT_STATE_PUBLISHER_ID, valuesEvent);
            }

            // EVENT NOTIFICATIONS
            for(DigitalTwinStateEventNotification<?> notification : notifications)
                publishEventNotification(notification);

        } catch (Exception e) {
            throw new WldtDigitalTwinStateException(String.format("Error notifying DT State Update ! Error: %s", e.getLocalizedMessage()));
        }
    }

    /**
     * Builds the delta of the values touched by the changes of a transaction.
     * @return an array with two elements: the new values (only what is changed) and the previous values of the same
     * resources (only what existed before and is changed or removed)
     */
    private DigitalTwinStateValues[] buildValuesDelta(DigitalTwinState endState,
                                                      DigitalTwinStateValues previousValues,
                                                      List<DigitalTwinStateChange> valueChanges) throws Exception {

        // The removal of a property, relationship, action or event is not a values change (see
        // DigitalTwinStateFilter.isValueChange): it is carried only by the description update
        Set<String> touchedProperties = new LinkedHashSet<>();
        Set<String> touchedRelationships = new LinkedHashSet<>();
        Set<String> triggeredActions = new LinkedHashSet<>();
        Set<String> triggeredEvents = new LinkedHashSet<>();

        for(DigitalTwinStateChange change : valueChanges) {

            String name = DigitalTwinStateChangeFilter.resolveResourceName(change);

            switch (change.getResourceType()) {
                case PROPERTY:
                case PROPERTY_VALUE:
                    touchedProperties.add(name);
                    break;
                case RELATIONSHIP:
                case RELATIONSHIP_INSTANCE:
                    touchedRelationships.add(name);
                    break;
                case ACTION:
                    triggeredActions.add(name);
                    break;
                case EVENT:
                    triggeredEvents.add(name);
                    break;
                default:
                    break;
            }
        }

        Map<String, Object> newProperties = new HashMap<>();
        Map<String, Object> oldProperties = new HashMap<>();
        for(String key : touchedProperties) {
            Optional<DigitalTwinStateProperty<?>> property = endState.getProperty(key);
            if(!property.isPresent())
                continue;

            Object newValue = property.get().getValue();
            boolean existed = previousValues.getPropertyValues().containsKey(key);
            Object oldValue = previousValues.getPropertyValues().get(key);

            // A property whose value is unchanged is not part of the delta (it is still in the change list)
            if(!existed || !Objects.equals(oldValue, newValue)) {
                newProperties.put(key, newValue);
                if(existed)
                    oldProperties.put(key, oldValue);
            }
        }

        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> newRelationships = new HashMap<>();
        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> oldRelationships = new HashMap<>();
        for(String name : touchedRelationships) {
            Optional<DigitalTwinStateRelationship<?>> relationship = endState.getRelationship(name);
            if(!relationship.isPresent())
                continue;

            newRelationships.put(name, relationship.get().getInstances());
            if(previousValues.getRelationshipInstances().containsKey(name))
                oldRelationships.put(name, previousValues.getRelationshipInstances().get(name));
        }

        Map<String, Instant> newActions = new HashMap<>();
        Map<String, Instant> oldActions = new HashMap<>();
        for(String key : triggeredActions) {
            Optional<Instant> last = endState.getLastActionInvocation(key);
            if(last.isPresent())
                newActions.put(key, last.get());
            if(previousValues.getLastActionInvocations().containsKey(key))
                oldActions.put(key, previousValues.getLastActionInvocations().get(key));
        }

        Map<String, Instant> newEvents = new HashMap<>();
        Map<String, Instant> oldEvents = new HashMap<>();
        for(String key : triggeredEvents) {
            Optional<Instant> last = endState.getLastEventNotification(key);
            if(last.isPresent())
                newEvents.put(key, last.get());
            if(previousValues.getLastEventNotifications().containsKey(key))
                oldEvents.put(key, previousValues.getLastEventNotifications().get(key));
        }

        DigitalTwinStateValues newValues = new DigitalTwinStateValues(endState.getDescriptionId(), endState.getEvaluationInstant(),
                newProperties, newRelationships, newActions, newEvents);

        DigitalTwinStateValues oldValues = new DigitalTwinStateValues(previousValues.getDescriptionId(), previousValues.getEvaluationInstant(),
                oldProperties, oldRelationships, oldActions, oldEvents);

        return new DigitalTwinStateValues[] { newValues, oldValues };
    }

    /**
     * Returns the WLDT Event Type to subscribe in order to receive notification about the update of the
     * description (structure) of the Digital Twin State
     * @return the WLDT Event Type of the Digital Twin State Description updates
     */
    public static String getStateDescriptionUpdateWldtEventMessageType() {
        return WldtEventTypes.DT_STATE_DESCRIPTION_UPDATE_MESSAGE_EVENT_TYPE;
    }

    /**
     * Returns the WLDT Event Type to subscribe in order to receive notification about the update of the
     * values of the Digital Twin State
     * @return the WLDT Event Type of the Digital Twin State Values updates
     */
    public static String getStateValuesUpdateWldtEventMessageType() {
        return WldtEventTypes.DT_STATE_VALUES_UPDATE_MESSAGE_EVENT_TYPE;
    }

    /**
     * Reads the list of the changes that altered the structure from a description update event
     * @param event the event published on the description update topic
     * @return a new list with the changes carried by the event (empty if the event does not carry them)
     */
    public static List<DigitalTwinStateChange> getDescriptionChanges(WldtEvent<?> event) {
        return getChanges(event, DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST);
    }

    /**
     * Reads the list of the changes that altered the values from a values update event
     * @param event the event published on the values update topic
     * @return a new list with the changes carried by the event (empty if the event does not carry them)
     */
    public static List<DigitalTwinStateChange> getValuesChanges(WldtEvent<?> event) {
        return getChanges(event, DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST);
    }

    @SuppressWarnings("unchecked")
    private static List<DigitalTwinStateChange> getChanges(WldtEvent<?> event, String metadataKey) {
        if (event != null) {
            Optional<Object> metadata = event.getMetadata(metadataKey);
            if (metadata.isPresent() && metadata.get() instanceof List)
                return new ArrayList<>((List<DigitalTwinStateChange>) metadata.get());
        }
        return new ArrayList<>();
    }

    /**
     * Method to notify the occurrence of the target Digital Twin State Event. It must be called within a Digital
     * Twin State transaction (see {@link #startStateTransaction()}): the instant of the last notification of the event
     * becomes part of the state values (change with {@code OPERATION_UPDATE_VALUE}) and the notification is
     * published on the event bus when the transaction is committed, after the description and values updates.
     * If the transaction is rolled back the notification is discarded.
     * @param digitalTwinStateEventNotification the notification (its event key must be defined in the state)
     * @throws WldtDigitalTwinStateEventNotificationException if there is no open transaction or the event is not defined
     */
    public void notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws WldtDigitalTwinStateEventNotificationException{

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateEventNotificationException("Trying to notify a Digital Twin State Event without start a transaction ! Call startStateTransaction() and then commitStateTransaction()");

        try {

            if (digitalTwinStateEventNotification == null || digitalTwinStateEventNotification.getDigitalEventKey() == null)
                throw new WldtDigitalTwinStateEventNotificationException("Event notification or event key = null !");

            String eventKey = digitalTwinStateEventNotification.getDigitalEventKey();

            // The event must be defined in the current state or registered by the current transaction
            DigitalTwinStateEvent event = findTransactionEvent(eventKey);
            if(event == null)
                throw new WldtDigitalTwinStateEventNotificationException(String.format("Event with Key:%s Not Found !", eventKey));

            // Track the last notification of the event as a value change of the transaction
            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                    DigitalTwinStateChange.ResourceType.EVENT,
                    event,
                    Instant.now()));

            // The notification will be published at commit time
            this.pendingEventNotifications.add(digitalTwinStateEventNotification);

        } catch (WldtDigitalTwinStateEventNotificationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("notifyDigitalTwinStateEvent() -> Error Notifying State Listeners ! Error: {}", e.getLocalizedMessage());
            throw new WldtDigitalTwinStateEventNotificationException(e.getLocalizedMessage());
        }

    }

    /**
     * Publishes the notification of a Digital Twin State Event on the event bus
     */
    private void publishEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws Exception {
        WldtEvent<DigitalTwinStateEventNotification<?>> notificationEvent = new WldtEvent<>(getEventNotificationWldtEventMessageType(digitalTwinStateEventNotification.getDigitalEventKey()));
        notificationEvent.setBody(digitalTwinStateEventNotification);
        notificationEvent.putMetadata(DT_STATE_EVENT_METADATA_KEY_EVENT_KEY, digitalTwinStateEventNotification.getDigitalEventKey());

        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, DT_STATE_PUBLISHER_ID, notificationEvent);
    }

    /**
     * Finds an event defined in the current state or added by the open transaction
     */
    private DigitalTwinStateEvent findTransactionEvent(String eventKey) throws WldtDigitalTwinStateEventException {

        Optional<DigitalTwinStateEvent> stateEvent = this.digitalTwinState.getEvent(eventKey);
        if(stateEvent.isPresent())
            return stateEvent.get();

        for(DigitalTwinStateChange change : this.digitalTwinStateTransaction.getDigitalTwinStateChangeList())
            if(DigitalTwinStateChange.ResourceType.EVENT.equals(change.getResourceType())
                    && DigitalTwinStateChange.Operation.OPERATION_ADD.equals(change.getOperation())
                    && change.getResource() instanceof DigitalTwinStateEvent
                    && eventKey.equals(((DigitalTwinStateEvent) change.getResource()).getKey()))
                return (DigitalTwinStateEvent) change.getResource();

        return null;
    }

    /**
     * Finds an action defined in the current state or enabled by the open transaction
     */
    private DigitalTwinStateAction findTransactionAction(String actionKey) throws WldtDigitalTwinStateActionException {

        Optional<DigitalTwinStateAction> stateAction = this.digitalTwinState.getAction(actionKey);
        if(stateAction.isPresent())
            return stateAction.get();

        for(DigitalTwinStateChange change : this.digitalTwinStateTransaction.getDigitalTwinStateChangeList())
            if(DigitalTwinStateChange.ResourceType.ACTION.equals(change.getResourceType())
                    && DigitalTwinStateChange.Operation.OPERATION_ADD.equals(change.getOperation())
                    && change.getResource() instanceof DigitalTwinStateAction
                    && actionKey.equals(((DigitalTwinStateAction) change.getResource()).getKey()))
                return (DigitalTwinStateAction) change.getResource();

        return null;
    }

    /**
     * Notifies that the action with the given key has been executed now. It must be called within a Digital Twin
     * State transaction: it is NOT called automatically when a digital action is invoked, the developer must call it
     * (usually after publishing the corresponding physical event/action). The instant of the last invocation of the
     * action becomes part of the state values (change with {@code OPERATION_UPDATE_VALUE}).
     * @param actionKey the key of the executed action (it must be defined in the state)
     * @throws WldtDigitalTwinStateException if there is no open transaction or the action is not defined
     */
    public void notifyActionExecuted(String actionKey) throws WldtDigitalTwinStateException {
        notifyActionExecuted(actionKey, Instant.now());
    }

    /**
     * Notifies that the action with the given key has been executed at the given instant. See
     * {@link #notifyActionExecuted(String)}.
     * @param actionKey the key of the executed action (it must be defined in the state)
     * @param executionInstant the instant of the execution (null = now)
     * @throws WldtDigitalTwinStateException if there is no open transaction or the action is not defined
     */
    public void notifyActionExecuted(String actionKey, Instant executionInstant) throws WldtDigitalTwinStateException {

        if(!this.isEditing || this.digitalTwinStateTransaction == null)
            throw new WldtDigitalTwinStateException("Trying to notify an executed action without start a transaction ! Call startStateTransaction() and then commitStateTransaction()");

        if(actionKey == null)
            throw new WldtDigitalTwinStateException("Action key = null !");

        try {

            DigitalTwinStateAction action = findTransactionAction(actionKey);
            if(action == null)
                throw new WldtDigitalTwinStateException(String.format("Action with Key:%s Not Found !", actionKey));

            this.digitalTwinStateTransaction.addStateChange(new DigitalTwinStateChange(
                    DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                    DigitalTwinStateChange.ResourceType.ACTION,
                    action,
                    executionInstant != null ? executionInstant : Instant.now()));

        } catch (WldtDigitalTwinStateException e) {
            throw e;
        } catch (Exception e) {
            String errorMsg = String.format("Exception notifying executed action ! Error: %s", e.getLocalizedMessage());
            logger.error(errorMsg);
            throw new WldtDigitalTwinStateException(errorMsg);
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
     * Returns the last DT State Transaction
     * @return The last DT State Transaction
     */
    public DigitalTwinStateTransaction getDigitalTwinStateTransaction() {
        return digitalTwinStateTransaction;
    }

    /**
     * Returns the description (structure, without values) of the current Digital Twin State
     * @return The current Digital Twin State Description
     */
    public DigitalTwinStateDescription getDigitalTwinStateDescription() {
        return digitalTwinState.getDescription();
    }

    /**
     * Returns the complete values of the current Digital Twin State, associated to the id of the current description
     * @return The current Digital Twin State Values
     */
    public DigitalTwinStateValues getDigitalTwinStateValues() {
        return digitalTwinState.getValues();
    }

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    /**
     * Handle the registration of the metrics related to the computation of the Digital Twin State if the Monitoring Interface is correctly configured on the DT State Manager.
     */
    private void handleMetricsRegistration() {

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DT_MODEL)){

            // Build metric namespace
            this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();

            // Register Counter Metric(s) - DT State Computation
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DT_STATE_COMPUTATION_SUCCESS_COUNT,
                    WldtMetricComponent.DT_MODEL));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DT_STATE_COMPUTATION_ERROR_COUNT,
                    WldtMetricComponent.DT_MODEL));

            // Register Execution Time Metrics - DT State Computation
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DT_STATE_COMPUTATION_EXEC_TIME,
                    WldtMetricComponent.DT_MODEL));
        }

    }

    public MonitoringInterface getMonitoringInterface() {
        return monitoringInterface;
    }

    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {

        // Check if the monitoring interface is not null
        if(monitoringInterface != null) {

            // Set the Monitoring Interface Reference to the DT Model
            this.monitoringInterface = monitoringInterface;

            // Handle Metrics Registration
            this.handleMetricsRegistration();
        }
        else
            logger.error("Cannot set Monitoring Interface to null for Digital Twin State Manager (DT Id: {}) !", this.digitalTwinId);
    }
}

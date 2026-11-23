package it.wldt.core.state.action;

import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the events published on the event bus when the actions of the Digital Twin State change. The actions are part
 * of the structure (description): their definition changes produce a description update event and no values event;
 * the execution of an action ({@code notifyActionExecuted}) produces a values update event with its last invocation.
 */
public class DigitalTwinStateActionObserverTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String ACTION_KEY_1 = "testActionKey1";
    private final static String ACTION_KEY_2 = "testActionKey2";
    private final static String ACTION_TYPE = "testActionType";
    private final static String ACTION_CONTENT_TYPE = "application/json";
    private final static String ACTION_CONTENT_TYPE_UPDATED = "text/plain";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    private DigitalTwinStateAction targetAction1;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }

    private void enableAction() throws WldtDigitalTwinStateException {

        initTestDtState();

        targetAction1 = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(targetAction1);
        digitalTwinStateManager.commitStateTransaction();
    }

    @Test
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        DigitalTwinStateDescription originalDescription = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        targetAction1 = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(targetAction1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        // The action is part of the new description
        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription(), collector.description(0));
        assertEquals(1, collector.description(0).getActions().size());
        assertEquals(ACTION_KEY_1, collector.description(0).getActions().get(0).getKey());
        assertEquals(ACTION_TYPE, collector.description(0).getActions().get(0).getType());
        assertEquals(ACTION_CONTENT_TYPE, collector.description(0).getActions().get(0).getContentType());
        assertNotEquals(originalDescription.getId(), collector.description(0).getId());
        assertEquals(originalDescription, collector.previousDescription(0));

        // State Change List
        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.ACTION, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(targetAction1, collector.descriptionChanges(0).get(0).getResource());

        // An action carries no value until it is executed: no values event
        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeStateUpdatedAction() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        enableAction();
        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        DigitalTwinStateAction updatedAction = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(updatedAction);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertEquals(1, collector.description(0).getActions().size());
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, collector.description(0).getActions().get(0).getContentType());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(ACTION_CONTENT_TYPE, collector.previousDescription(0).getActions().get(0).getContentType());

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.ACTION, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(updatedAction, collector.descriptionChanges(0).get(0).getResource());

        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeStateDeletedAction() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        enableAction();
        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertTrue(collector.description(0).getActions().isEmpty());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(descriptionBefore, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_REMOVE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.ACTION, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(targetAction1, collector.descriptionChanges(0).get(0).getResource());

        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeActionExecuted() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        enableAction();
        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        Instant executionInstant = Instant.now().minusSeconds(30);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.notifyActionExecuted(ACTION_KEY_1, executionInstant);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        // The structure does not change, the last invocation is a value of the state
        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertEquals(executionInstant, collector.delta(0).getLastActionInvocations().get(ACTION_KEY_1));
        assertTrue(collector.delta(0).getPropertyValues().isEmpty());
        assertTrue(collector.previousValues(0).getLastActionInvocations().isEmpty());

        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.ACTION, collector.valuesChanges(0).get(0).getResourceType());
        assertEquals(executionInstant, collector.valuesChanges(0).get(0).getValueInstant());

        assertEquals(executionInstant, digitalTwinStateManager.getDigitalTwinStateValues().getLastActionInvocations().get(ACTION_KEY_1));

        collector.stop();
    }

    @Test
    public void notifyActionExecutedRequiresTransactionAndExistingAction() throws WldtDigitalTwinStateException {

        enableAction();

        // Outside of a transaction
        assertThrows(WldtDigitalTwinStateException.class, () -> digitalTwinStateManager.notifyActionExecuted(ACTION_KEY_1));

        // Action not defined in the state
        digitalTwinStateManager.startStateTransaction();
        assertThrows(WldtDigitalTwinStateException.class, () -> digitalTwinStateManager.notifyActionExecuted(ACTION_KEY_2));
        digitalTwinStateManager.rollbackTransaction();
    }
}

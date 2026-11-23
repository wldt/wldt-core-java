package it.wldt.core.state.relationship;

import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the events published on the event bus when the relationships of the Digital Twin State change: the
 * relationship definition is part of the structure (description update event) while its instances are values
 * (values update event with the complete list of the instances of the touched relationship).
 */
public class DigitalTwinStateRelationshipObserverTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private static final String REL_TYPE_1 = "relType1";

    private static final String REL_NAME_1 = "relName1";

    private static final String REL_TYPE_2 = "relType2";

    private static final String REL_NAME_2 = "relName2";

    private static final String REL_INSTANCE_TARGET_ID_1 = "targetId1";

    private static final String REL_INSTANCE_KEY_1 = "relInstanceKey1";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    private DigitalTwinStateRelationship<String> testRelationship1;

    private DigitalTwinStateRelationshipInstance<String> testRelationshipInstance1;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }

    private void createRelationship() throws WldtDigitalTwinStateException {

        initTestDtState();

        testRelationship1 = new DigitalTwinStateRelationship<>(REL_NAME_1, REL_TYPE_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createRelationship(testRelationship1);
        digitalTwinStateManager.commitStateTransaction();
    }

    private void createRelationshipInstance() throws WldtDigitalTwinStateException {

        testRelationshipInstance1 = new DigitalTwinStateRelationshipInstance<>(REL_NAME_1, REL_INSTANCE_TARGET_ID_1, REL_INSTANCE_KEY_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.addRelationshipInstance(testRelationshipInstance1);
        digitalTwinStateManager.commitStateTransaction();
    }

    @Test
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        DigitalTwinStateDescription originalDescription = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        testRelationship1 = new DigitalTwinStateRelationship<>(REL_NAME_1, REL_TYPE_1);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createRelationship(testRelationship1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        assertTrue(collector.awaitValuesEvents(1, 2000));

        // Description: the relationship definition
        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription(), collector.description(0));
        assertEquals(1, collector.description(0).getRelationships().size());
        assertEquals(REL_NAME_1, collector.description(0).getRelationships().get(0).getName());
        assertEquals(REL_TYPE_1, collector.description(0).getRelationships().get(0).getType());
        assertNotEquals(originalDescription.getId(), collector.description(0).getId());
        assertEquals(originalDescription, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.RELATIONSHIP, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testRelationship1, collector.descriptionChanges(0).get(0).getResource());

        // Values: the new relationship has no instances yet
        assertEquals(collector.description(0).getId(), collector.delta(0).getDescriptionId());
        assertTrue(collector.delta(0).getRelationshipInstances().containsKey(REL_NAME_1));
        assertTrue(collector.delta(0).getRelationshipInstances().get(REL_NAME_1).isEmpty());

        collector.stop();
    }

    @Test
    public void observeStateDeletedRelationship() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        createRelationship();
        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationship(REL_NAME_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertTrue(collector.description(0).getRelationships().isEmpty());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(descriptionBefore, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_REMOVE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.RELATIONSHIP, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testRelationship1, collector.descriptionChanges(0).get(0).getResource());

        // The removal is carried only by the description: there is no values update and no value remains
        assertTrue(collector.getValuesEvents().isEmpty());
        assertFalse(digitalTwinStateManager.getDigitalTwinStateValues().getRelationshipInstances().containsKey(REL_NAME_1));

        collector.stop();
    }

    @Test
    public void observeStateChangesCreateRelationshipInstance() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        createRelationship();
        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        testRelationshipInstance1 = new DigitalTwinStateRelationshipInstance<>(REL_NAME_1, REL_INSTANCE_TARGET_ID_1, REL_INSTANCE_KEY_1);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.addRelationshipInstance(testRelationshipInstance1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        // The instance is a value: the structure does not change
        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertEquals(1, collector.delta(0).getRelationshipInstances().get(REL_NAME_1).size());
        assertEquals(REL_INSTANCE_KEY_1, collector.delta(0).getRelationshipInstances().get(REL_NAME_1).get(0).getKey());
        assertTrue(collector.previousValues(0).getRelationshipInstances().get(REL_NAME_1).isEmpty());

        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE, collector.valuesChanges(0).get(0).getResourceType());
        assertEquals(testRelationshipInstance1, collector.valuesChanges(0).get(0).getResource());

        collector.stop();
    }

    @Test
    public void observeStateDeletedRelationshipInstance() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        createRelationship();
        createRelationshipInstance();
        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertTrue(collector.delta(0).getRelationshipInstances().get(REL_NAME_1).isEmpty());
        assertEquals(1, collector.previousValues(0).getRelationshipInstances().get(REL_NAME_1).size());

        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_REMOVE, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE, collector.valuesChanges(0).get(0).getResourceType());
        assertEquals(testRelationshipInstance1, collector.valuesChanges(0).get(0).getResource());

        collector.stop();
    }
}

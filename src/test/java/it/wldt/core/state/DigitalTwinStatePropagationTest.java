package it.wldt.core.state;

import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the propagation of the Digital Twin State on the event bus: description id, description events only when the
 * structure really changes, values events with only the changed values, transactions and event notifications.
 */
public class DigitalTwinStatePropagationTest {

    private static final String DIGITAL_TWIN_ID = "dt-propagation-test";
    private static final String SUBSCRIBER_ID = "propagation-test-subscriber";

    private DigitalTwinStateManager manager;
    private StateEventCollector collector;

    @BeforeEach
    public void setUp() throws Exception {
        manager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID);
        collector.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        collector.stop();
    }

    private void createProperty(String key, Object value) throws Exception {
        manager.startStateTransaction();
        manager.createProperty(new DigitalTwinStateProperty<>(key, value));
        manager.commitStateTransaction();
    }

    @Test
    public void descriptionIdIsTheCreationTimestampFollowedByTheSequenceNumber() throws Exception {

        long before = System.currentTimeMillis();
        DigitalTwinStateManager created = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        long after = System.currentTimeMillis();

        String id = created.getDigitalTwinStateDescription().getId();
        String[] parts = id.split("_");

        assertEquals(2, parts.length);
        long creationTimestamp = Long.parseLong(parts[0]);
        assertTrue(creationTimestamp >= before && creationTimestamp <= after);
        assertEquals("0", parts[1]);

        // The values always refer to the id of the current description
        assertEquals(id, created.getDigitalTwinStateValues().getDescriptionId());
    }

    @Test
    public void sequenceNumberGrowsOnlyWhenTheStructureChanges() throws Exception {

        String initialId = manager.getDigitalTwinStateDescription().getId();
        String prefix = initialId.substring(0, initialId.lastIndexOf('_') + 1);
        assertEquals(prefix + "0", initialId);

        // New property: structure changes
        createProperty("p1", 1);
        assertEquals(prefix + "1", manager.getDigitalTwinStateDescription().getId());
        assertEquals(prefix + "1", manager.getDigitalTwinStateValues().getDescriptionId());

        // Value update: same structure
        manager.startStateTransaction();
        manager.updatePropertyValue(new DigitalTwinStateProperty<>("p1", 2));
        manager.commitStateTransaction();
        assertEquals(prefix + "1", manager.getDigitalTwinStateDescription().getId());

        // Update of the property with exactly the same structure: the description is not changed
        manager.startStateTransaction();
        manager.updateProperty(new DigitalTwinStateProperty<>("p1", 3));
        manager.commitStateTransaction();
        assertEquals(prefix + "1", manager.getDigitalTwinStateDescription().getId());

        // Update of the property that changes its structure
        DigitalTwinStateProperty<Integer> writable = new DigitalTwinStateProperty<>("p1", 3);
        writable.setType("another-type");
        manager.startStateTransaction();
        manager.updateProperty(writable);
        manager.commitStateTransaction();
        assertEquals(prefix + "2", manager.getDigitalTwinStateDescription().getId());

        // The sequence goes on with the other kinds of resource
        manager.startStateTransaction();
        manager.enableAction(new DigitalTwinStateAction("a1", "t", "c"));
        manager.commitStateTransaction();
        assertEquals(prefix + "3", manager.getDigitalTwinStateDescription().getId());

        // Several structural changes in the same transaction produce a single new description
        manager.startStateTransaction();
        manager.createProperty(new DigitalTwinStateProperty<>("p2", 1));
        manager.registerEvent(new DigitalTwinStateEvent("e1", "t", "c"));
        manager.commitStateTransaction();
        assertEquals(prefix + "4", manager.getDigitalTwinStateDescription().getId());
    }

    @Test
    public void noDescriptionEventWhenTheStructureDoesNotChange() throws Exception {

        createProperty("p1", 1);
        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        assertTrue(collector.awaitValuesEvents(1, 2000));

        // Identical update of the property structure with a new value
        manager.startStateTransaction();
        manager.updateProperty(new DigitalTwinStateProperty<>("p1", 5));
        manager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(2, 2000));
        collector.settle();

        assertEquals(1, collector.getDescriptionEvents().size());
        assertEquals(5, collector.delta(1).getPropertyValue("p1"));
    }

    @Test
    public void onlyTheChangedValuesTravelInTheValuesEvent() throws Exception {

        manager.startStateTransaction();
        manager.createProperty(new DigitalTwinStateProperty<>("p1", 1));
        manager.createProperty(new DigitalTwinStateProperty<>("p2", 2));
        manager.createProperty(new DigitalTwinStateProperty<>("p3", 3));
        manager.commitStateTransaction();
        assertTrue(collector.awaitValuesEvents(1, 2000));

        manager.startStateTransaction();
        manager.updatePropertyValue(new DigitalTwinStateProperty<>("p2", 20));
        manager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(2, 2000));

        // The first event carries the three initial values, the second only the changed one
        assertEquals(3, collector.delta(0).getPropertyValues().size());
        assertEquals(1, collector.delta(1).getPropertyValues().size());
        assertEquals(20, collector.delta(1).getPropertyValue("p2"));
        assertEquals(2, collector.previousValues(1).getPropertyValue("p2"));
        assertEquals(1, collector.previousValues(1).getPropertyValues().size());

        // The state manager has the complete values
        assertEquals(3, manager.getDigitalTwinStateValues().getPropertyValues().size());
        assertEquals(20, manager.getDigitalTwinStateValues().getPropertyValue("p2"));
    }

    @Test
    public void sameValueUpdateHasAnEmptyDeltaButIsNotifiedWithTheNewInstant() throws Exception {

        createProperty("p1", 7);
        assertTrue(collector.awaitValuesEvents(1, 2000));
        Instant firstInstant = collector.delta(0).getEvaluationInstant();

        Thread.sleep(5);

        manager.startStateTransaction();
        manager.updatePropertyValue(new DigitalTwinStateProperty<>("p1", 7));
        manager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(2, 2000));
        collector.settle();

        // The value did not change: it is not in the delta, but the update is in the change list
        assertTrue(collector.delta(1).getPropertyValues().isEmpty());
        assertEquals(1, collector.valuesChanges(1).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE, collector.valuesChanges(1).get(0).getOperation());
        assertTrue(collector.delta(1).getEvaluationInstant().isAfter(firstInstant));
        assertEquals(1, collector.getDescriptionEvents().size());
    }

    @Test
    public void descriptionIsPublishedBeforeTheValuesOfTheSameTransaction() throws Exception {

        createProperty("p1", 1);
        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        assertTrue(collector.awaitValuesEvents(1, 2000));

        // The values refer to the id of the new description
        assertEquals(collector.description(0).getId(), collector.delta(0).getDescriptionId());
        assertTrue(collector.getDescriptionEvents().get(0).getCreationTimestamp() <= collector.getValuesEvents().get(0).getCreationTimestamp());
    }

    @Test
    public void transactionWithoutChangesPublishesNothing() throws Exception {

        manager.startStateTransaction();
        manager.commitStateTransaction();

        collector.settle();
        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertTrue(collector.getValuesEvents().isEmpty());
    }

    @Test
    public void completeStateIsNeverExposedByTheManager() {

        for (Method method : DigitalTwinStateManager.class.getMethods())
            assertNotEquals(DigitalTwinState.class, method.getReturnType(), "Manager method exposing the complete state: " + method);

        for (Method method : DigitalTwinStateTransaction.class.getMethods())
            assertNotEquals(DigitalTwinState.class, method.getReturnType(), "Transaction method exposing the complete state: " + method);
    }

    @Test
    public void removingAnActionDropsItsLastInvocation() throws Exception {

        manager.startStateTransaction();
        manager.enableAction(new DigitalTwinStateAction("a1", "t", "c"));
        manager.commitStateTransaction();

        manager.startStateTransaction();
        manager.notifyActionExecuted("a1");
        manager.commitStateTransaction();
        assertTrue(manager.getDigitalTwinStateValues().getLastActionInvocations().containsKey("a1"));

        manager.startStateTransaction();
        manager.disableAction("a1");
        manager.commitStateTransaction();

        // The same action defined again does not inherit the old invocation
        manager.startStateTransaction();
        manager.enableAction(new DigitalTwinStateAction("a1", "t", "c"));
        manager.commitStateTransaction();

        assertFalse(manager.getDigitalTwinStateValues().getLastActionInvocations().containsKey("a1"));
    }

    @Test
    public void notifyActionExecutedUsesTheCurrentInstantByDefault() throws Exception {

        manager.startStateTransaction();
        manager.enableAction(new DigitalTwinStateAction("a1", "t", "c"));
        manager.commitStateTransaction();

        Instant before = Instant.now();
        manager.startStateTransaction();
        manager.notifyActionExecuted("a1");
        manager.commitStateTransaction();
        Instant after = Instant.now();

        Instant recorded = manager.getDigitalTwinStateValues().getLastActionInvocations().get("a1");
        assertFalse(recorded.isBefore(before));
        assertFalse(recorded.isAfter(after));
    }

    @Test
    public void rollbackDiscardsTheLastTriggeredChange() throws Exception {

        manager.startStateTransaction();
        manager.enableAction(new DigitalTwinStateAction("a1", "t", "c"));
        manager.commitStateTransaction();

        manager.startStateTransaction();
        manager.notifyActionExecuted("a1");
        manager.rollbackTransaction();
        manager.commitStateTransaction();

        assertTrue(manager.getDigitalTwinStateValues().getLastActionInvocations().isEmpty());
    }

    @Test
    public void invalidActionsAndEventsAreRejected() throws Exception {

        // Outside of a transaction
        assertThrows(WldtDigitalTwinStateException.class, () -> manager.notifyActionExecuted("missing"));

        manager.startStateTransaction();
        assertThrows(WldtDigitalTwinStateException.class, () -> manager.notifyActionExecuted("missing"));
        assertThrows(WldtDigitalTwinStateException.class, () -> manager.notifyActionExecuted(null));
        manager.rollbackTransaction();
    }
}

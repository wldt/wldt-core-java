package it.wldt.core.state;

import it.wldt.core.event.WldtEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.wldt.core.state.DigitalTwinStateChange.Operation.*;
import static it.wldt.core.state.DigitalTwinStateChange.ResourceType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the local copy of the Digital Twin State kept by the components that observe the event bus.
 */
public class DigitalTwinStateReplicaTest {

    private DigitalTwinStateReplica replica;

    private static DigitalTwinState state(String descriptionId, int temp, boolean withAlarm) throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", temp));
        properties.put("hum", new DigitalTwinStateProperty<>("hum", 40));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        if (withAlarm)
            events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        return DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new HashMap<>(), events, new HashMap<>()), descriptionId);
    }

    private static DigitalTwinStateValues delta(DigitalTwinState state, String... properties) {
        return state.getValues().filter(DigitalTwinStateFilter.all()
                .withPropertyNames(properties).withActionNames().withEventNames().withRelationshipNames());
    }

    private static WldtEvent<DigitalTwinStateValues> valuesEvent(DigitalTwinStateValues delta, DigitalTwinStateChange... changes) throws Exception {
        WldtEvent<DigitalTwinStateValues> event = new WldtEvent<>(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());
        event.setBody(delta);
        event.putMetadata(DigitalTwinStateManager.DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
        return event;
    }

    private static WldtEvent<DigitalTwinStateDescription> descriptionEvent(DigitalTwinStateDescription description, DigitalTwinStateChange... changes) throws Exception {
        WldtEvent<DigitalTwinStateDescription> event = new WldtEvent<>(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType());
        event.setBody(description);
        event.putMetadata(DigitalTwinStateManager.DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
        return event;
    }

    private static DigitalTwinStateChange valueChange(String property) throws Exception {
        return new DigitalTwinStateChange(OPERATION_UPDATE_VALUE, PROPERTY_VALUE, new DigitalTwinStateProperty<>(property, 1));
    }

    @BeforeEach
    public void setUp() throws Exception {
        replica = new DigitalTwinStateReplica();
        DigitalTwinState initial = state("10_0", 20, false);
        replica.seed(initial.getDescription(), initial.getValues());
        Thread.sleep(5);
    }

    @Test
    public void notSeededReplicaIsEmpty() {
        DigitalTwinStateReplica empty = new DigitalTwinStateReplica();
        assertFalse(empty.isSeeded());
        assertNull(empty.getDescription());
        assertNull(empty.getValues());
    }

    @Test
    public void valuesDeltaIsAppliedToTheCompleteValues() throws Exception {
        DigitalTwinState updated = state("10_0", 30, false);

        assertTrue(replica.applyValuesEvent(valuesEvent(delta(updated, "temp"), valueChange("temp"))));

        assertEquals(30, replica.getValues().getPropertyValue("temp"));
        assertEquals(40, replica.getValues().getPropertyValue("hum"));
    }

    @Test
    public void removedPropertyIsDroppedFromTheValuesWhenTheDescriptionIsApplied() throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", 20));
        DigitalTwinState withoutHum = DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new HashMap<>(), new HashMap<>(), new HashMap<>()), "10_1");
        DigitalTwinStateChange removal = new DigitalTwinStateChange(OPERATION_REMOVE, PROPERTY, new DigitalTwinStateProperty<>("hum", 40));

        // The description is applied: the removal has no values update, nothing is pending
        assertTrue(replica.applyDescriptionEvent(descriptionEvent(withoutHum.getDescription(), removal)));
        assertNull(replica.applyNextPendingValues());
        assertFalse(replica.getValues().getPropertyValues().containsKey("hum"));
        assertTrue(replica.getValues().getPropertyValues().containsKey("temp"));
        assertEquals("10_1", replica.getValues().getDescriptionId());
    }

    @Test
    public void descriptionIsAppliedAndValuesAreAlignedToIt() throws Exception {
        DigitalTwinState withAlarm = state("10_1", 20, true);

        assertTrue(replica.applyDescriptionEvent(descriptionEvent(withAlarm.getDescription(),
                new DigitalTwinStateChange(OPERATION_ADD, EVENT, new DigitalTwinStateEvent("alarm", "t", "string")))));

        assertEquals("10_1", replica.getDescription().getId());
        assertEquals("10_1", replica.getValues().getDescriptionId());
        assertEquals(1, replica.getDescription().getEvents().size());
    }

    @Test
    public void valuesWaitingForTheirDescriptionAreProcessedInArrivalOrder() throws Exception {
        DigitalTwinState v1 = state("10_1", 21, true);
        DigitalTwinState v2 = state("10_1", 22, true);
        DigitalTwinState v3 = state("10_1", 23, true);

        // Three values updates arrive before their description: nothing is applied, nothing is lost
        assertFalse(replica.applyValuesEvent(valuesEvent(delta(v1, "temp"), valueChange("temp"))));
        assertFalse(replica.applyValuesEvent(valuesEvent(delta(v2, "temp"), valueChange("temp"))));
        assertFalse(replica.applyValuesEvent(valuesEvent(delta(v3, "temp"), valueChange("temp"))));
        assertEquals(3, replica.getPendingValuesCount());
        assertEquals(20, replica.getValues().getPropertyValue("temp"));

        assertTrue(replica.applyDescriptionEvent(descriptionEvent(v1.getDescription())));
        assertEquals("10_1", replica.getDescription().getId());

        // The values are applied one at a time in the order they arrived, each one with its own snapshot
        assertNotNull(replica.applyNextPendingValues());
        assertEquals(21, replica.getValues().getPropertyValue("temp"));
        assertNotNull(replica.applyNextPendingValues());
        assertEquals(22, replica.getValues().getPropertyValue("temp"));
        assertNotNull(replica.applyNextPendingValues());
        assertEquals(23, replica.getValues().getPropertyValue("temp"));
        assertNull(replica.applyNextPendingValues());
        assertEquals(0, replica.getPendingValuesCount());
        assertEquals(23, replica.getValues().getPropertyValue("temp"));
    }

    @Test
    public void valuesOfAnEvenNewerDescriptionStayPending() throws Exception {
        DigitalTwinState d1 = state("10_1", 21, true);
        DigitalTwinState d2 = state("10_2", 99, true);

        replica.applyValuesEvent(valuesEvent(delta(d2, "temp"), valueChange("temp")));

        assertTrue(replica.applyDescriptionEvent(descriptionEvent(d1.getDescription())));

        // The values refer to a newer description: they cannot be applied yet
        assertNull(replica.applyNextPendingValues());
        assertEquals(1, replica.getPendingValuesCount());
        assertEquals(20, replica.getValues().getPropertyValue("temp"));

        assertTrue(replica.applyDescriptionEvent(descriptionEvent(d2.getDescription())));
        assertNotNull(replica.applyNextPendingValues());
        assertEquals(99, replica.getValues().getPropertyValue("temp"));
    }

    @Test
    public void valuesOfAnOlderDescriptionAreStillApplied() throws Exception {
        DigitalTwinState d1 = state("10_1", 20, true);
        replica.applyDescriptionEvent(descriptionEvent(d1.getDescription()));

        // Delivered late: refers to the previous description but it must not be lost
        DigitalTwinState late = state("10_0", 77, false);
        assertTrue(replica.applyValuesEvent(valuesEvent(delta(late, "temp"), valueChange("temp"))));

        assertEquals(77, replica.getValues().getPropertyValue("temp"));
        // The complete values always refer to the current description
        assertEquals("10_1", replica.getValues().getDescriptionId());
    }

    @Test
    public void eventsAlreadyIncludedInTheSeedAreSkipped() throws Exception {
        // A delta with an instant that is not after the instant of the seeded values is already included in them
        DigitalTwinState old = state("10_0", 5, false);
        DigitalTwinStateReplica seeded = new DigitalTwinStateReplica();
        Thread.sleep(5);
        DigitalTwinState initial = state("10_0", 20, false);
        seeded.seed(initial.getDescription(), initial.getValues());

        assertTrue(old.getEvaluationInstant().isBefore(initial.getEvaluationInstant()) || old.getEvaluationInstant().equals(initial.getEvaluationInstant()));
        assertFalse(seeded.applyValuesEvent(valuesEvent(delta(old, "temp"), valueChange("temp"))));
        assertEquals(20, seeded.getValues().getPropertyValue("temp"));

        // An older or equal description is skipped too
        assertFalse(seeded.applyDescriptionEvent(descriptionEvent(initial.getDescription())));
    }

    @Test
    public void descriptionIdsAreCompared() {
        assertEquals(0, DigitalTwinStateReplica.compareIds("10_3", "10_3"));
        assertTrue(DigitalTwinStateReplica.compareIds("10_2", "10_3") < 0);
        assertTrue(DigitalTwinStateReplica.compareIds("10_11", "10_3") > 0);
        // Different creation timestamps or unparsable ids are considered newer
        assertTrue(DigitalTwinStateReplica.compareIds("11_0", "10_3") > 0);
        assertTrue(DigitalTwinStateReplica.compareIds("abc", "10_3") > 0);
    }

    @Test
    public void eventsOfAnotherStateManagerAreIgnored() throws Exception {
        // Same sequence number but a different creation timestamp of the manager: not the followed state
        DigitalTwinState foreign = state("99_0", 77, false);

        assertFalse(replica.applyValuesEvent(valuesEvent(delta(foreign, "temp"), valueChange("temp"))));
        assertEquals(0, replica.getPendingValuesCount());
        assertEquals(20, replica.getValues().getPropertyValue("temp"));

        DigitalTwinState foreignNewer = state("99_5", 77, true);
        assertFalse(replica.applyDescriptionEvent(descriptionEvent(foreignNewer.getDescription())));
        assertEquals("10_0", replica.getDescription().getId());

        assertTrue(DigitalTwinStateReplica.sameLineage("10_1", "10_9"));
        assertFalse(DigitalTwinStateReplica.sameLineage("10_1", "11_1"));
    }

    @Test
    public void clearDropsEverything() throws Exception {
        replica.applyValuesEvent(valuesEvent(delta(state("10_5", 1, false), "temp"), valueChange("temp")));
        replica.clear();

        assertFalse(replica.isSeeded());
        assertEquals(0, replica.getPendingValuesCount());
    }

    @Test
    public void valuesInstantFollowsTheLastDelta() throws Exception {
        Instant before = replica.getValues().getEvaluationInstant();
        DigitalTwinState updated = state("10_0", 20, false);

        replica.applyValuesEvent(valuesEvent(delta(updated), valueChange("temp")));

        assertTrue(replica.getValues().getEvaluationInstant().isAfter(before));
    }
}

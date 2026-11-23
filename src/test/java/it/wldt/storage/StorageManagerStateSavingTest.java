package it.wldt.storage;

import it.wldt.core.state.*;
import it.wldt.exception.StorageException;
import it.wldt.storage.model.state.DigitalTwinStateDescriptionRecord;
import it.wldt.storage.model.state.DigitalTwinStateValuesRecord;
import it.wldt.storage.query.QueryResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static it.wldt.core.state.StateTestEvents.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the handling of the description and values events of the Storage Manager: the description and the values
 * (complete, taken from the local copy of the state) are always saved, and the storages receive the values delta too.
 */
public class StorageManagerStateSavingTest {

    private static final String DESCRIPTION_ID_0 = "5000_0";

    /**
     * Default storage that also records the arguments of every saveDigitalTwinStateValues call
     */
    private static class RecordingStorage extends DefaultWldtStorage {

        private final List<DigitalTwinStateValues> completeValues = new ArrayList<>();
        private final List<DigitalTwinStateValues> deltas = new ArrayList<>();
        private final List<List<DigitalTwinStateChange>> changes = new ArrayList<>();

        RecordingStorage(String id) {
            super(id, true);
        }

        @Override
        public void saveDigitalTwinStateValues(DigitalTwinStateValues digitalTwinStateValues, DigitalTwinStateValues digitalTwinStateValuesDelta, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws StorageException, IllegalArgumentException {
            completeValues.add(digitalTwinStateValues);
            deltas.add(digitalTwinStateValuesDelta);
            changes.add(digitalTwinStateChangeList);
            super.saveDigitalTwinStateValues(digitalTwinStateValues, digitalTwinStateValuesDelta, digitalTwinStateChangeList);
        }
    }

    private RecordingStorage storage;

    @BeforeEach
    public void setUp() throws Exception {
        storage = new RecordingStorage("saving-test-storage");
    }

    private StorageManager manager() throws Exception {
        StorageManager manager = new StorageManager("storage-saving-test-dt");
        manager.putStorage(storage);
        DigitalTwinState initial = state(DESCRIPTION_ID_0, 20, false);
        manager.seedDigitalTwinState(initial.getDescription(), initial.getValues());
        Thread.sleep(5);
        return manager;
    }

    private List<DigitalTwinStateValuesRecord> storedValues() {
        return StorageTestUtils.inTimeRange(storage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, 0, Long.MAX_VALUE);
    }

    private List<DigitalTwinStateDescriptionRecord> storedDescriptions() {
        return StorageTestUtils.inTimeRange(storage, QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION, 0, Long.MAX_VALUE);
    }

    @Test
    public void everyValuesUpdateIsSavedWithCompleteValues() throws Exception {
        StorageManager manager = manager();

        DigitalTwinState updated = state(DESCRIPTION_ID_0, 30, false);
        manager.onStateValuesEvent(valuesEvent(delta(updated, "temp"), valueChange("temp")));

        List<DigitalTwinStateValuesRecord> values = storedValues();
        assertEquals(1, values.size());
        assertEquals(DESCRIPTION_ID_0, values.get(0).getDescriptionId());
        // The delta carried only temp: the stored values are complete
        assertEquals(30, values.get(0).getValues().getPropertyValue("temp"));
        assertEquals(40, values.get(0).getValues().getPropertyValue("hum"));
        assertEquals(1, values.get(0).getStateChangeList().size());
    }

    @Test
    public void valuesUpdatesAreAlwaysSavedAndAccumulateInTheCompleteValues() throws Exception {
        StorageManager manager = manager();

        manager.onStateValuesEvent(valuesEvent(delta(state(DESCRIPTION_ID_0, 20, false), "hum"), valueChange("hum")));
        manager.onStateValuesEvent(valuesEvent(delta(state(DESCRIPTION_ID_0, 31, false), "temp"), valueChange("temp")));

        List<DigitalTwinStateValuesRecord> values = storedValues();
        assertEquals(2, values.size());
        for (DigitalTwinStateValuesRecord record : values)
            assertEquals(2, record.getValues().getPropertyValues().size());
    }

    @Test
    public void descriptionIsSavedComplete() throws Exception {
        StorageManager manager = manager();

        DigitalTwinState withAlarm = state("5000_1", 20, true);
        manager.onStateDescriptionEvent(descriptionEvent(withAlarm.getDescription(),
                new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_ADD,
                        DigitalTwinStateChange.ResourceType.EVENT, new DigitalTwinStateEvent("alarm", "t", "string"))));

        List<DigitalTwinStateDescriptionRecord> descriptions = storedDescriptions();
        assertEquals(1, descriptions.size());
        assertEquals("5000_1", descriptions.get(0).getDescriptionId());
        assertEquals(2, descriptions.get(0).getDescription().getProperties().size());
        assertEquals(1, descriptions.get(0).getDescription().getEvents().size());
        assertEquals(1, descriptions.get(0).getStateChangeList().size());
    }

    @Test
    public void valuesWaitingForTheirDescriptionAreSavedAfterIt() throws Exception {
        StorageManager manager = manager();

        DigitalTwinState newer = state("5000_1", 50, true);

        // Values of a description not received yet: kept, not saved
        manager.onStateValuesEvent(valuesEvent(delta(newer, "temp"), valueChange("temp")));
        assertTrue(storedValues().isEmpty());

        manager.onStateDescriptionEvent(descriptionEvent(newer.getDescription()));

        assertEquals(1, storedDescriptions().size());
        assertEquals(1, storedValues().size());
        assertEquals("5000_1", storedValues().get(0).getDescriptionId());
        assertEquals(50, storedValues().get(0).getValues().getPropertyValue("temp"));
        assertEquals(1, storedValues().get(0).getStateChangeList().size());
    }

    @Test
    public void descriptionWithARemovalSavesTheAlignedValues() throws Exception {
        StorageManager manager = manager();

        // New description without the property "hum"
        java.util.Map<String, DigitalTwinStateProperty<?>> properties = new java.util.HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", 20));
        DigitalTwinState withoutHum = DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>()), "5000_1");

        manager.onStateDescriptionEvent(descriptionEvent(withoutHum.getDescription(),
                new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.PROPERTY, new DigitalTwinStateProperty<>("hum", 40))));

        assertEquals(1, storedDescriptions().size());

        // There is no values event for the removal, so the storage saves the values aligned to the new description
        List<DigitalTwinStateValuesRecord> values = storedValues();
        assertEquals(1, values.size());
        assertEquals("5000_1", values.get(0).getDescriptionId());
        assertFalse(values.get(0).getValues().getPropertyValues().containsKey("hum"));
        assertEquals(20, values.get(0).getValues().getPropertyValue("temp"));
    }

    @Test
    public void descriptionWithoutRemovalsDoesNotSaveValues() throws Exception {
        StorageManager manager = manager();

        DigitalTwinState withAlarm = state("5000_1", 20, true);
        manager.onStateDescriptionEvent(descriptionEvent(withAlarm.getDescription(),
                new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_ADD,
                        DigitalTwinStateChange.ResourceType.EVENT, new DigitalTwinStateEvent("alarm", "t", "string"))));

        assertEquals(1, storedDescriptions().size());
        assertTrue(storedValues().isEmpty());
    }

    @Test
    public void storageReceivesTheCompleteValuesTheDeltaAndTheChanges() throws Exception {
        StorageManager manager = manager();

        manager.onStateValuesEvent(valuesEvent(delta(state(DESCRIPTION_ID_0, 30, false), "temp"), valueChange("temp")));

        assertEquals(1, storage.completeValues.size());
        // Complete values
        assertEquals(30, storage.completeValues.get(0).getPropertyValue("temp"));
        assertEquals(40, storage.completeValues.get(0).getPropertyValue("hum"));
        // Only what changed with the event
        assertNotNull(storage.deltas.get(0));
        assertEquals(1, storage.deltas.get(0).getPropertyValues().size());
        assertEquals(30, storage.deltas.get(0).getPropertyValue("temp"));
        assertEquals(1, storage.changes.get(0).size());
    }

    @Test
    public void deltaOfAPendingValuesEventIsPassedToTheStorage() throws Exception {
        StorageManager manager = manager();

        DigitalTwinState newer = state("5000_1", 50, true);
        manager.onStateValuesEvent(valuesEvent(delta(newer, "temp"), valueChange("temp")));
        assertTrue(storage.deltas.isEmpty());

        manager.onStateDescriptionEvent(descriptionEvent(newer.getDescription()));

        assertEquals(1, storage.deltas.size());
        assertNotNull(storage.deltas.get(0));
        assertEquals(1, storage.deltas.get(0).getPropertyValues().size());
        assertEquals(50, storage.deltas.get(0).getPropertyValue("temp"));
        assertEquals(50, storage.completeValues.get(0).getPropertyValue("temp"));
    }

    @Test
    public void deltaIsNullWhenTheValuesAreSavedAfterADescriptionRemoval() throws Exception {
        StorageManager manager = manager();

        java.util.Map<String, DigitalTwinStateProperty<?>> properties = new java.util.HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", 20));
        DigitalTwinState withoutHum = DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new java.util.HashMap<>(), new java.util.HashMap<>(), new java.util.HashMap<>()), "5000_1");

        manager.onStateDescriptionEvent(descriptionEvent(withoutHum.getDescription(),
                new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_REMOVE,
                        DigitalTwinStateChange.ResourceType.PROPERTY, new DigitalTwinStateProperty<>("hum", 40))));

        assertEquals(1, storage.deltas.size());
        assertNull(storage.deltas.get(0));
        assertFalse(storage.completeValues.get(0).getPropertyValues().containsKey("hum"));
        assertEquals(1, storage.changes.get(0).size());
    }
}

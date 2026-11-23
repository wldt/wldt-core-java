package it.wldt.core.state.property;

import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateDescription;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.StateEventCollector;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the events published on the event bus when the properties of the Digital Twin State change: the description
 * update event (only if the structure really changes) and the values update event (only the changed values).
 */
public class DigitalTwinStatePropertyObserverTester {

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public final String DIGITAL_TWIN_ID = "dt00001";

    public static final String TEST_PROPERTY_TYPE = "test_type";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    public DigitalTwinStateProperty<String> testProperty1 = null;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
    }

    private void createProperty() throws WldtDigitalTwinStateException {

        testProperty1 = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
        testProperty1.setType(TEST_PROPERTY_TYPE);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.commitStateTransaction();
    }

    private DigitalTwinStateProperty<String> updatedProperty() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        updatedProperty.setType(TEST_PROPERTY_TYPE);
        return updatedProperty;
    }

    @Test
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        DigitalTwinStateDescription originalDescription = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        createProperty();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        assertTrue(collector.awaitValuesEvents(1, 2000));

        // DESCRIPTION EVENT: the complete new description, previous description and structural changes in the metadata
        DigitalTwinStateDescription received = collector.description(0);
        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription(), received);
        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription().getId(), received.getId());
        assertNotEquals(originalDescription.getId(), received.getId());
        assertEquals(1, received.getProperties().size());
        assertEquals(TEST_PROPERTY_KEY_0001, received.getProperties().get(0).getKey());
        assertEquals(TEST_PROPERTY_TYPE, received.getProperties().get(0).getType());
        assertTrue(received.getProperties().get(0).isReadable());
        assertTrue(received.getProperties().get(0).isWritable());

        assertEquals(originalDescription.getId(), collector.previousDescription(0).getId());
        assertTrue(collector.previousDescription(0).isEmpty());

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.PROPERTY, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testProperty1, collector.descriptionChanges(0).get(0).getResource());

        // VALUES EVENT: the delta (the initial value of the new property), associated to the new description
        assertEquals(received.getId(), collector.delta(0).getDescriptionId());
        assertEquals(1, collector.delta(0).getPropertyValues().size());
        assertEquals(TEST_PROPERTY_VALUE_0001, collector.delta(0).getPropertyValue(TEST_PROPERTY_KEY_0001));
        assertTrue(collector.previousValues(0).getPropertyValues().isEmpty());
        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.valuesChanges(0).get(0).getOperation());

        collector.stop();
    }

    @Test
    public void observeStateUpdatedProperty() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        createProperty();

        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        DigitalTwinStateProperty<String> updatedProperty = updatedProperty();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateProperty(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        // The structure is the same: no description event and the description id does not change
        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, digitalTwinStateManager.getDigitalTwinStateDescription().getId());

        // Only the changed value is in the delta, the previous value in the metadata
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertEquals(1, collector.delta(0).getPropertyValues().size());
        assertEquals(TEST_PROPERTY_VALUE_0001_UPDATED, collector.delta(0).getPropertyValue(TEST_PROPERTY_KEY_0001));
        assertEquals(TEST_PROPERTY_VALUE_0001, collector.previousValues(0).getPropertyValue(TEST_PROPERTY_KEY_0001));

        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.PROPERTY, collector.valuesChanges(0).get(0).getResourceType());
        assertEquals(updatedProperty, collector.valuesChanges(0).get(0).getResource());

        collector.stop();
    }

    @Test
    public void observeStateDeletedProperty() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        createProperty();

        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteProperty(TEST_PROPERTY_KEY_0001);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        // The new description does not contain the property anymore
        assertTrue(collector.description(0).getProperties().isEmpty());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(descriptionBefore, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_REMOVE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.PROPERTY, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testProperty1, collector.descriptionChanges(0).get(0).getResource());

        // The removal is carried only by the description: there is no values update
        assertTrue(collector.getValuesEvents().isEmpty());

        // The value of the removed property is not part of the values anymore
        assertFalse(digitalTwinStateManager.getDigitalTwinStateValues().getPropertyValues().containsKey(TEST_PROPERTY_KEY_0001));
        assertEquals(collector.description(0).getId(), digitalTwinStateManager.getDigitalTwinStateValues().getDescriptionId());

        collector.stop();
    }

    @Test
    public void observePropertyValueUpdated() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        createProperty();

        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        DigitalTwinStateProperty<String> updatedProperty = updatedProperty();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updatePropertyValue(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertEquals(TEST_PROPERTY_VALUE_0001_UPDATED, collector.delta(0).getPropertyValue(TEST_PROPERTY_KEY_0001));
        assertEquals(TEST_PROPERTY_VALUE_0001, collector.previousValues(0).getPropertyValue(TEST_PROPERTY_KEY_0001));
        assertEquals(TEST_PROPERTY_VALUE_0001_UPDATED, digitalTwinStateManager.getDigitalTwinStateValues().getPropertyValue(TEST_PROPERTY_KEY_0001));

        assertEquals(1, collector.valuesChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.PROPERTY_VALUE, collector.valuesChanges(0).get(0).getResourceType());
        assertEquals(updatedProperty, collector.valuesChanges(0).get(0).getResource());

        collector.stop();
    }
}

package it.wldt.state;

import it.wldt.core.state.DefaultDigitalTwinState;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.IDigitalTwinState;

import it.wldt.exception.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStatePropertyCRUDTester {

    public static final String TEST_KEY_0001 = "testKey0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING";
    public static final String TEST_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private void initTestDtState() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinState.createProperty(testProperty1);

    }

    @Test
    public void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void readProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void updateProperty() throws WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {

        //Init DigitaTwin State
        initTestDtState();

        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);

        digitalTwinState.updateProperty(updatedProperty);

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(updatedProperty, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001_UPDATED, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void deleteProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {
        //Init DigitalTwin State
        initTestDtState();

        //Remove target property
        digitalTwinState.deleteProperty(TEST_KEY_0001);
        assertFalse(digitalTwinState.getPropertyList().isPresent());
    }

}

package it.wldt.core.state.property;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;

import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStatePropertyCRUDTester {

    public static final String TEST_KEY_0001 = "testKey0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING";

    public static final String TEST_KEY_0002 = "testKey0002";
    public static final String TEST_VALUE_0002 = "TEST-STRING-2";

    public static final String TEST_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static final String TEST_PROPERTY_TYPE = "test_type";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    public DigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    public static DigitalTwinStateProperty<String> testProperty2 = null;

    private void initTestDtState() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {

        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager();
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }

    @Test
    public void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void createMultiplePropertiesSingleTransaction() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        testProperty2 = new DigitalTwinStateProperty<>(TEST_KEY_0002, TEST_VALUE_0002);
        testProperty2.setKey(TEST_KEY_0002);
        testProperty2.setReadable(true);
        testProperty2.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.createProperty(testProperty2);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(2, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0002).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
        assertEquals(testProperty2, digitalTwinState.readProperty(TEST_KEY_0002).get());
        assertEquals(TEST_VALUE_0002, digitalTwinState.readProperty(TEST_KEY_0002).get().getValue());
    }

    @Test
    public void createMultiplePropertiesMultipleTransaction() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        testProperty2 = new DigitalTwinStateProperty<>(TEST_KEY_0002, TEST_VALUE_0002);
        testProperty2.setKey(TEST_KEY_0002);
        testProperty2.setReadable(true);
        testProperty2.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.commitStateTransaction();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty2);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(2, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0002).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
        assertEquals(testProperty2, digitalTwinState.readProperty(TEST_KEY_0002).get());
        assertEquals(TEST_VALUE_0002, digitalTwinState.readProperty(TEST_KEY_0002).get().getValue());
    }

    @Test
    public void readProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void updateProperty() throws WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateProperty(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.getPropertyList().isPresent());
        assertEquals(1, digitalTwinState.getPropertyList().get().size());
        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(updatedProperty, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001_UPDATED, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
    }

    @Test
    public void updatePropertyValue() throws WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_KEY_0001, TEST_VALUE_0001_UPDATED, true, true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updatePropertyValue(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

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

        createProperty();

        //Remove target property
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteProperty(TEST_KEY_0001);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.getPropertyList().isPresent());
    }

    @Test
    public void propertyTypeValidationWithMethod() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyNotFoundException {

        String TEST_PROPERTY_TYPE = "test_type";

        //Init DigitaTwin State
        initTestDtState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setType(TEST_PROPERTY_TYPE);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
        assertEquals(TEST_PROPERTY_TYPE, digitalTwinState.readProperty(TEST_KEY_0001).get().getType());

    }

    @Test
    public void propertyTypeValidationWithConstructor() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyNotFoundException {

        //Init DigitaTwin State
        initTestDtState();

        testProperty1 = new DigitalTwinStateProperty<>(TEST_KEY_0001, TEST_VALUE_0001, TEST_PROPERTY_TYPE);
        testProperty1.setKey(TEST_KEY_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.readProperty(TEST_KEY_0001).isPresent());
        assertEquals(testProperty1, digitalTwinState.readProperty(TEST_KEY_0001).get());
        assertEquals(TEST_VALUE_0001, digitalTwinState.readProperty(TEST_KEY_0001).get().getValue());
        assertEquals(TEST_PROPERTY_TYPE, digitalTwinState.readProperty(TEST_KEY_0001).get().getType());

    }

}

package it.wldt.storage;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinStorageTest2 {
//
//    private final String DIGITAL_TWIN_ID = "dt00001";
//    private final String TEST_KEY_0001 = "testKey0001";
//    private final String TEST_VALUE_0001 = "TEST-STRING";
//    private final String TEST_VALUE_0002 = "TEST-STRING-2";
//
//    @Test
//    public void checkDigitalTwinStateCount() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException, InterruptedException {
//
//        DigitalTwinStateManager digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
//        IWldtStorage wldtStorage = new DefaultWldtStorage();
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001, digitalTwinStateManager, null);
//
//        // Save Initial Digital Twin State
//        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//        int TARGET_STATE_UPDATE_COUNT = 10;
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-r1-%d", TEST_VALUE_0002, i), digitalTwinStateManager, null);
//
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//            Thread.sleep(100);
//        }
//
//        Thread.sleep(1000);
//
//        assertEquals(TARGET_STATE_UPDATE_COUNT+1, wldtStorage.getDigitalTwinStateCount());
//    }
//
//    private void addStateProperty(String propertyKey, String propertyValue, DigitalTwinStateManager digitalTwinStateManager, ArrayList<DigitalTwinStateChange> currentChangeList) throws  WldtDigitalTwinStateException {
//
//        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(propertyKey, propertyValue);
//        property.setKey(propertyKey);
//        property.setReadable(true);
//        property.setWritable(true);
//
//        digitalTwinStateManager.startStateTransaction();
//        digitalTwinStateManager.createProperty(property);
//
//        if(currentChangeList != null)
//            currentChangeList.addAll(digitalTwinStateManager.getDigitalTwinStateTransaction().getDigitalTwinStateChangeList());
//
//        digitalTwinStateManager.commitStateTransaction();
//    }
//
//    private void updateStateProperty(String propertyKey, String propertyValue, DigitalTwinStateManager digitalTwinStateManager, ArrayList<DigitalTwinStateChange> currentChangeList) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {
//
//        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(propertyKey, propertyValue);
//        property.setKey(propertyKey);
//        property.setReadable(true);
//        property.setWritable(true);
//
//        digitalTwinStateManager.startStateTransaction();
//        digitalTwinStateManager.updateProperty(property);
//
//        if(currentChangeList != null)
//            currentChangeList.addAll(digitalTwinStateManager.getDigitalTwinStateTransaction().getDigitalTwinStateChangeList());
//
//        digitalTwinStateManager.commitStateTransaction();
//    }
//
//    private void testStatesStructure(DigitalTwinState referenceDigitalTwinState, DigitalTwinState targetdigitalTwinState) throws WldtDigitalTwinStatePropertyException {
//
//        assertTrue(referenceDigitalTwinState.getPropertyList().isPresent());
//        assertTrue(targetdigitalTwinState.getPropertyList().isPresent());
//        assertEquals(referenceDigitalTwinState.getPropertyList().get().size(), targetdigitalTwinState.getPropertyList().get().size());
//
//    }
//
//    private void testStateProperty(DigitalTwinState digitalTwinState, String propertyKey, Object propertyValue) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {
//
//        assertTrue(digitalTwinState.readProperty(propertyKey).isPresent());
//        assertEquals(propertyValue, digitalTwinState.readProperty(propertyKey).get().getValue());
//
//    }

}

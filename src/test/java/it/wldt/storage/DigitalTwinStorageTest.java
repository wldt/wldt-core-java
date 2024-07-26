package it.wldt.storage;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.*;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigitalTwinStorageTest {

//    private final String DIGITAL_TWIN_ID = "dt00001";
//    private final String TEST_KEY_0001 = "testKey0001";
//    private final String TEST_VALUE_0001 = "TEST-STRING";
//    private final String TEST_VALUE_0002 = "TEST-STRING-2";
//
//    private DigitalTwinStateManager digitalTwinStateManager;
//    private IWldtStorage wldtStorage;
//    private DigitalTwinState digitalTwinState;
//    private ArrayList<DigitalTwinStateChange> currentChangeList;
//
//    @BeforeEach
//    public void initTest() throws WldtDigitalTwinStateException {
//        digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
//        wldtStorage = new DefaultWldtStorage();
//        currentChangeList = new ArrayList<>();
//    }
//
//    @AfterEach
//    public void tearDown() throws WldtDigitalTwinStateException {
//        digitalTwinStateManager = null;
//        wldtStorage = null;
//        digitalTwinState = null;
//        currentChangeList = null;
//    }
//
//    @Test
//    @Order(1)
//    public void saveDigitalTwinStateNoChangeList() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Read the current DT State
//        digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
//
//        wldtStorage.saveDigitalTwinState(digitalTwinState, null);
//
//        Optional<DigitalTwinState> lastDigitalTwinStateOptional = wldtStorage.getLastDigitalTwinState();
//
//        //Check if the Last DT State is available in the Storage
//        assertTrue(lastDigitalTwinStateOptional.isPresent());
//
//        // Load the last DT State in the Storage
//        DigitalTwinState lastDigitalTwinState = lastDigitalTwinStateOptional.get();
//
//        // Test DT State Validity
//        testStatesStructure(digitalTwinState, lastDigitalTwinState);
//        testStateProperty(digitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
//        testStateProperty(lastDigitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
//    }
//
//    @Test
//    @Order(2)
//    public void saveDigitalTwinStateWithChangeList() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Read the current DT State
//        digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
//        wldtStorage.saveDigitalTwinState(digitalTwinState, currentChangeList);
//
//        Optional<DigitalTwinState> lastDigitalTwinStateOptional = wldtStorage.getLastDigitalTwinState();
//
//        //Check if the Last DT State is available in the Storage
//        assertTrue(lastDigitalTwinStateOptional.isPresent());
//
//        // Load the last DT State in the Storage
//        DigitalTwinState lastDigitalTwinState = lastDigitalTwinStateOptional.get();
//
//        // Test DT State Validity
//        testStatesStructure(digitalTwinState, lastDigitalTwinState);
//        testStateProperty(digitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
//        testStateProperty(lastDigitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Check Digital Twin State Change List associated to our State
//        List<DigitalTwinStateChange> storedChangeList = wldtStorage.getDigitalTwinStateChangeList(lastDigitalTwinState);
//        assertNotNull(currentChangeList);
//        assertFalse(currentChangeList.isEmpty());
//        assertNotNull(storedChangeList);
//        assertFalse(storedChangeList.isEmpty());
//        assertEquals(currentChangeList.size(), storedChangeList.size());
//
//    }
//
//    @Test
//    @Order(3)
//    public void checkDigitalTwinStateCount() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException, InterruptedException {
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Save Initial Digital Twin State
//        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//        int TARGET_STATE_UPDATE_COUNT = 10;
//
//        Thread.sleep(100);
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-checkDigitalTwinStateCount-r1-%d", TEST_VALUE_0002, i));
//
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//            Thread.sleep(100);
//        }
//
//        Thread.sleep(1000);
//
//        int targetUpdateCount = TARGET_STATE_UPDATE_COUNT+1;
//
//        System.out.println(digitalTwinStateManager.getDigitalTwinState());
//
//        assertEquals(targetUpdateCount, wldtStorage.getDigitalTwinStateCount());
//    }
//
//    @Test
//    @Order(4)
//    public void getDigitalTwinStateInTimeRange() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException, InterruptedException {
//
//        long startTimeStamp = System.currentTimeMillis();
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Save Initial Digital Twin State
//        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//        int TARGET_STATE_UPDATE_COUNT = 10;
//
//        Thread.sleep(100);
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger1-%d", TEST_VALUE_0002, i));
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//            Thread.sleep(100);
//        }
//
//        long middleTimeStamp = System.currentTimeMillis();
//
//        Thread.sleep(100);
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger2-%d", TEST_VALUE_0002, i));
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//            Thread.sleep(100);
//        }
//
//        Thread.sleep(1000);
//
//        long endTimeStamp = System.currentTimeMillis();
//
//        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;
//
//        assertEquals(TOTAL_TARGET_UPDATE_COUNT, wldtStorage.getDigitalTwinStateCount());
//
//        // Check Complete Range
//        List<DigitalTwinState> digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(startTimeStamp, endTimeStamp);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(TOTAL_TARGET_UPDATE_COUNT, digitalTwinStateList.size());
//
//        // Check First Block Range
//        digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(startTimeStamp, middleTimeStamp);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(TARGET_STATE_UPDATE_COUNT+1, digitalTwinStateList.size());
//
//        // Check Final Block Range
//        digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(middleTimeStamp, endTimeStamp);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(TARGET_STATE_UPDATE_COUNT, digitalTwinStateList.size());
//    }
//
//    @Test
//    @Order(5)
//    public void getDigitalTwinStateInRange() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException, InterruptedException {
//
//        //Create Property
//        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
//
//        // Save Initial Digital Twin State
//        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//
//        int TARGET_STATE_UPDATE_COUNT = 10;
//
//        Thread.sleep(100);
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger1-%d", TEST_VALUE_0002, i));
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//            Thread.sleep(100);
//        }
//
//        Thread.sleep(100);
//
//        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
//            // Create Property
//            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger2-%d", TEST_VALUE_0002, i));
//            // Save the new DT State
//            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
//            Thread.sleep(100);
//        }
//
//        Thread.sleep(1000);
//
//        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;
//
//        assertEquals(TOTAL_TARGET_UPDATE_COUNT, wldtStorage.getDigitalTwinStateCount());
//
//        // Check Complete Range
//        List<DigitalTwinState> digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(0, TOTAL_TARGET_UPDATE_COUNT-1);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(TOTAL_TARGET_UPDATE_COUNT, digitalTwinStateList.size());
//
//        // Check First Block Range
//        digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(0, 5);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(6, digitalTwinStateList.size());
//
//        // Check Final Block Range
//        digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(5, TOTAL_TARGET_UPDATE_COUNT-1);
//        assertNotNull(digitalTwinStateList);
//        assertFalse(digitalTwinStateList.isEmpty());
//        assertEquals(16, digitalTwinStateList.size());
//    }
//
//    private void addStateProperty(String propertyKey, String propertyValue) throws  WldtDigitalTwinStateException {
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
//    private void updateStateProperty(String propertyKey, String propertyValue) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {
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

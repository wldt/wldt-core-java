package it.wldt.storage;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.physical.*;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultStorageTest {

    private final String DIGITAL_TWIN_ID = "dt00001";
    private final String TEST_KEY_0001 = "testKey0001";
    private final String TEST_VALUE_0001 = "TEST-STRING";
    private final String TEST_VALUE_0002 = "TEST-STRING-2";

    private DigitalTwinStateManager digitalTwinStateManager;
    private WldtStorage wldtStorage;
    private DigitalTwinState digitalTwinState;
    private ArrayList<DigitalTwinStateChange> currentChangeList;

    private void addStateProperty(String propertyKey, String propertyValue) throws  WldtDigitalTwinStateException {

        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(propertyKey, propertyValue);
        property.setKey(propertyKey);
        property.setReadable(true);
        property.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(property);

        if(currentChangeList != null)
            currentChangeList.addAll(digitalTwinStateManager.getDigitalTwinStateTransaction().getDigitalTwinStateChangeList());

        digitalTwinStateManager.commitStateTransaction();
    }

    private void updateStateProperty(String propertyKey, String propertyValue) throws WldtDigitalTwinStateException {

        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(propertyKey, propertyValue);
        property.setKey(propertyKey);
        property.setReadable(true);
        property.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateProperty(property);

        if(currentChangeList != null)
            currentChangeList.addAll(digitalTwinStateManager.getDigitalTwinStateTransaction().getDigitalTwinStateChangeList());

        digitalTwinStateManager.commitStateTransaction();
    }

    private void testStatesStructure(DigitalTwinState referenceDigitalTwinState, DigitalTwinState targetdigitalTwinState) throws WldtDigitalTwinStatePropertyException {

        assertTrue(referenceDigitalTwinState.getPropertyList().isPresent());
        assertTrue(targetdigitalTwinState.getPropertyList().isPresent());
        assertEquals(referenceDigitalTwinState.getPropertyList().get().size(), targetdigitalTwinState.getPropertyList().get().size());

    }

    private void testStateProperty(DigitalTwinState digitalTwinState, String propertyKey, Object propertyValue) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        assertTrue(digitalTwinState.readProperty(propertyKey).isPresent());
        assertEquals(propertyValue, digitalTwinState.readProperty(propertyKey).get().getValue());

    }

    @BeforeEach
    public void initTest() throws WldtDigitalTwinStateException {
        digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        wldtStorage = new DefaultWldtStorage("test_storage_id", true);
        currentChangeList = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() throws StorageException {
        digitalTwinStateManager = null;
        wldtStorage.clear();
        wldtStorage = null;
        digitalTwinState = null;
        currentChangeList = null;
    }

    @Test
    @Order(1)
    public void saveDigitalTwinStateNoChangeList() throws WldtDigitalTwinStateException, StorageException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Read the current DT State
        digitalTwinState = digitalTwinStateManager.getDigitalTwinState();

        wldtStorage.saveDigitalTwinState(digitalTwinState, null);

        Optional<DigitalTwinState> lastDigitalTwinStateOptional = wldtStorage.getLastDigitalTwinState();

        //Check if the Last DT State is available in the Storage
        assertTrue(lastDigitalTwinStateOptional.isPresent());

        // Load the last DT State in the Storage
        DigitalTwinState lastDigitalTwinState = lastDigitalTwinStateOptional.get();

        // Test DT State Validity
        testStatesStructure(digitalTwinState, lastDigitalTwinState);
        testStateProperty(digitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
        testStateProperty(lastDigitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
    }

    @Test
    @Order(2)
    public void saveDigitalTwinStateWithChangeList() throws StorageException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyNotFoundException, WldtDigitalTwinStateException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Read the current DT State
        digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        wldtStorage.saveDigitalTwinState(digitalTwinState, currentChangeList);

        Optional<DigitalTwinState> lastDigitalTwinStateOptional = wldtStorage.getLastDigitalTwinState();

        //Check if the Last DT State is available in the Storage
        assertTrue(lastDigitalTwinStateOptional.isPresent());

        // Load the last DT State in the Storage
        DigitalTwinState lastDigitalTwinState = lastDigitalTwinStateOptional.get();

        // Test DT State Validity
        testStatesStructure(digitalTwinState, lastDigitalTwinState);
        testStateProperty(digitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);
        testStateProperty(lastDigitalTwinState, TEST_KEY_0001, TEST_VALUE_0001);

        // Check Digital Twin State Change List associated to our State
        List<DigitalTwinStateChange> storedChangeList = wldtStorage.getDigitalTwinStateChangeList(lastDigitalTwinState);
        assertNotNull(currentChangeList);
        assertFalse(currentChangeList.isEmpty());
        assertNotNull(storedChangeList);
        assertFalse(storedChangeList.isEmpty());
        assertEquals(currentChangeList.size(), storedChangeList.size());

    }

    @Test
    @Order(3)
    public void checkDigitalTwinStateCount() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State
        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Create Property
            updateStateProperty(TEST_KEY_0001, String.format("%s-checkDigitalTwinStateCount-r1-%d", TEST_VALUE_0002, i));

            // Save the new DT State
            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);

            Thread.sleep(100);
        }

        Thread.sleep(1000);

        int targetUpdateCount = TARGET_STATE_UPDATE_COUNT+1;

        System.out.println(digitalTwinStateManager.getDigitalTwinState());

        assertEquals(targetUpdateCount, wldtStorage.getDigitalTwinStateCount());
    }

    @Test
    @Order(4)
    public void getDigitalTwinStateInTimeRange() throws WldtDigitalTwinStateException, InterruptedException, StorageException {

        long startTimeStamp = System.currentTimeMillis();

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State
        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Create Property
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger1-%d", TEST_VALUE_0002, i));
            // Save the new DT State
            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
            Thread.sleep(100);
        }

        long middleTimeStamp = System.currentTimeMillis();

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Create Property
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger2-%d", TEST_VALUE_0002, i));
            // Save the new DT State
            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
            Thread.sleep(100);
        }

        Thread.sleep(1000);

        long endTimeStamp = System.currentTimeMillis();

        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;

        assertEquals(TOTAL_TARGET_UPDATE_COUNT, wldtStorage.getDigitalTwinStateCount());

        // Check Complete Range
        List<DigitalTwinState> digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(startTimeStamp, endTimeStamp);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(TOTAL_TARGET_UPDATE_COUNT, digitalTwinStateList.size());

        // Check First Block Range
        digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(startTimeStamp, middleTimeStamp);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(TARGET_STATE_UPDATE_COUNT+1, digitalTwinStateList.size());

        // Check Final Block Range
        digitalTwinStateList = wldtStorage.getDigitalTwinStateInTimeRange(middleTimeStamp, endTimeStamp);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(TARGET_STATE_UPDATE_COUNT, digitalTwinStateList.size());
    }

    @Test
    @Order(5)
    public void getDigitalTwinStateInRange() throws WldtDigitalTwinStateException, InterruptedException, StorageException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State
        wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Create Property
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger1-%d", TEST_VALUE_0002, i));
            // Save the new DT State
            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
            Thread.sleep(100);
        }

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Create Property
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger2-%d", TEST_VALUE_0002, i));
            // Save the new DT State
            wldtStorage.saveDigitalTwinState(digitalTwinStateManager.getDigitalTwinState(), null);
            Thread.sleep(100);
        }

        Thread.sleep(1000);

        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;

        assertEquals(TOTAL_TARGET_UPDATE_COUNT, wldtStorage.getDigitalTwinStateCount());

        // Check Complete Range
        List<DigitalTwinState> digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(0, TOTAL_TARGET_UPDATE_COUNT-1);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(TOTAL_TARGET_UPDATE_COUNT, digitalTwinStateList.size());

        // Check First Block Range
        digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(0, 5);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(6, digitalTwinStateList.size());

        // Check Final Block Range
        digitalTwinStateList = wldtStorage.getDigitalTwinStateInRange(5, TOTAL_TARGET_UPDATE_COUNT-1);
        assertNotNull(digitalTwinStateList);
        assertFalse(digitalTwinStateList.isEmpty());
        assertEquals(16, digitalTwinStateList.size());
    }

    @Test
    @Order(6)
    public void testLifeCycleStorage() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int LIFE_CYCLE_STATE_CHANGE_NUMBER = 50;
        for(int i=0; i<LIFE_CYCLE_STATE_CHANGE_NUMBER; i++){

            // Pickup a random state
            LifeCycleState[] states = LifeCycleState.values();
            int randomIndex = new Random().nextInt(states.length);

            // Save the LifeCycle State
            wldtStorage.saveLifeCycleState(new LifeCycleStateVariation(System.currentTimeMillis(), states[randomIndex]));

            Thread.sleep(100);
        }

        // Check the number of LifeCycle States
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, wldtStorage.getLifeCycleStateCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the LifeCycle States in the complete range
        Map<Long, LifeCycleState> lifeCycleStateMap = wldtStorage.getLifeCycleStateInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of LifeCycle States stored in the target range
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, lifeCycleStateMap.size());

        // Check validity of the LifeCycle States in the target range
        for(Map.Entry<Long, LifeCycleState> entry : lifeCycleStateMap.entrySet()){
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
        }

        // Retrieve the LifeCycle States in the target range
        Map<Long, LifeCycleState> lifeCycleStateMapInRange = wldtStorage.getLifeCycleStateInRange(0, LIFE_CYCLE_STATE_CHANGE_NUMBER-1);

        // Check the number of LifeCycle States stored in the target range
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, lifeCycleStateMapInRange.size());

        // Check validity of the LifeCycle States in the target range
        for(Map.Entry<Long, LifeCycleState> entry : lifeCycleStateMapInRange.entrySet()){
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
        }
    }

    @Test
    @Order(7)
    public void testPhysicalAssetActionRequest() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalAssetActionRequest physicalAssetActionRequest = new PhysicalAssetActionRequest(System.currentTimeMillis(),
                    "test-action-key",
                    "test-request-body",
                    null);

            // Save the Physical Asset Action Request
            wldtStorage.savePhysicalAssetActionRequest(physicalAssetActionRequest);

            Thread.sleep(100);
        }

        // Check the number of Physical Asset Action Request
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getPhysicalAssetActionRequestCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Physical Asset Action Requests in the target range
        List<PhysicalAssetActionRequest> resultList = wldtStorage.getPhysicalAssetActionRequestInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Action Requests in the target range
        for(PhysicalAssetActionRequest entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }

        // Retrieve the Physical Asset Action Requests in the target range
        resultList = wldtStorage.getPhysicalAssetActionRequestInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Action Requests in the target range
        for(PhysicalAssetActionRequest entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }
    }

    @Test
    @Order(8)
    public void testDigitalActionRequest() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            DigitalActionRequest digitalActionRequest = new DigitalActionRequest(System.currentTimeMillis(),
                    "test-action-key",
                    "test-request-body",
                    null);

            // Save the Digital Action Request
            wldtStorage.saveDigitalActionRequest(digitalActionRequest);

            Thread.sleep(100);
        }

        // Check the number of Digital Action Request
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getDigitalActionRequestCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<DigitalActionRequest> resultList = wldtStorage.getDigitalActionRequestInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of Digital Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Digital Action Requests in the target range
        for(DigitalActionRequest entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }

        // Retrieve the Digital Action Requests in the target range
        resultList = wldtStorage.getDigitalActionRequestInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of Digital Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Digital Action Requests in the target range
        for(DigitalActionRequest entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }
    }

    @Test
    @Order(9)
    public void testNewPhysicalAssetDescriptionStorage() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification = new PhysicalAssetDescriptionNotification(
                    System.currentTimeMillis(),
                    "test-adapter",
                    new PhysicalAssetDescription(
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>()));

            // Save the Physical Asset Description Notification
            wldtStorage.saveNewPhysicalAssetDescriptionNotification(physicalAssetDescriptionNotification);

            Thread.sleep(100);
        }

        // Check the number of Physical Asset Description Notification
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getNewPhysicalAssetDescriptionNotificationCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<PhysicalAssetDescriptionNotification> resultList = wldtStorage.getNewPhysicalAssetDescriptionNotificationInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }

        // Retrieve the Physical Asset Description Notification in the target range
        resultList = wldtStorage.getNewPhysicalAssetDescriptionNotificationInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of thePhysical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }
    }

    @Test
    @Order(10)
    public void testUpdatedPhysicalAssetDescriptionStorage() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification = new PhysicalAssetDescriptionNotification(
                    System.currentTimeMillis(),
                    "test-adapter",
                    new PhysicalAssetDescription(
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new ArrayList<>()));

            // Save the Physical Asset Description Notification
            wldtStorage.saveUpdatedPhysicalAssetDescriptionNotification(physicalAssetDescriptionNotification);

            Thread.sleep(100);
        }

        // Check the number of Physical Asset Description Notification
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<PhysicalAssetDescriptionNotification> resultList = wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }

        // Retrieve the Physical Asset Description Notification in the target range
        resultList = wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of thePhysical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }
    }

    @Test
    @Order(11)
    public void testPhysicalPropertyVariation() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalAssetPropertyVariation newData = new PhysicalAssetPropertyVariation(
                    System.currentTimeMillis(),
                    "property-test",
                    "variation-body-test",
                    null);

            // Save the Physical Asset Description Notification
            wldtStorage.savePhysicalAssetPropertyVariation(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getPhysicalAssetPropertyVariationCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalAssetPropertyVariation> resultList = wldtStorage.getPhysicalAssetPropertyVariationInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetPropertyVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPropertykey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = wldtStorage.getPhysicalAssetPropertyVariationInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetPropertyVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPropertykey());
            assertNotNull(entry.getBody());
        }
    }

    @Test
    @Order(12)
    public void testPhysicalAssetRelationshipInstanceCreated() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        PhysicalAssetRelationship<String> relationship = new PhysicalAssetRelationship<>("test-relationship", "test-relationship-type");

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalRelationshipInstanceVariation newData = new PhysicalRelationshipInstanceVariation(
                    System.currentTimeMillis(),
                    new PhysicalAssetRelationshipInstance<String>(relationship,
                            String.format("%s-%d", "test-relationship-target", i), null));

            // Save the Physical Asset Description Notification
            wldtStorage.savePhysicalAssetRelationshipInstanceCreatedEvent(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getPhysicalAssetRelationshipInstanceCreatedEventCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalRelationshipInstanceVariation> resultList = wldtStorage.getPhysicalAssetRelationshipInstanceCreatedEventInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPhysicalAssetRelationshipInstance());
        }

        // Retrieve the entities in the target range
        resultList = wldtStorage.getPhysicalAssetRelationshipInstanceCreatedEventInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPhysicalAssetRelationshipInstance());
        }
    }

    @Test
    @Order(13)
    public void testPhysicalAssetRelationshipInstanceDeleted() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        PhysicalAssetRelationship<String> relationship = new PhysicalAssetRelationship<>("test-relationship", "test-relationship-type");

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalRelationshipInstanceVariation newData = new PhysicalRelationshipInstanceVariation(
                    System.currentTimeMillis(),
                    new PhysicalAssetRelationshipInstance<String>(relationship,
                            String.format("%s-%d", "test-relationship-target", i), null));

            // Save the Physical Asset Description Notification
            wldtStorage.savePhysicalAssetRelationshipInstanceDeletedEvent(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getPhysicalAssetRelationshipInstanceDeletedEventCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalRelationshipInstanceVariation> resultList = wldtStorage.getPhysicalAssetRelationshipInstanceDeletedEventInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPhysicalAssetRelationshipInstance());
        }

        // Retrieve the entities in the target range
        resultList = wldtStorage.getPhysicalAssetRelationshipInstanceDeletedEventInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariation entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPhysicalAssetRelationshipInstance());
        }
    }

    @Test
    @Order(14)
    public void testDigitalTwinStateEventNotification() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            DigitalTwinStateEventNotification<String> newData = new DigitalTwinStateEventNotification(
                    "event-key",
                    "test-event-body",
                    System.currentTimeMillis());

            // Save the Physical Asset Description Notification
            wldtStorage.saveDigitalTwinStateEventNotification(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getDigitalTwinStateEventNotificationCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<DigitalTwinStateEventNotification<?>> resultList = wldtStorage.getDigitalTwinStateEventNotificationInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(DigitalTwinStateEventNotification<?> entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getDigitalEventKey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = wldtStorage.getDigitalTwinStateEventNotificationInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(DigitalTwinStateEventNotification<?> entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getDigitalEventKey());
            assertNotNull(entry.getBody());
        }
    }
    @Test
    @Order(15)
    public void testPhsyicalEventNotification() throws StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random LifeCycle States
        int EVENT_TEST_NUMBER = 50;
        for(int i=0; i<EVENT_TEST_NUMBER; i++){

            PhysicalAssetEventNotification newData = new PhysicalAssetEventNotification(
                    System.currentTimeMillis(),
                    "event-key",
                    "test-event-body",
                    null);

            // Save the Physical Asset Description Notification
            wldtStorage.savePhysicalAssetEventNotification(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, wldtStorage.getPhysicalAssetEventNotificationCount());

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalAssetEventNotification> resultList = wldtStorage.getPhysicalAssetEventNotificationInTimeRange(startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetEventNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventkey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = wldtStorage.getPhysicalAssetEventNotificationInRange(0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetEventNotification entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventkey());
            assertNotNull(entry.getBody());
        }
    }
}

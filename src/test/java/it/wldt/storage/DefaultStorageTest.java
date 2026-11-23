package it.wldt.storage;

import it.wldt.storage.query.QueryResourceType;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.physical.*;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.storage.model.digital.DigitalActionRequestRecord;
import it.wldt.storage.model.lifecycle.LifeCycleVariationRecord;
import it.wldt.storage.model.physical.*;
import it.wldt.storage.model.state.DigitalTwinStateEventNotificationRecord;
import it.wldt.storage.model.state.DigitalTwinStateDescriptionRecord;
import it.wldt.storage.model.state.DigitalTwinStateHistoryEntry;
import it.wldt.storage.model.state.DigitalTwinStateHistoryRecord;
import it.wldt.storage.model.state.DigitalTwinStateValuesRecord;
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

    private void saveCurrentDescription(List<DigitalTwinStateChange> changes) throws StorageException {
        wldtStorage.saveDigitalTwinStateDescription(digitalTwinStateManager.getDigitalTwinStateDescription(), changes);
    }

    private void saveCurrentValues(List<DigitalTwinStateChange> changes) throws StorageException {
        wldtStorage.saveDigitalTwinStateValues(digitalTwinStateManager.getDigitalTwinStateValues(), null, changes);
    }

    private void testStateProperty(DigitalTwinStateValuesRecord valuesRecord, String propertyKey, Object propertyValue) {
        assertTrue(valuesRecord.getValues().getPropertyValues().containsKey(propertyKey));
        assertEquals(propertyValue, valuesRecord.getValues().getPropertyValue(propertyKey));
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
        currentChangeList = null;
    }

    @Test
    @Order(1)
    public void saveDigitalTwinStateNoChangeList() throws WldtDigitalTwinStateException, StorageException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save the current description and values
        saveCurrentDescription(null);
        saveCurrentValues(null);

        DigitalTwinStateDescriptionRecord lastDescriptionRecord = StorageTestUtils.<DigitalTwinStateDescriptionRecord>last(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION);
        DigitalTwinStateValuesRecord lastValuesRecord = StorageTestUtils.<DigitalTwinStateValuesRecord>last(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES);

        //Check if the Last description and values are available in the Storage
        assertNotNull(lastDescriptionRecord);
        assertNotNull(lastValuesRecord);

        // Test the description and its id
        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription().getId(), lastDescriptionRecord.getDescriptionId());
        assertEquals(1, lastDescriptionRecord.getDescription().getProperties().size());
        assertEquals(TEST_KEY_0001, lastDescriptionRecord.getDescription().getProperties().get(0).getKey());

        // The values are saved together with the id of the associated description
        assertEquals(lastDescriptionRecord.getDescriptionId(), lastValuesRecord.getDescriptionId());
        assertEquals(lastDescriptionRecord.getDescriptionId(), lastValuesRecord.getValues().getDescriptionId());
        testStateProperty(lastValuesRecord, TEST_KEY_0001, TEST_VALUE_0001);
    }

    @Test
    @Order(2)
    public void saveDigitalTwinStateWithChangeList() throws StorageException, WldtDigitalTwinStateException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        saveCurrentDescription(currentChangeList);
        saveCurrentValues(currentChangeList);

        DigitalTwinStateDescriptionRecord lastDescriptionRecord = StorageTestUtils.<DigitalTwinStateDescriptionRecord>last(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION);
        DigitalTwinStateValuesRecord lastValuesRecord = StorageTestUtils.<DigitalTwinStateValuesRecord>last(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES);

        assertNotNull(lastDescriptionRecord);
        assertNotNull(lastValuesRecord);
        testStateProperty(lastValuesRecord, TEST_KEY_0001, TEST_VALUE_0001);

        // Check Digital Twin State Change List associated to our description and values
        assertNotNull(currentChangeList);
        assertFalse(currentChangeList.isEmpty());

        List<DigitalTwinStateChange> storedDescriptionChangeList = lastDescriptionRecord.getStateChangeList();
        assertNotNull(storedDescriptionChangeList);
        assertEquals(currentChangeList.size(), storedDescriptionChangeList.size());

        List<DigitalTwinStateChange> storedValuesChangeList = lastValuesRecord.getStateChangeList();
        assertNotNull(storedValuesChangeList);
        assertEquals(currentChangeList.size(), storedValuesChangeList.size());
    }

    @Test
    @Order(3)
    public void checkDigitalTwinStateCount() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State (description and values)
        saveCurrentDescription(null);
        saveCurrentValues(null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            // Update the property
            updateStateProperty(TEST_KEY_0001, String.format("%s-checkDigitalTwinStateCount-r1-%d", TEST_VALUE_0002, i));

            // Save the new values (the structure is the same)
            saveCurrentValues(null);

            Thread.sleep(100);
        }

        Thread.sleep(1000);

        int targetUpdateCount = TARGET_STATE_UPDATE_COUNT+1;

        assertEquals(targetUpdateCount, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES));
        assertEquals(1, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION));

        // The complete state is a single history record with one description and all its values
        List<DigitalTwinStateHistoryRecord> history = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE, 0, System.currentTimeMillis());
        assertEquals(1, history.size());
        assertEquals(1, history.get(0).size());
        DigitalTwinStateHistoryEntry entry = history.get(0).getEntries().values().iterator().next();
        assertEquals(targetUpdateCount, entry.getValuesList().size());
        assertEquals(1, entry.getDescription().getProperties().size());
    }

    @Test
    @Order(4)
    public void getDigitalTwinStateInTimeRange() throws WldtDigitalTwinStateException, InterruptedException, StorageException {

        long startTimeStamp = System.currentTimeMillis();

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State
        saveCurrentDescription(null);
        saveCurrentValues(null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger1-%d", TEST_VALUE_0002, i));
            saveCurrentValues(null);
            Thread.sleep(100);
        }

        long middleTimeStamp = System.currentTimeMillis();

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInTimeRanger2-%d", TEST_VALUE_0002, i));
            saveCurrentValues(null);
            Thread.sleep(100);
        }

        Thread.sleep(1000);

        long endTimeStamp = System.currentTimeMillis();

        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;

        assertEquals(TOTAL_TARGET_UPDATE_COUNT, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES));

        // Check Complete Range
        List<DigitalTwinStateValuesRecord> valuesList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, startTimeStamp, endTimeStamp);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(TOTAL_TARGET_UPDATE_COUNT, valuesList.size());

        // Check First Block Range
        valuesList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, startTimeStamp, middleTimeStamp);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(TARGET_STATE_UPDATE_COUNT+1, valuesList.size());

        // Check Final Block Range
        valuesList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, middleTimeStamp, endTimeStamp);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(TARGET_STATE_UPDATE_COUNT, valuesList.size());

        // The complete state in the interval: one description with the values of the interval
        List<DigitalTwinStateHistoryRecord> history = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE, middleTimeStamp, endTimeStamp);
        assertEquals(1, history.size());
        assertEquals(1, history.get(0).size());
        assertEquals(TARGET_STATE_UPDATE_COUNT, history.get(0).getEntries().values().iterator().next().getValuesList().size());
    }

    @Test
    @Order(5)
    public void getDigitalTwinStateInRange() throws WldtDigitalTwinStateException, InterruptedException, StorageException {

        //Create Property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);

        // Save Initial Digital Twin State
        saveCurrentDescription(null);
        saveCurrentValues(null);

        int TARGET_STATE_UPDATE_COUNT = 10;

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger1-%d", TEST_VALUE_0002, i));
            saveCurrentValues(null);
            Thread.sleep(100);
        }

        Thread.sleep(100);

        for(int i=0; i<TARGET_STATE_UPDATE_COUNT; i++){
            updateStateProperty(TEST_KEY_0001, String.format("%s-getDigitalTwinStateInRanger2-%d", TEST_VALUE_0002, i));
            saveCurrentValues(null);
            Thread.sleep(100);
        }

        Thread.sleep(1000);

        int TOTAL_TARGET_UPDATE_COUNT = 2*TARGET_STATE_UPDATE_COUNT+1;

        assertEquals(TOTAL_TARGET_UPDATE_COUNT, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES));

        // Check Complete Range
        List<DigitalTwinStateValuesRecord> valuesList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, 0, TOTAL_TARGET_UPDATE_COUNT-1);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(TOTAL_TARGET_UPDATE_COUNT, valuesList.size());

        // Check First Block Range
        valuesList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, 0, 5);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(6, valuesList.size());

        // Check Final Block Range
        valuesList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_VALUES, 5, TOTAL_TARGET_UPDATE_COUNT-1);
        assertNotNull(valuesList);
        assertFalse(valuesList.isEmpty());
        assertEquals(16, valuesList.size());
    }

    @Test
    @Order(5)
    public void completeStateQueryGroupsTheValuesByDescription() throws WldtDigitalTwinStateException, InterruptedException, StorageException {

        // First description: one property
        addStateProperty(TEST_KEY_0001, TEST_VALUE_0001);
        saveCurrentDescription(null);
        saveCurrentValues(null);
        String firstDescriptionId = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        Thread.sleep(20);
        updateStateProperty(TEST_KEY_0001, TEST_VALUE_0002);
        saveCurrentValues(null);

        // The structure changes: a new property creates a new description
        Thread.sleep(20);
        addStateProperty("testKey0002", "second");
        saveCurrentDescription(null);
        saveCurrentValues(null);
        String secondDescriptionId = digitalTwinStateManager.getDigitalTwinStateDescription().getId();
        assertNotEquals(firstDescriptionId, secondDescriptionId);

        List<DigitalTwinStateHistoryRecord> history = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE, 0, System.currentTimeMillis() + 1000);

        // A single history record with one entry for each description, each one with its own values
        assertEquals(1, history.size());
        DigitalTwinStateHistoryRecord record = history.get(0);
        assertEquals(2, record.size());
        assertEquals(Arrays.asList(firstDescriptionId, secondDescriptionId), new ArrayList<>(record.getEntries().keySet()));
        assertEquals(1, record.getEntry(firstDescriptionId).getDescription().getProperties().size());
        assertEquals(2, record.getValuesList(firstDescriptionId).size());
        assertEquals(2, record.getEntry(secondDescriptionId).getDescription().getProperties().size());
        assertEquals(1, record.getValuesList(secondDescriptionId).size());
        assertEquals(2, record.getValuesList(secondDescriptionId).get(0).getValues().getPropertyValues().size());
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
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.LIFE_CYCLE_EVENT));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the LifeCycle States in the complete range
        List<LifeCycleVariationRecord> lifeCycleStateMap = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.LIFE_CYCLE_EVENT, startTimeStamp, endTimeStamp);

        // Check the number of LifeCycle States stored in the target range
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, lifeCycleStateMap.size());

        // Check validity of the LifeCycle States in the target range
        for(LifeCycleVariationRecord entry : lifeCycleStateMap) {
            assertNotNull(entry);
            assertNotNull(entry.getLifeCycleState());
        }

        // Retrieve the LifeCycle States in the target range
        List<LifeCycleVariationRecord> lifeCycleStateMapInRange = StorageTestUtils.inRange(wldtStorage, QueryResourceType.LIFE_CYCLE_EVENT, 0, LIFE_CYCLE_STATE_CHANGE_NUMBER-1);

        // Check the number of LifeCycle States stored in the target range
        assertEquals(LIFE_CYCLE_STATE_CHANGE_NUMBER, lifeCycleStateMapInRange.size());

        // Check validity of the LifeCycle States in the target range
        for(LifeCycleVariationRecord entry : lifeCycleStateMapInRange) {
            assertNotNull(entry);
            assertNotNull(entry.getLifeCycleState());
        }
    }

    @Test
    @Order(7)
    public void testPhysicalAssetActionRequest() throws WldtDigitalTwinStateException, StorageException, InterruptedException {

        // Save Initial experiment time
        long startTimeStamp = System.currentTimeMillis();

        // Generate 50 different random action
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.PHYSICAL_ACTION_REQUEST));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Physical Asset Action Requests in the target range
        List<PhysicalAssetActionRequestRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.PHYSICAL_ACTION_REQUEST, startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Action Requests in the target range
        for(PhysicalAssetActionRequestRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }

        // Retrieve the Physical Asset Action Requests in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.PHYSICAL_ACTION_REQUEST, 0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Action Requests in the target range
        for(PhysicalAssetActionRequestRecord entry : resultList){
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_ACTION_REQUEST));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<DigitalActionRequestRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_ACTION_REQUEST, startTimeStamp, endTimeStamp);

        // Check the number of Digital Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Digital Action Requests in the target range
        for(DigitalActionRequestRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getActionkey());
            assertNotNull(entry.getRequestBody());
        }

        // Retrieve the Digital Action Requests in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.DIGITAL_ACTION_REQUEST, 0, EVENT_TEST_NUMBER-1);

        // Check the number of Digital Action Requests stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Digital Action Requests in the target range
        for(DigitalActionRequestRecord entry : resultList){
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.NEW_PAD_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<PhysicalAssetDescriptionNotificationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.NEW_PAD_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }

        // Retrieve the Physical Asset Description Notification in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.NEW_PAD_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of thePhysical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotificationRecord entry : resultList){
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.UPDATED_PAD_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the Digital Action Requests in the target range
        List<PhysicalAssetDescriptionNotificationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.UPDATED_PAD_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the Physical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getAdapterId());
            assertNotNull(entry.getPhysicalAssetDescription());
            assertNotNull(entry.getPhysicalAssetDescription().getActions());
            assertNotNull(entry.getPhysicalAssetDescription().getProperties());
            assertNotNull(entry.getPhysicalAssetDescription().getEvents());
        }

        // Retrieve the Physical Asset Description Notification in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.UPDATED_PAD_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of Physical Asset Description Notification stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of thePhysical Asset Description Notification in the target range
        for(PhysicalAssetDescriptionNotificationRecord entry : resultList){
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalAssetPropertyVariationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION, startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetPropertyVariationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getPropertykey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetPropertyVariationRecord entry : resultList){
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
            wldtStorage.savePhysicalAssetRelationshipInstanceCreatedNotification(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalRelationshipInstanceVariationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getInstanceKey());
            assertNotNull(entry.getInstanceTargetId());
            assertNotNull(entry.getRelationshipName());
            assertNotNull(entry.getRelationshipType());
        }

        // Retrieve the entities in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getInstanceKey());
            assertNotNull(entry.getInstanceTargetId());
            assertNotNull(entry.getRelationshipName());
            assertNotNull(entry.getRelationshipType());
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
            wldtStorage.savePhysicalAssetRelationshipInstanceDeletedNotification(newData);

            Thread.sleep(100);
        }

        // Check the number of entities
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalRelationshipInstanceVariationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getInstanceKey());
            assertNotNull(entry.getInstanceTargetId());
            assertNotNull(entry.getRelationshipName());
            assertNotNull(entry.getRelationshipType());
        }

        // Retrieve the entities in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalRelationshipInstanceVariationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getInstanceKey());
            assertNotNull(entry.getInstanceTargetId());
            assertNotNull(entry.getRelationshipName());
            assertNotNull(entry.getRelationshipType());
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_EVENT_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<DigitalTwinStateEventNotificationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_EVENT_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(DigitalTwinStateEventNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventKey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.DIGITAL_TWIN_STATE_EVENT_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(DigitalTwinStateEventNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventKey());
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
        assertEquals(EVENT_TEST_NUMBER, StorageTestUtils.count(wldtStorage, QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION));

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        // Check the entities in the target range
        List<PhysicalAssetEventNotificationRecord> resultList = StorageTestUtils.inTimeRange(wldtStorage, QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION, startTimeStamp, endTimeStamp);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetEventNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventkey());
            assertNotNull(entry.getBody());
        }

        // Retrieve the entities in the target range
        resultList = StorageTestUtils.inRange(wldtStorage, QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION, 0, EVENT_TEST_NUMBER-1);

        // Check the number of entities stored in the target range
        assertEquals(EVENT_TEST_NUMBER, resultList.size());

        // Check validity of the entities in the target range
        for(PhysicalAssetEventNotificationRecord entry : resultList){
            assertNotNull(entry);
            assertNotNull(entry.getEventkey());
            assertNotNull(entry.getBody());
        }
    }
}

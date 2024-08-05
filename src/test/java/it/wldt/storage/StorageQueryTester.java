package it.wldt.storage;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.EventManager;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.observer.IWldtEventObserverListener;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.exception.*;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoShadowingFunction;
import it.wldt.storage.model.digital.DigitalActionRequestRecord;
import it.wldt.storage.model.lifecycle.LifeCycleVariationRecord;
import it.wldt.storage.model.physical.*;
import it.wldt.storage.model.state.DigitalTwinStateRecord;
import it.wldt.storage.query.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageQueryTester {

    private static final Logger logger = LoggerFactory.getLogger(StorageQueryTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private static final String DEFAULT_STORAGE_ID = "default-storage";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoDigitalAdapter digitalAdapter = null;

    private DemoPhysicalAdapter physicalAdapter = null;

    // Save Initial experiment time
    private long startTimeStamp = System.currentTimeMillis();

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, StorageException {

        logger.info("Setting up Test Environment ...");

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoShadowingFunction());

        // Physical Adapter with Configuration with Relationship Enabled
        physicalAdapter = new DemoPhysicalAdapter(
                String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-physical-adapter"),
                new DemoPhysicalAdapterConfiguration(),
                true,
                true);

        digitalTwin.addPhysicalAdapter(physicalAdapter);

        // Add a new Default Storage Instance to the Digital Twin Storage Manager to observe all the events
        digitalTwin.getStorageManager().putStorage(new DefaultWldtStorage(DEFAULT_STORAGE_ID, true));

        // Digital Adapter with Configuration
        digitalAdapter = new DemoDigitalAdapter(
                String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-digital-adapter"),
                new DemoDigitalAdapterConfiguration()
        );

        digitalTwin.addDigitalAdapter(digitalAdapter);

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(TEST_DIGITAL_TWIN_ID);

        // Add the Twin to the Engine
        digitalTwinEngine.addDigitalTwin(digitalTwin);

        // Start the Digital Twin
        digitalTwinEngine.startDigitalTwin(TEST_DIGITAL_TWIN_ID);

    }

    @AfterEach
    public void tearDown() throws WldtEngineException, EventBusException {

        logger.info("Cleaning up Test Environment ...");

        if(digitalTwinEngine != null && digitalTwin != null && digitalTwinEngine.getDigitalTwinCount() > 0) {
            digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
            digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
            digitalTwin = null;
            digitalTwinEngine = null;
        }

        SharedTestMetrics.getInstance().resetMetrics();
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID);
    }

    @Test
    @Order(1)
    public void testDigitalTwinStateUpdates() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        WldtEventObserver queryResultObserver = new WldtEventObserver(TEST_DIGITAL_TWIN_ID,
                "query-result-observe",
                new IWldtEventObserverListener() {
                    @Override
                    public void onEventSubscribed(String eventType) {

                    }

                    @Override
                    public void onEventUnSubscribed(String eventType) {

                    }

                    @Override
                    public void onStateEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onPhysicalAssetEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onPhysicalAssetActionEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onDigitalActionEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onPhysicalAssetDescriptionEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onLifeCycleEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onQueryRequestEvent(WldtEvent<?> wldtEvent) {

                    }

                    @Override
                    public void onQueryResultEvent(WldtEvent<?> wldtEvent) {
                        logger.info("Received Query Result Event: {}", wldtEvent);
                    }
                });

        // Observe the Query Result Events
        queryResultObserver.observeStorageQueryResultEvents();

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        // Send the Query Request to the Storage Manager for the target DT
        EventManager.publishStorageQueryRequest(TEST_DIGITAL_TWIN_ID, "tester_publisher_id", queryRequest);

        Thread.sleep(10000);
    }

    @Test
    @Order(2)
    public void testQueryExecutorSync() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

    }

    @Test
    @Order(3)
    public void testQueryExecutorAsync() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        final QueryResult<?>[] receivedQueryResult = {null};

        // Send the Query Request to the Storage Manager for the target DT
        queryExecutor.asyncQueryExecute(queryRequest, new IQueryResultListener() {
            @Override
            public void onQueryResult(QueryResult<?> queryResult) {
                receivedQueryResult[0] = queryResult;
            }
        });

        assertNotNull(receivedQueryResult[0]);
        assertEquals(receivedQueryResult[0].getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(receivedQueryResult[0].getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        Thread.sleep(10000);
    }

    @Test
    @Order(4)
    public void testSyncStateQuery() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        //Thread.sleep(10000);
    }

    @Test
    @Order(5)
    public void testSyncStateRangeQueries() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof DigitalTwinStateRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes + 1 Initial State Update
        int targetStateUpdates = TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + 4 + 1;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof DigitalTwinStateRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes
        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(6)
    public void testSyncPhysicalPropertyVariationQueries() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetPropertyVariationRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes + 1 Initial State Update
        int targetStateUpdates = TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetPropertyVariationRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes
        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(7)
    public void testSyncPhysicalEventNotificationQueries() throws InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetEventNotificationRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes + 1 Initial State Update
        int targetStateUpdates = TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetEventNotificationRecord));

        // we have 10 Energy Updates, 4 Relationship Instance changes
        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(8)
    public void testSyncPhysicalActionQueries() throws InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ACTION_REQUEST);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetActionRequestRecord));

        // we have 2 Actions
        int targetStateUpdates = 2;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ACTION_REQUEST);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_ACTION_REQUEST);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetActionRequestRecord));

        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(9)
    public void testSyncDigitalActionQueries() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_ACTION_REQUEST);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof DigitalActionRequestRecord));

        // we have 2 Actions
        int targetStateUpdates = 2;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_ACTION_REQUEST);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_ACTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_ACTION_REQUEST);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof DigitalActionRequestRecord));

        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(10)
    public void testSyncPadNotificationQueries() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.NEW_PAD_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.NEW_PAD_NOTIFICATION);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetDescriptionNotificationRecord));

        // we have 1 PAD Published by the Physical Adapter
        int targetStateUpdates = 1;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.NEW_PAD_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.NEW_PAD_NOTIFICATION);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.NEW_PAD_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.NEW_PAD_NOTIFICATION);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalAssetDescriptionNotificationRecord));

        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(11)
    public void testSyncRelationshipInstanceCreatedQuery() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalRelationshipInstanceVariationRecord));

        // we have 2 Relationships Created
        int targetStateUpdates = 2;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalRelationshipInstanceVariationRecord));

        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(12)
    public void testSyncRelationshipInstanceDeletedQuery() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalRelationshipInstanceVariationRecord));

        // we have 2 Relationships Created
        int targetStateUpdates = 2;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only DigitalTwinStateRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof PhysicalRelationshipInstanceVariationRecord));

        assertEquals(targetStateUpdates, resultList.size());

    }

    @Test
    @Order(13)
    public void testSyncLifeCycleQuery() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Save Final experiment time
        long endTimeStamp = System.currentTimeMillis();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

        /////////////////// Query in Time Range //////////////////////

        // Create Query Request to the Storage Manager
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.LIFE_CYCLE_EVENT);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(endTimeStamp);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.TIME_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.LIFE_CYCLE_EVENT);

        List<?> resultList =  queryResult.getResults();

        // Check the result list is not null and contains only the correct instance type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof LifeCycleVariationRecord));

        // The current DT is not stopped or destroyed, the target States are 4
        int targetStateUpdates = 4;
        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Sample Range //////////////////////

        // Query to get the number of collectes samples
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.LIFE_CYCLE_EVENT);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.COUNT);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.LIFE_CYCLE_EVENT);

        // Check the result list is not null and contains only the correct Type
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));

        // Get the State Count from the Query Result
        int availableStateCount = (int)resultList.get(0);

        // Check the number of collected samples
        assertEquals(targetStateUpdates, availableStateCount);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.LIFE_CYCLE_EVENT);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(availableStateCount-1);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.SAMPLE_RANGE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.LIFE_CYCLE_EVENT);

        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only LifeCycleVariationRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof LifeCycleVariationRecord));

        assertEquals(targetStateUpdates, resultList.size());

        ///////////////// Query in Last Life Cycle State //////////////////////
        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.LIFE_CYCLE_EVENT);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        // Send the Query Request to the Storage Manager for the target DT
        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.LIFE_CYCLE_EVENT);

        // Extract the number of collected samples
        resultList =  queryResult.getResults();

        // Check the result list is not null and contains only LifeCycleVariationRecord
        assertTrue(resultList != null && !resultList.isEmpty());
        assertInstanceOf(LifeCycleVariationRecord.class, resultList.get(0));

        // Check the last Life Cycle State is SYNCHRONIZED
        assertEquals(LifeCycleState.SYNCHRONIZED, ((LifeCycleVariationRecord)resultList.get(0)).getLifeCycleState());
    }

    @Test
    @Order(14)
    public void testDigitalAdapterQueryExecutorSync() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        // Send the Query Request to the Storage Manager for the target DT
        QueryResult<?> queryResult = digitalAdapter.testSyncQuery(queryRequest);

        assertNotNull(queryResult);
        assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

    }

    @Test
    @Order(15)
    public void testDigitalAdapterQueryExecutorAsync() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        // Create Query Request to the Storage Manager for the Last Digital Twin State
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
        queryRequest.setRequestType(QueryRequestType.LAST_VALUE);

        final QueryResult<?>[] receivedQueryResult = {null};

        // Send the Query Request to the Storage Manager for the target DT
        digitalAdapter.testAsyncQuery(queryRequest, new IQueryResultListener() {
            @Override
            public void onQueryResult(QueryResult<?> queryResult) {
                receivedQueryResult[0] = queryResult;
            }
        });

        Thread.sleep(5000);

        assertNotNull(receivedQueryResult[0]);
        assertEquals(receivedQueryResult[0].getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
        assertEquals(receivedQueryResult[0].getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);

        Thread.sleep(10000);
    }
}

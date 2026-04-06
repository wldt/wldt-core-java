package it.wldt.storage;

import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.handler.DefaultAugmentationFunctionHandler;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.storage.augmentation.function.StorageTestErrorStatefulAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestErrorStatelessAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestStatefulAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestStatelessAugmentationFunction;
import it.wldt.storage.model.augmentation.*;
import it.wldt.storage.query.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AugmentationStorageQueryTester {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationStorageQueryTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dt-augmentation-storage-query-test";

    private static final String DEFAULT_STORAGE_ID = "default-aug-query-storage";

    private static final String TEST_HANDLER_ID = "aug-query-test-handler";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoDigitalAdapter digitalAdapter = null;

    private DemoPhysicalAdapter physicalAdapter = null;

    // Save initial experiment time
    private long startTimeStamp = System.currentTimeMillis();

    @BeforeEach
    public void setUp() throws KernelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, StorageException, AugmentationFunctionException, InterruptedException {

        logger.info("Setting up Test Environment ...");

        startTimeStamp = System.currentTimeMillis();

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new AugmentationStorageDigitalTwinModel());

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

        // Create an instance of the Augmentation Function Handler (empty, functions registered after DT start)
        AugmentationFunctionHandler myAugmentationFunctionHandler = new DefaultAugmentationFunctionHandler(TEST_HANDLER_ID);

        // Add the handler to the Digital Twin Augmentation Manager (sets digitalTwinId on the handler)
        digitalTwin.getAugmentationManager().addAugmentationFunctionHandler(myAugmentationFunctionHandler);

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(TEST_DIGITAL_TWIN_ID);

        // Add the Twin to the Engine
        digitalTwinEngine.addDigitalTwin(digitalTwin);

        // Start the Digital Twin
        digitalTwinEngine.startDigitalTwin(TEST_DIGITAL_TWIN_ID);

        // Wait for StorageManager to be initialized and subscribed to events
        Thread.sleep(2000);

        // Register the 4 test augmentation functions AFTER DT start so StorageManager captures registration events
        // Small delays between registrations to avoid timestamp collisions in the storage map
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).isPresent()) {
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .registerAugmentationFunction(new StorageTestStatelessAugmentationFunction());
            Thread.sleep(50);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .registerAugmentationFunction(new StorageTestStatefulAugmentationFunction());
            Thread.sleep(50);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .registerAugmentationFunction(new StorageTestErrorStatelessAugmentationFunction());
            Thread.sleep(50);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .registerAugmentationFunction(new StorageTestErrorStatefulAugmentationFunction());
        }
    }

    @AfterEach
    public void tearDown() throws WldtEngineException, EventBusException {

        logger.info("Cleaning up Test Environment ...");

        if (digitalTwinEngine != null && digitalTwin != null && digitalTwinEngine.getDigitalTwinCount() > 0) {
            digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
            digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
            digitalTwin = null;
            digitalTwinEngine = null;
        }

        SharedTestMetrics.getInstance().resetMetrics();
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID);
    }

    private void waitForDtExecution() throws InterruptedException {
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));
        Thread.sleep(5000); // wait for augmentation functions to complete
    }

    @Test
    @Order(1)
    public void testSyncQueryAugmentationFunctionRegistration() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        /////////////////// COUNT query //////////////////////

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.COUNT, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, queryResult.getOriginalRequest().getResourceType());

        List<?> resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof Integer));
        assertEquals(4, (int) resultList.get(0));

        /////////////////// TIME_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(System.currentTimeMillis());

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.TIME_RANGE, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, queryResult.getOriginalRequest().getResourceType());

        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(4, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionRegistrationRecord));

        /////////////////// SAMPLE_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(3);

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.SAMPLE_RANGE, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_REGISTRATION, queryResult.getOriginalRequest().getResourceType());

        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(4, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionRegistrationRecord));
    }

    @Test
    @Order(2)
    public void testSyncQueryAugmentationFunctionRequest() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        /////////////////// COUNT query //////////////////////

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.COUNT, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_REQUEST, queryResult.getOriginalRequest().getResourceType());

        List<?> resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        int count = (int) resultList.get(0);
        assertTrue(count >= 2, "Expected at least 2 augmentation function requests, got: " + count);

        /////////////////// TIME_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(System.currentTimeMillis());

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionRequestRecord));

        /////////////////// SAMPLE_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_REQUEST);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(count - 1);

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(count, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionRequestRecord));
    }

    @Test
    @Order(3)
    public void testSyncQueryAugmentationFunctionResult() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        // Expected result count: 1 (stateless) + EXECUTION_COUNT (stateful)
        int expectedResultCount = 1 + StorageTestStatefulAugmentationFunction.EXECUTION_COUNT;

        /////////////////// COUNT query //////////////////////

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_RESULT);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.COUNT, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_RESULT, queryResult.getOriginalRequest().getResourceType());

        List<?> resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedResultCount, (int) resultList.get(0));

        /////////////////// TIME_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_RESULT);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(System.currentTimeMillis());

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedResultCount, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionResultRecord));

        // Verify at least one record has key == StorageTestStatelessAugmentationFunction.RESULT_KEY
        boolean foundStatelessKey = false;
        for (Object item : resultList) {
            AugmentationFunctionResultRecord record = (AugmentationFunctionResultRecord) item;
            if (StorageTestStatelessAugmentationFunction.RESULT_KEY.equals(record.getKey())) {
                foundStatelessKey = true;
                break;
            }
        }
        assertTrue(foundStatelessKey, "Expected at least one result with key == " + StorageTestStatelessAugmentationFunction.RESULT_KEY);

        /////////////////// SAMPLE_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_RESULT);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(expectedResultCount - 1);

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedResultCount, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionResultRecord));
    }

    @Test
    @Order(4)
    public void testSyncQueryAugmentationFunctionError() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        // Expected error count: 1 (stateless error) + ERROR_COUNT (stateful errors)
        int expectedErrorCount = 1 + StorageTestErrorStatefulAugmentationFunction.ERROR_COUNT;

        /////////////////// COUNT query //////////////////////

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_ERROR);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.COUNT, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_ERROR, queryResult.getOriginalRequest().getResourceType());

        List<?> resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedErrorCount, (int) resultList.get(0));

        /////////////////// TIME_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_ERROR);
        queryRequest.setRequestType(QueryRequestType.TIME_RANGE);
        queryRequest.setStartTimestampMs(startTimeStamp);
        queryRequest.setEndTimestampMs(System.currentTimeMillis());

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedErrorCount, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionErrorRecord));

        // Verify that the result list contains records with both ERROR and CRITICAL types
        boolean foundError = false;
        boolean foundCritical = false;
        for (Object item : resultList) {
            AugmentationFunctionErrorRecord record = (AugmentationFunctionErrorRecord) item;
            if (record.getErrorType() == AugmentationFunctionErrorType.ERROR) {
                foundError = true;
            }
            if (record.getErrorType() == AugmentationFunctionErrorType.CRITICAL) {
                foundCritical = true;
            }
        }
        assertTrue(foundError, "Expected at least one error with type ERROR");
        assertTrue(foundCritical, "Expected at least one error with type CRITICAL");

        /////////////////// SAMPLE_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_ERROR);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(expectedErrorCount - 1);

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedErrorCount, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionErrorRecord));
    }

    @Test
    @Order(5)
    public void testSyncQueryAugmentationFunctionUnregistration() throws InterruptedException, StorageException, WldtEngineException, EventBusException, AugmentationFunctionException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        // Manually unregister all 4 augmentation functions (unregistration does not happen automatically on DT stop)
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).isPresent()) {
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .unRegisterAugmentationFunction(StorageTestStatelessAugmentationFunction.FUNCTION_ID);
            Thread.sleep(500);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .unRegisterAugmentationFunction(StorageTestStatefulAugmentationFunction.FUNCTION_ID);
            Thread.sleep(500);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .unRegisterAugmentationFunction(StorageTestErrorStatelessAugmentationFunction.FUNCTION_ID);
            Thread.sleep(500);
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_HANDLER_ID).get()
                    .unRegisterAugmentationFunction(StorageTestErrorStatefulAugmentationFunction.FUNCTION_ID);
        }

        // Wait for unregistration events to be processed
        Thread.sleep(5000);

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        /////////////////// COUNT query //////////////////////

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        QueryResult<?> queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        assertEquals(QueryRequestType.COUNT, queryResult.getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION, queryResult.getOriginalRequest().getResourceType());

        List<?> resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(4, (int) resultList.get(0));

        /////////////////// SAMPLE_RANGE query //////////////////////

        queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_UNREGISTRATION);
        queryRequest.setRequestType(QueryRequestType.SAMPLE_RANGE);
        queryRequest.setStartIndex(0);
        queryRequest.setEndIndex(3);

        queryResult = queryExecutor.syncQueryExecute(queryRequest);

        assertNotNull(queryResult);
        resultList = queryResult.getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(4, resultList.size());
        assertTrue(resultList.stream().allMatch(item -> item instanceof AugmentationFunctionUnregistrationRecord));
    }

    @Test
    @Order(6)
    public void testAsyncQueryAugmentationFunctionResult() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        waitForDtExecution();

        QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "aug-query-executor");

        // Expected result count: 1 (stateless) + EXECUTION_COUNT (stateful)
        int expectedResultCount = 1 + StorageTestStatefulAugmentationFunction.EXECUTION_COUNT;

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setResourceType(QueryResourceType.AUGMENTATION_FUNCTION_RESULT);
        queryRequest.setRequestType(QueryRequestType.COUNT);

        final QueryResult<?>[] received = {null};

        queryExecutor.asyncQueryExecute(queryRequest, new IQueryResultListener() {
            @Override
            public void onQueryResult(QueryResult<?> queryResult) {
                received[0] = queryResult;
            }
        });

        Thread.sleep(3000);

        assertNotNull(received[0]);
        assertEquals(QueryRequestType.COUNT, received[0].getOriginalRequest().getRequestType());
        assertEquals(QueryResourceType.AUGMENTATION_FUNCTION_RESULT, received[0].getOriginalRequest().getResourceType());

        List<?> resultList = received[0].getResults();
        assertTrue(resultList != null && !resultList.isEmpty());
        assertEquals(expectedResultCount, (int) resultList.get(0));
    }
}










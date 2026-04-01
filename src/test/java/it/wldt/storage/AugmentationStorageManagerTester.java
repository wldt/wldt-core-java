package it.wldt.storage;

import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.augmentation.function.AugmentationFunctionType;
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
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.storage.augmentation.function.StorageTestErrorStatefulAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestErrorStatelessAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestStatefulAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestStatelessAugmentationFunction;
import it.wldt.storage.model.StorageStats;
import it.wldt.storage.model.augmentation.*;
import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AugmentationStorageManagerTester {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationStorageManagerTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dt-augmentation-storage-manager-test";

    private static final String DEFAULT_STORAGE_ID = "default-aug-storage";

    private static final String TEST_HANDLER_ID = "aug-storage-test-handler";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoDigitalAdapter digitalAdapter = null;

    private DemoPhysicalAdapter physicalAdapter = null;

    @BeforeEach
    public void setUp() throws KernelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, StorageException, AugmentationFunctionException, InterruptedException {

        logger.info("Setting up Test Environment ...");

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

    @Test
    @Order(1)
    public void testAugmentationFunctionRegistrationStorage() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait until all the physical messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Additional wait for augmentation startup
        Thread.sleep(3000);

        DefaultWldtStorage defaultWldtStorage = null;

        if (digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        assertNotNull(defaultWldtStorage);

        // All 4 functions registered
        assertEquals(4, defaultWldtStorage.getAugmentationFunctionRegistrationCount());

        // Check that the registration records contain both STATELESS and STATEFUL types
        List<AugmentationFunctionRegistrationRecord> registrations = defaultWldtStorage.getAugmentationFunctionRegistrationInRange(0, 3);
        assertNotNull(registrations);
        assertEquals(4, registrations.size());

        int statelessCount = 0;
        int statefulCount = 0;
        for (AugmentationFunctionRegistrationRecord record : registrations) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
            if (record.getType() == AugmentationFunctionType.STATELESS) {
                statelessCount++;
            } else if (record.getType() == AugmentationFunctionType.STATEFUL) {
                statefulCount++;
            }
        }

        assertEquals(2, statelessCount);
        assertEquals(2, statefulCount);

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testAugmentationFunctionRequestAndResultStorage() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait until all the physical messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Wait for augmentation functions to complete (stateful needs EXECUTION_COUNT * SLEEP_BETWEEN_EXECUTIONS_MS + margin)
        Thread.sleep(5000);

        DefaultWldtStorage defaultWldtStorage = null;

        if (digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        assertNotNull(defaultWldtStorage);

        // At least 2 requests (at least 1 per stateless function executed)
        assertTrue(defaultWldtStorage.getAugmentationFunctionRequestCount() >= 2);

        // Total expected results: 1 (stateless) + EXECUTION_COUNT (stateful) = 6
        int expectedResultCount = 1 + StorageTestStatefulAugmentationFunction.EXECUTION_COUNT;
        assertEquals(expectedResultCount, defaultWldtStorage.getAugmentationFunctionResultCount());

        // Validate result records
        List<AugmentationFunctionResultRecord> results = defaultWldtStorage.getAugmentationFunctionResultInRange(0, expectedResultCount - 1);
        assertNotNull(results);
        assertEquals(expectedResultCount, results.size());

        for (AugmentationFunctionResultRecord record : results) {
            assertNotNull(record.getKey());
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
        }

        Thread.sleep(2000);
    }

    @Test
    @Order(3)
    public void testAugmentationFunctionErrorStorage() throws InterruptedException, StorageException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait until all the physical messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Wait for augmentation functions to complete
        Thread.sleep(5000);

        DefaultWldtStorage defaultWldtStorage = null;

        if (digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        assertNotNull(defaultWldtStorage);

        // Expected error count: 1 (stateless error) + ERROR_COUNT (stateful errors) = 4
        int expectedErrorCount = 1 + StorageTestErrorStatefulAugmentationFunction.ERROR_COUNT;
        assertEquals(expectedErrorCount, defaultWldtStorage.getAugmentationFunctionErrorCount());

        // Validate error records
        List<AugmentationFunctionErrorRecord> errors = defaultWldtStorage.getAugmentationFunctionErrorsInRange(0, expectedErrorCount - 1);
        assertNotNull(errors);
        assertEquals(expectedErrorCount, errors.size());

        Set<AugmentationFunctionErrorType> foundTypes = new HashSet<>();
        for (AugmentationFunctionErrorRecord record : errors) {
            assertNotNull(record.getErrorId());
            assertNotNull(record.getErrorType());
            assertNotNull(record.getMessage());
            foundTypes.add(record.getErrorType());
        }

        // Check that error types include both ERROR and CRITICAL
        assertTrue(foundTypes.contains(AugmentationFunctionErrorType.ERROR));
        assertTrue(foundTypes.contains(AugmentationFunctionErrorType.CRITICAL));

        Thread.sleep(2000);
    }

    @Test
    @Order(4)
    public void testAugmentationFunctionUnregistrationStorage() throws InterruptedException, StorageException, WldtEngineException, AugmentationFunctionException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait until all the physical messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Wait for augmentation functions to complete
        Thread.sleep(5000);

        DefaultWldtStorage defaultWldtStorage = null;

        if (digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        assertNotNull(defaultWldtStorage);

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

        // All 4 functions unregistered
        assertEquals(4, defaultWldtStorage.getAugmentationFunctionUnregistrationCount());

        // Validate unregistration records
        List<AugmentationFunctionUnregistrationRecord> unregistrations = defaultWldtStorage.getAugmentationFunctionUnregistrationInRange(0, 3);
        assertNotNull(unregistrations);
        assertEquals(4, unregistrations.size());

        for (AugmentationFunctionUnregistrationRecord record : unregistrations) {
            assertNotNull(record.getAugmentationFunctionId());
            assertNotNull(record.getAugmentationFunctionHandlerId());
            assertNotNull(record.getType());
        }
    }

    @Test
    @Order(5)
    public void testStorageStatsIncludeAugmentationData() throws InterruptedException, StorageException, WldtEngineException, AugmentationFunctionException {

        // Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait until all the physical messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Wait for augmentation functions to complete
        Thread.sleep(5000);

        DefaultWldtStorage defaultWldtStorage = null;

        if (digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        assertNotNull(defaultWldtStorage);

        // Manually unregister all 4 augmentation functions to get unregistration data
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

        StorageStats storageStats = defaultWldtStorage.getStorageStats();
        assertNotNull(storageStats);

        // Validate augmentation-related stats fields are present and non-empty
        // (exact counts are verified in dedicated tests 1-4, here we validate stats population)
        assertNotNull(storageStats.getAugmentationFunctionRegistrationStats());
        assertTrue(storageStats.getAugmentationFunctionRegistrationStats().getRecordCount() > 0);

        assertNotNull(storageStats.getAugmentationFunctionResultStats());
        assertTrue(storageStats.getAugmentationFunctionResultStats().getRecordCount() > 0);

        assertNotNull(storageStats.getAugmentationFunctionErrorStats());
        assertTrue(storageStats.getAugmentationFunctionErrorStats().getRecordCount() > 0);

        assertNotNull(storageStats.getAugmentationFunctionUnregistrationStats());
        assertTrue(storageStats.getAugmentationFunctionUnregistrationStats().getRecordCount() > 0);

        assertNotNull(storageStats.getAugmentationFunctionRequestStats());
        assertTrue(storageStats.getAugmentationFunctionRequestStats().getRecordCount() > 0);
    }
}














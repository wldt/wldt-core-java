package it.wldt.storage;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.exception.*;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoShadowingFunction;
import it.wldt.storage.model.StorageStats;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StorageManagerTester {

    private static final Logger logger = LoggerFactory.getLogger(StorageManagerTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private static final String DEFAULT_STORAGE_ID = "default-storage";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoDigitalAdapter digitalAdapter = null;

    private DemoPhysicalAdapter physicalAdapter = null;

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
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // we have 10 Energy Updates, 2 Events of OverHeating, 4 Relationship Instance changes
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, defaultWldtStorage.getPhysicalAssetPropertyVariationCount());

        // we have 2 Events of OverHeating, 4 Relationship Instance changes
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, defaultWldtStorage.getPhysicalAssetEventNotificationCount());

        // we have 2 Relationship Instance Created
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationCount());

        // we have 2 Relationship Instance Deleted
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationCount());

        Thread.sleep(2000);
    }


    @Test
    @Order(2)
    public void testDigitalTwinStateUpdates() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(5000);

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // we have 10 Energy Updates
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, defaultWldtStorage.getPhysicalAssetPropertyVariationCount());

        // we have 2 Events of OverHeating, 4 Relationship Instance changes
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, defaultWldtStorage.getPhysicalAssetEventNotificationCount());

        // we have 2 Relationship Instance Created
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationCount());

        // we have 2 Relationship Instance Deleted
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationCount());

        // The total number of DT State Update (according to the current ShadowingFunction) is equals to the number
        // of received physical asset property variation events + the initial State publication at the Sync moment (+1)
        // + the number of Event Notification generated by the DT Model and associated to DT events
        // + the number of variations associated to Relationship changes (in our scenario +4)
        int totalStateUpdates = 4 + 1 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES;
        assertEquals(totalStateUpdates, defaultWldtStorage.getDigitalTwinStateCount());

        // Check the number of received DT State Update Events
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, defaultWldtStorage.getDigitalTwinStateEventNotificationCount());

        Thread.sleep(2000);
    }

    @Test
    @Order(3)
    public void basicDigitalActionTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        Thread.sleep(2000);

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // we have 10 Energy Updates + after physical action and changes
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + 2, defaultWldtStorage.getPhysicalAssetPropertyVariationCount());

        // we have 2 Events of OverHeating, 4 Relationship Instance changes
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, defaultWldtStorage.getPhysicalAssetEventNotificationCount());

        // we have 2 Relationship Instance Created
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationCount());

        // we have 2 Relationship Instance Deleted
        assertEquals(2, defaultWldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationCount());

        // The total number of DT State Update (according to the current ShadowingFunction) is equals to the number
        // of received physical asset property variation events + the initial State publication at the Sync moment (+1)
        // + the number of Event Notification generated by the DT Model and associated to DT events
        // + the number of variations associated to Relationship changes (in our scenario +4)
        // + 2 After Physical Action and the associated changes
        int totalStateUpdates = 4 + 1 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + 2;
        assertEquals(totalStateUpdates, defaultWldtStorage.getDigitalTwinStateCount());

        // Check the number of received DT State Update Events
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, defaultWldtStorage.getDigitalTwinStateEventNotificationCount());

        // Check DT Digital Actions Received
        assertEquals(2, defaultWldtStorage.getDigitalActionRequestCount());

        // Check DT Physical Actions Received
        assertEquals(2, defaultWldtStorage.getPhysicalAssetActionRequestCount());

    }

    @Test
    @Order(4)
    public void testPhysicalAdapterPadEvents() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // Check the number of received Physical Asset Description WLDT Events
        assertEquals(1, defaultWldtStorage.getNewPhysicalAssetDescriptionNotificationCount());

        Thread.sleep(2000);
    }


    @Test
    @Order(5)
    public void testLifeCycleEvents() throws InterruptedException, WldtEngineException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // Forcing Digital Twin Stop
        if(digitalTwinEngine != null && digitalTwin != null) {
            digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
            Thread.sleep(2000);
            digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
            Thread.sleep(2000);
            //digitalTwin = null;
            //digitalTwinEngine = null;
        }

        while(digitalTwinEngine != null && digitalTwinEngine.getDigitalTwinCount() > 0)
            Thread.sleep(1000);


        // Considering the entire life cycle of the Digital Twin we have 7 Life Cycle State Changes
        int targetMessages = 7;
        assertEquals(targetMessages, defaultWldtStorage.getLifeCycleStateCount());

    }

    @Test
    @Order(6)
    public void testStorageStats() throws InterruptedException, WldtEngineException, StorageException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        DefaultWldtStorage defaultWldtStorage = null;

        if(digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID) instanceof DefaultWldtStorage)
            defaultWldtStorage = (DefaultWldtStorage) digitalTwin.getStorageManager().getStorage(DEFAULT_STORAGE_ID);

        // Check if the Default Storage has been correctly initialized
        assertNotNull(defaultWldtStorage);

        // Forcing Digital Twin Stop
        if(digitalTwinEngine != null && digitalTwin != null) {
            digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
            Thread.sleep(2000);
            digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
            Thread.sleep(2000);
            //digitalTwin = null;
            //digitalTwinEngine = null;
        }

        while(digitalTwinEngine != null && digitalTwinEngine.getDigitalTwinCount() > 0)
            Thread.sleep(1000);


        StorageStats storageStats = defaultWldtStorage.getStorageStats();

        // Check the Storage Stats
        assertEquals(7, storageStats.getLifeCycleVariationStats().getRecordCount());
        assertEquals(1, storageStats.getNewPhysicalAssetDescriptionNotificationStats().getRecordCount());
        assertEquals(10, storageStats.getPhysicalAssetPropertyVariationStats().getRecordCount());;
    }

}

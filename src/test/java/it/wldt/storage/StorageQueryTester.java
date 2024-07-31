package it.wldt.storage;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
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
import it.wldt.storage.query.*;
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
public class StorageQueryTester {

    private static final Logger logger = LoggerFactory.getLogger(StorageQueryTester.class);

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

        Thread.sleep(10000);
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

        // Send the Query Request to the Storage Manager for the target DT
        queryExecutor.asyncQueryExecute(queryRequest, new IQueryResultListener() {
            @Override
            public void onQueryResult(QueryResult<?> queryResult) {
                assertNotNull(queryResult);
                assertEquals(queryResult.getOriginalRequest().getRequestType(), QueryRequestType.LAST_VALUE);
                assertEquals(queryResult.getOriginalRequest().getResourceType(), QueryResourceType.DIGITAL_TWIN_STATE);
            }
        });

        Thread.sleep(10000);
    }

}

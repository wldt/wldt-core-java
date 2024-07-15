package it.wldt.process.observer;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.exception.*;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoShadowingFunction;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventObserverTester {

    private static final Logger logger = LoggerFactory.getLogger(EventObserverTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private WldtEventObserver eventObserver = null;

    private TestObserverListener testObserverListener = null;

    private DemoDigitalAdapter digitalAdapter = null;

    private DemoPhysicalAdapter physicalAdapter = null;

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

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

        // Digital Adapter with Configuration
        digitalAdapter = new DemoDigitalAdapter(
                String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-digital-adapter"),
                new DemoDigitalAdapterConfiguration()
        );

        digitalTwin.addDigitalAdapter(digitalAdapter);

        // Create the target Observer
        testObserverListener = new TestObserverListener();

        eventObserver = new WldtEventObserver(
                digitalTwin.getDigitalTwinId(),
                "test-observer",
                testObserverListener);

        // Start all the available observation
        eventObserver.observePhysicalAssetEvents();
        eventObserver.observePhysicalAssetActionEvents();
        eventObserver.observeStateEvents();
        eventObserver.observeDigitalActionEvents();

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
        digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
        digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
        digitalTwin = null;
        digitalTwinEngine = null;

        // Cancel all the available observation
        eventObserver.unObservePhysicalAssetEvents();
        eventObserver.unObservePhysicalAssetActionEvents();
        eventObserver.unObserveStateEvents();
        eventObserver.unObserveDigitalActionEvents();

        SharedTestMetrics.getInstance().resetMetrics();
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID);
    }

    @Test
    @Order(1)
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Check Received Physical Asset WLDT Events (Property Variations & Events)
        assertNotNull(testObserverListener.getPhysicalAssetEvents());

        // Considering the testing Physical Adapter we have10 Energy Updates, 2 Events of OverHeating, 4 Relationship Instance changes
        int targetPhysicalAssetMessages = 4 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES;
        int totalReceivedEvents = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getPhysicalAssetEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalReceivedEvents += eventList.size();
        }
        assertEquals(targetPhysicalAssetMessages, totalReceivedEvents);

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testDigitalTwinStateUpdates() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Check Received Physical Asset WLDT Events (Property Variations & Events)
        assertNotNull(testObserverListener.getPhysicalAssetEvents());

        // Considering the testing Physical Adapter we have10 Energy Updates, 2 Events of OverHeating, 4 Relationship Instance changes
        int targetPhysicalAssetMessages = 4 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES;
        int totalPhysicalAssetReceivedEvents = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getPhysicalAssetEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalPhysicalAssetReceivedEvents += eventList.size();
        }
        assertEquals(targetPhysicalAssetMessages, totalPhysicalAssetReceivedEvents);

        //Check DT State Updates Events
        assertNotNull(testObserverListener.getDtStateEvents());

        // The total number of DT State Update (according to the current ShadowingFunction) is equals to the number
        // of received physical asset property variation events + the initial State publication at the Sync moment (+1)
        // + the number of Event Notification generated by the DT Model and associated to DT events
        // + the number of variations associated to Relationship changes (in our scenario +4)
        int totalStateUpdates = 4 + 1 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES;
        int totalReceivedEvents = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getDtStateEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalReceivedEvents += eventList.size();
        }

        assertEquals(totalStateUpdates, totalReceivedEvents);

        Thread.sleep(2000);
    }

    @Test
    @Order(3)
    public void basicDigitalActionTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

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

        //Check Received Physical Asset WLDT Events (Property Variations & Events)
        assertNotNull(testObserverListener.getPhysicalAssetEvents());

        // Considering the testing Physical Adapter we have10 Energy Updates, 2 Events of OverHeating, 4 Relationship Instance changes
        // Same as Before + 2 Events associated to the changes triggered by the Actions sent
        int targetPhysicalAssetMessages = 4 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES + 2;
        int totalPhysicalAssetReceivedEvents = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getPhysicalAssetEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalPhysicalAssetReceivedEvents += eventList.size();
        }
        assertEquals(targetPhysicalAssetMessages, totalPhysicalAssetReceivedEvents);

        //Check DT State Updates Events
        assertNotNull(testObserverListener.getDtStateEvents());

        // The total number of DT State Update (according to the current ShadowingFunction) is equals to the number
        // of received physical asset property variation events + the initial State publication at the Sync moment
        // + the number of Event Notification generated by the DT Model and associated to DT events
        // + 2 State changes associated to the variations triggered by the Actions sent
        // + the number of variations associated to Relationship changes (in our scenario +4)
        int totalStateUpdates = 4 + 1 + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                + TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES + 2;
        int totalReceivedEvents = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getDtStateEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalReceivedEvents += eventList.size();
        }

        assertEquals(totalStateUpdates, totalReceivedEvents);

        //Check DT Digital Actions Received
        assertNotNull(testObserverListener.getDigitalActionEvents());

        // 2 Digital Actions Messages trigger in the test function
        int targetDigitalActionMessages = 2;
        int totalDigitalActionMessages = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getDigitalActionEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalDigitalActionMessages += eventList.size();
        }
        assertEquals(targetDigitalActionMessages, totalDigitalActionMessages);

        //Check DT Physical Actions Received
        assertNotNull(testObserverListener.getPhysicalActionEvents());

        // 2 Physical Actions Messages trigger in the test function
        int targetPhysicalActionMessages = 2;
        int totalPhysicalActionMessages = 0;

        for (Map.Entry<String, List<WldtEvent<?>>> entry : testObserverListener.getDigitalActionEvents().entrySet()) {
            String eventType = entry.getKey();
            List<WldtEvent<?>> eventList = entry.getValue();
            totalPhysicalActionMessages += eventList.size();
        }
        assertEquals(targetPhysicalActionMessages, totalPhysicalActionMessages);

    }

}

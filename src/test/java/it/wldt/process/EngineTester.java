package it.wldt.process;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.twin.DigitalTwin;
import it.wldt.core.twin.LifeCycleState;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EngineTester {

    private static final Logger logger = LoggerFactory.getLogger(EngineTester.class);

    private final String TEST_DIGITAL_TWIN_ID_1 = "dtTest0001";

    private final String TEST_DIGITAL_TWIN_ID_2 = "dtTest0002";

    private final String TEST_DIGITAL_TWIN_ID_3 = "dtTest0003";

    private DigitalTwinEngine digitalTwinEngine = null;

    private DigitalTwin createNewDigitalTwin(String digitalTwinId) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException {

        DigitalTwin digitalTwin = new DigitalTwin(
                new DemoShadowingFunction(digitalTwinId),
                digitalTwinId);

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(
                new DemoPhysicalAdapter(
                        digitalTwinId,
                        String.format("%s-%s", digitalTwinId, "test-physical-adapter"),
                        new DemoPhysicalAdapterConfiguration(),
                        true));

        // Digital Adapter with Configuration
        digitalTwin.addDigitalAdapter(
                new DemoDigitalAdapter(
                        digitalTwinId,
                        String.format("%s-%s", digitalTwinId, "test-digital-adapter"),
                        new DemoDigitalAdapterConfiguration())
        );

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(digitalTwinId);

        //digitalTwin.startLifeCycle();

        return digitalTwin;
    }

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtEngineException {

        logger.info("Setting up Test Environment ...");

        // Create Digital Twin Engine
        digitalTwinEngine = new DigitalTwinEngine();

        DigitalTwin digitalTwin1 = createNewDigitalTwin(TEST_DIGITAL_TWIN_ID_1);
        DigitalTwin digitalTwin2 = createNewDigitalTwin(TEST_DIGITAL_TWIN_ID_2);
        DigitalTwin digitalTwin3 = createNewDigitalTwin(TEST_DIGITAL_TWIN_ID_3);

        digitalTwinEngine.addDigitalTwin(digitalTwin1);
        digitalTwinEngine.addDigitalTwin(digitalTwin2);
        digitalTwinEngine.addDigitalTwin(digitalTwin3);

    }

    @AfterEach
    public void tearDown() throws WldtEngineException {
        logger.info("Cleaning up Test Environment ...");
        SharedTestMetrics.getInstance().resetMetrics();
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID_1);
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID_2);
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID_3);
    }

    @Test
    @Order(1)
    public void testTwinsStart() throws WldtConfigurationException, WldtEngineException, InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Start all Digital Twins
        digitalTwinEngine.startAll();

        // Check that the engine has the correct number of twins
        assertEquals(3, digitalTwinEngine.getDigitalTwinMap().size());

        // Sleep to wait that all twins are correctly started
        Thread.sleep(5000);

        // Check that all Twins are correctly started through their Life Cycle State
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_1);
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_2);
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_3);

        //Stop all twins
        digitalTwinEngine.stopAll();

        // Sleep to wait that all twins are correctly stopped
        Thread.sleep(5000);

        // Check that all Twins are correctly started through their Life Cycle State
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_1);
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_2);
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_3);

        Thread.sleep(10*60000);
    }

    @Test
    @Order(1)
    public void testReceivedMessages() throws WldtConfigurationException, WldtEngineException, InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Start all Digital Twins
        digitalTwinEngine.startAll();

        // Check that the engine has the correct number of twins
        assertEquals(3, digitalTwinEngine.getDigitalTwinMap().size());

        // Sleep to wait that all twins are correctly started
        Thread.sleep(2*60000);

        // Check that all Twins are correctly started through their Life Cycle State
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_1);
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_2);
        checkTwinIsActive(TEST_DIGITAL_TWIN_ID_3);

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        checkReceivedMessagedForTargetTwin(TEST_DIGITAL_TWIN_ID_1);
        checkReceivedMessagedForTargetTwin(TEST_DIGITAL_TWIN_ID_2);
        checkReceivedMessagedForTargetTwin(TEST_DIGITAL_TWIN_ID_3);

        //Stop all twins
        digitalTwinEngine.stopAll();

        // Sleep to wait that all twins are correctly stopped
        Thread.sleep(5000);

        // Check that all Twins are correctly started through their Life Cycle State
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_1);
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_2);
        checkTwinIsStoppedOrDestroyed(TEST_DIGITAL_TWIN_ID_3);

        Thread.sleep(10*60000);
    }

    private void checkTwinIsActive(String digitalTwinId) {
        assertNotEquals(LifeCycleState.NONE.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.CREATED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.STOPPED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.DESTROYED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
    }

    private void checkTwinIsStoppedOrDestroyed(String digitalTwinId) {
        assertNotEquals(LifeCycleState.NONE.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.STARTED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.BOUND.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.UN_BOUND.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.SYNCHRONIZED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
        assertNotEquals(LifeCycleState.NOT_SYNCHRONIZED.getValue(), digitalTwinEngine.getDigitalTwinMap().get(digitalTwinId).getCurrentLifeCycleState().getValue());
    }

    private void checkReceivedMessagedForTargetTwin(String digitalTwinId){

        //Check Generated Physical Events Not Null
        assertNotNull(SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(digitalTwinId));

        //Check Received Physical Event on the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(digitalTwinId).size());

        //Check Received Physical Events on the Shadowing Function Not Null
        assertNotNull(SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(digitalTwinId));

        //Check Received Physical Asset Events Availability correctly received by the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(digitalTwinId).size());

        //Check DT State Update not null
        assertNotNull(SharedTestMetrics.getInstance().getDigitalAdapterStateUpdateList(digitalTwinId));

        //Check Correct Digital Twin State Property Update Events have been received on the Digital Adapter through DT State Updates
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getDigitalAdapterStateUpdateList(digitalTwinId).size());

        //Check DT Event Notification not null
        assertNotNull(SharedTestMetrics.getInstance().getDigitalAdapterEventNotificationMap().get(digitalTwinId));

        //Check if Digital Twin State Events Notifications have been correctly received by the Digital Adapter after passing through the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, SharedTestMetrics.getInstance().getDigitalAdapterEventNotificationList(digitalTwinId).size());

    }

}

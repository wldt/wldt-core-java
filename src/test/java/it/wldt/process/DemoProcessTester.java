package it.wldt.process;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.twin.DigitalTwin;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoShadowingFunction;
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
public class DemoProcessTester {

    private static final Logger logger = LoggerFactory.getLogger(DemoProcessTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private DigitalTwin digitalTwin = null;

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException {

        logger.info("Setting up Test Environment ...");

        digitalTwin = new DigitalTwin(
                new DemoShadowingFunction(TEST_DIGITAL_TWIN_ID),
                TEST_DIGITAL_TWIN_ID);

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(
                new DemoPhysicalAdapter(
                        TEST_DIGITAL_TWIN_ID,
                        String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-physical-adapter"),
                        new DemoPhysicalAdapterConfiguration(),
                        true));

        // Digital Adapter with Configuration
        digitalTwin.addDigitalAdapter(
                new DemoDigitalAdapter(
                        TEST_DIGITAL_TWIN_ID,
                        String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-digital-adapter"),
                        new DemoDigitalAdapterConfiguration())
        );

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(TEST_DIGITAL_TWIN_ID);

        digitalTwin.startLifeCycle();

    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up Test Environment ...");
        digitalTwin.stopLifeCycle();
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

        //Check Generated Physical Events Not Null
        assertNotNull(SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(TEST_DIGITAL_TWIN_ID));

        //Check Received Physical Event on the Shadowing Function
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(TEST_DIGITAL_TWIN_ID).size());

        //Check Received Physical Events on the Shadowing Function Not Null
        assertNotNull(SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID));

        //Check Received Physical Asset Events Availability correctly received by the Shadowing Function
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID).size());

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testDigitalTwinStateUpdates() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Check Generated Physical Events Not Null
        assertNotNull(SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(TEST_DIGITAL_TWIN_ID));

        //Check Received Physical Event on the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getPhysicalAdapterPropertyEventList(TEST_DIGITAL_TWIN_ID).size());

        //Check Received Physical Events on the Shadowing Function Not Null
        assertNotNull(SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID));

        //Check Received Physical Asset Events Availability correctly received by the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID).size());

        //Check DT State Update not null
        assertNotNull(SharedTestMetrics.getInstance().getDigitalAdapterStateUpdateList(TEST_DIGITAL_TWIN_ID));

        //Check Correct Digital Twin State Property Update Events have been received on the Digital Adapter through DT State Updates
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getDigitalAdapterStateUpdateList(TEST_DIGITAL_TWIN_ID).size());

        //Check DT Event Notification not null
        assertNotNull(SharedTestMetrics.getInstance().getDigitalAdapterEventNotificationMap().get(TEST_DIGITAL_TWIN_ID));

        //Check if Digital Twin State Events Notifications have been correctly received by the Digital Adapter after passing through the Shadowing Function
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, SharedTestMetrics.getInstance().getDigitalAdapterEventNotificationList(TEST_DIGITAL_TWIN_ID).size());

        Thread.sleep(2000);
    }


}

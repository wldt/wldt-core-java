package it.wldt.monitoring;

import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.*;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.handler.TestMonitoringInterfaceHandler;
import it.wldt.monitoring.metrics.WldtMetric;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.process.DemoProcessTester;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoDigitalTwinModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DigitalTwinProcessMetricsTests {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DemoProcessTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    @BeforeEach
    public void setUp() throws KernelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        logger.info("Setting up Test Environment ...");

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoDigitalTwinModel());

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(
                new DemoPhysicalAdapter(
                        String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-physical-adapter"),
                        new DemoPhysicalAdapterConfiguration(),
                        true));

        // Digital Adapter with Configuration
        digitalTwin.addDigitalAdapter(
                new DemoDigitalAdapter(
                        String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-digital-adapter"),
                        new DemoDigitalAdapterConfiguration())
        );

        // Set the configuration for the Monitoring Interface
        digitalTwin.getMonitoringInterface().setConfiguration(
                new MonitoringInterfaceConfiguration.Builder()
                        .withDtModelMonitoring()
                        .withPhysicalAdapterMonitoring()
                        .withDigitalAdapterMonitoring()
                        .build()
        );

        // Retrieve the Monitoring Interface to set the Handler to be used
        digitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(TEST_DIGITAL_TWIN_ID));

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(TEST_DIGITAL_TWIN_ID);

        // Add the Twin to the Engine
        digitalTwinEngine.addDigitalTwin(digitalTwin);

        // Start the Digital Twin
        digitalTwinEngine.startDigitalTwin(TEST_DIGITAL_TWIN_ID);

    }

    @AfterEach
    public void tearDown() throws WldtEngineException {
        logger.info("Cleaning up Test Environment ...");
        digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);
        digitalTwinEngine.removeDigitalTwin(TEST_DIGITAL_TWIN_ID);
        digitalTwin = null;
        digitalTwinEngine = null;
        SharedTestMetrics.getInstance().resetMetrics();
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(TEST_DIGITAL_TWIN_ID);
    }

    @Test
    @Order(1)
    public void testPhysicalEventsProcessing() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve Shared Stats Components
        List<WldtMetricComponent> registeredComponentList = SharedTestMetrics.getInstance().getMonitoringRegisteredComponentList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredMetricList = SharedTestMetrics.getInstance().getMonitoringRegisteredMetricList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredUpdateMetricsList = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(TEST_DIGITAL_TWIN_ID);

        // Check Registered Stats are not null
        assertNotNull(registeredComponentList);
        assertNotNull(registeredMetricList);
        assertNotNull(registeredUpdateMetricsList);

        // Filter Metrics Update for Their Type and create a List of resulting metrics updates
        List<WldtMetric> paEventExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PA_PROPERTY_VARIATION_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, paEventExecutionTime.size());

        // Filter PA Event Processing Success Count
        List<WldtMetric> paEventSuccessCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PA_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, paEventSuccessCount.size());

        // Filter PA Event Processing Error Count
        List<WldtMetric> paEventErrorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PA_PROPERTY_VARIATION_EXEC_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, paEventErrorCount.size());

        //Check Received Physical Events on the Shadowing Function Not Null
        //assertNotNull(SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID));

        //Check Received Physical Asset Events Availability correctly received by the Shadowing Function
        //assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, SharedTestMetrics.getInstance().getShadowingFunctionPropertyEventList(TEST_DIGITAL_TWIN_ID).size());

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testDigitalTwinStateUpdates() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {

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

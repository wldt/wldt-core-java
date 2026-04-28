package it.wldt.monitoring;

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

    private DemoDigitalAdapter demoDigitalAdapter;

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
                        true,
                        true));

        // Create the Digital Adapter ad field instance to be used for emulating Digital Actions
        demoDigitalAdapter = new DemoDigitalAdapter(
                String.format("%s-%s", TEST_DIGITAL_TWIN_ID, "test-digital-adapter"),
                new DemoDigitalAdapterConfiguration());

        // Digital Adapter with Configuration
        digitalTwin.addDigitalAdapter(demoDigitalAdapter);

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
    public void testModelPhysicalPropertyVariationEventsProcessing() throws InterruptedException {

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
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, paEventExecutionTime.size());

        // Filter PA Event Processing Success Count
        List<WldtMetric> paEventSuccessCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, paEventSuccessCount.size());

        // Filter PA Event Processing Error Count
        List<WldtMetric> paEventErrorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_PROPERTY_VARIATION_EXEC_ERROR_COUNT))
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
    public void testModelPhysicalEventNotificationProcessing() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_EVENT_NOTIFICATION_EXEC_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(3)
    public void testModelPhysicalRelationshipCreationProcessing() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_CREATED_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }


    @Test
    @Order(4)
    public void testModelPhysicalRelationshipDeletionProcessing() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.PT_RELATIONSHIP_INSTANCE_DELETED_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(5)
    public void testModelDigitalActionProcessing() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        Thread.sleep(2000);

        // Retrieve Shared Stats Components
        List<WldtMetricComponent> registeredComponentList = SharedTestMetrics.getInstance().getMonitoringRegisteredComponentList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredMetricList = SharedTestMetrics.getInstance().getMonitoringRegisteredMetricList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredUpdateMetricsList = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(TEST_DIGITAL_TWIN_ID);

        // Check Registered Stats are not null
        assertNotNull(registeredComponentList);
        assertNotNull(registeredMetricList);
        assertNotNull(registeredUpdateMetricsList);

        // Filter Metrics Update for Their Type and create a List of resulting metrics updates
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(2, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(2, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ACTION_REQUEST_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(6)
    public void testModelDigitalTwinStateProcessing() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DT_STATE_COMPUTATION_EXEC_TIME))
                .collect(Collectors.toList());

        // The target Number of Computed State is:
        // 1 (initial creation after PAD) +
        // 10 DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES Property Variations
        // 2 Relationship Instance Created
        // 2 Relationship Instance Deleted

        int totalTargetStateCount = 1 + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + 2 + 2;

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(totalTargetStateCount, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DT_STATE_COMPUTATION_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(totalTargetStateCount, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.DT_STATE_COMPUTATION_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(7)
    public void testPhysicalAdapterPropertyVariationEventsProcessing() throws InterruptedException {

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

        // Filter PA Event Processing Success Count
        List<WldtMetric> paEventSuccessCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, paEventSuccessCount.size());

        // Filter PA Event Processing Error Count
        List<WldtMetric> paEventErrorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_ERROR_COUNT))
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
    @Order(8)
    public void testPhysicalAdapterEventNotificationProcessing() throws InterruptedException {

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

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(9)
    public void testPhysicalAdapterRelationshipCreationProcessing() throws InterruptedException {

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

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }


    @Test
    @Order(10)
    public void testModelPhysicalAdapterRelationshipDeletionProcessing() throws InterruptedException {

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

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_REL_INSTANCE_CREATE, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(11)
    public void testPhysicalAdapterActionProcessing() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        Thread.sleep(2000);

        // Retrieve Shared Stats Components
        List<WldtMetricComponent> registeredComponentList = SharedTestMetrics.getInstance().getMonitoringRegisteredComponentList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredMetricList = SharedTestMetrics.getInstance().getMonitoringRegisteredMetricList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredUpdateMetricsList = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(TEST_DIGITAL_TWIN_ID);

        // Check Registered Stats are not null
        assertNotNull(registeredComponentList);
        assertNotNull(registeredMetricList);
        assertNotNull(registeredUpdateMetricsList);

        // Filter Metrics Update for Their Type and create a List of resulting metrics updates
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(2, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(2, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.PHYSICAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(12)
    public void testDigitalAdapterDigitalTwinStateProcessing() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_EXEC_TIME))
                .collect(Collectors.toList());

        // The target Number of Computed State is:
        // NOT COUNTED 1 (initial creation after PAD) - NOT PROPAGATED FOR THE CURRENT IMPLEMENTATION BY THE DT MODEL
        // 10 DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES Property Variations
        // 2 Relationship Instance Created
        // 2 Relationship Instance Deleted

        int totalTargetStateCount = DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + 2 + 2;

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(totalTargetStateCount, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(totalTargetStateCount, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(13)
    public void testDigitalAdapterEventNotification() throws InterruptedException {

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
        List<WldtMetric> metricExecutionTime = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_EXEC_TIME))
                .collect(Collectors.toList());

        //Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, metricExecutionTime.size());

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

    @Test
    @Order(14)
    public void testDigitalAdapterActionProcessing() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");

        Thread.sleep(2000);

        //Emulate Digital Action on the Digital Adapter
        demoDigitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");

        Thread.sleep(2000);

        // Retrieve Shared Stats Components
        List<WldtMetricComponent> registeredComponentList = SharedTestMetrics.getInstance().getMonitoringRegisteredComponentList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredMetricList = SharedTestMetrics.getInstance().getMonitoringRegisteredMetricList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredUpdateMetricsList = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(TEST_DIGITAL_TWIN_ID);

        // Check Registered Stats are not null
        assertNotNull(registeredComponentList);
        assertNotNull(registeredMetricList);
        assertNotNull(registeredUpdateMetricsList);

        // Filter Processing Success Count
        List<WldtMetric> successCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_SUCCESS_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(2, successCount.size());

        // Filter Processing Error Count
        List<WldtMetric> errorCount = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DIGITAL_ADAPTER) && m.getName().equals(CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_ERROR_COUNT))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(0, errorCount.size());

        Thread.sleep(2000);
    }

}

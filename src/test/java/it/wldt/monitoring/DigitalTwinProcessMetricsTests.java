package it.wldt.monitoring;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.handler.DefaultAugmentationFunctionHandler;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.request.AugmentationFunctionRequestType;
import it.wldt.augmentation.stateless.function.RandomNumberAugmentationFunction;
import it.wldt.augmentation.stateless.function.ThrowingStatelessAugmentationFunction;
import it.wldt.augmentation.stateless.generic.GenericResultAugmentationDigitalTwinModel;
import it.wldt.augmentation.stateful.function.StatefulPeriodicRandomNumberAugmentationFunction;
import it.wldt.augmentation.stateful.generic.StatefulAutoStopAugmentationDigitalTwinModel;
import it.wldt.augmentation.stateful.generic.StatefulGenericResultAugmentationDigitalTwinModel;
import it.wldt.storage.DefaultWldtStorage;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.*;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.handler.TestMonitoringInterfaceHandler;
import it.wldt.monitoring.metrics.*;
import it.wldt.process.DemoProcessTester;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoDigitalTwinModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @Order(15)
    public void testLifeCycleMetrics() throws InterruptedException, WldtEngineException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        Thread.sleep(2000);

        // Retrieve Shared Stats Components
        List<WldtMetricComponent> registeredComponentList = SharedTestMetrics.getInstance().getMonitoringRegisteredComponentList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredMetricList = SharedTestMetrics.getInstance().getMonitoringRegisteredMetricList(TEST_DIGITAL_TWIN_ID);
        List<WldtMetric> registeredUpdateMetricsList = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(TEST_DIGITAL_TWIN_ID);

        // Check Registered Stats are not null
        assertNotNull(registeredComponentList);
        assertNotNull(registeredMetricList);
        assertNotNull(registeredUpdateMetricsList);

        // Stop the Digital Twin
        digitalTwinEngine.stopDigitalTwin(TEST_DIGITAL_TWIN_ID);

        Thread.sleep(5000);

        // Filter Gauge Metric
        List<WldtMetric> lifecycleVariationMetricList = registeredUpdateMetricsList.stream()
                .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL) && m.getName().equals(CoreMonitoringUtils.LIFE_CYCLE_VALUE))
                .collect(Collectors.toList());

        // Check the number of execution is equals to the number of generated PA Property Variation Events
        assertEquals(7, lifecycleVariationMetricList.size());

        // Verify the order of Life Cycle Values received in the experiment
        // Target Order
        // 3 CREATED
        // 4 STARTED
        // 7 BOUND
        // 8 SYNC
        // 5 UNBOUND
        // 2 STOPPED
        // 1 DESTROYED
        List<Integer> expectedLifeCycleSequence = Arrays.asList(3, 4, 7, 8, 5, 2, 1);

        // Check the received metric value list with the target expected values
        //List<Integer> actualLifeCycleSequence = lifecycleVariationMetricList.stream()
        //        .map(metric -> ((Number) ((WldtGauge) metric).getValue()).intValue())
        //        .collect(Collectors.toList());

        List<Integer> actualLifeCycleSequence = lifecycleVariationMetricList.stream()
                .map(metric -> ((Number) ((WldtUpDownCounter) metric).getValue()).intValue())
                .collect(Collectors.toList());

        assertEquals(expectedLifeCycleSequence, actualLifeCycleSequence);

        Thread.sleep(2000);
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Stateless
    // -------------------------------------------------------------------------

    @Nested
    class StatelessAugmentationMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugStateless";
        private static final String HANDLER_ID = "test-stateless-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, it.wldt.exception.AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new GenericResultAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new RandomNumberAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(16)
        public void testStatelessHandlerRegistrationMetrics() throws InterruptedException {
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS));

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // 1 stateless function registered in handler
            List<WldtMetric> registeredStatelessCount = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_HANDLER_REGISTERED_STATELESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, registeredStatelessCount.size());

            // 1 handler added to AugmentationManager
            List<WldtMetric> handlerCount = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AUGMENTATION_FUNCTION_HANDLER_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, handlerCount.size());

            Thread.sleep(2000);
        }

        @Test
        @Order(17)
        public void testStatelessFunctionExecutionMetrics() throws InterruptedException {
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS));

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            int N = DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES;

            // Function-level: success count per each property variation
            List<WldtMetric> execSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATELESS_EXEC_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(N, execSuccess.size());

            // Function-level: execution timer updated same number of times
            List<WldtMetric> execTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATELESS_EXEC_TIME))
                    .collect(Collectors.toList());
            assertEquals(N, execTime.size());

            // DT Model-level: executeAugmentationFunction called once per property variation
            List<WldtMetric> modelExecSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_STATELESS_EXEC_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(N, modelExecSuccess.size());

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Stateful
    // -------------------------------------------------------------------------

    @Nested
    class StatefulAugmentationMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugStateful";
        private static final String HANDLER_ID = "test-stateful-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, it.wldt.exception.AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new StatefulGenericResultAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            // StatefulGenericResultAugmentationDigitalTwinModel auto-starts this function
            // when onAugmentationFunctionListAvailable() fires
            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(18)
        public void testStatefulFunctionStartMetrics() throws InterruptedException {
            // Wait for: PA binding + PAD notification + model to call startAugmentationFunction
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS));

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // 1 stateful function registered in handler
            List<WldtMetric> registeredStatefulCount = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_HANDLER_REGISTERED_STATEFUL_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, registeredStatefulCount.size());

            // Function-level: start success
            List<WldtMetric> startSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, startSuccess.size());

            // Function-level: start timer
            List<WldtMetric> startTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_EXEC_TIME))
                    .collect(Collectors.toList());
            assertEquals(1, startTime.size());

            // Handler-level: running count incremented
            List<WldtMetric> runningCount = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_HANDLER_STATEFUL_RUNNING_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, runningCount.size());

            // DT Model-level: startAugmentationFunction called once
            List<WldtMetric> modelStartSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_STATEFUL_START_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, modelStartSuccess.size());

            Thread.sleep(2000);
        }

        @Test
        @Order(19)
        public void testStatefulFunctionStateUpdateMetrics() throws InterruptedException {
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS));

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // N state changes = N property updates + relationship creation/deletion (at minimum N updates)
            int N = DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES;

            // Function-level: state update success count — at least N (one per property variation state change)
            List<WldtMetric> stateUpdateSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertNotNull(stateUpdateSuccess);
            assertTrue(stateUpdateSuccess.size() >= N);

            // Function-level: state update timer updated same number of times
            List<WldtMetric> stateUpdateTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_EXEC_TIME))
                    .collect(Collectors.toList());
            assertNotNull(stateUpdateTime);
            assertEquals(stateUpdateSuccess.size(), stateUpdateTime.size());

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Stateful Stop (handler + function level)
    // -------------------------------------------------------------------------

    @Nested
    class StatefulFunctionStopMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugStatefulStop";
        private static final String HANDLER_ID = "test-stateful-stop-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new StatefulAutoStopAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(20)
        public void testStatefulFunctionStopMetrics() throws InterruptedException, AugmentationFunctionException {
            // Wait for function to start (triggered by model on onAugmentationFunctionListAvailable)
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS * 3);

            // Call stop directly on the handler — tests function-level and handler-level stop metrics
            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .stopAugmentationFunction(
                                StatefulPeriodicRandomNumberAugmentationFunction.FUNCTION_ID,
                                new AugmentationFunctionRequest(
                                        java.util.UUID.randomUUID().toString(),
                                        new AugmentationFunctionContext(),
                                        AugmentationFunctionRequestType.STOP));

            Thread.sleep(1000);

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // Function-level: stop success = 1
            List<WldtMetric> stopSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, stopSuccess.size());

            // Function-level: stop timer updated = 1
            List<WldtMetric> stopTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_EXEC_TIME))
                    .collect(Collectors.toList());
            assertEquals(1, stopTime.size());

            // Handler-level: running count updated at least twice (increment on start + decrement on stop)
            List<WldtMetric> runningCount = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_HANDLER_STATEFUL_RUNNING_COUNT))
                    .collect(Collectors.toList());
            assertTrue(runningCount.size() >= 2);

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Stateful Stop (DT Model level)
    // -------------------------------------------------------------------------

    @Nested
    class StatefulDtModelStopMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugStatefulDtStop";
        private static final String HANDLER_ID = "test-stateful-dt-stop-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new StatefulAutoStopAugmentationDigitalTwinModel(true));

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(21)
        public void testStatefulDtModelStopMetrics() throws InterruptedException {
            // Wait for: start + first periodic result (1s period) + auto-stop callback
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS) + 3000);

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // DT Model level: stop success = 1 (model called this.stopAugmentationFunction on first result)
            List<WldtMetric> modelStopSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_STATEFUL_STOP_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, modelStopSuccess.size());

            // Function-level: stop success = 1
            List<WldtMetric> funcStopSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, funcStopSuccess.size());

            // Function-level: stop timer = 1
            List<WldtMetric> funcStopTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_EXEC_TIME))
                    .collect(Collectors.toList());
            assertEquals(1, funcStopTime.size());

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Query execution (function level)
    // -------------------------------------------------------------------------

    @Nested
    class StatefulQueryMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugQuery";
        private static final String HANDLER_ID = "test-stateful-query-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, AugmentationFunctionException, it.wldt.exception.StorageException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new StatefulAutoStopAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            // Storage required so syncQueryExecute succeeds (returns empty result, not exception)
            augDigitalTwin.getStorageManager().putStorage(new DefaultWldtStorage("test-query-storage", true));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent()) {
                // Configure function with a queryRequest so handler executes a query on start
                StatefulPeriodicRandomNumberAugmentationFunction function =
                        new StatefulPeriodicRandomNumberAugmentationFunction();
                QueryRequest queryRequest = new QueryRequest();
                queryRequest.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
                function.getContextRequest().setQueryRequest(queryRequest);
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(function);
            }

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(22)
        public void testStatefulQueryMetrics() throws InterruptedException {
            // Wait for: DT sync + start event processed (query executed during start)
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS * 5);

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // Function-level: query success >= 1 (executed during start flow)
            List<WldtMetric> querySuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertTrue(querySuccess.size() >= 1);

            // Function-level: query timer updated same number of times
            List<WldtMetric> queryTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_TIME))
                    .collect(Collectors.toList());
            assertTrue(queryTime.size() >= 1);

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — DT Model callback metrics
    // -------------------------------------------------------------------------

    @Nested
    class DtModelCallbackMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugCallbacks";
        private static final String HANDLER_ID = "test-callback-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new StatefulAutoStopAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(23)
        public void testDtModelCallbackMetrics() throws InterruptedException {
            // Wait for: bind + start + at least 1 periodic result (1s period)
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS) + 2000);

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // DT Model: result callback success >= 1 (from onAugmentationFunctionResultEvent)
            List<WldtMetric> resultSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_RESULT_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertTrue(resultSuccess.size() >= 1);

            // DT Model: result exec timer >= 1
            List<WldtMetric> resultTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_RESULT_EXEC_TIME))
                    .collect(Collectors.toList());
            assertTrue(resultTime.size() >= 1);

            // DT Model: list registered = 1 (from onAugmentationFunctionListAvailable — fires once at start)
            List<WldtMetric> listRegisteredSuccess = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_LIST_REGISTERED_SUCCESS_COUNT))
                    .collect(Collectors.toList());
            assertEquals(1, listRegisteredSuccess.size());

            // DT Model: list registered timer = 1
            List<WldtMetric> listRegisteredTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.DT_MODEL)
                            && m.getName().equals(CoreMonitoringUtils.AF_LIST_REGISTERED_EXEC_TIME))
                    .collect(Collectors.toList());
            assertEquals(1, listRegisteredTime.size());

            Thread.sleep(2000);
        }
    }

    // -------------------------------------------------------------------------
    // Augmentation Function Metrics — Stateless error path
    // -------------------------------------------------------------------------

    @Nested
    class StatelessErrorPathMetricsTests {

        private static final String AUG_DT_ID = "dtTestAugStatelessError";
        private static final String HANDLER_ID = "test-stateless-error-handler";

        private DigitalTwin augDigitalTwin = null;
        private DigitalTwinEngine augDigitalTwinEngine = null;

        @BeforeEach
        public void setUp() throws KernelException, WldtRuntimeException, EventBusException,
                WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException,
                WldtEngineException, AugmentationFunctionException {

            SharedTestMetrics.getInstance().registerDigitalTwin(AUG_DT_ID);

            augDigitalTwinEngine = new DigitalTwinEngine();
            augDigitalTwin = new DigitalTwin(AUG_DT_ID, new GenericResultAugmentationDigitalTwinModel());

            augDigitalTwin.addPhysicalAdapter(
                    new DemoPhysicalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "pa"),
                            new DemoPhysicalAdapterConfiguration(),
                            true,
                            true));

            augDigitalTwin.addDigitalAdapter(
                    new DemoDigitalAdapter(
                            String.format("%s-%s", AUG_DT_ID, "da"),
                            new DemoDigitalAdapterConfiguration()));

            AugmentationFunctionHandler handler = new DefaultAugmentationFunctionHandler(HANDLER_ID);
            augDigitalTwin.getAugmentationManager().addAugmentationFunctionHandler(handler);

            if (augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID).isPresent())
                augDigitalTwin.getAugmentationManager().getAugmentationFunctionHandler(HANDLER_ID)
                        .get()
                        .registerAugmentationFunction(new ThrowingStatelessAugmentationFunction());

            augDigitalTwin.getMonitoringInterface().setConfiguration(
                    new MonitoringInterfaceConfiguration.Builder()
                            .withDtModelMonitoring()
                            .withAugmentationMonitoring()
                            .build());

            augDigitalTwin.getMonitoringInterface().setHandler(new TestMonitoringInterfaceHandler(AUG_DT_ID));

            augDigitalTwinEngine.addDigitalTwin(augDigitalTwin);
            augDigitalTwinEngine.startDigitalTwin(AUG_DT_ID);
        }

        @AfterEach
        public void tearDown() throws WldtEngineException, InterruptedException {
            augDigitalTwinEngine.stopDigitalTwin(AUG_DT_ID);
            augDigitalTwinEngine.removeDigitalTwin(AUG_DT_ID);
            augDigitalTwin = null;
            augDigitalTwinEngine = null;
            Thread.sleep(500);
            SharedTestMetrics.getInstance().resetMetrics();
            SharedTestMetrics.getInstance().unRegisterDigitalTwin(AUG_DT_ID);
        }

        @Test
        @Order(24)
        public void testStatelessErrorMetrics() throws InterruptedException {
            // Wait for N property variation events (each triggers executeAugmentationFunction
            // which calls ThrowingStatelessAugmentationFunction.run() -> throws -> error metric)
            Thread.sleep(DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS +
                    ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES +
                            DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) *
                            DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS));

            List<WldtMetric> updatedMetrics = SharedTestMetrics.getInstance().getMonitoringUpdatedMetricList(AUG_DT_ID);
            assertNotNull(updatedMetrics);

            // Function-level: error count >= 1
            List<WldtMetric> execError = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATELESS_EXEC_ERROR_COUNT))
                    .collect(Collectors.toList());
            assertTrue(execError.size() >= 1);

            // Function-level: timer still updated even on error path
            List<WldtMetric> execTime = updatedMetrics.stream()
                    .filter(m -> m.getComponent().equals(WldtMetricComponent.AUGMENTATION)
                            && m.getName().equals(CoreMonitoringUtils.AF_FUNCTION_STATELESS_EXEC_TIME))
                    .collect(Collectors.toList());
            assertTrue(execTime.size() >= 1);

            Thread.sleep(2000);
        }
    }
}

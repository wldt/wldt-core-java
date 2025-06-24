package it.wldt.management;

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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 30/05/2025 - 10:46
 */
public class ManagementInterfaceTester {

    private static final Logger logger = LoggerFactory.getLogger(ManagementInterfaceTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoManagementInterface managementInterface = null;

    public static final String RESOURCE_ID = "resource1";

    private static final String RESOURCE_TYPE = "wldt.test.dictionary";

    private static final String RESOURCE_NAME = "Test Dictionary Resource";

    private static final String PROPERTY_NAME_1 = "property1";

    private static final String PROPERTY_NAME_2 = "property2";

    private static final String PROPERTY_NAME_3 = "property3";

    private static final String PROPERTY_NAME_4 = "property4";

    private static final String PROPERTY_VALUE_1 = "value1";

    private static final String PROPERTY_VALUE_1_UPDATED = "value1_updated";

    private static final double PROPERTY_VALUE_2 = 42.0;

    private static final boolean PROPERTY_VALUE_3 = true;

    private static final String PROPERTY_VALUE_4 = "newValue";

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        logger.info("Setting up Test Environment ...");

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoShadowingFunction());
        //digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoShadowingFunctionResourceTest());

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

        // Create our test resource as a Map with configuration properties
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put(PROPERTY_NAME_1, PROPERTY_VALUE_1);
        configurationProperties.put(PROPERTY_NAME_2, PROPERTY_VALUE_2);
        configurationProperties.put(PROPERTY_NAME_3, PROPERTY_VALUE_3);

        // Create a new Managed Resource to be used by the Digital Twin and managed by the Management Interface
        DictionaryManagedResource dictionaryManagedResource = new DictionaryManagedResource(
                RESOURCE_ID,
                RESOURCE_TYPE,
                RESOURCE_NAME,
                configurationProperties);

        // Add a Managed Resource to the Digital Twin
        digitalTwin.getResourceManager().addResource(dictionaryManagedResource);

        // Create a new Management Interface
        managementInterface = new DemoManagementInterface();

        // Add the Management Interface to the Digital Twin
        digitalTwin.setManagementInterface(managementInterface);

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
    public void testReadResourceFromManagementInterface() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Reading Resource through the Management Interface ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Create the Resource Request to read the Resource from the Interface
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.READ_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert that the Resource Response contains the expected Resource ID
        assertNotNull(resourceResponseOptional.get().getResourceId(), "Resource ID should not be null");

        // Assert the resource response contains the expected values
        assertInstanceOf(Map.class, resourceResponseOptional.get().getResource(), "Resource should be of type Map");

        // Assert that the resource contains the expected properties
        Map<String, Object> resourceContent = (Map<String, Object>) resourceResponseOptional.get().getResource();
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_1), "Resource should contain property: " + PROPERTY_NAME_1);
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_2), "Resource should contain property: " + PROPERTY_NAME_2);
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_3), "Resource should contain property: " + PROPERTY_NAME_3);
        assertEquals(PROPERTY_VALUE_1, resourceContent.get(PROPERTY_NAME_1), "Property value for " + PROPERTY_NAME_1 + " should match");
        assertEquals(PROPERTY_VALUE_2, resourceContent.get(PROPERTY_NAME_2), "Property value for " + PROPERTY_NAME_2 + " should match");
        assertEquals(PROPERTY_VALUE_3, resourceContent.get(PROPERTY_NAME_3), "Property value for " + PROPERTY_NAME_3 + " should match");

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testReadSubResourceFromManagementInterface() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Reading SubResource through the Management Interface ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Create the Resource Request to read the Resource from the Interface
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_1);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.READ_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert that the Resource Response contains the expected Resource ID
        assertNotNull(resourceResponseOptional.get().getResourceId(), "Resource ID should not be null");

        // Assert the resource response contains the expected values
        assertInstanceOf(String.class, resourceResponseOptional.get().getResource(), "SubResource should be of type String");

        // Assert that the resource contains the expected value
        String subResourceContent = (String) resourceResponseOptional.get().getResource();
        assertEquals(PROPERTY_VALUE_1, subResourceContent, "SubResource value for " + PROPERTY_NAME_1 + " should match");

        Thread.sleep(2000);
    }

    @Test
    @Order(3)
    public void testWriteResourceFromManagementInterface() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Writing Resource through the Management Interface ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Create the Resource Request to create a Resource from the Interface
        // Since the main resource is a Map and we want to create a new property we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_4);

        // Now we set the content of the request to the new value we want to set for the property
        resourceRequest.setContent(PROPERTY_VALUE_4);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.CREATE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(String.class, resourceResponseOptional.get().getResource(), "SubResource should be of type String");

        // Assert that the resource contains the expected value
        String subResourceContent = (String) resourceResponseOptional.get().getResource();
        assertEquals(PROPERTY_VALUE_4, subResourceContent, "SubResource value for " + PROPERTY_NAME_4 + " should match");

        // Wait for the resource to be updated
        Thread.sleep(2000);
    }

    @Test
    @Order(4)
    public void testUpdateResourceFromManagementInterface() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Updating Resource through the Management Interface ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Create the Resource Request to read the Resource from the Interface
        // Since the main resource is a Map and we want to update a property we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_1);

        // Now we set the content of the request to the new value we want to set for the property
        resourceRequest.setContent(PROPERTY_VALUE_1_UPDATED);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.UPDATE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(String.class, resourceResponseOptional.get().getResource(), "SubResource should be of type String");

        // Assert that the resource contains the expected value
        String subResourceContent = (String) resourceResponseOptional.get().getResource();
        assertEquals(PROPERTY_VALUE_1_UPDATED, subResourceContent, "SubResource value for " + PROPERTY_NAME_1 + " should match");

        // Wait for the resource to be updated
        Thread.sleep(2000);
    }

    @Test
    @Order(5)
    public void testDeleteResourceFromManagementInterface() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Deleting Resource through the Management Interface ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Create the Resource Request to read the Resource from the Interface
        // Since the main resource is a Map and we want to update a property we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_1);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.DELETE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Wait for the resource to be updated
        Thread.sleep(2000);

        // Handle a new Resource Request to read the entire Dictionary Resource
        resourceRequest = new ResourceRequest<>(RESOURCE_ID);

        // Test reading a resource from the Management Interface
        resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.READ_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert that the Resource Response contains the expected Resource ID
        assertNotNull(resourceResponseOptional.get().getResourceId(), "Resource ID should not be null");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(Map.class, resourceResponseOptional.get().getResource(), "Resource should be of type Map");

        // Assert the number of properties in the resource and then check the remaining properties
        Map<String, Object> resourceContent = (Map<String, Object>) resourceResponseOptional.get().getResource();
        assertEquals(2, resourceContent.size(), "Resource should contain 2 properties after deletion");
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_2), "Resource should contain property: " + PROPERTY_NAME_2);
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_3), "Resource should contain property: " + PROPERTY_NAME_3);
        assertEquals(PROPERTY_VALUE_2, resourceContent.get(PROPERTY_NAME_2), "Property value for " + PROPERTY_NAME_2 + " should match");
        assertEquals(PROPERTY_VALUE_3, resourceContent.get(PROPERTY_NAME_3), "Property value for " + PROPERTY_NAME_3 + " should match");

        Thread.sleep(2000);
    }

    @Test
    @Order(6)
    public void testWriteResourceFromManagementInterfaceWithCallback() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Writing Resource through the Management Interface with Callback ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // ------ Retrieve and Add the Callback to the specific Managed Resource ------

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Add a callback to the Managed Resource
        dictionaryManagedResource.addObserver(new IResourceObserver() {
            @Override
            public void onCreate(String resourceId, String subResourceId) {
                // Save to the Shared Test Metrics the notification of the new sub-resource created
                SharedTestMetrics.getInstance().addManagedResourceNotification(TEST_DIGITAL_TWIN_ID, resourceId, subResourceId);
            }

            @Override
            public void onUpdate(String resourceId, String subResourceId) {

            }

            @Override
            public void onDelete(String resourceId, String subResourceId) {

            }
        });

        // Create the Resource Request to read the Resource from the Interface
        // Since the main resource is a Map and we want to create a new roperty we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_4);

        // Now we set the content of the request to the new value we want to set for the property
        resourceRequest.setContent(PROPERTY_VALUE_4);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.CREATE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(String.class, resourceResponseOptional.get().getResource(), "SubResource should be of type String");

        // Assert that the resource contains the expected value
        String subResourceContent = (String) resourceResponseOptional.get().getResource();
        assertEquals(PROPERTY_VALUE_4, subResourceContent, "SubResource value for " + PROPERTY_NAME_4 + " should match");

        // Wait for the resource to be updated
        Thread.sleep(2000);

        // Check that the callback was triggered and the notification was saved in the Shared Test Metrics
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).containsKey(RESOURCE_ID), "Resource Manager Notification Map should contain the resource ID after addition");
        assertEquals(PROPERTY_NAME_4, SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).get(RESOURCE_ID), "Resource Manager Notification Map should contain the sub-resource ID after addition");

    }

    @Test
    @Order(7)
    public void testUpdateResourceFromManagementInterfaceWithCallback() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Updating Resource through the Management Interface with Callback ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // ------ Retrieve and Add the Callback to the specific Managed Resource ------

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Add a callback to the Managed Resource
        dictionaryManagedResource.addObserver(new IResourceObserver() {
            @Override
            public void onCreate(String resourceId, String subResourceId) {
            }

            @Override
            public void onUpdate(String resourceId, String subResourceId) {
                // Save to the Shared Test Metrics the notification of the new sub-resource created
                SharedTestMetrics.getInstance().addManagedResourceNotification(TEST_DIGITAL_TWIN_ID, resourceId, subResourceId);
            }

            @Override
            public void onDelete(String resourceId, String subResourceId) {

            }
        });

        // Create the Resource Request to read the Resource from the Interface
        // Since the main resource is a Map and we want to update a property we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_1);

        // Now we set the content of the request to the new value we want to set for the property
        resourceRequest.setContent(PROPERTY_VALUE_1_UPDATED);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.UPDATE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(String.class, resourceResponseOptional.get().getResource(), "SubResource should be of type String");

        // Assert that the resource contains the expected value
        String subResourceContent = (String) resourceResponseOptional.get().getResource();
        assertEquals(PROPERTY_VALUE_1_UPDATED, subResourceContent, "SubResource value for " + PROPERTY_NAME_1 + " should match");

        // Wait for the resource to be updated
        Thread.sleep(2000);

        // Check that the callback was triggered and the notification was saved in the Shared Test Metrics
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).containsKey(RESOURCE_ID), "Resource Manager Notification Map should contain the resource ID after addition");
        assertEquals(PROPERTY_NAME_1, SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).get(RESOURCE_ID), "Resource Manager Notification Map should contain the sub-resource ID after addition");
    }

    @Test
    @Order(8)
    public void testDeleteResourceFromManagementInterfaceWithCallback() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Deleting Resource through the Management Interface with callback...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // ------ Retrieve and Add the Callback to the specific Managed Resource ------

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Add a callback to the Managed Resource
        dictionaryManagedResource.addObserver(new IResourceObserver() {
            @Override
            public void onCreate(String resourceId, String subResourceId) {
            }

            @Override
            public void onUpdate(String resourceId, String subResourceId) {
            }

            @Override
            public void onDelete(String resourceId, String subResourceId) {
                // Save to the Shared Test Metrics the notification of the new sub-resource created
                SharedTestMetrics.getInstance().addManagedResourceNotification(TEST_DIGITAL_TWIN_ID, resourceId, subResourceId);
            }
        });

        // Create the Resource Request to read the Resource from the Interface
        // Since the main resource is a Map and we want to update a property we have to set the sub-resource ID as
        // the new property name that we want to add or update
        ResourceRequest<String> resourceRequest = new ResourceRequest<>(RESOURCE_ID, PROPERTY_NAME_1);

        // Test reading a resource from the Management Interface
        Optional<ResourceResponse<?>> resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.DELETE_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Wait for the resource to be updated
        Thread.sleep(2000);

        // Handle a new Resource Request to read the entire Dictionary Resource
        resourceRequest = new ResourceRequest<>(RESOURCE_ID);

        // Test reading a resource from the Management Interface
        resourceResponseOptional = managementInterface.emulateIncomingRequest(DemoManagementInterface.READ_REQUEST, resourceRequest);

        // Check if the Resource Response is present
        if (resourceResponseOptional.isPresent()) {
            ResourceResponse<?> response = resourceResponseOptional.get();
            logger.info("Resource Response: {}", response);
            // Check if the Resource is present in the Response
            if (response.getResource() != null) {
                logger.info("Resource Content: {}", response.getResource());
            } else {
                logger.warn("Resource Content is null");
            }
        } else {
            logger.error("Resource Response is not present");
        }

        // Assert that the Resource Response is present
        assertNotNull(resourceResponseOptional, "Resource Response should not be null");

        // Assert that the Optional Resource Response is present
        assertTrue(resourceResponseOptional.isPresent(), "Resource Response should be present");

        // Assert that the Resource Response contains the expected Resource ID
        assertNotNull(resourceResponseOptional.get().getResourceId(), "Resource ID should not be null");

        // Assert Resource Response is not error
        assertFalse(resourceResponseOptional.get().isError(), "Resource Response should not be an error response");

        // Assert the resource response contains the expected values
        assertInstanceOf(Map.class, resourceResponseOptional.get().getResource(), "Resource should be of type Map");

        // Assert the number of properties in the resource and then check the remaining properties
        Map<String, Object> resourceContent = (Map<String, Object>) resourceResponseOptional.get().getResource();
        assertEquals(2, resourceContent.size(), "Resource should contain 2 properties after deletion");
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_2), "Resource should contain property: " + PROPERTY_NAME_2);
        assertTrue(resourceContent.containsKey(PROPERTY_NAME_3), "Resource should contain property: " + PROPERTY_NAME_3);
        assertEquals(PROPERTY_VALUE_2, resourceContent.get(PROPERTY_NAME_2), "Property value for " + PROPERTY_NAME_2 + " should match");
        assertEquals(PROPERTY_VALUE_3, resourceContent.get(PROPERTY_NAME_3), "Property value for " + PROPERTY_NAME_3 + " should match");

        Thread.sleep(2000);

        // Check that the callback was triggered and the notification was saved in the Shared Test Metrics
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).containsKey(RESOURCE_ID), "Resource Manager Notification Map should contain the resource ID after addition");
        assertEquals(PROPERTY_NAME_1, SharedTestMetrics.getInstance().getManagedResourceNotificationMap().get(TEST_DIGITAL_TWIN_ID).get(RESOURCE_ID), "Resource Manager Notification Map should contain the sub-resource ID after addition");
    }

}

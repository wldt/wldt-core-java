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
public class ResourceManagerTester {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerTester.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    private DemoManagementInterface managementInterface = null;

    public static final String RESOURCE_ID = "resource1";

    public static final String RESOURCE_ID_2 = "resource2";

    private static final String RESOURCE_TYPE = "wldt.test.dictionary";

    private static final String RESOURCE_NAME = "Test Dictionary Resource";

    private static final String PROPERTY_NAME_1 = "property1";

    private static final String PROPERTY_NAME_2 = "property2";

    private static final String PROPERTY_NAME_3 = "property3";

    private static final String PROPERTY_NAME_4 = "property4";

    private static final String PROPERTY_NAME_5 = "property5";

    private static final String PROPERTY_VALUE_1 = "value1";

    private static final String PROPERTY_VALUE_1_UPDATED = "value1_updated";

    private static final double PROPERTY_VALUE_2 = 42.0;

    private static final boolean PROPERTY_VALUE_3 = true;

    private static final String PROPERTY_VALUE_4 = "value_4";

    private static final String PROPERTY_VALUE_5 = "value_5";

    @BeforeEach
    public void setUp() throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        logger.info("Setting up Test Environment ...");

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoShadowingFunction());

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
    public void testReadResourceFromResourceManager() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Reading Resource from the Resource Manager ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

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

        // Assert that the count of properties in the resource is correct
        assertEquals(3, dictionaryManagedResource.getResource().size(), "Resource should contain 3 properties");

        // Assert that the resource contains the expected properties and values
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_1), "Resource should contain property: " + PROPERTY_NAME_1);
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_2), "Resource should contain property: " + PROPERTY_NAME_2);
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_3), "Resource should contain property: " + PROPERTY_NAME_3);
        assertEquals(PROPERTY_VALUE_1, dictionaryManagedResource.getResource().get(PROPERTY_NAME_1), "Property value for " + PROPERTY_NAME_1 + " should match");
        assertEquals(PROPERTY_VALUE_2, dictionaryManagedResource.getResource().get(PROPERTY_NAME_2), "Property value for " + PROPERTY_NAME_2 + " should match");
        assertEquals(PROPERTY_VALUE_3, dictionaryManagedResource.getResource().get(PROPERTY_NAME_3), "Property value for " + PROPERTY_NAME_3 + " should match");

        Thread.sleep(2000);
    }

    @Test
    @Order(2)
    public void testAddResourceToResourceManager() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Adding new Resource to the Resource Manager ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Add an Observer to the Resource Manager
        digitalTwin.getResourceManager().addObserver(new IResourceManagerObserver() {
            @Override
            public void onManagerResourceAdded(String resourceId) {
                SharedTestMetrics.getInstance().addResourceManagerNotification(TEST_DIGITAL_TWIN_ID, resourceId);
            }

            @Override
            public void onManagerResourceRemoved(String resourceId) {
            }

            @Override
            public void onManagerResourceUpdated(String resourceId) {

            }

            @Override
            public void onManagerResourceListCleared() {

            }
        });

        // Create a new Managed Resource to be used by the Digital Twin and managed by the Management Interface
        // always using the same class DictionaryManagedResource but with different properties
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put(PROPERTY_NAME_4, PROPERTY_VALUE_4);

        // Create the new DictionaryManagedResource with the new properties
        DictionaryManagedResource dictionaryManagedResource2 = new DictionaryManagedResource(
                RESOURCE_ID_2,
                RESOURCE_TYPE,
                "Test Dictionary Resource 2",
                configurationProperties);

        // Add the new Managed Resource to the Digital Twin
        digitalTwin.getResourceManager().addResource(dictionaryManagedResource2);

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Assert that the count of properties in the resource is correct
        assertEquals(1, dictionaryManagedResource.getResource().size(), "Resource should contain 1 properties");

        // Assert that the resource contains the expected properties and values
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_4), "Resource should contain property: " + PROPERTY_NAME_4);
        assertEquals(PROPERTY_VALUE_4, dictionaryManagedResource.getResource().get(PROPERTY_NAME_4), "Property value for " + PROPERTY_NAME_4 + " should match");

        Thread.sleep(2000);

        // Check if the notification has been received
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().get(TEST_DIGITAL_TWIN_ID).contains(RESOURCE_ID_2), "Resource Manager Notification Map should contain the resource ID after addition");
    }

    @Test
    @Order(3)
    public void testUpdateResourceToResourceManager() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Adding new Resource to the Resource Manager ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Add an Observer to the Resource Manager
        digitalTwin.getResourceManager().addObserver(new IResourceManagerObserver() {
            @Override
            public void onManagerResourceAdded(String resourceId) {
            }

            @Override
            public void onManagerResourceRemoved(String resourceId) {

            }

            @Override
            public void onManagerResourceUpdated(String resourceId) {
                SharedTestMetrics.getInstance().addResourceManagerNotification(TEST_DIGITAL_TWIN_ID, resourceId);
            }

            @Override
            public void onManagerResourceListCleared() {

            }
        });

        // Create a new Managed Resource to be used by the Digital Twin and managed by the Management Interface
        // always using the same class DictionaryManagedResource but with different properties
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put(PROPERTY_NAME_4, PROPERTY_VALUE_4);

        // Create the new DictionaryManagedResource with the new properties
        DictionaryManagedResource dictionaryManagedResource2 = new DictionaryManagedResource(
                RESOURCE_ID_2,
                RESOURCE_TYPE,
                "Test Dictionary Resource 2",
                configurationProperties);

        // Add the new Managed Resource to the Digital Twin
        digitalTwin.getResourceManager().addResource(dictionaryManagedResource2);

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Assert that the count of properties in the resource is correct
        assertEquals(1, dictionaryManagedResource.getResource().size(), "Resource should contain 1 properties");

        // Assert that the resource contains the expected properties and values
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_4), "Resource should contain property: " + PROPERTY_NAME_4);
        assertEquals(PROPERTY_VALUE_4, dictionaryManagedResource.getResource().get(PROPERTY_NAME_4), "Property value for " + PROPERTY_NAME_4 + " should match");

        Thread.sleep(2000);

        // Now we will update the first resource with a new property

        // Read the resource from the Resource Manager
        resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Cast the Resource to DictionaryManagedResource
        dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Update the resource by adding a new property
        dictionaryManagedResource.getResource().put(PROPERTY_NAME_5, PROPERTY_VALUE_5);

        // Update the resource in the Resource Manager
        digitalTwin.getResourceManager().updateResource(dictionaryManagedResource);

        Thread.sleep(2000);

        // Read the resource from the Resource Manager again
        resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null after update");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present after update");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Assert that the count of properties in the resource is correct
        assertEquals(2, dictionaryManagedResource.getResource().size(), "Resource should contain 2 properties after update");

        // Assert that the resource contains the expected properties and values
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_4), "Resource should contain property: " + PROPERTY_NAME_4);
        assertEquals(PROPERTY_VALUE_4, dictionaryManagedResource.getResource().get(PROPERTY_NAME_4), "Property value for " + PROPERTY_NAME_4 + " should match after update");
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_5), "Resource should contain property: " + PROPERTY_NAME_5);
        assertEquals(PROPERTY_VALUE_5, dictionaryManagedResource.getResource().get(PROPERTY_NAME_5), "Property value for " + PROPERTY_NAME_5 + " should match after update");

        // Check if the update notification has been received
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().get(TEST_DIGITAL_TWIN_ID).contains(RESOURCE_ID_2), "Resource Manager Notification Map should contain the resource ID after update");
    }

    @Test
    @Order(4)
    public void testDeleteResourceFromResourceManager() throws InterruptedException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        logger.info("Testing Removing new Resource to the Resource Manager ...");

        // Wait for the Management Interface to be started
        Thread.sleep(5000);

        // Add an Observer to the Resource Manager
        digitalTwin.getResourceManager().addObserver(new IResourceManagerObserver() {
            @Override
            public void onManagerResourceAdded(String resourceId) {
            }

            @Override
            public void onManagerResourceRemoved(String resourceId) {
                SharedTestMetrics.getInstance().addResourceManagerNotification(TEST_DIGITAL_TWIN_ID, resourceId);
            }

            @Override
            public void onManagerResourceUpdated(String resourceId) {

            }

            @Override
            public void onManagerResourceListCleared() {

            }
        });

        // Create a new Managed Resource to be used by the Digital Twin and managed by the Management Interface
        // always using the same class DictionaryManagedResource but with different properties
        Map<String, Object> configurationProperties = new HashMap<>();
        configurationProperties.put(PROPERTY_NAME_4, PROPERTY_VALUE_4);

        // Create the new DictionaryManagedResource with the new properties
        DictionaryManagedResource dictionaryManagedResource2 = new DictionaryManagedResource(
                RESOURCE_ID_2,
                RESOURCE_TYPE,
                "Test Dictionary Resource 2",
                configurationProperties);

        // Add the new Managed Resource to the Digital Twin
        digitalTwin.getResourceManager().addResource(dictionaryManagedResource2);

        // Read the resource from the Resource Manager
        Optional<ManagedResource<?, ?, ?>> resourceOptional = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is present
        assertNotNull(resourceOptional, "Resource should not be null");

        // Assert that the Optional Resource is present
        assertTrue(resourceOptional.isPresent(), "Resource should be present");

        // Assert that the Resource is of type DictionaryManagedResource
        assertInstanceOf(DictionaryManagedResource.class, resourceOptional.get(), "Resource should be of type DictionaryManagedResource");

        // Cast the Resource to DictionaryManagedResource
        DictionaryManagedResource dictionaryManagedResource = (DictionaryManagedResource) resourceOptional.get();

        // Assert that the count of properties in the resource is correct
        assertEquals(1, dictionaryManagedResource.getResource().size(), "Resource should contain 1 properties");

        // Assert that the resource contains the expected properties and values
        assertTrue(dictionaryManagedResource.getResource().containsKey(PROPERTY_NAME_4), "Resource should contain property: " + PROPERTY_NAME_4);
        assertEquals(PROPERTY_VALUE_4, dictionaryManagedResource.getResource().get(PROPERTY_NAME_4), "Property value for " + PROPERTY_NAME_4 + " should match");

        Thread.sleep(2000);

        // Remove the first resource from the Resource Manager through its ID
        digitalTwin.getResourceManager().removeResource(RESOURCE_ID);

        // Read the resource from the Resource Manager again
        Optional<ManagedResource<?, ?, ?>> resourceOptionalAfterRemoval = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID);

        // Assert that the Resource is not present anymore
        assertNotNull(resourceOptionalAfterRemoval, "Resource Optional should not be null after removal attempt");

        // Assert that the Optional Resource is not present
        assertFalse(resourceOptionalAfterRemoval.isPresent(), "Resource should not be present after removal");

        // For the second resource, we will remove it using the removeResource passing the ManagedResource object
        digitalTwin.getResourceManager().removeResource(dictionaryManagedResource2);

        // Read the resource from the Resource Manager again
        Optional<ManagedResource<?, ?, ?>> resourceOptionalAfterSecondRemoval = digitalTwin.getResourceManager().getResourceById(RESOURCE_ID_2);

        // Assert that the Resource is not present anymore
        assertNotNull(resourceOptionalAfterSecondRemoval, "Resource Optional should not be null after second removal attempt");

        // Assert that the Optional Resource is not present
        assertFalse(resourceOptionalAfterSecondRemoval.isPresent(), "Resource should not be present after second removal");

        Thread.sleep(2000);

        // Check if the removal notification has been received
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().containsKey(TEST_DIGITAL_TWIN_ID), "Resource Manager Notification Map should contain the Digital Twin ID");
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().get(TEST_DIGITAL_TWIN_ID).contains(RESOURCE_ID), "Resource Manager Notification Map should not contain the resource ID after removal");
        assertTrue(SharedTestMetrics.getInstance().getResourceManagerNotificationMap().get(TEST_DIGITAL_TWIN_ID).contains(RESOURCE_ID_2), "Resource Manager Notification Map should not contain the resource ID after second removal");
    }

}

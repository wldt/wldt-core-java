package it.wldt.augmentation.stateful.generic;

import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.handler.DefaultAugmentationFunctionHandler;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.augmentation.stateful.function.StatefulPeriodicRandomNumberAugmentationFunction;
import it.wldt.augmentation.stateful.function.StatefulStateDrivenRandomNumberAugmentationFunction;
import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import org.junit.jupiter.api.*;

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
public class StatefulGenericResultAugmentationProcessTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulGenericResultAugmentationProcessTest.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";
    private static final String TEST_AUGMENTATION_HANDLER_ID = "test-augmentation-handler";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    @BeforeEach
    public void setUp() throws KernelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, AugmentationFunctionException {

        logger.info("Setting up Test Environment ...");

        // Register DT to Shared Test Metrics
        SharedTestMetrics.getInstance().registerDigitalTwin(TEST_DIGITAL_TWIN_ID);

        // Create Digital Twin Engine
        digitalTwinEngine = new DigitalTwinEngine();

        // Create new Digital Twin with a specific Digital Twin Model
        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new StatefulGenericResultAugmentationDigitalTwinModel());

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

        // Create an instance of the Augmentation Manager to test the augmentation functions
        AugmentationFunctionHandler myAugmentationFunctionHandler = new DefaultAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID);

        // Set the Augmentation Manager to the specific Digital Twin to test the augmentation functions
        digitalTwin.getAugmentationManager().addAugmentationFunctionHandler(myAugmentationFunctionHandler);

        // Register the augmentation function to the augmentation manager
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).isPresent()) {

            // Register Stateful Periodic Augmentation Function
//            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
//                    .get()
//                    .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

            // Register Stateful State Driven Augmentation Function that reacts to new Digital Twin State Variations
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
                    .get()
                    .registerAugmentationFunction(new StatefulStateDrivenRandomNumberAugmentationFunction());
        }

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
    public void testStatefulAugmentationFunction() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {


        /*
        /////////////// TEST CONTEXT ///////////////
        // The Stateful augmentation function is triggered when it is registered and when the list of available functions
        // is returned to the Digital Twin Model.
        ///////////////////////////////////////////
        */

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve from the Shared Metrics the received Augmentation Function Result Events
        List<List<AugmentationFunctionResult<?>>> augmentationFunctionResultList = SharedTestMetrics.getInstance().getAugmentationFunctionResultNotification(
                TEST_DIGITAL_TWIN_ID,
                TEST_AUGMENTATION_HANDLER_ID,
                StatefulStateDrivenRandomNumberAugmentationFunction.FUNCTION_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augmentationFunctionResultList);

        //Check the number of received Augmentation Function Result Events is equal to the number of Physical Asset Property Update Events
        //since the Stateless Augmentation Function is executed every time a new Physical Asset Property Update Event is received by the Digital Twin Model
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, augmentationFunctionResultList.size());

        // For each received Augmentation Function Result Event
        for (List<AugmentationFunctionResult<?>> resultList : augmentationFunctionResultList) {

            // For each received Augmentation Function Result inside the Augmentation Function Result Event
            for (AugmentationFunctionResult<?> augmentationFunctionResult : resultList) {

                // Check the Augmentation Function Result Type
                Assertions.assertEquals(AugmentationFunctionResultType.GENERIC_RESULT, augmentationFunctionResult.getType());

                // Check that the Result value is not null
                assertNotNull(augmentationFunctionResult.getValue());
            }

        }

    }

    @Test
    @Order(2)
    public void testAugmentationFunctionInitialRegistration() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve from the Shared Metrics the received Augmentation Function Registration Events
        Map<String, List<AugmentationFunction>> augFuntionRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionRegistrationMap);

        // Check that the Map contains the expected Augmentation Handler ID
        assert (augFuntionRegistrationMap.containsKey(TEST_AUGMENTATION_HANDLER_ID));

        // Check that 1 Augmentation Function Registration Event has been received for the expected Augmentation Handler ID
        assertEquals(1, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the registered Augmentation Function is correct
        assertEquals(StatefulStateDrivenRandomNumberAugmentationFunction.class, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(0).getClass());
    }

    @Test
    @Order(3)
    public void testDynamicAugmentationFunctionRegistration() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException, AugmentationFunctionException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve from the Shared Metrics the received Augmentation Function Registration Events
        Map<String, List<AugmentationFunction>> augFuntionRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionRegistrationMap);

        // Check that the Map contains the expected Augmentation Handler ID
        assert (augFuntionRegistrationMap.containsKey(TEST_AUGMENTATION_HANDLER_ID));

        // Check that 1 Augmentation Function Registration Event has been received for the expected Augmentation Handler ID
        assertEquals(1, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the registered Augmentation Function is correct
        assertEquals(StatefulStateDrivenRandomNumberAugmentationFunction.class, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(0).getClass());

        // Add a new Augmentation Function to the Augmentation Manager of the Digital Twin
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).isPresent())
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
                    .get()
                    .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

        // Wait until the Augmentation Function Registration Event is received
        Thread.sleep(5000);

        // Update the Shared Metrics with the received Augmentation Function Registration Events
        augFuntionRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionRegistrationMap);

        // Check that 2 Augmentation Function Registration Events have been received for the expected Augmentation Handler ID
        assertEquals(2, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the registered Augmentation Function is correct
        assertEquals(StatefulPeriodicRandomNumberAugmentationFunction.class, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(1).getClass());
    }

    @Test
    @Order(4)
    public void testDynamicAugmentationFunctionUnRegistration() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException, AugmentationFunctionException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve from the Shared Metrics the received Augmentation Function Registration Events
        Map<String, List<AugmentationFunction>> augFuntionRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionRegistrationMap);

        // Check that the Map contains the expected Augmentation Handler ID
        assert (augFuntionRegistrationMap.containsKey(TEST_AUGMENTATION_HANDLER_ID));

        // Check that 1 Augmentation Function Registration Event has been received for the expected Augmentation Handler ID
        assertEquals(1, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the registered Augmentation Function is correct
        assertEquals(StatefulStateDrivenRandomNumberAugmentationFunction.class, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(0).getClass());

        // Add a new Augmentation Function to the Augmentation Manager of the Digital Twin
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).isPresent())
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
                    .get()
                    .registerAugmentationFunction(new StatefulPeriodicRandomNumberAugmentationFunction());

        // Wait until the Augmentation Function Registration Event is received
        Thread.sleep(5000);

        // Update the Shared Metrics with the received Augmentation Function Registration Events
        augFuntionRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionRegistrationMap);

        // Check that 2 Augmentation Function Registration Events have been received for the expected Augmentation Handler ID
        assertEquals(2, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the registered Augmentation Function is correct
        assertEquals(StatefulPeriodicRandomNumberAugmentationFunction.class, augFuntionRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(1).getClass());

        // Remove the first registered Augmentation Function from the Augmentation Manager of the Digital Twin
        if (digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).isPresent())
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
                    .get()
                    .unRegisterAugmentationFunction(StatefulPeriodicRandomNumberAugmentationFunction.FUNCTION_ID);

        // Wait until the Augmentation Function UnRegistration Event is received
        Thread.sleep(5000);

        // Retrieve the list of received Augmentation Function UnRegistration Events from the Shared Metrics
        Map<String, List<AugmentationFunction>> augFuntionUnRegistrationMap = SharedTestMetrics.getInstance().
                getAugmentationFunctionUnRegistrationCallbackMap().
                get(TEST_DIGITAL_TWIN_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augFuntionUnRegistrationMap);

        // Check that only 1 Augmentation Function Registration Event is present for the expected Augmentation Handler ID
        assertEquals(1, augFuntionUnRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).size());

        // Check that the type of the un-registered Augmentation Function is correct
        assertEquals(StatefulPeriodicRandomNumberAugmentationFunction.class, augFuntionUnRegistrationMap.get(TEST_AUGMENTATION_HANDLER_ID).get(0).getClass());
    }
}

package it.wldt.augmentation.stateless.state;

import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.augmentation.handler.DefaultAugmentationFunctionHandler;
import it.wldt.augmentation.stateless.function.RandomStateResultAugmentationFunction;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StateResultAugmentationProcessTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StateResultAugmentationProcessTest.class);

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
        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new StateResultAugmentationDigitalTwinModel());

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
        if(digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).isPresent())
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID)
                    .get()
                    .registerAugmentationFunction(new RandomStateResultAugmentationFunction());

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
    public void testStatelessAugmentationFunction() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {

        /*
        /////////////// TEST CONTEXT ///////////////
        The test Stateless Augmentation Function is executed every time a new Physical Asset Property Update Event
        is received by the Digital Twin Model.
        ///////////////////////////////////////////
        */

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Wait until all the messages have been received
        Thread.sleep((DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS + ((DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS)));

        // Retrieve from the Shared Metrics the received Augmentation Function Result Events
        List<AugmentationFunctionResultList> augmentationFunctionResultLists = SharedTestMetrics.getInstance().getAugmentationFunctionResultNotification(
                TEST_DIGITAL_TWIN_ID,
                TEST_AUGMENTATION_HANDLER_ID,
                RandomStateResultAugmentationFunction.FUNCTION_ID);

        //Check Received Augmentation Function Result is Not Null
        assertNotNull(augmentationFunctionResultLists);

        //Check the number of received Augmentation Function Result Events is equal to the number of Physical Asset Property Update Events
        //since the Stateless Augmentation Function is executed every time a new Physical Asset Property Update Event is received by the Digital Twin Model
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, augmentationFunctionResultLists.size());

        // For each received Augmentation Function Result Event
        for(AugmentationFunctionResultList resultList : augmentationFunctionResultLists){

            // Get Position 0 for Property Result, Position 1 for Event Result, Position 2 for Relationship Result, and Position 3 for Relationship Instance Result
            AugmentationFunctionResult<?> propertyResult = resultList.get(0);
            AugmentationFunctionResult<?> eventResult = resultList.get(1);
            AugmentationFunctionResult<?> relationshipResult = resultList.get(2);
            AugmentationFunctionResult<?> relationshipInstanceResult = resultList.get(3);

            // Assert results are not null
            assertNotNull(propertyResult);
            assertNotNull(eventResult);
            assertNotNull(relationshipResult);
            assertNotNull(relationshipInstanceResult);

            // Assert results are of the expected type
            assertEquals(AugmentationFunctionResultType.PROPERTY_RESULT, propertyResult.getType());
            assertEquals(AugmentationFunctionResultType.EVENT_RESULT, eventResult.getType());
            assertEquals(AugmentationFunctionResultType.RELATIONSHIP_RESULT, relationshipResult.getType());
            assertEquals(AugmentationFunctionResultType.RELATIONSHIP_INSTANCE_RESULT, relationshipInstanceResult.getType());
        }
    }

}

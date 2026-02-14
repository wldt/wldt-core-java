package it.wldt.augmentation;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AugmentationProcessTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationProcessTest.class);

    private static final String TEST_DIGITAL_TWIN_ID = "dtTest0001";
    private static final String TEST_AUGMENTATION_HANDLER_ID = "test-augmentation-handler";

    private DigitalTwin digitalTwin = null;

    private DigitalTwinEngine digitalTwinEngine = null;

    @BeforeEach
    public void setUp() throws KernelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException, AugmentationFunctionException {

        logger.info("Setting up Test Environment ...");

        digitalTwinEngine = new DigitalTwinEngine();

        digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new AugDigitalTwinModel());

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
            digitalTwin.getAugmentationManager().getAugmentationFunctionHandler(TEST_AUGMENTATION_HANDLER_ID).get().registerAugmentationFunction(new RandomNumberAugmentationFunction());

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
    public void testStatelessAugmentationFunction() throws WldtConfigurationException, EventBusException, KernelException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        // Wait for the test execution
        Thread.sleep(10000);
    }

}

package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MonitoringConfiguration} and its nested {@link MonitoringConfiguration.Builder}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Default state — all flags false, default namespace</li>
 *   <li>Individual flag enablement via builder methods</li>
 *   <li>{@code withAllMonitoring()} enables all flags simultaneously</li>
 *   <li>Selective combination of flags</li>
 *   <li>{@code isAnyMonitoringEnabled()} logic</li>
 *   <li>Custom namespace configuration</li>
 *   <li>Guard validation on blank/null namespace</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MonitoringConfigurationTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(MonitoringConfigurationTest.class);

    @BeforeEach
    public void setUp() {
        logger.info("Setting up MonitoringConfigurationTest ...");
    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up MonitoringConfigurationTest ...");
    }

    // -------------------------------------------------------------------------
    // Default state
    // -------------------------------------------------------------------------

    /**
     * A freshly built configuration with no builder methods called
     * must have all flags false and the default custom namespace.
     */
    @Test
    @Order(1)
    public void testDefaultStateAllFlagsFalse() {
        logger.info("Testing default MonitoringConfiguration state — all flags false ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder().build();

        assertFalse(config.isDtModelMonitoringEnabled());
        assertFalse(config.isEventBusMonitoringEnabled());
        assertFalse(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isAugmentationMonitoringEnabled());
        assertFalse(config.isStorageMonitoringEnabled());
        assertFalse(config.isAnyMonitoringEnabled());
        assertEquals(MonitoringConfiguration.DEFAULT_CUSTOM_NAMESPACE,
                config.getCustomMetricNamespace());
    }

    // -------------------------------------------------------------------------
    // Individual flag enablement
    // -------------------------------------------------------------------------

    /**
     * withDtModelMonitoring() enables only the DT Model flag.
     */
    @Test
    @Order(2)
    public void testWithDtModelMonitoringEnablesOnlyDtModel() {
        logger.info("Testing withDtModelMonitoring() enables only DT Model flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withDtModelMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
        assertFalse(config.isEventBusMonitoringEnabled());
        assertFalse(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isAugmentationMonitoringEnabled());
        assertFalse(config.isStorageMonitoringEnabled());
        assertTrue(config.isAnyMonitoringEnabled());
    }

    /**
     * withEventBusMonitoring() enables only the Event Bus flag.
     */
    @Test
    @Order(3)
    public void testWithEventBusMonitoringEnablesOnlyEventBus() {
        logger.info("Testing withEventBusMonitoring() enables only Event Bus flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withEventBusMonitoring()
                .build();

        assertTrue(config.isEventBusMonitoringEnabled());
        assertFalse(config.isDtModelMonitoringEnabled());
        assertTrue(config.isAnyMonitoringEnabled());
    }

    /**
     * withPhysicalAdapterMonitoring() enables only the Physical Adapter flag.
     */
    @Test
    @Order(4)
    public void testWithPhysicalAdapterMonitoring() {
        logger.info("Testing withPhysicalAdapterMonitoring() enables only Physical Adapter flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withPhysicalAdapterMonitoring()
                .build();

        assertTrue(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDtModelMonitoringEnabled());
    }

    /**
     * withDigitalAdapterMonitoring() enables only the Digital Adapter flag.
     */
    @Test
    @Order(5)
    public void testWithDigitalAdapterMonitoring() {
        logger.info("Testing withDigitalAdapterMonitoring() enables only Digital Adapter flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withDigitalAdapterMonitoring()
                .build();

        assertTrue(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isDtModelMonitoringEnabled());
    }

    /**
     * withAugmentationMonitoring() enables only the Augmentation flag.
     */
    @Test
    @Order(6)
    public void testWithAugmentationMonitoring() {
        logger.info("Testing withAugmentationMonitoring() enables only Augmentation flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withAugmentationMonitoring()
                .build();

        assertTrue(config.isAugmentationMonitoringEnabled());
        assertFalse(config.isDtModelMonitoringEnabled());
    }

    /**
     * withStorageMonitoring() enables only the Storage flag.
     */
    @Test
    @Order(7)
    public void testWithStorageMonitoring() {
        logger.info("Testing withStorageMonitoring() enables only Storage flag ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withStorageMonitoring()
                .build();

        assertTrue(config.isStorageMonitoringEnabled());
        assertFalse(config.isDtModelMonitoringEnabled());
    }

    // -------------------------------------------------------------------------
    // withAllMonitoring()
    // -------------------------------------------------------------------------

    /**
     * withAllMonitoring() enables all six component flags simultaneously.
     */
    @Test
    @Order(8)
    public void testWithAllMonitoringEnablesAllFlags() {
        logger.info("Testing withAllMonitoring() enables all six component flags ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withAllMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
        assertTrue(config.isEventBusMonitoringEnabled());
        assertTrue(config.isPhysicalAdapterMonitoringEnabled());
        assertTrue(config.isDigitalAdapterMonitoringEnabled());
        assertTrue(config.isAugmentationMonitoringEnabled());
        assertTrue(config.isStorageMonitoringEnabled());
        assertTrue(config.isAnyMonitoringEnabled());
    }

    // -------------------------------------------------------------------------
    // Selective combination
    // -------------------------------------------------------------------------

    /**
     * Selective combination — only specified flags enabled, others remain false.
     */
    @Test
    @Order(9)
    public void testSelectiveFlagCombination() {
        logger.info("Testing selective flag combination (DT Model + Storage) ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withDtModelMonitoring()
                .withStorageMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
        assertTrue(config.isStorageMonitoringEnabled());
        assertFalse(config.isEventBusMonitoringEnabled());
        assertFalse(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isAugmentationMonitoringEnabled());
        assertTrue(config.isAnyMonitoringEnabled());
    }

    // -------------------------------------------------------------------------
    // Custom namespace
    // -------------------------------------------------------------------------

    /**
     * withCustomNamespace() sets a custom namespace prefix correctly.
     */
    @Test
    @Order(10)
    public void testCustomNamespaceIsSet() {
        logger.info("Testing withCustomNamespace() sets the namespace correctly ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withCustomNamespace("myapp.sensor")
                .build();

        assertEquals("myapp.sensor", config.getCustomMetricNamespace());
    }

    /**
     * Null namespace must throw IllegalArgumentException.
     */
    @Test
    @Order(11)
    public void testNullNamespaceThrows() {
        logger.info("Testing withCustomNamespace() rejects null ...");
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringConfiguration.Builder().withCustomNamespace(null));
    }

    /**
     * Blank namespace must throw IllegalArgumentException.
     */
    @Test
    @Order(12)
    public void testBlankNamespaceThrows() {
        logger.info("Testing withCustomNamespace() rejects blank string ...");
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringConfiguration.Builder().withCustomNamespace("   "));
    }
}
package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MonitoringInterfaceConfiguration} and its nested {@link MonitoringInterfaceConfiguration.Builder}.
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
public class MonitoringInterfaceConfigurationTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(MonitoringInterfaceConfigurationTest.class);

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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder().build();

        assertFalse(config.isDtModelMonitoringEnabled());
        assertFalse(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isAugmentationMonitoringEnabled());
        assertFalse(config.isStorageMonitoringEnabled());
        assertFalse(config.isAnyMonitoringEnabled());
        assertEquals(MonitoringInterfaceConfiguration.DEFAULT_CUSTOM_NAMESPACE,
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
                .withDtModelMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
        assertFalse(config.isPhysicalAdapterMonitoringEnabled());
        assertFalse(config.isDigitalAdapterMonitoringEnabled());
        assertFalse(config.isAugmentationMonitoringEnabled());
        assertFalse(config.isStorageMonitoringEnabled());
        assertTrue(config.isAnyMonitoringEnabled());
    }

    /**
     * withPhysicalAdapterMonitoring() enables only the Physical Adapter flag.
     */
    @Test
    @Order(4)
    public void testWithPhysicalAdapterMonitoring() {
        logger.info("Testing withPhysicalAdapterMonitoring() enables only Physical Adapter flag ...");
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
                .withAllMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
                .withDtModelMonitoring()
                .withStorageMonitoring()
                .build();

        assertTrue(config.isDtModelMonitoringEnabled());
        assertTrue(config.isStorageMonitoringEnabled());
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
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
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
                () -> new MonitoringInterfaceConfiguration.Builder().withCustomNamespace(null));
    }

    /**
     * Blank namespace must throw IllegalArgumentException.
     */
    @Test
    @Order(12)
    public void testBlankNamespaceThrows() {
        logger.info("Testing withCustomNamespace() rejects blank string ...");
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringInterfaceConfiguration.Builder().withCustomNamespace("   "));
    }
}
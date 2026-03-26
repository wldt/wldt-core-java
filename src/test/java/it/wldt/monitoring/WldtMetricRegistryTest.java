package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.metrics.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WldtMetricRegistry}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Lazy registration on first push</li>
 *   <li>Explicit register() and deregister()</li>
 *   <li>Delta computation for WldtCounter across multiple pushes</li>
 *   <li>Delta computation for WldtUpDownCounter (positive and negative)</li>
 *   <li>No delta for WldtGauge, WldtTimer, WldtHistogram</li>
 *   <li>getMetric() query by full name</li>
 *   <li>getAllMetrics() snapshot</li>
 *   <li>isRegistered() check</li>
 *   <li>deregister() resets delta tracking</li>
 *   <li>clear() empties the registry</li>
 *   <li>Guard validation (null inputs)</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WldtMetricRegistryTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(WldtMetricRegistryTest.class);

    private static final String NS   = "wldt.internal";
    private static final String NAME = "dt_model.events_processed";

    private WldtMetricRegistry registry;

    @BeforeEach
    public void setUp() {
        logger.info("Setting up WldtMetricRegistryTest ...");
        registry = new WldtMetricRegistry();
    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up WldtMetricRegistryTest ...");
        registry = null;
    }

    // -------------------------------------------------------------------------
    // Lazy registration
    // -------------------------------------------------------------------------

    /**
     * On the first push the metric is auto-registered and returned
     * without a delta (delta=null).
     */
    @Test
    @Order(1)
    public void testLazyRegistrationOnFirstPush() {
        logger.info("Testing lazy registration on first push ...");
        WldtCounter incoming = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 5L);

        WldtMetric result = registry.computeAndRegister(incoming);

        assertTrue(result instanceof WldtCounter);
        WldtCounter counter = (WldtCounter) result;
        assertEquals(5L, counter.getValue());
        assertNull(counter.getDelta(), "Delta must be null on first push");
        assertFalse(counter.isDeltaAvailable());
        assertTrue(registry.isRegistered(incoming.getFullName()));
    }

    // -------------------------------------------------------------------------
    // WldtCounter delta computation
    // -------------------------------------------------------------------------

    /**
     * Second push computes correct non-negative delta for WldtCounter.
     */
    @Test
    @Order(2)
    public void testCounterDeltaOnSecondPush() {
        logger.info("Testing WldtCounter delta computed on second push ...");
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 5L));

        WldtCounter result = (WldtCounter) registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 8L));

        assertEquals(8L, result.getValue());
        assertEquals(Long.valueOf(3L), result.getDelta());
        assertTrue(result.isDeltaAvailable());
    }

    /**
     * Multiple sequential pushes accumulate delta correctly at each step.
     */
    @Test
    @Order(3)
    public void testCounterDeltaAcrossMultiplePushes() {
        logger.info("Testing WldtCounter delta across multiple sequential pushes ...");
        long[] values          = {0L, 5L, 12L, 20L, 20L};
        Long[] expectedDeltas  = {null, 5L, 7L, 8L, 0L};

        for (int i = 0; i < values.length; i++) {
            WldtCounter result = (WldtCounter) registry.computeAndRegister(
                    new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, values[i]));
            assertEquals(values[i], result.getValue(), "Value mismatch at step " + i);
            assertEquals(expectedDeltas[i], result.getDelta(), "Delta mismatch at step " + i);
        }
    }

    /**
     * Delta is clamped to zero when new value equals previous value.
     */
    @Test
    @Order(4)
    public void testCounterDeltaZeroWhenValueUnchanged() {
        logger.info("Testing WldtCounter delta is zero when value unchanged ...");
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L));
        WldtCounter result = (WldtCounter) registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L));

        assertEquals(Long.valueOf(0L), result.getDelta());
    }

    // -------------------------------------------------------------------------
    // WldtUpDownCounter delta computation
    // -------------------------------------------------------------------------

    /**
     * WldtUpDownCounter computes positive delta when count increases.
     */
    @Test
    @Order(5)
    public void testUpDownCounterPositiveDelta() {
        logger.info("Testing WldtUpDownCounter positive delta computation ...");
        registry.computeAndRegister(
                new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 2L));
        WldtUpDownCounter result = (WldtUpDownCounter) registry.computeAndRegister(
                new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 5L));

        assertEquals(Long.valueOf(3L), result.getDelta());
    }

    /**
     * WldtUpDownCounter computes negative delta when count decreases.
     */
    @Test
    @Order(6)
    public void testUpDownCounterNegativeDelta() {
        logger.info("Testing WldtUpDownCounter negative delta computation ...");
        registry.computeAndRegister(
                new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 5L));
        WldtUpDownCounter result = (WldtUpDownCounter) registry.computeAndRegister(
                new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 3L));

        assertEquals(Long.valueOf(-2L), result.getDelta());
    }

    /**
     * First push of WldtUpDownCounter has null delta.
     */
    @Test
    @Order(7)
    public void testUpDownCounterNullDeltaOnFirstPush() {
        logger.info("Testing WldtUpDownCounter delta is null on first push ...");
        WldtUpDownCounter result = (WldtUpDownCounter) registry.computeAndRegister(
                new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 3L));

        assertNull(result.getDelta());
    }

    // -------------------------------------------------------------------------
    // No delta for Gauge, Timer, Histogram
    // -------------------------------------------------------------------------

    /**
     * WldtGauge passes through unchanged — no delta enrichment.
     */
    @Test
    @Order(8)
    public void testGaugePassesThroughUnchanged() {
        logger.info("Testing WldtGauge passes through without delta enrichment ...");
        registry.computeAndRegister(
                new WldtGauge(NS, "event_bus.queue_depth", WldtMetricComponent.EVENT_BUS, 10.0));
        WldtGauge result = (WldtGauge) registry.computeAndRegister(
                new WldtGauge(NS, "event_bus.queue_depth", WldtMetricComponent.EVENT_BUS, 15.0));

        assertEquals(15.0, result.getValue(), 0.0001);
    }

    /**
     * WldtTimer passes through unchanged on repeated pushes.
     */
    @Test
    @Order(9)
    public void testTimerPassesThroughUnchanged() {
        logger.info("Testing WldtTimer passes through without delta enrichment ...");
        registry.computeAndRegister(
                new WldtTimer(NS, "dt_model.latency_ms", WldtMetricComponent.DT_MODEL, 100L));
        WldtTimer result = (WldtTimer) registry.computeAndRegister(
                new WldtTimer(NS, "dt_model.latency_ms", WldtMetricComponent.DT_MODEL, 200L));

        assertEquals(200L, result.getDurationMs());
    }

    /**
     * WldtHistogram passes through unchanged on repeated pushes.
     */
    @Test
    @Order(10)
    public void testHistogramPassesThroughUnchanged() {
        logger.info("Testing WldtHistogram passes through without delta enrichment ...");
        registry.computeAndRegister(new WldtHistogram(NS, "pa.msg_size",
                WldtMetricComponent.PHYSICAL_ADAPTER, 4L, 400.0, 80.0, 120.0));
        WldtHistogram result = (WldtHistogram) registry.computeAndRegister(
                new WldtHistogram(NS, "pa.msg_size",
                        WldtMetricComponent.PHYSICAL_ADAPTER, 8L, 900.0, 70.0, 140.0));

        assertEquals(8L, result.getCount());
    }

    // -------------------------------------------------------------------------
    // Explicit register() and deregister()
    // -------------------------------------------------------------------------

    /**
     * Explicit register() pre-populates the registry.
     * Next push computes delta against pre-registered value.
     */
    @Test
    @Order(11)
    public void testExplicitRegisterAffectsDeltaOnNextPush() {
        logger.info("Testing explicit register() influences delta on next push ...");
        registry.register(new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L));

        WldtCounter result = (WldtCounter) registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 14L));

        assertEquals(Long.valueOf(4L), result.getDelta());
    }

    /**
     * After deregister(), the next push is treated as a first push (delta=null).
     */
    @Test
    @Order(12)
    public void testDeregisterResetsTracking() {
        logger.info("Testing deregister() resets delta tracking ...");
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L));
        registry.deregister(NS + "." + NAME);

        WldtCounter result = (WldtCounter) registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 15L));

        assertNull(result.getDelta(), "After deregister delta must be null on next push");
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    /**
     * getMetric() returns the last registered raw value for a known full name.
     */
    @Test
    @Order(13)
    public void testGetMetricReturnsLastValue() {
        logger.info("Testing getMetric() returns last pushed value ...");
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 5L));
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 9L));

        assertTrue(registry.getMetric(NS + "." + NAME).isPresent());
        WldtCounter stored = (WldtCounter) registry.getMetric(NS + "." + NAME).get();
        assertEquals(9L, stored.getValue());
    }

    /**
     * getMetric() returns empty Optional for unknown full name.
     */
    @Test
    @Order(14)
    public void testGetMetricReturnsEmptyForUnknown() {
        logger.info("Testing getMetric() returns empty for unknown metric ...");
        assertFalse(registry.getMetric("wldt.internal.unknown.metric").isPresent());
    }

    /**
     * getAllMetrics() returns all currently registered entries.
     */
    @Test
    @Order(15)
    public void testGetAllMetricsSize() {
        logger.info("Testing getAllMetrics() returns correct entry count ...");
        registry.computeAndRegister(
                new WldtCounter(NS, "metric.one", WldtMetricComponent.DT_MODEL, 1L));
        registry.computeAndRegister(
                new WldtGauge(NS, "metric.two", WldtMetricComponent.EVENT_BUS, 2.0));
        registry.computeAndRegister(
                new WldtTimer(NS, "metric.three", WldtMetricComponent.STORAGE, 300L));

        assertEquals(3, registry.getAllMetrics().size());
    }

    /**
     * isRegistered() returns true after first push and false before.
     */
    @Test
    @Order(16)
    public void testIsRegistered() {
        logger.info("Testing isRegistered() lifecycle ...");
        assertFalse(registry.isRegistered(NS + "." + NAME));
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 1L));
        assertTrue(registry.isRegistered(NS + "." + NAME));
    }

    /**
     * clear() empties the registry completely.
     */
    @Test
    @Order(17)
    public void testClearEmptiesRegistry() {
        logger.info("Testing clear() empties the registry ...");
        registry.computeAndRegister(
                new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 5L));
        registry.computeAndRegister(
                new WldtGauge(NS, "gauge.metric", WldtMetricComponent.EVENT_BUS, 1.0));

        assertEquals(2, registry.size());
        registry.clear();
        assertEquals(0, registry.size());
    }

    // -------------------------------------------------------------------------
    // Guard validation
    // -------------------------------------------------------------------------

    /**
     * computeAndRegister() with null metric must throw IllegalArgumentException.
     */
    @Test
    @Order(18)
    public void testComputeAndRegisterNullThrows() {
        logger.info("Testing computeAndRegister() rejects null ...");
        assertThrows(IllegalArgumentException.class,
                () -> registry.computeAndRegister(null));
    }

    /**
     * register() with null metric must throw IllegalArgumentException.
     */
    @Test
    @Order(19)
    public void testRegisterNullThrows() {
        logger.info("Testing register() rejects null ...");
        assertThrows(IllegalArgumentException.class,
                () -> registry.register(null));
    }

    /**
     * deregister() with null fullName must throw IllegalArgumentException.
     */
    @Test
    @Order(20)
    public void testDeregisterNullThrows() {
        logger.info("Testing deregister() rejects null fullName ...");
        assertThrows(IllegalArgumentException.class,
                () -> registry.deregister(null));
    }

    /**
     * getMetric() with blank fullName must throw IllegalArgumentException.
     */
    @Test
    @Order(21)
    public void testGetMetricBlankThrows() {
        logger.info("Testing getMetric() rejects blank fullName ...");
        assertThrows(IllegalArgumentException.class,
                () -> registry.getMetric("   "));
    }
}
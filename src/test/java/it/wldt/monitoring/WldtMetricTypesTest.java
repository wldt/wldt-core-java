package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.metrics.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all WldtMetric subtypes.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Valid construction and field accessors for each type</li>
 *   <li>Validation guards (null namespace, null name, negative values, NaN, Infinite)</li>
 *   <li>{@code WldtMetric.getFullName()} composition</li>
 *   <li>{@code WldtTimer.since()} factory method</li>
 *   <li>{@code WldtTimer.getDurationSeconds()} conversion</li>
 *   <li>{@code WldtHistogram.getMean()} computation</li>
 *   <li>{@code WldtCounter.withDelta()} immutable copy</li>
 *   <li>{@code WldtUpDownCounter.withDelta()} immutable copy</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WldtMetricTypesTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(WldtMetricTypesTest.class);

    private static final String NS   = "wldt.internal";
    private static final String NAME = "dt_model.events_processed";

    @BeforeEach
    public void setUp() {
        logger.info("Setting up WldtMetricTypesTest ...");
    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up WldtMetricTypesTest ...");
    }

    // -------------------------------------------------------------------------
    // WldtMetric base
    // -------------------------------------------------------------------------

    /**
     * Verifies that getFullName() returns namespace.name.
     */
    @Test
    @Order(1)
    public void testGetFullName() {
        logger.info("Testing getFullName() ...");
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        assertEquals("wldt.internal.dt_model.events_processed", c.getFullName());
    }

    /**
     * Verifies that the timestamp recorded at construction is within the
     * expected range around the current time.
     */
    @Test
    @Order(2)
    public void testTimestampIsSet() {
        logger.info("Testing timestampMs is set at construction time ...");
        long before = System.currentTimeMillis();
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 1L);
        long after  = System.currentTimeMillis();
        assertTrue(c.getTimestampMs() >= before);
        assertTrue(c.getTimestampMs() <= after);
    }

    // -------------------------------------------------------------------------
    // WldtCounter
    // -------------------------------------------------------------------------

    /**
     * Valid WldtCounter — checks value and absence of delta on first construction.
     */
    @Test
    @Order(3)
    public void testCounterValidConstruction() {
        logger.info("Testing WldtCounter valid construction ...");
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 42L);
        assertEquals(42L, c.getValue());
        assertNull(c.getDelta(), "Delta should be null when not set");
        assertFalse(c.isDeltaAvailable());
    }

    /**
     * Valid WldtCounter with explicit delta — both value and delta accessible.
     */
    @Test
    @Order(4)
    public void testCounterWithExplicitDelta() {
        logger.info("Testing WldtCounter with explicit delta in constructor ...");
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 42L, 5L);
        assertEquals(42L, c.getValue());
        assertEquals(Long.valueOf(5L), c.getDelta());
        assertTrue(c.isDeltaAvailable());
    }

    /**
     * withDelta() returns a new immutable instance with delta set; original unchanged.
     */
    @Test
    @Order(5)
    public void testCounterWithDeltaImmutableCopy() {
        logger.info("Testing WldtCounter.withDelta() produces immutable copy ...");
        WldtCounter original = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 42L);
        WldtCounter enriched = original.withDelta(7L);

        assertNull(original.getDelta(), "Original delta must remain null");
        assertEquals(Long.valueOf(7L), enriched.getDelta());
        assertEquals(42L, enriched.getValue());
        assertEquals(original.getNamespace(), enriched.getNamespace());
        assertEquals(original.getName(), enriched.getName());
        assertEquals(original.getComponent(), enriched.getComponent());
    }

    /**
     * Negative value must throw IllegalArgumentException.
     */
    @Test
    @Order(6)
    public void testCounterNegativeValueThrows() {
        logger.info("Testing WldtCounter rejects negative value ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, -1L));
    }

    /**
     * Negative delta must throw IllegalArgumentException.
     */
    @Test
    @Order(7)
    public void testCounterNegativeDeltaThrows() {
        logger.info("Testing WldtCounter rejects negative delta ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L, -3L));
    }

    /**
     * Null namespace must throw IllegalArgumentException.
     */
    @Test
    @Order(8)
    public void testCounterNullNamespaceThrows() {
        logger.info("Testing WldtCounter rejects null namespace ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(null, NAME, WldtMetricComponent.DT_MODEL, 1L));
    }

    /**
     * Blank name must throw IllegalArgumentException.
     */
    @Test
    @Order(9)
    public void testCounterBlankNameThrows() {
        logger.info("Testing WldtCounter rejects blank name ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, "   ", WldtMetricComponent.DT_MODEL, 1L));
    }

    /**
     * Null component must throw IllegalArgumentException.
     */
    @Test
    @Order(10)
    public void testCounterNullComponentThrows() {
        logger.info("Testing WldtCounter rejects null component ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, NAME, null, 1L));
    }

    // -------------------------------------------------------------------------
    // WldtUpDownCounter
    // -------------------------------------------------------------------------

    /**
     * Valid WldtUpDownCounter with positive value and no delta on first construction.
     */
    @Test
    @Order(11)
    public void testUpDownCounterPositiveValue() {
        logger.info("Testing WldtUpDownCounter valid construction with positive value ...");
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "physical_adapter.connected_count",
                WldtMetricComponent.PHYSICAL_ADAPTER, 3L);
        assertEquals(3L, c.getValue());
        assertNull(c.getDelta());
        assertFalse(c.isDeltaAvailable());
    }

    /**
     * WldtUpDownCounter accepts negative values (count decreased below zero).
     */
    @Test
    @Order(12)
    public void testUpDownCounterNegativeValueAllowed() {
        logger.info("Testing WldtUpDownCounter accepts negative value ...");
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "test.metric",
                WldtMetricComponent.CUSTOM, -2L);
        assertEquals(-2L, c.getValue());
    }

    /**
     * withDelta() on WldtUpDownCounter returns enriched copy with signed delta.
     */
    @Test
    @Order(13)
    public void testUpDownCounterWithDeltaImmutableCopy() {
        logger.info("Testing WldtUpDownCounter.withDelta() produces immutable copy with signed delta ...");
        WldtUpDownCounter original = new WldtUpDownCounter(NS, "test.metric",
                WldtMetricComponent.CUSTOM, 5L);
        WldtUpDownCounter enriched = original.withDelta(-2L);

        assertNull(original.getDelta(), "Original delta must remain null");
        assertEquals(Long.valueOf(-2L), enriched.getDelta());
        assertEquals(5L, enriched.getValue());
        assertTrue(enriched.isDeltaAvailable());
    }

    // -------------------------------------------------------------------------
    // WldtGauge
    // -------------------------------------------------------------------------

    /**
     * Valid WldtGauge construction with positive double value.
     */
    @Test
    @Order(14)
    public void testGaugeValidConstruction() {
        logger.info("Testing WldtGauge valid construction ...");
        WldtGauge g = new WldtGauge(NS, "event_bus.queue_depth",
                WldtMetricComponent.EVENT_BUS, 12.5);
        assertEquals(12.5, g.getValue(), 0.0001);
    }

    /**
     * WldtGauge accepts zero and negative values.
     */
    @Test
    @Order(15)
    public void testGaugeZeroAndNegativeAllowed() {
        logger.info("Testing WldtGauge accepts zero and negative values ...");
        WldtGauge zero = new WldtGauge(NS, "test.metric", WldtMetricComponent.CUSTOM, 0.0);
        WldtGauge neg  = new WldtGauge(NS, "test.metric", WldtMetricComponent.CUSTOM, -5.5);
        assertEquals(0.0,  zero.getValue(), 0.0001);
        assertEquals(-5.5, neg.getValue(),  0.0001);
    }

    /**
     * NaN value must throw IllegalArgumentException.
     */
    @Test
    @Order(16)
    public void testGaugeNaNThrows() {
        logger.info("Testing WldtGauge rejects NaN value ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtGauge(NS, "test.metric", WldtMetricComponent.CUSTOM, Double.NaN));
    }

    /**
     * Infinite value must throw IllegalArgumentException.
     */
    @Test
    @Order(17)
    public void testGaugeInfiniteThrows() {
        logger.info("Testing WldtGauge rejects Infinite value ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtGauge(NS, "test.metric", WldtMetricComponent.CUSTOM,
                        Double.POSITIVE_INFINITY));
    }

    // -------------------------------------------------------------------------
    // WldtTimer
    // -------------------------------------------------------------------------

    /**
     * Valid WldtTimer construction — durationMs and getDurationSeconds() accessible.
     */
    @Test
    @Order(18)
    public void testTimerValidConstruction() {
        logger.info("Testing WldtTimer valid construction ...");
        WldtTimer t = new WldtTimer(NS, "dt_model.processing_latency_ms",
                WldtMetricComponent.DT_MODEL, 150L);
        assertEquals(150L, t.getDurationMs());
        assertEquals(0.15, t.getDurationSeconds(), 0.0001);
    }

    /**
     * Zero duration is valid for extremely fast operations.
     */
    @Test
    @Order(19)
    public void testTimerZeroDurationAllowed() {
        logger.info("Testing WldtTimer accepts zero duration ...");
        WldtTimer t = new WldtTimer(NS, "test.metric", WldtMetricComponent.CUSTOM, 0L);
        assertEquals(0L, t.getDurationMs());
    }

    /**
     * Negative duration must throw IllegalArgumentException.
     */
    @Test
    @Order(20)
    public void testTimerNegativeDurationThrows() {
        logger.info("Testing WldtTimer rejects negative duration ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtTimer(NS, "test.metric", WldtMetricComponent.CUSTOM, -10L));
    }

    /**
     * WldtTimer.since() computes duration from a past startMs correctly.
     */
    @Test
    @Order(21)
    public void testTimerSinceFactory() throws InterruptedException {
        logger.info("Testing WldtTimer.since() factory method ...");
        long start = System.currentTimeMillis();
        Thread.sleep(20);
        WldtTimer t = WldtTimer.since(NS, "test.metric", WldtMetricComponent.CUSTOM, start);
        assertTrue(t.getDurationMs() >= 20L, "Duration must be >= 20ms");
        assertTrue(t.getDurationMs() < 2000L, "Duration must be < 2000ms");
    }

    // -------------------------------------------------------------------------
    // WldtHistogram
    // -------------------------------------------------------------------------

    /**
     * Valid WldtHistogram — all fields accessible and getMean() computed correctly.
     */
    @Test
    @Order(22)
    public void testHistogramValidConstruction() {
        logger.info("Testing WldtHistogram valid construction and getMean() ...");
        WldtHistogram h = new WldtHistogram(NS, "physical_adapter.message_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER, 4L, 400.0, 80.0, 120.0);
        assertEquals(4L,    h.getCount());
        assertEquals(400.0, h.getSum(),  0.0001);
        assertEquals(80.0,  h.getMin(),  0.0001);
        assertEquals(120.0, h.getMax(),  0.0001);
        assertEquals(100.0, h.getMean(), 0.0001);
    }

    /**
     * count <= 0 must throw IllegalArgumentException.
     */
    @Test
    @Order(23)
    public void testHistogramZeroCountThrows() {
        logger.info("Testing WldtHistogram rejects zero count ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "test.metric", WldtMetricComponent.CUSTOM,
                        0L, 100.0, 10.0, 20.0));
    }

    /**
     * min > max must throw IllegalArgumentException.
     */
    @Test
    @Order(24)
    public void testHistogramMinGreaterThanMaxThrows() {
        logger.info("Testing WldtHistogram rejects min > max ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "test.metric", WldtMetricComponent.CUSTOM,
                        5L, 100.0, 50.0, 30.0));
    }

    /**
     * NaN sum must throw IllegalArgumentException.
     */
    @Test
    @Order(25)
    public void testHistogramNaNSumThrows() {
        logger.info("Testing WldtHistogram rejects NaN sum ...");
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "test.metric", WldtMetricComponent.CUSTOM,
                        5L, Double.NaN, 10.0, 20.0));
    }
}
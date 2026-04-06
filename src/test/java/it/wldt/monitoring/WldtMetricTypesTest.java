package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.metrics.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all WldtMetric subtypes.
 *
 * <p>Covers construction, validation guards, and all mutation methods
 * ({@code update()}, {@code increment()}, {@code decrement()}, {@code observe()})
 * together with their supporting fields (delta, min/max, cumulative stats).</p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WldtMetricTypesTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(WldtMetricTypesTest.class);

    private static final String NS   = "wldt.internal";
    private static final String NAME = "dt_model.events_processed";

    @BeforeEach public void setUp()    { logger.info("Setting up WldtMetricTypesTest ..."); }
    @AfterEach  public void tearDown() { logger.info("Cleaning up WldtMetricTypesTest ..."); }

    // -------------------------------------------------------------------------
    // WldtMetric base
    // -------------------------------------------------------------------------

    @Test @Order(1)
    public void testGetFullName() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        assertEquals("wldt.internal.dt_model.events_processed", c.getFullName());
    }

    @Test @Order(2)
    public void testTimestampAndLastUpdatedSetAtConstruction() {
        long before = System.currentTimeMillis();
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 1L);
        long after  = System.currentTimeMillis();
        assertTrue(c.getTimestampMs()   >= before && c.getTimestampMs()   <= after);
        assertTrue(c.getLastUpdatedMs() >= before && c.getLastUpdatedMs() <= after);
    }

    // -------------------------------------------------------------------------
    // WldtCounter — construction
    // -------------------------------------------------------------------------

    @Test @Order(3)
    public void testCounterInitialState() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 42L);
        assertEquals(42L, c.getValue());
        assertNull(c.getDelta());
        assertFalse(c.isDeltaAvailable());
        assertEquals(0L, c.getTotalIncrements());
    }

    @Test @Order(4)
    public void testCounterNegativeInitialValueThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, -1L));
    }

    @Test @Order(5)
    public void testCounterNullNamespaceThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(null, NAME, WldtMetricComponent.DT_MODEL, 1L));
    }

    @Test @Order(6)
    public void testCounterNullComponentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtCounter(NS, NAME, null, 1L));
    }

    // -------------------------------------------------------------------------
    // WldtCounter — update()
    // -------------------------------------------------------------------------

    @Test @Order(7)
    public void testCounterUpdateComputesDelta() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        c.update(15L);
        assertEquals(15L, c.getValue());
        assertEquals(Long.valueOf(5L), c.getDelta());
        assertTrue(c.isDeltaAvailable());
        assertEquals(1L, c.getTotalIncrements());
    }

    @Test @Order(8)
    public void testCounterUpdateDecreaseThrows() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        assertThrows(IllegalArgumentException.class, () -> c.update(5L));
    }

    @Test @Order(9)
    public void testCounterUpdateSameValueDeltaZero() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        c.update(10L);
        assertEquals(Long.valueOf(0L), c.getDelta());
    }

    // -------------------------------------------------------------------------
    // WldtCounter — increment()
    // -------------------------------------------------------------------------

    @Test @Order(10)
    public void testCounterIncrementByOne() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 5L);
        c.increment();
        assertEquals(6L, c.getValue());
        assertEquals(Long.valueOf(1L), c.getDelta());
        assertEquals(1L, c.getTotalIncrements());
    }

    @Test @Order(11)
    public void testCounterIncrementByAmount() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        c.increment(7L);
        assertEquals(7L, c.getValue());
        assertEquals(Long.valueOf(7L), c.getDelta());
    }

    @Test @Order(12)
    public void testCounterIncrementZeroAmountThrows() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        assertThrows(IllegalArgumentException.class, () -> c.increment(0L));
    }

    @Test @Order(13)
    public void testCounterIncrementNegativeAmountThrows() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        assertThrows(IllegalArgumentException.class, () -> c.increment(-3L));
    }

    @Test @Order(14)
    public void testCounterMultipleIncrementsTotalIncrements() {
        WldtCounter c = new WldtCounter(NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        c.increment();
        c.increment(2L);
        c.increment(3L);
        assertEquals(6L, c.getValue());
        assertEquals(3L, c.getTotalIncrements());
    }

    // -------------------------------------------------------------------------
    // WldtUpDownCounter — construction
    // -------------------------------------------------------------------------

    @Test @Order(15)
    public void testUpDownCounterInitialState() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 3L);
        assertEquals(3L, c.getValue());
        assertNull(c.getDelta());
        assertFalse(c.isDeltaAvailable());
        assertEquals(3L, c.getPeakValue());
        assertEquals(3L, c.getTroughValue());
        assertEquals(0L, c.getTotalUpdates());
    }

    @Test @Order(16)
    public void testUpDownCounterNegativeInitialAllowed() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, -5L);
        assertEquals(-5L, c.getValue());
        assertEquals(-5L, c.getPeakValue());
        assertEquals(-5L, c.getTroughValue());
    }

    // -------------------------------------------------------------------------
    // WldtUpDownCounter — update(), increment(), decrement()
    // -------------------------------------------------------------------------

    @Test @Order(17)
    public void testUpDownCounterUpdatePositiveDelta() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 2L);
        c.update(5L);
        assertEquals(Long.valueOf(3L), c.getDelta());
        assertEquals(5L, c.getPeakValue());
        assertEquals(2L, c.getTroughValue());
    }

    @Test @Order(18)
    public void testUpDownCounterUpdateNegativeDelta() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 5L);
        c.update(3L);
        assertEquals(Long.valueOf(-2L), c.getDelta());
        assertEquals(3L, c.getTroughValue());
    }

    @Test @Order(19)
    public void testUpDownCounterIncrement() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 0L);
        c.increment();
        assertEquals(1L, c.getValue());
        assertEquals(Long.valueOf(1L), c.getDelta());
    }

    @Test @Order(20)
    public void testUpDownCounterIncrementByAmount() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 0L);
        c.increment(4L);
        assertEquals(4L, c.getValue());
        assertEquals(Long.valueOf(4L), c.getDelta());
    }

    @Test @Order(21)
    public void testUpDownCounterDecrement() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 10L);
        c.decrement();
        assertEquals(9L, c.getValue());
        assertEquals(Long.valueOf(-1L), c.getDelta());
    }

    @Test @Order(22)
    public void testUpDownCounterDecrementByAmount() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 10L);
        c.decrement(3L);
        assertEquals(7L, c.getValue());
        assertEquals(Long.valueOf(-3L), c.getDelta());
        assertEquals(7L, c.getTroughValue());
    }

    @Test @Order(23)
    public void testUpDownCounterDecrementZeroThrows() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 5L);
        assertThrows(IllegalArgumentException.class, () -> c.decrement(0L));
    }

    @Test @Order(24)
    public void testUpDownCounterPeakTroughTracking() {
        WldtUpDownCounter c = new WldtUpDownCounter(NS, "m", WldtMetricComponent.CUSTOM, 5L);
        c.update(10L);
        c.update(2L);
        c.update(8L);
        assertEquals(10L, c.getPeakValue());
        assertEquals(2L,  c.getTroughValue());
        assertEquals(3L,  c.getTotalUpdates());
    }

    // -------------------------------------------------------------------------
    // WldtGauge
    // -------------------------------------------------------------------------

    @Test @Order(25)
    public void testGaugeInitialState() {
        WldtGauge g = new WldtGauge(NS, "queue.depth", WldtMetricComponent.CUSTOM, 12.5);
        assertEquals(12.5, g.getValue(), 0.0001);
        assertNull(g.getPreviousValue());
        assertNull(g.getDelta());
        assertEquals(12.5, g.getMinObserved(), 0.0001);
        assertEquals(12.5, g.getMaxObserved(), 0.0001);
        assertEquals(0L, g.getUpdateCount());
    }

    @Test @Order(26)
    public void testGaugeUpdateComputesDeltaAndPrevious() {
        WldtGauge g = new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, 10.0);
        g.update(15.5);
        assertEquals(15.5,  g.getValue(),         0.0001);
        assertEquals(10.0,  g.getPreviousValue(),  0.0001);
        assertEquals(5.5,   g.getDelta(),          0.0001);
        assertEquals(10.0,  g.getMinObserved(),    0.0001);
        assertEquals(15.5,  g.getMaxObserved(),    0.0001);
        assertEquals(1L,    g.getUpdateCount());
    }

    @Test @Order(27)
    public void testGaugeUpdateNegativeDelta() {
        WldtGauge g = new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, 20.0);
        g.update(8.0);
        assertEquals(-12.0, g.getDelta(), 0.0001);
        assertEquals(8.0,   g.getMinObserved(), 0.0001);
    }

    @Test @Order(28)
    public void testGaugeMinMaxTracking() {
        WldtGauge g = new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, 5.0);
        g.update(1.0);
        g.update(9.0);
        g.update(3.0);
        assertEquals(1.0, g.getMinObserved(), 0.0001);
        assertEquals(9.0, g.getMaxObserved(), 0.0001);
        assertEquals(3L,  g.getUpdateCount());
    }

    @Test @Order(29)
    public void testGaugeNaNThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, Double.NaN));
    }

    @Test @Order(30)
    public void testGaugeUpdateNaNThrows() {
        WldtGauge g = new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, 1.0);
        assertThrows(IllegalArgumentException.class, () -> g.update(Double.NaN));
    }

    @Test @Order(31)
    public void testGaugeInfiniteThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtGauge(NS, "q", WldtMetricComponent.CUSTOM, Double.POSITIVE_INFINITY));
    }

    // -------------------------------------------------------------------------
    // WldtTimer
    // -------------------------------------------------------------------------

    @Test @Order(32)
    public void testTimerInitialState() {
        WldtTimer t = new WldtTimer(NS, "latency", WldtMetricComponent.DT_MODEL, 150L);
        assertEquals(150L,  t.getDurationMs());
        assertEquals(0.15,  t.getDurationSeconds(), 0.0001);
        assertEquals(150L,  t.getMinDurationMs());
        assertEquals(150L,  t.getMaxDurationMs());
        assertEquals(150L,  t.getTotalDurationMs());
        assertEquals(1L,    t.getObservationCount());
        assertEquals(150.0, t.getMeanDurationMs(), 0.0001);
    }

    @Test @Order(33)
    public void testTimerZeroDurationAllowed() {
        WldtTimer t = new WldtTimer(NS, "m", WldtMetricComponent.CUSTOM, 0L);
        assertEquals(0L, t.getDurationMs());
    }

    @Test @Order(34)
    public void testTimerNegativeDurationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtTimer(NS, "m", WldtMetricComponent.CUSTOM, -10L));
    }

    @Test @Order(35)
    public void testTimerUpdateAccumulates() {
        WldtTimer t = new WldtTimer(NS, "latency", WldtMetricComponent.DT_MODEL, 100L);
        t.update(200L);
        t.update(50L);
        assertEquals(50L,    t.getDurationMs());
        assertEquals(50L,    t.getMinDurationMs());
        assertEquals(200L,   t.getMaxDurationMs());
        assertEquals(350L,   t.getTotalDurationMs());
        assertEquals(3L,     t.getObservationCount());
        assertEquals(350.0 / 3, t.getMeanDurationMs(), 0.0001);
    }

    @Test @Order(36)
    public void testTimerUpdateNegativeThrows() {
        WldtTimer t = new WldtTimer(NS, "m", WldtMetricComponent.CUSTOM, 10L);
        assertThrows(IllegalArgumentException.class, () -> t.update(-5L));
    }

    @Test @Order(37)
    public void testTimerUpdateSince() throws InterruptedException {
        WldtTimer t = new WldtTimer(NS, "m", WldtMetricComponent.CUSTOM, 0L);
        long start = System.currentTimeMillis();
        Thread.sleep(20);
        t.updateSince(start);
        assertTrue(t.getDurationMs() >= 20L);
        assertEquals(2L, t.getObservationCount());
    }

    // -------------------------------------------------------------------------
    // WldtHistogram
    // -------------------------------------------------------------------------

    @Test @Order(38)
    public void testHistogramInitialState() {
        WldtHistogram h = new WldtHistogram(NS, "msg_size", WldtMetricComponent.PHYSICAL_ADAPTER,
                4L, 400.0, 80.0, 120.0);
        assertEquals(4L,    h.getCount());
        assertEquals(400.0, h.getSum(),  0.0001);
        assertEquals(80.0,  h.getMin(),  0.0001);
        assertEquals(120.0, h.getMax(),  0.0001);
        assertEquals(100.0, h.getMean(), 0.0001);
        assertEquals(4L,    h.getTotalCount());
        assertEquals(400.0, h.getTotalSum(),  0.0001);
        assertEquals(80.0,  h.getGlobalMin(), 0.0001);
        assertEquals(120.0, h.getGlobalMax(), 0.0001);
        assertEquals(1L,    h.getWindowCount());
        assertEquals(100.0, h.getGlobalMean(), 0.0001);
    }

    @Test @Order(39)
    public void testHistogramZeroCountThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM, 0L, 10.0, 1.0, 5.0));
    }

    @Test @Order(40)
    public void testHistogramMinGreaterThanMaxThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM, 2L, 10.0, 9.0, 5.0));
    }

    @Test @Order(41)
    public void testHistogramNaNSumThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM, 2L, Double.NaN, 1.0, 5.0));
    }

    @Test @Order(42)
    public void testHistogramObserveSingleValue() {
        WldtHistogram h = new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM,
                1L, 10.0, 10.0, 10.0);
        h.observe(20.0);
        assertEquals(2L,    h.getTotalCount());
        assertEquals(30.0,  h.getTotalSum(),  0.0001);
        assertEquals(10.0,  h.getGlobalMin(), 0.0001);
        assertEquals(20.0,  h.getGlobalMax(), 0.0001);
        assertEquals(2L,    h.getWindowCount());
        // current window after observe is count=1, sum=20, min=20, max=20
        assertEquals(1L,    h.getCount());
        assertEquals(20.0,  h.getSum(),  0.0001);
        assertEquals(20.0,  h.getMean(), 0.0001);
    }

    @Test @Order(43)
    public void testHistogramObserveNaNThrows() {
        WldtHistogram h = new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM,
                1L, 5.0, 5.0, 5.0);
        assertThrows(IllegalArgumentException.class, () -> h.observe(Double.NaN));
    }

    @Test @Order(44)
    public void testHistogramUpdateWindow() {
        WldtHistogram h = new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM,
                2L, 20.0, 8.0, 12.0);
        h.update(3L, 60.0, 5.0, 25.0);
        assertEquals(5L,    h.getTotalCount());
        assertEquals(80.0,  h.getTotalSum(),  0.0001);
        assertEquals(5.0,   h.getGlobalMin(), 0.0001);
        assertEquals(25.0,  h.getGlobalMax(), 0.0001);
        assertEquals(2L,    h.getWindowCount());
        assertEquals(3L,    h.getCount());
        assertEquals(60.0,  h.getSum(),  0.0001);
    }

    @Test @Order(45)
    public void testHistogramGlobalMeanAcrossMultipleObservations() {
        WldtHistogram h = new WldtHistogram(NS, "m", WldtMetricComponent.CUSTOM,
                1L, 10.0, 10.0, 10.0);
        h.observe(20.0);
        h.observe(30.0);
        // total: count=3, sum=60
        assertEquals(3L,   h.getTotalCount());
        assertEquals(60.0, h.getTotalSum(),   0.0001);
        assertEquals(20.0, h.getGlobalMean(), 0.0001);
    }
}

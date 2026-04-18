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
 *   <li>Explicit {@link WldtMetricRegistry#register(WldtMetric)} stores the live instance</li>
 *   <li>{@link WldtMetricRegistry#update(WldtMetric)} mutates the stored instance in-place</li>
 *   <li>Delta is computed by the metric class itself (WldtCounter, WldtUpDownCounter)</li>
 *   <li>Gauge, Timer, Histogram accumulate supporting fields on update</li>
 *   <li>update() on unregistered metric throws {@link IllegalStateException}</li>
 *   <li>update() with type mismatch throws {@link IllegalArgumentException}</li>
 *   <li>deregister() removes the metric; next register starts fresh</li>
 *   <li>getMetric(), getAllMetrics(), isRegistered(), size(), clear() query methods</li>
 *   <li>Null / blank guard validation</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WldtMetricRegistryTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(WldtMetricRegistryTest.class);

    private static final String TEST_DT_ID = "test_dt_id";

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
    // register() and isRegistered()
    // -------------------------------------------------------------------------

    @Test @Order(1)
    public void testRegisterStoresMetric() {
        WldtCounter c = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L);
        registry.register(c);
        assertTrue(registry.isRegistered(c.getFullName()));
    }

    @Test @Order(2)
    public void testRegisterNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    // -------------------------------------------------------------------------
    // update() — WldtCounter delta
    // -------------------------------------------------------------------------

    @Test @Order(3)
    public void testCounterUpdateComputesDelta() {
        WldtCounter live = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L);
        registry.register(live);

        WldtMetric returned = registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 8L));

        assertSame(live, returned, "registry.update() must return the stored live instance");
        assertEquals(8L, live.getValue());
        assertEquals(Long.valueOf(3L), live.getDelta());
        assertTrue(live.isDeltaAvailable());
    }

    @Test @Order(4)
    public void testCounterMultipleUpdatesDeltaAccumulates() {
        WldtCounter live = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        registry.register(live);

        registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L));
        registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 12L));
        registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 20L));

        assertEquals(20L, live.getValue());
        assertEquals(Long.valueOf(8L), live.getDelta());
        assertEquals(3L, live.getTotalIncrements());
    }

    // -------------------------------------------------------------------------
    // update() — WldtUpDownCounter signed delta
    // -------------------------------------------------------------------------

    @Test @Order(5)
    public void testUpDownCounterPositiveDelta() {
        WldtUpDownCounter live = new WldtUpDownCounter(TEST_DT_ID, NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 2L);
        registry.register(live);
        registry.update(new WldtUpDownCounter(TEST_DT_ID, NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 5L));
        assertEquals(Long.valueOf(3L), live.getDelta());
    }

    @Test @Order(6)
    public void testUpDownCounterNegativeDelta() {
        WldtUpDownCounter live = new WldtUpDownCounter(TEST_DT_ID, NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 5L);
        registry.register(live);
        registry.update(new WldtUpDownCounter(TEST_DT_ID, NS, "pa.connected", WldtMetricComponent.PHYSICAL_ADAPTER, 3L));
        assertEquals(Long.valueOf(-2L), live.getDelta());
        assertEquals(3L, live.getTroughValue());
    }

    // -------------------------------------------------------------------------
    // update() — WldtGauge accumulates min/max
    // -------------------------------------------------------------------------

    @Test @Order(7)
    public void testGaugeUpdateAccumulatesMinMax() {
        WldtGauge live = new WldtGauge(TEST_DT_ID, NS, "queue.depth", WldtMetricComponent.CUSTOM, 10.0);
        registry.register(live);
        registry.update(new WldtGauge(TEST_DT_ID, NS, "queue.depth", WldtMetricComponent.CUSTOM, 15.0));
        registry.update(new WldtGauge(TEST_DT_ID, NS, "queue.depth", WldtMetricComponent.CUSTOM, 3.0));
        assertEquals(3.0,  live.getValue(),      0.0001);
        assertEquals(3.0,  live.getMinObserved(), 0.0001);
        assertEquals(15.0, live.getMaxObserved(), 0.0001);
        assertEquals(2L,   live.getUpdateCount());
    }

    // -------------------------------------------------------------------------
    // update() — WldtTimer accumulates min/max/count
    // -------------------------------------------------------------------------

    @Test @Order(8)
    public void testTimerUpdateAccumulates() {
        WldtTimer live = new WldtTimer(TEST_DT_ID, NS, "latency", WldtMetricComponent.DT_MODEL, 100L);
        registry.register(live);
        registry.update(new WldtTimer(TEST_DT_ID, NS, "latency", WldtMetricComponent.DT_MODEL, 200L));
        registry.update(new WldtTimer(TEST_DT_ID, NS, "latency", WldtMetricComponent.DT_MODEL, 50L));
        assertEquals(50L,  live.getDurationMs());
        assertEquals(50L,  live.getMinDurationMs());
        assertEquals(200L, live.getMaxDurationMs());
        assertEquals(350L, live.getTotalDurationMs());
        assertEquals(3L,   live.getObservationCount());
    }

    // -------------------------------------------------------------------------
    // update() — WldtHistogram merges windows
    // -------------------------------------------------------------------------

    @Test @Order(9)
    public void testHistogramUpdateMergesWindows() {
        WldtHistogram live = new WldtHistogram(TEST_DT_ID, NS, "msg_size", WldtMetricComponent.PHYSICAL_ADAPTER,
                2L, 20.0, 8.0, 12.0);
        registry.register(live);
        registry.update(new WldtHistogram(TEST_DT_ID, NS, "msg_size", WldtMetricComponent.PHYSICAL_ADAPTER,
                3L, 60.0, 5.0, 25.0));
        assertEquals(5L,    live.getTotalCount());
        assertEquals(80.0,  live.getTotalSum(),  0.0001);
        assertEquals(5.0,   live.getGlobalMin(), 0.0001);
        assertEquals(25.0,  live.getGlobalMax(), 0.0001);
        assertEquals(2L,    live.getWindowCount());
    }

    // -------------------------------------------------------------------------
    // update() — error cases
    // -------------------------------------------------------------------------

    @Test @Order(10)
    public void testUpdateUnregisteredMetricThrows() {
        assertThrows(IllegalStateException.class,
                () -> registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L)));
    }

    @Test @Order(11)
    public void testUpdateNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.update(null));
    }

    @Test @Order(12)
    public void testUpdateTypeMismatchThrows() {
        registry.register(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 0L));
        assertThrows(IllegalArgumentException.class,
                () -> registry.update(new WldtGauge(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5.0)));
    }

    // -------------------------------------------------------------------------
    // deregister()
    // -------------------------------------------------------------------------

    @Test @Order(13)
    public void testDeregisterRemovesMetric() {
        WldtCounter c = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        registry.register(c);
        registry.deregister(NS + "." + NAME);
        assertFalse(registry.isRegistered(NS + "." + NAME));
    }

    @Test @Order(14)
    public void testDeregisterThenReregisterStartsFresh() {
        WldtCounter live1 = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 10L);
        registry.register(live1);
        registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 15L));
        registry.deregister(NS + "." + NAME);

        WldtCounter live2 = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 0L);
        registry.register(live2);
        assertNull(live2.getDelta(), "Delta must be null for freshly registered metric");
    }

    @Test @Order(15)
    public void testDeregisterNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.deregister(null));
    }

    // -------------------------------------------------------------------------
    // Query methods
    // -------------------------------------------------------------------------

    @Test @Order(16)
    public void testGetMetricReturnsLiveInstance() {
        WldtCounter live = new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L);
        registry.register(live);
        registry.update(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 9L));

        assertTrue(registry.getMetric(NS + "." + NAME).isPresent());
        assertSame(live, registry.getMetric(NS + "." + NAME).get());
        assertEquals(9L, ((WldtCounter) registry.getMetric(NS + "." + NAME).get()).getValue());
    }

    @Test @Order(17)
    public void testGetMetricEmptyForUnknown() {
        assertFalse(registry.getMetric("wldt.internal.unknown").isPresent());
    }

    @Test @Order(18)
    public void testGetAllMetricsSize() {
        registry.register(new WldtCounter(TEST_DT_ID, NS, "m1", WldtMetricComponent.DT_MODEL, 0L));
        registry.register(new WldtGauge(TEST_DT_ID, NS, "m2", WldtMetricComponent.CUSTOM, 1.0));
        registry.register(new WldtTimer(TEST_DT_ID, NS, "m3", WldtMetricComponent.STORAGE, 10L));
        assertEquals(3, registry.getAllMetrics().size());
    }

    @Test @Order(19)
    public void testClearEmptiesRegistry() {
        registry.register(new WldtCounter(TEST_DT_ID, NS, NAME, WldtMetricComponent.DT_MODEL, 5L));
        registry.register(new WldtGauge(TEST_DT_ID, NS, "g", WldtMetricComponent.CUSTOM, 1.0));
        assertEquals(2, registry.size());
        registry.clear();
        assertEquals(0, registry.size());
    }

    @Test @Order(20)
    public void testGetMetricBlankThrows() {
        assertThrows(IllegalArgumentException.class, () -> registry.getMetric("   "));
    }
}

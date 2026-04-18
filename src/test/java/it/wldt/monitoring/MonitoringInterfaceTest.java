package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.metrics.*;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MonitoringInterface}.
 *
 * <p>Registration semantics (new in this version):</p>
 * <ul>
 *   <li>A push <em>with</em> an initial value fires {@code onMetricRegistered} with an
 *       <strong>empty snapshot</strong> (no value, {@code isInitialized()==false}), then
 *       immediately fires {@code onMetricUpdated} with the actual initial value
 *       ({@code delta==null} — first value, not an increment).</li>
 *   <li>A push <em>without</em> a value fires only {@code onMetricRegistered} (empty snapshot).</li>
 *   <li>Every subsequent push for the same name fires {@code onMetricUpdated} with the
 *       mutated live snapshot ({@code delta} reflects the change from previous value).</li>
 * </ul>
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Flag gating — metrics from disabled components are silently discarded</li>
 *   <li>No-value registration — only {@code onMetricRegistered} fires</li>
 *   <li>Push with initial value — both callbacks fire; registered snapshot is empty</li>
 *   <li>Subsequent push — {@code onMetricUpdated} with non-null delta</li>
 *   <li>Component passed correctly to both callbacks</li>
 *   <li>Custom metric support via {@code trackCustomMetric()}</li>
 *   <li>Query support ({@code getMetric()}, {@code getAllMetrics()}, {@code isMetricRegistered()})</li>
 *   <li>Concurrent push safety</li>
 *   <li>Guard validation (null metric, wrong CUSTOM component)</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MonitoringInterfaceTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(MonitoringInterfaceTest.class);

    private static final String TEST_DT_ID = "test_dt_id";
    private static final String NS   = "wldt.internal";
    private static final String CUST = "custom.myapp";

    private TestMonitoringHandler handler;
    private MonitoringInterface   monitoringInterface;

    @BeforeEach
    public void setUp() {
        logger.info("Setting up MonitoringInterfaceTest ...");
        handler = new TestMonitoringHandler();
        MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
                .withDtModelMonitoring()
                .withCustomNamespace(CUST)
                .build();
        monitoringInterface = new MonitoringInterface();
        monitoringInterface.setConfiguration(config);
        monitoringInterface.setHandler(handler);
    }

    @AfterEach
    public void tearDown() {
        logger.info("Cleaning up MonitoringInterfaceTest ...");
        handler = null;
        monitoringInterface = null;
    }

    // -------------------------------------------------------------------------
    // Flag gating
    // -------------------------------------------------------------------------

    @Test @Order(1)
    public void testEnabledComponentWithValueFiresBothCallbacks() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(1, handler.updatedMetrics.size(),
                "Push with initial value must also fire onMetricUpdated");
    }

    @Test @Order(2)
    public void testDisabledComponentIsDiscarded() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "pa.messages", WldtMetricComponent.PHYSICAL_ADAPTER, 1L));
        assertTrue(handler.registeredMetrics.isEmpty());
        assertTrue(handler.updatedMetrics.isEmpty());
    }

    @Test @Order(3)
    public void testDisabledStorageIsDiscarded() {
        monitoringInterface.notifyMetric(
                new WldtTimer(TEST_DT_ID, NS, "write.latency", WldtMetricComponent.STORAGE, 50L));
        assertTrue(handler.registeredMetrics.isEmpty());
        assertTrue(handler.updatedMetrics.isEmpty());
    }

    // -------------------------------------------------------------------------
    // No-value registration
    // -------------------------------------------------------------------------

    @Test @Order(4)
    public void testNoValuePushFiresOnlyRegisteredCallback() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL));
        assertEquals(1, handler.registeredMetrics.size());
        assertTrue(handler.updatedMetrics.isEmpty(),
                "No-value push must not fire onMetricUpdated");
        assertFalse(handler.registeredMetrics.get(0).isInitialized(),
                "Registered snapshot must be uninitialized");
    }

    @Test @Order(5)
    public void testNoValueThenValuePushFiresUpdatedWithNullDelta() {
        // Register without value
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL));
        // First push with actual value
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 5L));

        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(1, handler.updatedMetrics.size());
        assertNull(handler.updatedDeltas.get(0).delta,
                "First actual value after no-value registration must have null delta");
        assertEquals(5L, ((WldtCounter) handler.updatedMetrics.get(0)).getValue());
    }

    // -------------------------------------------------------------------------
    // Registered vs Updated callbacks (push with initial value)
    // -------------------------------------------------------------------------

    @Test @Order(6)
    public void testFirstPushWithValueFiresRegisteredWithEmptySnapshot() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(WldtMetricComponent.DT_MODEL, handler.registeredComponents.get(0));
        assertFalse(handler.registeredMetrics.get(0).isInitialized(),
                "onMetricRegistered must receive an empty (uninitialized) snapshot");
    }

    @Test @Order(7)
    public void testFirstPushWithValueImmediatelyFiresUpdated() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        assertEquals(1, handler.updatedMetrics.size());
        assertTrue(handler.updatedMetrics.get(0).isInitialized());
        assertNull(handler.updatedDeltas.get(0).delta,
                "Initial value push must have null delta (first value, not an increment)");
        assertEquals(1L, ((WldtCounter) handler.updatedMetrics.get(0)).getValue());
    }

    @Test @Order(8)
    public void testSecondPushFiresUpdatedWithDelta() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 4L));

        // 1 registered + 2 updated (initial value + incremental update)
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(2, handler.updatedMetrics.size());
        assertEquals(WldtMetricComponent.DT_MODEL, handler.updatedComponents.get(1));
        assertEquals(Long.valueOf(3L), handler.updatedDeltas.get(1).delta);
    }

    @Test @Order(9)
    public void testDeltaSemanticsThroughFullLifecycle() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 5L));
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 8L));

        // Registered snapshot: empty, no delta
        assertFalse(handler.registeredMetrics.get(0).isInitialized());
        assertNull(handler.registeredDeltas.get(0).delta,
                "Empty snapshot must have null delta");

        // First onMetricUpdated: initial value=5, delta=null (initialization)
        assertNull(handler.updatedDeltas.get(0).delta,
                "Initial value push delta must be null");
        assertEquals(5L, ((WldtCounter) handler.updatedMetrics.get(0)).getValue());

        // Second onMetricUpdated: value=8, delta=3
        assertEquals(Long.valueOf(3L), handler.updatedDeltas.get(1).delta);
        assertEquals(8L, ((WldtCounter) handler.updatedMetrics.get(1)).getValue());
    }

    // -------------------------------------------------------------------------
    // Component routing
    // -------------------------------------------------------------------------

    @Test @Order(10)
    public void testCustomMetricBypassesFlagGatingAndFiresBothCallbacks() {
        monitoringInterface.trackCustomMetric(
                new WldtGauge(TEST_DT_ID, CUST, "room.temp", WldtMetricComponent.CUSTOM, 21.5));
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(1, handler.updatedMetrics.size());
        assertEquals(WldtMetricComponent.CUSTOM, handler.registeredComponents.get(0));
        assertEquals(WldtMetricComponent.CUSTOM, handler.updatedComponents.get(0));
    }

    @Test @Order(11)
    public void testCustomMetricSecondPushFiresUpdatedWithDelta() {
        monitoringInterface.trackCustomMetric(
                new WldtCounter(TEST_DT_ID, CUST, "c", WldtMetricComponent.CUSTOM, 10L));
        monitoringInterface.trackCustomMetric(
                new WldtCounter(TEST_DT_ID, CUST, "c", WldtMetricComponent.CUSTOM, 14L));

        // registered=1, updated=2 (init + increment)
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(2, handler.updatedMetrics.size());
        assertNull(handler.updatedDeltas.get(0).delta, "Initial push delta must be null");
        assertEquals(Long.valueOf(4L), handler.updatedDeltas.get(1).delta);
    }

    // -------------------------------------------------------------------------
    // Query support
    // -------------------------------------------------------------------------

    @Test @Order(12)
    public void testGetMetricReturnsLiveInstance() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 7L));
        Optional<WldtMetric> result = monitoringInterface.getMetric(NS + ".events");
        assertTrue(result.isPresent());
        assertEquals(7L, ((WldtCounter) result.get()).getValue());
    }

    @Test @Order(13)
    public void testGetMetricEmptyForUnknown() {
        assertFalse(monitoringInterface.getMetric("wldt.internal.unknown").isPresent());
    }

    @Test @Order(14)
    public void testGetAllMetricsSize() {
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        monitoringInterface.notifyMetric(
                new WldtGauge(TEST_DT_ID, NS, "model.cpu", WldtMetricComponent.DT_MODEL, 2.0));
        assertEquals(2, monitoringInterface.getAllMetrics().size());
    }

    @Test @Order(15)
    public void testIsMetricRegisteredLifecycle() {
        assertFalse(monitoringInterface.isMetricRegistered(NS + ".events"));
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        assertTrue(monitoringInterface.isMetricRegistered(NS + ".events"));
    }

    @Test @Order(16)
    public void testExplicitRegisterMetricWithValueFiresBothCallbacksThenUpdate() {
        monitoringInterface.registerMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 10L));
        // registerMetric with a value fires both registered (empty) and updated (initial value)
        assertEquals(1, handler.registeredMetrics.size(),
                "registerMetric() must fire onMetricRegistered");
        assertEquals(1, handler.updatedMetrics.size(),
                "registerMetric() with value must fire onMetricUpdated with initial value");
        assertNull(handler.updatedDeltas.get(0).delta,
                "Initial value via registerMetric() must have null delta");

        // Subsequent notifyMetric goes into update path; delta = 13 - 10 = 3
        monitoringInterface.notifyMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 13L));
        assertEquals(2, handler.updatedMetrics.size());
        assertEquals(Long.valueOf(3L), handler.updatedDeltas.get(1).delta);
    }

    @Test @Order(17)
    public void testExplicitRegisterMetricWithoutValueFiresOnlyRegistered() {
        monitoringInterface.registerMetric(
                new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL));
        assertEquals(1, handler.registeredMetrics.size());
        assertTrue(handler.updatedMetrics.isEmpty(),
                "registerMetric() without value must not fire onMetricUpdated");
        assertFalse(handler.registeredMetrics.get(0).isInitialized());
    }

    // -------------------------------------------------------------------------
    // Concurrent push safety
    // -------------------------------------------------------------------------

    @Test @Order(18)
    public void testConcurrentPushesAreHandledSafely() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch allPushed   = new CountDownLatch(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            final double value = i + 1.0;
            new Thread(() -> {
                try {
                    startSignal.await();
                    monitoringInterface.notifyMetric(
                            new WldtGauge(TEST_DT_ID, NS, "concurrent.gauge",
                                    WldtMetricComponent.DT_MODEL, value));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allPushed.countDown();
                }
            }).start();
        }

        startSignal.countDown();
        assertTrue(allPushed.await(5000, TimeUnit.MILLISECONDS),
                "All threads must complete within 5 seconds");
        // Exactly one registration; the registering thread also fires onMetricUpdated,
        // so total updated = threadCount (1 from registering thread + threadCount-1 updates)
        assertEquals(1, handler.registeredMetrics.size());
        assertEquals(threadCount, handler.updatedMetrics.size());
    }

    // -------------------------------------------------------------------------
    // Guard validation
    // -------------------------------------------------------------------------

    @Test @Order(19)
    public void testNotifyNullDoesNotThrow() {
        assertDoesNotThrow(() -> monitoringInterface.notifyMetric(null));
        assertTrue(handler.registeredMetrics.isEmpty());
    }

    @Test @Order(20)
    public void testTrackCustomMetricWrongComponentThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> monitoringInterface.trackCustomMetric(
                        new WldtCounter(TEST_DT_ID, NS, "events", WldtMetricComponent.DT_MODEL, 1L)));
    }

    @Test @Order(21)
    public void testTrackCustomMetricNullThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> monitoringInterface.trackCustomMetric(null));
    }

    // =========================================================================
    // Test doubles
    // =========================================================================

    /** Snapshot of a metric's delta value captured at callback time. */
    static class DeltaSnapshot {
        final Long delta;
        DeltaSnapshot(WldtMetric m) {
            this.delta = (m instanceof WldtCounter) ? ((WldtCounter) m).getDelta() : null;
        }
    }

    static class TestMonitoringHandler extends MonitoringInterfaceHandler {

        final List<WldtMetric>          registeredMetrics    = new ArrayList<>();
        final List<WldtMetricComponent> registeredComponents = new ArrayList<>();
        final List<DeltaSnapshot>       registeredDeltas     = new ArrayList<>();
        final List<WldtMetric>          updatedMetrics       = new ArrayList<>();
        final List<WldtMetricComponent> updatedComponents    = new ArrayList<>();
        final List<DeltaSnapshot>       updatedDeltas        = new ArrayList<>();

        @Override
        public synchronized void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {
            registeredMetrics.add(metric);
            registeredComponents.add(component);
            registeredDeltas.add(new DeltaSnapshot(metric));
        }

        @Override
        public synchronized void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
            updatedMetrics.add(metric);
            updatedComponents.add(component);
            updatedDeltas.add(new DeltaSnapshot(metric));
        }
    }
}

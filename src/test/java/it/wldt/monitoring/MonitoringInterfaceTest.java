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
 * <p>Uses a {@link TestMonitoringHandler} — a concrete inner implementation of
 * {@link WldtMonitoringHandler} that captures all received metrics in per-component
 * lists, allowing assertions on routing, delta values, and callback counts.</p>
 *
 * <p>Uses a {@link NoOpLogger} — a minimal no-op implementation of {@link WldtLogger}
 * that satisfies the MonitoringInterface constructor without producing any output.</p>
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Flag gating — metrics from disabled components are silently discarded</li>
 *   <li>Routing — each component routes to the correct handler callback</li>
 *   <li>Delta injection — counter delta computed before dispatch</li>
 *   <li>Lazy registration — first push auto-registers the metric</li>
 *   <li>Custom metric push via {@code trackCustomMetric()}</li>
 *   <li>{@code getMetric()} and {@code getAllMetrics()} query support</li>
 *   <li>Constructor guard validation</li>
 *   <li>{@code trackCustomMetric()} guard validation (non-CUSTOM component)</li>
 *   <li>Concurrent push safety via CountDownLatch</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MonitoringInterfaceTest {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(MonitoringInterfaceTest.class);

    private static final String NS   = "wldt.internal";
    private static final String CUST = "custom.myapp";

    private static TestMonitoringHandler handler;
    private static MonitoringInterface   monitoringInterface;

    @BeforeEach
    public void setUp() {
        logger.info("Setting up MonitoringInterfaceTest ...");

        handler = new TestMonitoringHandler();

        MonitoringConfiguration config = new MonitoringConfiguration.Builder()
                .withDtModelMonitoring()
                .withEventBusMonitoring()
                .withCustomNamespace(CUST)
                .build();

        monitoringInterface = new MonitoringInterface(config, handler, new NoOpLogger());
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

    /**
     * Metric from DT_MODEL (enabled) must reach the handler callback.
     */
    @Test
    @Order(1)
    public void testEnabledComponentReachesHandler() {
        logger.info("Testing metric from enabled component reaches handler ...");
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 1L));

        assertEquals(1, handler.dtModelMetrics.size());
    }

    /**
     * Metric from PHYSICAL_ADAPTER (disabled in setUp config) must be silently discarded.
     */
    @Test
    @Order(2)
    public void testDisabledComponentIsDiscarded() {
        logger.info("Testing metric from disabled component (PHYSICAL_ADAPTER) is discarded ...");
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "pa.messages", WldtMetricComponent.PHYSICAL_ADAPTER, 1L));

        assertTrue(handler.physicalAdapterMetrics.isEmpty(),
                "Physical adapter metrics must be discarded when flag is disabled");
    }

    /**
     * Metric from STORAGE (disabled in setUp config) must be silently discarded.
     */
    @Test
    @Order(3)
    public void testDisabledStorageIsDiscarded() {
        logger.info("Testing metric from disabled component (STORAGE) is discarded ...");
        monitoringInterface.notifyMetric(
                new WldtTimer(NS, "write.latency", WldtMetricComponent.STORAGE, 50L));

        assertTrue(handler.storageMetrics.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Routing
    // -------------------------------------------------------------------------

    /**
     * DT_MODEL metric routes exclusively to onDigitalTwinModelMetric().
     */
    @Test
    @Order(4)
    public void testRoutingDtModelCallback() {
        logger.info("Testing DT_MODEL metric routes to correct callback ...");
        monitoringInterface.notifyMetric(
                new WldtGauge(NS, "state.props", WldtMetricComponent.DT_MODEL, 5.0));

        assertEquals(1, handler.dtModelMetrics.size());
        assertTrue(handler.eventBusMetrics.isEmpty());
        assertTrue(handler.customMetrics.isEmpty());
    }

    /**
     * EVENT_BUS metric routes exclusively to onEventBusMetric().
     */
    @Test
    @Order(5)
    public void testRoutingEventBusCallback() {
        logger.info("Testing EVENT_BUS metric routes to correct callback ...");
        monitoringInterface.notifyMetric(
                new WldtGauge(NS, "queue.depth", WldtMetricComponent.EVENT_BUS, 3.0));

        assertEquals(1, handler.eventBusMetrics.size());
        assertTrue(handler.dtModelMetrics.isEmpty());
    }

    /**
     * CUSTOM metric via trackCustomMetric() routes to onCustomMetric().
     */
    @Test
    @Order(6)
    public void testRoutingCustomMetricCallback() {
        logger.info("Testing CUSTOM metric routes to onCustomMetric() ...");
        monitoringInterface.trackCustomMetric(
                new WldtGauge(CUST, "room.temperature", WldtMetricComponent.CUSTOM, 21.5));

        assertEquals(1, handler.customMetrics.size());
        assertTrue(handler.dtModelMetrics.isEmpty());
    }

    // -------------------------------------------------------------------------
    // Delta injection
    // -------------------------------------------------------------------------

    /**
     * On first push delta is null; on second push delta is computed and injected
     * by the registry before dispatch to the handler.
     */
    @Test
    @Order(7)
    public void testDeltaInjectedBeforeDispatch() {
        logger.info("Testing delta is computed and injected before dispatch to handler ...");
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 5L));
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 8L));

        assertEquals(2, handler.dtModelMetrics.size());

        WldtCounter first  = (WldtCounter) handler.dtModelMetrics.get(0);
        WldtCounter second = (WldtCounter) handler.dtModelMetrics.get(1);

        assertNull(first.getDelta(), "Delta on first push must be null");
        assertEquals(Long.valueOf(3L), second.getDelta(), "Delta on second push must be 3");
        assertEquals(8L, second.getValue());
    }

    /**
     * Custom WldtCounter pushed via trackCustomMetric() also receives delta injection.
     */
    @Test
    @Order(8)
    public void testCustomCounterAlsoGetsDeltaInjected() {
        logger.info("Testing custom WldtCounter also receives delta injection ...");
        monitoringInterface.trackCustomMetric(
                new WldtCounter(CUST, "custom.count", WldtMetricComponent.CUSTOM, 10L));
        monitoringInterface.trackCustomMetric(
                new WldtCounter(CUST, "custom.count", WldtMetricComponent.CUSTOM, 14L));

        assertEquals(2, handler.customMetrics.size());
        WldtCounter second = (WldtCounter) handler.customMetrics.get(1);
        assertEquals(Long.valueOf(4L), second.getDelta());
    }

    // -------------------------------------------------------------------------
    // Query support
    // -------------------------------------------------------------------------

    /**
     * getMetric() returns the last pushed value for a registered metric.
     */
    @Test
    @Order(9)
    public void testGetMetricReturnsLastPushedValue() {
        logger.info("Testing getMetric() returns last pushed value ...");
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 7L));

        Optional<WldtMetric> result = monitoringInterface.getMetric(NS + ".events");
        assertTrue(result.isPresent());
        assertEquals(7L, ((WldtCounter) result.get()).getValue());
    }

    /**
     * getMetric() returns empty Optional for a name that has never been pushed.
     */
    @Test
    @Order(10)
    public void testGetMetricEmptyForUnknownName() {
        logger.info("Testing getMetric() returns empty for unknown metric name ...");
        assertFalse(monitoringInterface.getMetric("wldt.internal.unknown").isPresent());
    }

    /**
     * getAllMetrics() returns all pushed metrics from enabled components.
     */
    @Test
    @Order(11)
    public void testGetAllMetricsReturnsRegisteredMetrics() {
        logger.info("Testing getAllMetrics() returns correct number of registered metrics ...");
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        monitoringInterface.notifyMetric(
                new WldtGauge(NS, "queue.depth", WldtMetricComponent.EVENT_BUS, 2.0));

        assertEquals(2, monitoringInterface.getAllMetrics().size());
    }

    /**
     * isMetricRegistered() returns true after a push and false before.
     */
    @Test
    @Order(12)
    public void testIsMetricRegisteredLifecycle() {
        logger.info("Testing isMetricRegistered() lifecycle ...");
        assertFalse(monitoringInterface.isMetricRegistered(NS + ".events"));
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 1L));
        assertTrue(monitoringInterface.isMetricRegistered(NS + ".events"));
    }

    /**
     * registerMetric() pre-populates the registry; next push computes delta
     * against the pre-registered value.
     */
    @Test
    @Order(13)
    public void testExplicitRegisterMetricInfluencesDelta() {
        logger.info("Testing explicit registerMetric() influences delta on next push ...");
        monitoringInterface.registerMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 10L));
        monitoringInterface.notifyMetric(
                new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 13L));

        WldtCounter dispatched = (WldtCounter) handler.dtModelMetrics.get(0);
        assertEquals(Long.valueOf(3L), dispatched.getDelta());
    }

    // -------------------------------------------------------------------------
    // Concurrent push safety
    // -------------------------------------------------------------------------

    /**
     * Multiple concurrent pushes from different threads must all reach the handler
     * and be counted correctly. Uses CountDownLatch as synchronization barrier,
     * consistent with the WLDT test framework pattern.
     */
    @Test
    @Order(14)
    public void testConcurrentPushesAreHandledSafely() throws InterruptedException {
        logger.info("Testing concurrent pushes from multiple threads are handled safely ...");

        int threadCount = 10;
        CountDownLatch allPushed = new CountDownLatch(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            final long value = i + 1;
            new Thread(() -> {
                try {
                    startSignal.await();
                    monitoringInterface.notifyMetric(
                            new WldtCounter(NS, "concurrent.counter",
                                    WldtMetricComponent.DT_MODEL, value));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    allPushed.countDown();
                }
            }).start();
        }

        startSignal.countDown();
        boolean completed = allPushed.await(5000, TimeUnit.MILLISECONDS);

        assertTrue(completed, "All threads must complete within 5 seconds");
        assertEquals(threadCount, handler.dtModelMetrics.size(),
                "All " + threadCount + " metrics must reach the handler");
    }

    // -------------------------------------------------------------------------
    // Guard validation
    // -------------------------------------------------------------------------

    /**
     * Null configuration must throw IllegalArgumentException at construction.
     */
    @Test
    @Order(15)
    public void testNullConfigurationThrows() {
        logger.info("Testing MonitoringInterface constructor rejects null configuration ...");
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringInterface(null, handler, new NoOpLogger()));
    }

    /**
     * Null handler must throw IllegalArgumentException at construction.
     */
    @Test
    @Order(16)
    public void testNullHandlerThrows() {
        logger.info("Testing MonitoringInterface constructor rejects null handler ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringInterface(config, null, new NoOpLogger()));
    }

    /**
     * Null logger must throw IllegalArgumentException at construction.
     */
    @Test
    @Order(17)
    public void testNullLoggerThrows() {
        logger.info("Testing MonitoringInterface constructor rejects null logger ...");
        MonitoringConfiguration config = new MonitoringConfiguration.Builder().build();
        assertThrows(IllegalArgumentException.class,
                () -> new MonitoringInterface(config, handler, null));
    }

    /**
     * notifyMetric() with null must be silently guarded — no exception propagated.
     */
    @Test
    @Order(18)
    public void testNotifyNullMetricDoesNotThrow() {
        logger.info("Testing notifyMetric(null) does not propagate exception ...");
        assertDoesNotThrow(() -> monitoringInterface.notifyMetric(null));
        assertTrue(handler.dtModelMetrics.isEmpty());
    }

    /**
     * trackCustomMetric() with a non-CUSTOM component must throw
     * IllegalArgumentException.
     */
    @Test
    @Order(19)
    public void testTrackCustomMetricWrongComponentThrows() {
        logger.info("Testing trackCustomMetric() rejects non-CUSTOM component ...");
        assertThrows(IllegalArgumentException.class,
                () -> monitoringInterface.trackCustomMetric(
                        new WldtCounter(NS, "events", WldtMetricComponent.DT_MODEL, 1L)));
    }

    /**
     * trackCustomMetric() with null must throw IllegalArgumentException.
     */
    @Test
    @Order(20)
    public void testTrackCustomMetricNullThrows() {
        logger.info("Testing trackCustomMetric(null) throws IllegalArgumentException ...");
        assertThrows(IllegalArgumentException.class,
                () -> monitoringInterface.trackCustomMetric(null));
    }

    // =========================================================================
    // Test doubles
    // =========================================================================

    /**
     * Concrete WldtMonitoringHandler that captures received metrics in
     * per-component lists. Used by all tests to assert on dispatched metrics.
     */
    static class TestMonitoringHandler extends WldtMonitoringHandler {

        final List<WldtMetric> dtModelMetrics        = new ArrayList<WldtMetric>();
        final List<WldtMetric> eventBusMetrics        = new ArrayList<WldtMetric>();
        final List<WldtMetric> physicalAdapterMetrics = new ArrayList<WldtMetric>();
        final List<WldtMetric> digitalAdapterMetrics  = new ArrayList<WldtMetric>();
        final List<WldtMetric> augmentationMetrics    = new ArrayList<WldtMetric>();
        final List<WldtMetric> storageMetrics         = new ArrayList<WldtMetric>();
        final List<WldtMetric> customMetrics          = new ArrayList<WldtMetric>();

        @Override
        public synchronized void onDigitalTwinModelMetric(WldtMetric metric) {
            dtModelMetrics.add(metric);
        }

        @Override
        public synchronized void onEventBusMetric(WldtMetric metric) {
            eventBusMetrics.add(metric);
        }

        @Override
        public synchronized void onPhysicalAdapterMetric(WldtMetric metric) {
            physicalAdapterMetrics.add(metric);
        }

        @Override
        public synchronized void onDigitalAdapterMetric(WldtMetric metric) {
            digitalAdapterMetrics.add(metric);
        }

        @Override
        public synchronized void onAugmentationMetric(WldtMetric metric) {
            augmentationMetrics.add(metric);
        }

        @Override
        public synchronized void onStorageMetric(WldtMetric metric) {
            storageMetrics.add(metric);
        }

        @Override
        public synchronized void onCustomMetric(WldtMetric metric) {
            customMetrics.add(metric);
        }
    }

    /**
     * Minimal no-op implementation of {@link WldtLogger}.
     * Satisfies the MonitoringInterface constructor without any real logging output.
     * Used only in tests where a real logger instance is not needed.
     */
    static class NoOpLogger implements WldtLogger {
        public String getName()                                            { return "noop"; }
        public void trace(String msg)                                      {}
        public void trace(String f, Object a)                             {}
        public void trace(String f, Object a, Object b)                   {}
        public void trace(String f, Object... args)                       {}
        public void trace(String msg, Throwable t)                        {}
        public boolean isTraceEnabled()                                    { return false; }
        public void debug(String msg)                                      {}
        public void debug(String f, Object a)                             {}
        public void debug(String f, Object a, Object b)                   {}
        public void debug(String f, Object... args)                       {}
        public void debug(String msg, Throwable t)                        {}
        public boolean isDebugEnabled()                                    { return false; }
        public void info(String msg)                                       {}
        public void info(String f, Object a)                              {}
        public void info(String f, Object a, Object b)                    {}
        public void info(String f, Object... args)                        {}
        public void info(String msg, Throwable t)                         {}
        public boolean isInfoEnabled()                                     { return false; }
        public void warn(String msg)                                       {}
        public void warn(String f, Object a)                              {}
        public void warn(String f, Object a, Object b)                    {}
        public void warn(String f, Object... args)                        {}
        public void warn(String msg, Throwable t)                         {}
        public boolean isWarnEnabled()                                     { return false; }
        public void error(String msg)                                      {}
        public void error(String f, Object a)                             {}
        public void error(String f, Object a, Object b)                   {}
        public void error(String f, Object... args)                       {}
        public void error(String msg, Throwable t)                        {}
        public boolean isErrorEnabled()                                    { return false; }
    }
}
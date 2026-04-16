package it.wldt.monitoring.prometheus;

import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.MonitoringInterfaceConfiguration;
import it.wldt.monitoring.metrics.*;

/**
 * Runnable demonstration of {@link PrometheusMonitoringInterfaceHandler}.
 *
 * <p>Select the test mode by commenting/uncommenting the variables at the top of
 * {@link #main}. Metrics are injected directly through {@link MonitoringInterface} —
 * no full {@code DigitalTwin} lifecycle is needed for this demo.</p>
 */
public class PrometheusMonitoringExample {

    public static void main(String[] args) throws Exception {

        // ── Mode selection ────────────────────────────────────────────────────
        // Uncomment exactly one RUN_* block.

        // Option 1: HTTP scrape — Prometheus polls http://localhost:<HTTP_PORT>/metrics
        final boolean RUN_HTTP_SERVER  = false;

        // Option 2: Push Gateway — metrics pushed every PUSH_INTERVAL_MS milliseconds
        final boolean RUN_PUSH_GATEWAY = true;

        // Option 3: Push Gateway with HTTP Basic Auth
        final boolean RUN_PUSH_GATEWAY_AUTH = false;
        // ─────────────────────────────────────────────────────────────────────

        // ── HTTP server settings ──────────────────────────────────────────────
        final int    HTTP_PORT         = 9090;
        // ── Push Gateway settings ─────────────────────────────────────────────
        final String PG_ADDRESS        = "192.168.0.115:9091"; // host:port, no scheme
        final String PG_JOB            = "wldt-demo";
        final long   PUSH_INTERVAL_MS  = 5_000L;
        // ── Push Gateway Basic Auth (only used when RUN_PUSH_GATEWAY_AUTH=true) ─
        final String PG_USERNAME       = "admin";
        final String PG_PASSWORD       = "secret";
        // ─────────────────────────────────────────────────────────────────────

        if (RUN_HTTP_SERVER) {
            runHttpServer(HTTP_PORT);
        } else if (RUN_PUSH_GATEWAY_AUTH) {
            runPushGateway(PG_ADDRESS, PG_JOB, PUSH_INTERVAL_MS, PG_USERNAME, PG_PASSWORD);
        } else if (RUN_PUSH_GATEWAY) {
            runPushGateway(PG_ADDRESS, PG_JOB, PUSH_INTERVAL_MS, null, null);
        } else {
            System.err.println("No mode selected — set exactly one RUN_* variable to true.");
        }
    }

    // -------------------------------------------------------------------------
    // HTTP server mode
    // -------------------------------------------------------------------------

    private static void runHttpServer(int port) throws Exception {
        System.out.println("=== Prometheus HTTP Scrape Mode ===");
        System.out.println("Metrics will be available at: http://localhost:" + port + "/metrics");
        System.out.println("Press ENTER to stop.");

        PrometheusHandlerConfiguration config = new PrometheusHandlerConfiguration.Builder()
                .withHttpServer()
                .withHttpPort(port)
                .withMetricPrefix("wldt")
                .build();

        PrometheusMonitoringInterfaceHandler handler =
                new PrometheusMonitoringInterfaceHandler(config);

        MonitoringInterface mi = buildMonitoringInterface(handler);
        handler.start();

        // Push an initial batch of metrics
        pushDemoMetrics(mi);
        System.out.println("Initial metrics pushed. Curl http://localhost:" + port + "/metrics to inspect.");

        // Simulate ongoing updates
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int iteration = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(3000);
                        iteration++;
                        pushIterationMetrics(mi, iteration);
                        System.out.println("Metrics updated (iteration " + iteration + ")");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();

        // Block until ENTER
        System.in.read();

        handler.stop();
        System.out.println("Handler stopped.");
    }

    // -------------------------------------------------------------------------
    // Push Gateway mode
    // -------------------------------------------------------------------------

    private static void runPushGateway(String address, String job, long pushIntervalMs,
                                       String username, String password) throws Exception {
        System.out.println("=== Prometheus Push Gateway Mode ===");
        System.out.println("Pushing to: " + address + "  job=" + job);
        if (username != null) System.out.println("Basic auth: " + username);
        System.out.println("Push interval: " + pushIntervalMs + " ms   |   Demo runs for 30 s");

        PrometheusHandlerConfiguration.Builder cfgBuilder =
                new PrometheusHandlerConfiguration.Builder()
                        .withPushGateway(address)
                        .withJobName(job)
                        .withPushIntervalMs(pushIntervalMs)
                        .withMetricPrefix("wldt");

        if (username != null) cfgBuilder.withPushGatewayAuth(username, password);

        PrometheusMonitoringInterfaceHandler handler =
                new PrometheusMonitoringInterfaceHandler(cfgBuilder.build());

        MonitoringInterface mi = buildMonitoringInterface(handler);
        handler.start();

        // Push some metrics, then simulate ongoing updates for 30 s
        pushDemoMetrics(mi);
        for (int i = 1; i <= 6; i++) {
            Thread.sleep(5_000);
            pushIterationMetrics(mi, i);
            System.out.println("Metrics updated (iteration " + i + ")");
        }

        handler.stop();
        System.out.println("Handler stopped.");
    }

    // -------------------------------------------------------------------------
    // MonitoringInterface wiring
    // -------------------------------------------------------------------------

    private static MonitoringInterface buildMonitoringInterface(
            PrometheusMonitoringInterfaceHandler handler) {

        MonitoringInterfaceConfiguration miConfig =
                new MonitoringInterfaceConfiguration.Builder()
                        .withAllMonitoring()
                        .build();

        MonitoringInterface mi = new MonitoringInterface();
        mi.setConfiguration(miConfig);
        mi.setHandler(handler);
        return mi;
    }

    // -------------------------------------------------------------------------
    // Demo metric pushes
    // -------------------------------------------------------------------------

    /**
     * Pushes one sample of each metric type to demonstrate registration and
     * the first update for all five WLDT metric types.
     */
    private static void pushDemoMetrics(MonitoringInterface mi) {
        // WldtCounter — DT_MODEL component
        mi.notifyMetric(new WldtCounter(
                "demo.dt_model", "events_processed",
                WldtMetricComponent.DT_MODEL, 1L));

        // WldtUpDownCounter — DIGITAL_ADAPTER component
        mi.notifyMetric(new WldtUpDownCounter(
                "demo.digital_adapter", "active_connections",
                WldtMetricComponent.DIGITAL_ADAPTER, 3L));

        // WldtGauge — PHYSICAL_ADAPTER component
        mi.notifyMetric(new WldtGauge(
                "demo.physical_adapter", "sensor_temperature_celsius",
                WldtMetricComponent.PHYSICAL_ADAPTER, 22.5));

        // WldtTimer — DT_MODEL component
        mi.notifyMetric(new WldtTimer(
                "demo.dt_model", "property_variation_processing_latency",
                WldtMetricComponent.DT_MODEL, 45L));

        // WldtHistogram — PHYSICAL_ADAPTER component
        mi.notifyMetric(new WldtHistogram(
                "demo.physical_adapter", "payload_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER,
                10L, 4_800.0, 120.0, 980.0));

        // Custom metric bypassing flag gating
        mi.notifyMetric(new WldtCounter(
                "demo.custom", "custom_business_events",
                WldtMetricComponent.CUSTOM, 1L));
    }

    /**
     * Pushes subsequent updates to simulate live DT activity.
     * Counter advances, gauge fluctuates, timer records new latencies.
     */
    private static void pushIterationMetrics(MonitoringInterface mi, int iteration) {
        // Counter — increment by 5 per iteration via absolute value
        mi.notifyMetric(new WldtCounter(
                "demo.dt_model", "events_processed",
                WldtMetricComponent.DT_MODEL, 1L + (iteration * 5L)));

        // UpDownCounter — oscillate connections count
        long connections = 3L + (iteration % 3) - 1;
        mi.notifyMetric(new WldtUpDownCounter(
                "demo.digital_adapter", "active_connections",
                WldtMetricComponent.DIGITAL_ADAPTER, connections));

        // Gauge — simulate temperature variation
        double temp = 22.5 + (Math.sin(iteration * 0.5) * 3.0);
        mi.notifyMetric(new WldtGauge(
                "demo.physical_adapter", "sensor_temperature_celsius",
                WldtMetricComponent.PHYSICAL_ADAPTER, temp));

        // Timer — latency from 30 ms to 120 ms
        long latency = 30L + (long) (Math.abs(Math.sin(iteration * 0.8)) * 90.0);
        mi.notifyMetric(new WldtTimer(
                "demo.dt_model", "property_variation_processing_latency",
                WldtMetricComponent.DT_MODEL, latency));

        // Histogram — varying payload size windows
        long count = 5L + iteration;
        double sum  = count * (300.0 + iteration * 20.0);
        mi.notifyMetric(new WldtHistogram(
                "demo.physical_adapter", "payload_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER,
                count, sum, 100.0 + iteration, 1000.0 + iteration));

        // Custom counter
        mi.notifyMetric(new WldtCounter(
                "demo.custom", "custom_business_events",
                WldtMetricComponent.CUSTOM, (long) iteration));
    }
}

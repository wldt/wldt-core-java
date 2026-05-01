package it.wldt.monitoring.prometheus;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.monitoring.MonitoringInterfaceConfiguration;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoDigitalTwinModel;
import it.wldt.utils.SharedTestMetrics;

/**
 * Demonstrates {@link PrometheusMonitoringInterfaceHandler} wired to a real
 * {@link DigitalTwin} running the standard demo adapter stack (same setup as
 * {@code DigitalTwinProcessMetricsTests}). Metrics are generated automatically
 * by the DT as the physical adapter publishes property updates, events, and
 * relationship changes — no manual metric injection needed.
 *
 * <p>Select the exposition mode and target by editing the variables at the top
 * of {@link #main}.</p>
 */
public class PrometheusMonitoringInterfaceExample {

    private static final String DT_ID = "dt-example-1";

    public static void main(String[] args) throws Exception {

        // ── Mode selection ────────────────────────────────────────────────────
        // Uncomment exactly one RUN_* block.

        // Option 1: HTTP scrape — Prometheus polls http://localhost:<HTTP_PORT>/metrics
        final boolean RUN_HTTP_SERVER       = false;

        // Option 2: Push Gateway — metrics pushed every PUSH_INTERVAL_MS milliseconds
        final boolean RUN_PUSH_GATEWAY      = true;

        // Option 3: Push Gateway with HTTP Basic Auth
        final boolean RUN_PUSH_GATEWAY_AUTH = false;
        // ─────────────────────────────────────────────────────────────────────

        // ── HTTP server settings ──────────────────────────────────────────────
        final int    HTTP_PORT        = 9090;
        // ── Push Gateway settings ─────────────────────────────────────────────
        final String PG_ADDRESS       = "192.168.0.152:9091"; // host:port, no scheme
        final String PG_JOB           = "wldt-dt-example";
        final long   PUSH_INTERVAL_MS = 1_000L;
        // ── Push Gateway Basic Auth (only used when RUN_PUSH_GATEWAY_AUTH=true) ─
        final String PG_USERNAME      = "admin";
        final String PG_PASSWORD      = "secret";
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
        System.out.println("Metrics: http://localhost:" + port + "/metrics");
        System.out.println("Press ENTER to stop.");

        PrometheusHandlerConfiguration config = new PrometheusHandlerConfiguration.Builder()
                .withHttpServer()
                .withHttpPort(port)
                .withImmediatePushForComponents(
                        WldtMetricComponent.DT_MODEL,
                        WldtMetricComponent.PHYSICAL_ADAPTER,
                        WldtMetricComponent.DIGITAL_ADAPTER,
                        WldtMetricComponent.AUGMENTATION,
                        WldtMetricComponent.STORAGE)
                .build();

        PrometheusMonitoringInterfaceHandler handler =
                new PrometheusMonitoringInterfaceHandler(config);

        DemoDigitalAdapter digitalAdapter = new DemoDigitalAdapter(
                DT_ID + "-digital-adapter", new DemoDigitalAdapterConfiguration());
        DigitalTwinEngine engine = startDt(handler, digitalAdapter);

        handler.start();

        // Wait for the physical adapter to finish publishing all property updates,
        // events, and relationship changes before triggering digital actions.
        long waitMs = DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + (DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                   + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

        System.out.println("Waiting " + waitMs + " ms for PA events to complete...");
        Thread.sleep(waitMs);

        // Trigger digital actions to generate DT_MODEL and PHYSICAL_ADAPTER action metrics
        System.out.println("Invoking digital actions...");
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        Thread.sleep(2000);
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        Thread.sleep(2000);

        System.out.println("All events processed. Scrape " +
                "http://localhost:" + port + "/metrics to observe DT metrics.");

        System.in.read();

        handler.stop();
        stopDt(engine);
    }

    // -------------------------------------------------------------------------
    // Push Gateway mode
    // -------------------------------------------------------------------------

    private static void runPushGateway(String address, String job, long pushIntervalMs,
                                       String username, String password) throws Exception {

        System.out.println("=== Prometheus Push Gateway Mode ===");
        System.out.println("Pushing to: " + address + "  job=" + job);
        if (username != null) System.out.println("Basic auth: " + username);
        System.out.println("Push interval: " + pushIntervalMs + " ms");

        PrometheusHandlerConfiguration.Builder cfgBuilder =
                new PrometheusHandlerConfiguration.Builder()
                        .withPushGateway(address)
                        .withJobName(job)
                        .withPushIntervalMs(pushIntervalMs)
                        .withDtId(DT_ID)
                        .withImmediatePushForComponents(
                                WldtMetricComponent.DT_MODEL,
                                WldtMetricComponent.PHYSICAL_ADAPTER,
                                WldtMetricComponent.DIGITAL_ADAPTER,
                                WldtMetricComponent.AUGMENTATION,
                                WldtMetricComponent.STORAGE);

        if (username != null) cfgBuilder.withPushGatewayAuth(username, password);

        PrometheusMonitoringInterfaceHandler handler =
                new PrometheusMonitoringInterfaceHandler(cfgBuilder.build());

        DemoDigitalAdapter digitalAdapter = new DemoDigitalAdapter(
                DT_ID + "-digital-adapter", new DemoDigitalAdapterConfiguration());
        DigitalTwinEngine engine = startDt(handler, digitalAdapter);

        handler.start();

        long waitMs = DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + (DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                   + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

        System.out.println("Waiting " + waitMs + " ms for PA events to complete...");
        Thread.sleep(waitMs);

        System.out.println("Invoking digital actions...");
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        Thread.sleep(2000);
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        Thread.sleep(2000);

        System.out.println("All events processed. Keeping handler alive for 30 s...");
        Thread.sleep(30_000);

        handler.stop();
        stopDt(engine);
    }

    // -------------------------------------------------------------------------
    // DT lifecycle helpers
    // -------------------------------------------------------------------------

    /**
     * Builds and starts the Digital Twin with the demo adapter stack, wiring
     * the given Prometheus handler to the DT's MonitoringInterface before start.
     * The {@code digitalAdapter} instance is created externally so the caller
     * can invoke actions on it after the DT is running.
     */
    private static DigitalTwinEngine startDt(PrometheusMonitoringInterfaceHandler handler,
                                             DemoDigitalAdapter digitalAdapter)
            throws Exception {

        DigitalTwinEngine engine = new DigitalTwinEngine();

        DigitalTwin dt = new DigitalTwin(DT_ID, new DemoDigitalTwinModel());

        dt.addPhysicalAdapter(
                new DemoPhysicalAdapter(
                        DT_ID + "-physical-adapter",
                        new DemoPhysicalAdapterConfiguration(),
                        true,   // isTelemetryOn
                        true)); // isRelationshipOn

        dt.addDigitalAdapter(digitalAdapter);

        dt.getMonitoringInterface().setConfiguration(
                new MonitoringInterfaceConfiguration.Builder()
                        .withDtModelMonitoring()
                        .withPhysicalAdapterMonitoring()
                        .withDigitalAdapterMonitoring()
                        .build());

        dt.getMonitoringInterface().setHandler(handler);

        // Required by DemoDigitalTwinModel — it records events into SharedTestMetrics
        SharedTestMetrics.getInstance().registerDigitalTwin(DT_ID);

        engine.addDigitalTwin(dt);
        engine.startDigitalTwin(DT_ID);

        System.out.println("Digital Twin '" + DT_ID + "' started.");
        return engine;
    }

    private static void stopDt(DigitalTwinEngine engine) throws Exception {
        engine.stopDigitalTwin(DT_ID);
        engine.removeDigitalTwin(DT_ID);
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(DT_ID);
        System.out.println("Digital Twin '" + DT_ID + "' stopped.");
    }
}

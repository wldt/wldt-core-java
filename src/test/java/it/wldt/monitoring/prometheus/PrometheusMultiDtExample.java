package it.wldt.monitoring.prometheus;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.monitoring.MonitoringInterfaceConfiguration;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoDigitalTwinModel;
import it.wldt.utils.SharedTestMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs up to N Digital Twins in parallel, each wired to its own
 * {@link PrometheusMonitoringInterfaceHandler} pushing to a PushGateway.
 *
 * <p>Every DT gets a unique job name ({@code wldt-dt-example-<i>}) so that
 * {@code stop()} can delete only that DT's metrics from the PushGateway
 * without affecting any other running instance.</p>
 *
 * <p>Usage: pass the desired DT count as the first CLI argument, or leave it
 * blank to use {@link #DEFAULT_DT_COUNT}.</p>
 */
public class PrometheusMultiDtExample {

    // ── Configuration ─────────────────────────────────────────────────────────
    static final int     DEFAULT_DT_COUNT  = 10;
    static final String  PG_ADDRESS        = "192.168.0.115:9091"; // host:port, no scheme
    static final long    PUSH_INTERVAL_MS  = 1_000L;
    static final boolean USE_AUTH          = false;
    static final String  PG_USERNAME       = "admin";   // ignored when USE_AUTH = false
    static final String  PG_PASSWORD       = "secret";  // ignored when USE_AUTH = false
    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {

        int dtCount = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_DT_COUNT;
        System.out.println("=== Prometheus Multi-DT Example — " + dtCount + " Digital Twin(s) in parallel ===");

        List<DtRunner> runners = new ArrayList<DtRunner>(dtCount);
        for (int i = 1; i <= dtCount; i++)
            runners.add(new DtRunner(
                    "dt-example-" + i,
                    PG_ADDRESS,
                    PUSH_INTERVAL_MS,
                    USE_AUTH ? PG_USERNAME : null,
                    USE_AUTH ? PG_PASSWORD : null));

        ExecutorService pool = Executors.newFixedThreadPool(dtCount);
        List<Future<?>> futures = new ArrayList<Future<?>>(dtCount);
        for (DtRunner r : runners)
            futures.add(pool.submit(r));

        // Wait for every DT to complete its lifecycle
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                System.err.println("[Main] A DT runner failed: " + e.getCause().getMessage());
            }
        }

        pool.shutdown();
        System.out.println("=== All " + dtCount + " Digital Twin(s) completed ===");
    }

    // -------------------------------------------------------------------------
    // Per-DT runner — one instance per parallel DT
    // -------------------------------------------------------------------------

    static final class DtRunner implements Runnable {

        private final String dtId;
        private final String pgAddress;
        private final long   pushIntervalMs;
        private final String pgUsername;  // nullable — null means no auth
        private final String pgPassword;  // nullable

        DtRunner(String dtId, String pgAddress, long pushIntervalMs,
                 String pgUsername, String pgPassword) {
            this.dtId           = dtId;
            this.pgAddress      = pgAddress;
            this.pushIntervalMs = pushIntervalMs;
            this.pgUsername     = pgUsername;
            this.pgPassword     = pgPassword;
        }

        @Override
        public void run() {
            PrometheusMonitoringInterfaceHandler handler = null;
            DigitalTwinEngine engine = null;
            DemoDigitalAdapter digitalAdapter = null;

            try {
                // Each DT owns a unique job so stop() deletes only its own metrics
                PrometheusHandlerConfiguration.Builder cfgBuilder =
                        new PrometheusHandlerConfiguration.Builder()
                                .withPushGateway(pgAddress)
                                .withJobName("wldt-" + dtId)
                                .withPushIntervalMs(pushIntervalMs)
                                .withDtId(dtId);

                if (pgUsername != null)
                    cfgBuilder.withPushGatewayAuth(pgUsername, pgPassword);

                handler = new PrometheusMonitoringInterfaceHandler(cfgBuilder.build());

                digitalAdapter = new DemoDigitalAdapter(
                        dtId + "-digital-adapter", new DemoDigitalAdapterConfiguration());

                engine = buildAndStartDt(handler, digitalAdapter);
                handler.start();

                System.out.println("[" + dtId + "] Started — job=wldt-" + dtId);

                // Wait for all PA property updates + event notifications to be processed
                long waitMs = DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                        + (DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                           + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                        * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

                Thread.sleep(waitMs);

                // Trigger digital actions to generate action metrics
                digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY,  "ON");
                Thread.sleep(2_000);
                digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
                Thread.sleep(2_000);

                System.out.println("[" + dtId + "] Lifecycle complete.");

            } catch (Exception e) {
                System.err.println("[" + dtId + "] Error during run: " + e.getMessage());
                throw new RuntimeException(e);

            } finally {
                // Always stop cleanly — handler.stop() also deletes PushGateway metrics
                if (handler != null)
                    handler.stop();

                if (engine != null) {
                    try {
                        engine.stopDigitalTwin(dtId);
                        engine.removeDigitalTwin(dtId);
                    } catch (Exception e) {
                        System.err.println("[" + dtId + "] Stop error: " + e.getMessage());
                    }
                }

                SharedTestMetrics.getInstance().unRegisterDigitalTwin(dtId);
                System.out.println("[" + dtId + "] Stopped and PushGateway metrics deleted.");
            }
        }

        private DigitalTwinEngine buildAndStartDt(PrometheusMonitoringInterfaceHandler handler,
                                                   DemoDigitalAdapter digitalAdapter) throws Exception {
            DigitalTwinEngine engine = new DigitalTwinEngine();

            DigitalTwin dt = new DigitalTwin(dtId, new DemoDigitalTwinModel());

            dt.addPhysicalAdapter(new DemoPhysicalAdapter(
                    dtId + "-physical-adapter",
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

            SharedTestMetrics.getInstance().registerDigitalTwin(dtId);
            engine.addDigitalTwin(dt);
            engine.startDigitalTwin(dtId);

            return engine;
        }
    }
}

package it.wldt.monitoring.prometheus;

import it.wldt.augmentation.handler.AugmentationFunctionHandler;
import it.wldt.augmentation.handler.DefaultAugmentationFunctionHandler;
import it.wldt.augmentation.stateless.function.RandomNumberAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestErrorStatefulAugmentationFunction;
import it.wldt.storage.augmentation.function.StorageTestStatefulAugmentationFunction;
import it.wldt.augmentation.stateless.generic.GenericResultAugmentationDigitalTwinModel;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.monitoring.MonitoringInterfaceConfiguration;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.storage.DefaultWldtStorage;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryRequestType;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;
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

    private static AugmentationFunctionHandler augHandler;
    private static GenericResultAugmentationDigitalTwinModel dtModel;

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

        // Wait for DT model to subscribe to augmentation events, then fire one
        // unregister+re-register cycle to populate af_unregistered_* and af_registered_* metrics.
        Thread.sleep(500);
        System.out.println("Unregistering and re-registering RandomNumber function to populate af_unregistered/af_registered metrics...");
        augHandler.unRegisterAugmentationFunction(RandomNumberAugmentationFunction.FUNCTION_ID);
        augHandler.registerAugmentationFunction(new RandomNumberAugmentationFunction());
        System.out.println("Done.");

        // Small delay before starting stateful AFs.
        Thread.sleep(500);

        // Start ALL stateful AFs together so they run while PA data is flowing in.
        startAllStatefulFunctions();

        // Wait for the physical adapter to finish publishing all property updates,
        // events, and relationship changes before triggering digital actions.
        long waitMs = DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + (DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                   + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

        System.out.println("Waiting " + waitMs + " ms for PA events to complete...");
        Thread.sleep(waitMs);

        // Stop ALL stateful AFs together.
        stopAllStatefulFunctions();

        // Trigger digital actions to generate DT_MODEL and PHYSICAL_ADAPTER action metrics
        System.out.println("Invoking digital actions...");
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        Thread.sleep(2000);
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        Thread.sleep(2000);

        System.out.println("Running storage query to populate query metrics...");
        runStorageQuery(digitalAdapter);
        Thread.sleep(1000);

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

        System.out.println("Unregistering and re-registering RandomNumber function to populate af_unregistered/af_registered metrics...");
        augHandler.unRegisterAugmentationFunction(RandomNumberAugmentationFunction.FUNCTION_ID);
        augHandler.registerAugmentationFunction(new RandomNumberAugmentationFunction());
        System.out.println("Done.");

        // Start ALL stateful AFs together so they run while PA data is flowing in.
        startAllStatefulFunctions();

        long waitMs = DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS
                + (DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES
                   + DemoPhysicalAdapter.DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES)
                * DemoPhysicalAdapter.DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

        System.out.println("Waiting " + waitMs + " ms for PA events to complete...");
        Thread.sleep(waitMs);

        // Stop ALL stateful AFs together.
        stopAllStatefulFunctions();

        System.out.println("Invoking digital actions...");
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        Thread.sleep(2000);
        digitalAdapter.invokeAction(DemoPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        Thread.sleep(2000);

        System.out.println("Running storage query to populate query metrics...");
        runStorageQuery(digitalAdapter);
        Thread.sleep(1000);

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
     * All augmentation functions are registered before {@code engine.startDigitalTwin()} so
     * the handler holds a full list when the DT model starts, populating {@code af_list_registered_*}.
     * After start, callers perform an unregister+re-register cycle to populate
     * {@code af_unregistered_*} and {@code af_registered_*} while the model is subscribed.
     */
    private static DigitalTwinEngine startDt(PrometheusMonitoringInterfaceHandler handler,
                                             DemoDigitalAdapter digitalAdapter)
            throws Exception {

        DigitalTwinEngine engine = new DigitalTwinEngine();

        dtModel = new GenericResultAugmentationDigitalTwinModel();
        DigitalTwin dt = new DigitalTwin(DT_ID, dtModel);

        dt.addPhysicalAdapter(
                new DemoPhysicalAdapter(
                        DT_ID + "-physical-adapter",
                        new DemoPhysicalAdapterConfiguration(),
                        true,   // isTelemetryOn
                        true)); // isRelationshipOn

        dt.addDigitalAdapter(digitalAdapter);

        dt.getStorageManager().putStorage(new DefaultWldtStorage(DT_ID + "-storage", true));

        augHandler = new DefaultAugmentationFunctionHandler(DT_ID + "-aug-handler");
        dt.getAugmentationManager().addAugmentationFunctionHandler(augHandler);

        // Register all functions before start so the handler holds a full list when
        // DigitalTwinModel.startHandlingAugmentationFunction() runs and fires af_list_registered_*.
        augHandler.registerAugmentationFunction(new RandomNumberAugmentationFunction());
        augHandler.registerAugmentationFunction(new StorageTestStatefulAugmentationFunction());
        augHandler.registerAugmentationFunction(new StorageTestErrorStatefulAugmentationFunction());
        augHandler.registerAugmentationFunction(new PrometheusTestStatefulAugmentationFunction());

        dt.getMonitoringInterface().setConfiguration(
                new MonitoringInterfaceConfiguration.Builder()
                        .withDtModelMonitoring()
                        .withPhysicalAdapterMonitoring()
                        .withDigitalAdapterMonitoring()
                        .withAugmentationMonitoring()
                        .withStorageMonitoring()
                        .build());

        dt.getMonitoringInterface().setHandler(handler);

        // Required by GenericResultAugmentationDigitalTwinModel — it records events into SharedTestMetrics
        SharedTestMetrics.getInstance().registerDigitalTwin(DT_ID);

        engine.addDigitalTwin(dt);
        engine.startDigitalTwin(DT_ID);

        System.out.println("Digital Twin '" + DT_ID + "' started.");
        return engine;
    }

    /**
     * Starts all three stateful AFs simultaneously via the DT model so that af_stateful_start_*
     * metrics fire for all of them while PA data is flowing in.
     */
    private static void startAllStatefulFunctions() throws Exception {
        if (dtModel == null) return;
        System.out.println("Starting all stateful AFs simultaneously via DT model...");
        dtModel.triggerStartAugmentationFunction(StorageTestStatefulAugmentationFunction.FUNCTION_ID);
        dtModel.triggerStartAugmentationFunction(StorageTestErrorStatefulAugmentationFunction.FUNCTION_ID);
        dtModel.triggerStartAugmentationFunction(PrometheusTestStatefulAugmentationFunction.FUNCTION_ID);
        System.out.println("All stateful AFs started.");
    }

    /**
     * Stops all three stateful AFs simultaneously via the DT model so that af_stateful_stop_*
     * metrics fire for all of them.
     */
    private static void stopAllStatefulFunctions() throws Exception {
        if (dtModel == null) return;
        System.out.println("Stopping all stateful AFs simultaneously via DT model...");
        dtModel.triggerStopAugmentationFunction(StorageTestStatefulAugmentationFunction.FUNCTION_ID);
        dtModel.triggerStopAugmentationFunction(StorageTestErrorStatefulAugmentationFunction.FUNCTION_ID);
        dtModel.triggerStopAugmentationFunction(PrometheusTestStatefulAugmentationFunction.FUNCTION_ID);
        System.out.println("All stateful AFs stopped.");
    }

    private static void runStorageQuery(DemoDigitalAdapter digitalAdapter) {
        try {
            QueryRequest query = new QueryRequest();
            query.setResourceType(QueryResourceType.DIGITAL_TWIN_STATE);
            query.setRequestType(QueryRequestType.LAST_VALUE);
            QueryResult<?> result = digitalAdapter.testSyncQuery(query);
            if (result != null && result.isSuccessful())
                System.out.println("Storage query OK — results: " + result.getTotalResults());
            else
                System.out.println("Storage query failed: " + (result != null ? result.getErrorMessage() : "null result"));
        } catch (Exception e) {
            System.err.println("Storage query exception: " + e.getMessage());
        }
    }

    private static void stopDt(DigitalTwinEngine engine) throws Exception {
        engine.stopDigitalTwin(DT_ID);
        engine.removeDigitalTwin(DT_ID);
        SharedTestMetrics.getInstance().unRegisterDigitalTwin(DT_ID);
        System.out.println("Digital Twin '" + DT_ID + "' stopped.");
    }
}

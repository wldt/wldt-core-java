package it.wldt.monitoring.prometheus;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.exporter.pushgateway.PushGateway;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterfaceHandler;
import it.wldt.monitoring.metrics.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link MonitoringInterfaceHandler} implementation that maps WLDT metrics to Prometheus
 * metric types and exposes them either via an HTTP scrape endpoint or by pushing to a
 * Prometheus PushGateway.
 *
 * <p>All Prometheus internals ({@code PrometheusRegistry}, {@code Counter}, {@code Gauge},
 * {@code HTTPServer}, {@code PushGateway}) are fully encapsulated here. The WLDT core never
 * references any Prometheus type — integration is driven exclusively through the two callbacks
 * {@link #onMetricRegistered} and {@link #onMetricUpdated}.</p>
 *
 * <h3>WLDT → Prometheus mapping</h3>
 * <table>
 *   <tr><th>WLDT type</th><th>Prometheus type</th><th>Update strategy</th></tr>
 *   <tr><td>{@link WldtCounter}</td><td>{@code Counter}</td><td>{@code inc(delta)} when delta is available</td></tr>
 *   <tr><td>{@link WldtUpDownCounter}</td><td>{@code Gauge}</td><td>{@code set(value)}</td></tr>
 *   <tr><td>{@link WldtGauge}</td><td>{@code Gauge}</td><td>{@code set(value)}</td></tr>
 *   <tr><td>{@link WldtTimer}</td><td>6× {@code Gauge}</td><td>one gauge per stat (duration, min, max, mean, total, count)</td></tr>
 *   <tr><td>{@link WldtHistogram}</td><td>6× {@code Gauge}</td><td>one gauge per stat (window and cumulative)</td></tr>
 * </table>
 *
 * <p>Every Prometheus metric carries a {@code component} label whose value is the
 * {@link WldtMetricComponent} enum name (e.g. {@code "DT_MODEL"}, {@code "PHYSICAL_ADAPTER"}).</p>
 *
 * <h3>Lifecycle</h3>
 * <pre>{@code
 * PrometheusMonitoringInterfaceHandler handler =
 *     new PrometheusMonitoringInterfaceHandler(config);
 * digitalTwin.getMonitoringInterface().setHandler(handler);
 * handler.start();          // open HTTP server or start push scheduler
 * // ... DT runs ...
 * handler.stop();           // close HTTP server or stop push scheduler
 * }</pre>
 *
 * <p><strong>Thread safety:</strong> metric registration is serialized via {@code synchronized}
 * on the internal map. Update calls (hot path) use lock-free {@code ConcurrentHashMap.get()}.
 * All Prometheus metric mutations are thread-safe within the Prometheus 1.x client.</p>
 */
public class PrometheusMonitoringInterfaceHandler extends MonitoringInterfaceHandler {

    private static final String[] TIMER_SUFFIXES     = {"duration_ms", "min_ms", "max_ms", "mean_ms", "total_ms", "count"};
    private static final String[] HISTOGRAM_SUFFIXES = {"window_count", "window_sum", "window_min", "window_max", "total_count", "total_sum"};

    private final PrometheusHandlerConfiguration config;
    private final PrometheusRegistry             registry;

    /**
     * Stores Prometheus metric objects keyed by sanitized Prometheus metric name.
     * Values are:
     * <ul>
     *   <li>{@code Counter}           — for WldtCounter</li>
     *   <li>{@code Gauge}             — for WldtUpDownCounter / WldtGauge</li>
     *   <li>{@code Map<String,Gauge>} — for WldtTimer / WldtHistogram (keyed by suffix)</li>
     * </ul>
     */
    private final ConcurrentHashMap<String, Object> prometheusMetrics;

    private volatile HTTPServer              httpServer;
    private volatile ScheduledExecutorService pushScheduler;
    private volatile PushGateway             pushGateway;

    public PrometheusMonitoringInterfaceHandler(PrometheusHandlerConfiguration config) {
        this.config           = config;
        this.registry         = new PrometheusRegistry();
        this.prometheusMetrics = new ConcurrentHashMap<String, Object>();
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Starts the configured exposition mode.
     * <ul>
     *   <li>HTTP_SERVER: opens the scrape endpoint immediately.</li>
     *   <li>PUSH_GATEWAY: starts a background thread that pushes metrics at the
     *       configured interval (first push fires immediately).</li>
     * </ul>
     *
     * @throws IOException if the HTTP server port cannot be bound, or if the initial
     *                     PushGateway connection check fails
     */
    public void start() throws IOException {
        if (config.getWorkingMode() == PrometheusHandlerConfiguration.WorkingMode.HTTP_SERVER) {
            httpServer = HTTPServer.builder()
                    .port(config.getHttpPort())
                    .registry(registry)
                    .buildAndStart();
            System.out.println("[PrometheusHandler] HTTP scrape endpoint started — " +
                    "http://localhost:" + config.getHttpPort() + "/metrics");
        } else {
            startPushGatewayScheduler();
        }
    }

    /** Shuts down the HTTP server or stops the push scheduler gracefully.
     *  In PUSH_GATEWAY mode, issues a DELETE to remove this DT's metrics before stopping. */
    public void stop() {
        deletePushGatewayMetrics();
        if (httpServer != null) {
            httpServer.close();
            httpServer = null;
            System.out.println("[PrometheusHandler] HTTP server stopped.");
        }
        if (pushScheduler != null) {
            pushScheduler.shutdown();
            try {
                if (!pushScheduler.awaitTermination(5, TimeUnit.SECONDS))
                    pushScheduler.shutdownNow();
            } catch (InterruptedException e) {
                pushScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            pushScheduler = null;
            System.out.println("[PrometheusHandler] Push Gateway scheduler stopped.");
        }
    }

    /**
     * Issues a {@code DELETE /metrics/job/<job>/dt_id/<dtId>} to the PushGateway,
     * removing all metrics published by this DT instance.
     * No-op if not in PUSH_GATEWAY mode or if {@code dtId} is not configured.
     */
    private void deletePushGatewayMetrics() {
        if (config.getWorkingMode() != PrometheusHandlerConfiguration.WorkingMode.PUSH_GATEWAY) return;
        String dtId = config.getDtId();
        // dt_id is a metric label, not a PushGateway grouping label — deletion must target the job path only.
        // If per-DT isolation is needed, give each DT a unique job name via withJobName().
        try {
            String url = "http://" + config.getPushGatewayAddress()
                    + "/metrics/job/" + java.net.URLEncoder.encode(config.getJobName(), "UTF-8");
            java.net.HttpURLConnection conn =
                    (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            conn.setRequestMethod("DELETE");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(5_000);
            int code = conn.getResponseCode();
            if (code >= 200 && code < 300)
                System.out.println("[PrometheusHandler] PushGateway metrics deleted for job="
                        + config.getJobName() + (dtId != null ? " dt_id=" + dtId : ""));
            else
                System.err.println("[PrometheusHandler] PushGateway DELETE returned HTTP " + code
                        + " for job=" + config.getJobName());
            conn.disconnect();
        } catch (Exception e) {
            System.err.println("[PrometheusHandler] Failed to delete PushGateway metrics for job="
                    + config.getJobName() + ": " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // MonitoringInterfaceHandler callbacks
    // -------------------------------------------------------------------------

    /**
     * Fires when a WLDT metric is seen for the first time. Creates and registers the
     * corresponding Prometheus metric structure. The {@code metric} parameter is an
     * <em>uninitialized empty snapshot</em> — only metadata (namespace, name, component)
     * is valid; numeric fields must not be read here.
     */
    @Override
    public void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {
        String promName = buildPromName(metric.getFullName());
        synchronized (prometheusMetrics) {
            if (!prometheusMetrics.containsKey(promName)) {
                Object obj = createPrometheusObject(metric, promName);
                if (obj != null) prometheusMetrics.put(promName, obj);
            }
        }
    }

    /**
     * Fires on every metric update. The {@code metric} parameter is an initialized
     * snapshot copy with up-to-date values and statistics.
     */
    @Override
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
        if (!metric.isInitialized()) return;

        String promName   = buildPromName(metric.getFullName());
        Object registered = prometheusMetrics.get(promName);
        if (registered == null) {
            System.err.println("[PrometheusHandler] WARN: update arrived for unregistered metric '" +
                    metric.getFullName() + "' (Prometheus name: '" + promName + "') — skipping");
            return;
        }

        //if(metric.getName().equals(CoreMonitoringUtils.LIFE_CYCLE_VALUE))
        //    System.out.println(metric.getFullName() + " is set to " + promName);

        applyUpdate(component, metric, registered);

        if (config.getImmediatePushComponents().contains(component))
            pushNow();
    }

    /**
     * Immediately pushes all metrics to the PushGateway, bypassing the scheduled interval.
     * No-op in HTTP_SERVER mode or before {@link #start()} is called.
     */
    public void pushNow() {
        if (pushGateway == null) return;
        try {
            pushGateway.push();
        } catch (Exception e) {
            System.err.println("[PrometheusHandler] Immediate push failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal — registration
    // -------------------------------------------------------------------------

    /**
     * Creates the Prometheus metric object(s) for the given WLDT metric type.
     * Returns {@code null} on failure (e.g. name collision after sanitization).
     */
    private Object createPrometheusObject(WldtMetric metric, String promName) {
        String help = "WLDT " + metric.getClass().getSimpleName() + ": " + metric.getFullName();
        try {
            if (metric instanceof WldtCounter) {
                return Counter.builder()
                        .name(promName)
                        .help(help)
                        .labelNames("component", "dt_id")
                        .register(registry);

            } else if (metric instanceof WldtUpDownCounter) {
                return Gauge.builder()
                        .name(promName)
                        .help(help)
                        .labelNames("component", "dt_id")
                        .register(registry);

            } else if (metric instanceof WldtGauge) {
                return Gauge.builder()
                        .name(promName)
                        .help(help)
                        .labelNames("component", "dt_id")
                        .register(registry);

            } else if (metric instanceof WldtTimer) {
                return buildMultiGauge(promName, help, TIMER_SUFFIXES);

            } else if (metric instanceof WldtHistogram) {
                return buildMultiGauge(promName, help, HISTOGRAM_SUFFIXES);
            }
        } catch (Exception e) {
            System.err.println("[PrometheusHandler] Failed to register '" + promName +
                    "': " + e.getMessage());
        }
        return null;
    }

    /** Builds a suffix → Gauge map and registers each gauge in the registry. */
    private Map<String, Gauge> buildMultiGauge(String baseName, String baseHelp, String[] suffixes) {
        Map<String, Gauge> gauges = new LinkedHashMap<String, Gauge>();
        for (String suffix : suffixes) {
            gauges.put(suffix, Gauge.builder()
                    .name(baseName + "_" + suffix)
                    .help(baseHelp + " [" + suffix + "]")
                    .labelNames("component", "dt_id")
                    .register(registry));
        }
        return gauges;
    }

    // -------------------------------------------------------------------------
    // Internal — updates
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void applyUpdate(WldtMetricComponent component, WldtMetric metric, Object registered) {
        String componentName = component.name();
        String dtId  = metric.getDigitalTwinId();

        if (metric instanceof WldtCounter && registered instanceof Counter) {
            WldtCounter wc = (WldtCounter) metric;
            if (wc.isDeltaAvailable() && wc.getDelta() > 0) {
                ((Counter) registered).labelValues(componentName, dtId).inc((double) wc.getDelta());
            }

        } else if (metric instanceof WldtUpDownCounter && registered instanceof Gauge) {
            ((Gauge) registered).labelValues(componentName, dtId).set((double) ((WldtUpDownCounter) metric).getValue());

        } else if (metric instanceof WldtGauge && registered instanceof Gauge) {
            ((Gauge) registered).labelValues(componentName, dtId).set(((WldtGauge) metric).getValue());

        } else if (metric instanceof WldtTimer && registered instanceof Map) {
            WldtTimer wt = (WldtTimer) metric;
            Map<String, Gauge> g = (Map<String, Gauge>) registered;
            g.get("duration_ms") .labelValues(componentName, dtId).set((double) wt.getDurationMs());
            g.get("min_ms")      .labelValues(componentName, dtId).set((double) wt.getMinDurationMs());
            g.get("max_ms")      .labelValues(componentName, dtId).set((double) wt.getMaxDurationMs());
            g.get("mean_ms")     .labelValues(componentName, dtId).set(wt.getMeanDurationMs());
            g.get("total_ms")    .labelValues(componentName, dtId).set((double) wt.getTotalDurationMs());
            g.get("count")       .labelValues(componentName, dtId).set((double) wt.getObservationCount());

        } else if (metric instanceof WldtHistogram && registered instanceof Map) {
            WldtHistogram wh = (WldtHistogram) metric;
            Map<String, Gauge> g = (Map<String, Gauge>) registered;
            g.get("window_count").labelValues(componentName, dtId).set((double) wh.getCount());
            g.get("window_sum")  .labelValues(componentName, dtId).set(wh.getSum());
            g.get("window_min")  .labelValues(componentName, dtId).set(wh.getMin());
            g.get("window_max")  .labelValues(componentName, dtId).set(wh.getMax());
            g.get("total_count") .labelValues(componentName, dtId).set((double) wh.getTotalCount());
            g.get("total_sum")   .labelValues(componentName, dtId).set(wh.getTotalSum());
        }
    }

    // -------------------------------------------------------------------------
    // Internal — Push Gateway
    // -------------------------------------------------------------------------

    private void startPushGatewayScheduler() {
        PushGateway.Builder pgBuilder = PushGateway.builder()
                .address(config.getPushGatewayAddress())
                .job(config.getJobName())
                .registry(registry);

        if (config.getPushGatewayUsername() != null) {
            pgBuilder = pgBuilder.basicAuth(
                    config.getPushGatewayUsername(),
                    config.getPushGatewayPassword());
        }

        pushGateway = pgBuilder.build();

        pushScheduler = Executors.newSingleThreadScheduledExecutor();
        pushScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    pushGateway.push();
                } catch (Exception e) {
                    System.err.println("[PrometheusHandler] PushGateway push failed: " + e.getMessage());
                }
            }
        }, 0L, config.getPushIntervalMs(), TimeUnit.MILLISECONDS);

        System.out.println("[PrometheusHandler] Push Gateway scheduler started — pushing to " +
                config.getPushGatewayAddress() + " every " + config.getPushIntervalMs() + " ms");
    }

    // -------------------------------------------------------------------------
    // Internal — name utilities
    // -------------------------------------------------------------------------

    /**
     * Builds a Prometheus-safe metric name from the WLDT metric full name.
     * Prepends the configured prefix, then replaces any character outside
     * {@code [a-zA-Z0-9_]} with {@code _}.
     */
    private String buildPromName(String wldtFullName) {
        return sanitize(config.getMetricPrefix() + "_" + wldtFullName);
    }

    static String sanitize(String raw) {
        return raw.replaceAll("[^a-zA-Z0-9_]", "_");
    }
}

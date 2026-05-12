package it.wldt.monitoring.prometheus;

import it.wldt.monitoring.metrics.WldtMetricComponent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Immutable configuration for {@link PrometheusMonitoringInterfaceHandler}.
 *
 * <p>Supports two working modes:</p>
 * <ul>
 *   <li>{@link WorkingMode#HTTP_SERVER} — starts an HTTP server on the configured port so a
 *       Prometheus instance can scrape metrics at {@code /metrics}.</li>
 *   <li>{@link WorkingMode#PUSH_GATEWAY} — periodically pushes metrics to a Prometheus
 *       PushGateway at the configured address, optionally with HTTP Basic Auth.</li>
 * </ul>
 *
 * <p>Build with the inner {@link Builder}:</p>
 * <pre>{@code
 * // HTTP scrape mode
 * PrometheusHandlerConfiguration cfg = new PrometheusHandlerConfiguration.Builder()
 *     .withHttpServer()
 *     .withHttpPort(9090)
 *     .withMetricPrefix("wldt")
 *     .build();
 *
 * // Push Gateway mode
 * PrometheusHandlerConfiguration cfg = new PrometheusHandlerConfiguration.Builder()
 *     .withPushGateway("localhost:9091")
 *     .withJobName("my-dt")
 *     .withPushGatewayAuth("user", "secret")
 *     .withPushIntervalMs(10_000L)
 *     .build();
 * }</pre>
 */
public final class PrometheusHandlerConfiguration {

    /** Determines how metrics are exposed to Prometheus. */
    public enum WorkingMode {
        /** Expose metrics via an HTTP server for Prometheus to scrape. */
        HTTP_SERVER,
        /** Push metrics to a Prometheus PushGateway at regular intervals. */
        PUSH_GATEWAY
    }

    public static final int    DEFAULT_HTTP_PORT        = 9090;
    public static final long   DEFAULT_PUSH_INTERVAL_MS = 1_000L;
    public static final String DEFAULT_METRIC_PREFIX    = "dw";
    public static final String DEFAULT_JOB_NAME         = "dw";

    private final WorkingMode workingMode;
    private final int         httpPort;
    private final String      pushGatewayAddress;   // "host:port" — no scheme
    private final String      jobName;
    private final String      pushGatewayUsername;  // nullable
    private final String      pushGatewayPassword;  // nullable
    private final long                      pushIntervalMs;
    private final String                    metricPrefix;
    private final String                    dtId;                    // nullable — used for per-DT PushGateway deletion
    private final Set<WldtMetricComponent>  immediatePushComponents; // components that trigger an immediate push on update

    private PrometheusHandlerConfiguration(Builder b) {
        this.workingMode             = b.workingMode;
        this.httpPort                = b.httpPort;
        this.pushGatewayAddress      = b.pushGatewayAddress;
        this.jobName                 = b.jobName;
        this.pushGatewayUsername     = b.pushGatewayUsername;
        this.pushGatewayPassword     = b.pushGatewayPassword;
        this.pushIntervalMs          = b.pushIntervalMs;
        this.metricPrefix            = b.metricPrefix;
        this.dtId                    = b.dtId;
        this.immediatePushComponents = Collections.unmodifiableSet(
                b.immediatePushComponents.isEmpty()
                        ? EnumSet.noneOf(WldtMetricComponent.class)
                        : EnumSet.copyOf(b.immediatePushComponents));
    }

    public WorkingMode getWorkingMode()        { return workingMode; }
    public int         getHttpPort()           { return httpPort; }
    /** @return PushGateway address in "host:port" format (no scheme) */
    public String      getPushGatewayAddress() { return pushGatewayAddress; }
    public String      getJobName()            { return jobName; }
    /** @return basic-auth username, or {@code null} if not configured */
    public String      getPushGatewayUsername(){ return pushGatewayUsername; }
    /** @return basic-auth password, or {@code null} if not configured */
    public String      getPushGatewayPassword(){ return pushGatewayPassword; }
    public long        getPushIntervalMs()     { return pushIntervalMs; }
    public String      getMetricPrefix()       { return metricPrefix; }
    /** @return DT instance ID used to scope PushGateway deletion, or {@code null} if not set */
    public String      getDtId()               { return dtId; }
    /** @return components for which every metric update triggers an immediate PushGateway push */
    public Set<WldtMetricComponent> getImmediatePushComponents() { return immediatePushComponents; }

    // -------------------------------------------------------------------------

    public static final class Builder {

        private WorkingMode workingMode         = WorkingMode.HTTP_SERVER;
        private int         httpPort            = DEFAULT_HTTP_PORT;
        private String      pushGatewayAddress  = null;
        private String      jobName             = DEFAULT_JOB_NAME;
        private String      pushGatewayUsername = null;
        private String      pushGatewayPassword = null;
        private long        pushIntervalMs      = DEFAULT_PUSH_INTERVAL_MS;
        private String                    metricPrefix             = DEFAULT_METRIC_PREFIX;
        private String                    dtId                     = null;
        private Set<WldtMetricComponent>  immediatePushComponents  = EnumSet.noneOf(WldtMetricComponent.class);

        /** Select HTTP server scrape mode (default). */
        public Builder withHttpServer() {
            this.workingMode = WorkingMode.HTTP_SERVER;
            return this;
        }

        /**
         * Select Push Gateway mode and set the target address.
         *
         * @param address PushGateway address in {@code "host:port"} format (no scheme).
         *                If a full URL is supplied the scheme is stripped automatically.
         */
        public Builder withPushGateway(String address) {
            this.workingMode        = WorkingMode.PUSH_GATEWAY;
            this.pushGatewayAddress = stripScheme(address);
            return this;
        }

        public Builder withHttpPort(int port) {
            this.httpPort = port;
            return this;
        }

        public Builder withJobName(String jobName) {
            this.jobName = jobName;
            return this;
        }

        public Builder withPushGatewayAuth(String username, String password) {
            this.pushGatewayUsername = username;
            this.pushGatewayPassword = password;
            return this;
        }

        public Builder withPushIntervalMs(long ms) {
            this.pushIntervalMs = ms;
            return this;
        }

        /** Prefix prepended to every Prometheus metric name (default {@value DEFAULT_METRIC_PREFIX}). */
        public Builder withMetricPrefix(String prefix) {
            this.metricPrefix = prefix;
            return this;
        }

        /**
         * Sets the Digital Twin ID used to scope PushGateway metric deletion on {@code stop()}.
         * When set, {@code stop()} issues a {@code DELETE /metrics/job/<job>/dt_id/<dtId>}
         * to remove only this DT's metrics from the PushGateway.
         */
        public Builder withDtId(String dtId) {
            this.dtId = dtId;
            return this;
        }

        /**
         * Enables immediate PushGateway push on every metric update for the given components.
         * Has no effect in HTTP_SERVER mode.
         */
        public Builder withImmediatePushForComponents(WldtMetricComponent... components) {
            for (WldtMetricComponent c : components)
                this.immediatePushComponents.add(c);
            return this;
        }

        public PrometheusHandlerConfiguration build() {
            if (workingMode == WorkingMode.PUSH_GATEWAY &&
                    (pushGatewayAddress == null || pushGatewayAddress.trim().isEmpty())) {
                throw new IllegalStateException(
                        "PUSH_GATEWAY mode requires a non-blank pushGatewayAddress — call withPushGateway(address)");
            }
            return new PrometheusHandlerConfiguration(this);
        }

        private static String stripScheme(String address) {
            if (address == null) return null;
            return address.replaceFirst("^https?://", "");
        }
    }
}

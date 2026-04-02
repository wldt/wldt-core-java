package it.wldt.monitoring.metrics;

/**
 * Abstract base class for all WLDT metric types.
 *
 * <p>Every metric produced by an internal DT component or by a developer via
 * {@code MonitoringInterface.trackCustomMetric()} extends this class.
 * It carries the common metadata that allows the {@code MonitoringInterface}
 * to route, filter, and enrich metrics before dispatching them to the
 * developer's {@code WldtMonitoringHandler}.</p>
 *
 * <p>Metadata fields:</p>
 * <ul>
 *   <li>{@code namespace}  — logical grouping prefix, e.g. {@code "wldt.internal"}
 *       for library-generated metrics or a custom string (configured via
 *       {@code MonitoringConfiguration}) for developer metrics.</li>
 *   <li>{@code name}       — metric identifier within its namespace, e.g.
 *       {@code "shadowing.processing.latency"}.</li>
 *   <li>{@code component}  — the {@link WldtMetricComponent} that emitted this
 *       metric (used by {@code MonitoringInterface} for flag-gating and routing).</li>
 *   <li>{@code timestampMs} — epoch milliseconds at the moment the metric was
 *       recorded; set automatically by subclass constructors.</li>
 * </ul>
 *
 * <p>Typed subclasses ({@link WldtCounter}, {@link WldtUpDownCounter},
 * {@link WldtGauge}, {@link WldtTimer}, {@link WldtHistogram}) carry the actual
 * measured value. {@code WldtMetric} itself acts as a generic escape hatch for
 * composite or non-standard metrics that do not fit any typed subclass.</p>
 */
public abstract class WldtMetric {

    /** Logical grouping prefix for this metric (e.g. {@code "wldt.internal"} or {@code "custom.myapp"}). */
    private final String namespace;

    /** Metric identifier within its namespace (e.g. {@code "shadowing.processing.latency"}). */
    private final String name;

    /** The DT component that produced this metric. Used for routing and flag-gating. */
    private final WldtMetricComponent component;

    /** Epoch milliseconds at the moment this metric instance was created. */
    private final long timestampMs;

    /**
     * Constructs a new {@code WldtMetric} with the given metadata.
     * The timestamp is set to {@link System#currentTimeMillis()} at construction time.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @throws IllegalArgumentException if namespace, name, or component is null
     */
    protected WldtMetric(String namespace, String name, WldtMetricComponent component) {
        if (namespace == null || namespace.trim().isEmpty())
            throw new IllegalArgumentException("Metric namespace must not be null or blank");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Metric name must not be null or blank");
        if (component == null)
            throw new IllegalArgumentException("Metric component must not be null");

        this.namespace   = namespace;
        this.name        = name;
        this.component   = component;
        this.timestampMs = System.currentTimeMillis();
    }

    /**
     * Returns the namespace of this metric.
     *
     * @return the namespace string, never null or blank
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Returns the name of this metric within its namespace.
     *
     * @return the metric name string, never null or blank
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the fully qualified metric identifier in the form {@code namespace.name}.
     *
     * @return dot-separated namespace and name
     */
    public String getFullName() {
        return namespace + "." + name;
    }

    /**
     * Returns the DT component that emitted this metric.
     *
     * @return the originating {@link WldtMetricComponent}
     */
    public WldtMetricComponent getComponent() {
        return component;
    }

    /**
     * Returns the epoch millisecond timestamp at which this metric was recorded.
     *
     * @return epoch milliseconds
     */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Returns a human-readable string representation of this metric's metadata.
     * Subclasses should override to append their specific value fields.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", component=" + component +
                ", timestampMs=" + timestampMs +
                '}';
    }
}
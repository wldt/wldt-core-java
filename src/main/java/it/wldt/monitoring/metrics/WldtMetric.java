package it.wldt.monitoring.metrics;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all WLDT metric types.
 *
 * <p>Every metric produced by an internal DT component or by a developer via
 * {@code MonitoringInterface.trackCustomMetric()} extends this class.
 * It carries the common metadata that allows the {@code MonitoringInterface}
 * to route, filter, and enrich metrics before dispatching them to the
 * developer's {@code MonitoringInterfaceHandler}.</p>
 *
 * <p>Metadata fields:</p>
 * <ul>
 *   <li>{@code namespace}  — logical grouping prefix, e.g. {@code "wldt.internal"}
 *       for library-generated metrics or a custom string for developer metrics.</li>
 *   <li>{@code name}       — metric identifier within its namespace.</li>
 *   <li>{@code component}  — the {@link WldtMetricComponent} that emitted this metric.</li>
 *   <li>{@code timestampMs} — epoch milliseconds at the moment the metric was created.</li>
 *   <li>{@code lastUpdatedMs} — epoch milliseconds of the most recent mutation.</li>
 * </ul>
 *
 * <p>Metric instances are mutable: subclasses update their value fields in-place
 * via typed mutation methods ({@code update()}, {@code increment()}, etc.) and
 * keep the {@code lastUpdatedMs} field current on each mutation.</p>
 */
public abstract class WldtMetric {

    private final String digitalTwinId;
    private final String namespace;
    private final String name;
    private final WldtMetricComponent component;
    private final long timestampMs;
    private Map<String, Object> metadata;

    /**
     * Identifies the specific component instance that owns this metric (e.g. adapter id, function id).
     * {@code null} for singleton components such as {@code DigitalTwinModel} and {@code AugmentationManager}.
     * Used by the registry as the inner key in the two-level {@code Map<fullName, Map<instanceId, WldtMetric>>}.
     */
    private String instanceId = null;

    /** Epoch milliseconds of the most recent in-place mutation; equals timestampMs before any mutation. */
    protected volatile long lastUpdatedMs;

    /**
     * {@code true} once the metric has received at least one measured value.
     * {@code false} for metrics created via the no-value constructor (pre-registration only).
     * Subclasses must set this to {@code true} on any mutation that records a real measurement.
     */
    protected volatile boolean initialized;

    /**
     * Constructs a new {@code WldtMetric}.
     * Both {@code timestampMs} and {@code lastUpdatedMs} are set to {@link System#currentTimeMillis()}.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @throws IllegalArgumentException if namespace, name, or component is null or blank
     */
    protected WldtMetric(String digitalTwinId, String namespace, String name, WldtMetricComponent component) {

        if (digitalTwinId == null || digitalTwinId.trim().isEmpty())
            throw new IllegalArgumentException("Digital Twin ID must not be null or blank");
        if (namespace == null || namespace.trim().isEmpty())
            throw new IllegalArgumentException("Metric namespace must not be null or blank");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Metric name must not be null or blank");
        if (component == null)
            throw new IllegalArgumentException("Metric component must not be null");

        this.digitalTwinId = digitalTwinId;
        this.namespace     = namespace;
        this.name          = name;
        this.component     = component;
        this.timestampMs   = System.currentTimeMillis();
        this.lastUpdatedMs = this.timestampMs;
        this.metadata      = new HashMap<>();
    }

    /**
     * Constructs a new {@code WldtMetric}.
     * Both {@code timestampMs} and {@code lastUpdatedMs} are set to {@link System#currentTimeMillis()}.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @param metadata  Additional Metadata Associated to the metric
     * @throws IllegalArgumentException if namespace, name, or component is null or blank
     */
    protected WldtMetric(String digitalTwinId, String namespace, String name, WldtMetricComponent component, Map<String, Object> metadata) {

        if (digitalTwinId == null || digitalTwinId.trim().isEmpty())
            throw new IllegalArgumentException("Digital Twin ID must not be null or blank");
        if (namespace == null || namespace.trim().isEmpty())
            throw new IllegalArgumentException("Metric namespace must not be null or blank");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Metric name must not be null or blank");
        if (component == null)
            throw new IllegalArgumentException("Metric component must not be null");

        this.digitalTwinId = digitalTwinId;
        this.namespace     = namespace;
        this.name          = name;
        this.component     = component;
        this.timestampMs   = System.currentTimeMillis();
        this.lastUpdatedMs = this.timestampMs;
        this.metadata      = metadata;
    }

    /** @return {@code true} if this metric has received at least one measured value */
    public boolean isInitialized() { return initialized; }

    /**
     * Returns a new instance of the same concrete type with the same namespace, name, and
     * component but with <em>no</em> measured value — i.e., an uninitialized snapshot.
     * Used by {@code MonitoringInterface} to fire {@code onMetricRegistered} with a clean
     * placeholder before the first value is reported via {@code onMetricUpdated}.
     *
     * @return a fresh uninitialized metric of the same concrete type
     */
    public abstract WldtMetric emptySnapshot();

    /**
     * Returns a deep copy of this metric instance, capturing all current field values.
     * The copy is an independent object — subsequent mutations to the original do not
     * affect the copy and vice versa. Used by {@code MonitoringInterface} to hand
     * immutable snapshots to {@code MonitoringInterfaceHandler} callbacks.
     *
     * @return a new metric instance of the same concrete type with identical field values
     */
    public abstract WldtMetric copy();

    /**
     * Merges the provided metadata map into this metric's existing metadata, overwriting any existing keys.
     * @param key the metadata key to add or update
     * @param value the metadata value to associate with the key
     */
    public void addMetadata(String key, Object value) {
        if(key != null && value != null && this.metadata != null)
            this.metadata.put(key, value);
    }

    /**
     * Removes the specified key from this metric's metadata if it exists.
     * @param key the metadata key to remove
     */
    public void removeMetadata(String key) {
        if(key != null && this.metadata != null)
            this.metadata.remove(key);
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public String getNamespace() { return namespace; }

    public String getName()      { return name; }

    public String getFullName()  { return namespace + "." + name; }

    public WldtMetricComponent getComponent() { return component; }

    public long getTimestampMs()    { return timestampMs; }

    public long getLastUpdatedMs()  { return lastUpdatedMs; }

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    /** @return the instance id of the component that owns this metric, or {@code null} for singletons */
    public String getInstanceId() { return instanceId; }

    /**
     * Sets the instance id that identifies the specific component instance owning this metric.
     * @param instanceId the instance identifier; {@code null} for singleton components
     */
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    /**
     * Fluent setter for {@link #setInstanceId(String)}.
     * @param instanceId the instance identifier
     * @return this metric, for chaining
     */
    public WldtMetric withInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Copies this metric's {@code instanceId} into {@code target}.
     * Called by copy constructors and {@code emptySnapshot()} implementations in subclasses.
     */
    protected void copyInstanceIdTo(WldtMetric target) {
        target.instanceId = this.instanceId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                "{namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", component=" + component +
                ", timestampMs=" + timestampMs +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

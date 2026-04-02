package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing a counter for discrete countable entities
 * that can both increase and decrease.
 *
 * <p>{@code WldtUpDownCounter} is semantically distinct from both
 * {@link WldtCounter} and {@link WldtGauge}:</p>
 * <ul>
 *   <li>Unlike {@link WldtCounter}, it can decrease — it is not monotonic.</li>
 *   <li>Unlike {@link WldtGauge}, it models discrete countable entities
 *       (e.g. "how many adapters are connected right now") rather than
 *       a continuously observed physical or computed value
 *       (e.g. "current CPU usage percentage").</li>
 * </ul>
 *
 * <p>Typical WLDT use cases:</p>
 * <ul>
 *   <li>{@code wldt.physical_adapter.connected_count} — number of Physical
 *       Adapters currently connected to the DT.</li>
 *   <li>{@code wldt.digital_adapter.active_count} — number of Digital Adapters
 *       currently active.</li>
 *   <li>{@code wldt.augmentation.registered_functions} — number of Augmentation
 *       Functions currently registered on the DT.</li>
 * </ul>
 *
 * <p><strong>Delta field:</strong> the {@code delta} field is computed and injected
 * by the {@code MonitoringInterface} registry before dispatching this metric to the
 * developer's {@code WldtMonitoringHandler}. It represents the signed difference
 * between the current absolute value and the previously registered value for this
 * metric — positive when entities were added, negative when removed.
 * It is {@code null} on the first push for a given metric name (no previous value
 * available yet). Developers can use {@code getDelta()} directly to feed Prometheus
 * {@code Gauge.inc()} / {@code Gauge.dec()} without maintaining their own state.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongUpDownCounter} — exact
 * semantic match, dedicated type in the OTel API.</p>
 *
 * <p><strong>Prometheus mapping:</strong> {@code Gauge} — no dedicated
 * UpDownCounter type in Prometheus; use {@code getDelta()} to drive
 * {@code Gauge.inc(delta)} or {@code Gauge.dec(Math.abs(delta))} based
 * on the sign of the delta.</p>
 */
public class WldtUpDownCounter extends WldtMetric {

    /**
     * The current absolute value of this up-down counter at the time this
     * metric was recorded. May be zero, positive, or negative.
     */
    private final long value;

    /**
     * The signed difference between this value and the previously registered
     * value for this metric, as computed by the {@code MonitoringInterface} registry.
     *
     * <p>Positive when the count increased, negative when it decreased.
     * Is {@code null} on the first push for a given metric name because no
     * previous value is available yet.</p>
     */
    private final Long delta;

    /**
     * Constructs a new {@code WldtUpDownCounter} metric without a delta value.
     * Used by library components when recording a raw counter value before
     * the {@code MonitoringInterface} registry enriches it with the delta.
     *
     * @param namespace the logical namespace grouping this metric
     *                  (e.g. {@code "wldt.internal"} or {@code "custom.myapp"})
     * @param name      the metric name within its namespace
     *                  (e.g. {@code "physical_adapter.connected_count"})
     * @param component the DT component that emitted this metric
     * @param value     the current absolute counter value at recording time;
     *                  may be zero, positive, or negative
     * @throws IllegalArgumentException if namespace, name, or component is null
     */
    public WldtUpDownCounter(String namespace, String name,
                             WldtMetricComponent component, long value) {
        this(namespace, name, component, value, null);
    }

    /**
     * Constructs a new {@code WldtUpDownCounter} metric with a pre-computed delta.
     * Used internally by the {@code MonitoringInterface} registry to produce
     * the enriched metric that is dispatched to the developer's handler.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @param value     the current absolute counter value; may be any long
     * @param delta     the signed difference from the previous recorded value,
     *                  or {@code null} if this is the first push for this metric name
     * @throws IllegalArgumentException if namespace, name, or component is null
     */
    public WldtUpDownCounter(String namespace, String name, WldtMetricComponent component,
                             long value, Long delta) {
        super(namespace, name, component);
        this.value = value;
        this.delta = delta;
    }

    /**
     * Returns the current absolute value of this up-down counter at the time
     * of recording. The value may be zero, positive, or negative.
     *
     * @return the current absolute counter value
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the signed delta between this value and the previously registered
     * value for this metric, as computed by the {@code MonitoringInterface} registry.
     *
     * <p>Returns {@code null} on the first push for a given metric name.
     * On subsequent pushes returns a {@code Long} that is positive when the
     * count increased and negative when it decreased.</p>
     *
     * <p>Typical Prometheus usage in a {@code WldtMonitoringHandler}:</p>
     * <pre>{@code
     * if (metric instanceof WldtUpDownCounter && ((WldtUpDownCounter) metric).getDelta() != null) {
     *     if (c.getDelta() > 0) prometheusGauge.inc(c.getDelta());
     *     else                  prometheusGauge.dec(Math.abs(c.getDelta()));
     * }
     * }</pre>
     *
     * @return the signed delta from the previous value, or {@code null} if unavailable
     */
    public Long getDelta() {
        return delta;
    }

    /**
     * Returns {@code true} if the delta value is available for this metric instance.
     * Equivalent to {@code getDelta() != null}.
     *
     * @return {@code true} if delta is present, {@code false} on first push
     */
    public boolean isDeltaAvailable() {
        return delta != null;
    }

    /**
     * Returns a new {@code WldtUpDownCounter} instance identical to this one
     * but with the given delta value injected.
     * Used internally by the {@code MonitoringInterface} registry to enrich
     * the metric before dispatching it to the handler.
     *
     * @param computedDelta the signed delta to inject
     * @return enriched copy of this counter with the delta set
     */
    public WldtUpDownCounter withDelta(long computedDelta) {
        return new WldtUpDownCounter(getNamespace(), getName(), getComponent(),
                value, computedDelta);
    }

    /**
     * Returns a string representation including the counter value and delta.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WldtUpDownCounter{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", timestampMs=" + getTimestampMs() +
                ", value=" + value +
                ", delta=" + (delta != null ? delta : "n/a") +
                '}';
    }
}
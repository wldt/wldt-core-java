package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing a monotonically increasing counter.
 *
 * <p>{@code WldtCounter} models discrete occurrences that accumulate over time
 * and never decrease. It is the correct choice whenever the quantity being
 * measured can only go up — such as the total number of events processed,
 * messages sent, or errors encountered.</p>
 *
 * <p>Typical WLDT use cases:</p>
 * <ul>
 *   <li>{@code wldt.dt_model.events_processed} — total physical events handled
 *       by the Shadowing Function since DT startup.</li>
 *   <li>{@code wldt.physical_adapter.messages_received} — total messages
 *       received from the physical asset.</li>
 *   <li>{@code wldt.event_bus.dropped_events} — total events dropped due to
 *       queue saturation.</li>
 * </ul>
 *
 * <p><strong>Delta field:</strong> the {@code delta} field is computed and injected
 * by the {@code MonitoringInterface} registry before dispatching this metric to the
 * developer's {@code WldtMonitoringHandler}. It represents the difference between
 * the current absolute value and the previously registered value for this metric.
 * It is {@code null} on the first push for a given metric name (no previous value
 * available yet). Developers can use {@code getDelta()} directly to feed Prometheus
 * {@code Counter.inc()} without maintaining their own tracking state.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongCounter} / {@code DoubleCounter}
 * — full semantic match, increment only.</p>
 *
 * <p><strong>Prometheus mapping:</strong> {@code Counter} — use {@code getDelta()}
 * to drive {@code Counter.inc(delta)} without any additional tracking in the handler.</p>
 *
 * <p>If the quantity being modelled can also decrease (e.g. number of active
 * connections), use {@link WldtUpDownCounter} instead. If it represents a
 * continuously observed point-in-time value (e.g. memory usage), use
 * {@link WldtGauge}.</p>
 */
public class WldtCounter extends WldtMetric {

    /**
     * The absolute cumulative counter value at the time this metric was recorded.
     * Always non-negative. Represents a cumulative total, not a delta.
     */
    private final long value;

    /**
     * The difference between this value and the previously registered value
     * for this metric, as computed by the {@code MonitoringInterface} registry.
     *
     * <p>Is {@code null} on the first push for a given metric name because no
     * previous value is available yet. On all subsequent pushes it is a
     * non-negative {@code Long}.</p>
     */
    private final Long delta;

    /**
     * Constructs a new {@code WldtCounter} metric without a delta value.
     * Used by library components when recording a raw counter value before
     * the {@code MonitoringInterface} registry enriches it with the delta.
     *
     * @param namespace the logical namespace grouping this metric
     *                  (e.g. {@code "wldt.internal"} or {@code "custom.myapp"})
     * @param name      the metric name within its namespace
     *                  (e.g. {@code "dt_model.events_processed"})
     * @param component the DT component that emitted this metric
     * @param value     the absolute cumulative counter value at recording time;
     *                  must be non-negative
     * @throws IllegalArgumentException if namespace, name, or component is null,
     *                                  or if value is negative
     */
    public WldtCounter(String namespace, String name, WldtMetricComponent component, long value) {
        this(namespace, name, component, value, null);
    }

    /**
     * Constructs a new {@code WldtCounter} metric with a pre-computed delta.
     * Used internally by the {@code MonitoringInterface} registry to produce
     * the enriched metric that is dispatched to the developer's handler.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @param value     the absolute cumulative counter value; must be non-negative
     * @param delta     the difference from the previous recorded value, or
     *                  {@code null} if this is the first push for this metric name
     * @throws IllegalArgumentException if namespace, name, or component is null,
     *                                  if value is negative,
     *                                  or if delta is non-null and negative
     */
    public WldtCounter(String namespace, String name, WldtMetricComponent component,
                       long value, Long delta) {
        super(namespace, name, component);
        if (value < 0)
            throw new IllegalArgumentException(
                    "WldtCounter value must be non-negative, got: " + value);
        if (delta != null && delta < 0)
            throw new IllegalArgumentException(
                    "WldtCounter delta must be non-negative when present, got: " + delta);
        this.value = value;
        this.delta = delta;
    }

    /**
     * Returns the absolute cumulative counter value at the time this metric
     * was recorded.
     *
     * @return non-negative cumulative count
     */
    public long getValue() {
        return value;
    }

    /**
     * Returns the delta between this value and the previously registered value
     * for this metric, as computed by the {@code MonitoringInterface} registry.
     *
     * <p>Returns {@code null} on the first push for a given metric name.
     * On all subsequent pushes returns a non-negative {@code Long} representing
     * the number of new occurrences since the last notification.</p>
     *
     * <p>Typical Prometheus usage in a {@code WldtMonitoringHandler}:</p>
     * <pre>{@code
     * if (metric instanceof WldtCounter && ((WldtCounter) metric).getDelta() != null)
     *     prometheusCounter.inc(c.getDelta());
     * }</pre>
     *
     * @return the delta from the previous value, or {@code null} if unavailable
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
     * Returns a new {@code WldtCounter} instance identical to this one but
     * with the given delta value injected.
     * Used internally by the {@code MonitoringInterface} registry to enrich
     * the metric before dispatching it to the handler.
     *
     * @param computedDelta the delta to inject; must be non-negative
     * @return enriched copy of this counter with the delta set
     */
    public WldtCounter withDelta(long computedDelta) {
        return new WldtCounter(getNamespace(), getName(), getComponent(), value, computedDelta);
    }

    /**
     * Returns a string representation including the counter value and delta.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WldtCounter{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", timestampMs=" + getTimestampMs() +
                ", value=" + value +
                ", delta=" + (delta != null ? delta : "n/a") +
                '}';
    }
}
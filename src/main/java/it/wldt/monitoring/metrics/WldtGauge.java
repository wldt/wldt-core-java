package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing a continuously observed numeric value
 * that can freely rise and fall over time.
 *
 * <p>{@code WldtGauge} models point-in-time observations of a physical or
 * computed quantity — the recorded value is a snapshot at the moment of
 * measurement, not an accumulation. It is the correct choice when the
 * value has no inherent direction constraint and represents a continuous
 * magnitude rather than a discrete entity count.</p>
 *
 * <p>Typical WLDT use cases:</p>
 * <ul>
 *   <li>{@code wldt.event_bus.queue_depth} — current number of pending
 *       events in the internal Event Bus queue.</li>
 *   <li>{@code wldt.dt_model.state_properties_count} — current number of
 *       properties declared in the Digital Twin State.</li>
 *   <li>{@code wldt.storage.entry_count} — current number of entries
 *       persisted in the Storage layer.</li>
 *   <li>{@code custom.myapp.room_temperature} — a developer-recorded
 *       sensor reading forwarded as a custom metric.</li>
 * </ul>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongGauge} /
 * {@code DoubleGauge} / {@code ObservableGauge} — full semantic match.
 * Use {@code ObservableGauge} when the value is polled asynchronously;
 * use the synchronous variants for push-based recordings as in WLDT.</p>
 *
 * <p><strong>Prometheus mapping:</strong> {@code Gauge} — full semantic match.
 * Prometheus {@code Gauge} supports arbitrary set, increment, and decrement
 * operations, making it a natural fit.</p>
 *
 * <p>If the quantity is a discrete entity count that can go up and down
 * (e.g. number of connected adapters), prefer {@link WldtUpDownCounter}.
 * If it is a duration measurement, prefer {@link WldtTimer}.</p>
 */
public class WldtGauge extends WldtMetric {

    /**
     * The observed value at the time this metric was recorded.
     * May be any finite double — positive, negative, or zero.
     */
    private final double value;

    /**
     * Constructs a new {@code WldtGauge} metric.
     *
     * @param namespace the logical namespace grouping this metric
     *                  (e.g. {@code "wldt.internal"} or {@code "custom.myapp"})
     * @param name      the metric name within its namespace
     *                  (e.g. {@code "event_bus.queue_depth"})
     * @param component the DT component that emitted this metric
     * @param value     the observed value at recording time; may be any finite double
     * @throws IllegalArgumentException if namespace, name, or component is null,
     *                                  or if value is NaN or infinite
     */
    public WldtGauge(String namespace, String name, WldtMetricComponent component, double value) {
        super(namespace, name, component);
        if (Double.isNaN(value) || Double.isInfinite(value))
            throw new IllegalArgumentException("WldtGauge value must be finite, got: " + value);
        this.value = value;
    }

    /**
     * Returns the observed gauge value at the time this metric was recorded.
     *
     * @return the point-in-time observed value
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns a string representation including the gauge value.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WldtGauge{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", timestampMs=" + getTimestampMs() +
                ", value=" + value +
                '}';
    }
}
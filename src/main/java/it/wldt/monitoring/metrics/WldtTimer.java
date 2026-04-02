package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing the duration of a measured operation in milliseconds.
 *
 * <p>{@code WldtTimer} captures how long a specific operation took to complete.
 * It is the correct choice for latency and processing-time measurements where
 * a single duration value per operation is sufficient. When a statistical
 * distribution of durations over many samples is needed, use
 * {@link WldtHistogram} instead.</p>
 *
 * <p>Typical WLDT use cases:</p>
 * <ul>
 *   <li>{@code wldt.dt_model.processing_latency_ms} — time taken by the
 *       Shadowing Function to process a single incoming physical event.</li>
 *   <li>{@code wldt.physical_adapter.message_processing_ms} — time taken to
 *       decode and forward a single message from the physical asset.</li>
 *   <li>{@code wldt.augmentation.function_execution_ms} — execution time of
 *       a single Augmentation Function invocation.</li>
 *   <li>{@code wldt.storage.write_latency_ms} — time taken to persist a single
 *       DT state update to the Storage layer.</li>
 * </ul>
 *
 * <p><strong>OpenTelemetry mapping:</strong> OTel has no dedicated Timer
 * instrument. The standard approach is to use a {@code LongHistogram} with unit
 * {@code "ms"} and record each duration as a single observation. In the
 * developer's {@code WldtMonitoringHandler}, call
 * {@code histogram.record(metric.getDurationMs())}.</p>
 *
 * <p><strong>Prometheus mapping:</strong> Two options are available in the handler
 * implementation:</p>
 * <ul>
 *   <li>{@code Histogram} — client-side bucket aggregation; preferred when
 *       Prometheus scrapes the application directly and quantile accuracy
 *       at query time is important.</li>
 *   <li>{@code Summary} — server-side quantile calculation; preferred when
 *       quantiles must be accurate at recording time rather than query time.</li>
 * </ul>
 * <p>Both are partial matches — the developer must choose between them in the
 * handler implementation based on their observability backend requirements.</p>
 */
public class WldtTimer extends WldtMetric {

    /**
     * The measured duration in milliseconds.
     * Always non-negative — a duration of zero is valid for extremely fast operations.
     */
    private final long durationMs;

    /**
     * Constructs a new {@code WldtTimer} metric with a pre-computed duration.
     * Use this constructor when the caller has already measured the elapsed time.
     *
     * @param namespace  the logical namespace grouping this metric
     *                   (e.g. {@code "wldt.internal"} or {@code "custom.myapp"})
     * @param name       the metric name within its namespace
     *                   (e.g. {@code "dt_model.processing_latency_ms"})
     * @param component  the DT component that emitted this metric
     * @param durationMs the measured duration in milliseconds; must be non-negative
     * @throws IllegalArgumentException if namespace, name, or component is null,
     *                                  or if durationMs is negative
     */
    public WldtTimer(String namespace, String name, WldtMetricComponent component, long durationMs) {
        super(namespace, name, component);
        if (durationMs < 0)
            throw new IllegalArgumentException("WldtTimer durationMs must be non-negative, got: " + durationMs);
        this.durationMs = durationMs;
    }

    /**
     * Factory method to create a {@code WldtTimer} by computing the elapsed time
     * between a recorded start time and the current system time.
     *
     * <p>Usage example inside a library component:</p>
     * <pre>{@code
     * long start = System.currentTimeMillis();
     * // ... operation ...
     * WldtTimer timer = WldtTimer.since(
     *     "wldt.internal", "dt_model.processing_latency_ms",
     *     WldtMetricComponent.DT_MODEL, start);
     * monitoringInterface.notifyMetric(timer);
     * }</pre>
     *
     * @param namespace  the logical namespace grouping this metric
     * @param name       the metric name within its namespace
     * @param component  the DT component that emitted this metric
     * @param startMs    the epoch millisecond timestamp when the operation started
     * @return a new {@code WldtTimer} with durationMs computed as
     *         {@code System.currentTimeMillis() - startMs}
     * @throws IllegalArgumentException if the computed duration is negative
     *                                  (i.e. startMs is in the future)
     */
    public static WldtTimer since(String namespace, String name,
                                  WldtMetricComponent component, long startMs) {
        return new WldtTimer(namespace, name, component, System.currentTimeMillis() - startMs);
    }

    /**
     * Returns the measured duration in milliseconds.
     *
     * @return non-negative duration in milliseconds
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Returns the measured duration converted to seconds as a double.
     * Convenience method for backends that expect second-precision values.
     *
     * @return duration in seconds, preserving sub-second precision
     */
    public double getDurationSeconds() {
        return durationMs / 1000.0;
    }

    /**
     * Returns a string representation including the duration value.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WldtTimer{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", timestampMs=" + getTimestampMs() +
                ", durationMs=" + durationMs +
                '}';
    }
}
package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing the statistical distribution of a set of observed samples.
 *
 * <p>{@code WldtHistogram} aggregates multiple observations into a compact
 * statistical summary carrying the sample count, sum, minimum, and maximum
 * values recorded over an observation window. It is the correct choice when
 * a single data point per operation is insufficient and the spread or
 * central tendency of a measurement over many occurrences is relevant.</p>
 *
 * <p>Typical WLDT use cases:</p>
 * <ul>
 *   <li>{@code wldt.physical_adapter.message_size_bytes} — distribution of
 *       the byte sizes of messages received from the physical asset over a
 *       processing window.</li>
 *   <li>{@code wldt.dt_model.processing_latency_ms} — latency distribution
 *       of Shadowing Function executions when individual {@link WldtTimer}
 *       push granularity is too high and pre-aggregation is preferred.</li>
 *   <li>{@code wldt.event_bus.batch_size} — distribution of event batch
 *       sizes dispatched through the internal Event Bus.</li>
 * </ul>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongHistogram} /
 * {@code DoubleHistogram} — full semantic match. The OTel SDK typically
 * manages bucket boundaries on the SDK side; {@code count} and {@code sum}
 * from this class map directly to the OTel histogram's cumulative fields.
 * {@code min} and {@code max} map to OTel's optional min/max attributes
 * when {@code ExplicitBucketHistogramAggregation} is configured.</p>
 *
 * <p><strong>Prometheus mapping:</strong> {@code Histogram} — full semantic
 * match. {@code count} maps to {@code _count}, {@code sum} maps to
 * {@code _sum}. {@code min} and {@code max} can be exposed as additional
 * gauge metrics or used internally by the handler. Prometheus bucket
 * boundaries ({@code _bucket}) must be defined in the handler implementation,
 * as WLDT histograms do not carry per-bucket counts.</p>
 *
 * <p>If only a single duration per operation needs to be recorded, prefer
 * the lighter-weight {@link WldtTimer}.</p>
 */
public class WldtHistogram extends WldtMetric {

    /**
     * The total number of observations included in this histogram snapshot.
     * Always positive — a histogram with zero observations should not be emitted.
     */
    private final long count;

    /**
     * The arithmetic sum of all observed values.
     * Used together with {@code count} to compute the mean: {@code sum / count}.
     */
    private final double sum;

    /**
     * The minimum observed value across all samples in this snapshot.
     * Always less than or equal to {@code max}.
     */
    private final double min;

    /**
     * The maximum observed value across all samples in this snapshot.
     * Always greater than or equal to {@code min}.
     */
    private final double max;

    /**
     * Constructs a new {@code WldtHistogram} metric from pre-aggregated statistics.
     *
     * @param namespace the logical namespace grouping this metric
     *                  (e.g. {@code "wldt.internal"} or {@code "custom.myapp"})
     * @param name      the metric name within its namespace
     *                  (e.g. {@code "physical_adapter.message_size_bytes"})
     * @param component the DT component that emitted this metric
     * @param count     total number of observations; must be positive
     * @param sum       arithmetic sum of all observed values
     * @param min       minimum observed value; must be less than or equal to max
     * @param max       maximum observed value; must be greater than or equal to min
     * @throws IllegalArgumentException if namespace, name, or component is null,
     *                                  if count is non-positive,
     *                                  if sum is NaN or infinite,
     *                                  if min or max are NaN or infinite,
     *                                  or if min is greater than max
     */
    public WldtHistogram(String namespace, String name, WldtMetricComponent component,
                         long count, double sum, double min, double max) {
        super(namespace, name, component);

        if (count <= 0)
            throw new IllegalArgumentException("WldtHistogram count must be positive, got: " + count);
        if (Double.isNaN(sum) || Double.isInfinite(sum))
            throw new IllegalArgumentException("WldtHistogram sum must be finite, got: " + sum);
        if (Double.isNaN(min) || Double.isInfinite(min))
            throw new IllegalArgumentException("WldtHistogram min must be finite, got: " + min);
        if (Double.isNaN(max) || Double.isInfinite(max))
            throw new IllegalArgumentException("WldtHistogram max must be finite, got: " + max);
        if (min > max)
            throw new IllegalArgumentException(
                    "WldtHistogram min must be <= max, got min=" + min + " max=" + max);

        this.count = count;
        this.sum   = sum;
        this.min   = min;
        this.max   = max;
    }

    /**
     * Returns the total number of observations included in this histogram snapshot.
     *
     * @return positive observation count
     */
    public long getCount() {
        return count;
    }

    /**
     * Returns the arithmetic sum of all observed values.
     * Divide by {@link #getCount()} to obtain the mean.
     *
     * @return sum of all observations
     */
    public double getSum() {
        return sum;
    }

    /**
     * Returns the minimum observed value across all samples in this snapshot.
     *
     * @return minimum observed value
     */
    public double getMin() {
        return min;
    }

    /**
     * Returns the maximum observed value across all samples in this snapshot.
     *
     * @return maximum observed value
     */
    public double getMax() {
        return max;
    }

    /**
     * Computes and returns the arithmetic mean of all observed values.
     * Equivalent to {@code getSum() / getCount()}.
     *
     * @return mean of all observations
     */
    public double getMean() {
        return sum / count;
    }

    /**
     * Returns a string representation including all statistical fields.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "WldtHistogram{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", timestampMs=" + getTimestampMs() +
                ", count=" + count +
                ", sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", mean=" + getMean() +
                '}';
    }
}
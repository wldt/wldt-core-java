package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing the statistical distribution of a set of observed samples.
 *
 * <p>Instances are mutable. The constructor records the first observation window. New
 * observations are added via {@link #observe(double)} (single value) or
 * {@link #update(long, double, double, double)} (pre-aggregated window). The metric tracks
 * both per-window fields ({@code count}, {@code sum}, {@code min}, {@code max}) and
 * cumulative fields ({@code totalCount}, {@code totalSum}, {@code globalMin}, {@code globalMax})
 * across all windows.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongHistogram} / {@code DoubleHistogram}.</p>
 * <p><strong>Prometheus mapping:</strong> {@code Histogram}.</p>
 */
public class WldtHistogram extends WldtMetric {

    // Current window fields (last observe/update call)
    private long   count;
    private double sum;
    private double min;
    private double max;

    // Cumulative fields across all windows
    private long   totalCount;
    private double totalSum;
    private double globalMin;
    private double globalMax;
    private long   windowCount;

    /**
     * Constructs a new {@code WldtHistogram} from the first pre-aggregated window.
     * All cumulative fields are initialized from the window values; {@code windowCount} is 1.
     *
     * @param namespace the logical namespace grouping this metric
     * @param name      the metric name within its namespace
     * @param component the DT component that emitted this metric
     * @param count     number of observations in this window; must be positive
     * @param sum       arithmetic sum of all observations; must be finite
     * @param min       minimum observation; must be finite and &le; max
     * @param max       maximum observation; must be finite and &ge; min
     * @throws IllegalArgumentException if any constraint is violated
     */
    /**
     * Constructs an uninitialized {@code WldtHistogram} for pre-registration.
     * {@link #isInitialized()} returns {@code false} until the first {@link #observe(double)}
     * or {@link #update(long, double, double, double)} call. All stats are 0 and must not
     * be used before initialization.
     */
    public WldtHistogram(String namespace, String name, WldtMetricComponent component) {
        super(namespace, name, component);
        this.count       = 0;
        this.sum         = 0;
        this.min         = 0;
        this.max         = 0;
        this.totalCount  = 0;
        this.totalSum    = 0;
        this.globalMin   = 0;
        this.globalMax   = 0;
        this.windowCount = 0;
        this.initialized = false;
    }

    public WldtHistogram(String namespace, String name, WldtMetricComponent component,
                         long count, double sum, double min, double max) {
        super(namespace, name, component);
        validateWindow(count, sum, min, max);
        this.count       = count;
        this.sum         = sum;
        this.min         = min;
        this.max         = max;
        this.totalCount  = count;
        this.totalSum    = sum;
        this.globalMin   = min;
        this.globalMax   = max;
        this.windowCount = 1;
        this.initialized = true;
    }

    /**
     * Adds a single observation to the histogram.
     * Equivalent to merging a window of {@code count=1, sum=value, min=value, max=value}.
     *
     * @param value the observed value; must be finite
     * @throws IllegalArgumentException if value is NaN or infinite
     */
    public synchronized WldtHistogram observe(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value))
            throw new IllegalArgumentException("WldtHistogram observation must be finite, got: " + value);
        if (!initialized) {
            this.count       = 1;
            this.sum         = value;
            this.min         = value;
            this.max         = value;
            this.totalCount  = 1;
            this.totalSum    = value;
            this.globalMin   = value;
            this.globalMax   = value;
            this.windowCount = 1;
            this.initialized = true;
            this.lastUpdatedMs = System.currentTimeMillis();
            return this;
        }
        this.count       = 1;
        this.sum         = value;
        this.min         = value;
        this.max         = value;
        this.totalCount++;
        this.totalSum   += value;
        this.globalMin   = Math.min(this.globalMin, value);
        this.globalMax   = Math.max(this.globalMax, value);
        this.windowCount++;
        this.lastUpdatedMs = System.currentTimeMillis();
        return this;
    }

    /**
     * Merges a pre-aggregated window into this histogram.
     *
     * @param count number of observations in the new window; must be positive
     * @param sum   arithmetic sum; must be finite
     * @param min   minimum in the window; must be finite and &le; max
     * @param max   maximum in the window; must be finite and &ge; min
     * @throws IllegalArgumentException if any constraint is violated
     */
    public synchronized WldtHistogram update(long count, double sum, double min, double max) {
        validateWindow(count, sum, min, max);
        if (!initialized) {
            this.count       = count;
            this.sum         = sum;
            this.min         = min;
            this.max         = max;
            this.totalCount  = count;
            this.totalSum    = sum;
            this.globalMin   = min;
            this.globalMax   = max;
            this.windowCount = 1;
            this.initialized = true;
            this.lastUpdatedMs = System.currentTimeMillis();
            return this;
        }
        this.count       = count;
        this.sum         = sum;
        this.min         = min;
        this.max         = max;
        this.totalCount  += count;
        this.totalSum    += sum;
        this.globalMin   = Math.min(this.globalMin, min);
        this.globalMax   = Math.max(this.globalMax, max);
        this.windowCount++;
        this.lastUpdatedMs = System.currentTimeMillis();
        return this;
    }

    private WldtHistogram(WldtHistogram source) {
        super(source.getNamespace(), source.getName(), source.getComponent());
        this.count       = source.count;
        this.sum         = source.sum;
        this.min         = source.min;
        this.max         = source.max;
        this.totalCount  = source.totalCount;
        this.totalSum    = source.totalSum;
        this.globalMin   = source.globalMin;
        this.globalMax   = source.globalMax;
        this.windowCount = source.windowCount;
        this.initialized = source.initialized;
        this.lastUpdatedMs = source.lastUpdatedMs;
    }

    @Override
    public synchronized WldtHistogram copy() { return new WldtHistogram(this); }

    @Override
    public WldtHistogram emptySnapshot() { return new WldtHistogram(getNamespace(), getName(), getComponent()); }

    // --- Current-window accessors ---

    /** @return observation count in the most recent window */
    public synchronized long getCount()   { return count; }

    /** @return sum of observations in the most recent window */
    public synchronized double getSum()   { return sum; }

    /** @return minimum in the most recent window */
    public synchronized double getMin()   { return min; }

    /** @return maximum in the most recent window */
    public synchronized double getMax()   { return max; }

    /** @return arithmetic mean of the most recent window, or {@link Double#NaN} if uninitialized */
    public synchronized double getMean()  { return count == 0 ? Double.NaN : sum / count; }

    // --- Cumulative accessors ---

    /** @return total observation count across all windows */
    public synchronized long getTotalCount()    { return totalCount; }

    /** @return total sum across all windows */
    public synchronized double getTotalSum()    { return totalSum; }

    /** @return global minimum across all windows */
    public synchronized double getGlobalMin()   { return globalMin; }

    /** @return global maximum across all windows */
    public synchronized double getGlobalMax()   { return globalMax; }

    /** @return number of windows recorded (including initial construction) */
    public synchronized long getWindowCount()   { return windowCount; }

    /** @return global arithmetic mean, or {@link Double#NaN} if uninitialized */
    public synchronized double getGlobalMean()  { return totalCount == 0 ? Double.NaN : totalSum / totalCount; }

    private static void validateWindow(long count, double sum, double min, double max) {
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
    }

    @Override
    public String toString() {
        return "WldtHistogram{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", count=" + count +
                ", sum=" + sum +
                ", min=" + min +
                ", max=" + max +
                ", totalCount=" + totalCount +
                ", totalSum=" + totalSum +
                ", globalMin=" + globalMin +
                ", globalMax=" + globalMax +
                ", windowCount=" + windowCount +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

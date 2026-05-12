package it.wldt.monitoring.metrics;

import java.util.Map;

/**
 * A WLDT metric representing the duration of a measured operation in milliseconds.
 *
 * <p>Instances are mutable. The constructor records the first observation. Subsequent
 * observations are recorded via {@link #update(long)} or {@link #updateSince(long)}.
 * The metric accumulates min, max, total, and observation count across all recordings.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongHistogram} with unit {@code "ms"}.</p>
 * <p><strong>Prometheus mapping:</strong> {@code Histogram} or {@code Summary}.</p>
 */
public class WldtTimer extends WldtMetric {

    private long durationMs;
    private long minDurationMs;
    private long maxDurationMs;
    private long totalDurationMs;
    private long observationCount;

    /**
     * Constructs a new {@code WldtTimer} recording the first observation.
     * All cumulative fields ({@code min}, {@code max}, {@code total}) are initialized
     * from {@code initialDurationMs}; {@code observationCount} starts at 1.
     *
     * @param namespace         the logical namespace grouping this metric
     * @param name              the metric name within its namespace
     * @param component         the DT component that emitted this metric
     * @param initialDurationMs the first measured duration in milliseconds; must be &ge; 0
     * @throws IllegalArgumentException if namespace, name, or component is null/blank,
     *                                  or if initialDurationMs is negative
     */
    /**
     * Constructs an uninitialized {@code WldtTimer} for pre-registration.
     * {@link #isInitialized()} returns {@code false} until the first {@link #update(long)}.
     * All duration and count fields are 0 and must not be used before initialization.
     */
    public WldtTimer(String digitalTwinId, String namespace, String name, WldtMetricComponent component) {
        super(digitalTwinId, namespace, name, component);
        this.durationMs       = 0;
        this.minDurationMs    = 0;
        this.maxDurationMs    = 0;
        this.totalDurationMs  = 0;
        this.observationCount = 0;
        this.initialized      = false;
    }

    public WldtTimer(String digitalTwinId, String namespace, String name, WldtMetricComponent component, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component,  metadata);
        this.durationMs       = 0;
        this.minDurationMs    = 0;
        this.maxDurationMs    = 0;
        this.totalDurationMs  = 0;
        this.observationCount = 0;
        this.initialized      = false;
    }

    public WldtTimer(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialDurationMs) {
        super(digitalTwinId, namespace, name, component);
        if (initialDurationMs < 0)
            throw new IllegalArgumentException("WldtTimer durationMs must be non-negative, got: " + initialDurationMs);
        this.durationMs       = initialDurationMs;
        this.minDurationMs    = initialDurationMs;
        this.maxDurationMs    = initialDurationMs;
        this.totalDurationMs  = initialDurationMs;
        this.observationCount = 1;
        this.initialized      = true;
    }

    public WldtTimer(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialDurationMs, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component,  metadata);
        if (initialDurationMs < 0)
            throw new IllegalArgumentException("WldtTimer durationMs must be non-negative, got: " + initialDurationMs);
        this.durationMs       = initialDurationMs;
        this.minDurationMs    = initialDurationMs;
        this.maxDurationMs    = initialDurationMs;
        this.totalDurationMs  = initialDurationMs;
        this.observationCount = 1;
        this.initialized      = true;
    }

    /**
     * Records a new duration observation, updating min/max/total and observation count.
     *
     * @param durationMs new measured duration in milliseconds; must be &ge; 0
     * @throws IllegalArgumentException if durationMs is negative
     */
    public synchronized WldtTimer update(long durationMs) {
        if (durationMs < 0)
            throw new IllegalArgumentException("WldtTimer update durationMs must be non-negative, got: " + durationMs);
        if (!initialized) {
            this.durationMs       = durationMs;
            this.minDurationMs    = durationMs;
            this.maxDurationMs    = durationMs;
            this.totalDurationMs  = durationMs;
            this.observationCount = 1;
            this.initialized      = true;
            this.lastUpdatedMs    = System.currentTimeMillis();
            return this;
        }
        this.durationMs       = durationMs;
        this.minDurationMs    = Math.min(this.minDurationMs, durationMs);
        this.maxDurationMs    = Math.max(this.maxDurationMs, durationMs);
        this.totalDurationMs += durationMs;
        this.observationCount++;
        this.lastUpdatedMs    = System.currentTimeMillis();
        return this;
    }

    /**
     * Records an observation by computing elapsed time since {@code startMs}.
     * Equivalent to {@code update(System.currentTimeMillis() - startMs)}.
     *
     * @param startMs epoch milliseconds when the operation started
     * @throws IllegalArgumentException if computed duration is negative (startMs in the future)
     */
    public synchronized WldtTimer updateSince(long startMs) {
        return update(System.currentTimeMillis() - startMs);
    }

    private WldtTimer(WldtTimer source) {
        super(source.getDigitalTwinId(), source.getNamespace(), source.getName(), source.getComponent(), source.getMetadata());
        this.durationMs       = source.durationMs;
        this.minDurationMs    = source.minDurationMs;
        this.maxDurationMs    = source.maxDurationMs;
        this.totalDurationMs  = source.totalDurationMs;
        this.observationCount = source.observationCount;
        this.initialized      = source.initialized;
        this.lastUpdatedMs    = source.lastUpdatedMs;
    }

    @Override
    public synchronized WldtTimer copy() { return new WldtTimer(this); }

    @Override
    public WldtTimer emptySnapshot() { return new WldtTimer(getDigitalTwinId(), getNamespace(), getName(), getComponent(), getMetadata()); }

    /** @return the most recent recorded duration in milliseconds */
    public synchronized long getDurationMs()        { return durationMs; }

    /** @return the most recent duration in seconds */
    public synchronized double getDurationSeconds() { return durationMs / 1000.0; }

    /** @return minimum duration ever recorded */
    public synchronized long getMinDurationMs()     { return minDurationMs; }

    /** @return maximum duration ever recorded */
    public synchronized long getMaxDurationMs()     { return maxDurationMs; }

    /** @return sum of all recorded durations */
    public synchronized long getTotalDurationMs()   { return totalDurationMs; }

    /** @return number of observations recorded (including initial construction) */
    public synchronized long getObservationCount()  { return observationCount; }

    /** @return arithmetic mean of all recorded durations, or {@link Double#NaN} if uninitialized */
    public synchronized double getMeanDurationMs()  {
        return observationCount == 0 ? Double.NaN : (double) totalDurationMs / observationCount;
    }

    @Override
    public String toString() {
        return "WldtTimer{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", durationMs=" + durationMs +
                ", minDurationMs=" + minDurationMs +
                ", maxDurationMs=" + maxDurationMs +
                ", totalDurationMs=" + totalDurationMs +
                ", observationCount=" + observationCount +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

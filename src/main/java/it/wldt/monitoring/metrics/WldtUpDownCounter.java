package it.wldt.monitoring.metrics;

import java.util.Map;

/**
 * A WLDT metric representing a counter for discrete entities that can increase or decrease.
 *
 * <p>Unlike {@link WldtCounter} (monotonically increasing), this counter accepts both positive
 * and negative deltas. Unlike {@link WldtGauge}, it models discrete entity counts rather than
 * continuously observed physical values.</p>
 *
 * <p>Instances are mutable. Use {@link #update(long)} to set an absolute value,
 * or {@link #increment()} / {@link #decrement()} to adjust by a step. The metric also tracks
 * {@link #getPeakValue()} and {@link #getTroughValue()} across all mutations.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongUpDownCounter}.</p>
 * <p><strong>Prometheus mapping:</strong> {@code Gauge} — use {@link #getDelta()} to drive
 * {@code Gauge.inc(delta)} or {@code Gauge.dec(Math.abs(delta))} based on sign.</p>
 */
public class WldtUpDownCounter extends WldtMetric {

    private long value;
    private Long delta;
    private long peakValue;
    private long troughValue;
    private long totalUpdates;

    /**
     * Constructs a new {@code WldtUpDownCounter} with the given initial value.
     * {@code peakValue} and {@code troughValue} are both initialized to {@code initialValue}.
     *
     * @param namespace    the logical namespace grouping this metric
     * @param name         the metric name within its namespace
     * @param component    the DT component that emitted this metric
     * @param initialValue the starting value; may be zero, positive, or negative
     * @throws IllegalArgumentException if namespace, name, or component is null/blank
     */
    /**
     * Constructs an uninitialized {@code WldtUpDownCounter} for pre-registration.
     * {@link #isInitialized()} returns {@code false} until the first mutation.
     */
    public WldtUpDownCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component) {
        super(digitalTwinId, namespace, name, component);
        this.value        = 0;
        this.delta        = null;
        this.peakValue    = 0;
        this.troughValue  = 0;
        this.totalUpdates = 0;
        this.initialized  = false;
    }

    public WldtUpDownCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component, metadata);
        this.value        = 0;
        this.delta        = null;
        this.peakValue    = 0;
        this.troughValue  = 0;
        this.totalUpdates = 0;
        this.initialized  = false;
    }

    public WldtUpDownCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialValue) {
        super(digitalTwinId, namespace, name, component);
        this.value        = initialValue;
        this.delta        = null;
        this.peakValue    = initialValue;
        this.troughValue  = initialValue;
        this.totalUpdates = 0;
        this.initialized  = true;
    }

    public WldtUpDownCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialValue, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component, metadata);
        this.value        = initialValue;
        this.delta        = null;
        this.peakValue    = initialValue;
        this.troughValue  = initialValue;
        this.totalUpdates = 0;
        this.initialized  = true;
    }

    /**
     * Sets the counter to a new absolute value and computes the signed delta.
     *
     * @param newAbsoluteValue new absolute value; may be any long
     */
    public synchronized WldtUpDownCounter update(long newAbsoluteValue) {
        if (!initialized) {
            this.value        = newAbsoluteValue;
            this.delta        = null;
            this.peakValue    = newAbsoluteValue;
            this.troughValue  = newAbsoluteValue;
            this.initialized  = true;
            this.lastUpdatedMs = System.currentTimeMillis();
            return this;
        }
        this.delta        = newAbsoluteValue - this.value;
        this.value        = newAbsoluteValue;
        this.peakValue    = Math.max(this.peakValue, newAbsoluteValue);
        this.troughValue  = Math.min(this.troughValue, newAbsoluteValue);
        this.totalUpdates++;
        this.lastUpdatedMs = System.currentTimeMillis();
        return this;
    }

    /** Increments the counter by 1. */
    public synchronized WldtUpDownCounter increment() { return increment(1L); }

    /**
     * Increments the counter by {@code amount}.
     *
     * @param amount positive step; must be &gt; 0
     * @throws IllegalArgumentException if amount &le; 0
     */
    public synchronized WldtUpDownCounter increment(long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("increment amount must be > 0, got: " + amount);
        return update(this.value + amount);
    }

    /** Decrements the counter by 1. */
    public synchronized WldtUpDownCounter decrement() { return decrement(1L); }

    /**
     * Decrements the counter by {@code amount}.
     *
     * @param amount positive step to subtract; must be &gt; 0
     * @throws IllegalArgumentException if amount &le; 0
     */
    public synchronized WldtUpDownCounter decrement(long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("decrement amount must be > 0, got: " + amount);
        return update(this.value - amount);
    }

    private WldtUpDownCounter(WldtUpDownCounter source) {
        super(source.getDigitalTwinId(), source.getNamespace(), source.getName(), source.getComponent(),  source.getMetadata());
        this.value        = source.value;
        this.delta        = source.delta;
        this.peakValue    = source.peakValue;
        this.troughValue  = source.troughValue;
        this.totalUpdates = source.totalUpdates;
        this.initialized  = source.initialized;
        this.lastUpdatedMs = source.lastUpdatedMs;
        source.copyInstanceIdTo(this);
    }

    @Override
    public synchronized WldtUpDownCounter copy() { return new WldtUpDownCounter(this); }

    @Override
    public WldtUpDownCounter emptySnapshot() {
        WldtUpDownCounter s = new WldtUpDownCounter(getDigitalTwinId(), getNamespace(), getName(), getComponent(), getMetadata());
        copyInstanceIdTo(s);
        return s;
    }

    /** @return current absolute counter value */
    public synchronized long getValue()          { return value; }

    /** @return signed delta from the previous value, or {@code null} before first mutation */
    public synchronized Long getDelta()          { return delta; }

    /** @return {@code true} if at least one mutation has occurred */
    public synchronized boolean isDeltaAvailable() { return delta != null; }

    /** @return highest value ever set (initialized to initialValue) */
    public synchronized long getPeakValue()      { return peakValue; }

    /** @return lowest value ever set (initialized to initialValue) */
    public synchronized long getTroughValue()    { return troughValue; }

    /** @return total number of times this metric has been mutated */
    public synchronized long getTotalUpdates()   { return totalUpdates; }

    @Override
    public String toString() {
        return "WldtUpDownCounter{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", value=" + value +
                ", delta=" + (delta != null ? delta : "n/a") +
                ", peakValue=" + peakValue +
                ", troughValue=" + troughValue +
                ", totalUpdates=" + totalUpdates +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

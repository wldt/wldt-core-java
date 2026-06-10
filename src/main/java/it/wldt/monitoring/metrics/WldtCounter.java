package it.wldt.monitoring.metrics;

import java.util.Map;

/**
 * A WLDT metric representing a monotonically increasing counter.
 *
 * <p>Instances are mutable and registered once. Call {@link #update(long)} to set a
 * new absolute value, or {@link #increment()} / {@link #increment(long)} to add to the
 * current value. The {@code delta} field is computed automatically on each mutation and
 * is {@code null} until the first mutation occurs.</p>
 *
 * <p><strong>Prometheus mapping:</strong> {@code Counter} — use {@link #getDelta()} to
 * drive {@code Counter.inc(delta)} without external tracking state.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code LongCounter} / {@code DoubleCounter}.</p>
 */
public class WldtCounter extends WldtMetric {

    private long value;
    private Long delta;
    private long totalIncrements;

    /**
     * Constructs a new {@code WldtCounter} with the given initial value.
     * {@code delta} is {@code null} and {@code totalIncrements} is {@code 0} until the
     * first mutation.
     *
     * @param namespace    the logical namespace grouping this metric
     * @param name         the metric name within its namespace
     * @param component    the DT component that emitted this metric
     * @param initialValue the starting cumulative counter value; must be non-negative
     * @throws IllegalArgumentException if namespace, name, or component is null/blank,
     *                                  or if initialValue is negative
     */
    /**
     * Constructs an uninitialized {@code WldtCounter} for pre-registration.
     * No value is recorded; {@link #isInitialized()} returns {@code false} until the
     * first mutation. Stats (delta, totalIncrements) are meaningless until initialized.
     */
    public WldtCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component) {
        super(digitalTwinId, namespace, name, component);
        this.value           = 0;
        this.delta           = null;
        this.totalIncrements = 0;
        this.initialized     = false;
    }

    public WldtCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component,  metadata);
        this.value           = 0;
        this.delta           = null;
        this.totalIncrements = 0;
        this.initialized     = false;
    }

    public WldtCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialValue) {
        super(digitalTwinId, namespace, name, component);
        if (initialValue < 0)
            throw new IllegalArgumentException("WldtCounter initialValue must be non-negative, got: " + initialValue);
        this.value           = initialValue;
        this.delta           = null;
        this.totalIncrements = 0;
        this.initialized     = true;
    }

    public WldtCounter(String digitalTwinId, String namespace, String name, WldtMetricComponent component, long initialValue, Map<String, Object> metadata) {
        super(digitalTwinId, namespace, name, component,  metadata);
        if (initialValue < 0)
            throw new IllegalArgumentException("WldtCounter initialValue must be non-negative, got: " + initialValue);
        this.value           = initialValue;
        this.delta           = null;
        this.totalIncrements = 0;
        this.initialized     = true;
    }

    /**
     * Sets the counter to a new absolute value and computes the delta.
     * {@code newAbsoluteValue} must be &ge; current value (monotonically increasing).
     *
     * @param newAbsoluteValue new cumulative counter value; must be &ge; current value
     * @throws IllegalArgumentException if newAbsoluteValue is less than current value
     */
    public synchronized WldtCounter update(long newAbsoluteValue) {
        if (!initialized) {
            if (newAbsoluteValue < 0)
                throw new IllegalArgumentException("WldtCounter value must be non-negative, got: " + newAbsoluteValue);
            this.value         = newAbsoluteValue;
            this.delta         = null;
            this.initialized   = true;
            this.lastUpdatedMs = System.currentTimeMillis();
            return this;
        }
        if (newAbsoluteValue < this.value)
            throw new IllegalArgumentException(
                    "WldtCounter update value must be >= current value (" + this.value + "), got: " + newAbsoluteValue);
        this.delta           = newAbsoluteValue - this.value;
        this.value           = newAbsoluteValue;
        this.totalIncrements++;
        this.lastUpdatedMs   = System.currentTimeMillis();
        return this;
    }

    /**
     * Increments the counter by 1.
     */
    public synchronized WldtCounter increment() {
        return increment(1L);
    }

    /**
     * Increments the counter by {@code amount}.
     *
     * @param amount positive increment; must be &gt; 0
     * @throws IllegalArgumentException if amount &le; 0
     */
    public synchronized WldtCounter increment(long amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("WldtCounter increment amount must be > 0, got: " + amount);
        this.delta           = amount;
        this.value          += amount;
        this.totalIncrements++;
        this.initialized     = true;
        this.lastUpdatedMs   = System.currentTimeMillis();
        return this;
    }

    private WldtCounter(WldtCounter source) {
        super(source.getDigitalTwinId(), source.getNamespace(), source.getName(), source.getComponent(), source.getMetadata());
        this.value           = source.value;
        this.delta           = source.delta;
        this.totalIncrements = source.totalIncrements;
        this.initialized     = source.initialized;
        this.lastUpdatedMs   = source.lastUpdatedMs;
        source.copyInstanceIdTo(this);
    }

    @Override
    public synchronized WldtCounter copy() { return new WldtCounter(this); }

    @Override
    public WldtCounter emptySnapshot() {
        WldtCounter s = new WldtCounter(getDigitalTwinId(), getNamespace(), getName(), getComponent(), getMetadata());
        copyInstanceIdTo(s);
        return s;
    }

    /** @return current cumulative counter value */
    public synchronized long getValue() { return value; }

    /**
     * @return delta from the previous value, or {@code null} if no mutation has occurred yet
     */
    public synchronized Long getDelta() { return delta; }

    /** @return {@code true} if at least one mutation has occurred */
    public synchronized boolean isDeltaAvailable() { return delta != null; }

    /** @return total number of times this metric has been mutated */
    public synchronized long getTotalIncrements() { return totalIncrements; }

    @Override
    public String toString() {
        return "WldtCounter{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", value=" + value +
                ", delta=" + (delta != null ? delta : "n/a") +
                ", totalIncrements=" + totalIncrements +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

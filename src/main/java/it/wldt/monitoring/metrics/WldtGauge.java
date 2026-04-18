package it.wldt.monitoring.metrics;

/**
 * A WLDT metric representing a continuously observed numeric value that can freely rise and fall.
 *
 * <p>Instances are mutable. Call {@link #update(double)} to record a new observation. On each
 * update the metric tracks the previous value, the signed delta, and the running min/max across
 * all observations.</p>
 *
 * <p><strong>OpenTelemetry mapping:</strong> {@code DoubleGauge} / {@code ObservableGauge}.</p>
 * <p><strong>Prometheus mapping:</strong> {@code Gauge}.</p>
 */
public class WldtGauge extends WldtMetric {

    private double value;
    private Double previousValue;
    private Double delta;
    private double minObserved;
    private double maxObserved;
    private long   updateCount;

    /**
     * Constructs a new {@code WldtGauge} with the given initial value.
     * {@code minObserved} and {@code maxObserved} are initialized to {@code initialValue}.
     * {@code previousValue} and {@code delta} are {@code null} until the first {@link #update(double)}.
     *
     * @param namespace    the logical namespace grouping this metric
     * @param name         the metric name within its namespace
     * @param component    the DT component that emitted this metric
     * @param initialValue the starting observed value; must be finite
     * @throws IllegalArgumentException if namespace, name, or component is null/blank,
     *                                  or if initialValue is NaN or infinite
     */
    /**
     * Constructs an uninitialized {@code WldtGauge} for pre-registration.
     * {@link #isInitialized()} returns {@code false} until the first {@link #update(double)}.
     */
    public WldtGauge(String digitalTwinId, String namespace, String name, WldtMetricComponent component) {
        super(digitalTwinId, namespace, name, component);
        this.value         = 0.0;
        this.previousValue = null;
        this.delta         = null;
        this.minObserved   = 0.0;
        this.maxObserved   = 0.0;
        this.updateCount   = 0;
        this.initialized   = false;
    }

    public WldtGauge(String digitalTwinId, String namespace, String name, WldtMetricComponent component, double initialValue) {
        super(digitalTwinId, namespace, name, component);
        if (Double.isNaN(initialValue) || Double.isInfinite(initialValue))
            throw new IllegalArgumentException("WldtGauge value must be finite, got: " + initialValue);
        this.value         = initialValue;
        this.previousValue = null;
        this.delta         = null;
        this.minObserved   = initialValue;
        this.maxObserved   = initialValue;
        this.updateCount   = 0;
        this.initialized   = true;
    }

    /**
     * Records a new observed value, updating the delta, previous value, and min/max tracking.
     *
     * @param newValue the new observed value; must be finite
     * @throws IllegalArgumentException if newValue is NaN or infinite
     */
    public synchronized WldtGauge update(double newValue) {
        if (Double.isNaN(newValue) || Double.isInfinite(newValue))
            throw new IllegalArgumentException("WldtGauge update value must be finite, got: " + newValue);
        if (!initialized) {
            this.value         = newValue;
            this.previousValue = null;
            this.delta         = null;
            this.minObserved   = newValue;
            this.maxObserved   = newValue;
            this.initialized   = true;
            this.lastUpdatedMs = System.currentTimeMillis();
            return this;
        }
        this.previousValue = this.value;
        this.delta         = newValue - this.value;
        this.value         = newValue;
        this.minObserved   = Math.min(this.minObserved, newValue);
        this.maxObserved   = Math.max(this.maxObserved, newValue);
        this.updateCount++;
        this.lastUpdatedMs = System.currentTimeMillis();
        return this;
    }

    private WldtGauge(WldtGauge source) {
        super(source.getDigitalTwinId(), source.getNamespace(), source.getName(), source.getComponent());
        this.value         = source.value;
        this.previousValue = source.previousValue;
        this.delta         = source.delta;
        this.minObserved   = source.minObserved;
        this.maxObserved   = source.maxObserved;
        this.updateCount   = source.updateCount;
        this.initialized   = source.initialized;
        this.lastUpdatedMs = source.lastUpdatedMs;
    }

    @Override
    public synchronized WldtGauge copy() { return new WldtGauge(this); }

    @Override
    public WldtGauge emptySnapshot() { return new WldtGauge(getDigitalTwinId(), getNamespace(), getName(), getComponent()); }

    /** @return current observed value */
    public synchronized double getValue()          { return value; }

    /** @return the value before the most recent update, or {@code null} if no update has occurred */
    public synchronized Double getPreviousValue()  { return previousValue; }

    /** @return signed difference (current - previous), or {@code null} before first update */
    public synchronized Double getDelta()          { return delta; }

    /** @return minimum value ever observed (including initial value) */
    public synchronized double getMinObserved()    { return minObserved; }

    /** @return maximum value ever observed (including initial value) */
    public synchronized double getMaxObserved()    { return maxObserved; }

    /** @return number of times {@link #update(double)} has been called */
    public synchronized long getUpdateCount()      { return updateCount; }

    @Override
    public String toString() {
        return "WldtGauge{" +
                "namespace='" + getNamespace() + '\'' +
                ", name='" + getName() + '\'' +
                ", component=" + getComponent() +
                ", value=" + value +
                ", previousValue=" + previousValue +
                ", delta=" + delta +
                ", minObserved=" + minObserved +
                ", maxObserved=" + maxObserved +
                ", updateCount=" + updateCount +
                ", lastUpdatedMs=" + lastUpdatedMs +
                '}';
    }
}

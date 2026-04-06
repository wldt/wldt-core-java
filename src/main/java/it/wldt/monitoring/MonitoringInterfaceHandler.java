package it.wldt.monitoring;

import it.wldt.monitoring.metrics.WldtMetric;
import it.wldt.monitoring.metrics.WldtMetricComponent;

/**
 * Abstract base class that developers extend to react to metric events from the WLDT monitoring system.
 *
 * <p>Two callbacks are provided:</p>
 * <ul>
 *   <li>{@link #onMetricRegistered(WldtMetricComponent, WldtMetric)} — fires once when a metric name
 *       is seen for the first time (first {@code notifyMetric()} call for that name).</li>
 *   <li>{@link #onMetricUpdated(WldtMetricComponent, WldtMetric)} — fires on every subsequent call
 *       for the same metric name. The {@link WldtMetric} parameter is a <em>snapshot copy</em> of
 *       the live stored instance taken at callback time — mutations to the registry after the callback
 *       do not affect the object received here.</li>
 * </ul>
 *
 * <p>The {@code component} parameter indicates which DT component emitted the metric.
 * Developers switch on it and use {@code instanceof} on the metric to access typed fields:</p>
 *
 * <pre>{@code
 * public class MyHandler extends MonitoringInterfaceHandler {
 *
 *     @Override
 *     public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
 *         if (component == WldtMetricComponent.DT_MODEL && metric instanceof WldtCounter) {
 *             WldtCounter c = (WldtCounter) metric;
 *             if (c.isDeltaAvailable())
 *                 prometheusCounter.inc(c.getDelta());
 *         }
 *         if (metric instanceof WldtTimer) {
 *             WldtTimer t = (WldtTimer) metric;
 *             prometheusHistogram.observe(t.getDurationSeconds());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Thread safety:</strong> callbacks may be invoked concurrently from multiple DT
 * component threads. Implementations must synchronize any shared mutable state.</p>
 */
public abstract class MonitoringInterfaceHandler {

    /**
     * Called the first time a metric with a given name is observed (registration event).
     * The metric is the live stored instance with its initial value.
     *
     * @param component the DT component that emitted the metric; never null
     * @param metric    the newly registered metric instance; never null
     */
    public void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {}

    /**
     * Called on every subsequent push for an already-registered metric (update event).
     * The metric is a <em>snapshot copy</em> of the live stored instance taken immediately
     * after mutation — supporting fields (delta, min/max, counts) reflect the full history
     * up to this point. The snapshot is independent: further mutations to the registry do
     * not affect the object passed here.
     *
     * @param component the DT component that emitted the metric; never null
     * @param metric    snapshot of the metric after mutation; never null
     */
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {}
}

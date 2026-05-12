package it.wldt.monitoring.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal registry that tracks all active metrics within a {@code MonitoringInterface}.
 *
 * <p>Each metric is registered once via {@link #register(WldtMetric)} and then updated
 * in-place via {@link #update(WldtMetric)}. The stored instance is the single live object
 * that accumulates history (delta, min/max, counts, etc.) across all pushes. Callers
 * must register a metric before updating it.</p>
 *
 * <p><strong>Thread safety:</strong> the internal store is a {@link ConcurrentHashMap};
 * individual metric mutations are synchronized within each metric class.</p>
 */
public class WldtMetricRegistry {

    private final Map<String, WldtMetric> store = new ConcurrentHashMap<>();

    /**
     * Registers a metric in the registry as the live instance for its full name.
     * If a metric with the same full name is already registered, it is replaced.
     *
     * @param metric the metric to register; must not be null
     * @throws IllegalArgumentException if metric is null
     */
    public void register(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Cannot register a null metric");
        store.put(metric.getFullName(), metric);
    }

    /**
     * Updates the live stored metric for the incoming metric's full name.
     *
     * <p>The incoming metric acts as a value carrier: its measurement fields are
     * extracted and applied to the stored instance via the appropriate typed mutation
     * method. The stored instance (now mutated) is returned for dispatch to the handler.</p>
     *
     * <p>Dispatch by type:</p>
     * <ul>
     *   <li>{@link WldtCounter}       → {@code stored.update(incoming.getValue())}</li>
     *   <li>{@link WldtUpDownCounter} → {@code stored.update(incoming.getValue())}</li>
     *   <li>{@link WldtGauge}         → {@code stored.update(incoming.getValue())}</li>
     *   <li>{@link WldtTimer}         → {@code stored.update(incoming.getDurationMs())}</li>
     *   <li>{@link WldtHistogram}     → {@code stored.update(count, sum, min, max)}</li>
     * </ul>
     *
     * @param incoming the metric carrying the new value; must not be null
     * @return the mutated live stored instance
     * @throws IllegalArgumentException if incoming is null
     * @throws IllegalStateException    if no metric with this full name has been registered
     */
    public WldtMetric update(WldtMetric incoming) {
        if (incoming == null)
            throw new IllegalArgumentException("Cannot update with a null metric");

        String key = incoming.getFullName();
        WldtMetric stored = store.get(key);
        if (stored == null)
            throw new IllegalStateException(
                    "Metric '" + key + "' is not registered. Call register() before update().");

        if (stored instanceof WldtCounter && incoming instanceof WldtCounter)
            ((WldtCounter) stored).update(((WldtCounter) incoming).getValue());
        else if (stored instanceof WldtUpDownCounter && incoming instanceof WldtUpDownCounter)
            ((WldtUpDownCounter) stored).update(((WldtUpDownCounter) incoming).getValue());
        else if (stored instanceof WldtGauge && incoming instanceof WldtGauge)
            ((WldtGauge) stored).update(((WldtGauge) incoming).getValue());
        else if (stored instanceof WldtTimer && incoming instanceof WldtTimer)
            ((WldtTimer) stored).update(((WldtTimer) incoming).getDurationMs());
        else if (stored instanceof WldtHistogram && incoming instanceof WldtHistogram) {
            WldtHistogram h = (WldtHistogram) incoming;
            ((WldtHistogram) stored).update(h.getCount(), h.getSum(), h.getMin(), h.getMax());
        } else {
            throw new IllegalArgumentException(
                    "Type mismatch: registered metric is " + stored.getClass().getSimpleName() +
                    " but incoming is " + incoming.getClass().getSimpleName());
        }

        return stored;
    }

    /**
     * Removes a metric from the registry by its full name.
     * After deregistration the next {@link #register(WldtMetric)} call resets delta tracking.
     *
     * @param fullName the full metric name ({@code namespace.name}); must not be null or blank
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public void deregister(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        store.remove(fullName);
    }

    /**
     * Returns the live stored metric for the given full name.
     *
     * @param fullName the full metric name; must not be null or blank
     * @return an {@link Optional} containing the stored metric, or empty if not registered
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Optional<WldtMetric> getMetric(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        return Optional.ofNullable(store.get(fullName));
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered metrics.
     *
     * @return unmodifiable map of full metric names to live metric instances
     */
    public Map<String, WldtMetric> getAllMetrics() {
        return Collections.unmodifiableMap(store);
    }

    /**
     * Returns {@code true} if a metric with the given full name is currently registered.
     *
     * @param fullName the full metric name; must not be null or blank
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public boolean isRegistered(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        return store.containsKey(fullName);
    }

    /** @return number of metrics currently registered */
    public int size() { return store.size(); }

    /**
     * Registers a metric only if no metric with the same full name is already registered.
     * This operation is atomic (backed by {@link java.util.concurrent.ConcurrentHashMap#putIfAbsent}).
     *
     * @param metric the metric to register; must not be null
     * @return {@code true} if the metric was newly registered, {@code false} if already present
     * @throws IllegalArgumentException if metric is null
     */
    public boolean registerIfAbsent(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Cannot register a null metric");
        return store.putIfAbsent(metric.getFullName(), metric) == null;
    }

    /** Removes all registered metrics. */
    public void clear() { store.clear(); }
}

package it.wldt.monitoring.metrics;

import it.wldt.monitoring.WldtMonitoringHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal registry that tracks all active metrics within a {@code MonitoringInterface}.
 *
 * <p>The {@code WldtMetricRegistry} serves two purposes:</p>
 * <ol>
 *   <li><strong>Delta computation</strong> — for {@link WldtCounter} and
 *       {@link WldtUpDownCounter}, the registry stores the last known absolute
 *       value and computes the delta before the metric is dispatched to the
 *       developer's {@link WldtMonitoringHandler}. This relieves developers
 *       from maintaining their own tracking state.</li>
 *   <li><strong>Query support</strong> — developers can interrogate the current
 *       registered value of any metric by its full name ({@code namespace.name})
 *       via {@link #getMetric(String)} and {@link #getAllMetrics()}.</li>
 * </ol>
 *
 * <p><strong>Registration policy:</strong> metrics are registered lazily on the
 * first call to {@link #computeAndRegister(WldtMetric)}. There is no need to
 * pre-declare metrics — they are auto-registered on their first push. Library
 * components that want explicit upfront registration can call
 * {@link #register(WldtMetric)} directly.</p>
 *
 * <p><strong>Thread safety:</strong> the internal store is a
 * {@link ConcurrentHashMap}, making all read and write operations safe for
 * concurrent access from multiple DT component threads.</p>
 */
public class WldtMetricRegistry {

    /**
     * Internal store mapping each metric's full name ({@code namespace.name})
     * to its last known {@link WldtMetric} instance.
     * ConcurrentHashMap ensures thread-safe access without external synchronization.
     */
    private final Map<String, WldtMetric> store = new ConcurrentHashMap<>();

    /**
     * Registers a metric in the registry without triggering a push notification.
     *
     * <p>If a metric with the same full name is already registered, the existing
     * entry is replaced with the new one. This method is typically called at DT
     * startup by the library to pre-register internal metrics, or by the developer
     * to pre-declare custom metrics before the first push.</p>
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
     * Removes a previously registered metric from the registry by its full name.
     *
     * <p>If no metric with the given full name exists, this method does nothing.
     * After deregistration, the next push for this metric name will be treated
     * as a first push — delta will be {@code null}.</p>
     *
     * @param fullName the full metric name in the form {@code namespace.name};
     *                 must not be null or blank
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public void deregister(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        store.remove(fullName);
    }

    /**
     * Processes an incoming metric, computes the delta if applicable, updates
     * the registry, and returns the enriched metric ready for dispatch.
     *
     * <p>The enrichment logic is as follows:</p>
     * <ul>
     *   <li>{@link WldtCounter} — delta is computed as
     *       {@code newValue - previousValue} if a previous entry exists,
     *       or {@code null} on the first push. The returned instance is a
     *       new {@code WldtCounter} with the delta injected via
     *       {@link WldtCounter#withDelta(long)}.</li>
     *   <li>{@link WldtUpDownCounter} — same logic, but delta may be negative
     *       when the count decreased.</li>
     *   <li>All other types ({@link WldtGauge}, {@link WldtTimer},
     *       {@link WldtHistogram}) — no delta is computed; the registry entry
     *       is updated and the metric is returned unchanged.</li>
     * </ul>
     *
     * <p>If the metric is not yet in the registry (first push), it is
     * auto-registered (lazy registration) before the enriched instance
     * is returned.</p>
     *
     * @param incoming the raw metric received from a DT component; must not be null
     * @return the enriched metric to dispatch to the handler — may be a new
     *         instance (for counters) or the same instance (for other types)
     * @throws IllegalArgumentException if incoming is null
     */
    public WldtMetric computeAndRegister(WldtMetric incoming) {
        if (incoming == null)
            throw new IllegalArgumentException("Cannot process a null metric");

        String key = incoming.getFullName();
        WldtMetric previous = store.get(key);

        WldtMetric enriched = enrich(incoming, previous);

        // Update the registry with the latest raw incoming value
        // (not the enriched copy, to keep the stored value clean)
        store.put(key, incoming);

        return enriched;
    }

    /**
     * Returns the last registered {@link WldtMetric} instance for the given
     * full name, if present.
     *
     * <p>The full name is in the form {@code namespace.name}, e.g.
     * {@code "wldt.internal.dt_model.events_processed"}.</p>
     *
     * @param fullName the full metric name to look up; must not be null or blank
     * @return an {@link Optional} containing the last known metric, or empty if
     *         no metric with that name has been registered yet
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Optional<WldtMetric> getMetric(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        return Optional.ofNullable(store.get(fullName));
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered metrics,
     * keyed by their full name ({@code namespace.name}).
     *
     * <p>The returned map is a live unmodifiable view — its contents reflect
     * the registry state at the time of the call. Modifications to the registry
     * after this call are not reflected in the returned map.</p>
     *
     * @return unmodifiable map of full metric names to their last known instances
     */
    public Map<String, WldtMetric> getAllMetrics() {
        return Collections.unmodifiableMap(store);
    }

    /**
     * Returns the number of metrics currently registered in this registry.
     *
     * @return the count of registered metrics
     */
    public int size() {
        return store.size();
    }

    /**
     * Returns {@code true} if a metric with the given full name is currently
     * registered in this registry.
     *
     * @param fullName the full metric name to check; must not be null or blank
     * @return {@code true} if registered, {@code false} otherwise
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public boolean isRegistered(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        return store.containsKey(fullName);
    }

    /**
     * Removes all registered metrics from this registry.
     * After this call the registry is empty and all subsequent pushes
     * will be treated as first pushes (delta will be {@code null}).
     */
    public void clear() {
        store.clear();
    }

    /**
     * Enriches an incoming metric with delta information by comparing it to
     * the previously stored value. Returns the enriched metric instance.
     *
     * <p>Only {@link WldtCounter} and {@link WldtUpDownCounter} are enriched
     * with a delta. All other types are returned unchanged.</p>
     *
     * @param incoming the new metric value
     * @param previous the previously stored metric value, or {@code null} if
     *                 this is the first push for this metric name
     * @return the enriched metric (a new instance for counters, the same
     *         instance for other types)
     */
    private WldtMetric enrich(WldtMetric incoming, WldtMetric previous) {

        // WldtCounter — compute non-negative delta from previous absolute value
        if (incoming instanceof WldtCounter) {
            WldtCounter newCounter = (WldtCounter) incoming;
            if (previous instanceof WldtCounter) {
                WldtCounter prevCounter = (WldtCounter) previous;
                long delta = newCounter.getValue() - prevCounter.getValue();
                return newCounter.withDelta(Math.max(delta, 0));
            }
            // First push — delta not yet available, return as-is (delta=null)
            return incoming;
        }

        // WldtUpDownCounter — compute signed delta from previous absolute value
        if (incoming instanceof WldtUpDownCounter) {
            WldtUpDownCounter newCounter = (WldtUpDownCounter) incoming;
            if (previous instanceof WldtUpDownCounter) {
                WldtUpDownCounter prevCounter = (WldtUpDownCounter) previous;
                long delta = newCounter.getValue() - prevCounter.getValue();
                return newCounter.withDelta(delta);
            }
            // First push — delta not yet available, return as-is (delta=null)
            return incoming;
        }

        // WldtGauge, WldtTimer, WldtHistogram — no delta applicable
        return incoming;
    }
}
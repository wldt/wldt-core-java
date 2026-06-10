package it.wldt.monitoring.metrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Internal registry that tracks all active metrics within a {@code MonitoringInterface}.
 *
 * <p>The registry uses a two-level structure: {@code Map<fullName, Map<instanceId, WldtMetric>>}.
 * The outer key is the metric's full name ({@code namespace.name}); the inner key is the
 * {@link WldtMetric#getInstanceId() instanceId} of the component instance that owns the metric,
 * or the sentinel {@value #SINGLETON_KEY} for singleton components (e.g. {@code DigitalTwinModel})
 * that have {@code instanceId == null}.</p>
 *
 * <p>This allows multiple component instances of the same type (e.g. several stateful augmentation
 * functions) to each maintain an independent live metric under the same full name, so the
 * {@code MonitoringInterfaceHandler} can distinguish them by instance while Prometheus exposes a
 * single labeled series.</p>
 *
 * <p><strong>Thread safety:</strong> both map levels use {@link ConcurrentHashMap}.</p>
 */
public class WldtMetricRegistry {

    /** Inner-key sentinel used when {@code WldtMetric.getInstanceId()} is {@code null}. */
    static final String SINGLETON_KEY = "";

    /**
     * Metric store, first key is the metric name, the second key is the instanceId of the component instance that owns the metric, or {@value #SINGLETON_KEY} for singleton components.
     */
    private final Map<String, Map<String, WldtMetric>> store = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private static String innerKey(WldtMetric metric) {
        String id = metric.getInstanceId();
        return id != null ? id : SINGLETON_KEY;
    }

    private static String innerKey(String instanceId) {
        return instanceId != null ? instanceId : SINGLETON_KEY;
    }

    // -------------------------------------------------------------------------
    // Core registration / update
    // -------------------------------------------------------------------------

    /**
     * Registers a metric as the live instance for its {@code (fullName, instanceId)} slot.
     * If an entry already exists for that slot it is replaced.
     *
     * @param metric the metric to register; must not be null
     * @throws IllegalArgumentException if metric is null
     */
    public void register(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Cannot register a null metric");
        store.computeIfAbsent(metric.getFullName(), k -> new ConcurrentHashMap<>())
             .put(innerKey(metric), metric);
    }

    /**
     * Registers a metric only if no entry with the same {@code (fullName, instanceId)} already exists.
     * This operation is atomic at the inner-map level.
     *
     * @param metric the metric to register; must not be null
     * @return {@code true} if newly registered, {@code false} if an entry already existed
     * @throws IllegalArgumentException if metric is null
     */
    public boolean registerIfAbsent(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Cannot register a null metric");
        Map<String, WldtMetric> inner =
                store.computeIfAbsent(metric.getFullName(), k -> new ConcurrentHashMap<>());
        return inner.putIfAbsent(innerKey(metric), metric) == null;
    }

    /**
     * Updates the live stored metric for the incoming metric's {@code (fullName, instanceId)} slot.
     *
     * <p>The incoming metric acts as a value carrier. Its measurement fields are applied to the
     * stored instance via the appropriate typed mutation method, and the mutated stored instance
     * is returned.</p>
     *
     * @param incoming the metric carrying the new value; must not be null
     * @return the mutated live stored instance
     * @throws IllegalArgumentException if incoming is null or types mismatch
     * @throws IllegalStateException    if no metric for this {@code (fullName, instanceId)} is registered
     */
    public WldtMetric update(WldtMetric incoming) {
        if (incoming == null)
            throw new IllegalArgumentException("Cannot update with a null metric");

        String fullName = incoming.getFullName();
        String ik       = innerKey(incoming);
        Map<String, WldtMetric> inner = store.get(fullName);
        if (inner == null)
            throw new IllegalStateException(
                    "Metric '" + fullName + "' is not registered. Call register() before update().");
        WldtMetric stored = inner.get(ik);
        if (stored == null)
            throw new IllegalStateException(
                    "Metric '" + fullName + "' instance '" + ik + "' is not registered.");

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

    // -------------------------------------------------------------------------
    // Deregistration
    // -------------------------------------------------------------------------

    /**
     * Removes ALL instances registered under {@code fullName} (backward-compatible behaviour).
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
     * Removes the specific {@code (fullName, instanceId)} entry.
     * If {@code instanceId} is {@code null} the singleton slot is removed.
     *
     * @param fullName   the full metric name; must not be null or blank
     * @param instanceId the instance id, or {@code null} for singletons
     */
    public void deregister(String fullName, String instanceId) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        Map<String, WldtMetric> inner = store.get(fullName);
        if (inner != null)
            inner.remove(innerKey(instanceId));
    }

    // -------------------------------------------------------------------------
    // Lookups
    // -------------------------------------------------------------------------

    /**
     * Returns the singleton live metric for {@code fullName} (backward-compatible).
     * Equivalent to {@link #getMetric(String, String) getMetric(fullName, null)}.
     *
     * @param fullName the full metric name; must not be null or blank
     * @return an {@link Optional} with the stored singleton metric, or empty
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Optional<WldtMetric> getMetric(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        Map<String, WldtMetric> inner = store.get(fullName);
        if (inner == null) return Optional.empty();
        return Optional.ofNullable(inner.get(SINGLETON_KEY));
    }

    /**
     * Returns the live metric for {@code (fullName, instanceId)}.
     * Pass {@code instanceId = null} to look up the singleton slot.
     *
     * @param fullName   the full metric name; must not be null or blank
     * @param instanceId the instance id, or {@code null} for singletons
     * @return an {@link Optional} with the stored metric, or empty
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public Optional<WldtMetric> getMetric(String fullName, String instanceId) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        Map<String, WldtMetric> inner = store.get(fullName);
        if (inner == null) return Optional.empty();
        return Optional.ofNullable(inner.get(innerKey(instanceId)));
    }

    /**
     * Returns an unmodifiable nested snapshot of all currently registered metrics.
     * The outer map key is the full metric name; the inner map key is the instance id
     * (or {@value #SINGLETON_KEY} for singletons).
     *
     * @return unmodifiable {@code Map<fullName, Map<instanceId, WldtMetric>>}
     */
    public Map<String, Map<String, WldtMetric>> getAllMetrics() {
        Map<String, Map<String, WldtMetric>> snapshot = new HashMap<>();
        for (Map.Entry<String, Map<String, WldtMetric>> e : store.entrySet())
            snapshot.put(e.getKey(), Collections.unmodifiableMap(e.getValue()));
        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Returns {@code true} if at least one instance is registered under {@code fullName}.
     *
     * @param fullName the full metric name; must not be null or blank
     * @throws IllegalArgumentException if fullName is null or blank
     */
    public boolean isRegistered(String fullName) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        Map<String, WldtMetric> inner = store.get(fullName);
        return inner != null && !inner.isEmpty();
    }

    /**
     * Returns {@code true} if the specific {@code (fullName, instanceId)} slot is registered.
     *
     * @param fullName   the full metric name; must not be null or blank
     * @param instanceId the instance id, or {@code null} for singletons
     */
    public boolean isRegistered(String fullName, String instanceId) {
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Metric full name must not be null or blank");
        Map<String, WldtMetric> inner = store.get(fullName);
        return inner != null && inner.containsKey(innerKey(instanceId));
    }

    /** @return total number of {@code (fullName, instanceId)} entries across all metrics */
    public int size() {
        int total = 0;
        for (Map<String, WldtMetric> inner : store.values())
            total += inner.size();
        return total;
    }

    /** Removes all registered metrics and instances. */
    public void clear() { store.clear(); }
}

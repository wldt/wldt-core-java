package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.metrics.*;

import java.util.Map;
import java.util.Optional;

/**
 * Central hub of the WLDT monitoring system.
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li><strong>Flag gating</strong> — silently discards metrics whose component flag is
 *       disabled in the {@link MonitoringInterfaceConfiguration}.</li>
 *   <li><strong>Registry management</strong> — maintains a {@link WldtMetricRegistry} that
 *       holds the live mutable metric instance for each registered name.</li>
 *   <li><strong>Registration vs update routing</strong> — the first push for a name calls
 *       {@link MonitoringInterfaceHandler#onMetricRegistered}; subsequent pushes call
 *       {@link MonitoringInterfaceHandler#onMetricUpdated} with the mutated live instance.</li>
 *   <li><strong>Custom metric support</strong> — {@link #trackCustomMetric(WldtMetric)} lets
 *       developers push their own metrics through the same pipeline, bypassing flag gating.</li>
 * </ol>
 *
 * <p>This class is {@code final} — the developer extension point is {@link MonitoringInterfaceHandler}.</p>
 */
public final class MonitoringInterface {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(MonitoringInterface.class);

    /**
     * Configuration of the Monitoring Interface
     */
    private MonitoringInterfaceConfiguration configuration;

    /**
     * Handler for the Monitoring Interface to process received metrics and fire appropriate callbacks
     */
    private MonitoringInterfaceHandler handler;

    /**
     * Metrics Registry to track and manage metrics registration across the different Digital Twin components
     */
    private final WldtMetricRegistry registry;

    public MonitoringInterface() {
        this.registry = new WldtMetricRegistry();
    }

    /**
     * Receives a metric from a DT component and routes it through the monitoring pipeline.
     *
     * <p>On first push for a given metric name: registers the metric instance and fires
     * {@link MonitoringInterfaceHandler#onMetricRegistered}.</p>
     * <p>On subsequent pushes: updates the live stored instance in-place and fires
     * {@link MonitoringInterfaceHandler#onMetricUpdated} with the mutated instance.</p>
     *
     * <p>If the component flag is disabled the metric is silently discarded.
     * Handler exceptions are caught and logged to prevent monitoring failures from
     * affecting DT processing.</p>
     *
     * @param metric the metric emitted by a DT component; silently ignored if null
     */
    public void notifyMetric(WldtMetric metric) {
        if (metric == null) {
            logger.warn("notifyMetric called with a null metric — discarding");
            return;
        }
        if (!isEnabled(metric.getComponent())) {
            logger.debug("Metric {} discarded — component {} monitoring is disabled",
                    metric.getFullName(), metric.getComponent());
            return;
        }
        try {
            dispatchMetric(metric);
        } catch (Exception e) {
            logger.error("Error dispatching metric {} from component {}: {}",
                    metric.getFullName(), metric.getComponent(), e.getMessage());
        }
    }

    /**
     * Pushes a custom developer metric through the monitoring pipeline, bypassing flag gating.
     * The metric's component must be {@link WldtMetricComponent#CUSTOM}.
     *
     * @param metric the custom metric; must not be null, component must be CUSTOM
     * @throws IllegalArgumentException if metric is null or its component is not CUSTOM
     */
    public void trackCustomMetric(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Custom metric must not be null");
        if (metric.getComponent() != WldtMetricComponent.CUSTOM)
            throw new IllegalArgumentException(
                    "trackCustomMetric requires component CUSTOM, got: " + metric.getComponent());
        try {
            dispatchMetric(metric);
        } catch (Exception e) {
            logger.error("Error dispatching custom metric {}: {}", metric.getFullName(), e.getMessage());
        }
    }

    /**
     * Returns the last registered metric for the given full name ({@code namespace.name}).
     *
     * @param fullName the full metric name
     * @return an {@link Optional} with the live metric, or empty if not yet registered
     */
    public Optional<WldtMetric> getMetric(String fullName) {
        return registry.getMetric(fullName);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered live metrics.
     *
     * @return unmodifiable map of full metric names to live instances
     */
    public Map<String, WldtMetric> getAllMetrics() {
        return registry.getAllMetrics();
    }

    /**
     * Returns {@code true} if a metric with the given full name is registered.
     *
     * @param fullName the full metric name
     */
    public boolean isMetricRegistered(String fullName) {
        return registry.isRegistered(fullName);
    }

    /**
     * Registers a new metric and fires {@link MonitoringInterfaceHandler#onMetricRegistered}.
     * Equivalent to calling {@link #notifyMetric(WldtMetric)} as the first push, but without
     * flag gating — use this at DT startup to declare metrics before measurements are available.
     *
     * @param metric the metric to register; must not be null
     */
    public void registerMetric(WldtMetric metric) {
        try {
            dispatchMetric(metric);
        } catch (Exception e) {
            logger.error("registerMetric: error registering '{}': {}",
                    metric != null ? metric.getFullName() : "null", e.getMessage());
        }
    }

    /**
     * Removes a metric from the registry by its full name.
     * After deregistration the next push for this name is treated as a fresh registration.
     *
     * @param fullName the full metric name to remove
     */
    public void deregisterMetric(String fullName) {
        registry.deregister(fullName);
    }

    public MonitoringInterfaceConfiguration getConfiguration() { return configuration; }
    public void setConfiguration(MonitoringInterfaceConfiguration configuration) { this.configuration = configuration; }

    public MonitoringInterfaceHandler getHandler() { return handler; }
    public void setHandler(MonitoringInterfaceHandler handler) { this.handler = handler; }

    public WldtMetricRegistry getRegistry() { return registry; }

    /** @return {@code true} if both configuration and handler are set */
    public boolean isActive() { return this.configuration != null && this.handler != null; }

    /** @return {@code true} if active and the given component's flag is enabled */
    public boolean isActive(WldtMetricComponent component) {
        return isEnabled(component) && this.configuration != null && this.handler != null;
    }

    // -------------------------------------------------------------------------
    // Convenience mutation methods
    // -------------------------------------------------------------------------

    /**
     * Increments a {@link WldtCounter} or {@link WldtUpDownCounter} by 1.
     * Looks up the metric by {@code namespace.name}, validates its type, applies the mutation,
     * and fires {@link MonitoringInterfaceHandler#onMetricUpdated}. All errors are logged internally.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     */
    public void increaseCounter(String namespace, String name) {
        increaseCounter(namespace, name, 1L);
    }

    /**
     * Increments a {@link WldtCounter} or {@link WldtUpDownCounter} by {@code amount}.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param amount    positive increment value
     */
    public void increaseCounter(String namespace, String name, long amount) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtCounter)
                notifyUpdated(((WldtCounter) metric).increment(amount));
            else if (metric instanceof WldtUpDownCounter)
                notifyUpdated(((WldtUpDownCounter) metric).increment(amount));
            else
                logger.error("increaseCounter: metric '{}.{}' is not a counter type (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("increaseCounter: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Sets a {@link WldtCounter} or {@link WldtUpDownCounter} to a new absolute value.
     * For {@link WldtCounter} the value must be &ge; the current value (monotonically increasing).
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param value     new absolute counter value; must be non-negative
     */
    public void updateCounter(String namespace, String name, long value) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtCounter)
                notifyUpdated(((WldtCounter) metric).update(value));
            else if (metric instanceof WldtUpDownCounter)
                notifyUpdated(((WldtUpDownCounter) metric).update(value));
            else
                logger.error("updateCounter: metric '{}.{}' is not a counter type (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("updateCounter: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Decrements a {@link WldtUpDownCounter} by 1.
     * {@link WldtCounter} does not support decrease — an error is logged if the wrong type is used.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     */
    public void decreaseCounter(String namespace, String name) {
        decreaseCounter(namespace, name, 1L);
    }

    /**
     * Decrements a {@link WldtUpDownCounter} by {@code amount}.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param amount    positive decrement value
     */
    public void decreaseCounter(String namespace, String name, long amount) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtUpDownCounter)
                notifyUpdated(((WldtUpDownCounter) metric).decrement(amount));
            else if (metric instanceof WldtCounter)
                logger.error("decreaseCounter: WldtCounter '{}.{}' is monotonically increasing — use WldtUpDownCounter to support decrements",
                        namespace, name);
            else
                logger.error("decreaseCounter: metric '{}.{}' is not a counter type (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("decreaseCounter: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Updates a {@link WldtGauge} to a new observed value.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param value     the new observed value; must be finite
     */
    public void updateGauge(String namespace, String name, double value) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtGauge)
                notifyUpdated(((WldtGauge) metric).update(value));
            else
                logger.error("updateGauge: metric '{}.{}' is not a WldtGauge (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("updateGauge: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Records a new duration observation on a {@link WldtTimer}.
     *
     * @param namespace  the metric namespace
     * @param name       the metric name within the namespace
     * @param durationMs measured duration in milliseconds; must be &ge; 0
     */
    public void updateTimer(String namespace, String name, long durationMs) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtTimer)
                notifyUpdated(((WldtTimer) metric).update(durationMs));
            else
                logger.error("updateTimer: metric '{}.{}' is not a WldtTimer (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("updateTimer: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Records a duration observation on a {@link WldtTimer} computed as
     * {@code System.currentTimeMillis() - startMs}.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param startMs   epoch milliseconds when the measured operation started
     */
    public void updateTimerSince(String namespace, String name, long startMs) {
        updateTimer(namespace, name, System.currentTimeMillis() - startMs);
    }

    /**
     * Adds a single observation to a {@link WldtHistogram}.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param value     the observed value; must be finite
     */
    public void histogramObservation(String namespace, String name, double value) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtHistogram)
                notifyUpdated(((WldtHistogram) metric).observe(value));
            else
                logger.error("histogramObservation: metric '{}.{}' is not a WldtHistogram (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("histogramObservation: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    /**
     * Merges a pre-aggregated window into a {@link WldtHistogram}.
     *
     * @param namespace the metric namespace
     * @param name      the metric name within the namespace
     * @param count     number of observations in the window; must be positive
     * @param sum       arithmetic sum of observations; must be finite
     * @param min       minimum observation; must be finite and &le; max
     * @param max       maximum observation; must be finite and &ge; min
     */
    public void histogramObservation(String namespace, String name,
                                     long count, double sum, double min, double max) {
        try {
            WldtMetric metric = resolveMetric(namespace, name);
            if (metric instanceof WldtHistogram)
                notifyUpdated(((WldtHistogram) metric).update(count, sum, min, max));
            else
                logger.error("histogramObservation: metric '{}.{}' is not a WldtHistogram (found {})",
                        namespace, name, metric.getClass().getSimpleName());
        } catch (Exception e) {
            logger.error("histogramObservation: error updating '{}.{}': {}", namespace, name, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void dispatchMetric(WldtMetric metric) {
        boolean isNew = registry.registerIfAbsent(metric);
        if (isNew) {
            if (handler != null) {
                handler.onMetricRegistered(metric.getComponent(), metric.emptySnapshot());
                if (metric.isInitialized())
                    handler.onMetricUpdated(metric.getComponent(), metric.copy());
            }
        } else {
            if (metric.isInitialized()) {
                WldtMetric live = registry.update(metric);
                if (handler != null)
                    handler.onMetricUpdated(live.getComponent(), live.copy());
            }
        }
    }

    /**
     * Looks up the live metric by {@code namespace.name}. Throws {@link IllegalStateException}
     * if not registered, so callers can catch it uniformly.
     */
    private WldtMetric resolveMetric(String namespace, String name) {
        String fullName = namespace + "." + name;
        return registry.getMetric(fullName).orElseThrow(() ->
                new IllegalStateException("Metric '" + fullName + "' is not registered"));
    }

    /**
     * Fires {@link MonitoringInterfaceHandler#onMetricUpdated} for the given already-mutated
     * live metric. Used by convenience methods that mutate the stored instance directly.
     */
    private void notifyUpdated(WldtMetric live) {
        if (handler != null)
            handler.onMetricUpdated(live.getComponent(), live.copy());
    }

    private boolean isEnabled(WldtMetricComponent component) {
        if (configuration == null) return false;
        switch (component) {
            case DT_MODEL:        return configuration.isDtModelMonitoringEnabled();
            case PHYSICAL_ADAPTER: return configuration.isPhysicalAdapterMonitoringEnabled();
            case DIGITAL_ADAPTER: return configuration.isDigitalAdapterMonitoringEnabled();
            case AUGMENTATION:    return configuration.isAugmentationMonitoringEnabled();
            case STORAGE:         return configuration.isStorageMonitoringEnabled();
            case CUSTOM:          return true;
            default:
                logger.warn("isEnabled() received unknown component: {}", component);
                return false;
        }
    }
}

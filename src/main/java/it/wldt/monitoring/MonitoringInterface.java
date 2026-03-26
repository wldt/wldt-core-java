package it.wldt.monitoring;

import it.wldt.log.WldtLogger;
import it.wldt.monitoring.metrics.*;

import java.util.Map;
import java.util.Optional;

/**
 * Concrete library class that serves as the central hub of the WLDT monitoring system.
 *
 * <p>{@code MonitoringInterface} is instantiated once by the developer and registered
 * on the {@code DigitalTwin} instance. Internally, the DT kernel injects it into all
 * active components (DT Model, Event Bus, Physical Adapters, Digital Adapters,
 * Augmentation Functions, Storage) so that each component can push metrics without
 * knowing about the developer's handler implementation.</p>
 *
 * <p>Responsibilities:</p>
 * <ol>
 *   <li><strong>Flag gating</strong> — checks {@link MonitoringConfiguration} before
 *       processing any incoming metric; silently discards metrics whose component
 *       flag is disabled.</li>
 *   <li><strong>Registry management</strong> — maintains a {@link WldtMetricRegistry}
 *       that tracks the last known value of every metric, performs lazy registration
 *       on first push, and exposes query methods to the developer.</li>
 *   <li><strong>Delta computation</strong> — for {@link WldtCounter} and
 *       {@link WldtUpDownCounter}, delegates to the registry to compute and inject
 *       the delta before dispatching the enriched metric to the handler.</li>
 *   <li><strong>Routing</strong> — dispatches the enriched metric to the correct
 *       typed callback on the developer's {@link WldtMonitoringHandler}.</li>
 *   <li><strong>Custom metric support</strong> — exposes {@link #trackCustomMetric(WldtMetric)}
 *       so that developers can push their own metrics through the same pipeline.</li>
 * </ol>
 *
 * <p>The {@link WldtLogger} composed into this class is used for internal monitoring
 * log messages (e.g. dispatch errors, flag-gated discards at DEBUG level). It is
 * separate from the application-level logger used by DT components.</p>
 *
 * <p>This class is declared {@code final} — it is not intended to be extended.
 * The developer extension point is {@link WldtMonitoringHandler}.</p>
 */
public final class MonitoringInterface {

    /** Monitoring configuration holding per-component enable flags and custom namespace. */
    private final MonitoringConfiguration configuration;

    /** Developer-provided handler that receives enriched metric push callbacks. */
    private final WldtMonitoringHandler handler;

    /** Logger used for internal monitoring system messages. */
    private final WldtLogger logger;

    /**
     * Internal registry tracking the last known value of every metric,
     * used for delta computation and developer query support.
     */
    private final WldtMetricRegistry registry;

    /**
     * Constructs a new {@code MonitoringInterface}.
     *
     * @param configuration monitoring flags and custom namespace configuration
     * @param handler       developer-provided handler that receives metric callbacks
     * @param logger        logger instance for internal monitoring log messages
     * @throws IllegalArgumentException if any parameter is null
     */
    public MonitoringInterface(MonitoringConfiguration configuration,
                               WldtMonitoringHandler handler,
                               WldtLogger logger) {
        if (configuration == null)
            throw new IllegalArgumentException("MonitoringConfiguration must not be null");
        if (handler == null)
            throw new IllegalArgumentException("WldtMonitoringHandler must not be null");
        if (logger == null)
            throw new IllegalArgumentException("WldtLogger must not be null");

        this.configuration = configuration;
        this.handler       = handler;
        this.logger        = logger;
        this.registry      = new WldtMetricRegistry();
    }

    /**
     * Receives a metric from an internal DT component, processes it through the
     * registry (lazy registration + delta computation), and dispatches the enriched
     * metric to the developer's handler if the component flag is enabled.
     *
     * <p>If the component flag for the metric's originating component is disabled
     * in the {@link MonitoringConfiguration}, the metric is silently discarded.
     * Any exception thrown by the handler callback is caught and logged at ERROR
     * level to prevent monitoring failures from propagating into DT processing.</p>
     *
     * @param metric the raw metric emitted by a DT component; must not be null
     */
    public void notifyMetric(WldtMetric metric) {
        if (metric == null) {
            logger.warn("notifyMetric called with a null metric — discarding");
            return;
        }

        // Check whether this component's monitoring is enabled
        if (!isEnabled(metric.getComponent())) {
            logger.debug("Metric {} discarded — component {} monitoring is disabled",
                    metric.getFullName(), metric.getComponent());
            return;
        }

        try {
            // Enrich with delta (for counters) and update registry
            WldtMetric enriched = registry.computeAndRegister(metric);

            // Dispatch enriched metric to the developer's handler
            route(enriched);

        } catch (Exception e) {
            logger.error("Error dispatching metric {} from component {}: {}",
                    metric.getFullName(), metric.getComponent(), e.getMessage());
        }
    }

    /**
     * Allows the developer to push a custom metric through the standard monitoring
     * pipeline. Custom metrics bypass per-component flag gating and are always
     * forwarded to {@link WldtMonitoringHandler#onCustomMetric(WldtMetric)}.
     *
     * <p>The metric's component must be set to {@link WldtMetricComponent#CUSTOM}.
     * The namespace should match the custom namespace configured in
     * {@link MonitoringConfiguration#getCustomMetricNamespace()}.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * monitoringInterface.trackCustomMetric(
     *     new WldtGauge("myapp.dt", "room.temperature",
     *                   WldtMetricComponent.CUSTOM, 21.5));
     * }</pre>
     *
     * @param metric the custom metric to push; must not be null and must have
     *               component {@link WldtMetricComponent#CUSTOM}
     * @throws IllegalArgumentException if metric is null or its component is not CUSTOM
     */
    public void trackCustomMetric(WldtMetric metric) {
        if (metric == null)
            throw new IllegalArgumentException("Custom metric must not be null");
        if (metric.getComponent() != WldtMetricComponent.CUSTOM)
            throw new IllegalArgumentException(
                    "trackCustomMetric requires component CUSTOM, got: " + metric.getComponent());
        try {
            WldtMetric enriched = registry.computeAndRegister(metric);
            handler.onCustomMetric(enriched);
        } catch (Exception e) {
            logger.error("Error dispatching custom metric {}: {}",
                    metric.getFullName(), e.getMessage());
        }
    }

    /**
     * Returns the last registered {@link WldtMetric} instance for the given
     * full metric name ({@code namespace.name}), if present.
     *
     * <p>This method allows developers to query the current known value of any
     * metric at any time, without waiting for the next push notification.</p>
     *
     * <p>Usage example:</p>
     * <pre>{@code
     * monitoringInterface.getMetric("wldt.internal.dt_model.events_processed")
     *     .ifPresent(m -> System.out.println("Current count: " + ((WldtCounter) m).getValue()));
     * }</pre>
     *
     * @param fullName the full metric name in the form {@code namespace.name}
     * @return an {@link Optional} containing the last known metric, or empty if
     *         no metric with that name has been registered yet
     */
    public Optional<WldtMetric> getMetric(String fullName) {
        return registry.getMetric(fullName);
    }

    /**
     * Returns an unmodifiable snapshot of all currently registered metrics,
     * keyed by their full name ({@code namespace.name}).
     *
     * @return unmodifiable map of all registered metrics
     */
    public Map<String, WldtMetric> getAllMetrics() {
        return registry.getAllMetrics();
    }

    /**
     * Returns {@code true} if a metric with the given full name is currently
     * registered in the internal registry.
     *
     * @param fullName the full metric name to check
     * @return {@code true} if registered, {@code false} otherwise
     */
    public boolean isMetricRegistered(String fullName) {
        return registry.isRegistered(fullName);
    }

    /**
     * Pre-registers a metric in the internal registry without triggering a push.
     * Useful for library components that want to declare metrics at startup
     * before the first actual measurement is available.
     *
     * @param metric the metric to pre-register; must not be null
     */
    public void registerMetric(WldtMetric metric) {
        registry.register(metric);
    }

    /**
     * Removes a metric from the internal registry by its full name.
     * After deregistration the next push for this metric will be treated as
     * a first push and delta will be {@code null}.
     *
     * @param fullName the full metric name to deregister
     */
    public void deregisterMetric(String fullName) {
        registry.deregister(fullName);
    }

    /**
     * Returns the {@link MonitoringConfiguration} associated with this interface.
     *
     * @return the active monitoring configuration
     */
    public MonitoringConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Routes the enriched metric to the appropriate typed callback on the
     * developer's {@link WldtMonitoringHandler} based on the originating component.
     *
     * @param metric the enriched metric to dispatch
     */
    private void route(WldtMetric metric) {
        switch (metric.getComponent()) {
            case DT_MODEL:
                handler.onDigitalTwinModelMetric(metric);
                break;
            case EVENT_BUS:
                handler.onEventBusMetric(metric);
                break;
            case PHYSICAL_ADAPTER:
                handler.onPhysicalAdapterMetric(metric);
                break;
            case DIGITAL_ADAPTER:
                handler.onDigitalAdapterMetric(metric);
                break;
            case AUGMENTATION:
                handler.onAugmentationMetric(metric);
                break;
            case STORAGE:
                handler.onStorageMetric(metric);
                break;
            case CUSTOM:
                handler.onCustomMetric(metric);
                break;
            default:
                logger.warn("route() received metric with unknown component: {}",
                        metric.getComponent());
                break;
        }
    }

    /**
     * Checks whether monitoring is enabled for the given component based on
     * the current {@link MonitoringConfiguration} flags.
     * Custom metrics always bypass flag gating.
     *
     * @param component the component to check
     * @return {@code true} if the corresponding flag is enabled or the
     *         component is {@link WldtMetricComponent#CUSTOM}
     */
    private boolean isEnabled(WldtMetricComponent component) {
        switch (component) {
            case DT_MODEL:
                return configuration.isDtModelMonitoringEnabled();
            case EVENT_BUS:
                return configuration.isEventBusMonitoringEnabled();
            case PHYSICAL_ADAPTER:
                return configuration.isPhysicalAdapterMonitoringEnabled();
            case DIGITAL_ADAPTER:
                return configuration.isDigitalAdapterMonitoringEnabled();
            case AUGMENTATION:
                return configuration.isAugmentationMonitoringEnabled();
            case STORAGE:
                return configuration.isStorageMonitoringEnabled();
            case CUSTOM:
                return true;
            default:
                logger.warn("isEnabled() received unknown component: {}", component);
                return false;
        }
    }
}
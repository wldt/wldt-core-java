package it.wldt.monitoring;

import it.wldt.monitoring.metrics.*;

/**
 * Abstract base class that developers extend to define the behaviour of the
 * WLDT monitoring system for a specific Digital Twin instance.
 *
 * <p>Each method in this class corresponds to a push notification from a specific
 * internal DT component. The {@link MonitoringInterface} calls these methods
 * after enriching the incoming metric (delta injection for counters, registry
 * update) and verifying that the component's monitoring flag is enabled in
 * {@link MonitoringInterfaceConfiguration}.</p>
 *
 * <p>Developers implement only the callbacks relevant to their use case.
 * All methods have a default no-op implementation so that partial overrides
 * are supported without boilerplate.</p>
 *
 * <p>The {@link WldtMetric} parameter in each callback is the base type.
 * Developers use {@code instanceof} pattern matching to access typed fields:</p>
 * <pre>{@code
 * public class MyHandler extends WldtMonitoringHandler {
 *
 *     private final Counter prometheusCounter = Counter.build()
 *         .name("wldt_dt_model_events_processed_total")
 *         .register();
 *
 *     @Override
 *     public void onDigitalTwinModelMetric(WldtMetric metric) {
 *         if (metric instanceof WldtCounter && ((WldtCounter) metric).isDeltaAvailable())
 *             prometheusCounter.inc(c.getDelta());
 *         else if (metric instanceof WldtTimer)
 *             prometheusHistogram.observe(((WldtTimer) metric).getDurationSeconds());
 *     }
 * }
 * }</pre>
 *
 * <p>Custom metrics pushed by the developer via
 * {@link MonitoringInterface#trackCustomMetric(WldtMetric)} are routed to
 * {@link #onCustomMetric(WldtMetric)}.</p>
 *
 * <p><strong>Thread safety:</strong> callback methods may be invoked from
 * different threads concurrently if multiple DT components push metrics
 * simultaneously. Implementations must ensure that any shared state
 * (e.g. Prometheus registry objects, counters) is accessed in a thread-safe
 * manner.</p>
 */
public abstract class MonitoringInterfaceHandler {

    /**
     * Called when a metric is emitted by the Digital Twin Model component,
     * which implements the Shadowing Function(s) of the DT.
     *
     * <p>Typical metric types and names:</p>
     * <ul>
     *   <li>{@link WldtCounter} {@code dt_model.events_processed} — total physical
     *       asset events processed since DT startup. Use {@code getDelta()} for
     *       Prometheus {@code Counter.inc()}.</li>
     *   <li>{@link WldtCounter} {@code dt_model.processing_errors} — total errors
     *       encountered during event processing.</li>
     *   <li>{@link WldtTimer} {@code dt_model.processing_latency_ms} — time taken
     *       to process a single physical event.</li>
     * </ul>
     *
     * @param metric the enriched metric emitted by the DT Model; never null
     */
    public void onDigitalTwinModelMetric(WldtMetric metric) {}

    /**
     * Called when a metric is emitted by a Physical Adapter component.
     *
     * <p>Typical metric types and names:</p>
     * <ul>
     *   <li>{@link WldtCounter} {@code physical_adapter.messages_received} — total
     *       messages received from the physical asset.</li>
     *   <li>{@link WldtTimer} {@code physical_adapter.message_processing_ms} — time
     *       taken to decode and forward a single physical message.</li>
     *   <li>{@link WldtUpDownCounter} {@code physical_adapter.connected_count} —
     *       current number of active Physical Adapter connections.</li>
     * </ul>
     *
     * @param metric the enriched metric emitted by a Physical Adapter; never null
     */
    public void onPhysicalAdapterMetric(WldtMetric metric) {}

    /**
     * Called when a metric is emitted by a Digital Adapter component.
     *
     * <p>Typical metric types and names:</p>
     * <ul>
     *   <li>{@link WldtCounter} {@code digital_adapter.requests_served} — total
     *       external requests served by the Digital Adapter.</li>
     *   <li>{@link WldtTimer} {@code digital_adapter.response_latency_ms} — time
     *       taken to respond to a single external request.</li>
     *   <li>{@link WldtUpDownCounter} {@code digital_adapter.active_count} —
     *       current number of active Digital Adapter instances.</li>
     * </ul>
     *
     * @param metric the enriched metric emitted by a Digital Adapter; never null
     */
    public void onDigitalAdapterMetric(WldtMetric metric) {}

    /**
     * Called when a metric is emitted by an Augmentation Function component.
     *
     * <p>Typical metric types and names:</p>
     * <ul>
     *   <li>{@link WldtCounter} {@code augmentation.function_invocations} — total
     *       number of Augmentation Function invocations since DT startup.</li>
     *   <li>{@link WldtTimer} {@code augmentation.function_execution_ms} — execution
     *       time of a single Augmentation Function invocation.</li>
     *   <li>{@link WldtUpDownCounter} {@code augmentation.registered_functions} —
     *       current number of registered Augmentation Functions.</li>
     * </ul>
     *
     * @param metric the enriched metric emitted by an Augmentation Function; never null
     */
    public void onAugmentationMetric(WldtMetric metric) {}

    /**
     * Called when a metric is emitted by the Storage layer component.
     *
     * <p>Typical metric types and names:</p>
     * <ul>
     *   <li>{@link WldtTimer} {@code storage.write_latency_ms} — time taken to
     *       persist a single DT state update.</li>
     *   <li>{@link WldtTimer} {@code storage.read_latency_ms} — time taken to
     *       retrieve a single stored entry.</li>
     *   <li>{@link WldtGauge} {@code storage.entry_count} — current number of
     *       entries persisted in the Storage layer.</li>
     * </ul>
     *
     * @param metric the enriched metric emitted by the Storage layer; never null
     */
    public void onStorageMetric(WldtMetric metric) {}

    /**
     * Called when a developer-defined custom metric is pushed via
     * {@link MonitoringInterface#trackCustomMetric(WldtMetric)}.
     *
     * <p>Custom metrics bypass per-component flag gating and are always forwarded
     * to this callback. The metric's namespace will match the custom namespace
     * configured in {@link MonitoringInterfaceConfiguration#getCustomMetricNamespace()}.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * @Override
     * public void onCustomMetric(WldtMetric metric) {
     *     if (metric instanceof WldtGauge)
     *         myGrafanaSink.record(metric.getFullName(), ((WldtGauge) metric).getValue());
     * }
     * }</pre>
     *
     * @param metric the custom metric pushed by the developer; never null
     */
    public void onCustomMetric(WldtMetric metric) {}
}
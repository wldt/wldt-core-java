package it.wldt.monitoring.metrics;

/**
 * Enumeration of the Digital Twin components that can emit monitoring metrics.
 *
 * <p>Each value identifies a specific internal module of the WLDT architecture.
 * The {@code MonitoringInterface} uses this enum to:</p>
 * <ul>
 *   <li>Check the corresponding enable flag in {@code MonitoringConfiguration}
 *       before dispatching a metric.</li>
 *   <li>Route the metric to the correct typed callback on the developer's
 *       {@code WldtMonitoringHandler}.</li>
 * </ul>
 *
 * <p>The {@link #CUSTOM} value is reserved for developer-defined metrics pushed
 * via {@code MonitoringInterface.trackCustomMetric()}. Custom metrics bypass
 * per-component flag gating and are always forwarded when a handler is registered.</p>
 */
public enum WldtMetricComponent {

    /**
     * The Digital Twin Model, which implements the Shadowing Function(s).
     * Typical metrics: processing latency, event count, error rate.
     */
    DT_MODEL,

    /**
     * A Physical Adapter managing the connection to the physical asset.
     * Typical metrics: messages received, processing latency, connection status.
     */
    PHYSICAL_ADAPTER,

    /**
     * A Digital Adapter exposing the DT state to external digital consumers.
     * Typical metrics: requests served, response latency.
     */
    DIGITAL_ADAPTER,

    /**
     * An Augmentation Function extending the DT with computed or enriched data.
     * Typical metrics: function invocations, computation time.
     */
    AUGMENTATION,

    /**
     * The Storage layer persisting DT state, events, and lifecycle data.
     * Typical metrics: write latency, read latency, stored entry count.
     */
    STORAGE,

    /**
     * A developer-defined custom metric with no associated internal component.
     * Custom metrics always bypass per-component flag gating.
     */
    CUSTOM
}
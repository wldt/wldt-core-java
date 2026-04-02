package it.wldt.monitoring.handler;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.MonitoringInterfaceHandler;
import it.wldt.monitoring.metrics.*;

/**
 * Test implementation of {@link MonitoringInterfaceHandler} for use in the WLDT test suite.
 *
 * <p>Each callback simply logs the received metric at INFO level using the
 * standard {@link WldtLogger}. No assertions, no state mutation — this class
 * is intended to verify that the monitoring push pipeline is wired correctly
 * and that metrics reach the handler without errors.</p>
 *
 * <p>Usage in a test class:</p>
 * <pre>{@code
 * MonitoringConfiguration config = new MonitoringConfiguration.Builder()
 *     .withDtModelMonitoring()
 *     .withEventBusMonitoring()
 *     .build();
 *
 * MonitoringInterface monitoring = new MonitoringInterface(
 *     config,
 *     new TestMonitoringHandler(),
 *     WldtLoggerProvider.getLogger(TestMonitoringHandler.class)
 * );
 *
 * digitalTwin.setMonitoringInterface(monitoring);
 * }</pre>
 */
public class TestMonitoringInterfaceHandler extends MonitoringInterfaceHandler {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(TestMonitoringInterfaceHandler.class);

    /**
     * Logs a metric received from the Digital Twin Model component.
     *
     * @param metric the enriched metric emitted by the DT Model; never null
     */
    @Override
    public void onDigitalTwinModelMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] DT_MODEL metric received -> {}", metric);
    }

    /**
     * Logs a metric received from a Physical Adapter component.
     *
     * @param metric the enriched metric emitted by a Physical Adapter; never null
     */
    @Override
    public void onPhysicalAdapterMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] PHYSICAL_ADAPTER metric received -> {}", metric);
    }

    /**
     * Logs a metric received from a Digital Adapter component.
     *
     * @param metric the enriched metric emitted by a Digital Adapter; never null
     */
    @Override
    public void onDigitalAdapterMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] DIGITAL_ADAPTER metric received -> {}", metric);
    }

    /**
     * Logs a metric received from an Augmentation Function component.
     *
     * @param metric the enriched metric emitted by an Augmentation Function; never null
     */
    @Override
    public void onAugmentationMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] AUGMENTATION metric received -> {}", metric);
    }

    /**
     * Logs a metric received from the Storage layer component.
     *
     * @param metric the enriched metric emitted by the Storage layer; never null
     */
    @Override
    public void onStorageMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] STORAGE metric received -> {}", metric);
    }

    /**
     * Logs a developer-defined custom metric.
     *
     * @param metric the custom metric pushed by the developer; never null
     */
    @Override
    public void onCustomMetric(WldtMetric metric) {
        logger.info("[TestMonitoringHandler] CUSTOM metric received -> {}", metric);
    }
}
package it.wldt.monitoring.handler;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.MonitoringInterfaceHandler;
import it.wldt.monitoring.metrics.WldtMetric;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.utils.SharedTestMetrics;

/**
 * Test implementation of {@link MonitoringInterfaceHandler} for use in the WLDT test suite.
 *
 * <p>Each callback logs the received metric at INFO level and records it in
 * {@link SharedTestMetrics} under the provided {@code digitalTwinId}, enabling
 * integration-test assertions on registered and updated metrics.</p>
 */
public class TestMonitoringInterfaceHandler extends MonitoringInterfaceHandler {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(TestMonitoringInterfaceHandler.class);

    private final String digitalTwinId;

    public TestMonitoringInterfaceHandler(String digitalTwinId) {
        this.digitalTwinId = digitalTwinId;
    }

    @Override
    public void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {
        logger.info("[TestMonitoringHandler] REGISTERED [{}] -> {}", component, metric);
        SharedTestMetrics.getInstance().addMonitoringRegisteredMetric(digitalTwinId, component, metric);
    }

    @Override
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
        logger.info("[TestMonitoringHandler] UPDATED [{}] -> {}", component, metric);
        SharedTestMetrics.getInstance().addMonitoringUpdatedMetric(digitalTwinId, component, metric);
    }
}

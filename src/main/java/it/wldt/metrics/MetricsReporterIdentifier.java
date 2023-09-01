package it.wldt.metrics;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public enum MetricsReporterIdentifier {

    METRICS_REPORTER_CSV("csv"),
    METRICS_REPORTER_GRAPHITE("graphite");

    public final String value;

    MetricsReporterIdentifier(String label) {
        this.value = label;
    }
}

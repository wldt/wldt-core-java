package it.wldt.metrics;

public enum MetricsReporterIdentifier {

    METRICS_REPORTER_CSV("csv"),
    METRICS_REPORTER_GRAPHITE("graphite");

    public final String value;

    MetricsReporterIdentifier(String label) {
        this.value = label;
    }
}

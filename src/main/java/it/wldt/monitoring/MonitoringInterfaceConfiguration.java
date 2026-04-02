package it.wldt.monitoring;

/**
 * Immutable configuration for the WLDT monitoring system.
 *
 * <p>{@code MonitoringConfiguration} controls which internal DT components
 * are monitored and defines the namespace prefix used for developer-defined
 * custom metrics. All flags default to {@code false} — the developer opts in
 * selectively by calling the corresponding builder methods.</p>
 *
 * <p>Instances are created exclusively through the nested {@link Builder}:</p>
 * <pre>{@code
 * MonitoringConfiguration config = new MonitoringConfiguration.Builder()
 *     .withDtModelMonitoring()
 *     .withEventBusMonitoring()
 *     .withPhysicalAdapterMonitoring()
 *     .withCustomNamespace("myapp.dt")
 *     .build();
 * }</pre>
 *
 * <p>Once built, a {@code MonitoringConfiguration} is immutable and safe
 * for concurrent read access from multiple threads.</p>
 */
public final class MonitoringInterfaceConfiguration {

    /**
     * Default namespace prefix applied to developer-defined custom metrics
     * when no custom namespace is explicitly configured.
     */
    public static final String DEFAULT_CUSTOM_NAMESPACE = "custom";

    /** Flag controlling whether metrics from the Digital Twin Model are monitored. */
    private final boolean dtModelMonitoringEnabled;

    /** Flag controlling whether metrics from Physical Adapters are monitored. */
    private final boolean physicalAdapterMonitoringEnabled;

    /** Flag controlling whether metrics from Digital Adapters are monitored. */
    private final boolean digitalAdapterMonitoringEnabled;

    /** Flag controlling whether metrics from Augmentation Functions are monitored. */
    private final boolean augmentationMonitoringEnabled;

    /** Flag controlling whether metrics from the Storage layer are monitored. */
    private final boolean storageMonitoringEnabled;

    /**
     * Namespace prefix for developer-defined custom metrics.
     * Defaults to {@link #DEFAULT_CUSTOM_NAMESPACE} if not set via the builder.
     */
    private final String customMetricNamespace;

    /**
     * Private constructor — instances are created only via {@link Builder}.
     *
     * @param builder the builder carrying all configuration values
     */
    private MonitoringInterfaceConfiguration(Builder builder) {
        this.dtModelMonitoringEnabled        = builder.dtModelMonitoringEnabled;
        this.physicalAdapterMonitoringEnabled = builder.physicalAdapterMonitoringEnabled;
        this.digitalAdapterMonitoringEnabled  = builder.digitalAdapterMonitoringEnabled;
        this.augmentationMonitoringEnabled   = builder.augmentationMonitoringEnabled;
        this.storageMonitoringEnabled        = builder.storageMonitoringEnabled;
        this.customMetricNamespace           = builder.customMetricNamespace;
    }

    /**
     * Returns {@code true} if monitoring is enabled for the Digital Twin Model
     * (Shadowing Function) component.
     *
     * @return {@code true} if DT Model monitoring is active
     */
    public boolean isDtModelMonitoringEnabled() {
        return dtModelMonitoringEnabled;
    }

    /**
     * Returns {@code true} if monitoring is enabled for Physical Adapter components.
     *
     * @return {@code true} if Physical Adapter monitoring is active
     */
    public boolean isPhysicalAdapterMonitoringEnabled() {
        return physicalAdapterMonitoringEnabled;
    }

    /**
     * Returns {@code true} if monitoring is enabled for Digital Adapter components.
     *
     * @return {@code true} if Digital Adapter monitoring is active
     */
    public boolean isDigitalAdapterMonitoringEnabled() {
        return digitalAdapterMonitoringEnabled;
    }

    /**
     * Returns {@code true} if monitoring is enabled for Augmentation Function
     * components.
     *
     * @return {@code true} if Augmentation monitoring is active
     */
    public boolean isAugmentationMonitoringEnabled() {
        return augmentationMonitoringEnabled;
    }

    /**
     * Returns {@code true} if monitoring is enabled for the Storage layer component.
     *
     * @return {@code true} if Storage monitoring is active
     */
    public boolean isStorageMonitoringEnabled() {
        return storageMonitoringEnabled;
    }

    /**
     * Returns the namespace prefix used for developer-defined custom metrics.
     * Defaults to {@link #DEFAULT_CUSTOM_NAMESPACE} if not explicitly configured.
     *
     * @return the custom metric namespace prefix, never null or blank
     */
    public String getCustomMetricNamespace() {
        return customMetricNamespace;
    }

    /**
     * Returns {@code true} if at least one component monitoring flag is enabled.
     * Useful for the {@code DigitalTwin} kernel to decide whether to inject
     * the {@code MonitoringInterface} into internal components at all.
     *
     * @return {@code true} if any monitoring is active
     */
    public boolean isAnyMonitoringEnabled() {
        return dtModelMonitoringEnabled
                || physicalAdapterMonitoringEnabled
                || digitalAdapterMonitoringEnabled
                || augmentationMonitoringEnabled
                || storageMonitoringEnabled;
    }

    /**
     * Returns a string representation of all active configuration flags.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "MonitoringConfiguration{" +
                "dtModel=" + dtModelMonitoringEnabled +
                ", physicalAdapter=" + physicalAdapterMonitoringEnabled +
                ", digitalAdapter=" + digitalAdapterMonitoringEnabled +
                ", augmentation=" + augmentationMonitoringEnabled +
                ", storage=" + storageMonitoringEnabled +
                ", customNamespace='" + customMetricNamespace + '\'' +
                '}';
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    /**
     * Fluent builder for {@link MonitoringInterfaceConfiguration}.
     *
     * <p>All monitoring flags default to {@code false}. The developer enables
     * only the components relevant to their use case. Example:</p>
     * <pre>{@code
     * MonitoringConfiguration config = new MonitoringConfiguration.Builder()
     *     .withDtModelMonitoring()
     *     .withPhysicalAdapterMonitoring()
     *     .withCustomNamespace("myapp.sensor")
     *     .build();
     * }</pre>
     */
    public static final class Builder {

        private boolean dtModelMonitoringEnabled        = false;
        private boolean physicalAdapterMonitoringEnabled = false;
        private boolean digitalAdapterMonitoringEnabled  = false;
        private boolean augmentationMonitoringEnabled   = false;
        private boolean storageMonitoringEnabled        = false;
        private String  customMetricNamespace           = DEFAULT_CUSTOM_NAMESPACE;

        /**
         * Enables monitoring for the Digital Twin Model (Shadowing Function) component.
         * When enabled, the {@code MonitoringInterface} will process and dispatch
         * metrics emitted by the {@code DigitalTwinModel}.
         *
         * @return this builder for method chaining
         */
        public Builder withDtModelMonitoring() {
            this.dtModelMonitoringEnabled = true;
            return this;
        }

        /**
         * Enables monitoring for Physical Adapter components.
         * When enabled, metrics such as messages received, processing latency,
         * and connection status will be processed and dispatched.
         *
         * @return this builder for method chaining
         */
        public Builder withPhysicalAdapterMonitoring() {
            this.physicalAdapterMonitoringEnabled = true;
            return this;
        }

        /**
         * Enables monitoring for Digital Adapter components.
         * When enabled, metrics such as requests served and response latency
         * will be processed and dispatched.
         *
         * @return this builder for method chaining
         */
        public Builder withDigitalAdapterMonitoring() {
            this.digitalAdapterMonitoringEnabled = true;
            return this;
        }

        /**
         * Enables monitoring for Augmentation Function components.
         * When enabled, metrics such as function invocation count and
         * computation time will be processed and dispatched.
         *
         * @return this builder for method chaining
         */
        public Builder withAugmentationMonitoring() {
            this.augmentationMonitoringEnabled = true;
            return this;
        }

        /**
         * Enables monitoring for the Storage layer component.
         * When enabled, metrics such as write latency, read latency,
         * and stored entry count will be processed and dispatched.
         *
         * @return this builder for method chaining
         */
        public Builder withStorageMonitoring() {
            this.storageMonitoringEnabled = true;
            return this;
        }

        /**
         * Enables monitoring for all internal DT components simultaneously.
         * Equivalent to calling all individual {@code with*Monitoring()} methods.
         *
         * @return this builder for method chaining
         */
        public Builder withAllMonitoring() {
            this.dtModelMonitoringEnabled        = true;
            this.physicalAdapterMonitoringEnabled = true;
            this.digitalAdapterMonitoringEnabled  = true;
            this.augmentationMonitoringEnabled   = true;
            this.storageMonitoringEnabled        = true;
            return this;
        }

        /**
         * Sets the namespace prefix for developer-defined custom metrics.
         * The namespace is prepended to the metric name to form the full name
         * used in the registry (e.g. {@code "myapp.dt.room.temperature"}).
         *
         * <p>If not called, defaults to {@link MonitoringInterfaceConfiguration#DEFAULT_CUSTOM_NAMESPACE}.</p>
         *
         * @param namespace the custom namespace prefix; must not be null or blank
         * @return this builder for method chaining
         * @throws IllegalArgumentException if namespace is null or blank
         */
        public Builder withCustomNamespace(String namespace) {
            if (namespace == null || namespace.trim().isEmpty())
                throw new IllegalArgumentException(
                        "Custom namespace must not be null or blank");
            this.customMetricNamespace = namespace;
            return this;
        }

        /**
         * Builds and returns an immutable {@link MonitoringInterfaceConfiguration} instance
         * from the current builder state.
         *
         * @return a new {@link MonitoringInterfaceConfiguration} with the configured flags
         */
        public MonitoringInterfaceConfiguration build() {
            return new MonitoringInterfaceConfiguration(this);
        }
    }
}
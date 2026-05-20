/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.augmentation.function;

import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtMetricComponent;

/**
 * Base class representing an augmentation function in the WLDT framework. An augmentation function is a modular
 * component that can be executed to perform specific tasks related to data augmentation, transformation, or enrichment.
 * This class provides a common structure for all augmentation functions, including properties such as id, name,
 * description, version, type, and context request. The context request allows the augmentation function to specify
 * the required context for its execution, enabling better management of dependencies and resources during the execution
 * process. Subclasses of this base class can implement specific types of augmentation functions with their own unique behavior and logic.
 */
public abstract class AugmentationFunction {

    /**
     * Metadata key used to tag all function metrics with the function's unique identifier.
     */
    public static final String METRIC_METADATA_AF_FUNCTION_ID_KEY = "af_function_id";

    /**
     * The unique id of the augmentation function.
     */
    private String id;

    /**
     * The name of the augmentation function.
     */
    private String name;

    /**
     * The description of the augmentation function.
     */
    private String description;

    /**
     * The version of the augmentation function.
     */
    private String version;

    /**
     * The type of the augmentation function.
     */
    private AugmentationFunctionType type;

    /**
     * The context request of the augmentation function.
     */
    private AugmentationFunctionContextRequest contextRequest;

    /**
     * The Monitoring Interface used to record metrics for this function.
     */
    protected MonitoringInterface monitoringInterface = null;

    /**
     * The namespace used to register and retrieve metrics for this function.
     */
    protected String metricsNamespace = null;

    /**
     * The Digital Twin id, needed when registering metrics.
     */
    protected String digitalTwinId = null;

    /**
     * Constructor of the AugmentationFunction class with all the parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param description the description of the augmentation function
     * @param version the version of the augmentation function
     * @param type the type of the augmentation function
     * @param contextRequest the context request of the augmentation function
     */
    public AugmentationFunction(String id,
                                String name,
                                String description,
                                String version,
                                AugmentationFunctionType type,
                                AugmentationFunctionContextRequest contextRequest) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.type = type;
        this.contextRequest = contextRequest;
    }

    /**
     * Constructor of the AugmentationFunction class with minimum parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param type the type of the augmentation function
     */
    public AugmentationFunction(String id,
                                String name,
                                AugmentationFunctionType type) {
        this(id, name, null, null, type, new AugmentationFunctionContextRequest());
    }

    /**
     * Gets the unique id of the augmentation function.
     * @return the unique id of the augmentation function
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique id of the augmentation function.
     * @param id the unique id of the augmentation function
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name of the augmentation function.
     * @return the name of the augmentation function
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the augmentation function.
     * @param name the name of the augmentation function
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the augmentation function.
     * @return the description of the augmentation function
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the augmentation function.
     * @param description the description of the augmentation function
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the version of the augmentation function.
     * @return the version of the augmentation function
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the augmentation function.
     * @param version the version of the augmentation function
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the type of the augmentation function.
     * @return the type of the augmentation function
     */
    public AugmentationFunctionType getType() {
        return type;
    }

    /**
     * Sets the type of the augmentation function.
     * @param type the type of the augmentation function
     */
    public void setType(AugmentationFunctionType type) {
        this.type = type;
    }

    /**
     * Gets the context request of the augmentation function, which specifies the required context for the execution of the function.
     * @return the context request of the augmentation function
     */
    public AugmentationFunctionContextRequest getContextRequest() {
        return contextRequest;
    }

    /**
     * Sets the context request of the augmentation function, which specifies the required context for the execution of the function.
     * @param contextRequest the context request of the augmentation function
     */
    public void setContextRequest(AugmentationFunctionContextRequest contextRequest) {
        this.contextRequest = contextRequest;
    }

    /**
     * Injects the MonitoringInterface into this function and triggers metric registration.
     * Called by the handler when the function is registered.
     * @param monitoringInterface the monitoring interface to use for metrics
     * @param digitalTwinId the id of the Digital Twin owning this function
     */
    public void setMonitoringInterface(MonitoringInterface monitoringInterface, String digitalTwinId) {
        if (monitoringInterface != null) {
            this.monitoringInterface = monitoringInterface;
            this.digitalTwinId = digitalTwinId;
            this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();
            handleMetricsRegistration();
        }
    }

    /**
     * Override in subclasses to register function-specific metrics.
     * Called automatically by {@link #setMonitoringInterface(MonitoringInterface, String)}.
     */
    protected void handleMetricsRegistration() {}

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationFunction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", type=").append(type);
        sb.append(", contextRequest=").append(contextRequest);
        sb.append('}');
        return sb.toString();
    }
}

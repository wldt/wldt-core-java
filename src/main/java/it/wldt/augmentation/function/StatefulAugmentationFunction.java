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
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.listener.StatefulAugmentationListener;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Extends the base AugmentationFunction class to represent a stateful augmentation function in the WLDT framework.
 * A stateful augmentation function is designed to maintain an internal state across multiple executions, allowing it
 * to perform more complex and long-running tasks compared to stateless augmentation functions. This class provides
 * abstract methods for starting and stopping the function, as well as handling updates to the digital twin's state
 * and event notifications. It also includes mechanisms for notifying listeners of results and errors during the
 * function's execution.
 */
public abstract class StatefulAugmentationFunction extends AugmentationFunction{

    /**
     * Logger instance for logging messages related to the StatefulAugmentationFunction class. This logger is used to
     * log important information, warnings, and errors that occur during the execution of stateful augmentation functions,
     * providing insights into the function's behavior and facilitating debugging and monitoring of the function's performance.
     */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulAugmentationFunction.class);

    /**
     * Listener for receiving results and errors from the execution of the stateful augmentation function. This
     * listener allows the function to communicate with other components of the system, such as the augmentation
     * handler or other interested parties, to provide updates on the function's execution status, results produced,
     * and any errors encountered. By using a listener, the stateful augmentation function can decouple its execution
     * logic from the handling of results and errors, enabling more flexible and modular design of the augmentation process.
     */
    private StatefulAugmentationListener statefulAugmentationListener;

    /**
     * The request associated with the current execution of the stateful augmentation function. This request contains
     * all the necessary information for the function's execution, including context and parameters. By storing the
     * request, the function can access relevant information during its execution and use it to produce results or
     * handle errors effectively.
     */
    private AugmentationFunctionRequest request;

    /**
     * Flag indicating whether the stateful augmentation function is currently running. This flag can be used to manage
     * the lifecycle of the function, ensuring that it is not started multiple times concurrently and allowing specific
     * logic to be executed based on the function's running state. For example, certain operations may only be allowed
     * when the function is running, while others may be restricted to when the function is not running.
     * By maintaining this flag, the function can implement appropriate behavior based on its execution state and
     * prevent potential issues that may arise from concurrent executions or invalid state transitions.
     */
    private boolean isRunning = false;

    /**
     * Constructor of the AugmentationFunction class with all the parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param description the description of the augmentation function
     * @param version the version of the augmentation function
     * @param contextRequest the context request of the augmentation function
     */
    public StatefulAugmentationFunction(String id,
                                        String name,
                                        String description,
                                        String version,
                                        AugmentationFunctionContextRequest contextRequest) {

        super(id, name, description, version, AugmentationFunctionType.STATEFUL, contextRequest);
    }

    /**
     * Constructor of the AugmentationFunction class with minimum parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     */
    public StatefulAugmentationFunction(String id,
                                        String name,
                                        String description,
                                        String version) {
        super(id,
                name,
                description,
                version,
                AugmentationFunctionType.STATEFUL,
                new AugmentationFunctionContextRequest());
    }

    @Override
    protected void handleMetricsRegistration() {
        if (monitoringInterface == null || !monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
            return;
        Map<String, Object> metadata = new HashMap<String, Object>() {{ put(METRIC_METADATA_AF_FUNCTION_ID_KEY, getId()); }};
        monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_EXEC_TIME, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_SUCCESS_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_ERROR_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_EXEC_TIME, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_SUCCESS_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_ERROR_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_EXEC_TIME, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_SUCCESS_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_ERROR_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_TIME, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_SUCCESS_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
        monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_ERROR_COUNT, WldtMetricComponent.AUGMENTATION, metadata));
    }

    /**
     * Method to handle the start of the Stateful Augmentation Function. This method is responsible for triggering the
     * start of the function and should be called when the function is requested to start. It takes an instance of
     * {@link AugmentationFunctionRequest} as a parameter, which contains all the necessary information for the
     * function's execution, including context. The method should call the abstract start method, which should be
     * implemented by specific implementations of the function to handle the logic of starting the function. If any
     * error occurs during the start of the function, an {@link AugmentationFunctionException} should be thrown to
     * indicate the failure of starting the function.
     * @param augmentationFunctionRequest the request of the augmentation function, containing all the necessary information to start the function, including context
     * @throws AugmentationFunctionException if any error occurs during the start of the function
     */
    public void handleStart(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        long startMs = System.currentTimeMillis();
        this.request = augmentationFunctionRequest;
        try {
            this.start(augmentationFunctionRequest);
            this.isRunning = true;
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_SUCCESS_COUNT);
        } catch (Exception e) {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_ERROR_COUNT);
            throw e;
        } finally {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.updateTimerSince(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_START_EXEC_TIME, startMs);
        }
    }

    /**
     * Method to handle the stop of the Stateful Augmentation Function. This method is responsible for triggering the
     * stop of the function and should be called when the function is requested to stop. It takes an instance of
     * {@link AugmentationFunctionRequest} as a parameter, which contains all the necessary information for the
     * function's execution, including context. The method should call the abstract stop method, which should be
     * implemented by specific implementations of the function to handle the logic of stopping the function. If any
     * error occurs during the stop of the function, an {@link AugmentationFunctionException} should be thrown to
     * indicate the failure of stopping the function.
     * @param augmentationFunctionRequest the request of the augmentation function, containing all the necessary information to stop the function, including context
     * @throws AugmentationFunctionException if any error occurs during the stop of the function
     */
    public void handleStop(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        long startMs = System.currentTimeMillis();
        this.request = augmentationFunctionRequest;
        try {
            this.stop(augmentationFunctionRequest);
            this.isRunning = false;
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_SUCCESS_COUNT);
        } catch (Exception e) {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_ERROR_COUNT);
            throw e;
        } finally {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.updateTimerSince(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATEFUL_STOP_EXEC_TIME, startMs);
        }
    }

    /**
     * Method to trigger the start of the Stateful Augmentation Function.
     * Specific implementation of the function should handle the logic of the start of the function
     * and return true if the function is started successfully, false otherwise.
     * @param request the request of the augmentation function, containing all the necessary information to start the function, including context
     * @throws AugmentationFunctionException if any error occurs during the start of the function
     */
    protected abstract void start(AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * Method to trigger the stop of the Stateful Augmentation Function.
     * Specific implementation of the function should handle the logic of the stop of the function
     * and return true if the function is stopped successfully, false otherwise.
     * @param request the request of the augmentation function, containing all the necessary information to stop the function, including context
     * @throws AugmentationFunctionException if any error occurs during the stop of the function
     */
    protected abstract void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * Framework entry point for state update dispatch. Instruments the call with metrics and delegates to
     * the abstract {@link #onStateUpdate(DigitalTwinState)}. Called by the handler — do not call directly.
     * @param digitalTwinState the new state of the digital twin
     * @throws AugmentationFunctionException if any error occurs during state update handling
     */
    public final void handleStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        long startMs = System.currentTimeMillis();
        try {
            onStateUpdate(digitalTwinState);
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_SUCCESS_COUNT);
        } catch (Exception e) {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_ERROR_COUNT);
            throw e;
        } finally {
            if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION))
                monitoringInterface.updateTimerSince(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_STATE_UPDATE_EXEC_TIME, startMs);
        }
    }

    /**
     * Called by the handler after executing a query on behalf of this function.
     * Updates the query execution metrics.
     * @param success true if the query completed without exception
     * @param startMs the timestamp (from {@link System#currentTimeMillis()}) before the query was issued
     */
    public void notifyQueryExecution(boolean success, long startMs) {
        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION)) {
            monitoringInterface.increaseCounter(metricsNamespace,
                    success ? CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_SUCCESS_COUNT
                            : CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_ERROR_COUNT);
            monitoringInterface.updateTimerSince(metricsNamespace, CoreMonitoringUtils.AF_FUNCTION_QUERY_EXEC_TIME, startMs);
        }
    }

    /**
     * Method to notify the running Stateful Augmentation Function of a new state update of the digital twin.
     * @param digitalTwinState the new state of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the state update to the function
     */
    public abstract void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException;

    /**
     * Method to notify the running Stateful Augmentation Function of a new query result refresh from the digital twin.
     * @param queryRequest the query request associated with the query result refresh to notify to the function
     * @param queryResult the new query result to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the query result refresh to the function
     */
    public abstract void onQueryResultRefresh(QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException;

    /**
     * Method to notify the running Stateful Augmentation Function of a new event notification of the digital twin.
     * @param digitalTwinStateEventNotification the new event notification of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the event notification to the function
     */
    public abstract void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws AugmentationFunctionException;

    /**
     * Gets the listener for receiving results and errors from the execution of the stateful augmentation function.
     * @return the listener for receiving results and errors from the execution of the stateful augmentation function
     */
    public StatefulAugmentationListener getStatefulAugmentationListener() {
        return statefulAugmentationListener;
    }

    /**
     * Sets the listener for receiving results and errors from the execution of the stateful augmentation function.
     * @param statefulAugmentationListener the listener for receiving results and errors from the execution of the stateful augmentation function
     */
    public void setStatefulAugmentationListener(StatefulAugmentationListener statefulAugmentationListener) {
        this.statefulAugmentationListener = statefulAugmentationListener;
    }

    /**
     * Notifies the listener of the results produced by the execution of the stateful augmentation function. This method
     * should be called by specific implementations of the function to provide updates on the results produced during
     * the function's execution. The method takes a list of {@link AugmentationFunctionResult} as a parameter,
     * which contains the results produced by the function. If the listener is not set or if the result list is null,
     * an error message is logged indicating that the notification cannot be sent.
     * @param resultList the list of results produced by the execution of the stateful augmentation function to notify to the listener
     */
    protected void notifyResult(AugmentationFunctionResultList resultList) {
        if (statefulAugmentationListener != null && resultList != null) {
            for(AugmentationFunctionResult<?> result : resultList) {
                result.setRequest(this.request);
            }
            if(resultList.hasError() && resultList.getAugmentationFunctionError() != null) {
                resultList.getAugmentationFunctionError().setAugmentationFunctionRequestId(request.getRequestId());
            }
            statefulAugmentationListener.onStatefulAugmentationFunctionResult(this.getId(), resultList);
        }
        else
            logger.error("Cannot notify result of the Stateful Augmentation Function with id {}: result listener or result list is null.", this.getId());
    }

    /**
     * Refreshes the query result of the stateful augmentation function by notifying the listener with the updated query request.
     */
    protected void refreshQueryResult() {
        if(super.getContextRequest().getQueryRequest() != null) {
            super.getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
            super.getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
            refreshQueryResult(super.getContextRequest().getQueryRequest());
        }
        else
            logger.warn("Cannot refresh query result of the Stateful Augmentation Function with id {}: query request is null in the context request.", this.getId());
    }

    /**
     * Refreshes the query result of the stateful augmentation function by notifying the listener with the updated query request.
     * @param queryRequest the updated query request to notify to the listener for refreshing the query result of the stateful augmentation function
     */
    protected void refreshQueryResult(QueryRequest queryRequest) {
        super.getContextRequest().setQueryRequest(queryRequest);
        if(statefulAugmentationListener != null) {
            statefulAugmentationListener.onStatefulAugmentationFunctionQueryResultRefresh(this.getId(), queryRequest);
        }
        else
            logger.error("Cannot refresh query result of the Stateful Augmentation Function with id {}: result listener is null.", this.getId());
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StatefulAugmentationFunction{");
        sb.append("resultListener=").append(statefulAugmentationListener);
        sb.append('}');
        return sb.toString();
    }
}

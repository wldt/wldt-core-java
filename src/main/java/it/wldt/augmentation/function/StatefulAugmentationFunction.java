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
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.List;
import java.util.UUID;

public abstract class StatefulAugmentationFunction extends AugmentationFunction{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulAugmentationFunction.class);

    private StatefulAugmentationListener statefulAugmentationListener;

    private AugmentationFunctionRequest request;

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

    public void handleStart(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        this.request = augmentationFunctionRequest;
        this.start(augmentationFunctionRequest);
    }

    public void handleStop(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        this.request = augmentationFunctionRequest;
        this.stop(augmentationFunctionRequest);
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
     * Method to notify the running Stateful Augmentation Function of a new state update of the digital twin.
     * @param digitalTwinState the new state of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the state update to the function
     */
    public abstract void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException;

    /**
     * TODO
     * @param queryRequest
     * @param queryResult
     * @throws AugmentationFunctionException
     */
    public abstract void onQueryResultRefresh(QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException;

    /**
     * Method to notify the running Stateful Augmentation Function of a new event notification of the digital twin.
     * @param digitalTwinStateEventNotification the new event notification of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the event notification to the function
     */
    public abstract void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws AugmentationFunctionException;

    /**
     * TODO: ...
     * @return
     */
    public StatefulAugmentationListener getStatefulAugmentationListener() {
        return statefulAugmentationListener;
    }

    /**
     * TODO: ...
     * @param statefulAugmentationListener
     */
    public void setStatefulAugmentationListener(StatefulAugmentationListener statefulAugmentationListener) {
        this.statefulAugmentationListener = statefulAugmentationListener;
    }

    /**
     * TODO: ...
     * @param resultList
     */
    protected void notifyResult(List<AugmentationFunctionResult<?>> resultList) {
        if (statefulAugmentationListener != null && resultList != null) {
            for(AugmentationFunctionResult<?> result : resultList) {
                result.setRequest(this.request);
            }
            statefulAugmentationListener.onStatefulAugmentationFunctionResult(this.getId(), resultList);
        }
        else
            logger.error("Cannot notify result of the Stateful Augmentation Function with id {}: result listener or result list is null.", this.getId());
    }

    /**
     * TODO
     * @param augmentationFunctionError
     */
    protected void notifyError(AugmentationFunctionError augmentationFunctionError) {
        if (statefulAugmentationListener != null) {
            augmentationFunctionError.setAugmentationFunctionRequestId(this.request != null ? this.request.getRequestId() : null);
            statefulAugmentationListener.onStatefulAugmentationFunctionError(this.getId(), augmentationFunctionError);
        }
        else
            logger.error("Cannot notify error of the Stateful Augmentation Function with id {}: result listener is null.", this.getId());
    }

    protected void refreshQueryResult() {
        if(super.getContextRequest().getQueryRequest() != null) {
            super.getContextRequest().getQueryRequest().setRequestTimestampMs(System.currentTimeMillis());
            super.getContextRequest().getQueryRequest().setRequestId(UUID.randomUUID().toString());
            refreshQueryResult(super.getContextRequest().getQueryRequest());
        }
        else
            logger.warn("Cannot refresh query result of the Stateful Augmentation Function with id {}: query request is null in the context request.", this.getId());
    }

    protected void refreshQueryResult(QueryRequest queryRequest) {
        super.getContextRequest().setQueryRequest(queryRequest);
        if(statefulAugmentationListener != null) {
            statefulAugmentationListener.onStatefulAugmentationFunctionQueryResultRefresh(this.getId(), queryRequest);
        }
        else
            logger.error("Cannot refresh query result of the Stateful Augmentation Function with id {}: result listener is null.", this.getId());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StatefulAugmentationFunction{");
        sb.append("resultListener=").append(statefulAugmentationListener);
        sb.append('}');
        return sb.toString();
    }
}

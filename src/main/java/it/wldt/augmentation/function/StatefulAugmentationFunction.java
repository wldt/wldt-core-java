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

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.listener.StatefulAugmentationListener;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.List;

public abstract class StatefulAugmentationFunction extends AugmentationFunction{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulAugmentationFunction.class);

    private StatefulAugmentationListener resultListener;

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

    /**
     * Method to trigger the start of the Stateful Augmentation Function.
     * Specific implementation of the function should handle the logic of the start of the function
     * and return true if the function is started successfully, false otherwise.
     * @param context the context of the augmentation function, containing all the necessary information to start the function
     * @throws AugmentationFunctionException if any error occurs during the start of the function
     */
    public abstract void start(AugmentationFunctionContext context) throws AugmentationFunctionException;

    /**
     * Method to trigger the stop of the Stateful Augmentation Function.
     * Specific implementation of the function should handle the logic of the stop of the function
     * and return true if the function is stopped successfully, false otherwise.
     * @param context the context of the augmentation function, containing all the necessary information to stop the function
     * @throws AugmentationFunctionException if any error occurs during the stop of the function
     */
    public abstract void stop(AugmentationFunctionContext context) throws AugmentationFunctionException;

    /**
     * Method to notify the running Stateful Augmentation Function of a new state update of the digital twin.
     * @param digitalTwinState the new state of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the state update to the function
     */
    public abstract void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException;

    /**
     * Method to notify the running Stateful Augmentation Function of a new event notification of the digital twin.
     * @param digitalTwinStateEventNotification the new event notification of the digital twin to notify to the function
     * @throws AugmentationFunctionException if any error occurs during the notification of the event notification to the function
     */
    public abstract void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws AugmentationFunctionException;

    public StatefulAugmentationListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(StatefulAugmentationListener resultListener) {
        this.resultListener = resultListener;
    }

    /**
     * TODO: ...
     * @param resultList
     */
    protected void notifyResult(List<AugmentationFunctionResult<?>> resultList) {
        if (resultListener != null && resultList != null) {
            resultListener.onStatefulAugmentationFunctionResult(this.getId(), resultList);
        }
        else
            logger.error("Cannot notify result of the Stateful Augmentation Function with id {}: result listener or result list is null.", this.getId());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StatefulAugmentationFunction{");
        sb.append("resultListener=").append(resultListener);
        sb.append('}');
        return sb.toString();
    }
}

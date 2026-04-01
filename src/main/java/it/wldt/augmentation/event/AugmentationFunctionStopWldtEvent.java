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
package it.wldt.augmentation.event;

import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

/**
 * This class represents an event that is triggered when the execution of an augmentation function stops. It extends the generic
 * {@link WldtEvent} class, with the payload being an instance of {@link AugmentationFunctionRequest}. The event contains
 * information about the specific augmentation function that has stopped executing, as well as the handler responsible
 * for executing the function. This allows for better tracking and management of augmentation function executions,
 * providing insights into which functions have stopped executing and enabling more effective handling of the execution
 * process, including any necessary cleanup or post-execution processing that may be required when a function stops.
 */
public class AugmentationFunctionStopWldtEvent extends WldtEvent<AugmentationFunctionRequest> {

    /**
     * Identifier of the augmentation function that has stopped executing.
     */
    private String augmentationFunctionId;

    /**
     * Identifier of the augmentation handler that was executing the function when it stopped.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionStopWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that was executing the function when it stopped.
     * @param augmentationFunctionId Identifier of the augmentation function that has stopped executing.
     * @param augmentationFunctionRequest The request for the execution of the augmentation function, encapsulated in an instance of {@link AugmentationFunctionRequest}. This can
     *                                    be useful for providing context about the function execution that has stopped, such as the parameters used for the execution and any relevant metadata.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionStopWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws EventBusException {
        super(String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_STOP_BASE_TYPE, augmentationHandlerId, augmentationFunctionId), augmentationFunctionRequest, null);
        this.augmentationHandlerId = augmentationHandlerId;
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier of the augmentation handler that was executing the function when it stopped.
     * @return The identifier of the augmentation handler that was executing the function when it stopped.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that was executing the function when it stopped.
     * @param augmentationHandlerId The identifier of the augmentation handler that was executing the function when it stopped.
     */
    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation function that has stopped executing.
     * @return The identifier of the augmentation function that has stopped executing.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the identifier of the augmentation function that has stopped executing.
     * @param augmentationFunctionId The identifier of the augmentation function that has stopped executing.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionStopWldtEvent{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationHandlerId='" + augmentationHandlerId + '\'' +
                '}';
    }
}

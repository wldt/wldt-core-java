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

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

/**
 * This class represents an event that is triggered when an error occurs during the execution of an augmentation function.
 * It extends the generic {@link WldtEvent} class, with the payload being an instance of {@link AugmentationFunctionError}.
 * The event contains information about the specific augmentation function that encountered the error, as well as the handler
 * responsible for executing the function. This allows for better tracking and debugging of errors in the augmentation process,
 * providing insights into which functions are causing issues and enabling more effective error handling and resolution.
 */
public class AugmentationFunctionErrorWldtEvent extends WldtEvent<AugmentationFunctionError> {

    /**
     * Identifier of the augmentation function that encountered the error.
     */
    private String augmentationFunctionId;

    /**
     * Identifier of the augmentation handler that was executing the function when the error occurred.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionErrorWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that was executing the function when the error occurred.
     * @param augmentationFunctionId Identifier of the augmentation function that encountered the error.
     * @param augmentationFunctionError The error that occurred during the execution of the augmentation function, encapsulated in an instance of {@link AugmentationFunctionError}.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionErrorWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) throws EventBusException {
        super(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE, augmentationFunctionError, null);
        this.augmentationHandlerId = augmentationHandlerId;
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier of the augmentation handler that was executing the function when the error occurred.
     * @return The identifier of the augmentation handler that was executing the function when the error occurred.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that was executing the function when the error occurred.
     * @param augmentationHandlerId The identifier of the augmentation handler that was executing the function when the error occurred.
     */
    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation function that encountered the error.
     * @return The identifier of the augmentation function that encountered the error.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the identifier of the augmentation function that encountered the error.
     * @param augmentationFunctionId The identifier of the augmentation function that encountered the error.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionErrorWldtEvent{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationHandlerId='" + augmentationHandlerId + '\'' +
                '}';
    }
}

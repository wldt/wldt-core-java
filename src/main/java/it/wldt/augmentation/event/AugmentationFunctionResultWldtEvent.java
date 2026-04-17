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

import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

import java.util.List;

/**
 * This class represents an event that is triggered when the results of an augmentation function execution are available. It extends the generic
 * {@link WldtEvent} class, with the payload being a list of instances of {@link AugmentationFunctionResult}. The event
 * contains information about the specific augmentation function that was executed, as well as the handler responsible for
 * executing the function. This allows for better tracking and management of augmentation function results, providing
 * insights into which functions are producing results and enabling more effective handling of the results processing and management.
 */
public class AugmentationFunctionResultWldtEvent extends WldtEvent<AugmentationFunctionResultList> {

    /**
     * Identifier of the augmentation function that was executed and produced the results.
     */
    private String augmentationFunctionId;

    /**
     * Identifier of the augmentation handler that was executing the function when the results were produced.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionResultWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that was executing the function when the results were produced.
     * @param augmentationFunctionId Identifier of the augmentation function that was executed and produced the results.
     * @param results The results of the execution of the augmentation function, encapsulated in a list of instances of {@link AugmentationFunctionResult}.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionResultWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionResultList results) throws EventBusException {
        super(String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_RESULT_BASE_TYPE, augmentationHandlerId, augmentationFunctionId), results, null);
        this.augmentationHandlerId = augmentationHandlerId;
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier of the augmentation handler that was executing the function when the results were produced.
     * @return The identifier of the augmentation handler that was executing the function when the results were produced.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that was executing the function when the results were produced.
     * @param augmentationHandlerId The identifier of the augmentation handler that was executing the function when the results were produced.
     */
    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation function that was executed and produced the results.
     * @return The identifier of the augmentation function that was executed and produced the results.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the identifier of the augmentation function that was executed and produced the results.
     * @param augmentationFunctionId The identifier of the augmentation function that was executed and produced the results.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionResultWldtEvent{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationHandlerId='" + augmentationHandlerId + '\'' +
                '}';
    }
}

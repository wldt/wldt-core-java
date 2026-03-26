package it.wldt.augmentation.event;

import it.wldt.augmentation.result.AugmentationFunctionResult;
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
public class AugmentationFunctionResultWldtEvent extends WldtEvent<List<AugmentationFunctionResult<?>>> {

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
    public AugmentationFunctionResultWldtEvent(String augmentationHandlerId, String augmentationFunctionId, List<AugmentationFunctionResult<?>> results) throws EventBusException {
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

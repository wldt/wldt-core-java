package it.wldt.augmentation.event;

import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

/**
 * This class represents an event that is triggered when the execution of an augmentation function starts. It extends the generic
 * {@link WldtEvent} class, with the payload being an instance of {@link AugmentationFunctionRequest}. The event contains
 * information about the specific augmentation function being executed, as well as the handler responsible for executing
 * the function. This allows for better tracking and management of augmentation function executions, providing insights
 * into which functions are being executed and enabling more effective handling of the execution process from the very beginning.
 */
public class AugmentationFunctionStartWldtEvent extends WldtEvent<AugmentationFunctionRequest> {

    /**
     * Identifier of the augmentation function being executed.
     */
    private String augmentationFunctionId;

    /**
     * Identifier of the augmentation handler that is executing the function.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionStartWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that is executing the function.
     * @param augmentationFunctionId Identifier of the augmentation function being executed.
     * @param augmentationFunctionRequest The request for the execution of the augmentation function, encapsulated in an instance of {@link AugmentationFunctionRequest}.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionStartWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws EventBusException {
        super(String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_START_BASE_TYPE, augmentationHandlerId, augmentationFunctionId), augmentationFunctionRequest, null);
        this.augmentationHandlerId = augmentationHandlerId;
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier of the augmentation handler that is executing the function.
     * @return The identifier of the augmentation handler that is executing the function.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that is executing the function.
     * @param augmentationHandlerId The identifier of the augmentation handler that is executing the function.
     */
    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation function being executed.
     * @return The identifier of the augmentation function being executed.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the identifier of the augmentation function being executed.
     * @param augmentationFunctionId The identifier of the augmentation function being executed.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionStartWldtEvent{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationHandlerId='" + augmentationHandlerId + '\'' +
                '}';
    }
}
package it.wldt.augmentation.event;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

// TODO ...
public class AugmentationFunctionErrorWldtEvent extends WldtEvent<AugmentationFunctionError> {

    private String augmentationFunctionId;

    private String augmentationHandlerId;

    public AugmentationFunctionErrorWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError) throws EventBusException {
        super(WldtEventTypes.AUGMENTATION_FUNCTION_ERROR_EVENT_TYPE, augmentationFunctionError, null);
        this.augmentationHandlerId = augmentationHandlerId;
        this.augmentationFunctionId = augmentationFunctionId;
    }

    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

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

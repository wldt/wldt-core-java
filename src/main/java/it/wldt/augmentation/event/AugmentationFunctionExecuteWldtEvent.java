package it.wldt.augmentation.event;

import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

public class AugmentationFunctionExecuteWldtEvent extends WldtEvent<AugmentationFunctionRequest> {

    private String augmentationFunctionId;

    private String augmentationHandlerId;

    public AugmentationFunctionExecuteWldtEvent(String augmentationHandlerId, String augmentationFunctionId, AugmentationFunctionRequest augmentationFunctionRequest) throws EventBusException {
        super(String.format("%s.%s.%s", WldtEventTypes.AUGMENTATION_FUNCTION_EXECUTE_BASE_TYPE, augmentationHandlerId, augmentationFunctionId), augmentationFunctionRequest, null);
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
        return "AugmentationFunctionExecuteWldtEvent{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationHandlerId='" + augmentationHandlerId + '\'' +
                '}';
    }
}

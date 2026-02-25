package it.wldt.augmentation.event;

import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

// TODO ...
public class AugmentationFunctionRegistrationWldtEvent extends WldtEvent<AugmentationFunction> {

    private String augmentationHandlerId;

    public AugmentationFunctionRegistrationWldtEvent(String augmentationHandlerId, AugmentationFunction augmentationFunction) throws EventBusException {
        super(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE, augmentationFunction, null);
        this.augmentationHandlerId = augmentationHandlerId;
    }

    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    public void setAugmentationHandlerId(String augmentationHandlerId) {
        this.augmentationHandlerId = augmentationHandlerId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationFunctionRegistrationWldtEvent{");
        sb.append("augmentationHandlerId='").append(augmentationHandlerId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

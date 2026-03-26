package it.wldt.augmentation.event;

import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

/**
 * This class represents an event that is triggered when a new augmentation function is registered in the system. It extends the generic
 * {@link WldtEvent} class, with the payload being an instance of {@link AugmentationFunction}. The event contains information about the specific
 * augmentation function that has been registered, as well as the handler responsible for managing the function. This allows for better tracking and
 * management of augmentation functions within the system, providing insights into which functions are available and enabling more effective handling of
 * function registration processes.
 */
public class AugmentationFunctionRegistrationWldtEvent extends WldtEvent<AugmentationFunction> {

    /**
     * Identifier of the augmentation handler that is responsible for managing the registered function.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionRegistrationWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that is responsible for managing the registered function.
     * @param augmentationFunction The augmentation function that has been registered, encapsulated in an instance of {@link AugmentationFunction}.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionRegistrationWldtEvent(String augmentationHandlerId, AugmentationFunction augmentationFunction) throws EventBusException {
        super(WldtEventTypes.AUGMENTATION_FUNCTION_REGISTERED_EVENT_TYPE, augmentationFunction, null);
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation handler that is responsible for managing the registered function.
     * @return The identifier of the augmentation handler that is responsible for managing the registered function.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that is responsible for managing the registered function.
     * @param augmentationHandlerId The identifier of the augmentation handler that is responsible for managing the registered function.
     */
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

package it.wldt.augmentation.event;

import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

/**
 * This class represents an event that is triggered when an augmentation function is unregistered from the system. It extends the generic
 * {@link WldtEvent} class, with the payload being an instance of {@link AugmentationFunction}. The event contains information about the specific
 * augmentation function that has been unregistered, as well as the handler responsible for managing the function. This allows for better tracking and
 * management of augmentation functions within the system, providing insights into which functions are available and enabling more effective handling of
 * function unregistration processes.
 */
public class AugmentationFunctionUnRegistrationWldtEvent extends WldtEvent<AugmentationFunction> {

    /**
     * Identifier of the augmentation handler that is responsible for managing the unregistered function.
     */
    private String augmentationHandlerId;

    /**
     * Constructor for the AugmentationFunctionUnRegistrationWldtEvent class.
     * @param augmentationHandlerId Identifier of the augmentation handler that is responsible for managing the unregistered function.
     * @param augmentationFunction The augmentation function that has been unregistered, encapsulated in an instance of {@link AugmentationFunction}.
     * @throws EventBusException if there is an issue with creating the event, such as invalid parameters or issues with the event bus system.
     */
    public AugmentationFunctionUnRegistrationWldtEvent(String augmentationHandlerId, AugmentationFunction augmentationFunction) throws EventBusException {
        super(WldtEventTypes.AUGMENTATION_FUNCTION_UNREGISTERED_EVENT_TYPE, augmentationFunction, null);
        this.augmentationHandlerId = augmentationHandlerId;
    }

    /**
     * Gets the identifier of the augmentation handler that is responsible for managing the unregistered function.
     * @return The identifier of the augmentation handler that is responsible for managing the unregistered function.
     */
    public String getAugmentationHandlerId() {
        return augmentationHandlerId;
    }

    /**
     * Sets the identifier of the augmentation handler that is responsible for managing the unregistered function.
     * @param augmentationHandlerId The identifier of the augmentation handler that is responsible for managing the unregistered function.
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

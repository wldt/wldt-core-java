package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.storage.model.StorageRecord;

/**
 * Represents a record for the registration of an augmentation function in the storage. This record contains information
 * about the augmentation function, such as its unique identifier, the handler identifier, and the type of augmentation function.
 */
public class AugmentationFunctionRegistrationRecord extends StorageRecord {

    /**
     * The unique identifier for the augmentation function.
     */
    private String augmentationFunctionId;

    /**
     * The identifier for the handler associated with the augmentation function. This handler is responsible for processing
     * the augmentation function and executing its logic when invoked.
     */
    private String augmentationFunctionHandlerId;

    /**
     * The type of the augmentation function, represented by the AugmentationFunctionType enum.
     */
    private AugmentationFunctionType type;

    /**
     * Default constructor for AugmentationFunctionRegistrationRecord. Initializes an empty record without setting any properties.
     */
    public AugmentationFunctionRegistrationRecord() {
    }

    /**
     * Constructor for AugmentationFunctionRegistrationRecord that initializes all properties of the record.
     * @param augmentationFunctionId The unique identifier for the augmentation function.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function.
     * @param type The type of the augmentation function, represented by the AugmentationFunctionType enum.
     */
    public AugmentationFunctionRegistrationRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType type) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.type = type;
    }

    /**
     * Gets the unique identifier for the augmentation function.
     * @return The unique identifier for the augmentation function.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the unique identifier for the augmentation function.
     * @param augmentationFunctionId The unique identifier for the augmentation function to set.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier for the handler associated with the augmentation function.
     * @return The identifier for the handler associated with the augmentation function.
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Sets the identifier for the handler associated with the augmentation function.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function to set.
     */
    public void setAugmentationFunctionHandlerId(String augmentationFunctionHandlerId) {
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
    }

    /**
     * Gets the type of the augmentation function, represented by the AugmentationFunctionType enum.
     * @return The type of the augmentation function, represented by the AugmentationFunctionType enum.
     */
    public AugmentationFunctionType getType() {
        return type;
    }

    /**
     * Sets the type of the augmentation function, represented by the AugmentationFunctionType enum.
     * @param type The type of the augmentation function, represented by the AugmentationFunctionType enum, to set.
     */
    public void setType(AugmentationFunctionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionRegistrationRecord{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", type=" + type +
                '}';
    }
}

package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.storage.model.StorageRecord;

/**
 * Represents a record for the unregistration of an augmentation function in the storage. This record contains information
 * about the augmentation function being unregistered, such as its unique identifier, the handler identifier, and the type
 * of augmentation function. The AugmentationFunctionUnregistrationRecord class serves as a data structure to hold all
 * relevant information about a specific unregistration of an augmentation function, allowing for better management and
 * tracking of unregistrations within the system.
 */
public class AugmentationFunctionUnregistrationRecord extends StorageRecord {

    /**
     * The unique identifier for the augmentation function being unregistered.
     */
    private String augmentationFunctionId;

    /**
     * The identifier for the handler associated with the augmentation function being unregistered.
     */
    private String augmentationFunctionHandlerId;

    /**
     * The type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum.
     */
    private AugmentationFunctionType type;

    /**
     * Default constructor for AugmentationFunctionUnregistrationRecord. Initializes an empty record without setting any properties.
     */
    public AugmentationFunctionUnregistrationRecord() {
    }

    /**
     * Constructor for AugmentationFunctionUnregistrationRecord that initializes all properties of the record.
     * @param augmentationFunctionId The unique identifier for the augmentation function being unregistered.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function being unregistered.
     * @param type The type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum.
     */
    public AugmentationFunctionUnregistrationRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType type) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.type = type;
    }

    /**
     * Gets the unique identifier for the augmentation function being unregistered.
     * @return The unique identifier for the augmentation function being unregistered.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the unique identifier for the augmentation function being unregistered.
     * @param augmentationFunctionId The unique identifier for the augmentation function being unregistered to set.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier for the handler associated with the augmentation function being unregistered.
     * @return The identifier for the handler associated with the augmentation function being unregistered.
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Sets the identifier for the handler associated with the augmentation function being unregistered.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function being unregistered to set.
     */
    public void setAugmentationFunctionHandlerId(String augmentationFunctionHandlerId) {
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
    }

    /**
     * Gets the type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum.
     * @return The type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum.
     */
    public AugmentationFunctionType getType() {
        return type;
    }

    /**
     * Sets the type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum.
     * @param type The type of the augmentation function being unregistered, represented by the AugmentationFunctionType enum to set.
     */
    public void setType(AugmentationFunctionType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionUnregistrationRecord{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", type=" + type +
                '}';
    }
}

package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.storage.model.StorageRecord;

public class AugmentationFunctionUnregistrationRecord extends StorageRecord {

    private String augmentationFunctionId;

    private String augmentationFunctionHandlerId;

    private AugmentationFunctionType type;

    public AugmentationFunctionUnregistrationRecord() {
    }

    public AugmentationFunctionUnregistrationRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType type) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.type = type;
    }

    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    public void setAugmentationFunctionHandlerId(String augmentationFunctionHandlerId) {
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
    }

    public AugmentationFunctionType getType() {
        return type;
    }

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

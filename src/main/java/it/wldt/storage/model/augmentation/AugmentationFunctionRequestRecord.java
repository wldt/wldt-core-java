package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.request.AugmentationFunctionRequestType;
import it.wldt.storage.model.StorageRecord;

public class AugmentationFunctionRequestRecord extends StorageRecord {

    private String augmentationFunctionId;

    private String augmentationFunctionHandlerId;

    private String requestId;

    private AugmentationFunctionContext context;

    private AugmentationFunctionRequestType type;

    public AugmentationFunctionRequestRecord() { }

    public AugmentationFunctionRequestRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType type) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.requestId = requestId;
        this.context = context;
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public AugmentationFunctionContext getContext() {
        return context;
    }

    public void setContext(AugmentationFunctionContext context) {
        this.context = context;
    }

    public AugmentationFunctionRequestType getType() {
        return type;
    }

    public void setType(AugmentationFunctionRequestType type) {
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

    @Override
    public String toString() {
        return "AugmentationFunctionRequestRecord{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", context=" + context +
                ", type=" + type +
                '}';
    }
}

package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.storage.model.StorageRecord;

import java.util.Map;

public class AugmentationFunctionErrorRecord extends StorageRecord {

    private String augmentationFunctionId;

    private String augmentationFunctionHandlerId;

    private String errorId;

    private String requestId;

    private AugmentationFunctionErrorType errorType;

    private String message;

    private Map<String, Object> metadata;

    public AugmentationFunctionErrorRecord() {
    }

    public AugmentationFunctionErrorRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, String errorId, String requestId, AugmentationFunctionErrorType errorType, String message, Map<String, Object> metadata) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.errorId = errorId;
        this.requestId = requestId;
        this.errorType = errorType;
        this.message = message;
        this.metadata = metadata;
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

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public AugmentationFunctionErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(AugmentationFunctionErrorType errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionErrorRecord{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", errorId='" + errorId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", errorType=" + errorType +
                ", message='" + message + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}

package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.storage.model.StorageRecord;

import java.util.Map;

public class AugmentationFunctionResultRecord extends StorageRecord {

    private String augmentationFunctionId;

    private String augmentationFunctionHandlerId;

    private String requestId;

    private String key;

    private AugmentationFunctionResultType type;

    private Object value;

    private AugmentationFunctionResultMetrics augmentationFunctionResultMetrics;

    private Map<String, Object> metadata;

    public AugmentationFunctionResultRecord() { }

    public AugmentationFunctionResultRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, String requestId, String key, AugmentationFunctionResultType type, Object value, AugmentationFunctionResultMetrics augmentationFunctionResultMetrics, Map<String, Object> metadata) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.requestId = requestId;
        this.key = key;
        this.type = type;
        this.value = value;
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
        this.metadata = metadata;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public AugmentationFunctionResultType getType() {
        return type;
    }

    public void setType(AugmentationFunctionResultType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public AugmentationFunctionResultMetrics getAugmentationFunctionResultMetrics() {
        return augmentationFunctionResultMetrics;
    }

    public void setAugmentationFunctionResultMetrics(AugmentationFunctionResultMetrics augmentationFunctionResultMetrics) {
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
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

    @Override
    public String toString() {
        return "AugmentationFunctionResultRecord{" +
                "augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", key='" + key + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", augmentationFunctionResultMetrics=" + augmentationFunctionResultMetrics +
                ", metadata=" + metadata +
                '}';
    }
}

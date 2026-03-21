package it.wldt.augmentation.result;

import it.wldt.augmentation.request.AugmentationFunctionRequest;

import java.util.Map;

public class AugmentationFunctionResult<T> {

    private AugmentationFunctionResultType type;

    private AugmentationFunctionResultMetrics augmentationFunctionResultMetrics;

    private String key;

    private T value;

    private Map<String, Object> metadata;

    private AugmentationFunctionRequest request;

    private final Long timestamp;

    public AugmentationFunctionResult(AugmentationFunctionResultType type,
                                      String key,
                                      T value,
                                      AugmentationFunctionResultMetrics augmentationFunctionResultMetrics,
                                      Map<String, Object> metadata) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.metadata = metadata;
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
        this.timestamp = System.currentTimeMillis();
    }

    public AugmentationFunctionResultType getType() {
        return type;
    }

    public void setType(AugmentationFunctionResultType type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public AugmentationFunctionRequest getRequest() {
        return request;
    }

    public void setRequest(AugmentationFunctionRequest request) {
        this.request = request;
    }

    public AugmentationFunctionResultMetrics getAugmentationFunctionResultMetrics() {
        return augmentationFunctionResultMetrics;
    }

    public void setAugmentationFunctionResultMetrics(AugmentationFunctionResultMetrics augmentationFunctionResultMetrics) {
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionResult{" +
                "type=" + type +
                ", augmentationFunctionResultMetrics=" + augmentationFunctionResultMetrics +
                ", key='" + key + '\'' +
                ", value=" + value +
                ", metadata=" + metadata +
                ", request=" + request +
                ", timestamp=" + timestamp +
                '}';
    }
}

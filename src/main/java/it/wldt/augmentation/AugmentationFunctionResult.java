package it.wldt.augmentation;

import java.util.Map;

public class AugmentationFunctionResult<T> {

    private AugmentationFunctionResultType augmentationFunctionResultType;

    private String key;

    private T value;

    private Map<String, Object> metadata;

    public AugmentationFunctionResult(AugmentationFunctionResultType augmentationFunctionResultType,
                                      String key,
                                      T value,
                                      Map<String, Object> metadata) {
        this.augmentationFunctionResultType = augmentationFunctionResultType;
        this.key = key;
        this.value = value;
        this.metadata = metadata;
    }

    public AugmentationFunctionResultType getAugmentationFunctionResultType() {
        return augmentationFunctionResultType;
    }

    public void setAugmentationFunctionResultType(AugmentationFunctionResultType augmentationFunctionResultType) {
        this.augmentationFunctionResultType = augmentationFunctionResultType;
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationFunctionResult{");
        sb.append("augmentationFunctionResultType=").append(augmentationFunctionResultType);
        sb.append(", key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}

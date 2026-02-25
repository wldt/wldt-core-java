package it.wldt.augmentation;

import java.util.Map;

public class AugmentationFunctionResult<T> {

    private AugmentationFunctionResultType type;

    private String key;

    private T value;

    private Map<String, Object> metadata;

    public AugmentationFunctionResult(AugmentationFunctionResultType type,
                                      String key,
                                      T value,
                                      Map<String, Object> metadata) {
        this.type = type;
        this.key = key;
        this.value = value;
        this.metadata = metadata;
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

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationFunctionResult{");
        sb.append("augmentationFunctionResultType=").append(type);
        sb.append(", key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}

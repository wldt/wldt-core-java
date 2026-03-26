package it.wldt.augmentation.result;

import it.wldt.augmentation.request.AugmentationFunctionRequest;

import java.util.Map;

/**
 * Class representing the result of an augmentation function execution. This class encapsulates the details of the
 * result produced by an augmentation function, including the type of result, the key associated with the result,
 * the value of the result, any relevant metadata, and the request that led to this result. The AugmentationFunctionResult
 * class serves as a data structure to hold all relevant information about a specific result from an augmentation
 * function execution, allowing for better management and tracking of results within the system.
 * @param <T>
 */
public class AugmentationFunctionResult<T> {

    /**
     * The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     */
    private AugmentationFunctionResultType type;

    /**
     * The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     */
    private AugmentationFunctionResultMetrics augmentationFunctionResultMetrics;

    /**
     * The key associated with the augmentation function result, represented as a string value. This key can be used to
     * identify the specific result or to associate it with a particular aspect of the augmentation function execution.
     */
    private String key;

    /**
     * The value of the augmentation function result, represented as a generic type T. This value can hold any type of data
     * produced by the augmentation function, allowing for flexibility in the types of results that can be represented and managed within the system.
     */
    private T value;

    /**
     * The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     * This metadata can contain any additional information relevant to the result, such as context, parameters, or other
     * details that may be useful for understanding or processing the result within the system.
     */
    private Map<String, Object> metadata;

    /**
     * The request that led to this augmentation function result, represented as an instance of AugmentationFunctionRequest.
     * This property allows for tracking the origin of the result and associating it with the specific request that
     * triggered the execution of the augmentation function, providing better traceability and context for the result within the system.
     */
    private AugmentationFunctionRequest request;

    /**
     * The timestamp indicating when the augmentation function result was created, represented as a long value (milliseconds since epoch).
     */
    private final Long timestamp;

    /**
     * Constructor for the AugmentationFunctionResult class, initializing all properties of the result.
     * @param type The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     * @param key The key associated with the augmentation function result, represented as a string value.
     * @param value The value of the augmentation function result, represented as a generic type T.
     * @param augmentationFunctionResultMetrics The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     * @param metadata The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     */
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

    /**
     * Gets the type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     * @return The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     */
    public AugmentationFunctionResultType getType() {
        return type;
    }

    /**
     * Sets the type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     * @param type The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     */
    public void setType(AugmentationFunctionResultType type) {
        this.type = type;
    }

    /**
     * Gets the key associated with the augmentation function result, represented as a string value.
     * @return The key associated with the augmentation function result, represented as a string value.
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key associated with the augmentation function result, represented as a string value.
     * @param key The key associated with the augmentation function result, represented as a string value.
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Gets the value of the augmentation function result, represented as a generic type T.
     * @return The value of the augmentation function result, represented as a generic type T.
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value of the augmentation function result, represented as a generic type T.
     * @param value The value of the augmentation function result, represented as a generic type T.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Gets the metadata associated with the augmentation function result, represented as a map of string keys to object values.
     * @return The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the metadata associated with the augmentation function result, represented as a map of string keys to object values.
     * @param metadata The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the request that led to this augmentation function result, represented as an instance of AugmentationFunctionRequest.
     * @return The request that led to this augmentation function result, represented as an instance of AugmentationFunctionRequest.
     */
    public AugmentationFunctionRequest getRequest() {
        return request;
    }

    /**
     * Sets the request that led to this augmentation function result, represented as an instance of AugmentationFunctionRequest.
     * @param request The request that led to this augmentation function result, represented as an instance of AugmentationFunctionRequest.
     */
    public void setRequest(AugmentationFunctionRequest request) {
        this.request = request;
    }

    /**
     * Gets the metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     * @return The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     */
    public AugmentationFunctionResultMetrics getAugmentationFunctionResultMetrics() {
        return augmentationFunctionResultMetrics;
    }

    /**
     * Sets the metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     * @param augmentationFunctionResultMetrics The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     */
    public void setAugmentationFunctionResultMetrics(AugmentationFunctionResultMetrics augmentationFunctionResultMetrics) {
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
    }

    /**
     * Gets the timestamp indicating when the augmentation function result was created, represented as a long value (milliseconds since epoch).
     * @return The timestamp indicating when the augmentation function result was created, represented as a long value (milliseconds since epoch).
     */
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

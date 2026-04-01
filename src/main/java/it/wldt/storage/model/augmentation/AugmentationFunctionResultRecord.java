package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.storage.model.StorageRecord;

import java.util.Map;

/**
 * Represents a record for the result of an augmentation function in the storage. This record contains information about
 * the augmentation function result, such as the unique identifier for the augmentation function, the handler identifier,
 * the request ID, the key associated with the result, the type of the result, the value of the result, the metrics
 * associated with the result, and any additional metadata. The AugmentationFunctionResultRecord class serves as a
 * data structure to hold all relevant information about a specific result of an augmentation function, allowing for
 * better management and tracking of results within the system.
 */
public class AugmentationFunctionResultRecord extends StorageRecord {

    /**
     * The unique identifier for the augmentation function that produced this result.
     */
    private String augmentationFunctionId;

    /**
     * The identifier for the handler associated with the augmentation function that produced this result.
     */
    private String augmentationFunctionHandlerId;

    /**
     * The unique identifier for the augmentation function request that led to this result.
     */
    private String requestId;

    /**
     * The key associated with the augmentation function result, represented as a string value.
     */
    private String key;

    /**
     * The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     */
    private AugmentationFunctionResultType type;

    /**
     * The value of the augmentation function result, represented as an object.
     */
    private Object value;

    /**
     * The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     */
    private AugmentationFunctionResultMetrics augmentationFunctionResultMetrics;

    /**
     * The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     */
    private Map<String, Object> metadata;

    /**
     * Default constructor for AugmentationFunctionResultRecord. Initializes an empty record without setting any properties.
     */
    public AugmentationFunctionResultRecord() { }

    /**
     * Constructor for AugmentationFunctionResultRecord that initializes all properties of the record.
     * @param augmentationFunctionId The unique identifier for the augmentation function that produced this result.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function that produced this result.
     * @param requestId The unique identifier for the augmentation function request that led to this result.
     * @param key The key associated with the augmentation function result, represented as a string value.
     * @param type The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType.
     * @param value The value of the augmentation function result, represented as an object.
     * @param augmentationFunctionResultMetrics The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics.
     * @param metadata The metadata associated with the augmentation function result, represented as a map of string keys to object values.
     */
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

    /**
     * Gets the unique identifier for the augmentation function request that led to this result.
     * @return The unique identifier for the augmentation function request that led to this result.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the unique identifier for the augmentation function request that led to this result.
     * @param requestId The unique identifier for the augmentation function request that led to this result to set.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
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
     * @param key The key associated with the augmentation function result, represented as a string value to set.
     */
    public void setKey(String key) {
        this.key = key;
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
     * @param type The type of the augmentation function result, represented as an enum value of AugmentationFunctionResultType to set.
     */
    public void setType(AugmentationFunctionResultType type) {
        this.type = type;
    }

    /**
     * Gets the value of the augmentation function result, represented as an object.
     * @return The value of the augmentation function result, represented as an object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the augmentation function result, represented as an object.
     * @param value The value of the augmentation function result, represented as an object to set.
     */
    public void setValue(Object value) {
        this.value = value;
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
     * @param augmentationFunctionResultMetrics The metrics associated with the augmentation function result, represented as an instance of AugmentationFunctionResultMetrics to set.
     */
    public void setAugmentationFunctionResultMetrics(AugmentationFunctionResultMetrics augmentationFunctionResultMetrics) {
        this.augmentationFunctionResultMetrics = augmentationFunctionResultMetrics;
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
     * @param metadata The metadata associated with the augmentation function result, represented as a map of string keys to object values to set.
     */
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Gets the unique identifier for the augmentation function that produced this result.
     * @return The unique identifier for the augmentation function that produced this result.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the unique identifier for the augmentation function that produced this result.
     * @param augmentationFunctionId The unique identifier for the augmentation function that produced this result to set.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier for the handler associated with the augmentation function that produced this result.
     * @return The identifier for the handler associated with the augmentation function that produced this result.
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Sets the identifier for the handler associated with the augmentation function that produced this result.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function that produced this result to set.
     */
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

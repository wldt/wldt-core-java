package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.storage.model.StorageRecord;

import java.util.Map;

/**
 * Represents a record of an error that occurred during the execution of an augmentation function in the storage system.
 */
public class AugmentationFunctionErrorRecord extends StorageRecord {

    /**
     * The unique identifier of the augmentation function that encountered the error.
     */
    private String augmentationFunctionId;

    /**
     * The unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     */
    private String augmentationFunctionHandlerId;

    /**
     * The unique identifier for the error instance, which can be used for tracking and correlation purposes.
     */
    private String errorId;

    /**
     * The unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     */
    private String requestId;

    /**
     * The type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     */
    private AugmentationFunctionErrorType errorType;

    /**
     * A descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     */
    private String message;

    /**
     * A map containing any additional metadata associated with the error, which can include contextual information,
     * parameters, or other relevant details that may be useful for understanding and analyzing the error within the system.
     */
    private Map<String, Object> metadata;

    /**
     * Default constructor for the AugmentationFunctionErrorRecord class.
     */
    public AugmentationFunctionErrorRecord() {
    }

    /**
     * Constructor for the AugmentationFunctionErrorRecord class, initializing all properties of the error record.
     * @param augmentationFunctionId The unique identifier of the augmentation function that encountered the error.
     * @param augmentationFunctionHandlerId The unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     * @param errorId The unique identifier for the error instance, which can be used for tracking and correlation purposes.
     * @param requestId The unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     * @param errorType The type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     * @param message A descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     * @param metadata A map containing any additional metadata associated with the error, which can include contextual information, parameters, or other relevant details that may be useful for understanding and analyzing the error within the system.
     */
    public AugmentationFunctionErrorRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, String errorId, String requestId, AugmentationFunctionErrorType errorType, String message, Map<String, Object> metadata) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.errorId = errorId;
        this.requestId = requestId;
        this.errorType = errorType;
        this.message = message;
        this.metadata = metadata;
    }

    /**
     * Gets the unique identifier of the augmentation function that encountered the error.
     * @return The unique identifier of the augmentation function that encountered the error.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the unique identifier of the augmentation function that encountered the error.
     * @param augmentationFunctionId The unique identifier of the augmentation function that encountered the error.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     * @return The unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Sets the unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     * @param augmentationFunctionHandlerId The unique identifier of the specific handler or instance of the augmentation function that encountered the error.
     */
    public void setAugmentationFunctionHandlerId(String augmentationFunctionHandlerId) {
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
    }

    /**
     * Gets the unique identifier for the error instance, which can be used for tracking and correlation purposes.
     * @return The unique identifier for the error instance, which can be used for tracking and correlation purposes.
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Sets the unique identifier for the error instance, which can be used for tracking and correlation purposes.
     * @param errorId The unique identifier for the error instance, which can be used for tracking and correlation purposes.
     */
    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    /**
     * Gets the unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     * @return The unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     * @param requestId The unique identifier of the request that led to the error, allowing for correlation between the error and the specific request that caused it.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     * @return The type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     */
    public AugmentationFunctionErrorType getErrorType() {
        return errorType;
    }

    /**
     * Sets the type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     * @param errorType The type of error that occurred during the execution of the augmentation function, represented as an enum value of AugmentationFunctionErrorType.
     */
    public void setErrorType(AugmentationFunctionErrorType errorType) {
        this.errorType = errorType;
    }

    /**
     * Gets the descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     * @return The descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     * @param message The descriptive message providing details about the error that occurred, which can be used for debugging and analysis purposes.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the map containing any additional metadata associated with the error, which can include contextual information,
     * parameters, or other relevant details that may be useful for understanding and analyzing the error within the system.
     * @return The map containing any additional metadata associated with the error, which can include contextual information,
     * parameters, or other relevant details that may be useful for understanding and analyzing the error within the system.
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Sets the map containing any additional metadata associated with the error, which can include contextual information,
     * @param metadata The map containing any additional metadata associated with the error, which can include contextual information,
     * parameters, or other relevant details that may be useful for understanding and analyzing the error within the system.
     */
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

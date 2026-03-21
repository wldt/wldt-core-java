package it.wldt.augmentation.error;

import java.util.HashMap;
import java.util.UUID;

public class AugmentationFunctionError {

    /**
     * Unique identifier for the error, can be generated using UUID or any other method to ensure uniqueness.
     */
    private final String errorId;

    /**
     * Timestamp of when the error occurred, can be generated using System.currentTimeMillis() or any other method to
     * get the current time in milliseconds.
     */
    private final Long timestamp;

    /**
     * Identifier of the augmentation function that generated the error, this can be used to trace back the error to
     * the specific function that caused it.
     */
    private final String augmentationFunctionId;

    /**
     * Identifier of the augmentation function handler that was executing the function when the error occurred, this
     * can be used to trace back the error to the specific handler that was executing the function when the error occurred.
     */
    private String augmentationFunctionHandlerId;

    /**
     * Identifier of the augmentation function request that was being processed when the error occurred, this can be
     * used to trace back the error to the specific request that was being processed when the error occurred.
     */
    private String augmentationFunctionRequestId;

    /**
     * Type of the error, this can be used to categorize the error and to provide more information about the nature
     * of the error. The error type can be defined as an enum or as a string, depending on the specific use case
     * and requirements.
     */
    private final AugmentationFunctionErrorType errorType;

    /**
     * Message describing the error, this can be used to provide more information about the error and to help with
     * debugging and troubleshooting. The message can be generated based on the specific error and can include details
     * such as the error type, the function that caused the error, and any other relevant information.
     */
    private final String message;

    /**
     * Metadata related to the error, this can be used to provide additional information about the error and to help
     * with debugging and troubleshooting. The metadata can include any relevant information such as the input
     * parameters of the function, the state of the system when the error occurred, and any other relevant information
     * that can help with understanding the error and finding a solution.
     */
    private HashMap<String, Object> metadata;

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param augmentationFunctionId the identifier of the augmentation function that generated the error
     * @param errorType the type of the error
     * @param message the message describing the error
     */
    public AugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionErrorType errorType, String message) {
        this(augmentationFunctionId, errorType, message, System.currentTimeMillis());
    }

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param augmentationFunctionId the identifier of the augmentation function that generated the error
     * @param errorType the type of the error
     * @param message the message describing the error
     * @param timestamp the timestamp of when the error occurred
     */
    public AugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionErrorType errorType, String message, Long timestamp) {
        this(augmentationFunctionId, errorType, message, System.currentTimeMillis(), new HashMap<>());

    }

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param augmentationFunctionId the identifier of the augmentation function that generated the error
     * @param errorType the type of the error
     * @param message the message describing the error
     * @param timestamp the timestamp of when the error occurred
     * @param metadata the metadata related to the error
     */
    public AugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionErrorType errorType, String message, Long timestamp, HashMap<String, Object> metadata) {
        this.errorId = UUID.randomUUID().toString();
        this.augmentationFunctionId = augmentationFunctionId;
        this.errorType = errorType;
        this.message = message;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the unique identifier for the error
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the timestamp of when the error occurred
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the identifier of the augmentation function that generated the error
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the identifier of the augmentation function handler that was executing the function when the error occurred
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the identifier of the augmentation function request that was being processed when the error occurred
     */
    public String getAugmentationFunctionRequestId() {
        return augmentationFunctionRequestId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the type of the error
     */
    public AugmentationFunctionErrorType getErrorType() {
        return errorType;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the message describing the error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the metadata related to the error
     */
    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Setters for the class attributes
     * @param augmentationFunctionHandlerId the identifier of the augmentation function handler that was executing
     *                                      the function when the error occurred
     */
    public void setAugmentationFunctionHandlerId(String augmentationFunctionHandlerId) {
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
    }

    /**
     * Setters for the class attributes
     * @param augmentationFunctionRequestId the identifier of the augmentation function request that was being
     *                                      processed when the error occurred
     */
    public void setAugmentationFunctionRequestId(String augmentationFunctionRequestId) {
        this.augmentationFunctionRequestId = augmentationFunctionRequestId;
    }

    /**
     * Setters for the class attributes
     * @param metadata the metadata related to the error
     */
    public void setMetadata(HashMap<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionError{" +
                "errorId='" + errorId + '\'' +
                ", timestamp=" + timestamp +
                ", augmentationFunctionId='" + augmentationFunctionId + '\'' +
                ", augmentationFunctionHandlerId='" + augmentationFunctionHandlerId + '\'' +
                ", augmentationFunctionRequestId='" + augmentationFunctionRequestId + '\'' +
                ", errorType=" + errorType +
                ", message='" + message + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}

package it.wldt.storage.model.augmentation;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.request.AugmentationFunctionRequestType;
import it.wldt.storage.model.StorageRecord;

/**
 * Represents a record for an augmentation function request in the storage. This record contains information about the
 * augmentation function request, such as the unique identifier for the augmentation function, the handler identifier,
 * the request ID, the context of the request, and the type of request. The AugmentationFunctionRequestRecord class
 * serves as a data structure to hold all relevant information about a specific request for an augmentation function,
 * allowing for better management and tracking of requests within the system.
 */
public class AugmentationFunctionRequestRecord extends StorageRecord {

    /**
     * The unique identifier for the augmentation function associated with this request.
     */
    private String augmentationFunctionId;

    /**
     * The identifier for the handler associated with the augmentation function.
     */
    private String augmentationFunctionHandlerId;

    /**
     * The unique identifier for the augmentation function request.
     */
    private String requestId;

    /**
     * The context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     */
    private AugmentationFunctionContext context;

    /**
     * The type of the augmentation function request, represented as an enum value of AugmentationFunctionRequestType.
     */
    private AugmentationFunctionRequestType type;

    /**
     * Default constructor for AugmentationFunctionRequestRecord. Initializes an empty record without setting any properties.
     */
    public AugmentationFunctionRequestRecord() { }

    /**
     * Constructor for AugmentationFunctionRequestRecord that initializes all properties of the record.
     * @param augmentationFunctionId The unique identifier for the augmentation function associated with this request.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function.
     * @param requestId The unique identifier for the augmentation function request.
     * @param context The context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @param type The type of the augmentation function request, represented as an enum value of AugmentationFunctionRequestType.
     */
    public AugmentationFunctionRequestRecord(String augmentationFunctionId, String augmentationFunctionHandlerId, String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType type) {
        this.augmentationFunctionId = augmentationFunctionId;
        this.augmentationFunctionHandlerId = augmentationFunctionHandlerId;
        this.requestId = requestId;
        this.context = context;
        this.type = type;
    }

    /**
     * Gets the unique identifier for the augmentation function request.
     * @return The unique identifier for the augmentation function request.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets the unique identifier for the augmentation function request.
     * @param requestId The unique identifier for the augmentation function request to set.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets the context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @return The context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     */
    public AugmentationFunctionContext getContext() {
        return context;
    }

    /**
     * Sets the context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @param context The context of the augmentation function request to set, containing all necessary information and parameters required for the execution of the augmentation function.
     */
    public void setContext(AugmentationFunctionContext context) {
        this.context = context;
    }

    /**
     * Gets the type of the augmentation function request, represented as an enum value of AugmentationFunctionRequestType.
     * @return The type of the augmentation function request, represented as an enum value of AugmentationFunctionRequestType.
     */
    public AugmentationFunctionRequestType getType() {
        return type;
    }

    /**
     * Sets the type of the augmentation function request, represented as an enum value of AugmentationFunctionRequestType.
     * @param type The type of the augmentation function request to set, represented as an enum value of AugmentationFunctionRequestType.
     */
    public void setType(AugmentationFunctionRequestType type) {
        this.type = type;
    }

    /**
     * Gets the unique identifier for the augmentation function associated with this request.
     * @return The unique identifier for the augmentation function associated with this request.
     */
    public String getAugmentationFunctionId() {
        return augmentationFunctionId;
    }

    /**
     * Sets the unique identifier for the augmentation function associated with this request.
     * @param augmentationFunctionId The unique identifier for the augmentation function associated with this request to set.
     */
    public void setAugmentationFunctionId(String augmentationFunctionId) {
        this.augmentationFunctionId = augmentationFunctionId;
    }

    /**
     * Gets the identifier for the handler associated with the augmentation function.
     * @return The identifier for the handler associated with the augmentation function.
     */
    public String getAugmentationFunctionHandlerId() {
        return augmentationFunctionHandlerId;
    }

    /**
     * Sets the identifier for the handler associated with the augmentation function.
     * @param augmentationFunctionHandlerId The identifier for the handler associated with the augmentation function to set.
     */
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

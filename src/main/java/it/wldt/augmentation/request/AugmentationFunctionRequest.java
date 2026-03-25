package it.wldt.augmentation.request;

import it.wldt.augmentation.context.AugmentationFunctionContext;

public class AugmentationFunctionRequest {

    private final String requestId;

    private final AugmentationFunctionContext context;

    private final Long timestamp;

    private final AugmentationFunctionRequestType type;

    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType requestType, Long timestamp) {
        this.requestId = requestId;
        this.context = context;
        this.type = requestType;
        this.timestamp = timestamp;
    }

    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType requestType) {
        this(requestId, context, requestType, System.currentTimeMillis());
    }

    public String getRequestId() {
        return requestId;
    }

    public AugmentationFunctionContext getContext() {
        return context;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public AugmentationFunctionRequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionRequest{" +
                "requestId='" + requestId + '\'' +
                ", context=" + context +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}

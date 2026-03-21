package it.wldt.augmentation.request;

import it.wldt.augmentation.context.AugmentationFunctionContext;

public class AugmentationFunctionRequest {

    private final String requestId;

    private final AugmentationFunctionContext context;

    private final Long timestamp;

    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context, Long timestamp) {
        this.requestId = requestId;
        this.context = context;
        this.timestamp = timestamp;
    }

    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context) {
        this(requestId, context, System.currentTimeMillis());
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

        @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AugmentationFunctionRequest{");
        sb.append("id='").append(requestId).append('\'');
        sb.append(", context=").append(context);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}

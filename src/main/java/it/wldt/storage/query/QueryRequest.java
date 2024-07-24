package it.wldt.storage.query;

public class QueryRequest {

    private QueryResourceType resourceType;

    private QueryRequestType requestType;

    long startTimestampMs;

    long endTimestampMs;

    public QueryRequest() {
    }

    public QueryRequest(QueryResourceType resourceType, QueryRequestType requestType, long startTimestampMs, long endTimestampMs) {
        this.resourceType = resourceType;
        this.requestType = requestType;
        this.startTimestampMs = startTimestampMs;
        this.endTimestampMs = endTimestampMs;
    }

    public QueryResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(QueryResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public QueryRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(QueryRequestType requestType) {
        this.requestType = requestType;
    }

    public long getStartTimestampMs() {
        return startTimestampMs;
    }

    public void setStartTimestampMs(long startTimestampMs) {
        this.startTimestampMs = startTimestampMs;
    }

    public long getEndTimestampMs() {
        return endTimestampMs;
    }

    public void setEndTimestampMs(long endTimestampMs) {
        this.endTimestampMs = endTimestampMs;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryRequest{");
        sb.append("resourceType=").append(resourceType);
        sb.append(", requestType=").append(requestType);
        sb.append(", startTimestampMs=").append(startTimestampMs);
        sb.append(", endTimestampMs=").append(endTimestampMs);
        sb.append('}');
        return sb.toString();
    }
}

/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.storage.query;

import java.util.UUID;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This class represents a Query Request to be sent to the Query Executor
 * The Query Request contains all the information needed to perform a query on the storage system
 */
public class QueryRequest {

    // Query Resource Type
    private QueryResourceType resourceType;

    // Query Request Type
    private QueryRequestType requestType;

    // Query Start Timestamp
    private long startTimestampMs;

    // Query End Timestamp
    private long endTimestampMs;

    // Query Start Index
    private int startIndex;

    // Query End Index
    private int endIndex;

    // Query Request Timestamp
    private long requestTimestampMs;

    // Query Request Id (it can be used to simplify the request identification and match the request with the response)
    private String requestId;

    /**
     * Default Constructor
     * Internally generates a unique request id and sets the request timestamp to the current time
     */
    public QueryRequest() {
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestampMs = System.currentTimeMillis();
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

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public long getRequestTimestampMs() {
        return requestTimestampMs;
    }

    public void setRequestTimestampMs(long requestTimestampMs) {
        this.requestTimestampMs = requestTimestampMs;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryRequest{");
        sb.append("resourceType=").append(resourceType);
        sb.append(", requestType=").append(requestType);
        sb.append(", startTimestampMs=").append(startTimestampMs);
        sb.append(", endTimestampMs=").append(endTimestampMs);
        sb.append(", startIndex=").append(startIndex);
        sb.append(", endIndex=").append(endIndex);
        sb.append(", requestTimestampMs=").append(requestTimestampMs);
        sb.append(", requestId='").append(requestId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

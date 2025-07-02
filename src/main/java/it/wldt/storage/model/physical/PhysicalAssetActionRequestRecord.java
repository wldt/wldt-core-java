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
package it.wldt.storage.model.physical;

import it.wldt.storage.model.StorageRecord;

import java.util.Map;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Physical Asset Action Request Record Class
 * Represents a Physical Asset Action Request Record in the storage
 */
public class PhysicalAssetActionRequestRecord extends StorageRecord {

    // Timestamp of the request
    private long requestTimestamp;

    // Action Key associated to the request
    private String actionkey;

    // Request Body
    private Object requestBody;

    // Metadata associated to the request
    private Map<String, Object> requestMetadata;

    /**
     * Default Constructor
     */
    public PhysicalAssetActionRequestRecord() {
    }

    /**
     * Constructor with all the required parameters
     * @param requestTimestamp timestamp of the request
     * @param actionkey action key associated to the request
     * @param requestBody body of the request
     * @param requestMetadata metadata associated to the request
     */
    public PhysicalAssetActionRequestRecord(long requestTimestamp, String actionkey, Object requestBody, Map<String, Object> requestMetadata) {
        this.requestTimestamp = requestTimestamp;
        this.actionkey = actionkey;
        this.requestBody = requestBody;
        this.requestMetadata = requestMetadata;
    }

    /**
     * Get the timestamp of the request
     * @return timestamp of the request
     */
    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    /**
     * Set the timestamp of the request
     * @param requestTimestamp timestamp of the request
     */
    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    /**
     * Get the action key associated to the request
     * @return action key associated to the request
     */
    public String getActionkey() {
        return actionkey;
    }

    /**
     * Set the action key associated to the request
     * @param actionkey action key associated to the request
     */
    public void setActionkey(String actionkey) {
        this.actionkey = actionkey;
    }

    /**
     * Get the body of the request
     * @return body of the request
     */
    public Object getRequestBody() {
        return requestBody;
    }

    /**
     * Set the body of the request
     * @param requestBody body of the request
     */
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * Get the metadata associated to the request
     * @return metadata associated to the request
     */
    public Map<String, Object> getRequestMetadata() {
        return requestMetadata;
    }

    /**
     * Set the metadata associated to the request
     * @param requestMetadata metadata associated to the request
     */
    public void setRequestMetadata(Map<String, Object> requestMetadata) {
        this.requestMetadata = requestMetadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetActionRequestRecord{");
        sb.append("recordId=").append(getId());
        sb.append("requestTimestamp=").append(requestTimestamp);
        sb.append(", actionkey='").append(actionkey).append('\'');
        sb.append(", requestBody=").append(requestBody);
        sb.append(", requestMetadata=").append(requestMetadata);
        sb.append('}');
        return sb.toString();
    }
}

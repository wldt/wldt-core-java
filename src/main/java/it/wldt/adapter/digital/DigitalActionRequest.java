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
package it.wldt.adapter.digital;

import java.util.Map;

/**
 * Author:
 *          Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 20/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic property action associated to the physical asset.
 * Each action is by a key, an action type and a content type used to identify the expected input required by the action.
 * The type of the action can be directly changed by the developer to associate it to a specific ontology or data type.
 *
 */
public class DigitalActionRequest {

    private long requestTimestamp;

    private String actionkey;

    private Object requestBody;

    private Map<String, Object> requestMetadata;

    private DigitalActionRequest() {
    }

    public DigitalActionRequest(long requestTimestamp, String actionkey, Object requestBody, Map<String, Object> requestMetadata) {
        this.requestTimestamp = requestTimestamp;
        this.actionkey = actionkey;
        this.requestBody = requestBody;
        this.requestMetadata = requestMetadata;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getActionkey() {
        return actionkey;
    }

    public void setActionkey(String actionkey) {
        this.actionkey = actionkey;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, Object> getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(Map<String, Object> requestMetadata) {
        this.requestMetadata = requestMetadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetActionRequest{");
        sb.append("requestTimestamp=").append(requestTimestamp);
        sb.append(", actionkey='").append(actionkey).append('\'');
        sb.append(", requestBody=").append(requestBody);
        sb.append(", requestMetadata=").append(requestMetadata);
        sb.append('}');
        return sb.toString();
    }
}

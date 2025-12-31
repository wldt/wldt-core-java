/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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

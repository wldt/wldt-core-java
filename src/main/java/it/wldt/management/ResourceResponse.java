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
package it.wldt.management;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 30/05/2025
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class represents a response for a resource in the Digital Twin framework with a generic type T
 * representing the content of the response.
 */
public class ResourceResponse<T> {

    /**
     * The unique identifier of the resource.
     * It can be used to identify the resource in the response.
     */
    private String resourceId;

    /**
     * The unique identifier of the sub-resource.
     * It can be used to identify the sub-resource in the response.
     */
    private String subResourceId;

    /**
     * The content of the resource.
     * It can be any type of object that represents the resource.
     */
    private T resource;

    /**
     * Metadata associated with the resource response.
     * It can contain additional information about the response.
     */
    private Map<String, Object> metadata;

    /**
     * Indicates whether the response contains an error.
     * If true, the response contains an error code and message.
     */
    private boolean isError = false;

    /**
     * The error code associated with the response.
     * It is set to -1 if there is no error.
     */
    private int errorCode = -1 ;

    /**
     * The error message associated with the response.
     * It is set to null if there is no error.
     */
    private String errorMessage;

    /**
     * Default constructor for ResourceResponse.
     * It is private to prevent instantiation without parameters.
     */
    private ResourceResponse() {
    }

    /**
     * Constructs a ResourceResponse with the specified error code and message.
     * This constructor is used to create an error response.
     *
     * @param errorCode    the error code associated with the response
     * @param errorMessage the error message associated with the response
     */
    public ResourceResponse(int errorCode, String errorMessage) {
        this.isError = true;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructs a ResourceResponse with the specified resourceId, subResourceId, resource, and metadata.
     * The isError field is set to false by default.
     *
     * @param resourceId   the unique identifier of the resource
     * @param resource      the content of the resource
     * @param metadata      metadata associated with the response
     */
    public ResourceResponse(String resourceId, T resource, Map<String, Object> metadata) {
        this(resourceId, null, resource, metadata);
    }

    /**
     * Constructs a ResourceResponse with the specified resourceId and subResourceId.
     * The resource and metadata fields are set to null.
     *
     * @param resourceId   the unique identifier of the resource
     * @param subResourceId the unique identifier of the sub-resource
     * @param resource      the content of the resource
     * @param metadata      metadata associated with the response
     */
    public ResourceResponse(String resourceId, String subResourceId, T resource, Map<String, Object> metadata) {
        this.resourceId = resourceId;
        this.subResourceId = subResourceId;
        this.resource = resource;
        this.metadata = metadata;
    }

    public T getResource() {
        return resource;
    }

    public void setResource(T resource) {
        this.resource = resource;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getSubResourceId() {
        return subResourceId;
    }

    public void setSubResourceId(String subResourceId) {
        this.subResourceId = subResourceId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ResourceResponse{");
        sb.append("resourceId='").append(resourceId).append('\'');
        sb.append(", subResourceId='").append(subResourceId).append('\'');
        sb.append(", resource=").append(resource);
        sb.append(", metadata=").append(metadata);
        sb.append(", isError=").append(isError);
        sb.append(", errorCode=").append(errorCode);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

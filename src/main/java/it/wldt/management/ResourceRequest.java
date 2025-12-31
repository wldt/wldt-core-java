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
 * This class represents a request for a resource in the Digital Twin framework with
 * a generic type T representing the content of the request.
 */
public class ResourceRequest<T> {

    /**
     * The unique identifier of the resource.
     * It can be used to identify the resource in the request.
     */
    private String resourceId;

    /**
     * The unique identifier of the sub-resource.
     * It can be used to identify the sub-resource in the request.
     */
    private String subResourceId;

    /**
     * The content of the resource.
     * It can be any type of object that represents the resource.
     */
    private T content;

    /**
     * Metadata associated with the resource request.
     * It can contain additional information about the request.
     */
    private Map<String, Object> metadata;

    /**
     * Default constructor for ResourceRequest.
     * It is private to prevent instantiation without parameters.
     */
    private ResourceRequest() {
    }

    /**
     * Constructs a ResourceRequest with the specified resourceId.
     * Other fields are set to null.
     *
     * @param resourceId the unique identifier of the resource
     */
    public ResourceRequest(String resourceId) {
        this(resourceId, null, null, null);
    }

    /**
     * Constructs a ResourceRequest with the specified resourceId, content, and metadata.
     * The subResourceId is set to null.
     *
     * @param resourceId the unique identifier of the resource
     * @param content the content of the resource
     * @param metadata metadata associated with the request
     */
    public ResourceRequest(String resourceId, T content, Map<String, Object> metadata) {
        this(resourceId, null, content, metadata);
    }

    /**
     * Constructs a ResourceRequest with the specified resourceId and subResourceId.
     * Content and metadata are set to null.
     *
     * @param resourceId the unique identifier of the resource
     * @param subResourceId the unique identifier of the sub-resource
     */
    public ResourceRequest(String resourceId, String subResourceId) {
        this(resourceId, subResourceId, null, null);
    }

    /**
     * Constructs a ResourceRequest with all fields specified.
     *
     * @param resourceId the unique identifier of the resource
     * @param subResourceId the unique identifier of the sub-resource
     * @param content the content of the resource
     * @param metadata metadata associated with the request
     */
    public ResourceRequest(String resourceId, String subResourceId, T content, Map<String, Object> metadata) {
        this.resourceId = resourceId;
        this.subResourceId = subResourceId;
        this.content = content;
        this.metadata = metadata;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
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
        final StringBuffer sb = new StringBuffer("ResourceRequest{");
        sb.append("resourceId='").append(resourceId).append('\'');
        sb.append(", subResourceId='").append(subResourceId).append('\'');
        sb.append(", content=").append(content);
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}

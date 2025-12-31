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

import java.util.List;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 30/05/2025
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class defines a managed resource in the Digital Twin framework with Input and Output types.
 */
public abstract class ManagedResource<R, I, O> {

    /**
     * The unique identifier of the resource.
     * This ID is used to identify the resource in the system.
     */
    protected String id;

    /**
     * The type of the resource.
     * This can be used to categorize or identify the resource type.
     */
    protected String type;

    /**
     * The display name of the resource.
     * This is used for user-friendly identification of the resource.
     */
    protected String name;

    /**
     * The actual resource object.
     * This is the core resource that is being managed.
     */
    protected R resource;

    /**
     * List of observers that can be notified about changes to the resource.
     * This is used to implement the Observer pattern for resource management.
     */
    private List<IResourceObserver> observers;

    /**
     * Default constructor for ManagedResource.
     * It is private to prevent instantiation without parameters.
     */
    private ManagedResource() {
    }

    /**
     * Constructs a ManagedResource with the specified id, type, name, and resource.
     *
     * @param id       the unique identifier of the resource
     * @param type     the type of the resource
     * @param name     the display name of the resource
     * @param resource the actual resource object
     */
    public ManagedResource(String id, String type, String name, R resource) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.resource = resource;
    }

    /**
     * Gets the unique identifier of the resource.
     * @return the unique identifier of the resource
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the resource.
     * @param id the unique identifier to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the type of the resource.
     * @return the type of the resource
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the resource.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the display name of the resource.
     * @return the display name of the resource
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the resource.
     * @param name the display name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    protected abstract ResourceResponse<O> onCreate(ResourceRequest<I> resourceRequest);

    public ResourceResponse<O> create(ResourceRequest<I> resourceRequest){
        if (resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("Error handling Null Request! Received request: " + resourceRequest);
        }

        ResourceResponse<O> result = onCreate(resourceRequest);

        // Notify observers about the deletion of the resource
        if (observers != null && !result.isError()) {
            for (IResourceObserver observer : observers) {
                observer.onCreate(result.getResourceId(), resourceRequest.getSubResourceId());
            }
        }

        return result;
    }

    protected abstract ResourceResponse<O> onRead(ResourceRequest<I> resourceRequest);

    public ResourceResponse<O> read(ResourceRequest<I> resourceRequest){
        if (resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("Error handling Null Request! Received request: " + resourceRequest);
        }
        return onRead(resourceRequest);
    }

    protected abstract ResourceResponse<O> onDelete(ResourceRequest<I> resourceRequest);

    public ResourceResponse<O> delete(ResourceRequest<I> resourceRequest){
        if (resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("Error handling Null Request! Received request: " + resourceRequest);
        }

        ResourceResponse<O> result = onDelete(resourceRequest);

        // Notify observers about the deletion of the resource
        if (observers != null && !result.isError()) {
            for (IResourceObserver observer : observers) {
                observer.onDelete(result.getResourceId(), resourceRequest.getSubResourceId());
            }
        }

        return result;
    }

    protected abstract ResourceResponse<O> onUpdate(ResourceRequest<I> resourceRequest);

    public ResourceResponse<O> update(ResourceRequest<I> resourceRequest){
        if (resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()) {
            throw new IllegalArgumentException("Error handling Null Request! Received request: " + resourceRequest);
        }

        ResourceResponse<O> result = onUpdate(resourceRequest);

        // Notify observers about the deletion of the resource
        if (observers != null && !result.isError()) {
            for (IResourceObserver observer : observers) {
                observer.onUpdate(result.getResourceId(), resourceRequest.getSubResourceId());
            }
        }

        return result;
    }

    public R getResource() {
        return resource;
    }

    public void setResource(R resource) {
        this.resource = resource;
    }

    /**
     * Adds an observer to the list of observers for this resource.
     * Observers will be notified of changes to the resource.
     *
     * @param observer the observer to add
     */
    public void addObserver(IResourceObserver observer) {
        if (observers == null) {
            observers = new java.util.ArrayList<>();
        }
        observers.add(observer);
    }

    /**
     * Removes an observer from the list of observers for this resource.
     * This is used to stop notifications to the observer.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(IResourceObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ManagedResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", resource=").append(resource);
        sb.append('}');
        return sb.toString();
    }
}

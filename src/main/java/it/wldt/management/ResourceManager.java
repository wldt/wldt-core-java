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

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 30/05/2025
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class manages resources in the Digital Twin framework in a centralized way allowing to add, remove,
 * and manage resources.
 */
public class ResourceManager {

    private final Map<String, ManagedResource<?, ?, ?>> resourcesMap;

    /**
     * The unique identifier of the digital twin associated with this ResourceManager.
     * This ID is used to identify the digital twin in the system.
     */
    private final String digitalTwinId;

    /**
     * List of observers that can be notified about changes on the Resource Manager.
     * This is used to implement the Observer pattern for resource management.
     */
    private List<IResourceManagerObserver> observers;

    /**
     * Default constructor for ResourceManager.
     * It initializes the ResourceManager instance.
     */
    public ResourceManager(String digitalTwinId) {
        this.resourcesMap = new HashMap<>();
        this.digitalTwinId = digitalTwinId;
    }

    /**
     * Adds a ManagedResource to the ResourceManager.
     * @param resource the ManagedResource to add
     */
    public void addResource(ManagedResource<?, ?, ?> resource) {
        if (resource != null && resource.getId() != null) {
            // Add the resource to the map
            resourcesMap.put(resource.getId(), resource);

            // Notify observers about the new resource
            notifyResourceAdded(resource.getId());
        } else {
            throw new IllegalArgumentException("Resource or Resource ID cannot be null");
        }
    }

    /**
     * Removes a ManagedResource from the ResourceManager.
     * @param resource the ManagedResource to remove
     */
    public void removeResource(ManagedResource<?, ?, ?> resource) {
        if (resource != null && resource.getId() != null) {
            // Remove the resource from the map
            resourcesMap.remove(resource.getId());

            // Notify observers about the removal
            notifyResourceRemoved(resource.getId());
        } else {
            throw new IllegalArgumentException("Resource or Resource ID cannot be null");
        }
    }

    /**
     * Removes a ManagedResource from the ResourceManager with the resource Id.
     * @param resourceId of the ManagedResource to remove
     */
    public void removeResource(String resourceId) {
        if (resourceId != null && !resourceId.isEmpty()) {
            // Remove the resource from the map
            resourcesMap.remove(resourceId);

            // Notify observers about the removal
            notifyResourceRemoved(resourceId);
        } else {
            throw new IllegalArgumentException("Resource ID cannot be null");
        }
    }

    /**
     * Updates a ManagedResource in the ResourceManager.
     * @param resource the ManagedResource to update
     */
    public void updateResource(ManagedResource<?, ?, ?> resource) {
        if (resource != null && resource.getId() != null) {
            // Update the resource in the map
            resourcesMap.put(resource.getId(), resource);

            // Notify observers about the update
            notifyResourceUpdated(resource.getId());
        } else {
            throw new IllegalArgumentException("Resource or Resource ID cannot be null");
        }
    }

    /**
     * Clears all resources from the ResourceManager.
     */
    public void clearResourceList() {
        resourcesMap.clear();

        // Notify observers that the resource list has been cleared
        notifyResourceListCleared();
    }

    /**
     * Retrieves a ManagedResource by its ID.
     * @param resourceId the unique identifier of the resource
     * @return the ManagedResource if found, otherwise null
     */
    public Optional<ManagedResource<?, ?, ?>> getResourceById(String resourceId) {
        if (resourceId != null) {
            return Optional.ofNullable(resourcesMap.get(resourceId));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the list of all ResourceHandlers.
     * @return a collection of all ResourceHandlers
     */
    public List<ManagedResource<?, ?, ?>> getResourceList() {
        // TODO Return a copy of the resources map to prevent external modification
        return new ArrayList<>(resourcesMap.values());
    }

    /**
     * Checks if a resource with the given ID exists in the ResourceManager.
     * @param resourceId the unique identifier of the resource
     * @return true if the resource exists, false otherwise
     */
    public boolean containsResource(String resourceId) {
        return resourcesMap.containsKey(resourceId);
    }

    /**
     * Returns the digital twin ID for the ResourceManager.
     */
    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    /**
     * Adds an observer to the list of observers for the resource manager.
     * Observers will be notified of changes on the ResourceManager.
     *
     * @param observer the observer to add
     */
    public void addObserver(IResourceManagerObserver observer) {
        if (observers == null) {
            observers = new java.util.ArrayList<>();
        }
        observers.add(observer);
    }

    /**
     * Removes an observer from the list of observers for the resource manager.
     * This is used to stop notifications to the observer.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(IResourceManagerObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    /**
     * Notifies all observers about a resource with the given ID being added.
     * This method is called when a new resource is added to the ResourceManager.
     *
     * @param resourceId the resourceId that has been added
     */
    public void notifyResourceAdded(String resourceId) {
        if (observers != null) {
            for (IResourceManagerObserver observer : observers) {
                observer.onManagerResourceAdded(resourceId);
            }
        }
    }

    /**
     * Notifies all observers about a resource with the given ID being removed.
     * This method is called when a resource is removed from the ResourceManager.
     *
     * @param resourceId the resourceId that has been removed
     */
    public void notifyResourceRemoved(String resourceId) {
        if (observers != null) {
            for (IResourceManagerObserver observer : observers) {
                observer.onManagerResourceRemoved(resourceId);
            }
        }
    }

    /**
     * Notifies all observers about a resource with the given ID being updated.
     * This method is called when a resource is updated in the ResourceManager.
     *
     * @param resourceId the resourceId that has been updated
     */
    public void notifyResourceUpdated(String resourceId) {
        if (observers != null) {
            for (IResourceManagerObserver observer : observers) {
                observer.onManagerResourceUpdated(resourceId);
            }
        }
    }

    /**
     * Notifies all observers when the list of resources is cleared.
     * This method is called when the ResourceManager's resource list is cleared.
     */
    public void notifyResourceListCleared() {
        if (observers != null) {
            for (IResourceManagerObserver observer : observers) {
                observer.onManagerResourceListCleared();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ResourceManager{");
        sb.append("resourcesMap=").append(resourcesMap);
        sb.append(", digitalTwinId='").append(digitalTwinId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

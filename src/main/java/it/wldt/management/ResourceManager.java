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

package it.wldt.management;


import java.util.*;
import java.util.stream.Collectors;

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

    private final String digitalTwinId;

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
            resourcesMap.put(resource.getId(), resource);
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
            resourcesMap.remove(resource.getId());
        } else {
            throw new IllegalArgumentException("Resource or Resource ID cannot be null");
        }
    }

    /**
     * Updates a ManagedResource in the ResourceManager.
     * @param resource the ManagedResource to update
     */
    public void updateResource(ManagedResource<?, ?, ?> resource) {
        if (resource != null && resource.getId() != null) {
            resourcesMap.put(resource.getId(), resource);
        } else {
            throw new IllegalArgumentException("Resource or Resource ID cannot be null");
        }
    }

    /**
     * Clears all resources from the ResourceManager.
     */
    public void clearResourceList() {
        resourcesMap.clear();
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
     * Returns the digital twin ID for the ResourceManager.
     */
    public String getDigitalTwinId() {
        return digitalTwinId;
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

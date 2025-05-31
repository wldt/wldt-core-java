package it.wldt.management;

import java.util.HashMap;
import java.util.Map;

public class DictionaryManagedResource extends ManagedResource<Map<String, Object>, Object, Object> {

    public DictionaryManagedResource(String id, String type, String name, Map<String, Object> dictionary) {
        super(id, type, name, dictionary);
    }

    public DictionaryManagedResource(String id, String type, String name) {
        super(id, type, name, new HashMap<>());
    }

    @Override
    public ResourceResponse<Object> create(ResourceRequest<Object> resourceRequest) {
        String key = resourceRequest.getResourceId();
        Object value = resourceRequest.getContent();
        resource.put(key, value);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), value, null);
    }

    @Override
    public ResourceResponse<Object> read(ResourceRequest<Object> resourceRequest) {

        // Validate the resource request
        if(resourceRequest == null || resourceRequest.getResourceId() == null)
            return new ResourceResponse<Object>(400, "Invalid Resource Request!");

        // Validate resource id to ensure it is not empty and matches the managed resource ID
        if(resourceRequest.getResourceId().isEmpty() || !resourceRequest.getResourceId().equals(this.getId()))
            return new ResourceResponse<Object>(400, "Resource ID cannot be empty or does not match the managed resource ID!");

        // Check if the request is for a sub-resource associated to a key in the dictionary
        if (resourceRequest.getSubResourceId() != null && !resourceRequest.getSubResourceId().isEmpty()) {
            String subResourceKey = resourceRequest.getSubResourceId();
            Object subResourceValue = resource.get(subResourceKey);
            return new ResourceResponse<Object>(this.getId(), subResourceKey, subResourceValue, null);
        }
        // If no sub-resource is specified, return the entire dictionary
        else {
            return new ResourceResponse<Object>(this.getId(), resource, null);
        }

    }

    @Override
    public ResourceResponse<Object> update(ResourceRequest<Object> resourceRequest) {
        String key = resourceRequest.getResourceId();
        Object value = resourceRequest.getContent();
        resource.put(key, value);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), value, null);
    }

    @Override
    public ResourceResponse<Object> delete(ResourceRequest<Object> resourceRequest) {
        String key = resourceRequest.getResourceId();
        Object value = resource.remove(key);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), value, null);
    }
}
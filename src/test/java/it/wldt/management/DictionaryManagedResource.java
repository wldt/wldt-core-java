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
    protected ResourceResponse<Object> onCreate(ResourceRequest<Object> resourceRequest) {
        // In this case the creation means adding a new key-value pair to the dictionary
        // so we have to take the sub-resource ID as the key and the content as the value since
        // the resource ID is the ID of the dictionary itself.

        if (resourceRequest == null ||
                resourceRequest.getResourceId() == null ||
                resourceRequest.getContent() == null ||
                resourceRequest.getSubResourceId().isEmpty())
            return new ResourceResponse<Object>(400, "Invalid Resource Request!");

        String key = resourceRequest.getSubResourceId();
        Object value = resourceRequest.getContent();
        resource.put(key, value);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), key, value, null);
    }

    @Override
    protected ResourceResponse<Object> onRead(ResourceRequest<Object> resourceRequest) {

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
    protected ResourceResponse<Object> onUpdate(ResourceRequest<Object> resourceRequest) {
        // The key is the sub-resource ID and the value is the content of the request.
        String key = resourceRequest.getSubResourceId();
        Object value = resourceRequest.getContent();
        resource.put(key, value);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), value, null);
    }

    @Override
    protected ResourceResponse<Object> onDelete(ResourceRequest<Object> resourceRequest) {
        // The key is the sub-resource ID and the value is the content of the request.
        String key = resourceRequest.getSubResourceId();
        Object value = resource.remove(key);
        return new ResourceResponse<Object>(resourceRequest.getResourceId(), value, null);
    }
}
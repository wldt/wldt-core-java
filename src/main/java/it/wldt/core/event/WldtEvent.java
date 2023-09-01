package it.wldt.core.event;

import it.wldt.exception.EventBusException;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This method describes and event processed, sent and received through the WLDT Event Bus and
 * any publisher or subscriber
 */
public class WldtEvent<T> {

    private String id;
    private String type;
    private String contentType;
    private T body;
    private Map<String, Object> metadata;
    private long creationTimestamp;

    private WldtEvent() {
        this.metadata = new HashMap<>();
        this.id = UUID.randomUUID().toString();
        this.creationTimestamp = System.currentTimeMillis();
    }

    public WldtEvent(String type) throws EventBusException {
        this();
        this.type = type;
    }

    public WldtEvent(String type, T body) throws EventBusException {
        this(type);
        this.body = body;
        this.contentType = body.getClass().getName();
    }

    public WldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        this(type);
        this.body = body;
        this.metadata = metadata;
        this.contentType = body.getClass().getName();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
        this.contentType = body.getClass().getName();
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Optional<Object> putMetadata(String key, Object value){
        if(this.metadata != null) {
            this.metadata.put(key, value);
            return Optional.of(this.metadata.get(key));
        }
        else
            return Optional.empty();
    }

    public Optional<Object> getMetadata(String key){
        if(this.metadata != null && this.metadata.containsKey(key))
            return Optional.of(this.metadata.get(key));
        else
            return Optional.empty();
    }

    public Optional<Object>  removeMetadata(String key){
        if(this.metadata != null && this.metadata.containsKey(key))
            return Optional.of(this.metadata.remove(key));
        else
            return Optional.empty();
    }

    private void setId(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private void setCreationTimestamp(long creationTimestamp){
        this.creationTimestamp = creationTimestamp;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WldtEvent<?> that = (WldtEvent<?>) o;
        return id.equals(that.id) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WldtEvent{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append(", body=").append(body);
        sb.append(", metadata=").append(metadata);
        sb.append(", creationTimestamp=").append(creationTimestamp);
        sb.append('}');
        return sb.toString();
    }
}


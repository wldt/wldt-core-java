package it.wldt.adapter.physical.event;

import it.wldt.exception.EventBusException;

import java.util.Map;

public class PhysicalAssetEventWldtEvent<T> extends PhysicalAssetWldtEvent<T> {

    public static final String PHYSICAL_EVENT_BASIC_TYPE = "dt.physical.event.event";

    private String physicalEventKey;

    public PhysicalAssetEventWldtEvent(String eventKey) throws EventBusException {
        super(eventKey);
        this.physicalEventKey = eventKey;
    }

    public PhysicalAssetEventWldtEvent(String eventKey, T body) throws EventBusException {
        super(eventKey, body);
        this.physicalEventKey = eventKey;
    }

    public PhysicalAssetEventWldtEvent(String eventKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(eventKey, body, metadata);
        this.physicalEventKey = eventKey;
    }

    public String getPhysicalEventKey() {
        return physicalEventKey;
    }

    public void setPhysicalEventKey(String physicalEventKey) {
        this.physicalEventKey = physicalEventKey;
    }

    @Override
    protected String getBasicEventType() {
        return PHYSICAL_EVENT_BASIC_TYPE;
    }
}

package it.wldt.adapter.physical.event;

import it.wldt.exception.EventBusException;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * A WLDT event describing a physical event occurred on the physical asset connected to a Physical Adapter
 */
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

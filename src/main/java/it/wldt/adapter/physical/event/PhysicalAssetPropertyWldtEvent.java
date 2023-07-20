package it.wldt.adapter.physical.event;

import it.wldt.exception.EventBusException;

import java.util.Map;

public class PhysicalAssetPropertyWldtEvent<T> extends PhysicalAssetWldtEvent<T> {

    public static final String PHYSICAL_EVENT_BASIC_TYPE = "dt.physical.event.property";

    private String physicalPropertyId;

    public PhysicalAssetPropertyWldtEvent(String propertyKey) throws EventBusException {
        super(propertyKey);
        this.physicalPropertyId = propertyKey;
    }

    public PhysicalAssetPropertyWldtEvent(String propertyKey, T body) throws EventBusException {
        super(propertyKey, body);
        this.physicalPropertyId = propertyKey;
    }

    public PhysicalAssetPropertyWldtEvent(String propertyKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(propertyKey, body, metadata);
        this.physicalPropertyId = propertyKey;
    }

    public String getPhysicalPropertyId() {
        return physicalPropertyId;
    }

    public void setPhysicalPropertyId(String physicalPropertyId) {
        this.physicalPropertyId = physicalPropertyId;
    }

    @Override
    protected String getBasicEventType() {
        return PHYSICAL_EVENT_BASIC_TYPE;
    }
}

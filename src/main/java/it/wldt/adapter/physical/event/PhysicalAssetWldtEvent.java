package it.wldt.adapter.physical.event;

import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Map;

public abstract class PhysicalAssetWldtEvent<T> extends WldtEvent<T> {
    public PhysicalAssetWldtEvent(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public PhysicalAssetWldtEvent(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public PhysicalAssetWldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        super(type, body, metadata);
        adaptEventType();
    }

    abstract protected String getBasicEventType();

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getBasicEventType(), this.getType()));
    }

    public static String buildEventType(String basicEventType, String eventType){
        if(eventType != null)
            return String.format("%s.%s", basicEventType, eventType);
        else
            return null;
    }


}

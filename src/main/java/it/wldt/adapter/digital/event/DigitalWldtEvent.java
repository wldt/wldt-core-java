package it.wldt.adapter.digital.event;

import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Map;

public class DigitalWldtEvent<T> extends WldtEvent<T> {

    public static final String DIGITAL_EVENT_BASIC_TYPE = "dt.digital.event";

    public DigitalWldtEvent(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public DigitalWldtEvent(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public DigitalWldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        super(type, body, metadata);
        adaptEventType();
    }

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getType()));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            return String.format("%s.%s", DIGITAL_EVENT_BASIC_TYPE, eventType);
        else
            return null;
    }

}

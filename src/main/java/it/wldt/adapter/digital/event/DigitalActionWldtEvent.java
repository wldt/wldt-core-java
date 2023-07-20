package it.wldt.adapter.digital.event;

import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Map;

public class DigitalActionWldtEvent<T> extends WldtEvent<T> {

    public static final String EVENT_BASIC_TYPE = "dt.digital.event.action";

    private final String actionKey;

    public DigitalActionWldtEvent(String actionKey) throws EventBusException {
        super(actionKey);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public DigitalActionWldtEvent(String actionKey, T body) throws EventBusException {
        super(actionKey, body);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public DigitalActionWldtEvent(String actionKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(actionKey, body, metadata);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public String getActionKey() {
        return actionKey;
    }

    private void adaptEventType(){
        if(actionKey != null)
            this.setType(buildEventType(actionKey));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            return String.format("%s.%s", EVENT_BASIC_TYPE, eventType);
        else
            return null;
    }

}

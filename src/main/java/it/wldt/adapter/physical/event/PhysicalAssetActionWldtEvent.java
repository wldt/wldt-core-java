package it.wldt.adapter.physical.event;

import it.wldt.exception.EventBusException;

import java.util.Map;

public class PhysicalAssetActionWldtEvent<T> extends PhysicalAssetWldtEvent<T> {

    public static final String EVENT_BASIC_TYPE = "dt.physical.event.action";

    private final String actionKey;

    public PhysicalAssetActionWldtEvent(String actionKey) throws EventBusException {
        super(actionKey);
        this.actionKey = actionKey;
    }

    public PhysicalAssetActionWldtEvent(String actionKey, T body) throws EventBusException {
        super(actionKey, body);
        this.actionKey = actionKey;
    }

    public PhysicalAssetActionWldtEvent(String actionKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(actionKey, body, metadata);
        this.actionKey = actionKey;
    }

    public String getActionKey() {
        return actionKey;
    }

    @Override
    protected String getBasicEventType() {
        return EVENT_BASIC_TYPE;
    }
}

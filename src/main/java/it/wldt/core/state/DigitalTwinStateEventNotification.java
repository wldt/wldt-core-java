package it.wldt.core.state;

import it.wldt.exception.EventBusException;
import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Notification associated to events of the Digital Twin State
 */
public class DigitalTwinStateEventNotification<T> {

    public static final String DIGITAL_TWIN_STATE_EVENT_BASIC_TYPE = "dt.digital.event.event";

    private String digitalEventKey;

    private T body;

    private Long timestamp;

    public DigitalTwinStateEventNotification(String digitalEventKey, T body, Long timestamp) {
        this.digitalEventKey = digitalEventKey;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getDigitalEventKey() {
        return digitalEventKey;
    }

    public void setDigitalEventKey(String digitalEventKey) {
        this.digitalEventKey = digitalEventKey;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateEventNotification{");
        sb.append("digitalEventKey='").append(digitalEventKey).append('\'');
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}

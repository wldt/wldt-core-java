package it.wldt.core.adapter.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 27/12/2023 - 11:28
 */
public class SwitchDigitalAdapter extends DigitalAdapter<String> {

    public SwitchDigitalAdapter() {
        super("test-digital-actions-adapter", "");
    }

    @Override
    protected void onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges) {

    }

    @Override
    protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }

    public <T> void invokeAction(String actionKey, T body){
        try {
            System.out.println("INVOKING ACTION: " + actionKey);
            publishDigitalActionWldtEvent(actionKey, body);
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }

}

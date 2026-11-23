package it.wldt.core.state;

import it.wldt.core.event.WldtEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test utility to build states (with the id of the description) and the description / values events of the
 * Digital Twin State, as the Digital Twin State Manager publishes them.
 */
public final class StateTestEvents {

    private StateTestEvents() {
    }

    /**
     * A state with the properties temp (with the given value) and hum (40), and the event alarm if requested
     */
    public static DigitalTwinState state(String descriptionId, int temp, boolean withAlarm) throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", temp));
        properties.put("hum", new DigitalTwinStateProperty<>("hum", 40));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        if (withAlarm)
            events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        return DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new HashMap<>(), events, new HashMap<>()), descriptionId);
    }

    /**
     * The values delta of a state limited to the listed properties
     */
    public static DigitalTwinStateValues delta(DigitalTwinState state, String... properties) {
        return state.getValues().filter(DigitalTwinStateFilter.all()
                .withPropertyNames(properties).withActionNames().withEventNames().withRelationshipNames());
    }

    public static WldtEvent<DigitalTwinStateValues> valuesEvent(DigitalTwinStateValues delta, DigitalTwinStateChange... changes) throws Exception {
        WldtEvent<DigitalTwinStateValues> event = new WldtEvent<>(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());
        event.setBody(delta);
        event.putMetadata(DigitalTwinStateManager.DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
        return event;
    }

    public static WldtEvent<DigitalTwinStateDescription> descriptionEvent(DigitalTwinStateDescription description, DigitalTwinStateChange... changes) throws Exception {
        WldtEvent<DigitalTwinStateDescription> event = new WldtEvent<>(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType());
        event.setBody(description);
        event.putMetadata(DigitalTwinStateManager.DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
        return event;
    }

    public static DigitalTwinStateChange valueChange(String property) throws Exception {
        return new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                DigitalTwinStateChange.ResourceType.PROPERTY_VALUE, new DigitalTwinStateProperty<>(property, 1));
    }
}

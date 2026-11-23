package it.wldt.core.state;

import java.time.Instant;

/**
 * Test utility, in the package of the Digital Twin State, to prepare states with the information that is normally
 * written only through state transactions (last invocation of the actions, last notification of the events,
 * description id).
 */
public final class DigitalTwinStateTestFactory {

    private DigitalTwinStateTestFactory() {
    }

    /**
     * Records the last invocation of an action directly on a state
     */
    public static DigitalTwinState withLastActionInvocation(DigitalTwinState state, String actionKey, Instant instant) {
        state.recordActionInvocation(actionKey, instant);
        return state;
    }

    /**
     * Records the last notification of an event directly on a state
     */
    public static DigitalTwinState withLastEventNotification(DigitalTwinState state, String eventKey, Instant instant) {
        state.recordEventNotification(eventKey, instant);
        return state;
    }

    /**
     * Assigns the id of the description to a state
     */
    public static DigitalTwinState withDescriptionId(DigitalTwinState state, String descriptionId) {
        state.setDescriptionId(descriptionId);
        return state;
    }
}

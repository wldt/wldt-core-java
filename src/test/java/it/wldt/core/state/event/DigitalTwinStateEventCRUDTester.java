package it.wldt.core.state.event;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinStateEventCRUDTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String EVENT_KEY_1 = "testEventKey1";

    private final static String EVENT_KEY_2 = "testEventKey2";

    private final static String EVENT_TYPE = "testEventType";

    private final static String EVENT_TYPE_2 = "testEventType2";

    private DigitalTwinStateManager digitalTwinStateManager;

    private DigitalTwinState digitalTwinState;

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }


    @Test
    public void registerEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        createDigitalTwinStateManager();

        DigitalTwinStateEvent newEvent = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(newEvent);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsEvent(EVENT_KEY_1));
        assertTrue(digitalTwinState.getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinState.getEvent(EVENT_KEY_1).get().getType());
        assertTrue(digitalTwinState.getEventList().isPresent());
        assertEquals(1, digitalTwinState.getEventList().get().size());
    }

    @Test
    public void updateRegisteredEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        registerEvent();

        assertTrue(digitalTwinState.getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinState.getEvent(EVENT_KEY_1).get().getType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateRegisteredEvent(new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE_2));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(EVENT_TYPE_2, digitalTwinState.getEvent(EVENT_KEY_1).get().getType());
    }

    @Test
    public void unregisterEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        registerEvent();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.unRegisterEvent(EVENT_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.containsEvent(EVENT_KEY_1));
        assertFalse(digitalTwinState.getEvent(EVENT_KEY_1).isPresent());
        assertFalse(digitalTwinState.getEventList().isPresent());
    }

}

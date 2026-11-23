package it.wldt.core.state.event;

import it.wldt.core.state.StateManagerTestAccess;
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
    private final static String EVENT_CONTENT_TYPE = "string";

    private DigitalTwinStateManager digitalTwinStateManager;

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }


    @Test
    public void registerEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        createDigitalTwinStateManager();

        DigitalTwinStateEvent newEvent = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE, EVENT_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(newEvent);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsEvent(EVENT_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).get().getType());
        assertEquals(EVENT_CONTENT_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).get().getContentType());
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getEventList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getEventList().get().size());
    }

    @Test
    @SuppressWarnings("deprecation") // the constructor with only the type must keep working until 0.9.0
    public void updateRegisteredEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        registerEvent();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).get().getType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateRegisteredEvent(new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE_2, EVENT_TYPE_2));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(EVENT_TYPE_2, StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).get().getType());
    }

    @Test
    public void unregisterEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        registerEvent();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.unRegisterEvent(EVENT_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).containsEvent(EVENT_KEY_1));
        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).getEvent(EVENT_KEY_1).isPresent());
        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).getEventList().isPresent());
    }

}

package it.wldt.core.state.event;

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

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }


    @Test
    public void registerEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        createDigitalTwinStateManager();

        DigitalTwinStateEvent newEvent = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(newEvent);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsEvent(EVENT_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getEventList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getEventList().get().size());
    }

    @Test
    public void updateRegisteredEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        registerEvent();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).get().getType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateRegisteredEvent(new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE_2));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(EVENT_TYPE_2, digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).get().getType());
    }

    @Test
    public void unregisterEvent() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventException {

        registerEvent();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.unRegisterEvent(EVENT_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinStateManager.getDigitalTwinState().containsEvent(EVENT_KEY_1));
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getEvent(EVENT_KEY_1).isPresent());
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getEventList().isPresent());
    }

}

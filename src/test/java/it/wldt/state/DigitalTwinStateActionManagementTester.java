package it.wldt.state;

import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.exception.WldtDigitalTwinStateActionConflictException;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStateActionNotFoundException;
import it.wldt.exception.WldtDigitalTwinStateException;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DigitalTwinStateActionManagementTester {

    private final static String ACTION_KEY_1 = "testActionKey1";
    private final static String ACTION_KEY_2 = "testActionKey2";
    private final static String ACTION_TYPE = "testActionType";
    private final static String ACTION_CONTENT_TYPE = "application/json";
    private final static String ACTION_CONTENT_TYPE_UPDATED = "text/plain";

    private IDigitalTwinStateManager createDigitalTwinState() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateActionException {
        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);
        IDigitalTwinStateManager digitalTwinState = new DigitalTwinStateManager();
        digitalTwinState.enableAction(action);
        return digitalTwinState;
    }


    @Test
    public void enableAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {
        IDigitalTwinStateManager digitalTwinState = createDigitalTwinState();
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());
    }

    @Test
    public void updateAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {
        IDigitalTwinStateManager digitalTwinState = createDigitalTwinState();
        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
        digitalTwinState.updateAction(new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
    }

    @Test
    public void disableAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {
        IDigitalTwinStateManager digitalTwinState = createDigitalTwinState();
        digitalTwinState.disableAction(ACTION_KEY_1);
        assertFalse(digitalTwinState.containsAction(ACTION_KEY_1));
        assertFalse(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertFalse(digitalTwinState.getActionList().isPresent());
    }

    @Test
    public void completeActionManagement() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {
        IDigitalTwinStateManager digitalTwinState = createDigitalTwinState();
        digitalTwinState.enableAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE));
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(2, digitalTwinState.getActionList().get().size());

        digitalTwinState.updateAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_2));
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinState.getAction(ACTION_KEY_2).get().getContentType());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
        assertEquals(2, digitalTwinState.getActionList().get().size());

        digitalTwinState.disableAction(ACTION_KEY_2);
        assertFalse(digitalTwinState.containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());

        digitalTwinState.disableAction(ACTION_KEY_1);
        assertFalse(digitalTwinState.getActionList().isPresent());
        assertEquals(Optional.empty(), digitalTwinState.getAction(ACTION_KEY_1));
    }
}

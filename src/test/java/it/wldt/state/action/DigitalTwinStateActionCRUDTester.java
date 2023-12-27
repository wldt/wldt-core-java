package it.wldt.state.action;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.exception.WldtDigitalTwinStateActionConflictException;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStateActionNotFoundException;
import it.wldt.exception.WldtDigitalTwinStateException;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinStateActionCRUDTester {

    private final static String ACTION_KEY_1 = "testActionKey1";
    private final static String ACTION_KEY_2 = "testActionKey2";
    private final static String ACTION_TYPE = "testActionType";
    private final static String ACTION_CONTENT_TYPE = "application/json";
    private final static String ACTION_CONTENT_TYPE_UPDATED = "text/plain";

    private DigitalTwinStateManager digitalTwinStateManager;

    private DigitalTwinState digitalTwinState;

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        this.digitalTwinStateManager = new DigitalTwinStateManager();
        this.digitalTwinState = this.digitalTwinStateManager.getDigitalTwinState();
    }


    @Test
    public void enableAction() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        createDigitalTwinStateManager();

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());
    }

    @Test
    public void updateAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        enableAction();

        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
    }

    @Test
    public void disableAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        enableAction();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.containsAction(ACTION_KEY_1));
        assertFalse(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertFalse(digitalTwinState.getActionList().isPresent());
    }

    @Test
    public void completeActionManagement() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        enableAction();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(2, digitalTwinState.getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsAction(ACTION_KEY_2));
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinState.getAction(ACTION_KEY_2).get().getContentType());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
        assertEquals(2, digitalTwinState.getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_2);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.getActionList().isPresent());
        assertEquals(Optional.empty(), digitalTwinState.getAction(ACTION_KEY_1));
    }
}

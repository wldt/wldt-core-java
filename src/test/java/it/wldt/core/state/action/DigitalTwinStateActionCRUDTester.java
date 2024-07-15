package it.wldt.core.state.action;

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

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String ACTION_KEY_1 = "testActionKey1";
    private final static String ACTION_KEY_2 = "testActionKey2";
    private final static String ACTION_TYPE = "testActionType";
    private final static String ACTION_CONTENT_TYPE = "application/json";
    private final static String ACTION_CONTENT_TYPE_UPDATED = "text/plain";


    @Test
    public void enableAction() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        DigitalTwinStateManager digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());
    }

    @Test
    public void updateAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        DigitalTwinStateManager digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        assertTrue(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getContentType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getContentType());
    }

    @Test
    public void disableAction() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        DigitalTwinStateManager digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
    }

    @Test
    public void completeActionManagement() throws WldtDigitalTwinStateActionConflictException, WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateActionNotFoundException {

        DigitalTwinStateManager digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(2, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_2));
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_2).get().getContentType());
        assertEquals(ACTION_CONTENT_TYPE, digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1).get().getContentType());
        assertEquals(2, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_2);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinStateManager.getDigitalTwinState().containsAction(ACTION_KEY_2));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinStateManager.getDigitalTwinState().getActionList().isPresent());
        assertEquals(Optional.empty(), digitalTwinStateManager.getDigitalTwinState().getAction(ACTION_KEY_1));
    }
}

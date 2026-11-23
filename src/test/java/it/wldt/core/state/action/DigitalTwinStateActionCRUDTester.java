package it.wldt.core.state.action;

import it.wldt.core.state.StateManagerTestAccess;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.exception.WldtDigitalTwinStateActionException;
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

    public DigitalTwinStateManager digitalTwinStateManager = null;

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }


    @Test
    public void enableAction() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        createDigitalTwinStateManager();

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getType());
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());
    }

    @Test
    public void updateAction() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        createDigitalTwinStateManager();

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getType());
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_CONTENT_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getContentType());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertEquals(ACTION_CONTENT_TYPE_UPDATED, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getContentType());
    }

    @Test
    public void disableAction() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        createDigitalTwinStateManager();

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getType());
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
    }

    @Test
    public void completeActionManagement() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException {

        createDigitalTwinStateManager();

        DigitalTwinStateAction action = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(action);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getType());
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_1));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_2));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(2, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(new DigitalTwinStateAction(ACTION_KEY_2, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_2));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_2).isPresent());
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_2).get().getContentType());
        assertEquals(ACTION_CONTENT_TYPE, StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1).get().getContentType());
        assertEquals(2, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_2);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).containsAction(ACTION_KEY_2));
        assertTrue(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(1, StateManagerTestAccess.state(digitalTwinStateManager).getActionList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(StateManagerTestAccess.state(digitalTwinStateManager).getActionList().isPresent());
        assertEquals(Optional.empty(), StateManagerTestAccess.state(digitalTwinStateManager).getAction(ACTION_KEY_1));
    }
}

package it.wldt.core.state.relationship;

import it.wldt.core.state.*;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinStateRelationshipCRUDTester {

    private static final String REL_TYPE_1 = "relType1";

    private static final String REL_NAME_1 = "relName1";

    private static final String REL_TYPE_2 = "relType2";

    private static final String REL_NAME_2 = "relName2";

    private static final String REL_INSTANCE_TARGET_ID_1 = "targetId1";

    private static final String REL_INSTANCE_KEY_1 = "relInstanceKey1";

    private DigitalTwinStateManager digitalTwinStateManager;

    private DigitalTwinState digitalTwinState;

    private void createDigitalTwinStateManager() {
        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager();
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }


    @Test
    public void createRelationship() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        createDigitalTwinStateManager();

        DigitalTwinStateRelationship<String> digitalTwinStateRelationship = new DigitalTwinStateRelationship<>(REL_NAME_1, REL_TYPE_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createRelationship(digitalTwinStateRelationship);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinState.getRelationship(REL_NAME_1).isPresent());
        assertEquals(REL_TYPE_1, digitalTwinState.getRelationship(REL_NAME_1).get().getType());
        assertTrue(digitalTwinState.getRelationshipList().isPresent());
        assertEquals(1, digitalTwinState.getRelationshipList().get().size());
    }

    @Test
    public void deleteRelationship() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        createRelationship();

        assertTrue(digitalTwinState.getRelationshipList().isPresent());
        assertEquals(1, digitalTwinState.getRelationshipList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationship(REL_NAME_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinState.containsRelationship(REL_NAME_1));
        assertFalse(digitalTwinState.getRelationship(REL_NAME_1).isPresent());
        assertFalse(digitalTwinState.getRelationshipList().isPresent());
    }

    @Test
    public void createRelationshipInstance() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        createRelationship();

        DigitalTwinStateRelationshipInstance<String> digitalTwinStateRelationshipInstance = new DigitalTwinStateRelationshipInstance<>(REL_NAME_1, REL_INSTANCE_TARGET_ID_1, REL_INSTANCE_KEY_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.addRelationshipInstance(digitalTwinStateRelationshipInstance);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinState.getRelationship(REL_NAME_1).isPresent());
        assertTrue(digitalTwinState.containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1));
        assertNotNull(digitalTwinState.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(REL_INSTANCE_TARGET_ID_1, digitalTwinState.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1).getTargetId());
        assertEquals(1, digitalTwinState.getRelationship(REL_NAME_1).get().getInstances().size());

    }

    @Test
    public void deleteRelationshipInstance() throws WldtDigitalTwinStateException, WldtDigitalTwinStateActionException, WldtDigitalTwinStateEventException {

        createRelationshipInstance();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinState.containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinState.getRelationship(REL_NAME_1).isPresent());
        assertFalse(digitalTwinState.containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_TARGET_ID_1));
        assertNull(digitalTwinState.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(0, digitalTwinState.getRelationship(REL_NAME_1).get().getInstances().size());

    }


}

package it.wldt.core.state.relationship;

import it.wldt.core.state.*;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DigitalTwinStateRelationshipCRUDTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private static final String REL_TYPE_1 = "relType1";

    private static final String REL_NAME_1 = "relName1";

    private static final String REL_TYPE_2 = "relType2";

    private static final String REL_NAME_2 = "relName2";

    private static final String REL_INSTANCE_TARGET_ID_1 = "targetId1";

    private static final String REL_INSTANCE_KEY_1 = "relInstanceKey1";

    private DigitalTwinStateManager digitalTwinStateManager;

    private void createDigitalTwinStateManager() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }


    @Test
    public void createRelationship() throws WldtDigitalTwinStateException {

        createDigitalTwinStateManager();

        DigitalTwinStateRelationship<String> digitalTwinStateRelationship = new DigitalTwinStateRelationship<>(REL_NAME_1, REL_TYPE_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createRelationship(digitalTwinStateRelationship);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).isPresent());
        assertEquals(REL_TYPE_1, digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getType());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getRelationshipList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getRelationshipList().get().size());
    }

    @Test
    public void deleteRelationship() throws WldtDigitalTwinStateException {

        createRelationship();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().getRelationshipList().isPresent());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getRelationshipList().get().size());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationship(REL_NAME_1);
        digitalTwinStateManager.commitStateTransaction();

        assertFalse(digitalTwinStateManager.getDigitalTwinState().containsRelationship(REL_NAME_1));
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).isPresent());
        assertFalse(digitalTwinStateManager.getDigitalTwinState().getRelationshipList().isPresent());
    }

    @Test
    public void createRelationshipInstance() throws WldtDigitalTwinStateException {

        createRelationship();

        DigitalTwinStateRelationshipInstance<String> digitalTwinStateRelationshipInstance = new DigitalTwinStateRelationshipInstance<>(REL_NAME_1, REL_INSTANCE_TARGET_ID_1, REL_INSTANCE_KEY_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.addRelationshipInstance(digitalTwinStateRelationshipInstance);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).isPresent());
        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1));
        assertNotNull(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(REL_INSTANCE_TARGET_ID_1, digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1).getTargetId());
        assertEquals(1, digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getInstances().size());

    }

    @Test
    public void deleteRelationshipInstance() throws WldtDigitalTwinStateException {

        createRelationshipInstance();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(digitalTwinStateManager.getDigitalTwinState().containsRelationship(REL_NAME_1));
        assertTrue(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).isPresent());
        assertFalse(digitalTwinStateManager.getDigitalTwinState().containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_TARGET_ID_1));
        assertNull(digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(0, digitalTwinStateManager.getDigitalTwinState().getRelationship(REL_NAME_1).get().getInstances().size());

    }


}

package it.wldt.core.relationship;

import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.core.twin.DigitalTwin;
import it.wldt.core.relationship.utils.RelationshipDigitalAdapter;
import it.wldt.core.relationship.utils.RelationshipPhysicalAdapter;
import it.wldt.core.relationship.utils.RelationshipShadowingFunction;
import it.wldt.core.relationship.utils.RelationshipsLifeCycleListener;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class PhysicalAdapterRelationshipsDefinitionTester {

    private final String DT_TARGET1_NAME = "dt.target1";
    private final String DT_TARGET2_NAME = "dt.target2";
    private DigitalTwin dt;

    private CountDownLatch syncLatch;
    private RelationshipShadowingFunction shadowingFunction;

    private RelationshipPhysicalAdapter physicalAdapter;

    private RelationshipsLifeCycleListener lifeCycleListener;

    private CountDownLatch relationshipLatch;
    private RelationshipDigitalAdapter digitalAdapter;

    @BeforeEach
    public void setUp() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException {

        System.out.println("Setting up Testing Environment ...");

        syncLatch = new CountDownLatch(1);
        relationshipLatch = new CountDownLatch(2);

        shadowingFunction = new RelationshipShadowingFunction();
        shadowingFunction.setRelationshipLatch(relationshipLatch);

        physicalAdapter = new RelationshipPhysicalAdapter();
        digitalAdapter = new RelationshipDigitalAdapter(null);

        lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);

        dt = new DigitalTwin(shadowingFunction, "relationship-dt");
        dt.addPhysicalAdapter(physicalAdapter);
        dt.addDigitalAdapter(digitalAdapter);
        dt.addLifeCycleListener(lifeCycleListener);
    }

    @AfterEach
    public void tearDown() {

        System.out.println("Cleaning up Testing Environment ...");

        syncLatch = null;
        relationshipLatch = null;
        shadowingFunction = null;
        physicalAdapter = null;
        digitalAdapter = null;
        lifeCycleListener = null;
        dt = null;
    }


    /**The aim of this test is to verify that if a PhysicalAdapter adds one or more PhysicalAssetRelationships to its PhysicalAssetDescription,
     * the ShadowingFunction is able to create the corresponding DigitalTwinStateRelationships and add them to the DigitalTwinState
     * */

    @Test
    @Order(1)
    public void physicalAssetDescriptionAndDTStateTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {

        dt.startLifeCycle();
        syncLatch.await(3000, TimeUnit.MILLISECONDS);

        assertNotNull(lifeCycleListener.getPhysicalAssetDescription());
        assertNotNull(lifeCycleListener.getDigitalTwinState());
        assertEquals(2, lifeCycleListener.getPhysicalAssetDescription().getRelationships().size());

        List<String> relationshipsName = lifeCycleListener.getPhysicalAssetDescription().getRelationships().stream()
                .map(PhysicalAssetRelationship::getName).collect(Collectors.toList());

        assertTrue(relationshipsName.contains(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME));
        assertTrue(relationshipsName.contains(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
        assertTrue(lifeCycleListener.getDigitalTwinState().containsRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME));
        assertTrue(lifeCycleListener.getDigitalTwinState().containsRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
        assertTrue(lifeCycleListener.getDigitalTwinState().getRelationshipList().isPresent());
        assertEquals(2, lifeCycleListener.getDigitalTwinState().getRelationshipList().get().size());

        //At the beginning each relationship has no instances
        assertEquals(0, lifeCycleListener.getDigitalTwinState().getRelationshipList().get().get(0).getInstances().size());
        assertEquals(0, lifeCycleListener.getDigitalTwinState().getRelationshipList().get().get(1).getInstances().size());
        dt.stopLifeCycle();
    }

}

package it.wldt.relationship;

import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.engine.WldtEngine;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhysicalAdapterRelationshipsDefinitionTester {

//
//    private final static String DT_TARGET1_NAME = "dt.target1";
//    private final static String DT_TARGET2_NAME = "dt.target2";
//
//    public WldtEngine initDT(RelationshipShadowingFunction shadowingFunction, RelationshipPhysicalAdapter physicalAdapter, LifeCycleListener listener) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException {
//        WldtEngine dt = new WldtEngine(shadowingFunction, "relationship-dt");
//        dt.addPhysicalAdapter(physicalAdapter);
//        dt.addDigitalAdapter(new RelationshipDigitalAdapter(null));
//        dt.addLifeCycleListener(listener);
//        return dt;
//    }
//
//
//    /**The aim of this test is to verify that if a PhysicalAdapter adds one or more PhysicalAssetRelationships to its PhysicalAssetDescription,
//     * the ShadowingModelFunction is able to create the corresponding DigitalTwinStateRelationships and add them to the DigitalTwinState
//     * */
//
//    @Test
//    @Order(3)
//    public void physicalAssetDescriptionAndDTStateTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
//        CountDownLatch syncLatch = new CountDownLatch(1);
//        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
//        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
//        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
//        WldtEngine dt = initDT(shadowingFunction, physicalAdapter, lifeCycleListener);
//        dt.startLifeCycle();
//        syncLatch.await(3000, TimeUnit.MILLISECONDS);
//        assertNotNull(lifeCycleListener.getPhysicalAssetDescription());
//        assertNotNull(lifeCycleListener.getDigitalTwinStateManager());
//        assertEquals(2, lifeCycleListener.getPhysicalAssetDescription().getRelationships().size());
//        List<String> relationshipsName = lifeCycleListener.getPhysicalAssetDescription().getRelationships().stream()
//                .map(PhysicalAssetRelationship::getName).collect(Collectors.toList());
//        assertTrue(relationshipsName.contains(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME));
//        assertTrue(relationshipsName.contains(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
//        assertTrue(lifeCycleListener.getDigitalTwinStateManager().containsRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME));
//        assertTrue(lifeCycleListener.getDigitalTwinStateManager().containsRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
//        assertTrue(lifeCycleListener.getDigitalTwinStateManager().getRelationshipList().isPresent());
//        assertEquals(2, lifeCycleListener.getDigitalTwinStateManager().getRelationshipList().get().size());
//        //At the beginning each relationship has no instances
//        assertEquals(0, lifeCycleListener.getDigitalTwinStateManager().getRelationshipList().get().get(0).getInstances().size());
//        assertEquals(0, lifeCycleListener.getDigitalTwinStateManager().getRelationshipList().get().get(1).getInstances().size());
//        dt.stopLifeCycle();
//    }
//
//    @Test
//    @Order(1)
//    public void relationshipInstanceCreationTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
//        CountDownLatch syncLatch = new CountDownLatch(1);
//        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
//        CountDownLatch relationshipLatch = new CountDownLatch(1);
//        shadowingFunction.setRelationshipLatch(relationshipLatch);
//        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
//        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
//        WldtEngine dt = initDT(shadowingFunction, physicalAdapter, lifeCycleListener);
//        dt.startLifeCycle();
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, true);
//        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);
//        DigitalTwinStateRelationship<String> containsRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinStateManager().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).get();
//        assertEquals(1, containsRelationship.getInstances().size());
//        assertEquals(DT_TARGET1_NAME, containsRelationship.getInstances().get(0).getTargetId());
//
//    }
//
//    @Test
//    @Order(2)
//    public void multipleRelationshipInstanceCreationTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
//        CountDownLatch syncLatch = new CountDownLatch(1);
//        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
//        CountDownLatch relationshipLatch = new CountDownLatch(3);
//        shadowingFunction.setRelationshipLatch(relationshipLatch);
//        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
//        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
//        WldtEngine dt = initDT(shadowingFunction, physicalAdapter, lifeCycleListener);
//        dt.startLifeCycle();
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, true);
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET2_NAME, true);
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, DT_TARGET1_NAME, true);
//        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);
//        DigitalTwinStateRelationship<String> containsRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinStateManager().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).get();
//        DigitalTwinStateRelationship<String> operatorRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinStateManager().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME).get();
//        assertEquals(2, containsRelationship.getInstances().size());
//        assertEquals(1, operatorRelationship.getInstances().size());
//    }
//
//    @Test
//    public void relationshipInstanceDeletionTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
//        CountDownLatch syncLatch = new CountDownLatch(1);
//        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
//        CountDownLatch relationshipLatch = new CountDownLatch(2);
//        shadowingFunction.setRelationshipLatch(relationshipLatch);
//        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
//        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
//        WldtEngine dt = initDT(shadowingFunction, physicalAdapter, lifeCycleListener);
//        dt.startLifeCycle();
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, true);
//        Thread.sleep(3000);
//        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, false);
//        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);
//        DigitalTwinStateRelationship<String> containsRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinStateManager().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).get();
//        assertEquals(0, containsRelationship.getInstances().size());
//    }

}

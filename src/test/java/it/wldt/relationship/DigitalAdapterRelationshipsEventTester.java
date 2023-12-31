package it.wldt.relationship;

import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.engine.WldtEngine;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigitalAdapterRelationshipsEventTester {

    public WldtEngine initDT(RelationshipShadowingFunction shadowingFunction,RelationshipDigitalAdapter digitalAdapter, RelationshipPhysicalAdapter physicalAdapter, LifeCycleListener listener) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException {
        WldtEngine dt = new WldtEngine(shadowingFunction, "relationship-dt");
        dt.addPhysicalAdapter(physicalAdapter);
        dt.addDigitalAdapter(digitalAdapter);
        dt.addLifeCycleListener(listener);
        return dt;
    }

    @Order(2)
    @Test
    public void digitalAdapterStateWithRelationshipTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
        CountDownLatch syncLatch = new CountDownLatch(1);
        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
        RelationshipDigitalAdapter digitalAdapter = new RelationshipDigitalAdapter(null);
        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
        WldtEngine dt = initDT(shadowingFunction, digitalAdapter, physicalAdapter, lifeCycleListener);
        dt.startLifeCycle();
        syncLatch.await(3000, TimeUnit.MILLISECONDS);
        assertTrue(digitalAdapter.getDigitalTwinState().getRelationshipList().isPresent());
        assertEquals(2, digitalAdapter.getDigitalTwinState().getRelationshipList().get().size());
        assertTrue(digitalAdapter.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME).isPresent());
        assertTrue(digitalAdapter.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).isPresent());
    }

    @Test
    @Order(1)
    public void digitalAdapterReceiveRelationshipsNotification() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {
        CountDownLatch syncLatch = new CountDownLatch(1);
        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
        CountDownLatch relationshipLatch = new CountDownLatch(1);
        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
        List<DigitalTwinStateRelationshipInstance<?>> instances = new ArrayList<>();
        RelationshipDigitalAdapter digitalAdapter = new RelationshipDigitalAdapter(instances);
        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);
        WldtEngine dt = initDT(shadowingFunction, digitalAdapter, physicalAdapter, lifeCycleListener);
        dt.startLifeCycle();
        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, "dt-human", true);
        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);
        assertFalse(instances.isEmpty());
        assertEquals(1, instances.size());
        assertTrue(instances.stream().map(DigitalTwinStateRelationshipInstance::getRelationshipName).collect(Collectors.toList()).contains(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, "dt-human", false);

        assertTrue(instances.isEmpty());

    }
}

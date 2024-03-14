package it.wldt.core.relationship;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.relationship.utils.RelationshipDigitalAdapter;
import it.wldt.core.relationship.utils.RelationshipsLifeCycleListener;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.*;
import it.wldt.core.relationship.utils.RelationshipPhysicalAdapter;
import it.wldt.core.relationship.utils.RelationshipShadowingFunction;
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

    public final String DIGITAL_TWIN_ID = "dt00001";

    public DigitalTwin initDT(RelationshipShadowingFunction shadowingFunction, RelationshipDigitalAdapter digitalAdapter, RelationshipPhysicalAdapter physicalAdapter, LifeCycleListener listener) throws ModelException, WldtRuntimeException, EventBusException, WldtConfigurationException, WldtWorkerException, WldtDigitalTwinStateException {

        DigitalTwin dt = new DigitalTwin(DIGITAL_TWIN_ID, shadowingFunction);
        dt.addPhysicalAdapter(physicalAdapter);
        dt.addDigitalAdapter(digitalAdapter);
        dt.addLifeCycleListener(listener);

        return dt;
    }

    @Order(2)
    @Test
    public void testDigitalAdapterStateWithRelationshipTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        CountDownLatch syncLatch = new CountDownLatch(1);

        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();
        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();
        RelationshipDigitalAdapter digitalAdapter = new RelationshipDigitalAdapter(null);
        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);

        DigitalTwin dt = initDT(shadowingFunction, digitalAdapter, physicalAdapter, lifeCycleListener);

        digitalTwinEngine.addDigitalTwin(dt, true);

        syncLatch.await(3000, TimeUnit.MILLISECONDS);

        assertTrue(digitalAdapter.getDigitalTwinState().getRelationshipList().isPresent());
        assertEquals(2, digitalAdapter.getDigitalTwinState().getRelationshipList().get().size());
        assertTrue(digitalAdapter.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME).isPresent());
        assertTrue(digitalAdapter.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).isPresent());

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);
    }

    @Test
    @Order(1)
    public void testDigitalAdapterReceiveRelationshipsNotification() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        CountDownLatch syncLatch = new CountDownLatch(1);

        RelationshipShadowingFunction shadowingFunction = new RelationshipShadowingFunction();

        CountDownLatch relationshipLatch = new CountDownLatch(1);

        RelationshipPhysicalAdapter physicalAdapter = new RelationshipPhysicalAdapter();

        List<DigitalTwinStateRelationshipInstance<?>> instances = new ArrayList<>();

        RelationshipDigitalAdapter digitalAdapter = new RelationshipDigitalAdapter(instances);
        RelationshipsLifeCycleListener lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);

        DigitalTwin dt = initDT(shadowingFunction, digitalAdapter, physicalAdapter, lifeCycleListener);

        digitalTwinEngine.addDigitalTwin(dt, true);

        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, "dt-human", true);

        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);

        assertFalse(instances.isEmpty());
        assertEquals(1, instances.size());
        assertTrue(instances.stream().map(DigitalTwinStateRelationshipInstance::getRelationshipName).collect(Collectors.toList()).contains(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME));
        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, "dt-human", false);

        assertTrue(instances.isEmpty());

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);

    }
}

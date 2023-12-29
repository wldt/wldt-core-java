package it.wldt.core.relationship;

import it.wldt.core.twin.DigitalTwin;
import it.wldt.core.relationship.utils.RelationshipDigitalAdapter;
import it.wldt.core.relationship.utils.RelationshipPhysicalAdapter;
import it.wldt.core.relationship.utils.RelationshipShadowingFunction;
import it.wldt.core.relationship.utils.RelationshipsLifeCycleListener;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class PhysicalAdapterRelationshipsDeletionTester {

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

    @Test
    @Order(1)
    public void relationshipInstanceDeletionTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException {

        dt.startLifeCycle();

        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, true);

        Thread.sleep(3000);

        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, false);
        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);

        DigitalTwinStateRelationship<String> containsRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).get();

        assertEquals(0, containsRelationship.getInstances().size());
    }

}

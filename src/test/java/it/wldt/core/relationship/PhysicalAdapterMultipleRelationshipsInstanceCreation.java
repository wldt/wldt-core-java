package it.wldt.core.relationship;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.relationship.utils.RelationshipDigitalAdapter;
import it.wldt.core.relationship.utils.RelationshipPhysicalAdapter;
import it.wldt.core.relationship.utils.RelationshipShadowingFunction;
import it.wldt.core.relationship.utils.RelationshipsLifeCycleListener;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(ExecutionMode.SAME_THREAD)
@Isolated
public class PhysicalAdapterMultipleRelationshipsInstanceCreation {

    public final String DIGITAL_TWIN_ID = "dt00001";

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
    public void setUp() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, WldtWorkerException, WldtDigitalTwinStateException {

        System.out.println("Setting up Testing Environment ...");

        syncLatch = new CountDownLatch(1);
        relationshipLatch = new CountDownLatch(2);

        shadowingFunction = new RelationshipShadowingFunction();
        shadowingFunction.setRelationshipLatch(relationshipLatch);

        physicalAdapter = new RelationshipPhysicalAdapter();
        digitalAdapter = new RelationshipDigitalAdapter(null);

        lifeCycleListener = new RelationshipsLifeCycleListener(syncLatch);

        dt = new DigitalTwin(DIGITAL_TWIN_ID, shadowingFunction);
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
    public void multipleRelationshipInstanceCreationTest() throws WldtConfigurationException, InterruptedException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        digitalTwinEngine.addDigitalTwin(dt, true);

        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET1_NAME, true);
        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME, DT_TARGET2_NAME, true);
        physicalAdapter.simulateRelationshipInstanceEvent(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME, DT_TARGET1_NAME, true);

        relationshipLatch.await(3000, TimeUnit.MILLISECONDS);
        DigitalTwinStateRelationship<String> containsRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_CONTAINS_NAME).get();
        DigitalTwinStateRelationship<String> operatorRelationship = (DigitalTwinStateRelationship<String>) lifeCycleListener.getDigitalTwinState().getRelationship(RelationshipPhysicalAdapter.RELATIONSHIP_OPERATOR_NAME).get();
        assertEquals(2, containsRelationship.getInstances().size());
        assertEquals(1, operatorRelationship.getInstances().size());

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);
    }

}

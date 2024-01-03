package it.wldt.core.state.relationship;


import it.wldt.core.event.*;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStateRelationshipObserverTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private static final String REL_TYPE_1 = "relType1";

    private static final String REL_NAME_1 = "relName1";

    private static final String REL_TYPE_2 = "relType2";

    private static final String REL_NAME_2 = "relName2";

    private static final String REL_INSTANCE_TARGET_ID_1 = "targetId1";

    private static final String REL_INSTANCE_KEY_1 = "relInstanceKey1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    public DigitalTwinState digitalTwinState = null;

    public DigitalTwinStateProperty<String> testProperty1 = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private WldtEvent<?> stateUpdatedReceivedWldtEvent;
    private DigitalTwinState receivedDigitalTwinStateUpdate;

    private WldtEvent<?> propertyUpdatedReceivedWldtEvent;
    private DigitalTwinStateProperty<?> propertyUpdatedReceivedProperty;

    private WldtEvent<?> propertyDeletedReceivedWldtEvent;

    private DigitalTwinStateProperty<?> propertyDeletedReceivedProperty;

    private DigitalTwinState receivedPreviousDigitalTwinState;

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    private ArrayList<DigitalTwinStateChange> receivedDigitalTwinStateChangeList;

    private WldtEventListener wldtEventListener;

    private DigitalTwinStateEvent testNewEvent1;

    private DigitalTwinStateRelationship<String> testRelationship1;

    private DigitalTwinStateRelationshipInstance<String> testRelationshipInstance1;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }

    private void createRelationship() throws WldtDigitalTwinStateException {

        testRelationship1 = new DigitalTwinStateRelationship<>(REL_NAME_1, REL_TYPE_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createRelationship(testRelationship1);
        digitalTwinStateManager.commitStateTransaction();
    }

    private void createRelationshipInstance() throws WldtDigitalTwinStateException {

        testRelationshipInstance1 = new DigitalTwinStateRelationshipInstance<>(REL_NAME_1, REL_INSTANCE_TARGET_ID_1, REL_INSTANCE_KEY_1);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.addRelationshipInstance(testRelationshipInstance1);
        digitalTwinStateManager.commitStateTransaction();

    }

    private WldtEventListener getWldtEventListener(){

        if(wldtEventListener == null)
            wldtEventListener = new WldtEventListener() {
                @Override
                public void onEventSubscribed(String eventType) {
                    System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
                }

                @Override
                public void onEventUnSubscribed(String eventType) {
                    System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
                }

                @Override
                public void onEvent(WldtEvent<?> wldtEvent) {

                    System.out.printf("Received Event: %s%n", wldtEvent);

                    stateUpdatedReceivedWldtEvent = wldtEvent;

                    //DT State Events Management
                    if(wldtEvent != null
                            && wldtEvent.getType().equals(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType())
                            && wldtEvent.getBody() != null
                            && (wldtEvent.getBody() instanceof DigitalTwinState)){

                        //Retrieve DT's State Update
                        DigitalTwinState newDigitalTwinState = (DigitalTwinState)wldtEvent.getBody();
                        DigitalTwinState previsousDigitalTwinState = null;
                        ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList = null;

                        Optional<?> prevDigitalTwinStateOptional = wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE);
                        Optional<?> digitalTwinStateChangeListOptional = wldtEvent.getMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST);

                        if(prevDigitalTwinStateOptional.isPresent() && prevDigitalTwinStateOptional.get() instanceof DigitalTwinState)
                            previsousDigitalTwinState = (DigitalTwinState) prevDigitalTwinStateOptional.get();

                        if(digitalTwinStateChangeListOptional.isPresent())
                            digitalTwinStateChangeList = (ArrayList<DigitalTwinStateChange>) digitalTwinStateChangeListOptional.get();


                        receivedDigitalTwinStateUpdate = newDigitalTwinState;
                        receivedPreviousDigitalTwinState = previsousDigitalTwinState;
                        receivedDigitalTwinStateChangeList = digitalTwinStateChangeList;
                    }

                    lock.countDown();
                }
            };

        return wldtEventListener;
    }

    @Test
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateEventException {

        //Init DigitaTwin State
        initTestDtState();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        createRelationship();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the event is correctly available in the State
        assertTrue(receivedDigitalTwinStateUpdate.containsRelationship(REL_NAME_1));
        assertTrue(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).isPresent());
        assertEquals(REL_TYPE_1, receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getType());
        assertTrue(receivedDigitalTwinStateUpdate.getRelationshipList().isPresent());
        assertEquals(1, receivedDigitalTwinStateUpdate.getRelationshipList().get().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_ADD);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.RELATIONSHIP);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testRelationship1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateDeletedRelationship() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateEventException {

        //Init DigitaTwin State
        initTestDtState();

        createRelationship();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationship(REL_NAME_1);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly removed from the State
        assertFalse(receivedDigitalTwinStateUpdate.containsRelationship(REL_NAME_1));
        assertFalse(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).isPresent());
        assertFalse(receivedDigitalTwinStateUpdate.getRelationshipList().isPresent());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_REMOVE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.RELATIONSHIP);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testRelationship1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateChangesCreateRelationshipInstance() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial Relationship
        createRelationship();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        //Create Relationship Instance
        createRelationshipInstance();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the event is correctly available in the State
        assertTrue(receivedDigitalTwinStateUpdate.containsRelationship(REL_NAME_1));
        assertTrue(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).isPresent());
        assertTrue(receivedDigitalTwinStateUpdate.containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1));
        assertNotNull(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(REL_INSTANCE_TARGET_ID_1, receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1).getTargetId());
        assertEquals(1, receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getInstances().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_ADD);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testRelationshipInstance1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateDeletedRelationshipInstance() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateEventException {

        //Init DigitaTwin State
        initTestDtState();

        createRelationship();

        createRelationshipInstance();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.deleteRelationshipInstance(REL_NAME_1, REL_INSTANCE_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly removed from the State
        assertTrue(receivedDigitalTwinStateUpdate.containsRelationship(REL_NAME_1));
        assertTrue(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).isPresent());
        assertFalse(receivedDigitalTwinStateUpdate.containsRelationshipInstance(REL_NAME_1, REL_INSTANCE_TARGET_ID_1));
        assertNull(receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getInstance(REL_INSTANCE_KEY_1));
        assertEquals(0, receivedDigitalTwinStateUpdate.getRelationship(REL_NAME_1).get().getInstances().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_REMOVE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testRelationshipInstance1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }
}

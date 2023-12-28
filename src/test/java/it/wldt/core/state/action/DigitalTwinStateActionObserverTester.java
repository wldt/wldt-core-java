package it.wldt.core.state.action;


import it.wldt.core.event.*;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStateActionObserverTester {

    private final static String ACTION_KEY_1 = "testActionKey1";
    private final static String ACTION_KEY_2 = "testActionKey2";
    private final static String ACTION_TYPE = "testActionType";
    private final static String ACTION_CONTENT_TYPE = "application/json";
    private final static String ACTION_CONTENT_TYPE_UPDATED = "text/plain";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    public DigitalTwinState digitalTwinState = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private WldtEvent<?> stateUpdatedReceivedWldtEvent;
    private DigitalTwinState receivedDigitalTwinStateUpdate;

    private DigitalTwinState receivedPreviousDigitalTwinState;

    private ArrayList<DigitalTwinStateChange> receivedDigitalTwinStateChangeList;

    private WldtEventListener wldtEventListener;

    private DigitalTwinStateAction targetAction1;

    private void initTestDtState() {
        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager();
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }

    private void enableAction() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {

        initTestDtState();

        targetAction1 = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.enableAction(targetAction1);
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
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateActionException {

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
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        enableAction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the action is correctly available in the State
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getType());
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_ADD);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.ACTION);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), targetAction1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateUpdatedAction() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateActionException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial Action to test update
        enableAction();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        //Update Action
        DigitalTwinStateAction updatedAction = new DigitalTwinStateAction(ACTION_KEY_1, ACTION_TYPE, ACTION_CONTENT_TYPE_UPDATED);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateAction(updatedAction);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the action is correctly available in the State
        assertTrue(digitalTwinState.containsAction(ACTION_KEY_1));
        assertTrue(digitalTwinState.getAction(ACTION_KEY_1).isPresent());
        assertEquals(ACTION_TYPE, digitalTwinState.getAction(ACTION_KEY_1).get().getType());
        assertEquals(ACTION_CONTENT_TYPE_UPDATED, digitalTwinState.getAction(ACTION_KEY_1).get().getContentType());
        assertTrue(digitalTwinState.getActionList().isPresent());
        assertEquals(1, digitalTwinState.getActionList().get().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_UPDATE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.ACTION);
        assertEquals(updatedAction, receivedDigitalTwinStateChangeList.get(0).getResource());

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateDeletedAction() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateActionException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial Action to test update
        enableAction();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.disableAction(ACTION_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly removed from the State
        assertFalse(receivedDigitalTwinStateUpdate.getAction(ACTION_KEY_1).isPresent());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_REMOVE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.ACTION);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), targetAction1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }
}

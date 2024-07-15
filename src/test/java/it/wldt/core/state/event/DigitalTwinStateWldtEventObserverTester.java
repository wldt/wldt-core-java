package it.wldt.core.state.event;


import it.wldt.core.event.*;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStateWldtEventObserverTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String EVENT_KEY_1 = "testEventKey1";

    private final static String EVENT_KEY_2 = "testEventKey2";

    private final static String EVENT_TYPE = "testEventType";

    private final static String EVENT_TYPE_2 = "testEventType2";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

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

    private ArrayList<DigitalTwinStateChange> receivedDigitalTwinStateChangeList;

    private WldtEventListener wldtEventListener;

    private DigitalTwinStateEvent testNewEvent1;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null && digitalTwinState == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
            digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
        }
    }

    private void registerEvent() throws WldtDigitalTwinStateException {

        testNewEvent1 = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(testNewEvent1);
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

        registerEvent();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the event is correctly available in the State
        assertTrue(digitalTwinState.containsEvent(EVENT_KEY_1));
        assertTrue(digitalTwinState.getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinState.getEvent(EVENT_KEY_1).get().getType());
        assertTrue(digitalTwinState.getEventList().isPresent());
        assertEquals(1, digitalTwinState.getEventList().get().size());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_ADD);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.EVENT);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testNewEvent1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateUpdatedEvent() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateEventException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial Event to test update
        registerEvent();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        assertTrue(digitalTwinState.getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE, digitalTwinState.getEvent(EVENT_KEY_1).get().getType());

        //Update Event
        digitalTwinStateManager.startStateTransaction();
        DigitalTwinStateEvent updatedEvent = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE_2);
        digitalTwinStateManager.updateRegisteredEvent(updatedEvent);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly updated in the State
        assertTrue(receivedDigitalTwinStateUpdate.getEvent(EVENT_KEY_1).isPresent());
        assertEquals(EVENT_TYPE_2, receivedDigitalTwinStateUpdate.getEvent(EVENT_KEY_1).get().getType());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_UPDATE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.EVENT);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), updatedEvent);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateDeletedEvent() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException, WldtDigitalTwinStateEventException {

        //Init DigitaTwin State
        initTestDtState();

        registerEvent();

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
        digitalTwinStateManager.unRegisterEvent(EVENT_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly removed from the State
        assertFalse(receivedDigitalTwinStateUpdate.containsEvent(EVENT_KEY_1));
        assertFalse(receivedDigitalTwinStateUpdate.getEvent(EVENT_KEY_1).isPresent());
        assertFalse(receivedDigitalTwinStateUpdate.getEventList().isPresent());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_REMOVE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.EVENT);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testNewEvent1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }
}

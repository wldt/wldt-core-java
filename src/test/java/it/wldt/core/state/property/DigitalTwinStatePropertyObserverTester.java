package it.wldt.core.state.property;


import it.wldt.core.event.*;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;

import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import it.wldt.exception.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStatePropertyObserverTester {

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public final String DIGITAL_TWIN_ID = "dt00001";

    public static final String TEST_PROPERTY_TYPE = "test_type";

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

    private void initTestDtState() throws WldtDigitalTwinStateException {
        //Init DigitaTwin State
        digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        digitalTwinState = digitalTwinStateManager.getDigitalTwinState();
    }

    private void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {

        testProperty1 = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
        testProperty1.setType(TEST_PROPERTY_TYPE);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.createProperty(testProperty1);
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
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

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

        createProperty();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property is correctly available in the State
        assertTrue(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).isPresent());
        assertEquals(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).get(), testProperty1);

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_ADD);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.PROPERTY);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testProperty1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateUpdatedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial Property to test update
        createProperty();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        updatedProperty.setType(TEST_PROPERTY_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateProperty(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly updated in the State
        assertTrue(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).isPresent());
        assertEquals(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).get(), updatedProperty);

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_UPDATE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.PROPERTY);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), updatedProperty);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observeStateDeletedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

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
        digitalTwinStateManager.deleteProperty(TEST_PROPERTY_KEY_0001);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly removed from the State
        assertFalse(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).isPresent());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_REMOVE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.PROPERTY);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), testProperty1);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }

    @Test
    public void observePropertyValueUpdated() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Create Initial property for testing
        createProperty();

        //Get original DT State before the test
        DigitalTwinState originalDtState = digitalTwinStateManager.getDigitalTwinState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        updatedProperty.setType(TEST_PROPERTY_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updatePropertyValue(updatedProperty);
        digitalTwinStateManager.commitStateTransaction();

        lock.await(2000, TimeUnit.MILLISECONDS);

        // Check Received Event
        assertNotNull(stateUpdatedReceivedWldtEvent);
        assertEquals(stateUpdatedReceivedWldtEvent.getType(), DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        assertEquals(stateUpdatedReceivedWldtEvent.getBody(), digitalTwinState);

        // Check if the property has been correctly updated in the State
        assertTrue(receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).isPresent());
        assertEquals(updatedProperty, receivedDigitalTwinStateUpdate.getProperty(TEST_PROPERTY_KEY_0001).get());

        // Check Received Previous DT State
        assertNotNull(receivedPreviousDigitalTwinState);
        assertEquals(receivedPreviousDigitalTwinState, originalDtState);

        // Check State Change List
        assertEquals(receivedDigitalTwinStateChangeList.size(), 1);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getOperation(), DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResourceType(), DigitalTwinStateChange.ResourceType.PROPERTY_VALUE);
        assertEquals(receivedDigitalTwinStateChangeList.get(0).getResource(), updatedProperty);

        //Remove Subscription for target topic
        WldtEventBus.getInstance().unSubscribe(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1, wldtEventFilter, getWldtEventListener());
    }
}

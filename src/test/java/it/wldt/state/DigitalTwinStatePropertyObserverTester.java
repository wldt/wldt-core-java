package it.wldt.state;


import it.wldt.core.event.*;
import it.wldt.core.state.DefaultDigitalTwinState;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.IDigitalTwinState;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class DigitalTwinStatePropertyObserverTester {

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static IDigitalTwinState digitalTwinState = null;

    public static DigitalTwinStateProperty<String> testProperty1 = null;

    private CountDownLatch lock = new CountDownLatch(1);

    private WldtEvent<?> propertyCreatedReceivedWldtEvent;
    private DigitalTwinStateProperty<?>  propertyCreatedReceivedProperty;

    private WldtEvent<?> propertyUpdatedReceivedWldtEvent;
    private DigitalTwinStateProperty<?> propertyUpdatedReceivedProperty;

    private WldtEvent<?> propertyDeletedReceivedWldtEvent;
    private DigitalTwinStateProperty<?> propertyDeletedReceivedProperty;

    private void initTestDtState() {
        //Init DigitaTwin State
        digitalTwinState = new DefaultDigitalTwinState();
    }

    private void createProperty() throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStateException {
        testProperty1 = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
        testProperty1.setReadable(true);
        testProperty1.setWritable(true);

        digitalTwinState.createProperty(testProperty1);
    }


    @Test
    public void observeStateCreatedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, new WldtEventListener() {
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

                propertyCreatedReceivedWldtEvent = wldtEvent;

                if(wldtEvent != null && wldtEvent.getBody() != null){

                    //If New Property Created
                    if(wldtEvent.getBody() instanceof DigitalTwinStateProperty &&
                            wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>) wldtEvent.getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) wldtEvent.getBody();
                            propertyCreatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        createProperty();

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(propertyCreatedReceivedWldtEvent);
        assertEquals(propertyCreatedReceivedWldtEvent.getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        assertEquals(propertyCreatedReceivedWldtEvent.getBody(), testProperty1);
        assertEquals(propertyCreatedReceivedProperty, testProperty1);

    }

    @Test
    public void observeStateUpdatedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, new WldtEventListener() {
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

                propertyUpdatedReceivedWldtEvent = wldtEvent;

                if(wldtEvent != null && wldtEvent.getBody() != null){

                    //If New Property Created
                    if(wldtEvent.getBody() instanceof DigitalTwinStateProperty &&
                            wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>) wldtEvent.getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) wldtEvent.getBody();
                            propertyUpdatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(propertyUpdatedReceivedWldtEvent);
        assertEquals(propertyUpdatedReceivedWldtEvent.getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        assertEquals(propertyUpdatedReceivedWldtEvent.getBody(), updatedProperty);
        assertEquals(propertyUpdatedReceivedProperty, updatedProperty);

    }

    @Test
    public void observeStateDeletedProperty() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();

        createProperty();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, new WldtEventListener() {
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

                propertyDeletedReceivedWldtEvent = wldtEvent;

                if(wldtEvent != null && wldtEvent.getBody() != null){

                    //If New Property Created
                    if(wldtEvent.getBody() instanceof DigitalTwinStateProperty &&
                            wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED)) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>) wldtEvent.getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) wldtEvent.getBody();
                            propertyDeletedReceivedProperty = digitalTwinStateProperty;
                        }
                    }

                }

                lock.countDown();
            }
        });

        digitalTwinState.deleteProperty(TEST_PROPERTY_KEY_0001);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(propertyDeletedReceivedWldtEvent);
        assertEquals(propertyDeletedReceivedWldtEvent.getType(), DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);
        assertEquals(propertyDeletedReceivedWldtEvent.getBody(), testProperty1);
        assertEquals(propertyDeletedReceivedProperty, testProperty1);

    }

    @Test
    public void observePropertyUpdated() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();
        createProperty();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(TEST_PROPERTY_KEY_0001));

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, new WldtEventListener() {
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

                propertyUpdatedReceivedWldtEvent = wldtEvent;

                if(wldtEvent != null && wldtEvent.getBody() != null){

                    //If New Property Created
                    if(wldtEvent.getBody() instanceof DigitalTwinStateProperty &&
                            wldtEvent.getType().equals(digitalTwinState.getPropertyUpdatedWldtEventMessageType(TEST_PROPERTY_KEY_0001))) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>) wldtEvent.getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) wldtEvent.getBody();
                            propertyUpdatedReceivedProperty = digitalTwinStateProperty;
                        }
                    }
                }

                lock.countDown();
            }
        });

        //Update Property
        DigitalTwinStateProperty<String> updatedProperty = new DigitalTwinStateProperty<String>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001_UPDATED, true, true);
        digitalTwinState.updateProperty(updatedProperty);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(propertyUpdatedReceivedWldtEvent);
        assertEquals(propertyUpdatedReceivedWldtEvent.getType(), digitalTwinState.getPropertyUpdatedWldtEventMessageType(TEST_PROPERTY_KEY_0001));
        assertEquals(propertyUpdatedReceivedWldtEvent.getBody(), updatedProperty);
        assertEquals(propertyUpdatedReceivedProperty, updatedProperty);

    }

    @Test
    public void observePropertyDeleted() throws WldtDigitalTwinStateException, InterruptedException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException, WldtDigitalTwinStatePropertyNotFoundException, EventBusException {

        //Init DigitaTwin State
        initTestDtState();
        createProperty();

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(TEST_PROPERTY_KEY_0001));

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Subscribe for target topic
        WldtEventBus.getInstance().subscribe(SUBSCRIBER_ID_1, wldtEventFilter, new WldtEventListener() {
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

                propertyDeletedReceivedWldtEvent = wldtEvent;

                if(wldtEvent != null && wldtEvent.getBody() != null){

                    //If New Property Created
                    if(wldtEvent.getBody() instanceof DigitalTwinStateProperty &&
                            wldtEvent.getType().equals(digitalTwinState.getPropertyDeletedWldtEventMessageType(TEST_PROPERTY_KEY_0001))) {

                        //Cast to the right property ! In that case a "simple" String
                        if((((DigitalTwinStateProperty<?>) wldtEvent.getBody()).getValue()) instanceof String){
                            DigitalTwinStateProperty<String> digitalTwinStateProperty = (DigitalTwinStateProperty<String>) wldtEvent.getBody();
                            propertyDeletedReceivedProperty = digitalTwinStateProperty;
                        }
                    }
                }

                lock.countDown();
            }
        });

        digitalTwinState.deleteProperty(TEST_PROPERTY_KEY_0001);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(propertyDeletedReceivedWldtEvent);
        assertEquals(propertyDeletedReceivedWldtEvent.getType(), digitalTwinState.getPropertyDeletedWldtEventMessageType(TEST_PROPERTY_KEY_0001));
        assertEquals(propertyDeletedReceivedWldtEvent.getBody(), testProperty1);
        assertEquals(propertyDeletedReceivedProperty, testProperty1);

    }

}

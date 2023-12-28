package it.wldt.core.event;

import it.wldt.core.event.*;
import it.wldt.exception.EventBusException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class EventBusTester {

    public static final String PUBLISHER_ID_1 = "testModulePublisher1";
    public static final String TEST_TOPIC_1 = "topic0001";
    public static final String TEST_VALUE_0001 = "TEST-STRING-1";
    public static final String METADATA_KEY_TEST_1 = "metadata-key-1";
    public static final String METADATA_VALUE_TEST_1 = "metadata-value-1";
    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public static final String TEST_TOPIC_2 = "topic0002";
    public static final String TEST_TOPIC_3 = "topic0003";
    public static final String TEST_TOPIC_4 = "topic0004";

    public static final int MESSAGE_COUNT = 100;
    public static final int PUBLISHER_SLEEP_TIME_MS = 10;
    public static int receivedMessageCount = 0;
    public static long delaySum = 0;

    private CountDownLatch lock = new CountDownLatch(1);

    private WldtEvent<?> receivedMessage;

    private List<String> targetSubscriptionList = new ArrayList<>();

    @Test
    public void testMultipleSubscriptions() throws InterruptedException, EventBusException {

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        WldtEventListener myWldtEventListener = new WldtEventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onSubscribe() called ! Event-Type:" + eventType);
                targetSubscriptionList.add(eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                System.out.println(SUBSCRIBER_ID_1  + " -> onUnSubscribe() called ! Event-Type:" + eventType);
                targetSubscriptionList.remove(eventType);
            }

            @Override
            public void onEvent(WldtEvent<?> eventMessage) {
                if(eventMessage != null){
                    WldtEvent<String> msg = (WldtEvent<String>)eventMessage;
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    System.out.println("Message Received in: " + diff);
                }

                receivedMessage = eventMessage;
                lock.countDown();
            }
        };


        //Subscribe to TOPIC 1
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myWldtEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //ReSubscribe to TOPIC 1
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myWldtEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //UnSubscribe from Topic 1
        testUnsubscribe(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myWldtEventListener);
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Collections.singletonList(TEST_TOPIC_1), myWldtEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        Thread.sleep(1000);

        //Subscribe to Topic 1 and Topic 2
        testSubscribeToEventTypes(SUBSCRIBER_ID_1, Arrays.asList(TEST_TOPIC_1, TEST_TOPIC_2), myWldtEventListener);
        testEventTransmission(TEST_TOPIC_1, TEST_VALUE_0001);
        testEventTransmission(TEST_TOPIC_2, TEST_VALUE_0001);
        Thread.sleep(1000);

        //UnSubscribe from Topic1
        testUnsubscribe(SUBSCRIBER_ID_1, Arrays.asList(TEST_TOPIC_1), myWldtEventListener);
        testEventTransmission(TEST_TOPIC_2, TEST_VALUE_0001);
        Thread.sleep(1000);

    }

    private void testUnsubscribe(String subscriberId, List<String> typeList, WldtEventListener wldtEventListener) throws EventBusException, InterruptedException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.addAll(typeList);
        WldtEventBus.getInstance().unSubscribe(subscriberId, wldtEventFilter, wldtEventListener);

        Thread.sleep(1000);

        for(String type : typeList)
            assertFalse(targetSubscriptionList.contains(type));
    }

    private void testSubscribeToEventTypes(String subscriberId, List<String> typeList, WldtEventListener wldtEventListener) throws EventBusException, InterruptedException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.addAll(typeList);
        WldtEventBus.getInstance().subscribe(subscriberId, wldtEventFilter, wldtEventListener);

        Thread.sleep(1000);
        assertEquals(targetSubscriptionList, typeList);
    }

    private void testEventTransmission(String targetTopic, String body) throws InterruptedException, EventBusException {

        lock = new CountDownLatch(1);

        //Define New Message
        WldtEvent<String> wldtEvent = new WldtEvent<>(targetTopic);
        wldtEvent.setBody(body);
        wldtEvent.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

        //Publish Message on the target Topic1
        WldtEventBus.getInstance().publishEvent(PUBLISHER_ID_1, wldtEvent);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedMessage);
        assertEquals(wldtEvent, receivedMessage);
        assertEquals(TEST_VALUE_0001, receivedMessage.getBody());
        assertEquals(wldtEvent.getMetadata(), receivedMessage.getMetadata());
    }

    @Test
    public void singlePubSubTest() throws InterruptedException, EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(TEST_TOPIC_1);

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
                if(wldtEvent != null){
                    WldtEvent<String> msg = (WldtEvent<String>) wldtEvent;
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    System.out.println("Message Received in: " + diff);
                }

                receivedMessage = wldtEvent;
                lock.countDown();
            }
        });

        //Define New Message
        WldtEvent<String> wldtEvent = new WldtEvent<>(TEST_TOPIC_1);
        wldtEvent.setBody(TEST_VALUE_0001);
        wldtEvent.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

        //Publish Message on the target Topic1
        WldtEventBus.getInstance().publishEvent(PUBLISHER_ID_1, wldtEvent);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedMessage);
        assertEquals(wldtEvent, receivedMessage);
        assertEquals(TEST_VALUE_0001, receivedMessage.getBody());
        assertEquals(wldtEvent.getMetadata(), receivedMessage.getMetadata());
    }

    @Test
    public void multipleMessagesPubSubTest() throws InterruptedException, EventBusException {

        receivedMessageCount = 0;
        delaySum = 0;

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(TEST_TOPIC_1);

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

                if(wldtEvent != null){
                    receivedMessageCount++;
                    WldtEvent<String> msg = (WldtEvent<String>) wldtEvent;
                    long diff = System.currentTimeMillis() - msg.getCreationTimestamp();
                    delaySum += diff;
                }

                lock.countDown();
            }
        });

        //Publish Message on the target Topic1
        for(int i=0; i<MESSAGE_COUNT; i++) {
            //Define New Message
            WldtEvent<String> wldtEvent = new WldtEvent<>(TEST_TOPIC_1);
            wldtEvent.setBody(TEST_VALUE_0001);
            wldtEvent.putMetadata(METADATA_KEY_TEST_1, METADATA_VALUE_TEST_1);

            WldtEventBus.getInstance().publishEvent(PUBLISHER_ID_1, wldtEvent);
            Thread.sleep(PUBLISHER_SLEEP_TIME_MS);
        }

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertEquals(MESSAGE_COUNT, receivedMessageCount);

        double averageDelay = (double)delaySum / (double)MESSAGE_COUNT;

        System.out.println("Average Internal Delay: " + averageDelay);
    }

}

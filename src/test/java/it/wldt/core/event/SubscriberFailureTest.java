package it.wldt.core.event;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A listener that throws must not stop the delivery of the following events to its subscriber.
 */
public class SubscriberFailureTest {

    private static final String DT_ID = "subscriber-failure-test-dt";

    @BeforeAll
    public static void useDefaultStrategy() {
        WldtEventBus.getInstance().setStrategy(new DefaultEventBusStrategy());
    }

    @AfterAll
    public static void restoreDefaultStrategy() {
        WldtEventBus.getInstance().setStrategy(new DefaultEventBusStrategy());
    }

    @Test
    public void listenerFailureDoesNotStopSubscriber() throws Exception {

        AtomicInteger received = new AtomicInteger();
        CountDownLatch secondEvent = new CountDownLatch(1);

        WldtEventListener listener = new WldtEventListener() {
            @Override
            public void onEventSubscribed(String eventType) {}

            @Override
            public void onEventUnSubscribed(String eventType) {}

            @Override
            public void onEvent(WldtEvent<?> event) {
                if (received.incrementAndGet() == 1)
                    throw new IllegalStateException("listener failure");
                secondEvent.countDown();
            }
        };

        WldtEventFilter filter = new WldtEventFilter();
        filter.add("test.failure");
        WldtEventBus.getInstance().subscribe(DT_ID, "failing-subscriber", filter, listener);

        WldtEventBus.getInstance().publishEvent(DT_ID, "publisher", new WldtEvent<>("test.failure"));
        WldtEventBus.getInstance().publishEvent(DT_ID, "publisher", new WldtEvent<>("test.failure"));

        assertTrue(secondEvent.await(5, TimeUnit.SECONDS), "the second event must still be delivered");
    }
}

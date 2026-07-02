package it.wldt.core.event;

import it.wldt.core.event.utils.EventBusTestUtils;
import it.wldt.exception.EventBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure correctness tests for WldtEventBus.
 *
 * No metrics, no latency measurement. All tests exploit the synchronous delivery model:
 * publishEvent() returns only after all onEvent() callbacks complete, so no latches are needed.
 *
 * OldDeprecatedStrategy is set explicitly in @BeforeEach so that correctness assertions
 * remain valid without CountDownLatches, even though the bus default is now asynchronous.
 */
public class EventBusCorrectnessTest {

    private static final String DT_A = "dt-correctness-a";
    private static final String DT_B = "dt-correctness-b";
    private static final String PUB  = "pub";
    private static final String TOPIC = "test.correctness.event";
    private static final String WILDCARD = "test.correctness.*";

    @BeforeEach
    void reset() throws Exception {
        EventBusTestUtils.resetEventBus();
        WldtEventBus.getInstance().setStrategy(new OldDeprecatedStrategy());
    }

    @Test
    void singlePubSubCorrectness() throws EventBusException {
        WldtEvent<?>[] capture = new WldtEvent<?>[1];
        WldtEventFilter filter = new WldtEventFilter();
        filter.add(TOPIC);
        WldtEventBus.getInstance().subscribe(DT_A, "sub", filter, EventBusTestUtils.capturingListener(capture));

        WldtEvent<String> sent = new WldtEvent<>(TOPIC, "hello-world");
        sent.putMetadata("key", "value");
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, sent);

        assertNotNull(capture[0]);
        assertEquals("hello-world", capture[0].getBody());
        assertEquals(sent.getId(), capture[0].getId());
    }

    @Test
    void eventMetadataPreservation() throws EventBusException {
        WldtEvent<?>[] capture = new WldtEvent<?>[1];
        WldtEventFilter filter = new WldtEventFilter();
        filter.add(TOPIC);
        WldtEventBus.getInstance().subscribe(DT_A, "sub", filter, EventBusTestUtils.capturingListener(capture));

        Map<String, Object> meta = new HashMap<>();
        meta.put("source", "sensor-01");
        meta.put("unit", "celsius");
        meta.put("priority", 42);

        WldtEvent<String> sent = new WldtEvent<>(TOPIC, "23.5");
        for (Map.Entry<String, Object> e : meta.entrySet()) sent.putMetadata(e.getKey(), e.getValue());
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, sent);

        assertNotNull(capture[0]);
        assertEquals(meta, capture[0].getMetadata());
    }

    @Test
    void wildcardMatchCorrectness() throws EventBusException {
        AtomicInteger wildcardReceived = new AtomicInteger(0);
        AtomicInteger exactReceived    = new AtomicInteger(0);

        WldtEventFilter wildcardFilter = new WldtEventFilter();
        wildcardFilter.add(WILDCARD);
        WldtEventBus.getInstance().subscribe(DT_A, "wildcard-sub", wildcardFilter,
                EventBusTestUtils.countingListener(wildcardReceived));

        WldtEventFilter exactFilter = new WldtEventFilter();
        exactFilter.add(TOPIC);
        WldtEventBus.getInstance().subscribe(DT_A, "exact-sub", exactFilter,
                EventBusTestUtils.countingListener(exactReceived));

        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>(TOPIC, "v1"));
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>("test.correctness.other", "v2"));
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>("unrelated.topic", "v3"));

        assertEquals(2, wildcardReceived.get(), "Wildcard should match both test.correctness.* events");
        assertEquals(1, exactReceived.get(),    "Exact should match only the exact topic");
    }

    @Test
    void subscribeUnsubscribeCorrectness() throws EventBusException {
        AtomicInteger received = new AtomicInteger(0);
        WldtEventListener listener = EventBusTestUtils.countingListener(received);
        WldtEventFilter filter = new WldtEventFilter();
        filter.add(TOPIC);

        WldtEventBus.getInstance().subscribe(DT_A, "sub", filter, listener);
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>(TOPIC, "before-unsub"));
        assertEquals(1, received.get());

        WldtEventBus.getInstance().unSubscribe(DT_A, "sub", filter, listener);
        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>(TOPIC, "after-unsub"));
        assertEquals(1, received.get(), "No additional events after unsubscribe");
    }

    @Test
    void multiSubscriberCorrectness() throws EventBusException {
        int subscriberCount = 10;
        AtomicInteger totalReceived = new AtomicInteger(0);
        WldtEventListener shared = EventBusTestUtils.countingListener(totalReceived);

        for (int i = 0; i < subscriberCount; i++) {
            WldtEventFilter f = new WldtEventFilter();
            f.add(TOPIC);
            WldtEventBus.getInstance().subscribe(DT_A, "sub-" + i, f, shared);
        }

        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>(TOPIC, "broadcast"));

        assertEquals(subscriberCount, totalReceived.get(), "All subscribers must receive the event");
    }

    @Test
    void dtIsolationCorrectness() throws EventBusException {
        AtomicInteger receivedA = new AtomicInteger(0);
        AtomicInteger receivedB = new AtomicInteger(0);

        WldtEventFilter fa = new WldtEventFilter();
        fa.add(TOPIC);
        WldtEventBus.getInstance().subscribe(DT_A, "sub-a", fa, EventBusTestUtils.countingListener(receivedA));

        WldtEventFilter fb = new WldtEventFilter();
        fb.add(TOPIC);
        WldtEventBus.getInstance().subscribe(DT_B, "sub-b", fb, EventBusTestUtils.countingListener(receivedB));

        WldtEventBus.getInstance().publishEvent(DT_A, PUB, new WldtEvent<>(TOPIC, "for-a-only"));

        assertEquals(1, receivedA.get(), "DT-A subscriber must receive the event");
        assertEquals(0, receivedB.get(), "DT-B subscriber must NOT receive DT-A events");
    }
}

package it.wldt.utils;

import it.wldt.core.event.DefaultEventBusStrategy;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

/**
 * Test isolation for the singleton event bus. Many tests create Digital Twins with the same ids in the same JVM and a
 * subscriber is identified only by its id, so the subscriptions left by the components of a stopped Digital Twin would
 * keep being fed (with the state, and the description ids, of the previous Digital Twin) instead of the ones of the
 * new Digital Twin. After every test the bus is recreated with the default strategy, removing every leftover subscription.
 * <p>
 * The queue of every Digital Twin id and the delivery thread of every subscriber id already known by the bus are recreated
 * (with a no-op subscription), so the bus is as ready as one that has already been used: the first real subscriptions are as
 * fast as before and the events published just before them (for example the first life cycle events) are not lost
 * because the delivery threads were not there yet.
 * The tests of the event bus itself (package {@code it.wldt.core.event}) configure the bus on their own and are skipped.
 * Registered automatically (see {@code META-INF/services} and {@code junit-platform.properties}).
 */
public class EventBusIsolationExtension implements AfterEachCallback {

    private static final String WARM_UP_SUBSCRIBER_ID = "bus-isolation-warm-up";
    private static final String WARM_UP_EVENT_TYPE = "bus.isolation.warm-up";

    @Override
    public void afterEach(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        if (testClass.getName().startsWith("it.wldt.core.event."))
            return;

        try {
            WldtEventBus bus = WldtEventBus.getInstance();
            java.util.List<String> knownDigitalTwinIds = knownKeys(bus, "dtQueues");
            java.util.List<String> knownSubscriberIds = knownKeys(bus, "subscriberThreads");

            bus.setStrategy(new DefaultEventBusStrategy());

            WldtEventFilter filter = new WldtEventFilter();
            filter.add(WARM_UP_EVENT_TYPE);
            WldtEventListener noOp = new WldtEventListener() {
                @Override public void onEventSubscribed(String eventType) { }
                @Override public void onEventUnSubscribed(String eventType) { }
                @Override public void onEvent(WldtEvent<?> wldtEvent) { }
            };
            for (String digitalTwinId : knownDigitalTwinIds)
                bus.subscribe(digitalTwinId, WARM_UP_SUBSCRIBER_ID, filter, noOp);

            // The delivery thread of a subscriber is created with its first subscription
            if (!knownDigitalTwinIds.isEmpty())
                for (String subscriberId : knownSubscriberIds)
                    bus.subscribe(knownDigitalTwinIds.get(0), subscriberId, filter, noOp);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to isolate the event bus between the tests", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<String> knownKeys(WldtEventBus bus, String mapFieldName) throws ReflectiveOperationException {
        Field strategyField = WldtEventBus.class.getDeclaredField("strategy");
        strategyField.setAccessible(true);
        Object strategy = strategyField.get(bus);
        if (!(strategy instanceof DefaultEventBusStrategy))
            return new ArrayList<>();

        Field mapField = DefaultEventBusStrategy.class.getDeclaredField(mapFieldName);
        mapField.setAccessible(true);
        return new ArrayList<>(((Map<String, ?>) mapField.get(strategy)).keySet());
    }
}

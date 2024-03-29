package it.wldt.core.adapter;

import it.wldt.core.adapter.shadowing.TestShadowingFunction;
import it.wldt.core.adapter.digital.SwitchDigitalAdapter;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.exception.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigitalMultipleActionsTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String ACTION1_KEY = "switch-on";
    private final static String ACTION2_KEY = "switch-off";

    private final static Logger logger = LoggerFactory.getLogger(DigitalMultipleActionsTester.class);

    private PhysicalAdapter createPhysicalAdapter(String id, String actionKey, CountDownLatch countDown, List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived){
        return new PhysicalAdapter(id) {

            @Override
            public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
                logger.info("Physical Adapter {} received action event: {}", this.getId(), physicalActionEvent);
                physicalAssetActionEventReceived.add(physicalActionEvent);
                countDown.countDown();
                logger.debug("PA action Processed");
            }

            @Override
            public void onAdapterStart() {
                try {
                    notifyPhysicalAdapterBound(new PhysicalAssetDescription(Collections.singletonList(new PhysicalAssetAction(actionKey, "sensor.actuation", "text/plain")), new ArrayList<>(), new ArrayList<>()));
                } catch (PhysicalAdapterException | EventBusException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAdapterStop() {

            }
        };
    }

    @Test
    @Order(1)
    public void multiplePhysicalAdapterTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        CountDownLatch countDown = new CountDownLatch(2);

        List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived1 = new ArrayList<>();
        List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived2 = new ArrayList<>();

        DigitalTwin dt = new DigitalTwin(DIGITAL_TWIN_ID, new TestShadowingFunction());
        dt.addPhysicalAdapter(createPhysicalAdapter("test.digital.actions.pa", ACTION1_KEY, countDown, physicalAssetActionEventReceived1));
        dt.addPhysicalAdapter(createPhysicalAdapter("test.digital.actions.pa2", ACTION2_KEY, countDown, physicalAssetActionEventReceived2));

        SwitchDigitalAdapter digitalAdapter = new SwitchDigitalAdapter();
        dt.addDigitalAdapter(digitalAdapter);

        digitalTwinEngine.addDigitalTwin(dt, true);

        assertEquals(0, physicalAssetActionEventReceived1.size());
        assertEquals(0, physicalAssetActionEventReceived2.size());

        digitalAdapter.invokeAction(ACTION1_KEY, "foo");
        digitalAdapter.invokeAction(ACTION2_KEY, "bar");

        countDown.await(5000, TimeUnit.MILLISECONDS);

        assertEquals(1, physicalAssetActionEventReceived1.size());
        assertTrue(physicalAssetActionEventReceived1.stream()
                .map(PhysicalAssetActionWldtEvent::getBody)
                .collect(Collectors.toList()).containsAll(Arrays.asList("foo")));

        physicalAssetActionEventReceived1.clear();

        assertEquals(1, physicalAssetActionEventReceived2.size());
        assertTrue(physicalAssetActionEventReceived2.stream()
                .map(PhysicalAssetActionWldtEvent::getBody)
                .collect(Collectors.toList()).containsAll(Arrays.asList("bar")));

        physicalAssetActionEventReceived2.clear();

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);
    }

}

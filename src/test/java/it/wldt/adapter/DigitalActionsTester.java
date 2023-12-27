package it.wldt.adapter;

import it.wldt.adapter.instance.DummyShadowingFunction;
import it.wldt.adapter.instance.SwitchDigitalAdapter;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.engine.WldtEngine;
import it.wldt.exception.*;
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

public class DigitalActionsTester {

    private final static String ACTION1_KEY = "switch-on";
    private final static String ACTION2_KEY = "switch-off";

    private final static Logger logger = LoggerFactory.getLogger(DigitalActionsTester.class);

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
    public void basicDigitalActionTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException {
        CountDownLatch countDown = new CountDownLatch(1);
        List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived = new ArrayList<>();
        WldtEngine dtEngine = new WldtEngine(new DummyShadowingFunction(), "digital.actions.tester.dt");
        dtEngine.addPhysicalAdapter(createPhysicalAdapter("test.digital.actions.pa", ACTION1_KEY, countDown, physicalAssetActionEventReceived));
        SwitchDigitalAdapter digitalAdapter = new SwitchDigitalAdapter();
        dtEngine.addDigitalAdapter(digitalAdapter);
        dtEngine.startLifeCycle();
        Thread.sleep(2000);
        assertEquals(0, physicalAssetActionEventReceived.size());
        digitalAdapter.invokeAction(ACTION1_KEY, "foo");
        countDown.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(1, physicalAssetActionEventReceived.size());
        assertEquals("foo", physicalAssetActionEventReceived.get(0).getBody());
        physicalAssetActionEventReceived.clear();
        dtEngine.stopLifeCycle();
    }

    @Test
    public void multiplePhysicalAdapterTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException {
        CountDownLatch countDown = new CountDownLatch(2);
        List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived = new ArrayList<>();
        WldtEngine dtEngine = new WldtEngine(new DummyShadowingFunction(), "test-digital-twin");
        dtEngine.addPhysicalAdapter(createPhysicalAdapter("test.digital.actions.pa", ACTION1_KEY, countDown, physicalAssetActionEventReceived));
        dtEngine.addPhysicalAdapter(createPhysicalAdapter("test.digital.actions.pa2", ACTION2_KEY, countDown, physicalAssetActionEventReceived));
        SwitchDigitalAdapter digitalAdapter = new SwitchDigitalAdapter();
        dtEngine.addDigitalAdapter(digitalAdapter);
        dtEngine.startLifeCycle();
        assertEquals(0, physicalAssetActionEventReceived.size());
        digitalAdapter.invokeAction(ACTION1_KEY, "foo");
        digitalAdapter.invokeAction(ACTION2_KEY, "bar");
        countDown.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(2, physicalAssetActionEventReceived.size());
        assertTrue(physicalAssetActionEventReceived.stream()
                .map(PhysicalAssetActionWldtEvent::getBody)
                .collect(Collectors.toList()).containsAll(Arrays.asList("foo", "bar")));
        physicalAssetActionEventReceived.clear();
        dtEngine.stopLifeCycle();
    }

}

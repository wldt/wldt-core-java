package it.wldt.core.adapter;

import it.wldt.core.adapter.digital.SwitchDigitalAdapter;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.adapter.shadowing.TestShadowingFunction;
import it.wldt.core.engine.WldtEngine;
import it.wldt.exception.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigitalSingleActionTester {

    private final static String ACTION1_KEY = "switch-on";
    private final static String ACTION2_KEY = "switch-off";

    private final static Logger logger = LoggerFactory.getLogger(DigitalSingleActionTester.class);

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
    public void basicDigitalActionTest() throws WldtConfigurationException, InterruptedException, ModelException, WldtRuntimeException, EventBusException {

        CountDownLatch countDown = new CountDownLatch(1);

        List<PhysicalAssetActionWldtEvent<?>> physicalAssetActionEventReceived = new ArrayList<>();

        WldtEngine dtEngine = new WldtEngine(new TestShadowingFunction(), "digital.actions.tester.dt");
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

}

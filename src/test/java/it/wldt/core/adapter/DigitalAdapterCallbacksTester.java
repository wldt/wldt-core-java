package it.wldt.core.adapter;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.digital.DigitalAdapterLifeCycleListener;
import it.wldt.core.adapter.shadowing.TestShadowingFunction;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static it.wldt.core.adapter.DigitalAdapterCallbacksTester.DigitalAdapterCallbacks.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DigitalAdapterCallbacksTester {

    private static final Logger logger = LoggerFactory.getLogger(DigitalAdapterCallbacksTester.class);

    public final String DIGITAL_TWIN_ID = "dt00001";

    private DigitalTwin createDigitalTwin(ShadowingFunction shadowingFunction, List<PhysicalAdapter> paAdapters, List<DigitalAdapter<?>> daAdapters) throws ModelException, WldtRuntimeException, EventBusException, WldtWorkerException, WldtDigitalTwinStateException {

        DigitalTwin dt = new DigitalTwin(DIGITAL_TWIN_ID, shadowingFunction);

        paAdapters.forEach(pa -> {
            try {
                dt.addPhysicalAdapter(pa);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        daAdapters.forEach(da -> {
            try {
                dt.addDigitalAdapter(da);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return dt;
    }
    private DigitalAdapterLifeCycleListener createDigitalAdapterLifeCycleLister(List<DigitalAdapterCallbacks> receivedCallbacks){
        return new DigitalAdapterLifeCycleListener() {
            @Override
            public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                receivedCallbacks.add(ON_PA_BOUND);
            }

            @Override
            public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                receivedCallbacks.add(ON_PA_UPDATE);
            }

            @Override
            public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
                receivedCallbacks.add(ON_PA_UN_BOUND);
            }

            @Override
            public void onDigitalAdapterBound(String adapterId) {
                receivedCallbacks.add(ON_DA_BOUND);
            }

            @Override
            public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
                receivedCallbacks.add(ON_DA_UN_BOUND);
            }

            @Override
            public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
                logger.debug("Test Digital Adapter - onDigitalTwinBound");
                receivedCallbacks.add(ON_DT_BOUND);
            }

            @Override
            public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
                logger.debug("Test Digital Adapter - onDigitalTwinUnBound");
                receivedCallbacks.add(ON_DT_UN_BOUND);
            }
        };
    }
    private DigitalAdapter<String> createDigitalAdapter(String id , List<DigitalAdapterCallbacks> receivedCallbacks){

        return new DigitalAdapter<String>(id) {

            @Override
            protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

            }

            @Override
            protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

            }

            @Override
            public void onAdapterStart() {
                receivedCallbacks.add(ON_START);
                notifyDigitalAdapterBound();
            }

            @Override
            public void onAdapterStop() {
                receivedCallbacks.add(ON_STOP);
            }

            @Override
            public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
                receivedCallbacks.add(ON_DT_SYNC);
            }

            @Override
            public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {
                receivedCallbacks.add(ON_DT_UN_SYNC);
            }

            @Override
            public void onDigitalTwinCreate() {
                logger.debug("Test Digital Adapter - onDigitalTwinCreate");
                receivedCallbacks.add(ON_DT_CREATE);

            }

            @Override
            public void onDigitalTwinStart() {
                logger.debug("Test Digital Adapter - onDigitalTwinStart");
                receivedCallbacks.add(ON_DT_START);
            }

            @Override
            public void onDigitalTwinStop() {
                logger.debug("Test Digital Adapter - onDigitalTwinStop");
                receivedCallbacks.add(ON_DT_STOP);
            }

            @Override
            public void onDigitalTwinDestroy() {
                logger.debug("Test Digital Adapter - onDigitalTwinDestroy");
                receivedCallbacks.add(ON_DT_DESTROY);
            }
        };
    }
    private PhysicalAdapter createPhysicalAdapter(String id, List<String> propertiesKeys){
        return new PhysicalAdapter(id) {
            @Override
            public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {

            }

            @Override
            public void onAdapterStart() {
                try {
                    List<PhysicalAssetProperty<?>> properties = propertiesKeys.stream()
                            .map(key -> new PhysicalAssetProperty<>(key, 0))
                            .collect(Collectors.toList());
                    notifyPhysicalAdapterBound(new PhysicalAssetDescription(new ArrayList<>(), properties, new ArrayList<>()));
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
    public void digitalTwinLifeCycleCallbacksTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        List<DigitalAdapterCallbacks> receivedCallbacks = new LinkedList<>();

        TestShadowingFunction shadowingFunction = new TestShadowingFunction();

        DigitalAdapter<String> da = createDigitalAdapter("test-digital-adapter", receivedCallbacks);
        da.setDigitalAdapterLifeCycleListener(createDigitalAdapterLifeCycleLister(receivedCallbacks));

        DigitalTwin dt = createDigitalTwin(shadowingFunction,
                Collections.singletonList(createPhysicalAdapter("test-pa", new ArrayList<>(Arrays.asList("temperature", "volume")))),
                Collections.singletonList(da));

        List<DigitalAdapterCallbacks> expected = new LinkedList<>(Arrays.asList(ON_DT_CREATE, ON_DT_START, ON_DT_SYNC, ON_DT_BOUND));

        digitalTwinEngine.addDigitalTwin(dt, true);

        Thread.sleep(2000);

        assertTrue(receivedCallbacks.containsAll(expected));
        shadowingFunction.simulateShadowingUnSync();

        Thread.sleep(3000);

        assertTrue(receivedCallbacks.contains(ON_DT_UN_SYNC));

        Thread.sleep(2000);

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);

        expected.addAll(Arrays.asList(ON_DT_UN_BOUND, ON_DT_STOP, ON_DT_DESTROY));

        assertTrue(receivedCallbacks.containsAll(expected));

    }

    @Test
    public void physicalAdaptersCallbacksTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        List<DigitalAdapterCallbacks> receivedCallbacks = new LinkedList<>();

        DigitalAdapter<String> da = createDigitalAdapter("test-digital-adapter", receivedCallbacks);
        da.setDigitalAdapterLifeCycleListener(createDigitalAdapterLifeCycleLister(receivedCallbacks));

        DigitalTwin dt = createDigitalTwin(new TestShadowingFunction(),
                Arrays.asList(createPhysicalAdapter("test-physical-adapter-1", new ArrayList<>(Collections.singletonList("temperature"))),
                        createPhysicalAdapter("test-physical-adapter-2", new ArrayList<>(Collections.singletonList("volume"))),
                        createPhysicalAdapter("test-physical-adapter-3", new ArrayList<>(Collections.singletonList("air-quality")))),
                Collections.singletonList(da));

        digitalTwinEngine.addDigitalTwin(dt, true);

        Thread.sleep(2000);
        assertEquals(3, receivedCallbacks.stream().filter(c -> c == ON_PA_BOUND).count());

        Thread.sleep(2000);

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);

        assertEquals(3, receivedCallbacks.stream().filter(c -> c == ON_PA_UN_BOUND).count());
    }

    @Test
    public void digitalAdaptersCallbacksTest() throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException, InterruptedException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        List<DigitalAdapterCallbacks> receivedCallbacks1 = new LinkedList<>();
        List<DigitalAdapterCallbacks> receivedCallbacks2 = new LinkedList<>();
        List<DigitalAdapterCallbacks> receivedCallbacks3 = new LinkedList<>();

        DigitalAdapter<String> da1 = createDigitalAdapter("test-digital-adapter1", receivedCallbacks1);
        da1.setDigitalAdapterLifeCycleListener(createDigitalAdapterLifeCycleLister(receivedCallbacks1));

        DigitalAdapter<String> da2 = createDigitalAdapter("test-digital-adapter2", receivedCallbacks2);
        da2.setDigitalAdapterLifeCycleListener(createDigitalAdapterLifeCycleLister(receivedCallbacks2));

        DigitalAdapter<String> da3 = createDigitalAdapter("test-digital-adapter3", receivedCallbacks3);
        da3.setDigitalAdapterLifeCycleListener(createDigitalAdapterLifeCycleLister(receivedCallbacks3));

        DigitalTwin dt = createDigitalTwin(new TestShadowingFunction(),
                Collections.singletonList(createPhysicalAdapter("test-physical-adapter", new ArrayList<>(Collections.singletonList("temperature")))),
                Arrays.asList(da1, da2, da3));

        digitalTwinEngine.addDigitalTwin(dt, true);

        Thread.sleep(2000);
        assertEquals(3, receivedCallbacks1.stream().filter(c -> c == ON_DA_BOUND).count());
        assertEquals(3, receivedCallbacks2.stream().filter(c -> c == ON_DA_BOUND).count());
        assertEquals(3, receivedCallbacks3.stream().filter(c -> c == ON_DA_BOUND).count());

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);

        assertEquals(3, receivedCallbacks1.stream().filter(c -> c == ON_DA_UN_BOUND).count());
        assertEquals(3, receivedCallbacks2.stream().filter(c -> c == ON_DA_UN_BOUND).count());
        assertEquals(3, receivedCallbacks3.stream().filter(c -> c == ON_DA_UN_BOUND).count());
    }

    enum DigitalAdapterCallbacks {
        ON_START, ON_STOP,
        ON_DT_CREATE, ON_DT_START, ON_DT_STOP, ON_DT_DESTROY,
        ON_DT_BOUND, ON_DT_UN_BOUND, ON_DT_SYNC, ON_DT_UN_SYNC,
        ON_PA_BOUND, ON_PA_UPDATE, ON_PA_UN_BOUND,
        ON_DA_BOUND, ON_DA_UN_BOUND;
    }

}

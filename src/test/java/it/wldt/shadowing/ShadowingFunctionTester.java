package it.wldt.shadowing;


import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.engine.WldtEngine;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.*;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.adapter.DummyDigitalAdapter;
import it.wldt.adapter.DummyDigitalAdapterConfiguration;
import it.wldt.adapter.DummyPhysicalAdapter;
import it.wldt.adapter.DummyPhysicalAdapterConfiguration;
import it.wldt.exception.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class ShadowingFunctionTester {

    private static final Logger logger = LoggerFactory.getLogger(ShadowingFunctionTester.class);

    private static CountDownLatch testCountDownLatch = null;

    private static List<WldtEvent<?>> receivedStateWldtEventList = null;

    private void createDigitalTwinStateObserver() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DigitalTwinStateManager.DT_STATE_PROPERTY_DELETED);

        WldtEventBus.getInstance().subscribe("demo-state-observer", wldtEventFilter, new WldtEventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                logger.info("DT-State-Observer - onEventSubscribed(): {}", eventType);
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                logger.info("DT-State-Observer - onEventUnSubscribed(): {}", eventType);
            }

            @Override
            public void onEvent(WldtEvent<?> wldtEvent) {

                if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)) {
                    logger.info("DT-State-Observer - onEvent(): Type: {} Event:{}", wldtEvent.getType(), wldtEvent);
                    receivedStateWldtEventList.add(wldtEvent);
                    testCountDownLatch.countDown();
                }
                else
                    logger.error("DT-State-Observer - ERROR Wrong Event Received: {}", wldtEvent);
            }
        });
    }

    private ShadowingModelFunction getTargetShadowingFunction(){

        return new ShadowingModelFunction("demo-shadowing-model-function") {

            private boolean isShadowed = false;

            @Override
            protected void onCreate() {
                logger.debug("Shadowing Function - onCreate()");
            }

            @Override
            protected void onStart() {
                logger.debug("Shadowing Function - onStart()");
            }

            @Override
            protected void onStop() {
                logger.debug("Shadowing Function - onStop()");
            }

            @Override
            protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
                logger.debug("DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

                try {

                    for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                        String adapterId = entry.getKey();
                        PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                        logger.info("Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                        //Analyze Physical Asset Properties for the target PhysicalAdapter
                        for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetDescription.getProperties()) {

                            String physicalPropertyKey = physicalAssetProperty.getKey();
                            String physicalPropertyType = physicalAssetProperty.getType();

                            logger.info("New Physical Property Detected ! Key: {} Type: {} InstanceType: {}", physicalPropertyKey, physicalPropertyType, physicalAssetProperty.getClass());

                            //Update Digital Twin State creating the new Property
                            if(!this.digitalTwinStateManager.containsProperty(physicalPropertyKey)) {

                                this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(
                                                physicalPropertyKey,
                                                physicalAssetProperty.getInitialValue()));

                                logger.info("New DigitalTwinStateProperty {} Created !", physicalPropertyKey);
                            }
                            else
                                logger.warn("DT Property {} already available !", physicalPropertyKey);

                            //Observe Physical Property in order to receive Physical Events related to Asset updates
                            logger.info("Observing Physical Asset Property: {}", physicalPropertyKey);
                            this.observePhysicalAssetProperty(physicalAssetProperty);
                        }

                        //Analyze Existing Physical Asset Event and observe them
                        for(PhysicalAssetEvent physicalAssetEvent : physicalAssetDescription.getEvents()){

                            String physicalEventKey = physicalAssetEvent.getKey();
                            String physicalEventType = physicalAssetEvent.getType();

                            logger.info("New Physical Event Detected ! Key: {} Type: {}", physicalEventKey, physicalEventType);

                            observePhysicalAssetEvent(physicalAssetEvent);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
                logger.debug("DigitalTwin - LifeCycleListener - onDigitalTwinUnBound()");
            }

            @Override
            protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {
                logger.debug("DigitalTwin - LifeCycleListener - onPhysicalAdapterBidingUpdate()");
            }

            @Override
            protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {

                try{

                    logger.info("ShadowingModelFunction Physical Asset Property Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null
                            && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())
                            && physicalPropertyEventMessage.getPhysicalPropertyId() != null
                            && this.digitalTwinStateManager.containsProperty(physicalPropertyEventMessage.getPhysicalPropertyId())
                            && physicalPropertyEventMessage.getBody() instanceof Double
                    ){

                        logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                        //Update DT State Property
                        this.digitalTwinStateManager.updateProperty(
                                new DigitalTwinStateProperty<Double>(
                                        physicalPropertyEventMessage.getPhysicalPropertyId(),
                                        (Double) physicalPropertyEventMessage.getBody()));

                        if(!isShadowed){
                            isShadowed = true;
                            notifyShadowingSync();
                        }

                    }
                    else
                        logger.error("WRONG Physical Event Message Received !");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent) {

            }

            @Override
            protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {

            }

            @Override
            protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

            }

            @Override
            protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

                logger.info("ShadowingModelFunction Physical Asset Event - Event Received: {}", physicalAssetEventWldtEvent);

                //TODO Handle Event MANAGEMENT ON THE DT
            }
        };
    }

    @Test
    public void testShadowingFunctionOnPhysicalEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedStateWldtEventList = new ArrayList<>();

        testCountDownLatch = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Register DigitalTwin State Observer
        createDigitalTwinStateObserver();

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Create Digital Adapter
        DummyDigitalAdapter dummyDigitalAdapter = new DummyDigitalAdapter("dummy-digital-adapter", new DummyDigitalAdapterConfiguration());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), "shadowing-tester-dt");
        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addDigitalAdapter(dummyDigitalAdapter);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        testCountDownLatch.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES *DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedStateWldtEventList);
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedStateWldtEventList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}

package it.wldt.core.shadowing;


import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.adapter.physical.TestPhysicalAdapterConfiguration;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.event.*;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.adapter.digital.TestDigitalAdapter;
import it.wldt.core.adapter.digital.TestDigitalAdapterConfiguration;
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

    public final String DIGITAL_TWIN_ID = "dt00001";

    private static final Logger logger = LoggerFactory.getLogger(ShadowingFunctionTester.class);

    private static CountDownLatch testCountDownLatch = null;

    private static List<WldtEvent<?>> receivedStateWldtEventList = null;

    private void createDigitalTwinStateObserver() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());

        WldtEventBus.getInstance().subscribe(DIGITAL_TWIN_ID, "demo-state-observer", wldtEventFilter, new WldtEventListener() {
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

                //DT State Events Management
                if(wldtEvent != null
                        && wldtEvent.getType().equals(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType())
                        && wldtEvent.getBody() != null
                        && (wldtEvent.getBody() instanceof DigitalTwinState)){

                    logger.info("DT-State-Observer - onEvent(): Type: {} Event:{}", wldtEvent.getType(), wldtEvent);
                    receivedStateWldtEventList.add(wldtEvent);
                    testCountDownLatch.countDown();
                }
                else
                    logger.error("DT-State-Observer - ERROR Wrong Event Received: {}", wldtEvent);
            }
        });
    }

    private ShadowingFunction getTargetShadowingFunction(){

        return new ShadowingFunction("demo-shadowing-model-function") {

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
                            if(!this.digitalTwinStateManager.getDigitalTwinState().containsProperty(physicalPropertyKey)) {

                                this.digitalTwinStateManager.startStateTransaction();
                                this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(
                                                physicalPropertyKey,
                                                physicalAssetProperty.getInitialValue()));
                                this.digitalTwinStateManager.commitStateTransaction();

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

                    logger.info("ShadowingFunction Physical Asset Property Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null
                            && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())
                            && physicalPropertyEventMessage.getPhysicalPropertyId() != null
                            && this.digitalTwinStateManager.getDigitalTwinState().containsProperty(physicalPropertyEventMessage.getPhysicalPropertyId())
                            && physicalPropertyEventMessage.getBody() instanceof Double
                    ){

                        logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                        //Update DT State Property
                        this.digitalTwinStateManager.startStateTransaction();
                        this.digitalTwinStateManager.updateProperty(
                                new DigitalTwinStateProperty<Double>(
                                        physicalPropertyEventMessage.getPhysicalPropertyId(),
                                        (Double) physicalPropertyEventMessage.getBody()));
                        this.digitalTwinStateManager.commitStateTransaction();

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

                logger.info("ShadowingFunction Physical Asset Event - Event Received: {}", physicalAssetEventWldtEvent);

                //TODO Handle Event MANAGEMENT ON THE DT
            }
        };
    }

    @Test
    public void testShadowingFunctionOnPhysicalEvents() throws WldtConfigurationException, EventBusException, ModelException, InterruptedException, WldtRuntimeException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        receivedStateWldtEventList = new ArrayList<>();

        testCountDownLatch = new CountDownLatch(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Register DigitalTwin State Observer
        createDigitalTwinStateObserver();

        //Create Physical Adapter
        TestPhysicalAdapter testPhysicalAdapter = new TestPhysicalAdapter("dummy-physical-adapter", new TestPhysicalAdapterConfiguration(), true);

        //Create Digital Adapter
        TestDigitalAdapter testDigitalAdapter = new TestDigitalAdapter("dummy-digital-adapter", new TestDigitalAdapterConfiguration());

        //Init the Engine
        DigitalTwin digitalTwin = new DigitalTwin(DIGITAL_TWIN_ID, getTargetShadowingFunction());
        digitalTwin.addPhysicalAdapter(testPhysicalAdapter);
        digitalTwin.addDigitalAdapter(testDigitalAdapter);

        digitalTwinEngine.addDigitalTwin(digitalTwin, true);

        //Wait until all the messages have been received
        testCountDownLatch.await((TestPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS
                        + (TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES * TestPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedStateWldtEventList);
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedStateWldtEventList.size());

        Thread.sleep(2000);

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);
    }

}

package it.wldt.lifecycle;

import it.wldt.adapter.DummyDigitalAdapter;
import it.wldt.adapter.DummyPhysicalAdapter;
import it.wldt.adapter.DummyPhysicalAdapterConfiguration;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.engine.WldtEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.adapter.DummyDigitalAdapterConfiguration;
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
public class LifeCycleTester {

    private static final Logger logger = LoggerFactory.getLogger(LifeCycleTester.class);

    private static CountDownLatch lock = null;

    private static List<PhysicalAssetPropertyWldtEvent<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalAssetPropertyWldtEvent<String>> receivedPhysicalSwitchEventMessageList = null;

    private static final String DEMO_MQTT_BODY = "DEMO_BODY_MQTT";

    private static final String DEMO_MQTT_MESSAGE_TYPE = "mqtt.telemetry";

    @Test
    public void testLifeCycle() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        this.receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        lock = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), true);

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(new ShadowingModelFunction("test-shadowing-function") {

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

                for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                    String adapterId = entry.getKey();
                    PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                    logger.info("Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                    try{
                        if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && physicalAssetDescription.getProperties().size() > 0){
                            logger.info("Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                            this.observePhysicalAssetProperties(physicalAssetDescription.getProperties());
                        }
                        else
                            logger.info("Empty property list on adapter {}. Nothing to observe !", adapterId);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
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
            protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalEventMessage) {

                logger.info("onPhysicalEvent()-> {}", physicalEventMessage);

                if(physicalEventMessage != null
                        && getPhysicalEventsFilter().contains(physicalEventMessage.getType())
                        && physicalEventMessage.getBody() instanceof Double){

                    if(!isShadowed){
                        isShadowed = true;
                        if(getShadowingModelListener() != null)
                            notifyShadowingSync();
                        else
                            logger.error("ERROR ShadowingListener = NULL !");
                    }

                    lock.countDown();
                    receivedPhysicalTelemetryEventMessageList.add((PhysicalAssetPropertyWldtEvent<Double>) physicalEventMessage);
                }
                else
                    logger.error("WRONG Physical Event Message Received !");
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

        }, "life-cycle-tester-dt");

        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addDigitalAdapter(new DummyDigitalAdapter("test-digital-adapter", new DummyDigitalAdapterConfiguration()));
        wldtEngine.addLifeCycleListener(new LifeCycleListener() {
            @Override
            public void onCreate() {
                logger.debug("LifeCycleListener - onCreate()");
            }

            @Override
            public void onStart() {
                logger.debug("LifeCycleListener - onStart()");
            }

            @Override
            public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                logger.debug("LifeCycleListener - onPhysicalAdapterBound({})", adapterId);
            }

            @Override
            public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
                logger.debug("LifeCycleListener - onPhysicalAdapterBindingUpdate({})", adapterId);
            }

            @Override
            public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
                logger.debug("LifeCycleListener - onAdapterUnBound({}) Reason: {}", adapterId, errorMessage);
            }

            @Override
            public void onDigitalAdapterBound(String adapterId) {
                logger.debug("LifeCycleListener - onDigitalAdapterBound({})", adapterId);
            }

            @Override
            public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
                logger.debug("LifeCycleListener - onDigitalAdapterUnBound({}) Error: {}", adapterId, errorMessage);
            }

            @Override
            public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
                logger.debug("LifeCycleListener - onBound()");
            }

            @Override
            public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
                logger.debug("LifeCycleListener - onUnBound() Reason: {}", errorMessage);
            }

            @Override
            public void onSync(DigitalTwinStateManager digitalTwinState) {
                logger.debug("LifeCycleListener - onSync() - DT State: {}", digitalTwinState);
            }

            @Override
            public void onUnSync(DigitalTwinStateManager digitalTwinState) {
                logger.debug("LifeCycleListener - onUnSync() - DT State: {}", digitalTwinState);
            }

            @Override
            public void onStop() {
                logger.debug("LifeCycleListener - onStop()");
            }

            @Override
            public void onDestroy() {
                logger.debug("LifeCycleListener - onDestroy()");
            }
        });

        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        lock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS + 100
                        + (DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES *DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

}

package it.wldt.core.lifecycle;

import it.wldt.core.adapter.digital.TestDigitalAdapter;
import it.wldt.core.adapter.physical.TestPhysicalAdapter;
import it.wldt.core.adapter.physical.TestPhysicalAdapterConfiguration;
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.adapter.digital.TestDigitalAdapterConfiguration;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.exception.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class LifeCycleTester {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(LifeCycleTester.class);

    public final String DIGITAL_TWIN_ID = "dt00001";

    private static CountDownLatch lock = null;

    private static List<PhysicalAssetPropertyWldtEvent<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalAssetPropertyWldtEvent<String>> receivedPhysicalSwitchEventMessageList = null;

    private static final String DEMO_MQTT_BODY = "DEMO_BODY_MQTT";

    private static final String DEMO_MQTT_MESSAGE_TYPE = "mqtt.telemetry";

    @Test
    public void testLifeCycle() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException, WldtWorkerException, WldtDigitalTwinStateException, WldtEngineException {

        DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

        this.receivedPhysicalTelemetryEventMessageList = new ArrayList<>();

        lock = new CountDownLatch(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Create Physical Adapter
        TestPhysicalAdapter testPhysicalAdapter = new TestPhysicalAdapter("dummy-physical-adapter", new TestPhysicalAdapterConfiguration(), true);

        //Init the Engine
        DigitalTwin digitalTwin = new DigitalTwin(DIGITAL_TWIN_ID, new ShadowingFunction("test-shadowing-function") {

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
                logger.info("ShadowingFunction Physical Asset Event - Event Received: {}", physicalAssetEventWldtEvent);
            }

        });

        digitalTwin.addPhysicalAdapter(testPhysicalAdapter);
        digitalTwin.addDigitalAdapter(new TestDigitalAdapter("test-digital-adapter", new TestDigitalAdapterConfiguration()));
        digitalTwin.addLifeCycleListener(new LifeCycleListener() {
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
            public void onSync(DigitalTwinState digitalTwinState) {
                logger.debug("LifeCycleListener - onSync() - DT State: {}", digitalTwinState);
            }

            @Override
            public void onUnSync(DigitalTwinState digitalTwinState) {
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

        digitalTwinEngine.addDigitalTwin(digitalTwin, true);

        //Wait until all the messages have been received
        lock.await((TestPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS + 100
                        + (TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES * TestPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)),
                TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);
        assertEquals(TestPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        Thread.sleep(2000);

        digitalTwinEngine.stopDigitalTwin(DIGITAL_TWIN_ID);
    }

}

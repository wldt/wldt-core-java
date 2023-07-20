package it.wldt.model;


import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingModelFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class ModelTester {

    private static final Logger logger = LoggerFactory.getLogger(ModelTester.class);

    private CountDownLatch lock = new CountDownLatch(1);

    private PhysicalAssetPropertyWldtEvent<String> receivedMessage = null;

    private static final String DEMO_MQTT_BODY = "DEMO_BODY_MQTT";

    private static final String DEMO_MQTT_MESSAGE_TYPE = "mqtt.telemetry";

    private ShadowingModelFunction testShadowingFunctionModel = new ShadowingModelFunction("demo-shadowing-model-function") {

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
            logger.debug("DigitalTwin - LifeCycleListener - onPhysicalAdapterBidingUpdate()");
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
    /*
    @Test
    public void testShadowingFunction() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(testShadowingFunctionModel, buildWldtConfiguration());

        wldtEngine.addPhysicalAdapter(new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), false));
        wldtEngine.startLifeCycle();

        //Wait just to complete the correct startup of the DT instance
        Thread.sleep(2000);

        //Generate an emulated Physical Event
        PhysicalPropertyEventMessage<String> physicalPropertyEventMessage = new PhysicalPropertyEventMessage<>(DEMO_MQTT_MESSAGE_TYPE);
        physicalPropertyEventMessage.setBody(DEMO_MQTT_BODY);
        physicalPropertyEventMessage.setContentType("text");

        //Publish Message on the target Topic1
        EventBus.getInstance().publishEvent("demo-physical-adapter", physicalPropertyEventMessage);

        lock.await(2000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedMessage);
        assertEquals(physicalPropertyEventMessage, receivedMessage);
        assertEquals(DEMO_MQTT_BODY, receivedMessage.getBody());
        assertEquals(PhysicalPropertyEventMessage.buildEventType(DEMO_MQTT_MESSAGE_TYPE), receivedMessage.getType());

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testInternalModelFunction() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, WldtRuntimeException {

        //Set EventBus Logger
        EventBus.getInstance().setEventLogger(new DefaultEventLogger());

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(testShadowingFunctionModel, buildWldtConfiguration());

        //Add two Model Function. The first observe the DT status and the second one creates a new property
        wldtEngine.getModelEngine().addStateModelFunction(new ObserverStateModelFunction("state-observer-model-function"), true, null);
        wldtEngine.getModelEngine().addStateModelFunction(new StateUpdateStateModelFunction("state-updated-model-function"), false, null);
    }
    */
}

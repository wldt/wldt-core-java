package it.wldt.adapter;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.instance.*;
import it.wldt.adapter.physical.event.*;
import it.wldt.core.state.*;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.core.engine.WldtEngine;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.model.ShadowingModelFunction;
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
public class AdaptersTester {

    private static final Logger logger = LoggerFactory.getLogger(AdaptersTester.class);

    private static CountDownLatch wldtEventsLock = null;

    private static CountDownLatch actionLock = null;

    private static List<PhysicalAssetPropertyWldtEvent<Double>> receivedPhysicalTelemetryEventMessageList = null;

    private static List<PhysicalAssetEventWldtEvent<?>> receivedPhysicalEventEventMessageList = null;

    private static List<PhysicalAssetPropertyWldtEvent<String>> receivedPhysicalSwitchEventMessageList = null;

    private static List<DigitalTwinStateEventNotification<?>> receivedEventNotificationList = null;

    private static List<DigitalTwinState> receivedDigitalTwinStateUpdateList = null;

    private WldtEngine buildWldtEngine(boolean physicalTelemetryOn) throws WldtConfigurationException, ModelException, WldtRuntimeException, EventBusException {

        //Create Physical Adapter
        DummyPhysicalAdapter dummyPhysicalAdapter = new DummyPhysicalAdapter("dummy-physical-adapter", new DummyPhysicalAdapterConfiguration(), physicalTelemetryOn);

        //Create Digital Adapter
        DummyDigitalAdapter dummyDigitalAdapter = new DummyDigitalAdapter("dummy-digital-adapter", new DummyDigitalAdapterConfiguration(),
                receivedDigitalTwinStateUpdateList,
                receivedEventNotificationList
        );

        //DummyShadowingFunction dummyShadowingFunction = new DummyShadowingFunction();

        //Init the Engine
        WldtEngine wldtEngine = new WldtEngine(getTargetShadowingFunction(), "adapters-tester-digital-twin");
        wldtEngine.addPhysicalAdapter(dummyPhysicalAdapter);
        wldtEngine.addDigitalAdapter(dummyDigitalAdapter);

        return wldtEngine;
    }

    @Test
    public void testPhysicalAdapterEvents() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedPhysicalTelemetryEventMessageList = new ArrayList<>();
        receivedPhysicalEventEventMessageList = new ArrayList<>();

        receivedDigitalTwinStateUpdateList = new ArrayList<>();
        receivedEventNotificationList = new ArrayList<>();

        wldtEventsLock = new CountDownLatch(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(true);
        wldtEngine.startLifeCycle();

        //Wait until all the messages have been received
        wldtEventsLock.await((DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS + ((DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES + DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES) * DummyPhysicalAdapter.MESSAGE_SLEEP_PERIOD_MS)), TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalTelemetryEventMessageList);

        //Check Received Physical Event on the Shadowing Function
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedPhysicalTelemetryEventMessageList.size());

        //Check Received Physical Asset Events Availability correctly received by the Shadowing Function
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, receivedPhysicalEventEventMessageList.size());

        //Check Correct Digital Twin State Property Update Events have been received on the Digital Adapter through DT State Updates
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES, receivedDigitalTwinStateUpdateList.size());

        //Check if Digital Twin State Events Notifications have been correctly received by the Digital Adapter after passing through the Shadowing Function
        assertEquals(DummyPhysicalAdapter.TARGET_PHYSICAL_ASSET_EVENT_UPDATES, receivedEventNotificationList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
    }

    @Test
    public void testPhysicalAdapterActions() throws WldtConfigurationException, EventBusException, ModelException, ModelFunctionException, InterruptedException, WldtRuntimeException {

        receivedPhysicalSwitchEventMessageList = new ArrayList<>();

        //Our target is to received two event changes associated to switch changes
        actionLock = new CountDownLatch(2);

        //Set EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        WldtEngine wldtEngine = buildWldtEngine(false);
        wldtEngine.startLifeCycle();

        logger.info("WLDT Started ! Sleeping (5s) before sending actions ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalAssetActionWldtEvent<String> switchOffPhysicalActionEvent = new PhysicalAssetActionWldtEvent<String>(DummyPhysicalAdapter.SWITCH_OFF_ACTION_KEY, "OFF");
        WldtEventBus.getInstance().publishEvent("demo-action-tester", switchOffPhysicalActionEvent);
        logger.info("Physical Action OFF Sent ! Sleeping (5s) ...");
        Thread.sleep(5000);

        //Send a Demo OFF PhysicalAction to the Adapter
        PhysicalAssetActionWldtEvent<String> switchOnPhysicalActionEvent = new PhysicalAssetActionWldtEvent<String>(DummyPhysicalAdapter.SWITCH_ON_ACTION_KEY, "ON");
        WldtEventBus.getInstance().publishEvent("demo-action-tester", switchOnPhysicalActionEvent);
        logger.info("Physical Action ON Sent ! Sleeping (5s) ...");

        //Wait until all the messages have been received
        actionLock.await(5000, TimeUnit.MILLISECONDS);

        assertNotNull(receivedPhysicalSwitchEventMessageList);
        assertEquals(2, receivedPhysicalSwitchEventMessageList.size());

        Thread.sleep(2000);

        wldtEngine.stopLifeCycle();
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

                try{

                    logger.debug("ShadowingModelFunction - DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

                    //Handle Shadowing & Update Digital Twin State
                    if(!isShadowed){

                        isShadowed = true;

                        this.digitalTwinStateManager.startStateTransaction();

                        for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                            String adapterId = entry.getKey();
                            PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                            //In that simple case the Digital Twin shadow all the properties and actions available in the physical asset
                            for(PhysicalAssetProperty<?> physicalAssetProperty : physicalAssetDescription.getProperties())
                                this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(physicalAssetProperty.getKey(), physicalAssetProperty.getInitialValue()));

                            for(PhysicalAssetAction physicalAssetAction : physicalAssetDescription.getActions())
                                this.digitalTwinStateManager.enableAction(new DigitalTwinStateAction(physicalAssetAction.getKey(),
                                        physicalAssetAction.getType(),
                                        physicalAssetAction.getContentType()));

                            for(PhysicalAssetEvent physicalAssetEvent: physicalAssetDescription.getEvents())
                                this.digitalTwinStateManager.registerEvent(new DigitalTwinStateEvent(physicalAssetEvent.getKey(), physicalAssetEvent.getType()));
                        }

                        this.digitalTwinStateManager.commitStateTransaction();

                        //Notify Shadowing Completed
                        notifyShadowingSync();
                    }

                    //Observer Target Physical Properties
                    for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                        String adapterId = entry.getKey();
                        PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                        logger.info("ShadowingModelFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                        try{
                            if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && physicalAssetDescription.getProperties().size() > 0){
                                logger.info("ShadowingModelFunction - Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                                this.observePhysicalAssetProperties(physicalAssetDescription.getProperties());
                            }
                            else
                                logger.info("ShadowingModelFunction - Empty property list on adapter {}. Nothing to observe !", adapterId);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    //Observe all the target available Physical Asset Events for each Adapter
                    for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                        String adapterId = entry.getKey();
                        PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                        logger.info("ShadowingModelFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                        try{
                            if(physicalAssetDescription != null && physicalAssetDescription.getEvents() != null && physicalAssetDescription.getEvents().size() > 0){
                                logger.info("ShadowingModelFunction - Observing Physical Asset Events: {}", physicalAssetDescription.getEvents());
                                this.observePhysicalAssetEvents(physicalAssetDescription.getEvents());
                            }
                            else
                                logger.info("ShadowingModelFunction - Empty event list on adapter {}. Nothing to observe !", adapterId);

                        }catch (Exception e){
                            e.printStackTrace();
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

                try {

                    logger.info("ShadowingModelFunction Physical Event Received: {}", physicalPropertyEventMessage);

                    if(physicalPropertyEventMessage != null && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())){

                        //Check if it is a switch change
                        if(PhysicalAssetPropertyWldtEvent.buildEventType(PhysicalAssetPropertyWldtEvent.PHYSICAL_EVENT_BASIC_TYPE, DummyPhysicalAdapter.SWITCH_PROPERTY_KEY).equals(physicalPropertyEventMessage.getType())
                                && physicalPropertyEventMessage.getBody() instanceof String){

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            if(actionLock != null)
                                actionLock.countDown();

                            if(receivedPhysicalSwitchEventMessageList != null)
                                receivedPhysicalSwitchEventMessageList.add((PhysicalAssetPropertyWldtEvent<String>) physicalPropertyEventMessage);
                        }
                        else{

                            logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                            //Update Digital Twin Status
                            this.digitalTwinStateManager.startStateTransaction();
                            this.digitalTwinStateManager.updateProperty(
                                    new DigitalTwinStateProperty<>(
                                            physicalPropertyEventMessage.getPhysicalPropertyId(),
                                            physicalPropertyEventMessage.getBody()));
                            this.digitalTwinStateManager.commitStateTransaction();

                            if(wldtEventsLock != null)
                                wldtEventsLock.countDown();

                            if(receivedPhysicalTelemetryEventMessageList != null)
                                receivedPhysicalTelemetryEventMessageList.add((PhysicalAssetPropertyWldtEvent<Double>) physicalPropertyEventMessage);
                        }
                    }
                    else
                        logger.error("WRONG Physical Event Message Received !");

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
                try {

                    logger.info("ShadowingModelFunction Physical Asset Event Notification - Event Received: {}", physicalAssetEventWldtEvent);
                    receivedPhysicalEventEventMessageList.add(physicalAssetEventWldtEvent);

                    //Handle the received physical event notification and map into a digital notification for digital adapters
                    this.digitalTwinStateManager.notifyDigitalTwinStateEvent(
                            new DigitalTwinStateEventNotification<String>(
                                    physicalAssetEventWldtEvent.getPhysicalEventKey(),
                                    (String)physicalAssetEventWldtEvent.getBody(), System.currentTimeMillis()));

                    if(wldtEventsLock != null)
                        wldtEventsLock.countDown();

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
        };
    }

}

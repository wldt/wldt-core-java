package it.wldt.process.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.EventBusException;
import it.wldt.process.physical.DemoPhysicalAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DemoShadowingFunction extends ShadowingModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(DemoShadowingFunction.class);

    private boolean isShadowed = false;

    public DemoShadowingFunction() {
        super("dummy-shadowing-function");
    }

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

                //Start DT State Change Transaction
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

                //Commit DT State Change Transaction to apply the changes on the DT State and notify about the change
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

            observeDigitalActionEvents();

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
                if(physicalPropertyEventMessage.getPhysicalPropertyId().equals(DemoPhysicalAdapter.SWITCH_PROPERTY_KEY)
                        && physicalPropertyEventMessage.getBody() instanceof String){

                    logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

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

            //Handle the received physical event notification and map into a digital notification for digital adapters
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(
                    new DigitalTwinStateEventNotification<String>(
                            physicalAssetEventWldtEvent.getPhysicalEventKey(),
                            (String)physicalAssetEventWldtEvent.getBody(),
                            System.currentTimeMillis()));

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
        try {
            logger.info("ShadowingModelFunction onDigitalActionEvent - Event Received: {}", digitalActionWldtEvent);
            //A basic Shadowing Function simply convert each DigitalActionWldtEvent in a PhysicalAssetActionWldtEvent
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }

}

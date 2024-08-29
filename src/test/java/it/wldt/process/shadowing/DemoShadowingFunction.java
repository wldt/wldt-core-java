package it.wldt.process.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class DemoShadowingFunction extends ShadowingFunction {

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

            logger.debug("ShadowingFunction - DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

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

                    // Add Actions to the DT State
                    for(PhysicalAssetAction physicalAssetAction : physicalAssetDescription.getActions())
                        this.digitalTwinStateManager.enableAction(new DigitalTwinStateAction(physicalAssetAction.getKey(),
                                physicalAssetAction.getType(),
                                physicalAssetAction.getContentType()));

                    // Add Events to the DT State
                    for(PhysicalAssetEvent physicalAssetEvent: physicalAssetDescription.getEvents())
                        this.digitalTwinStateManager.registerEvent(new DigitalTwinStateEvent(physicalAssetEvent.getKey(), physicalAssetEvent.getType()));

                    // Add Relationships to the DT State & Observe
                    for(PhysicalAssetRelationship<?> physicalAssetRelationship : physicalAssetDescription.getRelationships()) {

                        this.digitalTwinStateManager.createRelationship(new DigitalTwinStateRelationship<String>(
                                physicalAssetRelationship.getName(),
                                physicalAssetRelationship.getType()));

                        // The Digital Twin is interested to monitor variation of target physical relationship
                        this.observePhysicalAssetRelationship(physicalAssetRelationship);
                    }
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

                logger.info("ShadowingFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                try{
                    if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && physicalAssetDescription.getProperties().size() > 0){
                        logger.info("ShadowingFunction - Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                        this.observePhysicalAssetProperties(physicalAssetDescription.getProperties());
                    }
                    else
                        logger.info("ShadowingFunction - Empty property list on adapter {}. Nothing to observe !", adapterId);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Observe all the target available Physical Asset Events for each Adapter
            for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                String adapterId = entry.getKey();
                PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                logger.info("ShadowingFunction - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                try{
                    if(physicalAssetDescription != null && physicalAssetDescription.getEvents() != null && physicalAssetDescription.getEvents().size() > 0){
                        logger.info("ShadowingFunction - Observing Physical Asset Events: {}", physicalAssetDescription.getEvents());
                        this.observePhysicalAssetEvents(physicalAssetDescription.getEvents());
                    }
                    else
                        logger.info("ShadowingFunction - Empty event list on adapter {}. Nothing to observe !", adapterId);

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

            // Access the Storage Manager to retrieve the last value of the property
            this.

            logger.info("ShadowingFunction Physical Event Received: {}", physicalPropertyEventMessage);

            SharedTestMetrics.getInstance().addShadowingFunctionPropertyEvent(this.digitalTwinStateManager.getDigitalTwinId(), physicalPropertyEventMessage);

            if(physicalPropertyEventMessage != null && getPhysicalEventsFilter().contains(physicalPropertyEventMessage.getType())){

                //Check if it is a switch change
                if(physicalPropertyEventMessage.getPhysicalPropertyId().equals(DemoPhysicalAdapter.SWITCH_PROPERTY_KEY)
                        && physicalPropertyEventMessage.getBody() instanceof String){
                    logger.info("CORRECT PhysicalEvent Received -> Type: {} Message: {}", physicalPropertyEventMessage.getType(), physicalPropertyEventMessage);

                    //Update Digital Twin Status
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.updateProperty(
                            new DigitalTwinStateProperty<>(
                                    physicalPropertyEventMessage.getPhysicalPropertyId(),
                                    physicalPropertyEventMessage.getBody()));
                    this.digitalTwinStateManager.commitStateTransaction();
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
                logger.error("WRONG Physical Event Message Received ! Received Type: {}", physicalPropertyEventMessage.getType());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        try {

            logger.info("ShadowingFunction Physical Asset Event Notification - Event Received: {}", physicalAssetEventWldtEvent);

            SharedTestMetrics.getInstance().addShadowingFunctionEventNotification(this.digitalTwinStateManager.getDigitalTwinId(), physicalAssetEventWldtEvent);

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
        logger.info("ShadowingFunction onPhysicalAssetRelationshipEstablished - Event Received: {}", physicalAssetRelationshipWldtEvent);

        try {

            if(physicalAssetRelationshipWldtEvent != null
                    && physicalAssetRelationshipWldtEvent.getBody() != null){

                PhysicalAssetRelationshipInstance<?> paRelInstance = physicalAssetRelationshipWldtEvent.getBody();

                if(paRelInstance.getTargetId() instanceof String){

                    String relName = paRelInstance.getRelationship().getName();
                    String relKey = paRelInstance.getKey();
                    String relTargetId = (String)paRelInstance.getTargetId();

                    DigitalTwinStateRelationshipInstance<String> instance = new DigitalTwinStateRelationshipInstance<String>(relName, relTargetId, relKey);

                    //Update Digital Twin Status
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.addRelationshipInstance(instance);
                    this.digitalTwinStateManager.commitStateTransaction();
                }
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
        
    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {
        logger.info("ShadowingFunction onPhysicalAssetRelationshipDeleted - Event Received: {}", physicalAssetRelationshipWldtEvent);

        try {

            if(physicalAssetRelationshipWldtEvent != null
                    && physicalAssetRelationshipWldtEvent.getBody() != null){

                PhysicalAssetRelationshipInstance<?> paRelInstance = physicalAssetRelationshipWldtEvent.getBody();

                if(paRelInstance.getTargetId() instanceof String){

                    String relName = paRelInstance.getRelationship().getName();
                    String relKey = paRelInstance.getKey();
                    String relTargetId = (String)paRelInstance.getTargetId();

                    DigitalTwinStateRelationshipInstance<String> instance = new DigitalTwinStateRelationshipInstance<String>(relName, relTargetId, relKey);

                    //Update Digital Twin Status
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.deleteRelationshipInstance(relName, relKey);
                    this.digitalTwinStateManager.commitStateTransaction();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
        try {
            logger.info("ShadowingFunction onDigitalActionEvent - Event Received: {}", digitalActionWldtEvent);
            //A basic Shadowing Function simply convert each DigitalActionWldtEvent in a PhysicalAssetActionWldtEvent
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }

}

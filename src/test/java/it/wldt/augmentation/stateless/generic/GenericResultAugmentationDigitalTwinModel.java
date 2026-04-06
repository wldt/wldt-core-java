package it.wldt.augmentation.stateless.generic;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.stateless.function.RandomNumberAugmentationFunction;
import it.wldt.core.model.DigitalTwinModel;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;

import java.util.List;
import java.util.Map;

public class GenericResultAugmentationDigitalTwinModel extends DigitalTwinModel {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(GenericResultAugmentationDigitalTwinModel.class);

    private boolean isShadowed = false;

    public GenericResultAugmentationDigitalTwinModel() {
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

            logger.debug("DigitalTwinModel - DigitalTwin - LifeCycleListener - onDigitalTwinBound()");

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

                logger.info("DigitalTwinModel - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                try{
                    if(physicalAssetDescription != null && physicalAssetDescription.getProperties() != null && !physicalAssetDescription.getProperties().isEmpty()){
                        logger.info("DigitalTwinModel - Observing Physical Asset Properties: {}", physicalAssetDescription.getProperties());
                        this.observePhysicalAssetProperties(physicalAssetDescription.getProperties());
                    }
                    else
                        logger.info("DigitalTwinModel - Empty property list on adapter {}. Nothing to observe !", adapterId);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            //Observe all the target available Physical Asset Events for each Adapter
            for(Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()){

                String adapterId = entry.getKey();
                PhysicalAssetDescription physicalAssetDescription = entry.getValue();

                logger.info("DigitalTwinModel - Adapter ({}) Physical Asset Description: {}", adapterId, physicalAssetDescription);

                try{
                    if(physicalAssetDescription != null && physicalAssetDescription.getEvents() != null && !physicalAssetDescription.getEvents().isEmpty()){
                        logger.info("DigitalTwinModel - Observing Physical Asset Events: {}", physicalAssetDescription.getEvents());
                        this.observePhysicalAssetEvents(physicalAssetDescription.getEvents());
                    }
                    else
                        logger.info("DigitalTwinModel - Empty event list on adapter {}. Nothing to observe !", adapterId);

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

            logger.info("DigitalTwinModel Physical Event Received: {}", physicalPropertyEventMessage);

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

            // Test the Augmentation Function Execution for each received physical event notification
            this.executeAugmentationFunction(RandomNumberAugmentationFunction.FUNCTION_ID);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        try {

            logger.info("DigitalTwinModel Physical Asset Event Notification - Event Received: {}", physicalAssetEventWldtEvent);

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
        logger.info("DigitalTwinModel onPhysicalAssetRelationshipEstablished - Event Received: {}", physicalAssetRelationshipWldtEvent);

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
        logger.info("DigitalTwinModel onPhysicalAssetRelationshipDeleted - Event Received: {}", physicalAssetRelationshipWldtEvent);

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
            logger.info("DigitalTwinModel onDigitalActionEvent - Event Received: {}", digitalActionWldtEvent);
            //A basic Shadowing Function simply convert each DigitalActionWldtEvent in a PhysicalAssetActionWldtEvent
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId,
                                                     String augmentationFunctionId,
                                                     List<AugmentationFunctionResult<?>> augmentationFunctionResult) {

        // Save the augmentation function result in the shared test metrics for later verification
        SharedTestMetrics.getInstance().addAugmentationFunctionResultNotification(this.digitalTwinStateManager.getDigitalTwinId(),
                augmentationFunctionHandlerId,
                augmentationFunctionId,
                augmentationFunctionResult);

        // Iterate over the augmentation function result and log the result
        for(AugmentationFunctionResult<?> result : augmentationFunctionResult) {
            logger.info("Augmentation Function Result Received: {}", result);
        }
    }

    @Override
    protected void onAugmentationNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        logger.info("Augmentation Function Available - HandlerId: {} AugmentationFunction: {}", handlerId, augmentationFunction);

        // Register the received callback on the Shared Test Metrics for later verification
        SharedTestMetrics.getInstance().addAugmentationFunctionRegistrationCallback(this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
    }

    @Override
    protected void onAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        logger.info("Augmentation Function UnAvailable - HandlerId: {} AugmentationFunction: {}", handlerId, augmentationFunction);

        // Register the received callback on the Shared Test Metrics for later verification
        SharedTestMetrics.getInstance().addAugmentationFunctionUnRegistrationCallback(this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
    }

    @Override
    protected void onAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList) {
        logger.info("Augmentation Function List Registered - HandlerId: {} AugmentationFunctionList: {}", handlerId, augmentationFunctionList);

        // Register the received callback on the Shared Test Metrics for later verification
        for(AugmentationFunction augmentationFunction : augmentationFunctionList)
            SharedTestMetrics.getInstance().addAugmentationFunctionRegistrationCallback(this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
    }

    @Override
    protected void onAugmentationFunctionError(String handlerId, String functionId, AugmentationFunctionError augmentationFunctionError) {
        logger.info("Augmentation Function Error - HandlerId: {} FunctionId: {} Error: {}", handlerId, functionId, augmentationFunctionError);

        // Register the received callback on the Shared Test Metrics for later verification
        SharedTestMetrics.getInstance().addAugmentationFunctionErrorNotification(this.digitalTwinStateManager.getDigitalTwinId(), handlerId, functionId, augmentationFunctionError);
    }
}

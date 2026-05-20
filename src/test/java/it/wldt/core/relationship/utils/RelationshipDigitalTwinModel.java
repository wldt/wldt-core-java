package it.wldt.core.relationship.utils;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.core.model.DigitalTwinModel;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import it.wldt.exception.KernelException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RelationshipDigitalTwinModel extends DigitalTwinModel {

    private CountDownLatch relationshipLatch;

    public RelationshipDigitalTwinModel() {
        super("Relationship-shadowing-function");
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        try{

            this.digitalTwinStateManager.startStateTransaction();
            adaptersPhysicalAssetDescriptionMap.values().forEach(d -> {
                d.getRelationships().forEach(par -> {
                    try {
                        this.digitalTwinStateManager.createRelationship(new DigitalTwinStateRelationship<String>(par.getName(), "test-type"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                try {
                    this.observePhysicalAssetRelationships(d.getRelationships());
                } catch (KernelException | EventBusException e) {
                    e.printStackTrace();
                }
            });
            this.digitalTwinStateManager.commitStateTransaction();
            notifyShadowingSync();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {

    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {

    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent) {

        PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance = physicalAssetRelationshipWldtEvent.getBody();
        String relationshipName = physicalAssetRelationshipInstance.getRelationship().getName();
        DigitalTwinStateRelationshipInstance<?> instance = new DigitalTwinStateRelationshipInstance<>(relationshipName, physicalAssetRelationshipInstance.getTargetId(), physicalAssetRelationshipInstance.getKey());
        try {

            this.digitalTwinStateManager.startStateTransaction();
            this.digitalTwinStateManager.addRelationshipInstance(instance);
            this.digitalTwinStateManager.commitStateTransaction();

            if(this.relationshipLatch != null)
                this.relationshipLatch.countDown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {
        PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance = physicalAssetRelationshipWldtEvent.getBody();
        String relationshipName = physicalAssetRelationshipInstance.getRelationship().getName();
        try {

            this.digitalTwinStateManager.startStateTransaction();
            this.digitalTwinStateManager.deleteRelationshipInstance(relationshipName, physicalAssetRelationshipInstance.getKey());
            this.digitalTwinStateManager.commitStateTransaction();

            if(this.relationshipLatch != null)
                this.relationshipLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

    }

    @Override
    protected void onAugmentationFunctionError(String handlerId, String functionId, AugmentationFunctionError augmentationFunctionError) {}

    @Override
    protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId, String augmentationFunctionId, AugmentationFunctionResultList augmentationFunctionResults) {}

    @Override
    protected void onAugmentationNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {}

    @Override
    protected void onAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction) {}

    @Override
    protected void onAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList) {}

    public void setRelationshipLatch(CountDownLatch latch) {
        this.relationshipLatch = latch;
    }
}

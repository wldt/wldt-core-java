package it.wldt.relationship;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtDigitalTwinStateRelationshipException;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RelationshipShadowingFunction extends ShadowingModelFunction {
    private CountDownLatch relationshipLatch;

    public RelationshipShadowingFunction() {
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
        adaptersPhysicalAssetDescriptionMap.values().forEach(d -> {
            d.getRelationships().forEach(par -> {
                try {
                    this.digitalTwinState.createRelationship(new DigitalTwinStateRelationship<String>(par.getName(), "test-type"));
                } catch (WldtDigitalTwinStateRelationshipException e) {
                    e.printStackTrace();
                }
            });
            try {
                this.observePhysicalAssetRelationships(d.getRelationships());
            } catch (ModelException | EventBusException e) {
                e.printStackTrace();
            }
        });
        notifyShadowingSync();
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
            this.digitalTwinState.addRelationshipInstance(relationshipName, instance);
            if(this.relationshipLatch != null)
                this.relationshipLatch.countDown();
        } catch (WldtDigitalTwinStateRelationshipException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {
        PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance = physicalAssetRelationshipWldtEvent.getBody();
        String relationshipName = physicalAssetRelationshipInstance.getRelationship().getName();
        try {
            this.digitalTwinState.deleteRelationshipInstance(relationshipName, physicalAssetRelationshipInstance.getKey());
            if(this.relationshipLatch != null)
                this.relationshipLatch.countDown();
        } catch (WldtDigitalTwinStateRelationshipException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

    }

    public void setRelationshipLatch(CountDownLatch latch) {
        this.relationshipLatch = latch;
    }
}

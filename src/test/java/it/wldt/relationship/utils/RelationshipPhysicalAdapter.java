package it.wldt.relationship.utils;

import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;

public class RelationshipPhysicalAdapter extends PhysicalAdapter {

    public final static String RELATIONSHIP_CONTAINS_NAME = "contains";
    public final static String RELATIONSHIP_OPERATOR_NAME = "operator";

    public RelationshipPhysicalAdapter() {
        super("Relationship-physical-adapter");
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {

    }

    @Override
    public void onAdapterStart() {
        PhysicalAssetRelationship<String> relContains = new PhysicalAssetRelationship<>(RELATIONSHIP_CONTAINS_NAME);
        PhysicalAssetRelationship<String> relOperator = new PhysicalAssetRelationship<>(RELATIONSHIP_OPERATOR_NAME);
        PhysicalAssetDescription description = new PhysicalAssetDescription();
        description.getRelationships().add(relContains);
        description.getRelationships().add(relOperator);
        try {
            notifyPhysicalAdapterBound(description);
        } catch (PhysicalAdapterException | EventBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStop() {

    }

    public void simulateRelationshipInstanceEvent(String relationshipName, String targetId, boolean isCreationEvent){
        this.getPhysicalAssetDescription().getRelationships().stream()
                .filter(r -> r.getName().equals(relationshipName))
                .forEach(r -> {
                    try {
                        PhysicalAssetRelationship<String> relationship = (PhysicalAssetRelationship<String>) r;
                        if(isCreationEvent)
                            publishPhysicalAssetRelationshipCreatedWldtEvent(new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(relationship.createRelationshipInstance(targetId)));
                        else
                            publishPhysicalAssetRelationshipDeletedWldtEvent(new PhysicalAssetRelationshipInstanceDeletedWldtEvent<>(relationship.createRelationshipInstance(targetId)));

                    } catch (EventBusException e) {
                        e.printStackTrace();
                    }
                });
    }

}

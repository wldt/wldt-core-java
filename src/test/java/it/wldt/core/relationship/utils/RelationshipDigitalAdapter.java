package it.wldt.core.relationship.utils;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class RelationshipDigitalAdapter extends DigitalAdapter<String> {

    private DigitalTwinState digitalTwinState;
    private final List<DigitalTwinStateRelationshipInstance<?>> instancesNotification;
    private CountDownLatch relationshipNotificationLatch;

    public RelationshipDigitalAdapter(List<DigitalTwinStateRelationshipInstance<?>> instancesNotification) {
        super("Relationship-digital-adapter", "configuration");
        this.instancesNotification = instancesNotification;
    }


    @Override
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

        for(DigitalTwinStateChange digitalTwinStateChange : digitalTwinStateChangeList){
            //If the change involve a Relationship
            if(digitalTwinStateChange != null && digitalTwinStateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE)){
                if(digitalTwinStateChange.getOperation().equals(DigitalTwinStateChange.Operation.OPERATION_ADD) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateRelationshipInstance<?>){
                    System.out.println("DA receive instance notification");
                    if(this.instancesNotification != null)
                        this.instancesNotification.add((DigitalTwinStateRelationshipInstance<?>) digitalTwinStateChange.getResource());
                } else if(digitalTwinStateChange.getOperation().equals(DigitalTwinStateChange.Operation.OPERATION_REMOVE) && digitalTwinStateChange.getResource() instanceof DigitalTwinStateRelationshipInstance<?>){
                    System.out.println("DA RECEIVE DELETE EVENT");
                    if(this.instancesNotification != null)
                        this.instancesNotification.removeIf(r -> r.getKey().equals(((DigitalTwinStateRelationshipInstance<?>) digitalTwinStateChange.getResource()).getKey()));
                }
            }
        }
    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
        this.digitalTwinState = digitalTwinState;
    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }

    public DigitalTwinState getDigitalTwinState() {
        return digitalTwinState;
    }

    public CountDownLatch getRelationshipNotificationLatch() {
        return relationshipNotificationLatch;
    }

    public void setRelationshipNotificationLatch(CountDownLatch relationshipNotificationLatch) {
        this.relationshipNotificationLatch = relationshipNotificationLatch;
    }
}

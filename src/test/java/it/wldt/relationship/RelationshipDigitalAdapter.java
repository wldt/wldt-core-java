package it.wldt.relationship;

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

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }


    //TODO FIX
//    @Override
//    protected void onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
//        System.out.println("DA receive instance notification");
//        this.instancesNotification.add(digitalTwinStateRelationshipInstance);
//    }
//
//
//    @Override
//    protected void onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
//        System.out.println("DA RECEIVE DELETE EVENT");
//        this.instancesNotification.removeIf(r -> r.getKey().equals(digitalTwinStateRelationshipInstance.getKey()));
//    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

        this.digitalTwinState = digitalTwinState;
        //TODO FIX
//        if(digitalTwinState.getRelationshipList().isPresent())
//            observeDigitalTwinRelationships(digitalTwinState.getRelationshipList().get().stream()
//                    .map(DigitalTwinStateRelationship::getName)
//                    .collect(Collectors.toList()));
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

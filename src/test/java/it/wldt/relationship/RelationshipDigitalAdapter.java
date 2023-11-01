package it.wldt.relationship;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;

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
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {

    }

    @Override
    protected void onStateChangeActionEnabled(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeActionUpdated(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeActionDisabled(DigitalTwinStateAction digitalTwinStateAction) {

    }

    @Override
    protected void onStateChangeEventRegistered(DigitalTwinStateEvent digitalTwinStateEvent) {

    }

    @Override
    protected void onStateChangeEventRegistrationUpdated(DigitalTwinStateEvent digitalTwinStateEvent) {

    }

    @Override
    protected void onStateChangeEventUnregistered(DigitalTwinStateEvent digitalTwinStateEvent) {

    }

    @Override
    protected void onDigitalTwinStateEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }

    @Override
    protected void onStateChangeRelationshipCreated(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
        System.out.println("DA receive instance notification");
        this.instancesNotification.add(digitalTwinStateRelationshipInstance);
    }

    @Override
    protected void onStateChangeRelationshipDeleted(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {
        System.out.println("DA RECEIVE DELETE EVENT");
        this.instancesNotification.removeIf(r -> r.getKey().equals(digitalTwinStateRelationshipInstance.getKey()));
    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(IDigitalTwinStateManager digitalTwinState) {
        this.digitalTwinState = digitalTwinState;
        if(digitalTwinState.getRelationshipList().isPresent())
            observeDigitalTwinRelationships(digitalTwinState.getRelationshipList().get().stream()
                    .map(DigitalTwinStateRelationship::getName)
                    .collect(Collectors.toList()));
    }

    @Override
    public void onDigitalTwinUnSync(IDigitalTwinStateManager digitalTwinState) {

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

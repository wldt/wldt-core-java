package it.wldt.core.relationship.utils;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class RelationshipDigitalAdapter extends DigitalAdapter<String> {

    private volatile DigitalTwinStateDescription synchronizedDescription;
    private volatile DigitalTwinStateValues synchronizedValues;
    private final List<DigitalTwinStateRelationshipInstance<?>> instancesNotification;
    private CountDownLatch relationshipNotificationLatch;

    public RelationshipDigitalAdapter(List<DigitalTwinStateRelationshipInstance<?>> instancesNotification) {
        super("Relationship-digital-adapter", "configuration");
        this.instancesNotification = instancesNotification;
    }


    @Override
    protected void onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges) {

    }

    @Override
    protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {

        for(DigitalTwinStateChange digitalTwinStateChange : relevantChanges){
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
    public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {
        this.synchronizedDescription = digitalTwinStateDescription;
        this.synchronizedValues = digitalTwinStateValues;
    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

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

    /**
     * @return the current description of the Digital Twin State known by the adapter (kept up to date with the events)
     */
    public DigitalTwinStateDescription getCurrentDescription() {
        return getDigitalTwinStateDescription();
    }

    /**
     * @return the current values of the Digital Twin State known by the adapter (kept up to date with the events)
     */
    public DigitalTwinStateValues getCurrentValues() {
        return getDigitalTwinStateValues();
    }

    /**
     * Waits until the adapter knows the expected number of instances of a relationship
     * @return the instances of the relationship (the last known, also if the expected number has not been reached)
     */
    public List<DigitalTwinStateRelationshipInstance<?>> awaitRelationshipInstances(String relationshipName, int expectedInstances, long timeoutMs) throws InterruptedException {
        long end = System.currentTimeMillis() + timeoutMs;
        List<DigitalTwinStateRelationshipInstance<?>> instances = null;
        while (System.currentTimeMillis() < end) {
            DigitalTwinStateValues values = getCurrentValues();
            if (values != null) {
                instances = values.getRelationshipInstances().get(relationshipName);
                if (instances != null && instances.size() == expectedInstances)
                    return instances;
            }
            Thread.sleep(25);
        }
        return instances;
    }

    public CountDownLatch getRelationshipNotificationLatch() {
        return relationshipNotificationLatch;
    }

    public void setRelationshipNotificationLatch(CountDownLatch relationshipNotificationLatch) {
        this.relationshipNotificationLatch = relationshipNotificationLatch;
    }
}

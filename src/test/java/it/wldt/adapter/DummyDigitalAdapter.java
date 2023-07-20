package it.wldt.adapter;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class DummyDigitalAdapter extends DigitalAdapter<DummyDigitalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyDigitalAdapter.class);

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyCreatedMessageList = null;

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyUpdateMessageList = null;

    private List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyDeletedMessageList = null;

    private List<DigitalTwinStateEventNotification<?>> receivedDigitalTwinStateEventNotificationList = null;

    private List<IDigitalTwinState> receivedDigitalAdapterSyncDigitalTwinStateList = null;

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyCreatedMessageList,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyUpdateMessageList,
                               List<DigitalTwinStateProperty<?>> receivedDigitalTwinPropertyDeletedMessageList,
                               List<DigitalTwinStateEventNotification<?>> receivedDigitalTwinStateEventNotificationList,
                               List<IDigitalTwinState> receivedDigitalAdapterSyncDigitalTwinStateList
                               ) {

        super(id, configuration);

        this.receivedDigitalTwinPropertyCreatedMessageList = receivedDigitalTwinPropertyCreatedMessageList;
        this.receivedDigitalTwinPropertyUpdateMessageList = receivedDigitalTwinPropertyUpdateMessageList;
        this.receivedDigitalTwinPropertyDeletedMessageList = receivedDigitalTwinPropertyDeletedMessageList;
        this.receivedDigitalTwinStateEventNotificationList = receivedDigitalTwinStateEventNotificationList;
        this.receivedDigitalAdapterSyncDigitalTwinStateList = receivedDigitalAdapterSyncDigitalTwinStateList;
    }

    @Override
    public void onAdapterStart() {
        logger.info("DummyDigitalTwinAdapter -> onAdapterStart()");
    }

    @Override
    public void onAdapterStop() {
        logger.info("DummyDigitalTwinAdapter -> onAdapterStop()");
    }

    @Override
    public void onDigitalTwinSync(IDigitalTwinState digitalTwinState) {

        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinSync() - DT State: {}", digitalTwinState);

        if(this.receivedDigitalAdapterSyncDigitalTwinStateList != null)
            this.receivedDigitalAdapterSyncDigitalTwinStateList.add(digitalTwinState);

        //Observe for notification of all the available events
        try {
            if(digitalTwinState != null && digitalTwinState.getEventList().isPresent())
                this.observeDigitalTwinEventsNotifications(digitalTwinState.getEventList().get().stream().map(DigitalTwinStateEvent::getKey).collect(Collectors.toList()));
        }catch (Exception e){
            //logger.error("ERROR OBSERVING TARGET EVENT LIST ! Error: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDigitalTwinUnSync(IDigitalTwinState digitalTwinState) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinUnSync() - DT State: {}", digitalTwinState);
    }

    @Override
    public void onDigitalTwinCreate() {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinCreate()");
    }

    @Override
    public void onDigitalTwinStart() {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStart()");
    }

    @Override
    public void onDigitalTwinStop() {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStop()");
    }

    @Override
    public void onDigitalTwinDestroy() {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinDestroy()");
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyCreated() - Property CREATED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyCreatedMessageList != null)
            this.receivedDigitalTwinPropertyCreatedMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyUpdated() - Property UPDATED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyUpdateMessageList != null)
            this.receivedDigitalTwinPropertyUpdateMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.info("DummyDigitalTwinAdapter -> onStateChangePropertyDeleted() - Property DELETED: {}", digitalTwinStateProperty);

        if(this.receivedDigitalTwinPropertyDeletedMessageList != null)
            this.receivedDigitalTwinPropertyDeletedMessageList.add(digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {}

    @Override
    protected void onStateChangeActionEnabled(DigitalTwinStateAction digitalTwinStateAction) {}

    @Override
    protected void onStateChangeActionUpdated(DigitalTwinStateAction digitalTwinStateAction) {}

    @Override
    protected void onStateChangeActionDisabled(DigitalTwinStateAction digitalTwinStateAction) {}

    @Override
    protected void onStateChangeEventRegistered(DigitalTwinStateEvent digitalTwinStateEvent) {}

    @Override
    protected void onStateChangeEventRegistrationUpdated(DigitalTwinStateEvent digitalTwinStateEvent) {}

    @Override
    protected void onStateChangeEventUnregistered(DigitalTwinStateEvent digitalTwinStateEvent) {}

    @Override
    protected void onDigitalTwinStateEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - EVENT NOTIFICATION RECEIVED: {}", digitalTwinStateEventNotification);

        if(receivedDigitalTwinStateEventNotificationList != null)
            receivedDigitalTwinStateEventNotificationList.add(digitalTwinStateEventNotification);
    }

    @Override
    protected void onStateChangeRelationshipCreated(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {

    }

    @Override
    protected void onStateChangeRelationshipDeleted(DigitalTwinStateRelationship<?> digitalTwinStateRelationship) {

    }

    @Override
    protected void onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance<?> digitalTwinStateRelationshipInstance) {

    }

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyCreatedMessageList() {
        return receivedDigitalTwinPropertyCreatedMessageList;
    }

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyUpdateMessageList() {
        return receivedDigitalTwinPropertyUpdateMessageList;
    }

    public List<DigitalTwinStateProperty<?>> getReceivedDigitalTwinPropertyDeletedMessageList() {
        return receivedDigitalTwinPropertyDeletedMessageList;
    }

    public List<IDigitalTwinState> getReceivedDigitalAdapterSyncDigitalTwinStateList() {
        return receivedDigitalAdapterSyncDigitalTwinStateList;
    }
}

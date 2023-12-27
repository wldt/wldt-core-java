package it.wldt.adapter.instance;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DummyDigitalAdapter extends DigitalAdapter<DummyDigitalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DummyDigitalAdapter.class);

    private List<DigitalTwinStateEventNotification<?>> receivedEventNotificationList = null;

    private List<DigitalTwinState> receivedDigitalTwinStateUpdateList = null;

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public DummyDigitalAdapter(String id, DummyDigitalAdapterConfiguration configuration,
                               List<DigitalTwinState> receivedDigitalTwinStateUpdateList,
                               List<DigitalTwinStateEventNotification<?>> receivedEventNotificationList
                               ) {

        super(id, configuration);

        this.receivedEventNotificationList = receivedEventNotificationList;
        this.receivedDigitalTwinStateUpdateList = receivedDigitalTwinStateUpdateList;
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
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinSync() - DT State: {}", digitalTwinState);

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
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {
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
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

        logger.info("DummyDigitalTwinAdapter -> onStateUpdate() - New State: {}", newDigitalTwinState);
        logger.info("DummyDigitalTwinAdapter -> onStateUpdate() - Previous State: {}", previousDigitalTwinState);
        logger.info("DummyDigitalTwinAdapter -> onStateUpdate() - State's Changes: {}", digitalTwinStateChangeList);

        if(this.receivedDigitalTwinStateUpdateList != null)
            this.receivedDigitalTwinStateUpdateList.add(digitalTwinState);
    }


    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - EVENT NOTIFICATION RECEIVED: {}", digitalTwinStateEventNotification);

        if(receivedEventNotificationList != null)
            receivedEventNotificationList.add(digitalTwinStateEventNotification);
    }

    public List<DigitalTwinStateEventNotification<?>> getReceivedEventNotificationList() {
        return receivedEventNotificationList;
    }

    public List<DigitalTwinState> getReceivedDigitalTwinStateUpdateList() {
        return receivedDigitalTwinStateUpdateList;
    }
}

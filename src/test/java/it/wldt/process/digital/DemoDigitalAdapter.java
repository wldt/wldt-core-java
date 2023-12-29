package it.wldt.process.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.process.metrics.SharedTestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DemoDigitalAdapter extends DigitalAdapter<DemoDigitalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DemoDigitalAdapter.class);

    // Internal reference of the Digital Twin Id for statistics, tests and metrics
    private final String digitalTwinId;


    public DemoDigitalAdapter(String digitalTwinId, String id, DemoDigitalAdapterConfiguration configuration) {
        super(id, configuration);
        this.digitalTwinId = digitalTwinId;
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

            //Option 1 Observe all Physical Event Notification through direct call
            if(digitalTwinState != null && digitalTwinState.getEventList().isPresent())
                this.observeDigitalTwinEventsNotifications(digitalTwinState.getEventList().get().stream().map(DigitalTwinStateEvent::getKey).collect(Collectors.toList()));

        }catch (Exception e){
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
        SharedTestMetrics.getInstance().addDigitalAdapterStateUpdate(digitalTwinId, digitalTwinState);
    }


    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - EVENT NOTIFICATION RECEIVED: {}", digitalTwinStateEventNotification);
        SharedTestMetrics.getInstance().addDigitalAdapterEventNotification(digitalTwinId, digitalTwinStateEventNotification);
    }
}

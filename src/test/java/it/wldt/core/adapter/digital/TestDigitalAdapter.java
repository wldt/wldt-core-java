package it.wldt.core.adapter.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestDigitalAdapter extends DigitalAdapter<TestDigitalAdapterConfiguration> {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(TestDigitalAdapter.class);

    private List<DigitalTwinStateEventNotification<?>> receivedEventNotificationList = null;

    private List<DigitalTwinStateValues> receivedDigitalTwinStateUpdateList = null;

    public TestDigitalAdapter(String id, TestDigitalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public TestDigitalAdapter(String id, TestDigitalAdapterConfiguration configuration,
                              List<DigitalTwinStateValues> receivedDigitalTwinStateUpdateList,
                              List<DigitalTwinStateEventNotification<?>> receivedEventNotificationList
                               ) {

        super(id, configuration);

        this.receivedDigitalTwinStateUpdateList = receivedDigitalTwinStateUpdateList;
        this.receivedEventNotificationList = receivedEventNotificationList;
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
    public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinSync() - DT State Description: {} Values: {}", digitalTwinStateDescription, digitalTwinStateValues);

        //Observe for notification of all the available events
        try {

            //Option 1 Observe all Physical Event Notification through direct call
            if(digitalTwinStateDescription != null && !digitalTwinStateDescription.getEvents().isEmpty())
                this.observeDigitalTwinEventsNotifications(digitalTwinStateDescription.getEvents().stream().map(DigitalTwinStateDescription.EventDescription::getKey).collect(Collectors.toList()));

            //Option 2 Observe all Physical Event Notification through direct call
            //this.observeAllDigitalTwinEventsNotifications(digitalTwinStateDescription);

        }catch (Exception e){
            //logger.error("ERROR OBSERVING TARGET EVENT LIST ! Error: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinUnSync() - DT State Description: {} Values: {}", digitalTwinStateDescription, digitalTwinStateValues);
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
    protected void onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges) {

        logger.info("DummyDigitalTwinAdapter -> onStateDescriptionUpdate() - New Description: {}", description);
        logger.info("DummyDigitalTwinAdapter -> onStateDescriptionUpdate() - Description's Changes: {}", descriptionChanges);

    }

    @Override
    protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {

        logger.info("DummyDigitalTwinAdapter -> onStateValuesUpdate() - New Values: {}", values);
        logger.info("DummyDigitalTwinAdapter -> onStateValuesUpdate() - Relevant Changes: {}", relevantChanges);

        assertNotNull(values);
        assertNotNull(values.getDescriptionId());

        if(this.receivedDigitalTwinStateUpdateList != null)
            this.receivedDigitalTwinStateUpdateList.add(values);
    }


    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - EVENT NOTIFICATION RECEIVED: {}", digitalTwinStateEventNotification);

        if(receivedEventNotificationList != null){
            logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - ADDING TO LIST: {}", receivedEventNotificationList);
            receivedEventNotificationList.add(digitalTwinStateEventNotification);
            logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - ADDED TO LIST: {}", receivedEventNotificationList);
        }
    }

    public List<DigitalTwinStateEventNotification<?>> getReceivedEventNotificationList() {
        return receivedEventNotificationList;
    }

    public List<DigitalTwinStateValues> getReceivedDigitalTwinStateUpdateList() {
        return receivedDigitalTwinStateUpdateList;
    }
}

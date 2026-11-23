package it.wldt.process.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.storage.query.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DemoDigitalAdapter extends DigitalAdapter<DemoDigitalAdapterConfiguration> {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DemoDigitalAdapter.class);

    public DemoDigitalAdapter(String id, DemoDigitalAdapterConfiguration configuration) {
        super(id, configuration);
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

        }catch (Exception e){
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

    }

    @Override
    protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {
        logger.info("DummyDigitalTwinAdapter -> onStateValuesUpdate() - New Values: {}", values);
        logger.info("DummyDigitalTwinAdapter -> onStateValuesUpdate() - Changes: {}", relevantChanges);
        SharedTestMetrics.getInstance().addDigitalAdapterStateUpdate(digitalTwinId, values);
    }


    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("DummyDigitalTwinAdapter -> onDigitalTwinStateEventNotification() - EVENT NOTIFICATION RECEIVED: {}", digitalTwinStateEventNotification);
        SharedTestMetrics.getInstance().addDigitalAdapterEventNotification(digitalTwinId, digitalTwinStateEventNotification);
    }

    public <T> void invokeAction(String actionKey, T body){
        try {
            logger.info("INVOKING ACTION: {} BODY: {}", actionKey, body);
            publishDigitalActionWldtEvent(actionKey, body);
        } catch (EventBusException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to test the sync query execution
     * @param queryRequest - The query request
     * @return - The query result
     */
    public QueryResult<?> testSyncQuery(Query queryRequest){
        if(this.queryExecutor != null){
            return this.queryExecutor.syncQueryExecute(queryRequest);
        }
        else
            return null;
    }

    /**
     * This method is used to test the async query execution
     * @param queryRequest - The query request
     * @param queryResultListener - The query result listener
     */
    public void testAsyncQuery(Query queryRequest, IQueryResultListener queryResultListener){
        if(this.queryExecutor != null){
            this.queryExecutor.asyncQueryExecute(queryRequest, queryResultListener);
        }
    }
}

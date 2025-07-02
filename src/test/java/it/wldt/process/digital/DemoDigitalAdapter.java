package it.wldt.process.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.process.metrics.SharedTestMetrics;
import it.wldt.storage.query.*;
import java.util.ArrayList;
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
    public QueryResult<?> testSyncQuery(QueryRequest queryRequest){
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
    public void testAsyncQuery(QueryRequest queryRequest, IQueryResultListener queryResultListener){
        if(this.queryExecutor != null){
            this.queryExecutor.asyncQueryExecute(queryRequest, queryResultListener);
        }
    }
}

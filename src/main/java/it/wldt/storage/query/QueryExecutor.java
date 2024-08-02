package it.wldt.storage.query;

import it.wldt.core.event.*;
import it.wldt.core.event.observer.WldtEventObserver;
import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueryExecutor implements WldtEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WldtEventObserver.class);

    private static final TimeUnit QUERY_TIMEOUT_UNIT = TimeUnit.MILLISECONDS;

    private static final long QUERY_TIMEOUT_VALUE = 5000;

    // Digital Twin Id
    private String digitalTwinId = null;

    // Query Executor Id to identify the Query Executor on the Event Bus
    private String queryExecutorId = null;

    private Map<String, IQueryResultListener> queryResultListenerMap = null;

    public QueryExecutor(String digitalTwinId, String queryExecutorId) {
        // Set the Digital Twin Id and the Query Executor Id and create the Query Result Filter
        this.digitalTwinId = digitalTwinId;
        this.queryExecutorId = queryExecutorId;
        this.queryResultListenerMap = new HashMap<>();
    }

    /**
     * Observe Query Result Events
     * @param targetFilter Filter to be used to observe the events
     * @param wldtEventListener Observer Listener to be used to receive the events
     * @param eventTypeList List of Event Types to be observed
     */
    private void observeEventsWithFilter(WldtEventListener wldtEventListener, WldtEventFilter targetFilter, String... eventTypeList) {
        try{
            if(eventTypeList == null || eventTypeList.length == 0)
                logger.error("Error Observing Events ! Error: Empty/Null Filter or List ...");
            else {

                // If the filter has been already used cancel observation and clean the filter
                if(targetFilter != null)
                    unObserveEventsWithFilter(wldtEventListener, targetFilter);

                // Add target Event Types to the Filter
                if(targetFilter.addAll(Arrays.asList(eventTypeList)))
                    WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.queryExecutorId, targetFilter, wldtEventListener);
                else
                    logger.error("Error Observing Events ! Impossible to add event to the Filter ...");
            }

        }catch (Exception e){
            logger.error("Error Observing Events ! Error: {}, EventTypeList: {}", e.getLocalizedMessage(), eventTypeList);
        }
    }

    /**
     * Cancel Observation for the Events
     * @param wldtEventListener Observer Listener to be used to receive the events
     * @param targetFilter Filter to be used to observe the events
     * @throws EventBusException Exception thrown in case of error during the event bus operation
     */
    private void unObserveEventsWithFilter(WldtEventListener wldtEventListener, WldtEventFilter targetFilter) throws EventBusException {
        try{
            if (targetFilter != null && !targetFilter.isEmpty()) {
                WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.queryExecutorId, targetFilter, wldtEventListener);
                targetFilter.clear();
                targetFilter = new WldtEventFilter();
            }
            //else
            //    logger.warn("Warning Canceling Observation Observing Events ! Empty/Null Filter or List ...");
        }catch (Exception e){
            logger.error("Error Canceling Observation for Events ! Error: {}, Filter: {}", e.getLocalizedMessage(), targetFilter);
        }
    }

    /**
     * Synchronous Query Execution
     * @param queryRequest Query Request Object
     * @return Query Result Object containing the query result
     */
    public QueryResult<?> syncQueryExecute(QueryRequest queryRequest) {

        try{

            CountDownLatch latch = new CountDownLatch(1);
            final Object[] responseHolder = new Object[1];

            // Create Query Result Event Filter
            WldtEventFilter localQueryResultFilter = new WldtEventFilter();

            WldtEventListener queryResultListener = new WldtEventListener() {
                @Override
                public void onEventSubscribed(String eventType) {
                    logger.info("Correctly Subscribed to the Event Type: {}", eventType);
                }

                @Override
                public void onEventUnSubscribed(String eventType) {
                    logger.info("Correctly UnSubscribed from the Event Type: {}", eventType);
                }

                @Override
                public void onEvent(WldtEvent<?> wldtEvent) {

                    // Check if the Event Body is a Query Result
                    if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof QueryResult<?>) {
                        QueryResult<?> queryResult = (QueryResult<?>) wldtEvent.getBody();
                        responseHolder[0] = queryResult;
                        latch.countDown();
                    }
                    else {
                        logger.error("Error handling the QueryRequest Event ! The event body is not a QueryRequest !");
                        responseHolder[0] = new QueryResult<>(queryRequest, false, "Error handling the QueryRequest Event ! The event body is not a QueryRequest !");
                        latch.countDown();
                    }
                }
            };

            // Target Event Type for the Query Result
            String targetEventType = String.format("%s.%s",
                    WldtEventTypes.STORAGE_QUERY_RESULT_EVENT_TYPE,
                    queryRequest.getRequestId());

            // Observe the Query Result Events
            observeEventsWithFilter(queryResultListener, localQueryResultFilter, targetEventType);

            // Send the Query Request
            EventManager.publishStorageQueryRequest(this.digitalTwinId, this.queryExecutorId, queryRequest);

            // Wait for the response or timeout
            if (!latch.await(QUERY_TIMEOUT_VALUE, QUERY_TIMEOUT_UNIT)) {
                throw new TimeoutException("Timeout waiting for response to event: " + targetEventType);
            }

            // UnObserve the Query Result Events
            unObserveEventsWithFilter(queryResultListener, localQueryResultFilter);

            // Return the Query Result
            return (QueryResult<?>) responseHolder[0];

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    public void asyncQueryExecute(QueryRequest queryRequest, IQueryResultListener queryResultListener) {
        try{

            // Target Event Type for the Query Result
            String targetEventType = String.format("%s.%s",
                    WldtEventTypes.STORAGE_QUERY_RESULT_EVENT_TYPE,
                    queryRequest.getRequestId());

            // Subscribe to the Query Result Event
            WldtEventFilter wldtEventFilter = new WldtEventFilter();
            wldtEventFilter.add(targetEventType);
            WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.queryExecutorId, wldtEventFilter, this);

            // Add the Query Result Listener to the Map
            this.queryResultListenerMap.put(queryRequest.getRequestId(), queryResultListener);

            // Send the Query Request
            EventManager.publishStorageQueryRequest(this.digitalTwinId, this.queryExecutorId, queryRequest);

        }catch (Exception e){
            logger.error("Error handling the QueryRequest Event ! Error: {}", e.getMessage());
        }
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("QueryExecutor - Async Execution - Correctly Subscribed to the Event Type: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("QueryExecutor - Async Execution - Correctly UnSubscribed to the Event Type: {}", eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        try{
            // Check if the Event Body is a Query Result
            if(wldtEvent != null && wldtEvent.getBody() != null && wldtEvent.getBody() instanceof QueryResult<?>) {
                QueryResult<?> queryResult = (QueryResult<?>) wldtEvent.getBody();

                // Get the Query Result Listener
                IQueryResultListener queryResultListener = this.queryResultListenerMap.get(queryResult.getOriginalRequest().getRequestId());

                // Notify the Query Result Listener
                if(queryResultListener != null)
                    queryResultListener.onQueryResult(queryResult);
                else
                    logger.error("QueryExecutor - Async Execution - Error handling the QueryRequest Event ! The QueryResult Listener is null !");

                // UnSubscribe from the Query Result Event
                WldtEventFilter queryResultFilter = new WldtEventFilter();
                queryResultFilter.add(wldtEvent.getType());
                WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.queryExecutorId, new WldtEventFilter(), this);
            }
            else {
                logger.error("QueryExecutor - Async Execution - Error handling the QueryRequest Event ! The event body is not a QueryRequest !");
            }
        }catch (Exception e){
            logger.error("QueryExecutor - Async Execution - Error handling the QueryRequest Event ! Error: {}", e.getMessage());
        }
    }
}

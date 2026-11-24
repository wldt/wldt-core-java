package it.wldt.storage.query;

import it.wldt.core.event.EventManager;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.storage.model.StorageRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the {@link QueryExecutor} transport: failures are reported as results and pending queries are always cleaned.
 */
public class QueryExecutorTest {

    @SuppressWarnings("unchecked")
    private static Map<String, ?> pendingQueries(QueryExecutor executor) throws Exception {
        Field field = QueryExecutor.class.getDeclaredField("pendingQueries");
        field.setAccessible(true);
        return (Map<String, ?>) field.get(executor);
    }

    @Test
    public void noStorageManagerMeansTimeoutResultAndNoLeak() throws Exception {

        QueryExecutor executor = new QueryExecutor("query-executor-test-dt", "query-executor-test");

        // Nobody answers on this DT: the query must fail with a result (not hang, not throw) and leave nothing pending
        QueryResult<?> result = executor.syncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build());

        assertNotNull(result);
        assertFalse(result.isSuccessful());
        assertTrue(result.getErrorMessage().toLowerCase().contains("timeout"));
        assertTrue(pendingQueries(executor).isEmpty());
    }

    @Test
    public void constructorTimeoutIsUsedWhenTheQueryHasNone() throws Exception {
        QueryExecutor executor = new QueryExecutor("query-executor-timeout-dt", "query-executor-timeout", 200);

        long start = System.currentTimeMillis();
        QueryResult<?> result = executor.syncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build());

        assertFalse(result.isSuccessful());
        assertTrue(System.currentTimeMillis() - start < 3000, "must time out after ~200 ms, not the 5 s default");
        assertTrue(pendingQueries(executor).isEmpty());
    }

    @Test
    public void queryTimeoutOverridesExecutorDefault() throws Exception {
        QueryExecutor executor = new QueryExecutor("query-executor-override-dt", "query-executor-override");

        long start = System.currentTimeMillis();
        QueryResult<?> result = executor.syncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(200).build());

        assertFalse(result.isSuccessful());
        assertTrue(System.currentTimeMillis() - start < 3000, "must time out after ~200 ms, not the 5 s default");
        assertEquals(200, result.getOriginalQuery().getTimeoutMs());
    }

    @Test
    public void effectiveTimeoutIsStampedOnThePublishedQuery() {
        Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build();
        assertEquals(0, query.getTimeoutMs());
        assertEquals(7000, query.refresh(7000).getTimeoutMs());
        // the query's own timeout wins over the default
        assertEquals(300, Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(300).build().refresh(7000).getTimeoutMs());
        assertEquals(300, Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(300).build().refresh().getTimeoutMs());
    }

    @Test
    public void invalidTimeoutsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new QueryExecutor("dt", "id", 0));
        assertThrows(IllegalArgumentException.class, () -> Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(0));
    }

    @Test
    public void failedResultCarriesQueryAndNoRecords() {
        Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build();
        QueryResult<StorageRecord> result = QueryResult.failure(query, "boom");
        assertFalse(result.isSuccessful());
        assertEquals("boom", result.getErrorMessage());
        assertSame(query, result.getOriginalQuery());
        assertTrue(result.getItems().isEmpty());
        assertNull(result.first());
        assertNull(result.last());
    }

    /**
     * Subscribes a fake storage that answers every query request of the Digital Twin with an empty successful result
     */
    private static void answerQueries(String digitalTwinId) throws Exception {
        WldtEventFilter filter = new WldtEventFilter();
        filter.add(WldtEventTypes.STORAGE_QUERY_REQUEST_EVENT_TYPE);
        WldtEventBus.getInstance().subscribe(digitalTwinId, "fake-storage", filter, new WldtEventListener() {
            @Override public void onEventSubscribed(String eventType) {}
            @Override public void onEventUnSubscribed(String eventType) {}
            @Override public void onEvent(WldtEvent<?> event) {
                Query query = (Query) event.getBody();
                EventManager.publishStorageQueryResult(digitalTwinId, "fake-storage", QueryResult.success(query, new ArrayList<StorageRecord>()));
            }
        });
    }

    @Test
    public void listenerExceptionIsContainedAndNextQueryStillWorks() throws Exception {
        String dt = "query-executor-listener-exception-dt";
        answerQueries(dt);
        QueryExecutor executor = new QueryExecutor(dt, "query-executor-listener-exception");

        CountDownLatch first = new CountDownLatch(1);
        executor.asyncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build(), result -> {
            first.countDown();
            throw new IllegalStateException("listener failure");
        });
        assertTrue(first.await(3, TimeUnit.SECONDS));

        CountDownLatch second = new CountDownLatch(1);
        AtomicReference<QueryResult<?>> received = new AtomicReference<>();
        executor.asyncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build(), result -> {
            received.set(result);
            second.countDown();
        });
        assertTrue(second.await(3, TimeUnit.SECONDS));
        assertTrue(received.get().isSuccessful());
        executor.close();
    }

    @Test
    public void syncQueryInsideAsyncListenerCompletesWithoutTimeout() throws Exception {
        String dt = "query-executor-sync-in-async-dt";
        answerQueries(dt);
        QueryExecutor executor = new QueryExecutor(dt, "query-executor-sync-in-async");

        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<QueryResult<?>> nested = new AtomicReference<>();
        long start = System.currentTimeMillis();

        executor.asyncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build(), result -> {
            // The listener runs on a library pool thread, not on the thread that receives the results
            nested.set(executor.syncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build()));
            done.countDown();
        });

        assertTrue(done.await(4, TimeUnit.SECONDS));
        assertTrue(nested.get().isSuccessful(), "the nested sync query must not time out");
        assertTrue(System.currentTimeMillis() - start < 3000);
        executor.close();
    }

    @Test
    public void closeFailsPendingQueriesAndTheExecutorCanBeReused() throws Exception {
        QueryExecutor executor = new QueryExecutor("query-executor-close-dt", "query-executor-close");

        CountDownLatch failed = new CountDownLatch(1);
        AtomicReference<QueryResult<?>> received = new AtomicReference<>();
        // Nobody answers: the query stays pending until the executor is closed
        executor.asyncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(20000).build(), result -> {
            received.set(result);
            failed.countDown();
        });

        long deadline = System.currentTimeMillis() + 2000;
        while (pendingQueries(executor).isEmpty() && System.currentTimeMillis() < deadline)
            Thread.sleep(10);
        assertFalse(pendingQueries(executor).isEmpty());

        executor.close();

        assertTrue(failed.await(3, TimeUnit.SECONDS));
        assertFalse(received.get().isSuccessful());
        assertTrue(received.get().getErrorMessage().toLowerCase().contains("closed"));
        assertTrue(pendingQueries(executor).isEmpty());

        // Reusable: the next query subscribes again (and here times out, nobody answers)
        QueryResult<?> again = executor.syncQueryExecute(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(200).build());
        assertFalse(again.isSuccessful());
        assertTrue(again.getErrorMessage().toLowerCase().contains("timeout"));
        executor.close();
    }
}

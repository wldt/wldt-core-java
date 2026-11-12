package it.wldt.storage.query;

import it.wldt.storage.model.StorageRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

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
}

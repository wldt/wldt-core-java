package it.wldt.storage;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.storage.query.DefaultQueryManager;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The StorageManager skips queries whose caller already gave up (timeout expired).
 */
public class StorageManagerStaleQueryTest {

    private static class CountingQueryManager extends DefaultQueryManager {
        final AtomicInteger handled = new AtomicInteger();

        @Override
        public QueryResult<?> handleQuery(Query query, Map<String, WldtStorage> storageMap) {
            handled.incrementAndGet();
            return QueryResult.failure(query, "test");
        }
    }

    private StorageManager storageManager(CountingQueryManager queryManager) throws Exception {
        StorageManager storageManager = new StorageManager("stale-query-test-dt");
        storageManager.setQueryManager(queryManager);
        return storageManager;
    }

    private static WldtEvent<Query> event(Query query) throws Exception {
        return new WldtEvent<>(WldtEventTypes.STORAGE_QUERY_REQUEST_EVENT_TYPE, query);
    }

    @Test
    public void expiredQueryIsSkippedAndFreshQueryIsHandled() throws Exception {
        CountingQueryManager queryManager = new CountingQueryManager();
        StorageManager storageManager = storageManager(queryManager);

        Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(50).build();
        Thread.sleep(120);
        storageManager.onQueryRequestEvent(event(query));
        assertEquals(0, queryManager.handled.get());

        storageManager.onQueryRequestEvent(event(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).timeout(60_000).build()));
        assertEquals(1, queryManager.handled.get());
    }

    @Test
    public void queryWithoutTimeoutIsAlwaysHandled() throws Exception {
        CountingQueryManager queryManager = new CountingQueryManager();
        StorageManager storageManager = storageManager(queryManager);

        Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build();
        Thread.sleep(50);
        storageManager.onQueryRequestEvent(event(query));

        assertEquals(1, queryManager.handled.get());
    }
}

package it.wldt.storage;

import it.wldt.exception.StorageException;
import it.wldt.storage.model.AggregationRecord;
import it.wldt.storage.model.StorageRecord;
import it.wldt.storage.query.aggregation.AggregationFactory;
import it.wldt.storage.query.filter.FilterFactory;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryFieldNames;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;
import it.wldt.storage.query.SortCriterion;

import java.util.List;

/**
 * Test helpers to read a storage through the single {@link WldtStorage#execute(Query)} entry point.
 */
public final class StorageTestUtils {

    private StorageTestUtils() {}

    @SuppressWarnings("unchecked")
    private static <T extends StorageRecord> List<T> items(WldtStorage storage, Query query) {
        try {
            QueryResult<?> result = storage.execute(query);
            if (!result.isSuccessful())
                throw new IllegalStateException(result.getErrorMessage());
            return (List<T>) result.getItems();
        } catch (StorageException e) {
            throw new IllegalStateException(e);
        }
    }

    public static int count(WldtStorage storage, QueryResourceType resource) {
        List<AggregationRecord> rows = items(storage, Query.on(resource).aggregate(AggregationFactory.count().as("count")).build());
        return (int) rows.get(0).getLong("count");
    }

    /** Records with storage timestamp in [start, end] (both inclusive), oldest first. */
    public static <T extends StorageRecord> List<T> inTimeRange(WldtStorage storage, QueryResourceType resource, long start, long end) {
        if (start > end)
            throw new IllegalArgumentException("Start timestamp cannot be greater than end timestamp.");
        return items(storage, Query.on(resource)
                .where(FilterFactory.and(FilterFactory.gte(QueryFieldNames.TIMESTAMP, start), FilterFactory.lte(QueryFieldNames.TIMESTAMP, end)))
                .build());
    }

    /** Records at positions [startIndex, endIndex] (both inclusive) in timestamp order. */
    public static <T extends StorageRecord> List<T> inRange(WldtStorage storage, QueryResourceType resource, int startIndex, int endIndex) {
        if (startIndex < 0 || endIndex < 0 || startIndex > endIndex)
            throw new IllegalArgumentException("Invalid index range.");
        return items(storage, Query.on(resource).offset(startIndex).limit(endIndex - startIndex + 1).build());
    }

    /** The most recent record, null if the resource is empty. */
    public static <T extends StorageRecord> T last(WldtStorage storage, QueryResourceType resource) {
        List<T> result = items(storage, Query.on(resource).orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC).limit(1).build());
        return result.isEmpty() ? null : result.get(0);
    }
}

package it.wldt.storage.query;

import it.wldt.storage.query.inmemory.InMemoryQueryEvaluator;
import it.wldt.storage.model.AggregationRecord;
import it.wldt.storage.model.StorageRecord;
import it.wldt.storage.model.physical.PhysicalAssetPropertyVariationRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static it.wldt.storage.query.aggregation.AggregationFactory.*;
import static it.wldt.storage.query.filter.FilterFactory.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the Query model evaluated by the {@link InMemoryQueryEvaluator}.
 */
public class InMemoryQueryEvaluatorTest {

    private final List<Map.Entry<Long, PhysicalAssetPropertyVariationRecord>> source = new ArrayList<>();

    private static Map<String, Object> unit(String unit) {
        return Collections.<String, Object>singletonMap("unit", unit);
    }

    private void add(long timestamp, String key, Object body, String unit) {
        source.add(new AbstractMap.SimpleImmutableEntry<>(timestamp, new PhysicalAssetPropertyVariationRecord(timestamp, key, body, unit == null ? null : unit(unit))));
    }

    @BeforeEach
    public void setUp() {
        add(100, "temperature", 20.0, "C");
        add(200, "temperature", 22.0, "C");
        add(300, "humidity", 50, "%");
        add(400, "temperature", 30.0, "C");
        add(500, "humidity", 70, "%");
        add(600, "pressure", 1000, null);
    }

    private QueryResult<StorageRecord> run(Query query) {
        QueryResult<StorageRecord> result = InMemoryQueryEvaluator.evaluate(query, source);
        assertTrue(result.isSuccessful());
        return result;
    }

    private static Query.Builder on() {
        return Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION);
    }

    @Test
    public void defaultOrderIsTimestampAscending() {
        List<PhysicalAssetPropertyVariationRecord> items = run(on().build()).as(PhysicalAssetPropertyVariationRecord.class).getItems();
        assertEquals(6, items.size());
        assertEquals(100, items.get(0).getTimestamp());
        assertEquals(600, items.get(5).getTimestamp());
    }

    @Test
    public void lastValueIsDescendingWithLimitOne() {
        PhysicalAssetPropertyVariationRecord last = run(on()
                .where(eq(QueryFieldNames.PROPERTY_KEY, "temperature"))
                .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC).limit(1).build())
                .as(PhysicalAssetPropertyVariationRecord.class).first();
        assertEquals(400, last.getTimestamp());
        assertEquals(30.0, last.getBody());
    }

    @Test
    public void keyAndTimeRange() {
        List<PhysicalAssetPropertyVariationRecord> items = run(on()
                .where(and(eq(QueryFieldNames.PROPERTY_KEY, "temperature"), gte(QueryFieldNames.TIMESTAMP, 150L), lt(QueryFieldNames.TIMESTAMP, 400L)))
                .build()).as(PhysicalAssetPropertyVariationRecord.class).getItems();
        assertEquals(1, items.size());
        assertEquals(200, items.get(0).getTimestamp());
    }

    @Test
    public void inValueAndMetadataPath() {
        List<PhysicalAssetPropertyVariationRecord> items = run(on()
                .where(and(in(QueryFieldNames.PROPERTY_KEY, "temperature", "humidity"),
                        gt(QueryFieldNames.BODY, 25),
                        eq("variationMetadata.unit", "C")))
                .build()).as(PhysicalAssetPropertyVariationRecord.class).getItems();
        assertEquals(1, items.size());
        assertEquals(400, items.get(0).getTimestamp());
    }

    @Test
    public void orNotAndExists() {
        assertEquals(3, run(on().where(or(eq(QueryFieldNames.PROPERTY_KEY, "humidity"), eq(QueryFieldNames.PROPERTY_KEY, "pressure"))).build()).getItems().size());
        assertEquals(3, run(on().where(not(eq(QueryFieldNames.PROPERTY_KEY, "temperature"))).build()).getItems().size());
        // pressure has no metadata at all
        assertEquals(5, run(on().where(exists("variationMetadata.unit")).build()).getItems().size());
        assertEquals(1, run(on().where(not(exists("variationMetadata.unit"))).build()).getItems().size());
    }

    @Test
    public void offsetLimitAndTotal() {
        QueryResult<StorageRecord> result = run(on().offset(2).limit(2).withTotal().build());
        assertEquals(2, result.getItems().size());
        assertEquals(6L, result.getTotal().longValue());
        assertEquals(300, result.as(PhysicalAssetPropertyVariationRecord.class).first().getTimestamp());

        assertNull(run(on().limit(2).build()).getTotal());
        assertTrue(run(on().offset(100).build()).getItems().isEmpty());
    }

    @Test
    public void countWithoutGroupBy() {
        AggregationRecord count = run(on().where(eq(QueryFieldNames.PROPERTY_KEY, "temperature")).aggregate(count().as("n")).build())
                .as(AggregationRecord.class).first();
        assertEquals(3, count.getLong("n"));
    }

    @Test
    public void countOnEmptyInputStillReturnsOneRecord() {
        List<AggregationRecord> rows = run(on().where(eq(QueryFieldNames.PROPERTY_KEY, "missing")).aggregate(count().as("n")).build())
                .as(AggregationRecord.class).getItems();
        assertEquals(1, rows.size());
        assertEquals(0, rows.get(0).getLong("n"));
    }

    @Test
    public void groupByWithMetricsAndSort() {
        List<AggregationRecord> rows = run(on()
                .aggregate(groupBy(QueryFieldNames.PROPERTY_KEY), count().as("n"), avg(QueryFieldNames.BODY).as("avg"),
                        min(QueryFieldNames.BODY).as("min"), max(QueryFieldNames.BODY).as("max"), sum(QueryFieldNames.BODY).as("sum"))
                .orderBy("n", SortCriterion.Direction.DESC).build())
                .as(AggregationRecord.class).getItems();

        assertEquals(3, rows.size());

        AggregationRecord temperature = rows.get(0);
        assertEquals("temperature", temperature.getString(QueryFieldNames.PROPERTY_KEY));
        assertEquals(3, temperature.getLong("n"));
        assertEquals(24.0, temperature.getDouble("avg"), 0.0001);
        assertEquals(20.0, temperature.getDouble("min"), 0.0001);
        assertEquals(30.0, temperature.getDouble("max"), 0.0001);
        assertEquals(72.0, temperature.getDouble("sum"), 0.0001);

        AggregationRecord humidity = rows.get(1);
        assertEquals("humidity", humidity.getString(QueryFieldNames.PROPERTY_KEY));
        assertEquals(60.0, humidity.getDouble("avg"), 0.0001);
    }

    @Test
    public void totalCountsGroupsNotRecords() {
        QueryResult<StorageRecord> result = run(on().aggregate(groupBy(QueryFieldNames.PROPERTY_KEY), count().as("n")).limit(1).withTotal().build());
        assertEquals(1, result.getItems().size());
        assertEquals(3L, result.getTotal().longValue());
    }

    @Test
    public void enumValuesMatchByName() {
        List<Map.Entry<Long, PhysicalAssetPropertyVariationRecord>> local = new ArrayList<>();
        local.add(new AbstractMap.SimpleImmutableEntry<>(1L, new PhysicalAssetPropertyVariationRecord(1, "k", Thread.State.NEW, null)));
        QueryResult<StorageRecord> result = InMemoryQueryEvaluator.evaluate(on().where(eq(QueryFieldNames.BODY, "NEW")).build(), local);
        assertEquals(1, result.getItems().size());
    }

    @Test
    public void recordsWithSameTimestampAreAllKept() {
        add(100, "temperature", 21.0, "C");
        assertEquals(7, run(on().build()).getItems().size());
        assertEquals(2, run(on().where(eq(QueryFieldNames.TIMESTAMP, 100L)).build()).getItems().size());
    }

    @Test
    public void sameTimestampTiesFollowInsertionOrder() {
        add(100, "temperature", 21.0, "C");
        // the most recently inserted wins on a descending query, the oldest on an ascending one
        assertEquals(21.0, run(on().orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.ASC).where(eq(QueryFieldNames.TIMESTAMP, 100L)).build())
                .as(PhysicalAssetPropertyVariationRecord.class).last().getBody());
        assertEquals(21.0, run(on().orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC).limit(1)
                .where(eq(QueryFieldNames.TIMESTAMP, 100L)).build()).as(PhysicalAssetPropertyVariationRecord.class).first().getBody());
    }

    @Test
    public void limitedQueriesReturnTheSameAsTheFullSort() {
        // many records with duplicated timestamps and values, to exercise ties
        java.util.Random random = new java.util.Random(42);
        source.clear();
        for (int i = 0; i < 300; i++)
            add(random.nextInt(40), i % 3 == 0 ? "a" : "b", random.nextInt(10), "C");

        SortCriterion.Direction[] directions = {SortCriterion.Direction.ASC, SortCriterion.Direction.DESC};
        for (SortCriterion.Direction direction : directions) {
            for (String field : new String[]{QueryFieldNames.TIMESTAMP, QueryFieldNames.BODY}) {
                Query full = on().where(eq(QueryFieldNames.PROPERTY_KEY, "a")).orderBy(field, direction)
                        .orderBy(QueryFieldNames.PROPERTY_KEY, SortCriterion.Direction.ASC).build();
                List<StorageRecord> expected = run(full).getItems();

                for (int[] page : new int[][]{{0, 1}, {0, 7}, {5, 10}, {95, 20}, {0, 1000}, {200, 5}, {3, 0}}) {
                    Query limited = on().where(eq(QueryFieldNames.PROPERTY_KEY, "a")).orderBy(field, direction)
                            .orderBy(QueryFieldNames.PROPERTY_KEY, SortCriterion.Direction.ASC)
                            .offset(page[0]).limit(page[1]).withTotal().build();
                    QueryResult<StorageRecord> result = run(limited);

                    int from = Math.min(page[0], expected.size());
                    int to = Math.min(from + page[1], expected.size());
                    assertEquals(expected.subList(from, to), result.getItems(), field + " " + direction + " page " + java.util.Arrays.toString(page));
                    assertEquals(expected.size(), result.getTotal().longValue());
                }
            }
        }
    }

    @Test
    public void asChecksRecordType() {
        QueryResult<StorageRecord> result = run(on().build());
        assertThrows(ClassCastException.class, () -> result.as(AggregationRecord.class));
    }

    @Test
    public void builderRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> on().limit(-1));
        assertThrows(IllegalArgumentException.class, () -> on().offset(-1));
        assertThrows(IllegalArgumentException.class, () -> and());
        assertThrows(IllegalArgumentException.class, () -> eq("", 1));
        assertThrows(IllegalArgumentException.class, () -> on().aggregate(groupBy(QueryFieldNames.PROPERTY_KEY)));
        assertThrows(IllegalArgumentException.class, () -> sum(null));
    }

    @Test
    public void refreshKeepsContentAndChangesRequestId() {
        Query query = on().where(eq(QueryFieldNames.PROPERTY_KEY, "temperature")).limit(5).build();
        Query refreshed = query.refresh();
        assertNotEquals(query.getRequestId(), refreshed.getRequestId());
        assertEquals(query.getResource(), refreshed.getResource());
        assertSame(query.getFilter(), refreshed.getFilter());
        assertEquals(query.getLimit(), refreshed.getLimit());
    }
}

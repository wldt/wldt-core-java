package it.wldt.storage;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateTestFactory;
import it.wldt.storage.model.AggregationRecord;
import it.wldt.storage.model.state.DigitalTwinStateDescriptionRecord;
import it.wldt.storage.model.state.DigitalTwinStateHistoryEntry;
import it.wldt.storage.model.state.DigitalTwinStateHistoryRecord;
import it.wldt.storage.model.state.DigitalTwinStateValuesRecord;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryFieldNames;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;
import it.wldt.storage.query.SortCriterion;
import it.wldt.storage.query.aggregation.AggregationFactory;
import it.wldt.storage.query.filter.FilterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static it.wldt.core.state.StateTestEvents.state;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the queries on the Digital Twin State stored as description and values: the single resources
 * (description, values) and the complete state (description + the values associated to it) with filters on the
 * fields of the description and of the values, sorting, limits and aggregations.
 */
public class StateQueryTest {

    private static final String ID_0 = "7000_0";
    private static final String ID_1 = "7000_1";

    private DefaultWldtStorage storage;

    /**
     * Description 7000_0 with the values temp=10, 20, 30; description 7000_1 (with the event alarm) with the values temp=40, 50
     */
    @BeforeEach
    public void setUp() throws Exception {
        storage = new DefaultWldtStorage("state-query-storage", true);

        DigitalTwinState d0 = state(ID_0, 10, false);
        storage.saveDigitalTwinStateDescription(d0.getDescription(), null);
        storage.saveDigitalTwinStateValues(d0.getValues(), null, null);
        for (int temp : new int[]{20, 30}) {
            Thread.sleep(5);
            storage.saveDigitalTwinStateValues(state(ID_0, temp, false).getValues(), null, null);
        }

        Thread.sleep(5);
        DigitalTwinState d1 = state(ID_1, 40, true);
        storage.saveDigitalTwinStateDescription(d1.getDescription(), null);
        storage.saveDigitalTwinStateValues(d1.getValues(), null, null);
        Thread.sleep(5);
        storage.saveDigitalTwinStateValues(state(ID_1, 50, true).getValues(), null, null);
    }

    private QueryResult<?> run(Query query) throws Exception {
        QueryResult<?> result = storage.execute(query);
        assertTrue(result.isSuccessful(), result.getErrorMessage());
        return result;
    }

    @Test
    public void descriptionsAndValuesAreStoredSeparately() throws Exception {
        assertEquals(2, run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION).build()).getItems().size());
        assertEquals(5, run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE_VALUES).build()).getItems().size());
    }

    @Test
    public void valuesCarryTheIdOfTheirDescription() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE_VALUES)
                .where(FilterFactory.eq(QueryFieldNames.DESCRIPTION_ID, ID_1)).build());

        assertEquals(2, result.getItems().size());
        for (Object item : result.getItems())
            assertEquals(ID_1, ((DigitalTwinStateValuesRecord) item).getValues().getDescriptionId());
    }

    @Test
    public void descriptionsCanBeFilteredById() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE_DESCRIPTION)
                .where(FilterFactory.eq(QueryFieldNames.DESCRIPTION_ID, ID_1)).build());

        assertEquals(1, result.getItems().size());
        DigitalTwinStateDescriptionRecord record = (DigitalTwinStateDescriptionRecord) result.getItems().get(0);
        assertEquals(1, record.getDescription().getEvents().size());
    }

    /** A query on the complete state always returns a single history record */
    private static DigitalTwinStateHistoryRecord history(QueryResult<?> result) {
        assertEquals(1, result.getItems().size());
        assertTrue(result.getItems().get(0) instanceof DigitalTwinStateHistoryRecord);
        return (DigitalTwinStateHistoryRecord) result.getItems().get(0);
    }

    @Test
    public void completeStateMapsEveryDescriptionToItsValues() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build());

        DigitalTwinStateHistoryRecord history = history(result);
        assertEquals(2, history.size());
        // The total counts the selected rows (values records)
        assertEquals(5L, result.getTotal());

        // The entries follow the order of the rows
        assertEquals(Arrays.asList(ID_0, ID_1), new ArrayList<>(history.getEntries().keySet()));

        DigitalTwinStateHistoryEntry first = history.getEntry(ID_0);
        assertEquals(0, first.getDescription().getEvents().size());
        assertEquals(3, first.getValuesList().size());
        assertEquals(10, first.getValuesList().get(0).getValues().getPropertyValue("temp"));
        assertEquals(30, first.getValuesList().get(2).getValues().getPropertyValue("temp"));

        DigitalTwinStateHistoryEntry second = history.getEntry(ID_1);
        assertEquals(1, second.getDescription().getEvents().size());
        assertEquals(2, history.getValuesList(ID_1).size());
        assertEquals(2, history.getDescriptions().size());
    }

    @Test
    public void completeStateCanBeFilteredOnTheFieldsOfTheValues() throws Exception {
        DigitalTwinStateHistoryRecord history = history(run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .where(FilterFactory.gte("values.propertyValues.temp", 30)).build()));

        // The filter is evaluated on each values record: temp 30 of the first description, 40 and 50 of the second
        assertEquals(2, history.size());
        assertEquals(1, history.getValuesList(ID_0).size());
        assertEquals(30, history.getValuesList(ID_0).get(0).getValues().getPropertyValue("temp"));
        assertEquals(2, history.getValuesList(ID_1).size());
    }

    @Test
    public void completeStateCanBeFilteredOnTheDescriptionId() throws Exception {
        DigitalTwinStateHistoryRecord history = history(run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .where(FilterFactory.eq(QueryFieldNames.DESCRIPTION_ID, ID_0)).build()));

        assertEquals(1, history.size());
        assertEquals(3, history.getValuesList(ID_0).size());
    }

    @Test
    public void completeStateCanBeFilteredOnTheFieldsOfTheDescription() throws Exception {
        DigitalTwinStateHistoryRecord history = history(run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .where(FilterFactory.eq("description.id", ID_1)).build()));

        assertEquals(1, history.size());
        assertNotNull(history.getEntry(ID_1));
    }

    @Test
    public void completeStateSupportsSortAndLimitOnTheValues() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC).limit(1).build());

        // The most recent values together with their description
        DigitalTwinStateHistoryRecord history = history(result);
        assertEquals(1, history.size());
        assertEquals(1, history.getValuesList(ID_1).size());
        assertEquals(50, history.getValuesList(ID_1).get(0).getValues().getPropertyValue("temp"));
    }

    @Test
    public void descriptionsWithTheSameStructureAndDifferentIdsStayDistinct() throws Exception {
        // The structure of the first description is restored with a new id (for example a property added and removed)
        Thread.sleep(5);
        DigitalTwinState restored = state("7000_2", 60, false);
        storage.saveDigitalTwinStateDescription(restored.getDescription(), null);
        storage.saveDigitalTwinStateValues(restored.getValues(), null, null);

        DigitalTwinStateHistoryRecord history = history(run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build()));

        assertEquals(3, history.size());
        assertEquals(history.getEntry(ID_0).getDescription(), history.getEntry("7000_2").getDescription());
        assertEquals(3, history.getValuesList(ID_0).size());
        assertEquals(1, history.getValuesList("7000_2").size());
    }

    @Test
    public void completeStateWithoutMatchesReturnsAnEmptyHistory() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .where(FilterFactory.eq(QueryFieldNames.DESCRIPTION_ID, "unknown")).build());

        assertTrue(history(result).isEmpty());
    }

    @Test
    public void completeStateSupportsAggregationsOnDescriptionAndValuesFields() throws Exception {
        QueryResult<?> result = run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .aggregate(AggregationFactory.groupBy(QueryFieldNames.DESCRIPTION_ID),
                        AggregationFactory.count().as("count"),
                        AggregationFactory.avg("values.propertyValues.temp").as("avgTemp"))
                .orderBy(QueryFieldNames.DESCRIPTION_ID, SortCriterion.Direction.ASC)
                .build());

        assertEquals(2, result.getItems().size());

        AggregationRecord first = (AggregationRecord) result.getItems().get(0);
        assertEquals(3, first.getLong("count"));
        assertEquals(20.0, first.getDouble("avgTemp"), 0.0001);

        AggregationRecord second = (AggregationRecord) result.getItems().get(1);
        assertEquals(2, second.getLong("count"));
        assertEquals(45.0, second.getDouble("avgTemp"), 0.0001);
    }

    @Test
    public void descriptionWithoutValuesIsPartOfTheCompleteState() throws Exception {
        DigitalTwinState d2 = state("7000_2", 1, true);
        storage.saveDigitalTwinStateDescription(d2.getDescription(), null);

        DigitalTwinStateHistoryRecord history = history(run(Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build()));

        assertEquals(3, history.size());
        assertNotNull(history.getEntry("7000_2"));
        assertTrue(history.getValuesList("7000_2").isEmpty());
    }

    @Test
    public void statsReportDescriptionsAndValues() throws Exception {
        assertEquals(5, storage.getStorageStats().getStateValuesVariationStats().getRecordCount());
        assertEquals(2, storage.getStorageStats().getStateDescriptionVariationStats().getRecordCount());
    }
}

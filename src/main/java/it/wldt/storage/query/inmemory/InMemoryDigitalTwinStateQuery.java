/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.storage.query.inmemory;

import it.wldt.storage.model.StorageRecord;
import it.wldt.storage.model.state.DigitalTwinStateDescriptionRecord;
import it.wldt.storage.model.state.DigitalTwinStateHistoryEntry;
import it.wldt.storage.model.state.DigitalTwinStateHistoryRecord;
import it.wldt.storage.model.state.DigitalTwinStateValuesRecord;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResult;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory evaluation of a query on the complete Digital Twin State ({@code QueryResourceType.DIGITAL_TWIN_STATE}),
 * that is stored as two separated lists: the descriptions and the values.
 * <p>
 * Every values record is joined with the description it refers to (by description id) in a flat row, a description
 * without stored values becomes a row with null values, and the {@link InMemoryQueryEvaluator} evaluates filters,
 * sorting, limit/offset and aggregations on these rows, so every field of the description and of the values can be
 * used in a query. Then:
 * <ul>
 *     <li>without aggregation the selected rows are grouped by description in a <b>single</b>
 *     {@link DigitalTwinStateHistoryRecord} (description id &rarr; description + selected values, in the order of the
 *     query). The result always contains that record, that is empty if no row is selected. The total of the result
 *     counts the selected rows (values records), before limit and offset;</li>
 *     <li>with an aggregation the aggregation records of the evaluator are returned as they are.</li>
 * </ul>
 * The caller is responsible for passing a consistent snapshot of the two lists (the storage takes it under its locks).
 */
public final class InMemoryDigitalTwinStateQuery {

    private InMemoryDigitalTwinStateQuery() {}

    /**
     * Runs the query over the given snapshots of the stored descriptions and values
     * @param query the query to evaluate
     * @param descriptions storage timestamp to description record
     * @param values storage timestamp to values record
     * @return the query result
     */
    public static QueryResult<?> evaluate(Query query,
                                          List<? extends Map.Entry<Long, DigitalTwinStateDescriptionRecord>> descriptions,
                                          List<? extends Map.Entry<Long, DigitalTwinStateValuesRecord>> values) {

        Map<String, DigitalTwinStateDescriptionRecord> descriptionById = new HashMap<>();
        for (Map.Entry<Long, DigitalTwinStateDescriptionRecord> entry : descriptions)
            descriptionById.put(entry.getValue().getDescriptionId(), entry.getValue());

        // Rows: one for each values record joined with its description
        List<Map.Entry<Long, DigitalTwinStateSnapshotRecord>> rows = new ArrayList<>();
        Set<String> descriptionsWithValues = new HashSet<>();
        for (Map.Entry<Long, DigitalTwinStateValuesRecord> entry : values) {
            DigitalTwinStateValuesRecord valuesRecord = entry.getValue();
            descriptionsWithValues.add(valuesRecord.getDescriptionId());
            rows.add(new AbstractMap.SimpleEntry<>(entry.getKey(),
                    new DigitalTwinStateSnapshotRecord(descriptionById.get(valuesRecord.getDescriptionId()), valuesRecord)));
        }

        // Descriptions without any stored values
        for (Map.Entry<Long, DigitalTwinStateDescriptionRecord> entry : descriptions)
            if (!descriptionsWithValues.contains(entry.getValue().getDescriptionId()))
                rows.add(new AbstractMap.SimpleEntry<>(entry.getKey(), new DigitalTwinStateSnapshotRecord(entry.getValue(), null)));

        QueryResult<StorageRecord> result = InMemoryQueryEvaluator.evaluate(query, rows);

        // Aggregations (and errors) are returned as they are
        if (!result.isSuccessful() || query.getAggregation() != null)
            return result;

        // Group the selected rows by description, preserving the order of the query
        DigitalTwinStateHistoryRecord history = new DigitalTwinStateHistoryRecord();
        for (StorageRecord item : result.getItems()) {
            DigitalTwinStateSnapshotRecord row = (DigitalTwinStateSnapshotRecord) item;
            DigitalTwinStateHistoryEntry entry = history.getEntry(row.getDescriptionId());
            if (entry == null) {
                entry = new DigitalTwinStateHistoryEntry(row.getDescriptionId(), row.getDescription(), row.getDescriptionChangeList());
                history.addEntry(entry);
            }
            if (row.getValues() != null)
                entry.getValuesList().add(new DigitalTwinStateValuesRecord(row.getValues(), row.getStateChangeList()));
        }

        return QueryResult.success(query, Collections.singletonList(history),
                result.getTotal() != null ? result.getTotal() : result.getItems().size());
    }
}

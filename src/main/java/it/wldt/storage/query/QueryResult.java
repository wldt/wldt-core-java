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
package it.wldt.storage.query;

import it.wldt.storage.model.StorageRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * Result of a {@link Query}. The output of a query is always a list of record classes
 * (the {@link StorageRecord} hierarchy): the concrete records of the queried resource for a plain fetch,
 * {@code AggregationRecord} instances for an aggregation.
 *
 * @param <T> the record type contained in the result
 */
public class QueryResult<T extends StorageRecord> {

    private final Query originalQuery;

    private final boolean successful;

    private final String errorMessage;

    private final List<T> items;

    private final Long total;

    private QueryResult(Query originalQuery, boolean successful, String errorMessage, List<T> items, Long total) {
        this.originalQuery = originalQuery;
        this.successful = successful;
        this.errorMessage = errorMessage;
        this.items = items == null ? Collections.<T>emptyList() : Collections.unmodifiableList(items);
        this.total = total;
    }

    /**
     * Create a successful result
     * @param query the original query
     * @param items the result records
     * @param <T> the record type
     * @return the query result
     */
    public static <T extends StorageRecord> QueryResult<T> success(Query query, List<T> items) {
        return new QueryResult<>(query, true, null, items, null);
    }

    /**
     * Create a successful result carrying the total number of matches ignoring limit and offset
     * @param query the original query
     * @param items the result records
     * @param total the total number of matches
     * @param <T> the record type
     * @return the query result
     */
    public static <T extends StorageRecord> QueryResult<T> success(Query query, List<T> items, long total) {
        return new QueryResult<>(query, true, null, items, total);
    }

    /**
     * Create a failed result
     * @param query the original query
     * @param errorMessage the error message
     * @param <T> the record type
     * @return the query result
     */
    public static <T extends StorageRecord> QueryResult<T> failure(Query query, String errorMessage) {
        return new QueryResult<>(query, false, errorMessage, null, null);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Query getOriginalQuery() {
        return originalQuery;
    }

    /**
     * @return the result records (empty if the query failed or nothing matched)
     */
    public List<T> getItems() {
        return items;
    }

    /**
     * @return the number of matches ignoring limit and offset, null if not requested through {@link Query.Builder#withTotal()}
     */
    public Long getTotal() {
        return total;
    }

    /**
     * @return the first record, null if the result is empty
     */
    public T first() {
        return items.isEmpty() ? null : items.get(0);
    }

    /**
     * @return the last record, null if the result is empty
     */
    public T last() {
        return items.isEmpty() ? null : items.get(items.size() - 1);
    }

    /**
     * Typed view of the result, checking every record against the given class
     * @param recordClass the expected record class
     * @param <R> the expected record type
     * @return the same result typed with the expected record class
     * @throws ClassCastException if a record is not an instance of the given class
     */
    public <R extends StorageRecord> QueryResult<R> as(Class<R> recordClass) {
        List<R> typed = new ArrayList<>(items.size());
        for (T item : items)
            typed.add(recordClass.cast(item));
        return new QueryResult<>(originalQuery, successful, errorMessage, typed, total);
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "originalQuery=" + originalQuery +
                ", successful=" + successful +
                ", errorMessage='" + errorMessage + '\'' +
                ", items=" + items +
                ", total=" + total +
                '}';
    }
}

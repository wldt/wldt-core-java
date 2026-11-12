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

import it.wldt.storage.query.aggregation.Aggregation;
import it.wldt.storage.query.aggregation.AggregationGroupBy;
import it.wldt.storage.query.aggregation.AggregationMetric;
import it.wldt.storage.query.aggregation.AggregationPart;
import it.wldt.storage.query.filter.Filter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Immutable, backend-neutral description of a query on a storage resource.
 * A query is made of a resource, an optional filter tree, optional sorting, optional paging (limit/offset)
 * and an optional aggregation. Build it with {@link #on(QueryResourceType)}:
 * <pre>{@code
 * Query q = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
 *     .where(and(eq(QueryFieldNames.PROPERTY_KEY, "temperature"), gte(QueryFieldNames.TIMESTAMP, from)))
 *     .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
 *     .limit(100)
 *     .build();
 * }</pre>
 * Without an explicit sort, results are ordered by {@link QueryFieldNames#TIMESTAMP} ascending.
 */
public final class Query {

    private final QueryResourceType resource;
    private final Filter filter;
    private final List<SortCriterion> sort;
    private final Integer limit;
    private final int offset;
    private final Aggregation aggregation;
    private final boolean withTotal;
    private final long timeoutMs;
    private final String requestId;
    private final long requestTimestampMs;

    private Query(Builder builder) {
        this.resource = builder.resource;
        this.filter = builder.filter;
        this.sort = Collections.unmodifiableList(new ArrayList<>(builder.sort));
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.aggregation = builder.aggregation;
        this.withTotal = builder.withTotal;
        this.timeoutMs = builder.timeoutMs;
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestampMs = System.currentTimeMillis();
    }

    private Query(Query other, long timeoutMs) {
        this.resource = other.resource;
        this.filter = other.filter;
        this.sort = other.sort;
        this.limit = other.limit;
        this.offset = other.offset;
        this.aggregation = other.aggregation;
        this.withTotal = other.withTotal;
        this.timeoutMs = timeoutMs;
        this.requestId = UUID.randomUUID().toString();
        this.requestTimestampMs = System.currentTimeMillis();
    }

    /**
     * @return a copy of this query with a new request id and request timestamp, used to execute the same query
     * several times (e.g. periodic refresh) matching each execution with its own result
     */
    public Query refresh() {
        return new Query(this, timeoutMs);
    }

    /**
     * Same as {@link #refresh()} but, when this query has no timeout of its own, the copy gets the given one.
     * @param defaultTimeoutMs the timeout to use if this query does not define one
     * @return a copy of this query with a new request id and request timestamp
     */
    public Query refresh(long defaultTimeoutMs) {
        return new Query(this, timeoutMs > 0 ? timeoutMs : defaultTimeoutMs);
    }

    /**
     * Start building a query on the given resource
     * @param resource the resource to be queried
     * @return the query builder
     */
    public static Builder on(QueryResourceType resource) {
        if (resource == null)
            throw new IllegalArgumentException("Query resource cannot be null");
        return new Builder(resource);
    }

    public QueryResourceType getResource() {
        return resource;
    }

    /** @return the filter, null if all the records match */
    public Filter getFilter() {
        return filter;
    }

    /** @return the sort criteria, empty means default order (timestamp ascending) */
    public List<SortCriterion> getSort() {
        return sort;
    }

    /** @return the max number of results, null if unlimited */
    public Integer getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    /** @return the aggregation, null for a plain fetch */
    public Aggregation getAggregation() {
        return aggregation;
    }

    /** @return true if the result must carry the total number of matches ignoring limit and offset */
    public boolean isWithTotal() {
        return withTotal;
    }

    /**
     * @return how long (ms) the caller waits for the result, 0 if not set. A storage can skip a query that is
     * older than this, since nobody is waiting for its result any more.
     */
    public long getTimeoutMs() {
        return timeoutMs;
    }

    /** @return the unique id used to match the query with its result */
    public String getRequestId() {
        return requestId;
    }

    public long getRequestTimestampMs() {
        return requestTimestampMs;
    }

    @Override
    public String toString() {
        return "Query{resource=" + resource +
                ", filter=" + filter +
                ", sort=" + sort +
                ", limit=" + limit +
                ", offset=" + offset +
                ", aggregation=" + aggregation +
                ", withTotal=" + withTotal +
                ", timeoutMs=" + timeoutMs +
                ", requestId='" + requestId + '\'' +
                '}';
    }

    /**
     * Query builder
     */
    public static final class Builder {

        private final QueryResourceType resource;
        private Filter filter;
        private final List<SortCriterion> sort = new ArrayList<>();
        private Integer limit;
        private int offset;
        private Aggregation aggregation;
        private boolean withTotal;
        private long timeoutMs;

        private Builder(QueryResourceType resource) {
            this.resource = resource;
        }

        /**
         * @param filter the filter selecting the records, null selects all
         * @return this builder
         */
        public Builder where(Filter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Add a sort criterion (call multiple times for multi-field sorting)
         * @param field the field to sort by
         * @param direction the sort direction
         * @return this builder
         */
        public Builder orderBy(String field, SortCriterion.Direction direction) {
            this.sort.add(new SortCriterion(field, direction));
            return this;
        }

        /**
         * @param limit the max number of results
         * @return this builder
         */
        public Builder limit(int limit) {
            if (limit < 0)
                throw new IllegalArgumentException("Query limit cannot be negative");
            this.limit = limit;
            return this;
        }

        /**
         * @param offset the number of results to skip
         * @return this builder
         */
        public Builder offset(int offset) {
            if (offset < 0)
                throw new IllegalArgumentException("Query offset cannot be negative");
            this.offset = offset;
            return this;
        }

        /**
         * Turn the query into an aggregation, built from {@link it.wldt.storage.query.aggregation.AggregationFactory} helpers
         * @param parts at most one group-by and at least one metric
         * @return this builder
         */
        public Builder aggregate(AggregationPart... parts) {
            List<String> groupBy = new ArrayList<>();
            List<AggregationMetric> metrics = new ArrayList<>();
            for (AggregationPart part : parts) {
                if (part instanceof AggregationGroupBy)
                    groupBy.addAll(((AggregationGroupBy) part).getFields());
                else if (part instanceof AggregationMetric)
                    metrics.add((AggregationMetric) part);
            }
            this.aggregation = new Aggregation(groupBy, metrics);
            return this;
        }

        /**
         * Ask the storage to also compute the total number of matches ignoring limit and offset
         * @return this builder
         */
        public Builder withTotal() {
            this.withTotal = true;
            return this;
        }

        /**
         * How long the caller waits for the result. Overrides the default timeout of the {@code QueryExecutor};
         * use a larger value for heavy queries.
         * @param timeoutMs the timeout in milliseconds
         * @return this builder
         */
        public Builder timeout(long timeoutMs) {
            if (timeoutMs <= 0)
                throw new IllegalArgumentException("Query timeout must be positive");
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Query build() {
            return new Query(this);
        }
    }
}

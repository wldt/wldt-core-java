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

import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryFieldNames;
import it.wldt.storage.query.QueryResult;
import it.wldt.storage.query.SortCriterion;
import it.wldt.storage.query.aggregation.Aggregation;
import it.wldt.storage.query.aggregation.AggregationFunction;
import it.wldt.storage.query.aggregation.AggregationMetric;
import it.wldt.storage.query.filter.AndFilter;
import it.wldt.storage.query.filter.ComparisonFilter;
import it.wldt.storage.query.filter.Filter;
import it.wldt.storage.query.filter.FilterVisitor;
import it.wldt.storage.query.filter.NotFilter;
import it.wldt.storage.query.filter.OrFilter;
import it.wldt.storage.model.AggregationRecord;
import it.wldt.storage.model.StorageRecord;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Evaluates a {@link Query} over records held in memory (filter, sort, offset/limit, aggregation).
 * It is meant for storages without a query engine, like the default in-memory storage. Storages backed by a
 * database should translate the {@link Query} into native queries instead (see {@link FilterVisitor}).
 * <p>
 * The source is a list of (storage timestamp, record) entries; the timestamp is exposed as {@link QueryFieldNames#TIMESTAMP}.
 * Other fields are read through the record getters (dotted paths traverse maps and nested getters).
 */
public final class InMemoryQueryEvaluator {

    private static final Map<String, Method> GETTER_CACHE = new ConcurrentHashMap<>();

    private static final Method NO_GETTER;

    static {
        try {
            NO_GETTER = Object.class.getMethod("hashCode");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private InMemoryQueryEvaluator() {}

    /** A record together with its storage timestamp. */
    private static final class Row {
        private final long timestamp;
        private final StorageRecord record;
        // Insertion position, used to order records with equal sort values
        private final int sequence;

        private Row(long timestamp, StorageRecord record, int sequence) {
            this.timestamp = timestamp;
            this.record = record;
            this.sequence = sequence;
        }
    }

    /**
     * Run the query over the given records
     * @param query the query to evaluate
     * @param source storage timestamp to record
     * @return the query result
     */
    public static QueryResult<StorageRecord> evaluate(Query query, List<? extends Map.Entry<Long, ? extends StorageRecord>> source) {

        // Filter
        Predicate<Row> matcher = query.getFilter() == null ? row -> true : query.getFilter().accept(new RowMatcher());
        List<Map.Entry<Long, ? extends StorageRecord>> snapshot;
        synchronized (source) {
            snapshot = new ArrayList<>(source);
        }

        List<Row> resultRows;
        long total;

        if (query.getAggregation() != null) {
            List<Row> rows = new ArrayList<>();
            int sequence = 0;
            for (Map.Entry<Long, ? extends StorageRecord> entry : snapshot) {
                Row row = new Row(entry.getKey(), entry.getValue(), sequence++);
                if (matcher.test(row))
                    rows.add(row);
            }
            resultRows = aggregate(rows, query.getAggregation());
            sort(resultRows, query.getSort());
            total = resultRows.size();
        } else {
            // Default order: timestamp ascending
            Comparator<Row> order = comparator(query.getSort().isEmpty()
                    ? Collections.singletonList(new SortCriterion(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.ASC))
                    : query.getSort());

            if (query.getLimit() == null) {
                resultRows = new ArrayList<>();
                int sequence = 0;
                for (Map.Entry<Long, ? extends StorageRecord> entry : snapshot) {
                    Row row = new Row(entry.getKey(), entry.getValue(), sequence++);
                    if (matcher.test(row))
                        resultRows.add(row);
                }
                resultRows.sort(order);
                total = resultRows.size();
            } else {
                // Only offset + limit rows can be returned: keep just those in a bounded heap (worst row on top)
                // instead of sorting every matching record
                int keep = (int) Math.min((long) query.getOffset() + query.getLimit(), Integer.MAX_VALUE - 1);
                PriorityQueue<Row> best = new PriorityQueue<>(Math.min(keep, 1024) + 1, order.reversed());
                long matches = 0;
                int sequence = 0;
                for (Map.Entry<Long, ? extends StorageRecord> entry : snapshot) {
                    Row row = new Row(entry.getKey(), entry.getValue(), sequence++);
                    if (!matcher.test(row))
                        continue;
                    matches++;
                    if (best.size() < keep) {
                        best.add(row);
                    } else if (keep > 0 && order.compare(row, best.peek()) < 0) {
                        best.poll();
                        best.add(row);
                    }
                }
                resultRows = new ArrayList<>(best);
                resultRows.sort(order);
                total = matches;
            }
        }

        // Paging
        int from = Math.min(query.getOffset(), resultRows.size());
        int to = query.getLimit() == null ? resultRows.size() : (int) Math.min((long) from + query.getLimit(), resultRows.size());

        List<StorageRecord> items = new ArrayList<>(to - from);
        for (Row row : resultRows.subList(from, to))
            items.add(row.record);

        return query.isWithTotal() ? QueryResult.success(query, items, total) : QueryResult.success(query, items);
    }

    // ---------------------------------------------------------------- filter

    private static final class RowMatcher implements FilterVisitor<Predicate<Row>> {

        @Override
        public Predicate<Row> visitComparison(ComparisonFilter comparison) {
            final String field = comparison.getField();
            final Object expected = comparison.getValue();
            switch (comparison.getOperator()) {
                case EQ:
                    return row -> valuesEqual(resolve(row, field), expected);
                case NE:
                    return row -> !valuesEqual(resolve(row, field), expected);
                case GT:
                    return row -> { Integer c = compare(resolve(row, field), expected); return c != null && c > 0; };
                case GTE:
                    return row -> { Integer c = compare(resolve(row, field), expected); return c != null && c >= 0; };
                case LT:
                    return row -> { Integer c = compare(resolve(row, field), expected); return c != null && c < 0; };
                case LTE:
                    return row -> { Integer c = compare(resolve(row, field), expected); return c != null && c <= 0; };
                case IN:
                    final Iterable<?> candidates = expected instanceof Iterable ? (Iterable<?>) expected : Collections.singletonList(expected);
                    return row -> {
                        Object value = resolve(row, field);
                        for (Object candidate : candidates)
                            if (valuesEqual(value, candidate))
                                return true;
                        return false;
                    };
                case EXISTS:
                    final boolean shouldExist = !Boolean.FALSE.equals(expected);
                    return row -> (resolve(row, field) != null) == shouldExist;
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + comparison.getOperator());
            }
        }

        @Override
        public Predicate<Row> visitAnd(AndFilter and) {
            final List<Predicate<Row>> children = new ArrayList<>();
            for (Filter child : and.getChildren())
                children.add(child.accept(this));
            return row -> {
                for (Predicate<Row> child : children)
                    if (!child.test(row))
                        return false;
                return true;
            };
        }

        @Override
        public Predicate<Row> visitOr(OrFilter or) {
            final List<Predicate<Row>> children = new ArrayList<>();
            for (Filter child : or.getChildren())
                children.add(child.accept(this));
            return row -> {
                for (Predicate<Row> child : children)
                    if (child.test(row))
                        return true;
                return false;
            };
        }

        @Override
        public Predicate<Row> visitNot(NotFilter not) {
            return not.getChild().accept(this).negate();
        }
    }

    // ---------------------------------------------------------------- sort

    private static void sort(List<Row> rows, List<SortCriterion> specs) {
        if (specs.isEmpty())
            return;
        rows.sort(comparator(specs));
    }

    /**
     * Comparator of the sort criteria. Rows with equal sort values follow the insertion order,
     * reversed for a descending first criterion (so the most recently inserted comes first).
     */
    private static Comparator<Row> comparator(List<SortCriterion> specs) {
        Comparator<Row> comparator = null;
        for (SortCriterion spec : specs) {
            final String field = spec.getField();
            Comparator<Row> next = (a, b) -> compareNullsFirst(resolve(a, field), resolve(b, field));
            if (spec.getDirection() == SortCriterion.Direction.DESC)
                next = next.reversed();
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }
        Comparator<Row> insertion = Comparator.comparingInt(row -> row.sequence);
        if (specs.get(0).getDirection() == SortCriterion.Direction.DESC)
            insertion = insertion.reversed();
        return comparator.thenComparing(insertion);
    }

    private static int compareNullsFirst(Object a, Object b) {
        if (a == null || b == null)
            return a == b ? 0 : (a == null ? -1 : 1);
        Integer c = compare(a, b);
        return c == null ? 0 : c;
    }

    // ---------------------------------------------------------------- aggregation

    private static List<Row> aggregate(List<Row> rows, Aggregation aggregation) {

        Map<List<Object>, List<Row>> groups = new LinkedHashMap<>();
        for (Row row : rows) {
            List<Object> key = new ArrayList<>(aggregation.getGroupBy().size());
            for (String field : aggregation.getGroupBy())
                key.add(resolve(row, field));
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        // Without group-by the aggregation always produces one record, even on empty input
        if (groups.isEmpty() && aggregation.getGroupBy().isEmpty())
            groups.put(Collections.emptyList(), new ArrayList<>());

        List<Row> result = new ArrayList<>(groups.size());
        for (Map.Entry<List<Object>, List<Row>> group : groups.entrySet()) {

            Map<String, Object> groupKeys = new LinkedHashMap<>();
            for (int i = 0; i < aggregation.getGroupBy().size(); i++)
                groupKeys.put(aggregation.getGroupBy().get(i), group.getKey().get(i));

            Map<String, Object> metrics = new LinkedHashMap<>();
            for (AggregationMetric metric : aggregation.getMetrics())
                metrics.put(metric.getAlias(), computeMetric(metric, group.getValue()));

            result.add(new Row(0L, new AggregationRecord(groupKeys, metrics), result.size()));
        }
        return result;
    }

    private static Object computeMetric(AggregationMetric metric, List<Row> rows) {

        if (metric.getFunction() == AggregationFunction.COUNT && metric.getField() == null)
            return (long) rows.size();

        long count = 0;
        double sum = 0;
        Object best = null;
        for (Row row : rows) {
            Object value = resolve(row, metric.getField());
            if (value == null)
                continue;
            count++;
            if (value instanceof Number)
                sum += ((Number) value).doubleValue();
            if (best == null) {
                best = value;
            } else {
                Integer c = compare(value, best);
                if (c != null && ((metric.getFunction() == AggregationFunction.MIN && c < 0) || (metric.getFunction() == AggregationFunction.MAX && c > 0)))
                    best = value;
            }
        }

        switch (metric.getFunction()) {
            case COUNT:
                return count;
            case SUM:
                return sum;
            case AVG:
                return count == 0 ? null : sum / count;
            case MIN:
            case MAX:
                return best;
            default:
                throw new IllegalArgumentException("Unsupported aggregation function: " + metric.getFunction());
        }
    }

    // ---------------------------------------------------------------- field access and comparison

    private static Object resolve(Row row, String path) {

        if (row.record instanceof AggregationRecord)
            return ((AggregationRecord) row.record).get(path);

        if (QueryFieldNames.TIMESTAMP.equals(path))
            return row.timestamp;

        Object current = row.record;
        for (String segment : path.split("\\.")) {
            if (current == null)
                return null;
            current = current instanceof Map ? ((Map<?, ?>) current).get(segment) : readProperty(current, segment);
        }
        return current;
    }

    private static Object readProperty(Object target, String name) {
        Method getter = GETTER_CACHE.computeIfAbsent(target.getClass().getName() + "#" + name, k -> findGetter(target.getClass(), name));
        if (getter == NO_GETTER)
            return null;
        try {
            return getter.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static Method findGetter(Class<?> type, String name) {
        String suffix = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String prefix : new String[]{"get", "is"}) {
            try {
                Method method = type.getMethod(prefix + suffix);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                // try next prefix
            }
        }
        return NO_GETTER;
    }

    private static boolean valuesEqual(Object a, Object b) {
        if (a == null || b == null)
            return a == b;
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        if (a instanceof Enum || b instanceof Enum)
            return enumName(a).equals(enumName(b));
        return a.equals(b);
    }

    private static String enumName(Object value) {
        return value instanceof Enum ? ((Enum<?>) value).name() : String.valueOf(value);
    }

    /**
     * @return the comparison result, null if the values are null or not comparable
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Integer compare(Object a, Object b) {
        if (a == null || b == null)
            return null;
        if (a instanceof Number && b instanceof Number)
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        if (a instanceof Comparable && a.getClass().isInstance(b))
            return ((Comparable) a).compareTo(b);
        if (a instanceof Enum || b instanceof Enum)
            return enumName(a).compareTo(enumName(b));
        return null;
    }
}

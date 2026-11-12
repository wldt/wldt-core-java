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
package it.wldt.storage.query.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Static factory methods to build {@link Filter} trees, meant to be statically imported:
 * <pre>{@code
 * import static it.wldt.storage.query.filter.FilterFactory.*;
 * Filter f = and(eq("propertykey", "temperature"), gte("timestamp", from));
 * }</pre>
 */
public final class FilterFactory {

    private FilterFactory() {}

    public static Filter eq(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.EQ, value);
    }

    public static Filter ne(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.NE, value);
    }

    public static Filter gt(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.GT, value);
    }

    public static Filter gte(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.GTE, value);
    }

    public static Filter lt(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.LT, value);
    }

    public static Filter lte(String field, Object value) {
        return new ComparisonFilter(field, FilterOperator.LTE, value);
    }

    public static Filter in(String field, Object... values) {
        return new ComparisonFilter(field, FilterOperator.IN, Arrays.asList(values));
    }

    public static Filter in(String field, Collection<?> values) {
        return new ComparisonFilter(field, FilterOperator.IN, new ArrayList<>(values));
    }

    public static Filter exists(String field) {
        return new ComparisonFilter(field, FilterOperator.EXISTS, Boolean.TRUE);
    }

    public static Filter and(Filter... filters) {
        return new AndFilter(nonEmpty(filters));
    }

    public static Filter or(Filter... filters) {
        return new OrFilter(nonEmpty(filters));
    }

    public static Filter not(Filter filter) {
        return new NotFilter(filter);
    }

    private static List<Filter> nonEmpty(Filter[] filters) {
        if (filters == null || filters.length == 0)
            throw new IllegalArgumentException("At least one filter is required");
        return new ArrayList<>(Arrays.asList(filters));
    }
}

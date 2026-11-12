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
package it.wldt.storage.query.aggregation;

import java.util.Arrays;

/**
 * Static helpers to build aggregation parts, meant to be statically imported:
 * <pre>{@code
 * Query.on(PHYSICAL_ASSET_PROPERTY_VARIATION)
 *      .aggregate(groupBy("propertykey"), count().as("n"), avg("body").as("avg"))
 * }</pre>
 */
public final class AggregationFactory {

    private AggregationFactory() {}

    public static AggregationGroupBy groupBy(String... fields) {
        return new AggregationGroupBy(Arrays.asList(fields));
    }

    public static AggregationMetric count() {
        return new AggregationMetric(AggregationFunction.COUNT, null, null);
    }

    public static AggregationMetric sum(String field) {
        return new AggregationMetric(AggregationFunction.SUM, field, null);
    }

    public static AggregationMetric avg(String field) {
        return new AggregationMetric(AggregationFunction.AVG, field, null);
    }

    public static AggregationMetric min(String field) {
        return new AggregationMetric(AggregationFunction.MIN, field, null);
    }

    public static AggregationMetric max(String field) {
        return new AggregationMetric(AggregationFunction.MAX, field, null);
    }
}

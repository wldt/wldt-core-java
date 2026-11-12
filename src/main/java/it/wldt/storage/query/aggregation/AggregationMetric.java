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

/**
 * A single aggregated value: {@code function(field) AS alias}.
 */
public final class AggregationMetric implements AggregationPart {

    private final AggregationFunction function;
    private final String field;
    private final String alias;

    public AggregationMetric(AggregationFunction function, String field, String alias) {
        if (function == null)
            throw new IllegalArgumentException("Aggregation function cannot be null");
        if (function != AggregationFunction.COUNT && (field == null || field.isEmpty()))
            throw new IllegalArgumentException("Aggregation function " + function + " requires a field");
        this.function = function;
        this.field = field;
        this.alias = alias != null ? alias : defaultAlias(function, field);
    }

    /**
     * @param newAlias the name of the value in the resulting record
     * @return a copy of this metric with the given alias
     */
    public AggregationMetric as(String newAlias) {
        return new AggregationMetric(function, field, newAlias);
    }

    public AggregationFunction getFunction() {
        return function;
    }

    public String getField() {
        return field;
    }

    public String getAlias() {
        return alias;
    }

    private static String defaultAlias(AggregationFunction function, String field) {
        String base = function.name().toLowerCase();
        return field == null ? base : base + "_" + field;
    }
}

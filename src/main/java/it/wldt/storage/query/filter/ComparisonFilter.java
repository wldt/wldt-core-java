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

/**
 * Leaf condition: {@code field operator value}. The field can be a dotted path (e.g. "variationMetadata.unit").
 * The value is a collection for {@link FilterOperator#IN}, a Boolean for {@link FilterOperator#EXISTS}.
 */
public final class ComparisonFilter implements Filter {

    private final String field;
    private final FilterOperator operator;
    private final Object value;

    public ComparisonFilter(String field, FilterOperator operator, Object value) {
        if (field == null || field.isEmpty())
            throw new IllegalArgumentException("Filter field cannot be null or empty");
        if (operator == null)
            throw new IllegalArgumentException("Filter operator cannot be null");
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public FilterOperator getOperator() {
        return operator;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public <R> R accept(FilterVisitor<R> visitor) {
        return visitor.visitComparison(this);
    }

    @Override
    public String toString() {
        return field + " " + operator + " " + value;
    }
}

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
package it.wldt.storage.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of an aggregation query: one record per group.
 * It holds the group-by values ({@link #getGroupKeys()}) and the computed metrics ({@link #getMetrics()}), both
 * addressable by name through the typed getters. Names are looked up in the group keys first, then in the metrics.
 */
public class AggregationRecord extends StorageRecord {

    // Group-by field name -> value (empty when the aggregation has no group-by)
    private final Map<String, Object> groupKeys;

    // Metric alias -> computed value
    private final Map<String, Object> metrics;

    /**
     * Constructor
     * @param groupKeys group-by field name to value
     * @param metrics metric alias to computed value
     */
    public AggregationRecord(Map<String, Object> groupKeys, Map<String, Object> metrics) {
        this.groupKeys = Collections.unmodifiableMap(new LinkedHashMap<>(groupKeys));
        this.metrics = Collections.unmodifiableMap(new LinkedHashMap<>(metrics));
    }

    public Map<String, Object> getGroupKeys() {
        return groupKeys;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    /**
     * @param name a group-by field name or a metric alias
     * @return the value, null if the name is unknown
     */
    public Object get(String name) {
        return groupKeys.containsKey(name) ? groupKeys.get(name) : metrics.get(name);
    }

    public String getString(String name) {
        Object value = get(name);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * @param name a group-by field name or a metric alias
     * @return the value as long (0 if missing or not numeric)
     */
    public long getLong(String name) {
        Object value = get(name);
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }

    /**
     * @param name a group-by field name or a metric alias
     * @return the value as double (NaN if missing or not numeric)
     */
    public double getDouble(String name) {
        Object value = get(name);
        return value instanceof Number ? ((Number) value).doubleValue() : Double.NaN;
    }

    @Override
    public String toString() {
        return "AggregationRecord{groupKeys=" + groupKeys + ", metrics=" + metrics + '}';
    }
}

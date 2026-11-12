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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregation description: optional group-by fields plus a list of metrics.
 * Each resulting group is returned as an {@code AggregationRecord}.
 * Build it through {@link AggregationFactory} helpers passed to {@link it.wldt.storage.query.Query.Builder#aggregate(AggregationPart...)}.
 */
public final class Aggregation {

    private final List<String> groupBy;
    private final List<AggregationMetric> metrics;

    public Aggregation(List<String> groupBy, List<AggregationMetric> metrics) {
        if (metrics == null || metrics.isEmpty())
            throw new IllegalArgumentException("At least one aggregation metric is required");
        this.groupBy = Collections.unmodifiableList(new ArrayList<>(groupBy));
        this.metrics = Collections.unmodifiableList(new ArrayList<>(metrics));
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public List<AggregationMetric> getMetrics() {
        return metrics;
    }

    @Override
    public String toString() {
        return "Aggregation{groupBy=" + groupBy + ", metrics=" + metrics.size() + "}";
    }
}

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
package it.wldt.core.state;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of the <b>values</b> of a {@link DigitalTwinState}, without its structure:
 * <ul>
 *     <li>the current value of each property (by key)</li>
 *     <li>the instances of each relationship (by relationship name)</li>
 *     <li>the instant of the last invocation of each action (by key), if any</li>
 *     <li>the instant of the last notification of each event (by key), if any</li>
 * </ul>
 * Property values are copied at creation time, so later updates of the state do not alter an existing snapshot
 * (the value objects themselves are not deep-copied).
 * <p>
 * Use {@link DigitalTwinState#getValues()} or {@link DigitalTwinState#getValues(DigitalTwinStateFilter)}
 * to obtain it. The structure is available through {@link DigitalTwinStateDescription}.
 */
public final class DigitalTwinStateValues {

    private final Instant evaluationInstant;
    private final Map<String, Object> propertyValues;
    private final Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationshipInstances;
    private final Map<String, Instant> lastActionInvocations;
    private final Map<String, Instant> lastEventNotifications;

    DigitalTwinStateValues(Instant evaluationInstant,
                           Map<String, Object> propertyValues,
                           Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationshipInstances,
                           Map<String, Instant> lastActionInvocations,
                           Map<String, Instant> lastEventNotifications) {
        this.evaluationInstant = evaluationInstant;
        this.propertyValues = Collections.unmodifiableMap(new HashMap<>(propertyValues));

        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> instances = new HashMap<>();
        for (Map.Entry<String, List<DigitalTwinStateRelationshipInstance<?>>> e : relationshipInstances.entrySet())
            instances.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
        this.relationshipInstances = Collections.unmodifiableMap(instances);

        this.lastActionInvocations = Collections.unmodifiableMap(new HashMap<>(lastActionInvocations));
        this.lastEventNotifications = Collections.unmodifiableMap(new HashMap<>(lastEventNotifications));
    }

    /**
     * @return the evaluation instant of the state these values have been extracted from
     */
    public Instant getEvaluationInstant() {
        return evaluationInstant;
    }

    /**
     * @return the (unmodifiable) property values by property key, never null
     */
    public Map<String, Object> getPropertyValues() {
        return propertyValues;
    }

    /**
     * @param key the property key
     * @return the value of the property, or null if the property is not part of these values
     */
    public Object getPropertyValue(String key) {
        return propertyValues.get(key);
    }

    /**
     * @return the (unmodifiable) relationship instances by relationship name, never null.
     * A relationship without instances is present with an empty list
     */
    public Map<String, List<DigitalTwinStateRelationshipInstance<?>>> getRelationshipInstances() {
        return relationshipInstances;
    }

    /**
     * @return the (unmodifiable) instant of the last invocation by action key, never null.
     * Actions never invoked are not present
     */
    public Map<String, Instant> getLastActionInvocations() {
        return lastActionInvocations;
    }

    /**
     * @return the (unmodifiable) instant of the last notification by event key, never null.
     * Events never notified are not present
     */
    public Map<String, Instant> getLastEventNotifications() {
        return lastEventNotifications;
    }

    /**
     * @return true if there is no value at all
     */
    public boolean isEmpty() {
        return propertyValues.isEmpty() && relationshipInstances.isEmpty()
                && lastActionInvocations.isEmpty() && lastEventNotifications.isEmpty();
    }

    @Override
    public String toString() {
        return "DigitalTwinStateValues{propertyValues=" + propertyValues +
                ", relationshipInstances=" + relationshipInstances +
                ", lastActionInvocations=" + lastActionInvocations +
                ", lastEventNotifications=" + lastEventNotifications +
                ", evaluationInstant=" + evaluationInstant + '}';
    }
}

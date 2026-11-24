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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Every values object refers to the {@link DigitalTwinStateDescription} it belongs to through
 * {@link #getDescriptionId()}.
 * <p>
 * On the event bus the values travel as a <b>delta</b>: only the values changed by a state update are present.
 * Components that keep a local copy of the values merge the delta with {@link #applyUpdate(DigitalTwinStateValues)}.
 * <p>
 * Use {@link DigitalTwinStateManager#getDigitalTwinStateValues()} to obtain the current complete values.
 * The structure is available through {@link DigitalTwinStateDescription}.
 */
public final class DigitalTwinStateValues {

    private final String descriptionId;
    private final Instant evaluationInstant;
    private final Map<String, Object> propertyValues;
    private final Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationshipInstances;
    private final Map<String, Instant> lastActionInvocations;
    private final Map<String, Instant> lastEventNotifications;

    DigitalTwinStateValues(String descriptionId,
                           Instant evaluationInstant,
                           Map<String, Object> propertyValues,
                           Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationshipInstances,
                           Map<String, Instant> lastActionInvocations,
                           Map<String, Instant> lastEventNotifications) {
        this.descriptionId = descriptionId;
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
     * @return the id of the {@link DigitalTwinStateDescription} these values are associated to
     */
    public String getDescriptionId() {
        return descriptionId;
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

    /**
     * Builds the values accepted by the content filter. Each category (properties, relationships, actions,
     * events) is filtered independently by name (null name list = all, empty = none).
     * The id of the description and the evaluation instant are preserved.
     * @param filter the content filter (null or inactive = no filter)
     * @return this same instance if no filter is active, otherwise a new filtered values object
     */
    public DigitalTwinStateValues filter(DigitalTwinStateFilter filter) {

        if (filter == null || !filter.isActive())
            return this;

        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<String, Object> e : propertyValues.entrySet())
            if (filter.acceptsProperty(e.getKey()))
                properties.put(e.getKey(), e.getValue());

        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationships = new HashMap<>();
        for (Map.Entry<String, List<DigitalTwinStateRelationshipInstance<?>>> e : relationshipInstances.entrySet())
            if (filter.acceptsRelationship(e.getKey()))
                relationships.put(e.getKey(), e.getValue());

        Map<String, Instant> actions = new HashMap<>();
        for (Map.Entry<String, Instant> e : lastActionInvocations.entrySet())
            if (filter.acceptsAction(e.getKey()))
                actions.put(e.getKey(), e.getValue());

        Map<String, Instant> events = new HashMap<>();
        for (Map.Entry<String, Instant> e : lastEventNotifications.entrySet())
            if (filter.acceptsEvent(e.getKey()))
                events.put(e.getKey(), e.getValue());

        return new DigitalTwinStateValues(descriptionId, evaluationInstant, properties, relationships, actions, events);
    }

    /**
     * Applies a values <b>delta</b> (the body of a state values update event) to these values and returns the
     * resulting complete values. These values are not modified (they are immutable).
     * <ol>
     *     <li>the property values, the relationship instances (the whole list of the touched relationship) and the
     *     last action invocations / event notifications of the delta replace the previous ones</li>
     *     <li>the description id and the evaluation instant are the ones of the delta</li>
     * </ol>
     * The removal of a property, relationship, action or event is not carried by a values update: it is applied by
     * {@link #alignTo(DigitalTwinStateDescription)} when the new description is received.
     * @param delta the delta carried by the values update event
     * @return the new complete values
     */
    public DigitalTwinStateValues applyUpdate(DigitalTwinStateValues delta) {

        if (delta == null)
            return this;

        Map<String, Object> properties = new HashMap<>(this.propertyValues);
        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationships = new HashMap<>(this.relationshipInstances);
        Map<String, Instant> actions = new HashMap<>(this.lastActionInvocations);
        Map<String, Instant> events = new HashMap<>(this.lastEventNotifications);

        properties.putAll(delta.propertyValues);
        relationships.putAll(delta.relationshipInstances);
        actions.putAll(delta.lastActionInvocations);
        events.putAll(delta.lastEventNotifications);

        return new DigitalTwinStateValues(delta.descriptionId, delta.evaluationInstant,
                properties, relationships, actions, events);
    }

    /**
     * Aligns these values to a new description: the values of the resources that are not defined anymore by the
     * description (removed properties, relationships, actions and events) are dropped and the description id is
     * replaced with the one of the description. The evaluation instant becomes the one of the description (the
     * moment of the structural change) if it is later than the instant of these values.
     * @param description the new description
     * @return the aligned values (a new object)
     */
    public DigitalTwinStateValues alignTo(DigitalTwinStateDescription description) {

        if (description == null)
            return this;

        Set<String> propertyKeys = new HashSet<>();
        for (DigitalTwinStateDescription.PropertyDescription p : description.getProperties())
            propertyKeys.add(p.getKey());

        Set<String> relationshipNames = new HashSet<>();
        for (DigitalTwinStateDescription.RelationshipDescription r : description.getRelationships())
            relationshipNames.add(r.getName());

        Set<String> actionKeys = new HashSet<>();
        for (DigitalTwinStateDescription.ActionDescription a : description.getActions())
            actionKeys.add(a.getKey());

        Set<String> eventKeys = new HashSet<>();
        for (DigitalTwinStateDescription.EventDescription e : description.getEvents())
            eventKeys.add(e.getKey());

        Map<String, Object> properties = new HashMap<>(this.propertyValues);
        properties.keySet().retainAll(propertyKeys);

        Map<String, List<DigitalTwinStateRelationshipInstance<?>>> relationships = new HashMap<>(this.relationshipInstances);
        relationships.keySet().retainAll(relationshipNames);

        Map<String, Instant> actions = new HashMap<>(this.lastActionInvocations);
        actions.keySet().retainAll(actionKeys);

        Map<String, Instant> events = new HashMap<>(this.lastEventNotifications);
        events.keySet().retainAll(eventKeys);

        // The aligned values are valid from the structural change that produced the description: they take its
        // evaluation instant (never an instant earlier than the one of these values)
        Instant alignedInstant = this.evaluationInstant;
        if (description.getEvaluationInstant() != null
                && (alignedInstant == null || description.getEvaluationInstant().isAfter(alignedInstant)))
            alignedInstant = description.getEvaluationInstant();

        return new DigitalTwinStateValues(description.getId(), alignedInstant,
                properties, relationships, actions, events);
    }

    /**
     * Creates empty values associated to the given description id.
     * @param descriptionId the id of the description
     * @return empty values
     */
    public static DigitalTwinStateValues empty(String descriptionId) {
        return new DigitalTwinStateValues(descriptionId, Instant.now(),
                Collections.<String, Object>emptyMap(),
                Collections.<String, List<DigitalTwinStateRelationshipInstance<?>>>emptyMap(),
                Collections.<String, Instant>emptyMap(),
                Collections.<String, Instant>emptyMap());
    }

    @Override
    public String toString() {
        return "DigitalTwinStateValues{descriptionId='" + descriptionId + "', propertyValues=" + propertyValues +
                ", relationshipInstances=" + relationshipInstances +
                ", lastActionInvocations=" + lastActionInvocations +
                ", lastEventNotifications=" + lastEventNotifications +
                ", evaluationInstant=" + evaluationInstant + '}';
    }
}

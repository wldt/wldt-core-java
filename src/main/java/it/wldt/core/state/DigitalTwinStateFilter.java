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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Immutable <b>content filter</b> selecting which resources of a {@link DigitalTwinState} are considered, by name.
 * <p>
 * Each category (properties, actions, events, relationships) is filtered independently with the same semantics:
 * <ul>
 *     <li>a {@code null} name list keeps the whole category (default)</li>
 *     <li>an empty name list removes the whole category</li>
 *     <li>a non-empty name list keeps only the resources with the listed (exact) names</li>
 * </ul>
 * The filter is include-only: there are no wildcards, patterns or exclusion lists.
 * Instances are immutable, every {@code with*} method returns a new filter.
 */
public final class DigitalTwinStateFilter {

    private static final DigitalTwinStateFilter ALL = new DigitalTwinStateFilter(null, null, null, null);

    private final List<String> propertyNames;
    private final List<String> actionNames;
    private final List<String> eventNames;
    private final List<String> relationshipNames;

    private DigitalTwinStateFilter(List<String> propertyNames,
                                   List<String> actionNames,
                                   List<String> eventNames,
                                   List<String> relationshipNames) {
        this.propertyNames = propertyNames;
        this.actionNames = actionNames;
        this.eventNames = eventNames;
        this.relationshipNames = relationshipNames;
    }

    /**
     * @return the filter that accepts every resource
     */
    public static DigitalTwinStateFilter all() {
        return ALL;
    }

    /**
     * Builds a filter from the four name lists (each one follows the null/empty/non-empty semantics of this class).
     * The lists are copied.
     * @param propertyNames the property names
     * @param actionNames the action names
     * @param eventNames the event names
     * @param relationshipNames the relationship names
     * @return the filter
     */
    public static DigitalTwinStateFilter of(Collection<String> propertyNames,
                                            Collection<String> actionNames,
                                            Collection<String> eventNames,
                                            Collection<String> relationshipNames) {
        return new DigitalTwinStateFilter(copy(propertyNames), copy(actionNames), copy(eventNames), copy(relationshipNames));
    }

    /**
     * Restrict the properties considered.
     * @param names the property names, null to remove the restriction, empty to exclude every property
     * @return a new filter
     */
    public DigitalTwinStateFilter withPropertyNames(String... names) {
        return new DigitalTwinStateFilter(copy(names), actionNames, eventNames, relationshipNames);
    }

    /**
     * Restrict the actions considered.
     * @param names the action names, null to remove the restriction, empty to exclude every action
     * @return a new filter
     */
    public DigitalTwinStateFilter withActionNames(String... names) {
        return new DigitalTwinStateFilter(propertyNames, copy(names), eventNames, relationshipNames);
    }

    /**
     * Restrict the events considered.
     * @param names the event names, null to remove the restriction, empty to exclude every event
     * @return a new filter
     */
    public DigitalTwinStateFilter withEventNames(String... names) {
        return new DigitalTwinStateFilter(propertyNames, actionNames, copy(names), relationshipNames);
    }

    /**
     * Restrict the relationships considered.
     * @param names the relationship names, null to remove the restriction, empty to exclude every relationship
     * @return a new filter
     */
    public DigitalTwinStateFilter withRelationshipNames(String... names) {
        return new DigitalTwinStateFilter(propertyNames, actionNames, eventNames, copy(names));
    }

    /**
     * @return the accepted property names (null = all, empty = none)
     */
    public List<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * @return the accepted action names (null = all, empty = none)
     */
    public List<String> getActionNames() {
        return actionNames;
    }

    /**
     * @return the accepted event names (null = all, empty = none)
     */
    public List<String> getEventNames() {
        return eventNames;
    }

    /**
     * @return the accepted relationship names (null = all, empty = none)
     */
    public List<String> getRelationshipNames() {
        return relationshipNames;
    }

    /**
     * @return true if at least one category is restricted, false if this filter accepts everything
     */
    public boolean isActive() {
        return propertyNames != null || actionNames != null || eventNames != null || relationshipNames != null;
    }

    /**
     * @param name the property name
     * @return true if the property is accepted by this filter
     */
    public boolean acceptsProperty(String name) {
        return propertyNames == null || propertyNames.contains(name);
    }

    /**
     * @param name the action name
     * @return true if the action is accepted by this filter
     */
    public boolean acceptsAction(String name) {
        return actionNames == null || actionNames.contains(name);
    }

    /**
     * @param name the event name
     * @return true if the event is accepted by this filter
     */
    public boolean acceptsEvent(String name) {
        return eventNames == null || eventNames.contains(name);
    }

    /**
     * @param name the relationship name
     * @return true if the relationship is accepted by this filter
     */
    public boolean acceptsRelationship(String name) {
        return relationshipNames == null || relationshipNames.contains(name);
    }

    /**
     * Checks if a state change concerns a resource accepted by this filter (by category and name).
     * @param change the state change
     * @return true if the resource affected by the change is accepted by this filter
     */
    public boolean accepts(DigitalTwinStateChange change) {
        if (change == null)
            return false;

        String name = DigitalTwinStateChangeFilter.resolveResourceName(change);

        switch (change.getResourceType()) {
            case PROPERTY:
            case PROPERTY_VALUE:
                return acceptsProperty(name);
            case ACTION:
                return acceptsAction(name);
            case EVENT:
                return acceptsEvent(name);
            case RELATIONSHIP:
            case RELATIONSHIP_INSTANCE:
                return acceptsRelationship(name);
            default:
                return false;
        }
    }

    /**
     * Checks if a change alters the <b>structure</b> of the state (see {@link DigitalTwinStateDescription}).
     * The decision is taken on the operation first: only {@code OPERATION_ADD}, {@code OPERATION_UPDATE} and
     * {@code OPERATION_REMOVE} on the definition of a property, relationship, action or event are description
     * changes. {@code OPERATION_UPDATE_VALUE} (property value updates, last invocation of an action, last
     * notification of an event) and the changes on relationship instances never alter the structure.
     * @param change the state change
     * @return true if the change alters the description of the state
     */
    public static boolean isDescriptionChange(DigitalTwinStateChange change) {
        if (change == null || change.getOperation() == null || change.getResourceType() == null)
            return false;

        switch (change.getOperation()) {
            case OPERATION_ADD:
            case OPERATION_UPDATE:
            case OPERATION_REMOVE:
                switch (change.getResourceType()) {
                    case PROPERTY:
                    case RELATIONSHIP:
                    case ACTION:
                    case EVENT:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    /**
     * Checks if a change must produce a <b>values</b> update (see {@link DigitalTwinStateValues}):
     * <ul>
     *     <li>{@code OPERATION_UPDATE_VALUE}: property value, last invocation of an action, last notification of an event;</li>
     *     <li>{@code OPERATION_ADD} and {@code OPERATION_UPDATE} of a property or of a relationship (they carry a value
     *     / the instances);</li>
     *     <li>{@code OPERATION_ADD} and {@code OPERATION_REMOVE} of a relationship instance (the structure does not change).</li>
     * </ul>
     * The {@code OPERATION_REMOVE} of a property, relationship, action or event is <b>not</b> a values change: it is
     * only a description change and every component that keeps a local copy of the values drops the value of the
     * removed resource when it applies the new description. The same holds for the addition or update of the
     * definition of an action or of an event, that carry no value.
     * @param change the state change
     * @return true if the change produces a values update
     */
    public static boolean isValueChange(DigitalTwinStateChange change) {
        if (change == null || change.getOperation() == null || change.getResourceType() == null)
            return false;

        switch (change.getOperation()) {
            case OPERATION_UPDATE_VALUE:
                return true;
            case OPERATION_ADD:
            case OPERATION_UPDATE:
                switch (change.getResourceType()) {
                    case PROPERTY:
                    case PROPERTY_VALUE:
                    case RELATIONSHIP:
                    case RELATIONSHIP_INSTANCE:
                        return true;
                    default:
                        return false;
                }
            case OPERATION_REMOVE:
                return DigitalTwinStateChange.ResourceType.RELATIONSHIP_INSTANCE.equals(change.getResourceType());
            default:
                return false;
        }
    }

    /**
     * @param changes the changes of a state update (may be null)
     * @return the changes concerning resources accepted by this filter (empty if changes is null)
     */
    public List<DigitalTwinStateChange> acceptedChanges(List<DigitalTwinStateChange> changes) {
        List<DigitalTwinStateChange> result = new ArrayList<>();
        if (changes != null)
            for (DigitalTwinStateChange change : changes)
                if (accepts(change))
                    result.add(change);
        return result;
    }

    /**
     * @param changes the changes of a state update (may be null)
     * @return the changes that alter the structure of the state and concern resources accepted by this filter
     */
    public List<DigitalTwinStateChange> descriptionChanges(List<DigitalTwinStateChange> changes) {
        List<DigitalTwinStateChange> result = new ArrayList<>();
        if (changes != null)
            for (DigitalTwinStateChange change : changes)
                if (isDescriptionChange(change) && accepts(change))
                    result.add(change);
        return result;
    }

    /**
     * @param changes the changes of a state update (may be null)
     * @return the changes that alter the values of the state and concern resources accepted by this filter
     */
    public List<DigitalTwinStateChange> valueChanges(List<DigitalTwinStateChange> changes) {
        List<DigitalTwinStateChange> result = new ArrayList<>();
        if (changes != null)
            for (DigitalTwinStateChange change : changes)
                if (isValueChange(change) && accepts(change))
                    result.add(change);
        return result;
    }

    private static List<String> copy(String[] names) {
        return names == null ? null : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(names)));
    }

    private static List<String> copy(Collection<String> names) {
        return names == null ? null : Collections.unmodifiableList(new ArrayList<>(names));
    }

    @Override
    public String toString() {
        return "DigitalTwinStateFilter{propertyNames=" + propertyNames + ", actionNames=" + actionNames +
                ", eventNames=" + eventNames + ", relationshipNames=" + relationshipNames + '}';
    }
}

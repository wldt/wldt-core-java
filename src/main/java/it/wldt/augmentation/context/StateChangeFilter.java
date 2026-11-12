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
package it.wldt.augmentation.context;

import it.wldt.core.state.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable rule used as a <b>trigger filter</b> on the Digital Twin State changes: a stateful augmentation function
 * is notified of a state update only if at least one {@link DigitalTwinStateChange} of the update matches
 * one of its rules.
 * <p>
 * A rule is made of three optional criteria (operations, resource types, resource names). An empty criterion is a
 * wildcard, and all the non-empty criteria must be satisfied by the change (AND). For example
 * {@code StateChangeFilter.builder().operations(OPERATION_UPDATE_VALUE).resourceTypes(PROPERTY_VALUE).resourceNames("temperature").build()}
 * matches only value updates of the property "temperature".
 */
public final class StateChangeFilter {

    private final Set<DigitalTwinStateChange.Operation> operations;
    private final Set<DigitalTwinStateChange.ResourceType> resourceTypes;
    private final Set<String> resourceNames;

    private StateChangeFilter(Set<DigitalTwinStateChange.Operation> operations,
                              Set<DigitalTwinStateChange.ResourceType> resourceTypes,
                              Set<String> resourceNames) {
        this.operations = Collections.unmodifiableSet(operations);
        this.resourceTypes = Collections.unmodifiableSet(resourceTypes);
        this.resourceNames = Collections.unmodifiableSet(resourceNames);
    }

    /**
     * @return a new builder for a {@link StateChangeFilter}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Rule matching the value update of the given properties.
     * @param propertyNames names of the properties (empty = any property)
     * @return the rule
     */
    public static StateChangeFilter onPropertyValueUpdate(String... propertyNames) {
        return builder()
                .operations(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE)
                .resourceTypes(DigitalTwinStateChange.ResourceType.PROPERTY_VALUE)
                .resourceNames(propertyNames)
                .build();
    }

    /**
     * Rule matching any change on the given resource names, regardless of operation and type.
     * @param resourceNames the names of the resources (empty = any resource)
     * @return the rule
     */
    public static StateChangeFilter onResources(String... resourceNames) {
        return builder().resourceNames(resourceNames).build();
    }

    /**
     * @return the operations accepted by this rule (empty = any)
     */
    public Set<DigitalTwinStateChange.Operation> getOperations() {
        return operations;
    }

    /**
     * @return the resource types accepted by this rule (empty = any)
     */
    public Set<DigitalTwinStateChange.ResourceType> getResourceTypes() {
        return resourceTypes;
    }

    /**
     * @return the resource names accepted by this rule (empty = any)
     */
    public Set<String> getResourceNames() {
        return resourceNames;
    }

    /**
     * Checks if the change satisfies all the criteria of this rule.
     * @param change the state change to check
     * @return true if the change matches
     */
    public boolean matches(DigitalTwinStateChange change) {
        if (change == null)
            return false;

        if (!operations.isEmpty() && !operations.contains(change.getOperation()))
            return false;

        if (!resourceTypes.isEmpty() && !resourceTypes.contains(change.getResourceType()))
            return false;

        if (!resourceNames.isEmpty()) {
            String name = resolveResourceName(change);
            return name != null && resourceNames.contains(name);
        }

        return true;
    }

    /**
     * Extracts the name of the resource affected by a change. For relationship instances the name of the
     * relationship is returned.
     * @param change the state change
     * @return the resource name, or null if it cannot be determined
     */
    public static String resolveResourceName(DigitalTwinStateChange change) {
        if (change == null || change.getResource() == null)
            return null;

        DigitalTwinStateResource resource = change.getResource();

        if (resource instanceof DigitalTwinStateProperty)
            return ((DigitalTwinStateProperty<?>) resource).getKey();
        if (resource instanceof DigitalTwinStateEvent)
            return ((DigitalTwinStateEvent) resource).getKey();
        if (resource instanceof DigitalTwinStateAction)
            return ((DigitalTwinStateAction) resource).getKey();
        if (resource instanceof DigitalTwinStateRelationship)
            return ((DigitalTwinStateRelationship<?>) resource).getName();
        if (resource instanceof DigitalTwinStateRelationshipInstance)
            return ((DigitalTwinStateRelationshipInstance<?>) resource).getRelationshipName();

        return null;
    }

    @Override
    public String toString() {
        return "StateChangeFilter{operations=" + operations + ", resourceTypes=" + resourceTypes +
                ", resourceNames=" + resourceNames + '}';
    }

    /**
     * Builder of {@link StateChangeFilter}.
     */
    public static final class Builder {

        private final Set<DigitalTwinStateChange.Operation> operations = new HashSet<>();
        private final Set<DigitalTwinStateChange.ResourceType> resourceTypes = new HashSet<>();
        private final Set<String> resourceNames = new HashSet<>();

        private Builder() {
        }

        /**
         * @param values accepted operations
         * @return this builder
         */
        public Builder operations(DigitalTwinStateChange.Operation... values) {
            if (values != null)
                operations.addAll(Arrays.asList(values));
            return this;
        }

        /**
         * @param values accepted resource types
         * @return this builder
         */
        public Builder resourceTypes(DigitalTwinStateChange.ResourceType... values) {
            if (values != null)
                resourceTypes.addAll(Arrays.asList(values));
            return this;
        }

        /**
         * @param values accepted resource names
         * @return this builder
         */
        public Builder resourceNames(String... values) {
            if (values != null)
                resourceNames.addAll(Arrays.asList(values));
            return this;
        }

        /**
         * @param values accepted resource names
         * @return this builder
         */
        public Builder resourceNames(Collection<String> values) {
            if (values != null)
                resourceNames.addAll(values);
            return this;
        }

        /**
         * @return the immutable rule
         */
        public StateChangeFilter build() {
            return new StateChangeFilter(new HashSet<>(operations), new HashSet<>(resourceTypes), new HashSet<>(resourceNames));
        }
    }
}

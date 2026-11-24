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

import it.wldt.exception.WldtDigitalTwinStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * Represents a change in the state of a Digital Twin.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class DigitalTwinStateChange {

    /**
     * Enum representing different types of operations on the Digital Twin state.
     */
    public static enum Operation {
        OPERATION_UPDATE("update_resource"),
        OPERATION_UPDATE_VALUE("update_resource_value"),
        OPERATION_ADD("add_resource"),
        OPERATION_REMOVE("remove_resource");

        private String value;

        private Operation(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Enum representing different types of resources affected by a state change.
     */
    public static enum ResourceType {

        PROPERTY("property"),
        PROPERTY_VALUE("property_value"),
        EVENT("event"),
        ACTION("action"),
        RELATIONSHIP("relationship"),
        RELATIONSHIP_INSTANCE("relationship_instance");

        private String value;

        private ResourceType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    private Operation operation;

    private ResourceType resourceType;

    private DigitalTwinStateResource resource;

    /**
     * Instant carried by the changes that record the trigger of an action or of an event
     * ({@link ResourceType#ACTION} / {@link ResourceType#EVENT} with {@link Operation#OPERATION_UPDATE_VALUE}).
     * It is null for every other change.
     */
    private Instant valueInstant;

    /**
     * Private constructor to prevent direct instantiation without required parameters.
     */
    private DigitalTwinStateChange(){

    }

    /**
     * Creates a new instance of DigitalTwinStateChange with the specified parameters.
     *
     * @param operation     The operation performed on the state (e.g., update, add, remove).
     * @param resourceType  The type of resource affected by the state change.
     * @param resource      The specific resource involved in the state change.
     * @throws WldtDigitalTwinStateException If any of the provided parameters is null.
     */
    public DigitalTwinStateChange(Operation operation,
                                  ResourceType resourceType,
                                  DigitalTwinStateResource resource) throws WldtDigitalTwinStateException {

        if(operation == null || resourceType == null || resource == null)
            throw new WldtDigitalTwinStateException("Wrong DigitalTwinStateChange constructor parameters ! Missing or null value/values");

        this.operation = operation;
        this.resourceType = resourceType;
        this.resource = resource;
    }

    /**
     * Creates a change that carries also the instant of the trigger of an action or of an event. The instant is
     * needed because the last invocation of an action / last notification of an event is a value of the state and
     * the developer can specify when the action was executed (for example
     * {@code DigitalTwinStateManager.notifyActionExecuted(key, instant)}); the constructor without the instant
     * cannot carry it.
     *
     * @param operation     The operation performed on the state (usually {@link Operation#OPERATION_UPDATE_VALUE}).
     * @param resourceType  The type of resource ({@link ResourceType#ACTION} or {@link ResourceType#EVENT}).
     * @param resource      The action or the event that has been triggered.
     * @param valueInstant  The instant of the trigger.
     * @throws WldtDigitalTwinStateException If any of the provided parameters is null.
     */
    public DigitalTwinStateChange(Operation operation,
                                  ResourceType resourceType,
                                  DigitalTwinStateResource resource,
                                  Instant valueInstant) throws WldtDigitalTwinStateException {
        this(operation, resourceType, resource);

        if(valueInstant == null)
            throw new WldtDigitalTwinStateException("Wrong DigitalTwinStateChange constructor parameters ! Missing or null instant");

        this.valueInstant = valueInstant;
    }

    /**
     * @return the instant of the trigger carried by the changes on the last invocation of an action / last
     * notification of an event, null for every other change
     */
    public Instant getValueInstant() {
        return valueInstant;
    }

    /**
     * Checks if this change alters the <b>structure</b> of the state (see {@link DigitalTwinStateDescription}).
     * The decision is taken on the operation first: only {@code OPERATION_ADD}, {@code OPERATION_UPDATE} and
     * {@code OPERATION_REMOVE} on the definition of a property, relationship, action or event are description
     * changes. {@code OPERATION_UPDATE_VALUE} (property value updates, last invocation of an action, last
     * notification of an event) and the changes on relationship instances never alter the structure.
     * @return true if the change alters the description of the state
     */
    public boolean isDescriptionChange() {
        if (operation == null || resourceType == null)
            return false;

        switch (operation) {
            case OPERATION_ADD:
            case OPERATION_UPDATE:
            case OPERATION_REMOVE:
                switch (resourceType) {
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
     * Checks if this change must produce a <b>values</b> update (see {@link DigitalTwinStateValues}):
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
     * @return true if the change produces a values update
     */
    public boolean isValueChange() {
        if (operation == null || resourceType == null)
            return false;

        switch (operation) {
            case OPERATION_UPDATE_VALUE:
                return true;
            case OPERATION_ADD:
            case OPERATION_UPDATE:
                switch (resourceType) {
                    case PROPERTY:
                    case PROPERTY_VALUE:
                    case RELATIONSHIP:
                    case RELATIONSHIP_INSTANCE:
                        return true;
                    default:
                        return false;
                }
            case OPERATION_REMOVE:
                return ResourceType.RELATIONSHIP_INSTANCE.equals(resourceType);
            default:
                return false;
        }
    }

    /**
     * @param changes the changes of a state update (may be null)
     * @return the changes that alter the structure of the state, in the same order (empty if changes is null)
     */
    public static List<DigitalTwinStateChange> descriptionChanges(List<DigitalTwinStateChange> changes) {
        List<DigitalTwinStateChange> result = new ArrayList<>();
        if (changes != null)
            for (DigitalTwinStateChange change : changes)
                if (change != null && change.isDescriptionChange())
                    result.add(change);
        return result;
    }

    /**
     * @param changes the changes of a state update (may be null)
     * @return the changes that alter the values of the state, in the same order (empty if changes is null)
     */
    public static List<DigitalTwinStateChange> valueChanges(List<DigitalTwinStateChange> changes) {
        List<DigitalTwinStateChange> result = new ArrayList<>();
        if (changes != null)
            for (DigitalTwinStateChange change : changes)
                if (change != null && change.isValueChange())
                    result.add(change);
        return result;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public DigitalTwinStateResource getResource() {
        return resource;
    }

    public void setResource(DigitalTwinStateResource resource) {
        this.resource = resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DigitalTwinStateChange)) return false;
        DigitalTwinStateChange that = (DigitalTwinStateChange) o;
        return operation == that.operation && resourceType == that.resourceType && Objects.equals(resource, that.resource) && Objects.equals(valueInstant, that.valueInstant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, resourceType, resource, valueInstant);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinStateChange{");
        sb.append("operation=").append(operation);
        sb.append(", resourceType=").append(resourceType);
        sb.append(", resource=").append(resource);
        if (valueInstant != null)
            sb.append(", valueInstant=").append(valueInstant);
        sb.append('}');
        return sb.toString();
    }
}

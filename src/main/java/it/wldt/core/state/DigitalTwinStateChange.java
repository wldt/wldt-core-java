package it.wldt.core.state;

import it.wldt.exception.WldtDigitalTwinStateException;

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
        return operation == that.operation && resourceType == that.resourceType && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operation, resourceType, resource);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinStateChange{");
        sb.append("operation=").append(operation);
        sb.append(", resourceType=").append(resourceType);
        sb.append(", resource=").append(resource);
        sb.append('}');
        return sb.toString();
    }
}

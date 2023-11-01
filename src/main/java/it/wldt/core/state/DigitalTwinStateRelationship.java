package it.wldt.core.state;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Structures and describes a Relationship in the Digital Twins's State.
 * This is just the description of the relationships while the effective values/instances are described through the
 * other class DigitalTwinStateRelationshipInstance
 */
public class DigitalTwinStateRelationship<T> extends DigitalTwinStateResource {
    private final String name;
    private final String type;
    private final Map<String, DigitalTwinStateRelationshipInstance<T>> instances = new HashMap<>();

    public DigitalTwinStateRelationship(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<DigitalTwinStateRelationshipInstance<?>> getInstances() {
        return new ArrayList<>(instances.values());
    }

    public boolean containsInstance(String instanceKey){
        return instances.containsKey(instanceKey);
    }

    public DigitalTwinStateRelationshipInstance<T> getInstance(String instanceKey){
        return instances.get(instanceKey);
    }

    public void addInstance(DigitalTwinStateRelationshipInstance<?> i){
        this.instances.put(i.getKey(), (DigitalTwinStateRelationshipInstance<T>) i);
    }

    public void removeInstance(String instanceKey){
        if(containsInstance(instanceKey))
            this.instances.remove(instanceKey);
    }

    @Override
    public String toString() {
        return "DigitalTwinStateRelationship{" +
                "name='" + name + '\'' +
                '}';
    }
}

package it.wldt.adapter.physical;


import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic relationship associated to the Physical Asset and identified by its name.
 * This is just the description of the relationships while the effective values/instances are described through the
 * other class PhysicalAssetRelationshipInstance.
 */
public class PhysicalAssetRelationship<T> {

    //TODO: add the type of the target of the relationship
    //TODO: add list of properties

    private final String name;

    public PhysicalAssetRelationship(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PhysicalAssetRelationshipInstance<T> createRelationshipInstance(T targetId){
        return new PhysicalAssetRelationshipInstance<>(this, targetId);
    }

    public PhysicalAssetRelationshipInstance<T> createRelationshipInstance(T targetId, Map<String, Object> metadata){
        return new PhysicalAssetRelationshipInstance<>(this, targetId, metadata);
    }

    @Override
    public String toString() {
        return "PhysicalAssetRelationship{" +
                "name='" + name + '\'' +
                '}';
    }
}

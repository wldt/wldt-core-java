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

    private String name;

    private String type;

    public PhysicalAssetRelationship(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PhysicalAssetRelationshipInstance<T> createRelationshipInstance(T targetId){
        return new PhysicalAssetRelationshipInstance<>(this, targetId);
    }

    public PhysicalAssetRelationshipInstance<T> createRelationshipInstance(T targetId, Map<String, Object> metadata){
        return new PhysicalAssetRelationshipInstance<>(this, targetId, metadata);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetRelationship{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

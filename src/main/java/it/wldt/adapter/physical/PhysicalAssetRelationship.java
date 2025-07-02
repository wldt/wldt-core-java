/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 * Contributors:
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 */
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

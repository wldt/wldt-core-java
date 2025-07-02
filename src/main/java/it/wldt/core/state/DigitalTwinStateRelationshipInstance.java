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
 * Co-author:
 *              Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *              Samuele Burattini (samuele.burattini@unibo.it)
 */
package it.wldt.core.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Structures and describes a Relationship Instance in the Digital Twins's State.
 * This is effective description of a relationship while its generic declaration is described through the class
 * DigitalTwinStateRelationship.
 */
public class DigitalTwinStateRelationshipInstance<T> extends DigitalTwinStateResource {

    private final String relationshipName;
    private final T targetId;
    private final String instanceKey;
    private final Map<String, Object> metadata;

    public DigitalTwinStateRelationshipInstance(String relationshipName, T targetId, String instanceKey, Map<String, Object> metadata) {
        this.relationshipName = relationshipName;
        this.targetId = targetId;
        this.instanceKey = instanceKey;
        this.metadata = metadata;
    }

    public DigitalTwinStateRelationshipInstance(String relationshipName, T targetId, String instanceKey) {
        this(relationshipName, targetId, instanceKey, new HashMap<>());
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public T getTargetId() {
        return targetId;
    }

    public String getKey() {
        return instanceKey;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "DigitalTwinStateRelationshipInstance{" +
                "relationshipName='" + relationshipName + '\'' +
                ", targetId='" + targetId + '\'' +
                ", instanceKey='" + instanceKey + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}

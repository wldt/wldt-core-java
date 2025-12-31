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

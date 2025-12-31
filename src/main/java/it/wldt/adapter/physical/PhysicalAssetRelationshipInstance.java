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
package it.wldt.adapter.physical;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 *          
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define the instance of a relationship associated to the Physical Asset and identified by its name.
 * This is effective description of a relationship while its generic declaration is described through the class
 * PhysicalAssetRelationship.
 */
public class PhysicalAssetRelationshipInstance<T> {
    private final static String INSTANCE_PREFIX_KEY = "physical.asset.relationship";
    private final String key;
    private final T targetId;
    private final PhysicalAssetRelationship<T> relationship;
    private final Map<String, Object> metadata = new HashMap<>();

    public PhysicalAssetRelationshipInstance(PhysicalAssetRelationship<T> relationship, T targetId) {
        this.key = String.format("%s.%s.%s", INSTANCE_PREFIX_KEY, relationship.getName(), targetId);
        this.targetId = targetId;
        this.relationship = relationship;
    }

    public PhysicalAssetRelationshipInstance(PhysicalAssetRelationship<T> relationship, T targetId, Map<String, Object> metadata){
        this(relationship, targetId);

        if(metadata != null)
            this.metadata.putAll(metadata);
    }

    public String getKey() {
        return key;
    }

    public T getTargetId() {
        return targetId;
    }

    public PhysicalAssetRelationship<T> getRelationship() {
        return relationship;
    }

    public Optional<Map<String, Object>> getMetadata() {
        return metadata.isEmpty() ? Optional.empty() : Optional.of(metadata);
    }

    public Optional<Object> getMetadata(String key){
        return metadata.isEmpty() || !metadata.containsKey(key) ? Optional.empty() : Optional.of(metadata);
    }

    public void putMetadata(String key, Object data){
        if(key == null || key.isEmpty() || data == null)
            throw new IllegalArgumentException("Relationship metadata key or data cannot be null");
        metadata.put(key, data);
    }

    public void removeMetadata(String key){
        metadata.remove(key);
    }

    @Override
    public String toString() {
        return "PhysicalAssetRelationshipInstance{" +
                "key='" + key + '\'' +
                ", targetId='" + targetId + '\'' +
                ", relationship=" + relationship +
                ", metadata=" + metadata +
                '}';
    }
}

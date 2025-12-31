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
package it.wldt.storage.model.physical;

import it.wldt.storage.model.StorageRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Physical Relationship Instance Variation Record Class
 * Represents a Physical Relationship Instance Variation in the storage
 */
public class PhysicalRelationshipInstanceVariationRecord extends StorageRecord {

    // Timestamp of the variation
    private long variationTimestamp;

    // Instance Key associated to Relationship Instance Variation
    private String instanceKey;

    // Instance Target Id associated to Relationship Instance Variation
    private Object instanceTargetId;

    // Relationship Name associated to Relationship Instance Variation
    private String relationshipName;

    // Relationship Type associated to Relationship Instance Variation
    private String relationshipType;

    private final Map<String, Object> metadata = new HashMap<>();

    /**
     * Default Constructor
     */
    public PhysicalRelationshipInstanceVariationRecord() {
    }

    /**
     * Constructor
     *
     * @param instanceKey Instance Key associated to the variation
     * @param instanceTargetId Instance Target Id associated to the variation
     * @param relationshipName Relationship Name associated to the variation
     * @param relationshipType Relationship Type associated to the variation
     * @param metadata Metadata associated to the variation
     */
    public PhysicalRelationshipInstanceVariationRecord(long variationTimestamp, String instanceKey, Object instanceTargetId, String relationshipName, String relationshipType, Map<String, Object> metadata) {
        this.variationTimestamp = variationTimestamp;
        this.instanceKey = instanceKey;
        this.instanceTargetId = instanceTargetId;
        this.relationshipName = relationshipName;
        this.relationshipType = relationshipType;
        this.metadata.putAll(metadata);
    }

    /**
     * Get the Timestamp of the Relationship Instance Variation
     * @return Timestamp of the Relationship Instance Variation
     */
    public long getVariationTimestamp() {
        return variationTimestamp;
    }

    /**
     * Set the Timestamp of the Relationship Instance Variation
     * @param variationTimestamp Timestamp of the Relationship Instance Variation
     */
    public void setVariationTimestamp(long variationTimestamp) {
        this.variationTimestamp = variationTimestamp;
    }

    /**
     * Get the Instance Key of the Relationship Instance Variation
     * @return Instance Key of the Relationship Instance Variation
     */
    public String getInstanceKey() {
        return instanceKey;
    }

    /**
     * Set the Instance Key of the Relationship Instance Variation
     * @param instanceKey Instance Key of the Relationship Instance Variation
     */
    public void setInstanceKey(String instanceKey) {
        this.instanceKey = instanceKey;
    }

    /**
     * Get the Instance Target Id of the Relationship Instance Variation
     * @return Instance Target Id of the Relationship Instance Variation
     */
    public Object getInstanceTargetId() {
        return instanceTargetId;
    }

    /**
     * Set the Instance Target Id of the Relationship Instance Variation
     * @param instanceTargetId Instance Target Id of the Relationship Instance Variation
     */
    public void setInstanceTargetId(Object instanceTargetId) {
        this.instanceTargetId = instanceTargetId;
    }

    /**
     * Get the Relationship Name of the Relationship Instance Variation
     * @return Relationship Name of the Relationship Instance Variation
     */
    public String getRelationshipName() {
        return relationshipName;
    }

    /**
     * Set the Relationship Name of the Relationship Instance Variation
     * @param relationshipName Relationship Name of the Relationship Instance Variation
     */
    public void setRelationshipName(String relationshipName) {
        this.relationshipName = relationshipName;
    }

    /**
     * Get the Relationship Type of the Relationship Instance Variation
     * @return Relationship Type of the Relationship Instance Variation
     */
    public String getRelationshipType() {
        return relationshipType;
    }

    /**
     * Set the Relationship Type of the Relationship Instance Variation
     * @param relationshipType Relationship Type of the Relationship Instance Variation
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * Get the Metadata associated to the Relationship Instance Variation
     * @return Metadata associated to the Relationship Instance Variation
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalRelationshipInstanceVariationRecord{");
        sb.append("recordId=").append(getId());
        sb.append("variationTimestamp=").append(variationTimestamp);
        sb.append("instanceKey='").append(instanceKey).append('\'');
        sb.append(", instanceTargetId=").append(instanceTargetId);
        sb.append(", relationshipName='").append(relationshipName).append('\'');
        sb.append(", relationshipType='").append(relationshipType).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
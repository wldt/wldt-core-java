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
package it.wldt.storage.model.lifecycle;

import it.wldt.core.engine.LifeCycleState;
import it.wldt.storage.model.StorageRecord;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Life Cycle Variation Record Class
 * Represents a Life Cycle Variation Record in the storage
 */
public class LifeCycleVariationRecord extends StorageRecord {

    // Life Cycle State associated to the variation
    private LifeCycleState lifeCycleState;

    // Timestamp of the variation
    private long timestamp;

    /**
     * Default Constructor
     */
    public LifeCycleVariationRecord() {
    }

    /**
     * Constructor
     *
     * @param lifeCycleState Life Cycle State associated to the variation
     * @param timestamp Timestamp of the variation
     */
    public LifeCycleVariationRecord(LifeCycleState lifeCycleState, long timestamp) {
        this.lifeCycleState = lifeCycleState;
        this.timestamp = timestamp;
    }

    /**
     * Get the Life Cycle State associated to the variation
     * @return Life Cycle State associated to the variation
     */
    public LifeCycleState getLifeCycleState() {
        return lifeCycleState;
    }

    /**
     * Set the Life Cycle State associated to the variation
     * @param lifeCycleState Life Cycle State associated to the variation
     */
    public void setLifeCycleState(LifeCycleState lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    /**
     * Get the timestamp of the variation
     * @return timestamp of the variation
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp of the variation
     * @param timestamp timestamp of the variation
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LifeCycleVariationRecord{" +
                "recordId=" + getId() +
                "lifeCycleState=" + lifeCycleState +
                ", timestamp=" + timestamp +
                '}';
    }

}

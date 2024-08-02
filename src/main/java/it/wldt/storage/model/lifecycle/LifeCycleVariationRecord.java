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

package it.wldt.storage.model;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 03/09/2024
 * Represents the Storage Stats for a specific target Storage
 * Contains the information about the number of records stored in the storage and the time range of the stored records
 * in terms of start and end timestamp for each type of stored resource. For each category of stored resource a specific
 * the StorageStatsRecord object is used to store the information.
 */
public class StorageStats {

    /**
     * Storage Stats for the State Variation Records
     */
    private StorageStatsRecord stateVariationStats;

    /**
     * Storage Stats for the Life Cycle Variation Records
     */
    private StorageStatsRecord lifeCycleVariationStats;

    /**
     * Storage Stats for the Physical Asset Property Variation Records
     */
    private StorageStatsRecord physicalAssetPropertyVariationStats;

    /**
     * Storage Stats for the Physical Asset Event Notification Records
     */
    private StorageStatsRecord physicalAssetEventNotificationStats;

    /**
     * Storage Stats for the Physical Asset Action Request Records
     */
    private StorageStatsRecord physicalAssetActionRequestStats;

    /**
     * Storage Stats for the Digital Action Request Records
     */
    private StorageStatsRecord digitalActionRequestStats;

    /**
     * Storage Stats for the New Physical Asset Description Notification Records
     */
    private StorageStatsRecord newPhysicalAssetDescriptionNotificationStats;

    /**
     * Storage Stats for the Updated Physical Asset Description Notification Records
     */
    private StorageStatsRecord updatedPhysicalAssetDescriptionNotificationStats;

    /**
     * Storage Stats for the Physical Relationship Instance Created Notification Records
     */
    private StorageStatsRecord physicalRelationshipInstanceCreatedNotificationStats;

    /**
     * Storage Stats for the Physical Relationship Instance Deleted Notification Records
     */
    private StorageStatsRecord physicalRelationshipInstanceDeletedNotificationStats;

    /**
     * Default Constructor
     */
    public StorageStats() {
    }

    /**
     * Constructor
     * @param stateVariationStats Storage Stats for the State Variation Records
     * @param lifeCycleVariationStats Storage Stats for the Life Cycle Variation Records
     * @param physicalAssetPropertyVariationStats Storage Stats for the Physical Asset Property Variation Records
     * @param physicalAssetEventNotificationStats Storage Stats for the Physical Asset Event Notification Records
     * @param physicalAssetActionRequestStats Storage Stats for the Physical Asset Action Request Records
     * @param digitalActionRequestStats Storage Stats for the Digital Action Request Records
     * @param newPhysicalAssetDescriptionNotificationStats Storage Stats for the New Physical Asset Description Notification Records
     * @param updatedPhysicalAssetDescriptionNotificationStats Storage Stats for the Updated Physical Asset Description Notification Records
     * @param physicalRelationshipInstanceCreatedNotificationStats Storage Stats for the Physical Relationship Instance Created Notification Records
     * @param physicalRelationshipInstanceDeletedNotificationStats Storage Stats for the Physical Relationship Instance Deleted Notification Records
     */
    public StorageStats(StorageStatsRecord stateVariationStats, StorageStatsRecord lifeCycleVariationStats, StorageStatsRecord physicalAssetPropertyVariationStats, StorageStatsRecord physicalAssetEventNotificationStats, StorageStatsRecord physicalAssetActionRequestStats, StorageStatsRecord digitalActionRequestStats, StorageStatsRecord newPhysicalAssetDescriptionNotificationStats, StorageStatsRecord updatedPhysicalAssetDescriptionNotificationStats, StorageStatsRecord physicalRelationshipInstanceCreatedNotificationStats, StorageStatsRecord physicalRelationshipInstanceDeletedNotificationStats) {
        this.stateVariationStats = stateVariationStats;
        this.lifeCycleVariationStats = lifeCycleVariationStats;
        this.physicalAssetPropertyVariationStats = physicalAssetPropertyVariationStats;
        this.physicalAssetEventNotificationStats = physicalAssetEventNotificationStats;
        this.physicalAssetActionRequestStats = physicalAssetActionRequestStats;
        this.digitalActionRequestStats = digitalActionRequestStats;
        this.newPhysicalAssetDescriptionNotificationStats = newPhysicalAssetDescriptionNotificationStats;
        this.updatedPhysicalAssetDescriptionNotificationStats = updatedPhysicalAssetDescriptionNotificationStats;
        this.physicalRelationshipInstanceCreatedNotificationStats = physicalRelationshipInstanceCreatedNotificationStats;
        this.physicalRelationshipInstanceDeletedNotificationStats = physicalRelationshipInstanceDeletedNotificationStats;
    }

    /**
     * Get the Storage Stats for the State Variation Records
     * @return Storage Stats for the State Variation Records
     */
    public StorageStatsRecord getStateVariationStats() {
        return stateVariationStats;
    }

    /**
     * Set the Storage Stats for the State Variation Records
     * @param stateVariationStats Storage Stats for the State Variation Records
     */
    public void setStateVariationStats(StorageStatsRecord stateVariationStats) {
        this.stateVariationStats = stateVariationStats;
    }

    /**
     * Get the Storage Stats for the Life Cycle Variation Records
     * @return Storage Stats for the Life Cycle Variation Records
     */
    public StorageStatsRecord getLifeCycleVariationStats() {
        return lifeCycleVariationStats;
    }

    /**
     * Set the Storage Stats for the Life Cycle Variation Records
     * @param lifeCycleVariationStats Storage Stats for the Life Cycle Variation Records
     */
    public void setLifeCycleVariationStats(StorageStatsRecord lifeCycleVariationStats) {
        this.lifeCycleVariationStats = lifeCycleVariationStats;
    }

    /**
     * Get the Storage Stats for the Physical Asset Property Variation Records
     * @return Storage Stats for the Physical Asset Property Variation Records
     */
    public StorageStatsRecord getPhysicalAssetPropertyVariationStats() {
        return physicalAssetPropertyVariationStats;
    }

    /**
     * Set the Storage Stats for the Physical Asset Property Variation Records
     * @param physicalAssetPropertyVariationStats Storage Stats for the Physical Asset Property Variation Records
     */
    public void setPhysicalAssetPropertyVariationStats(StorageStatsRecord physicalAssetPropertyVariationStats) {
        this.physicalAssetPropertyVariationStats = physicalAssetPropertyVariationStats;
    }

    /**
     * Get the Storage Stats for the Physical Asset Event Notification Records
     * @return Storage Stats for the Physical Asset Event Notification Records
     */
    public StorageStatsRecord getPhysicalAssetEventNotificationStats() {
        return physicalAssetEventNotificationStats;
    }

    /**
     * Set the Storage Stats for the Physical Asset Event Notification Records
     * @param physicalAssetEventNotificationStats Storage Stats for the Physical Asset Event Notification Records
     */
    public void setPhysicalAssetEventNotificationStats(StorageStatsRecord physicalAssetEventNotificationStats) {
        this.physicalAssetEventNotificationStats = physicalAssetEventNotificationStats;
    }

    /**
     * Get the Storage Stats for the Physical Asset Action Request Records
     * @return Storage Stats for the Physical Asset Action Request Records
     */
    public StorageStatsRecord getPhysicalAssetActionRequestStats() {
        return physicalAssetActionRequestStats;
    }

    /**
     * Set the Storage Stats for the Physical Asset Action Request Records
     * @param physicalAssetActionRequestStats Storage Stats for the Physical Asset Action Request Records
     */
    public void setPhysicalAssetActionRequestStats(StorageStatsRecord physicalAssetActionRequestStats) {
        this.physicalAssetActionRequestStats = physicalAssetActionRequestStats;
    }

    /**
     * Get the Storage Stats for the Digital Action Request Records
     * @return Storage Stats for the Digital Action Request Records
     */
    public StorageStatsRecord getDigitalActionRequestStats() {
        return digitalActionRequestStats;
    }

    /**
     * Set the Storage Stats for the Digital Action Request Records
     * @param digitalActionRequestStats Storage Stats for the Digital Action Request Records
     */
    public void setDigitalActionRequestStats(StorageStatsRecord digitalActionRequestStats) {
        this.digitalActionRequestStats = digitalActionRequestStats;
    }

    /**
     * Get the Storage Stats for the New Physical Asset Description Notification Records
     * @return Storage Stats for the New Physical Asset Description Notification Records
     */
    public StorageStatsRecord getNewPhysicalAssetDescriptionNotificationStats() {
        return newPhysicalAssetDescriptionNotificationStats;
    }

    /**
     * Set the Storage Stats for the New Physical Asset Description Notification Records
     * @param newPhysicalAssetDescriptionNotificationStats Storage Stats for the New Physical Asset Description Notification Records
     */
    public void setNewPhysicalAssetDescriptionNotificationStats(StorageStatsRecord newPhysicalAssetDescriptionNotificationStats) {
        this.newPhysicalAssetDescriptionNotificationStats = newPhysicalAssetDescriptionNotificationStats;
    }

    /**
     * Get the Storage Stats for the Updated Physical Asset Description Notification Records
     * @return Storage Stats for the Updated Physical Asset Description Notification Records
     */
    public StorageStatsRecord getUpdatedPhysicalAssetDescriptionNotificationStats() {
        return updatedPhysicalAssetDescriptionNotificationStats;
    }

    /**
     * Set the Storage Stats for the Updated Physical Asset Description Notification Records
     * @param updatedPhysicalAssetDescriptionNotificationStats Storage Stats for the Updated Physical Asset Description Notification Records
     */
    public void setUpdatedPhysicalAssetDescriptionNotificationStats(StorageStatsRecord updatedPhysicalAssetDescriptionNotificationStats) {
        this.updatedPhysicalAssetDescriptionNotificationStats = updatedPhysicalAssetDescriptionNotificationStats;
    }

    /**
     * Get the Storage Stats for the Physical Relationship Instance Created Notification Records
     * @return Storage Stats for the Physical Relationship Instance Created Notification Records
     */
    public StorageStatsRecord getPhysicalRelationshipInstanceCreatedNotificationStats() {
        return physicalRelationshipInstanceCreatedNotificationStats;
    }

    /**
     * Set the Storage Stats for the Physical Relationship Instance Created Notification Records
     * @param physicalRelationshipInstanceCreatedNotificationStats Storage Stats for the Physical Relationship Instance Created Notification Records
     */
    public void setPhysicalRelationshipInstanceCreatedNotificationStats(StorageStatsRecord physicalRelationshipInstanceCreatedNotificationStats) {
        this.physicalRelationshipInstanceCreatedNotificationStats = physicalRelationshipInstanceCreatedNotificationStats;
    }

    /**
     * Get the Storage Stats for the Physical Relationship Instance Deleted Notification Records
     * @return Storage Stats for the Physical Relationship Instance Deleted Notification Records
     */
    public StorageStatsRecord getPhysicalRelationshipInstanceDeletedNotificationStats() {
        return physicalRelationshipInstanceDeletedNotificationStats;
    }

    /**
     * Set the Storage Stats for the Physical Relationship Instance Deleted Notification Records
     * @param physicalRelationshipInstanceDeletedNotificationStats Storage Stats for the Physical Relationship Instance Deleted Notification Records
     */
    public void setPhysicalRelationshipInstanceDeletedNotificationStats(StorageStatsRecord physicalRelationshipInstanceDeletedNotificationStats) {
        this.physicalRelationshipInstanceDeletedNotificationStats = physicalRelationshipInstanceDeletedNotificationStats;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StorageStats{");
        sb.append("stateVariationStats=").append(stateVariationStats);
        sb.append(", lifeCycleVariationStats=").append(lifeCycleVariationStats);
        sb.append(", physicalAssetPropertyVariationStats=").append(physicalAssetPropertyVariationStats);
        sb.append(", physicalAssetEventNotificationStats=").append(physicalAssetEventNotificationStats);
        sb.append(", physicalAssetActionRequestStats=").append(physicalAssetActionRequestStats);
        sb.append(", digitalActionRequestStats=").append(digitalActionRequestStats);
        sb.append(", newPhysicalAssetDescriptionNotificationStats=").append(newPhysicalAssetDescriptionNotificationStats);
        sb.append(", updatedPhysicalAssetDescriptionNotificationStats=").append(updatedPhysicalAssetDescriptionNotificationStats);
        sb.append(", physicalRelationshipInstanceCreatedNotificationStats=").append(physicalRelationshipInstanceCreatedNotificationStats);
        sb.append(", physicalRelationshipInstanceDeletedNotificationStats=").append(physicalRelationshipInstanceDeletedNotificationStats);
        sb.append('}');
        return sb.toString();
    }
}
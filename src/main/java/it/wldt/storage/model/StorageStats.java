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
     * Storage Stats for the Augmentation Function Error Records
     */
    private StorageStatsRecord augmentationFunctionErrorStats;

    /**
     * Storage Stats for the Augmentation Function Registration Records
     */
    private StorageStatsRecord augmentationFunctionRegistrationStats;

    /**
     * Storage Stats for the Augmentation Function Unregistration Records
     */
    private StorageStatsRecord augmentationFunctionUnregistrationStats;

    /**
     * Storage Stats for the Augmentation Function Request Records
     */
    private StorageStatsRecord augmentationFunctionRequestStats;

    /**
     * Storage Stats for the Augmentation Function Result Records
     */
    private StorageStatsRecord augmentationFunctionResultStats;

    /**
     * Default Constructor
     */
    public StorageStats() {
    }

    /**
     * Constructor with all the parameters
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
     * @param augmentationFunctionErrorStats Storage Stats for the Augmentation Function Error Records
     * @param augmentationFunctionRegistrationStats Storage Stats for the Augmentation Function Registration Records
     * @param augmentationFunctionUnregistrationStats Storage Stats for the Augmentation Function Unregistration Records
     * @param augmentationFunctionRequestStats Storage Stats for the Augmentation Function Request Records
     * @param augmentationFunctionResultStats Storage Stats for the Augmentation Function Result Records
     */
    public StorageStats(StorageStatsRecord stateVariationStats, StorageStatsRecord lifeCycleVariationStats, StorageStatsRecord physicalAssetPropertyVariationStats, StorageStatsRecord physicalAssetEventNotificationStats, StorageStatsRecord physicalAssetActionRequestStats, StorageStatsRecord digitalActionRequestStats, StorageStatsRecord newPhysicalAssetDescriptionNotificationStats, StorageStatsRecord updatedPhysicalAssetDescriptionNotificationStats, StorageStatsRecord physicalRelationshipInstanceCreatedNotificationStats, StorageStatsRecord physicalRelationshipInstanceDeletedNotificationStats, StorageStatsRecord augmentationFunctionErrorStats, StorageStatsRecord augmentationFunctionRegistrationStats, StorageStatsRecord augmentationFunctionUnregistrationStats, StorageStatsRecord augmentationFunctionRequestStats, StorageStatsRecord augmentationFunctionResultStats) {
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
        this.augmentationFunctionErrorStats = augmentationFunctionErrorStats;
        this.augmentationFunctionRegistrationStats = augmentationFunctionRegistrationStats;
        this.augmentationFunctionUnregistrationStats = augmentationFunctionUnregistrationStats;
        this.augmentationFunctionRequestStats = augmentationFunctionRequestStats;
        this.augmentationFunctionResultStats = augmentationFunctionResultStats;
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

    /**
     * Get the Storage Stats for the Augmentation Function Error Records
     * @return Storage Stats for the Augmentation Function Error Records
     */
    public StorageStatsRecord getAugmentationFunctionErrorStats() {
        return augmentationFunctionErrorStats;
    }

    /**
     * Set the Storage Stats for the Augmentation Function Error Records
     * @param augmentationFunctionErrorStats Storage Stats for the Augmentation Function Error Records
     */
    public void setAugmentationFunctionErrorStats(StorageStatsRecord augmentationFunctionErrorStats) {
        this.augmentationFunctionErrorStats = augmentationFunctionErrorStats;
    }

    /**
     * Get the Storage Stats for the Augmentation Function Registration Records
     * @return Storage Stats for the Augmentation Function Registration Records
     */
    public StorageStatsRecord getAugmentationFunctionRegistrationStats() {
        return augmentationFunctionRegistrationStats;
    }

    /**
     * Set the Storage Stats for the Augmentation Function Registration Records
     * @param augmentationFunctionRegistrationStats Storage Stats for the Augmentation Function Registration Records
     */
    public void setAugmentationFunctionRegistrationStats(StorageStatsRecord augmentationFunctionRegistrationStats) {
        this.augmentationFunctionRegistrationStats = augmentationFunctionRegistrationStats;
    }

    /**
     * Get the Storage Stats for the Augmentation Function Unregistration Records
     * @return Storage Stats for the Augmentation Function Unregistration Records
     */
    public StorageStatsRecord getAugmentationFunctionUnregistrationStats() {
        return augmentationFunctionUnregistrationStats;
    }

    /**
     * Set the Storage Stats for the Augmentation Function Unregistration Records
     * @param augmentationFunctionUnregistrationStats Storage Stats for the Augmentation Function Unregistration Records
     */
    public void setAugmentationFunctionUnregistrationStats(StorageStatsRecord augmentationFunctionUnregistrationStats) {
        this.augmentationFunctionUnregistrationStats = augmentationFunctionUnregistrationStats;
    }

    /**
     * Get the Storage Stats for the Augmentation Function Request Records
     * @return Storage Stats for the Augmentation Function Request Records
     */
    public StorageStatsRecord getAugmentationFunctionRequestStats() {
        return augmentationFunctionRequestStats;
    }

    /**
     * Set the Storage Stats for the Augmentation Function Request Records
     * @param augmentationFunctionRequestStats Storage Stats for the Augmentation Function Request Records
     */
    public void setAugmentationFunctionRequestStats(StorageStatsRecord augmentationFunctionRequestStats) {
        this.augmentationFunctionRequestStats = augmentationFunctionRequestStats;
    }

    /**
     * Get the Storage Stats for the Augmentation Function Result Records
     * @return Storage Stats for the Augmentation Function Result Records
     */
    public StorageStatsRecord getAugmentationFunctionResultStats() {
        return augmentationFunctionResultStats;
    }

    /**
     * Set the Storage Stats for the Augmentation Function Result Records
     * @param augmentationFunctionResultStats Storage Stats for the Augmentation Function Result Records
     */
    public void setAugmentationFunctionResultStats(StorageStatsRecord augmentationFunctionResultStats) {
        this.augmentationFunctionResultStats = augmentationFunctionResultStats;
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
        sb.append(", augmentationFunctionErrorStats=").append(augmentationFunctionErrorStats);
        sb.append(", augmentationFunctionRegistrationStats=").append(augmentationFunctionRegistrationStats);
        sb.append(", augmentationFunctionUnregistrationStats=").append(augmentationFunctionUnregistrationStats);
        sb.append(", augmentationFunctionRequestStats=").append(augmentationFunctionRequestStats);
        sb.append(", augmentationFunctionResultStats=").append(augmentationFunctionResultStats);
        sb.append('}');
        return sb.toString();
    }
}
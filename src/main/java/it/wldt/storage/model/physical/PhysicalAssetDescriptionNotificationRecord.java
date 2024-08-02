package it.wldt.storage.model.physical;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.storage.model.StorageRecord;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Physical Asset Description Notification Record Class
 * Represents a Physical Asset Description Notification Record in the storage
 */
public class PhysicalAssetDescriptionNotificationRecord extends StorageRecord {

    // Timestamp of the notification
    private long notificationTimestamp;

    // Adapter Id associated to the notification
    private String adapterId;

    // Physical Asset Description associated to the notification
    private PhysicalAssetDescription physicalAssetDescription;

    /**
     * Default Constructor
     */
    public PhysicalAssetDescriptionNotificationRecord() {
    }

    /**
     * Constructor
     *
     * @param notificationTimestamp Timestamp of the notification
     * @param adapterId Adapter Id associated to the notification
     * @param physicalAssetDescription Physical Asset Description associated to the notification
     */
    public PhysicalAssetDescriptionNotificationRecord(long notificationTimestamp, String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        this.notificationTimestamp = notificationTimestamp;
        this.adapterId = adapterId;
        this.physicalAssetDescription = physicalAssetDescription;
    }

    /**
     * Get the notification timestamp
     * @return notification timestamp
     */
    public long getNotificationTimestamp() {
        return notificationTimestamp;
    }

    /**
     * Set the notification timestamp
     * @param notificationTimestamp notification timestamp
     */
    public void setNotificationTimestamp(long notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    /**
     * Get the Physical Asset Description associated to the notification
     * @return Physical Asset Description associated to the notification
     */
    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    /**
     * Set the Physical Asset Description associated to the notification
     * @param physicalAssetDescription Physical Asset Description associated to the notification
     */
    public void setPhysicalAssetDescription(PhysicalAssetDescription physicalAssetDescription) {
        this.physicalAssetDescription = physicalAssetDescription;
    }

    /**
     * Get the Adapter Id associated to the notification
     * @return Adapter Id associated to the notification
     */
    public String getAdapterId() {
        return adapterId;
    }

    /**
     * Set the Adapter Id associated to the notification
     * @param adapterId Adapter Id associated to the notification
     */
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetDescriptionNotificationRecord{");
        sb.append("storageRecordId=").append(super.getId());
        sb.append("notificationTimestamp=").append(notificationTimestamp);
        sb.append(", adapterId='").append(adapterId).append('\'');
        sb.append(", physicalAssetDescription=").append(physicalAssetDescription);
        sb.append('}');
        return sb.toString();
    }
}

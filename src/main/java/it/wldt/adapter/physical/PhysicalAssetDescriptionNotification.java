package it.wldt.adapter.physical;

/**
 * Authors: Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 26/07/2024
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class structures the notification message used to notify a Physical Asset Description creation and update.
 */
public class PhysicalAssetDescriptionNotification {

    private long notificationTimestamp;

    private String adapterId;

    private PhysicalAssetDescription physicalAssetDescription;

    public PhysicalAssetDescriptionNotification() {
    }

    public PhysicalAssetDescriptionNotification(long notificationTimestamp, String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        this.notificationTimestamp = notificationTimestamp;
        this.adapterId = adapterId;
        this.physicalAssetDescription = physicalAssetDescription;
    }

    public long getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(long notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }

    public void setPhysicalAssetDescription(PhysicalAssetDescription physicalAssetDescription) {
        this.physicalAssetDescription = physicalAssetDescription;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetDescriptionNotification{");
        sb.append("notificationTimestamp=").append(notificationTimestamp);
        sb.append(", adapterId='").append(adapterId).append('\'');
        sb.append(", physicalAssetDescription=").append(physicalAssetDescription);
        sb.append('}');
        return sb.toString();
    }
}

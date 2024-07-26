package it.wldt.adapter.physical;

public class PhysicalRelationshipInstanceVariation {

    private long notificationTimestamp;

    private PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance;

    private PhysicalRelationshipInstanceVariation() {
    }

    public PhysicalRelationshipInstanceVariation(long notificationTimestamp, PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance) {
        this.notificationTimestamp = notificationTimestamp;
        this.physicalAssetRelationshipInstance = physicalAssetRelationshipInstance;
    }

    public long getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(long notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }

    public PhysicalAssetRelationshipInstance<?> getPhysicalAssetRelationshipInstance() {
        return physicalAssetRelationshipInstance;
    }

    public void setPhysicalAssetRelationshipInstance(PhysicalAssetRelationshipInstance<?> physicalAssetRelationshipInstance) {
        this.physicalAssetRelationshipInstance = physicalAssetRelationshipInstance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalRelationshipInstanceVariationNotification{");
        sb.append("notificationTimestamp=").append(notificationTimestamp);
        sb.append(", physicalAssetRelationshipInstance=").append(physicalAssetRelationshipInstance);
        sb.append('}');
        return sb.toString();
    }
}

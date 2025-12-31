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

/**
 * This class represents a variation in the state of a physical asset relationship instance.
 * It contains a timestamp indicating when the notification was generated and the instance of the physical asset relationship that has changed.
 * This class is used to notify listeners about changes in the physical asset relationship instances.
 */
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

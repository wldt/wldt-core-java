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

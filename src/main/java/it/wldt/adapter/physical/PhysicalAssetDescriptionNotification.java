/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
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

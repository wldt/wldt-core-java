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

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
package it.wldt.storage.model.state;

import it.wldt.storage.model.StorageRecord;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Digital Twin State Event Notification Record Class
 * Represents a Digital Twin State Event Notification Record in the storage
 */
public class DigitalTwinStateEventNotificationRecord extends StorageRecord {

    // Event Key associated to the notification
    private String eventKey;

    // Body of the notification
    private Object body;

    // Timestamp of the notification
    private Long timestamp;

    /**
     * Default Constructor
     */
    public DigitalTwinStateEventNotificationRecord() {
    }

    /**
     * Constructor
     *
     * @param eventKey Event Key associated to the notification
     * @param body Body of the notification
     * @param timestamp Timestamp of the notification
     */
    public DigitalTwinStateEventNotificationRecord(String eventKey, Object body, Long timestamp) {
        this.eventKey = eventKey;
        this.body = body;
        this.timestamp = timestamp;
    }

    /**
     * Get the Event Key associated to the notification
     * @return Event Key associated to the notification
     */
    public String getEventKey() {
        return eventKey;
    }

    /**
     * Set the Event Key associated to the notification
     * @param eventKey Event Key associated to the notification
     */
    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    /**
     * Get the Body of the notification
     * @return Body of the notification
     */
    public Object getBody() {
        return body;
    }

    /**
     * Set the Body of the notification
     * @param body Body of the notification
     */
    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * Get the Timestamp of the notification
     * @return Timestamp of the notification
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the Timestamp of the notification
     * @param timestamp Timestamp of the notification
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateEventNotificationRecord{");
        sb.append("recordId=").append(getId());
        sb.append("eventKey='").append(eventKey).append('\'');
        sb.append(", body=").append(body);
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }
}

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

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
package it.wldt.core.state;

import java.util.Objects;

/**
 * Author:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 24/08/2022
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic event associated to the Digital Twin State.
 * Events enable a mechanism for asynchronous messages to be sent by the digital twin (e.g., an overheating)
 * They are different from Properties that can change values according to the type of Digital Twin and may be
 * associated also to telemetry patterns.
 * Each event is associated to a Key and it is described by two separate fields:
 * <ul>
 *     <li><b>type</b>: the semantic type of the event, i.e. what the event represents
 *     (e.g., "iot.demo.overheating"), typically taken from an ontology or from the application domain;</li>
 *     <li><b>contentType</b>: the format used to represent the body of the event notifications
 *     (e.g., "string" or "application/json").</li>
 * </ul>
 * The same pair is available in the {@code PhysicalAssetEvent} of the Physical Asset Description.
 */
public class DigitalTwinStateEvent extends DigitalTwinStateResource {

    /**
     * Key uniquely identifying the Digital Twin State Event
     */
    private String key;

    /**
     * Semantic type of the Event: what the event represents (e.g., "iot.demo.overheating"), typically associated to
     * a specific ontology or application domain. It does not describe how the body of the notification is
     * represented, that is the role of the contentType.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network.
     */
    private String type = null;

    /**
     * Content-Type of the Event: the format used to represent the body of its notifications
     * (e.g., "string", "application/json"). It can be null if it has not been declared.
     */
    private String contentType = null;

    public DigitalTwinStateEvent() {
    }

    /**
     * Creates an event with only the semantic type. The contentType is not set (null).
     * @param key uniquely identifying the event in the Digital Twin State
     * @param type type of the event
     * @deprecated the constructor with only the type will not be supported anymore from version 0.9.0.
     * Use {@link #DigitalTwinStateEvent(String, String, String)} to set both the semantic type and the contentType.
     */
    @Deprecated
    public DigitalTwinStateEvent(String key, String type) {
        this(key, type, null);
    }

    /**
     * Creates an event with a semantic type and a content type.
     * @param key uniquely identifying the event in the Digital Twin State
     * @param type semantic type of the event (e.g., "iot.demo.overheating")
     * @param contentType format of the body of the event notifications (e.g., "string")
     */
    public DigitalTwinStateEvent(String key, String type, String contentType) {
        this.key = key;
        this.type = type;
        this.contentType = contentType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the semantic type of the event
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the semantic type of the event (e.g., "iot.demo.overheating")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the content type of the event, i.e. the format of the body of its notifications
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the content type of the event (e.g., "string")
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalTwinStateEvent that = (DigitalTwinStateEvent) o;
        return Objects.equals(key, that.key) && Objects.equals(type, that.type) && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, contentType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateEvent{");
        sb.append("key='").append(key).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

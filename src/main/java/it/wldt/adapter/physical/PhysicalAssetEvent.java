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

import java.util.Objects;

/**
 * Author:
 *           Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 24/08/2022
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic event associated to the Physical Asset.
 * Events enable a mechanism for asynchronous messages to be sent by the physical asset (e.g., an overheating)
 * They are different from Properties that can change values according to the type of Physical Asset and may be
 * associated also to telemetry patterns.
 * Each event is associated to a Key and a Type used to identify its nature and data structure.
 * By default, it is associated to the type of the Class (e.g., java.lang.String)
 * but it can be directly changed by the developer to associate it to a specific ontology or data type.
 */
public class PhysicalAssetEvent {

    /**
     * Key uniquely identifying the Physical Event
     */
    private String key;

    /**
     * Type of the Event. It can be directly assigned by the developer to associate it to a specific ontology or data type.
     */
    private String type = null;

    /**
     * Content type of the Event. It can be directly assigned by the developer to associate it to a specific content type (e.g., application/json)
     */
    private String contentType = null;

    public PhysicalAssetEvent() {
    }

    /**
     * Constructor for creating a PhysicalAssetEvent with a key and type.
     *
     * @param key The key uniquely identifying the Physical Event on the Physical Twin
     * @param type The type of the Event, which can be directly assigned by the developer to associate it to a specific ontology or data type.
     */
    public PhysicalAssetEvent(String key, String type) {
        this.key = key;
        this.type = type;
        this.contentType = null;
    }

    /**
     * Constructor for creating a PhysicalAssetEvent with a key, type, and content type.
     *
     * @param key The key uniquely identifying the Physical Event on the Physical Twin
     * @param type The type of the Event, which can be directly assigned by the developer to associate it to a specific ontology or data type.
     * @param contentType The content type of the Event, which can be directly assigned by the developer to associate it to a specific content type (e.g., application/json)
     */
    public PhysicalAssetEvent(String key, String type, String contentType) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalAssetEvent that = (PhysicalAssetEvent) o;
        return key.equals(that.key) && type.equals(that.type) && contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, contentType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetEvent{");
        sb.append("key='").append(key).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

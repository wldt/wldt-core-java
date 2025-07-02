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
     * Type of the Event. By default, it is associated to the type of the Class T (e.g., java.lang.String) but it
     * can be directly changed by the developer to associate it to a specific ontology or data type.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network. In that case, the field can be used to de-serialize the object and understand
     * the property type
     */
    private String type = null;

    public PhysicalAssetEvent() {
    }

    public PhysicalAssetEvent(String key, String type) {
        this.key = key;
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalAssetEvent that = (PhysicalAssetEvent) o;
        return key.equals(that.key) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetEvent{");
        sb.append("key='").append(key).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

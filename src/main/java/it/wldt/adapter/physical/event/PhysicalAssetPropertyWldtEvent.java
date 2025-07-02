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
package it.wldt.adapter.physical.event;

import it.wldt.exception.EventBusException;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * A WLDT event describing a variation on physical property occurred on the physical asset connected to a Physical Adapter
 */
public class PhysicalAssetPropertyWldtEvent<T> extends PhysicalAssetWldtEvent<T> {

    public static final String PHYSICAL_EVENT_BASIC_TYPE = "dt.physical.event.property";

    private String physicalPropertyId;

    public PhysicalAssetPropertyWldtEvent(String propertyKey) throws EventBusException {
        super(propertyKey);
        this.physicalPropertyId = propertyKey;
    }

    public PhysicalAssetPropertyWldtEvent(String propertyKey, T body) throws EventBusException {
        super(propertyKey, body);
        this.physicalPropertyId = propertyKey;
    }

    public PhysicalAssetPropertyWldtEvent(String propertyKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(propertyKey, body, metadata);
        this.physicalPropertyId = propertyKey;
    }

    public String getPhysicalPropertyId() {
        return physicalPropertyId;
    }

    public void setPhysicalPropertyId(String physicalPropertyId) {
        this.physicalPropertyId = physicalPropertyId;
    }

    @Override
    protected String getBasicEventType() {
        return PHYSICAL_EVENT_BASIC_TYPE;
    }
}

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
 * An Action that has as target the Physical Adapter to trigger something on the physical asset
 */
public class PhysicalAssetActionWldtEvent<T> extends PhysicalAssetWldtEvent<T> {

    public static final String EVENT_BASIC_TYPE = "dt.physical.event.action";

    private final String actionKey;

    public PhysicalAssetActionWldtEvent(String actionKey) throws EventBusException {
        super(actionKey);
        this.actionKey = actionKey;
    }

    public PhysicalAssetActionWldtEvent(String actionKey, T body) throws EventBusException {
        super(actionKey, body);
        this.actionKey = actionKey;
    }

    public PhysicalAssetActionWldtEvent(String actionKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(actionKey, body, metadata);
        this.actionKey = actionKey;
    }

    public String getActionKey() {
        return actionKey;
    }

    @Override
    protected String getBasicEventType() {
        return EVENT_BASIC_TYPE;
    }
}

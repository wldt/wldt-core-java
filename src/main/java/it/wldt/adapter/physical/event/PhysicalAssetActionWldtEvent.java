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

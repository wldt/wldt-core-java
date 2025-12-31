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

import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Abstract WLDTEvent used to describe and characterize any physical occurrence detected on the physical asset by
 * a Physical Adapter
 */
public abstract class PhysicalAssetWldtEvent<T> extends WldtEvent<T> {
    public PhysicalAssetWldtEvent(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public PhysicalAssetWldtEvent(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public PhysicalAssetWldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        super(type, body, metadata);
        adaptEventType();
    }

    abstract protected String getBasicEventType();

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getBasicEventType(), this.getType()));
    }

    public static String buildEventType(String basicEventType, String eventType){
        if(eventType != null)
            return String.format("%s.%s", basicEventType, eventType);
        else
            return null;
    }


}

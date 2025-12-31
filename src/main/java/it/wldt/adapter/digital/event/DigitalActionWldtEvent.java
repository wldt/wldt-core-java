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
package it.wldt.adapter.digital.event;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * An Action targeting the DT received on the Digital Interface and that should reach the Shadowing Function
 * for its management
 */
public class DigitalActionWldtEvent<T> extends WldtEvent<T> {

    //public static final String EVENT_BASIC_TYPE = "dt.digital.event.action";

    private final String actionKey;

    public DigitalActionWldtEvent(String actionKey) throws EventBusException {
        super(actionKey);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public DigitalActionWldtEvent(String actionKey, T body) throws EventBusException {
        super(actionKey, body);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public DigitalActionWldtEvent(String actionKey, T body, Map<String, Object> metadata) throws EventBusException {
        super(actionKey, body, metadata);
        this.actionKey = actionKey;
        adaptEventType();
    }

    public String getActionKey() {
        return actionKey;
    }

    private void adaptEventType(){
        if(actionKey != null)
            this.setType(buildEventType(actionKey));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            //return String.format("%s.%s", EVENT_BASIC_TYPE, eventType);
            return String.format("%s.%s", WldtEventTypes.DIGITAL_ACTION_EVENT_BASE_TYPE, eventType);
        else
            return null;
    }

}

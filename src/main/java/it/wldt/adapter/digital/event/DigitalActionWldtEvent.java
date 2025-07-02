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

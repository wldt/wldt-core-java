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
import it.wldt.exception.EventBusException;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * A Digital Event that can be used by the Digital Adapter
 */
public class DigitalWldtEvent<T> extends WldtEvent<T> {

    public static final String DIGITAL_EVENT_BASIC_TYPE = "dt.digital.event";

    public DigitalWldtEvent(String type) throws EventBusException {
        super(type);
        adaptEventType();
    }

    public DigitalWldtEvent(String type, T body) throws EventBusException {
        super(type, body);
        adaptEventType();
    }

    public DigitalWldtEvent(String type, T body, Map<String, Object> metadata) throws EventBusException {
        super(type, body, metadata);
        adaptEventType();
    }

    private void adaptEventType(){
        if(this.getType() != null)
            this.setType(buildEventType(this.getType()));
    }

    public static String buildEventType(String eventType){
        if(eventType != null)
            return String.format("%s.%s", DIGITAL_EVENT_BASIC_TYPE, eventType);
        else
            return null;
    }

}

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
package it.wldt.core.state;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Notification associated to events of the Digital Twin State
 */
public class DigitalTwinStateEventNotification<T> {

    public static final String DIGITAL_TWIN_STATE_EVENT_BASIC_TYPE = "dt.digital.event.event";

    private String digitalEventKey;

    private T body;

    private Long timestamp;

    public DigitalTwinStateEventNotification(String digitalEventKey, T body, Long timestamp) {
        this.digitalEventKey = digitalEventKey;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getDigitalEventKey() {
        return digitalEventKey;
    }

    public void setDigitalEventKey(String digitalEventKey) {
        this.digitalEventKey = digitalEventKey;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateEventNotification{");
        sb.append("digitalEventKey='").append(digitalEventKey).append('\'');
        sb.append(", body=").append(body);
        sb.append('}');
        return sb.toString();
    }
}

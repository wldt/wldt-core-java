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

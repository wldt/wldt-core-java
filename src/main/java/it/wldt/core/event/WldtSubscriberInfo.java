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
package it.wldt.core.event;

import it.wldt.exception.EventBusException;

import java.util.Objects;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Description of a specific WLDT Subscriber with its ID and Event Listener instance used for notification and events
 * delivery
 */
public class WldtSubscriberInfo {

    private String id;
    private WldtEventListener wldtEventListener;

    private WldtSubscriberInfo(){

    }

    public WldtSubscriberInfo(String id, WldtEventListener wldtEventListener) throws EventBusException {

        if(id == null || wldtEventListener == null)
            throw new EventBusException("Error creating SubscriberInfo ! Id or Listener = null !");

        this.id = id;
        this.wldtEventListener = wldtEventListener;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WldtEventListener getEventListener() {
        return wldtEventListener;
    }

    public void setEventListener(WldtEventListener wldtEventListener) {
        this.wldtEventListener = wldtEventListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WldtSubscriberInfo that = (WldtSubscriberInfo) o;
        return id.equals(that.id) && wldtEventListener.equals(that.wldtEventListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, wldtEventListener);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SubscriberInfo{");
        sb.append("id='").append(id).append('\'');
        sb.append(", eventListener=").append(wldtEventListener);
        sb.append('}');
        return sb.toString();
    }
}

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
package it.wldt.core.engine;

public class LifeCycleStateVariation {

    private long timestamp;

    private LifeCycleState lifeCycleState = LifeCycleState.NONE;

    public LifeCycleStateVariation() {
    }

    public LifeCycleStateVariation(long timestamp, LifeCycleState lifeCycleState) {
        this.timestamp = timestamp;
        this.lifeCycleState = lifeCycleState;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LifeCycleState getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(LifeCycleState lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LifeCycleStateVariation{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", lifeCycleState=").append(lifeCycleState);
        sb.append('}');
        return sb.toString();
    }
}

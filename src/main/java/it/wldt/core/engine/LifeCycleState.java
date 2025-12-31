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

public enum LifeCycleState {

        NONE("dt_none"),
        CREATED("dt_created"),
        STARTED("dt_started"),
        BOUND("dt_bound"),
        UN_BOUND("dt_un_bound"),
        SYNCHRONIZED("dt_synchronized"),
        NOT_SYNCHRONIZED("dt_not_synchronized"),
        STOPPED("dt_stopped"),
        DESTROYED("dt_destroyed");

        private String value;

        private LifeCycleState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static LifeCycleState fromValue(String value) {
            for (LifeCycleState state : LifeCycleState.values()) {
                if (state.getValue().equals(value)) {
                    return state;
                }
            }
            return null;
        }
    }
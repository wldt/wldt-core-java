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
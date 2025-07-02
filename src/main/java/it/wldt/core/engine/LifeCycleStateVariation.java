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

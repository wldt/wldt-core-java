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
package it.wldt.storage.model.state;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.storage.model.StorageRecord;

import java.util.List;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Digital Twin State Record Class
 * Represents a Digital Twin State Record in the storage
 */
public class DigitalTwinStateRecord extends StorageRecord {

    // Current Digital Twin State
    private DigitalTwinState currentState;

    // List of Digital Twin State Changes
    private List<DigitalTwinStateChange> stateChangeList;

    /**
     * Default Constructor
     */
    public DigitalTwinStateRecord() {
    }

    /**
     * Constructor
     *
     * @param currentState Current Digital Twin State
     * @param stateChangeList List of Digital Twin State Changes
     */
    public DigitalTwinStateRecord(DigitalTwinState currentState, List<DigitalTwinStateChange> stateChangeList) {
        this.currentState = currentState;
        this.stateChangeList = stateChangeList;
    }

    /**
     * Get the List of Digital Twin State Changes
     * @return List of Digital Twin State Changes
     */
    public List<DigitalTwinStateChange> getStateChangeList() {
        return stateChangeList;
    }

    /**
     * Set the List of Digital Twin State Changes
     * @param stateChangeList List of Digital Twin State Changes
     */
    public void setStateChangeList(List<DigitalTwinStateChange> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

    /**
     * Get the Current Digital Twin State
     * @return Current Digital Twin State
     */
    public DigitalTwinState getCurrentState() {
        return currentState;
    }

    /**
     * Set the Current Digital Twin State
     * @param currentState Current Digital Twin State
     */
    public void setCurrentState(DigitalTwinState currentState) {
        this.currentState = currentState;
    }

    @Override
    public String toString() {
        return "DigitalTwinStateRecord{" +
                "recordId=" + super.getId() +
                "currentState=" + currentState +
                ", stateChangeList=" + stateChangeList +
                '}';
    }

}

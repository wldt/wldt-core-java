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

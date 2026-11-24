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

import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateValues;
import it.wldt.storage.model.StorageRecord;

import java.util.List;

/**
 * Digital Twin State Values Record: a stored snapshot of the (complete) values of the Digital Twin State, associated
 * to the id of the description they refer to, together with the changes that produced it.
 */
public class DigitalTwinStateValuesRecord extends StorageRecord {

    // The complete Digital Twin State Values
    private DigitalTwinStateValues values;

    // List of Digital Twin State Changes that altered the values
    private List<DigitalTwinStateChange> stateChangeList;

    /**
     * Default Constructor
     */
    public DigitalTwinStateValuesRecord() {
    }

    /**
     * Constructor
     *
     * @param values the complete Digital Twin State Values
     * @param stateChangeList the Digital Twin State Changes that altered the values
     */
    public DigitalTwinStateValuesRecord(DigitalTwinStateValues values, List<DigitalTwinStateChange> stateChangeList) {
        this.values = values;
        this.stateChangeList = stateChangeList;
    }

    /**
     * @return the id of the Digital Twin State Description these values are associated to (the one carried by the
     * values), null if the record has no values
     */
    public String getDescriptionId() {
        return values != null ? values.getDescriptionId() : null;
    }

    /**
     * @return the complete Digital Twin State Values
     */
    public DigitalTwinStateValues getValues() {
        return values;
    }

    /**
     * @param values the complete Digital Twin State Values
     */
    public void setValues(DigitalTwinStateValues values) {
        this.values = values;
    }

    /**
     * @return the Digital Twin State Changes that altered the values
     */
    public List<DigitalTwinStateChange> getStateChangeList() {
        return stateChangeList;
    }

    /**
     * @param stateChangeList the Digital Twin State Changes that altered the values
     */
    public void setStateChangeList(List<DigitalTwinStateChange> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

    @Override
    public String toString() {
        return "DigitalTwinStateValuesRecord{descriptionId='" + getDescriptionId() + "', values=" + values +
                ", stateChangeList=" + stateChangeList + '}';
    }
}

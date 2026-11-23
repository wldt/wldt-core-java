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
import it.wldt.core.state.DigitalTwinStateDescription;
import it.wldt.storage.model.StorageRecord;

import java.util.List;

/**
 * Digital Twin State Description Record: a stored description (structure) of the Digital Twin State, together with
 * the changes that produced it. Every {@link DigitalTwinStateValuesRecord} refers to a description record through
 * the description id.
 */
public class DigitalTwinStateDescriptionRecord extends StorageRecord {

    // Id of the description (it is the same stored in the values records associated to it)
    private String descriptionId;

    // The complete Digital Twin State Description
    private DigitalTwinStateDescription description;

    // List of Digital Twin State Changes that altered the structure
    private List<DigitalTwinStateChange> stateChangeList;

    /**
     * Default Constructor
     */
    public DigitalTwinStateDescriptionRecord() {
    }

    /**
     * Constructor
     *
     * @param description the complete Digital Twin State Description
     * @param stateChangeList the Digital Twin State Changes that altered the structure
     */
    public DigitalTwinStateDescriptionRecord(DigitalTwinStateDescription description, List<DigitalTwinStateChange> stateChangeList) {
        this.description = description;
        this.descriptionId = description != null ? description.getId() : null;
        this.stateChangeList = stateChangeList;
    }

    /**
     * @return the id of the description
     */
    public String getDescriptionId() {
        return descriptionId;
    }

    /**
     * @param descriptionId the id of the description
     */
    public void setDescriptionId(String descriptionId) {
        this.descriptionId = descriptionId;
    }

    /**
     * @return the complete Digital Twin State Description
     */
    public DigitalTwinStateDescription getDescription() {
        return description;
    }

    /**
     * @param description the complete Digital Twin State Description
     */
    public void setDescription(DigitalTwinStateDescription description) {
        this.description = description;
        this.descriptionId = description != null ? description.getId() : this.descriptionId;
    }

    /**
     * @return the Digital Twin State Changes that altered the structure
     */
    public List<DigitalTwinStateChange> getStateChangeList() {
        return stateChangeList;
    }

    /**
     * @param stateChangeList the Digital Twin State Changes that altered the structure
     */
    public void setStateChangeList(List<DigitalTwinStateChange> stateChangeList) {
        this.stateChangeList = stateChangeList;
    }

    @Override
    public String toString() {
        return "DigitalTwinStateDescriptionRecord{descriptionId='" + descriptionId + "', description=" + description +
                ", stateChangeList=" + stateChangeList + '}';
    }
}

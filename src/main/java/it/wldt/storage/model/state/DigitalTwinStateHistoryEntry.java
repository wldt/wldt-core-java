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

import java.util.ArrayList;
import java.util.List;

/**
 * An entry of a {@link DigitalTwinStateHistoryRecord}: a stored Digital Twin State Description together with the
 * values associated to it (by description id) that were selected by a query on the complete state, in the order
 * requested by the query.
 */
public class DigitalTwinStateHistoryEntry {

    // Id of the description
    private String descriptionId;

    // The Digital Twin State Description
    private DigitalTwinStateDescription description;

    // The Digital Twin State Changes that altered the structure
    private List<DigitalTwinStateChange> descriptionChangeList;

    // The values associated to the description selected by the query
    private List<DigitalTwinStateValuesRecord> valuesList = new ArrayList<>();

    /**
     * Default Constructor
     */
    public DigitalTwinStateHistoryEntry() {
    }

    /**
     * Constructor
     * @param descriptionId the id of the description
     * @param description the description (may be null if it is not stored)
     * @param descriptionChangeList the changes that altered the structure
     */
    public DigitalTwinStateHistoryEntry(String descriptionId, DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChangeList) {
        this.descriptionId = descriptionId;
        this.description = description;
        this.descriptionChangeList = descriptionChangeList;
    }

    /** @return the id of the description */
    public String getDescriptionId() { return descriptionId; }

    /** @param descriptionId the id of the description */
    public void setDescriptionId(String descriptionId) { this.descriptionId = descriptionId; }

    /** @return the Digital Twin State Description */
    public DigitalTwinStateDescription getDescription() { return description; }

    /** @param description the Digital Twin State Description */
    public void setDescription(DigitalTwinStateDescription description) { this.description = description; }

    /** @return the changes that altered the structure */
    public List<DigitalTwinStateChange> getDescriptionChangeList() { return descriptionChangeList; }

    /** @param descriptionChangeList the changes that altered the structure */
    public void setDescriptionChangeList(List<DigitalTwinStateChange> descriptionChangeList) { this.descriptionChangeList = descriptionChangeList; }

    /** @return the values associated to the description that were selected by the query (never null, may be empty) */
    public List<DigitalTwinStateValuesRecord> getValuesList() { return valuesList; }

    /** @param valuesList the values associated to the description */
    public void setValuesList(List<DigitalTwinStateValuesRecord> valuesList) { this.valuesList = valuesList != null ? valuesList : new ArrayList<DigitalTwinStateValuesRecord>(); }

    @Override
    public String toString() {
        return "DigitalTwinStateHistoryEntry{descriptionId='" + descriptionId + "', description=" + description +
                ", valuesList=" + valuesList + '}';
    }
}

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
package it.wldt.storage.query.inmemory;

import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateDescription;
import it.wldt.core.state.DigitalTwinStateValues;
import it.wldt.storage.model.StorageRecord;
import it.wldt.storage.model.state.DigitalTwinStateDescriptionRecord;
import it.wldt.storage.model.state.DigitalTwinStateValuesRecord;

import java.util.List;

/**
 * Internal row of the queries on the complete Digital Twin State ({@code QueryResourceType.DIGITAL_TWIN_STATE}):
 * the join of a stored {@link DigitalTwinStateValuesRecord} with the description it refers to (by description id).
 * <p>
 * A filter, a sort criterion or an aggregation must be evaluated on one values record at a time (for example
 * {@code values.propertyValues.temperature > 20}): with a history record, that holds a list of values for each
 * description, it would be ambiguous whether the condition must hold for one or for all of them. The evaluator
 * therefore works on these flat rows, so every field of the description and of the values can be used in a query
 * ({@code descriptionId}, {@code description.*}, {@code values.*}, {@code stateChangeList},
 * {@code descriptionChangeList}, ...). The values are null for a description that has no stored values.
 * <p>
 * It is never returned to the caller: a non aggregated query returns the rows grouped in a
 * {@link it.wldt.storage.model.state.DigitalTwinStateHistoryRecord}, an aggregated query returns aggregation records.
 */
class DigitalTwinStateSnapshotRecord extends StorageRecord {

    // Id of the description (and of the values associated to it)
    private String descriptionId;

    // The Digital Twin State Description
    private DigitalTwinStateDescription description;

    // The Digital Twin State Changes that altered the structure
    private List<DigitalTwinStateChange> descriptionChangeList;

    // The complete Digital Twin State Values associated to the description (may be null)
    private DigitalTwinStateValues values;

    // The Digital Twin State Changes that altered the values
    private List<DigitalTwinStateChange> stateChangeList;

    /**
     * Default Constructor
     */
    public DigitalTwinStateSnapshotRecord() {
    }

    /**
     * Constructor
     *
     * @param descriptionRecord the stored description (may be null if unknown)
     * @param valuesRecord the stored values (may be null)
     */
    public DigitalTwinStateSnapshotRecord(DigitalTwinStateDescriptionRecord descriptionRecord, DigitalTwinStateValuesRecord valuesRecord) {
        if (descriptionRecord != null) {
            this.description = descriptionRecord.getDescription();
            this.descriptionChangeList = descriptionRecord.getStateChangeList();
            this.descriptionId = descriptionRecord.getDescriptionId();
        }
        if (valuesRecord != null) {
            this.values = valuesRecord.getValues();
            this.stateChangeList = valuesRecord.getStateChangeList();
            if (this.descriptionId == null)
                this.descriptionId = valuesRecord.getDescriptionId();
        }
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

    /** @return the complete values associated to the description (may be null) */
    public DigitalTwinStateValues getValues() { return values; }

    /** @param values the complete values associated to the description */
    public void setValues(DigitalTwinStateValues values) { this.values = values; }

    /** @return the changes that altered the values */
    public List<DigitalTwinStateChange> getStateChangeList() { return stateChangeList; }

    /** @param stateChangeList the changes that altered the values */
    public void setStateChangeList(List<DigitalTwinStateChange> stateChangeList) { this.stateChangeList = stateChangeList; }

    @Override
    public String toString() {
        return "DigitalTwinStateSnapshotRecord{descriptionId='" + descriptionId + "', description=" + description +
                ", values=" + values + '}';
    }
}

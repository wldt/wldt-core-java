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

import it.wldt.core.state.DigitalTwinStateDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Digital Twin State History Record: the result of a (non aggregated) query on the complete Digital Twin State
 * ({@code QueryResourceType.DIGITAL_TWIN_STATE}). It is a single record, always the only item of the query result,
 * that maps every selected Digital Twin State Description to the list of the values associated to it (by description
 * id) that were stored and selected by the query, in the order requested by the query.
 * <p>
 * The map is keyed by the <b>id</b> of the description and not by the description itself, because two descriptions
 * with the same structure but different ids (for example the structure is restored after a property has been added
 * and removed) are different stored descriptions. The entries follow the order in which the descriptions appear in
 * the selected rows. A description without selected values is present with an empty values list only when it has
 * no stored values at all.
 */
public class DigitalTwinStateHistoryRecord extends it.wldt.storage.model.StorageRecord {

    // Entries by description id, in the order of the selected rows
    private final Map<String, DigitalTwinStateHistoryEntry> entries = new LinkedHashMap<>();

    /**
     * Default Constructor (empty history)
     */
    public DigitalTwinStateHistoryRecord() {
    }

    /**
     * Adds an entry to the history (replacing an entry with the same description id)
     * @param entry the entry to add
     */
    public void addEntry(DigitalTwinStateHistoryEntry entry) {
        entries.put(entry.getDescriptionId(), entry);
    }

    /**
     * @return the entries by description id, in the order of the query (unmodifiable view, never null)
     */
    public Map<String, DigitalTwinStateHistoryEntry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * @param descriptionId the id of a description
     * @return the entry of the description, or null if the description is not part of the history
     */
    public DigitalTwinStateHistoryEntry getEntry(String descriptionId) {
        return entries.get(descriptionId);
    }

    /**
     * @param descriptionId the id of a description
     * @return the values associated to the description (empty if the description is not part of the history)
     */
    public List<DigitalTwinStateValuesRecord> getValuesList(String descriptionId) {
        DigitalTwinStateHistoryEntry entry = entries.get(descriptionId);
        return entry != null ? entry.getValuesList() : new ArrayList<DigitalTwinStateValuesRecord>();
    }

    /**
     * @return the descriptions of the history, in the order of the query
     */
    public List<DigitalTwinStateDescription> getDescriptions() {
        List<DigitalTwinStateDescription> descriptions = new ArrayList<>();
        for (DigitalTwinStateHistoryEntry entry : entries.values())
            descriptions.add(entry.getDescription());
        return descriptions;
    }

    /**
     * @return the number of descriptions of the history
     */
    public int size() {
        return entries.size();
    }

    /**
     * @return true if no description is part of the history
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public String toString() {
        return "DigitalTwinStateHistoryRecord{entries=" + entries + '}';
    }
}

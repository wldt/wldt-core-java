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
package it.wldt.storage.query;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This Enum represents the Query Resource Type used to specify the type of resource to be queried
 * on the storage system (e.g., Physical Asset Property Variation, Physical Asset Event Notification, Physical Action Request)
 */
public enum QueryResourceType {

    PHYSICAL_ASSET_PROPERTY_VARIATION("storage_physical_property_variation"),
    PHYSICAL_ASSET_EVENT_NOTIFICATION("storage_physical_event_notification"),
    PHYSICAL_ACTION_REQUEST("storage_physical_action_request"),
    DIGITAL_ACTION_REQUEST("storage_digital_action_request"),
    DIGITAL_TWIN_STATE("storage_dt_state"),
    NEW_PAD_NOTIFICATION("storage_new_pad_notification"),
    UPDATED_PAD_NOTIFICATION("storage_updated_pad_notification"),
    PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION("storage_physical_relationship_instance_created_notification"),
    PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION("storage_physical_relationship_instance_deleted_notification"),
    LIFE_CYCLE_EVENT("storage_life_cycle_event"),
    STORAGE_STATS("storage_stats");

    private String value;

    private QueryResourceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

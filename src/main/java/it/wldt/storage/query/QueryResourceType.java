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

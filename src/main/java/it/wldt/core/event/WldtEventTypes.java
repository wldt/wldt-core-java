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
package it.wldt.core.event;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * This class contains all the event types used in the WLDT Framework
 */
public class WldtEventTypes {

    public static final String MULTI_LEVEL_WILDCARD_VALUE = "*";

    /* Physical Interface Events */

    public static final String PHYSICAL_ACTION_TRIGGER_EVENT_BASE_TYPE = "dt.physical.event.action";

    public static final String PHYSICAL_EVENT_NOTIFICATION_EVENT_BASE_TYPE = "dt.physical.event.event";

    public static final String PHYSICAL_PROPERTY_VARIATION_EVENT_BASE_TYPE = "dt.physical.event.property";

    public static final String ALL_PHYSICAL_ACTION_TRIGGER_EVENT_TYPE = String.format("%s.%s", PHYSICAL_ACTION_TRIGGER_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    public static final String ALL_PHYSICAL_EVENT_NOTIFICATION_EVENT_TYPE = String.format("%s.%s", PHYSICAL_EVENT_NOTIFICATION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    public static final String ALL_PHYSICAL_PROPERTY_VARIATION_EVENT_TYPE = String.format("%s.%s", PHYSICAL_PROPERTY_VARIATION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    public static final String PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_BASE_TYPE = "dt.physical.event.relationship.created";

    public static final String PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_BASE_TYPE = "dt.physical.event.relationship.deleted";

    public static final String ALL_PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_TYPE = String.format("%s.%s", PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    public static final String ALL_PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_TYPE = String.format("%s.%s", PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);;

    public static final String PHYSICAL_ASSET_DESCRIPTION_AVAILABLE = "dt.physical.event.pad.available";

    public static final String PHYSICAL_ASSET_DESCRIPTION_UPDATED = "dt.physical.event.pad.updated";

    public static final String PHYSICAL_ASSET_DESCRIPTION_EVENT_METADATA_ADAPTER_ID = "adapter_id";

    /* Digital Interface Events */

    public static final String DIGITAL_ACTION_EVENT_BASE_TYPE = "dt.digital.event.action";

    public static final String ALL_DIGITAL_ACTION_EVENT_TYPE = String.format("%s.%s", DIGITAL_ACTION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    /* State Events */
    public static final String DT_STATE_UPDATE_MESSAGE_EVENT_TYPE = "dt.state.update";

    public static final String DT_STATE_EVENT_NOTIFICATION_EVENT_BASE_TYPE = "dt.state.event.notification";

    public static final String ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE = String.format("%s.%s", DT_STATE_EVENT_NOTIFICATION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    /* Life Cycle Events */
    public static final String DT_LIFE_CYCLE_EVENT_TYPE = "dt.lifecycle";

    public static final String STORAGE_QUERY_REQUEST_EVENT_TYPE = "dt.storage.query.request";

    public static final String STORAGE_QUERY_RESULT_EVENT_TYPE = "dt.storage.query.result";

    public static final String ALL_STORAGE_QUERY_RESULT_EVENT_TYPE = String.format("%s.%s", STORAGE_QUERY_RESULT_EVENT_TYPE, MULTI_LEVEL_WILDCARD_VALUE);
}

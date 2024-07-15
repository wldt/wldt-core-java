package it.wldt.core.event;

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

    //TODO Currently Not Used (PAD generation and notification should be updated)
    public static final String PHYSICAL_ASSET_DESCRIPTION_CREATED = "dt.physical.event.pad.created";
    public static final String PHYSICAL_ASSET_DESCRIPTION_UPDATED = "dt.physical.event.pad.updated";

    /* Digital Interface Events */

    public static final String DIGITAL_ACTION_EVENT_BASE_TYPE = "dt.digital.event.action";

    public static final String ALL_DIGITAL_ACTION_EVENT_TYPE = String.format("%s.%s", DIGITAL_ACTION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    /* State Events */
    public static final String DT_STATE_UPDATE_MESSAGE_EVENT_TYPE = "dt.state.update";

    public static final String DT_STATE_EVENT_NOTIFICATION_EVENT_BASE_TYPE = "dt.state.event.notification";

    public static final String ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE = String.format("%s.%s", DT_STATE_EVENT_NOTIFICATION_EVENT_BASE_TYPE, MULTI_LEVEL_WILDCARD_VALUE);

    /* Life Cycle Events */
    //TODO Currently Not Used -> Life Cycle Event Generation Should be Integrated
    public static final String DT_LIFE_CYCLE_CREATED_EVENT_TYPE = "dt.lifecycle.created";
    public static final String DT_LIFE_CYCLE_STARTED_EVENT_TYPE = "dt.lifecycle.created";
    public static final String DT_LIFE_CYCLE_PHYSICAL_ADAPTER_BOUND_EVENT_TYPE = "dt.lifecycle.adapter.physical.bound";
    public static final String DT_LIFE_CYCLE_PHYSICAL_ADAPTER_BINDING_UPDATE_EVENT_TYPE = "dt.lifecycle.adapter.physical.bindingupdate";
    public static final String DT_LIFE_CYCLE_PHYSICAL_ADAPTER_UNBOUND_EVENT_TYPE = "dt.lifecycle.adapter.physical.unbound";
    public static final String DT_LIFE_CYCLE_DIGITAL_ADAPTER_BOUND_EVENT_TYPE = "dt.lifecycle.adapter.digital.bound";
    public static final String DT_LIFE_CYCLE_DIGITAL_ADAPTER_BINDING_UPDATE_EVENT_TYPE = "dt.lifecycle.adapter.digital.bindingupdate";
    public static final String DT_LIFE_CYCLE_DIGITAL_ADAPTER_UNBOUND_EVENT_TYPE = "dt.lifecycle.adapter.digital.unbound";
    public static final String DT_LIFE_CYCLE_BOUND_EVENT_TYPE = "dt.lifecycle.bound";
    public static final String DT_LIFE_CYCLE_UNBOUND_EVENT_TYPE = "dt.lifecycle.unbound";
    public static final String DT_LIFE_CYCLE_SYNC_EVENT_TYPE = "dt.lifecycle.sync";
    public static final String DT_LIFE_CYCLE_NOT_SYNC_EVENT_TYPE = "dt.lifecycle.notsync";
    public static final String DT_LIFE_CYCLE_STOPPED_EVENT_TYPE = "dt.lifecycle.stopped";
    public static final String DT_LIFE_CYCLE_DESTROYED_EVENT_TYPE = "dt.lifecycle.destroyed";

}

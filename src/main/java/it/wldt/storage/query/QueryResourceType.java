package it.wldt.storage.query;

public enum QueryResourceType {

    PHYSICAL_ASSET_PROPERTY_VARIATION("storage_physical_property_variation"),
    PHYSICAL_ASSET_EVENT_NOTIFICATION("storage_physical_event_notification"),
    PHYSICAL_ACTION_REQUEST("storage_physical_action_request"),
    DIGITAL_ACTION_REQUEST("storage_digital_action_request"),
    DIGITAL_TWIN_STATE("storage_dt_state"),
    DIGITAL_TWIN_STATE_CHANGE_LIST("storage_dt_state_change_list"),
    NEW_PAD_NOTIFICATION("storage_new_pad_notification"),
    UPDATED_PAD_NOTIFICATION("storage_updated_pad_notification"),
    PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION("storage_physical_relationship_instance_created_notification"),
    PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION("storage_physical_relationship_instance_deleted_notification"),
    LIFE_CYCLE_EVENT("storage_life_cycle_event");

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

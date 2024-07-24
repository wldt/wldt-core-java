package it.wldt.storage.query;

public enum QueryResourceType {

    PHYSICAL_EVENT("storage_physical_event"),
    PHYSICAL_ACTION_EVENT("storage_physical_action_event"),
    DIGITAL_ACTION_EVENT("storage_digital_event"),
    STATE_EVENT("storage_state_event"),
    PAD_EVENT("storage_pad_event"),
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

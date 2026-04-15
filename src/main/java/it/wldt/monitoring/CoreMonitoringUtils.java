package it.wldt.monitoring;

public class CoreMonitoringUtils {

    public static final String NAMESPACE_PREFIX = "dt";

    public static final String DT_COMPONENT_MODEL_KEY = "dt_model";

    public static final String DT_COMPONENT_STATE_KEY = "dt_state";

    // Physical Asset - Property Variation
    public static final String PA_PROPERTY_VARIATION_EXEC_TIME = "pa_property_variation_exec_time";

    public static final String PA_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT = "pa_property_variation_exec_success_count";

    public static final String PA_PROPERTY_VARIATION_EXEC_ERROR_COUNT = "pa_property_variation_exec_error_count";

    // Physical Asset - Property Variation
    public static final String PA_EVENT_NOTIFICATION_EXEC_TIME = "pa_event_notification_exec_time";

    public static final String PA_EVENT_NOTIFICATION_EXEC_SUCCESS_COUNT = "pa_event_notification_exec_success_count";

    public static final String PA_EVENT_NOTIFICATION_EXEC_ERROR_COUNT = "pa_event_notification_exec_error_count";

    // Physical Asset - Relationship Instance Created
    public static final String PA_RELATIONSHIP_INSTANCE_CREATED_EXEC_TIME = "pa_rel_instance_created_exec_time";

    public static final String PA_RELATIONSHIP_INSTANCE_CREATED_SUCCESS_COUNT = "pa_rel_instance_created_exec_success_count";

    public static final String PA_RELATIONSHIP_INSTANCE_CREATED_ERROR_COUNT = "pa_rel_instance_created_exec_error_count";

    // Physical Asset - Relationship Instance Deleted
    public static final String PA_RELATIONSHIP_INSTANCE_DELETED_EXEC_TIME = "pa_rel_instance_deleted_exec_time";

    public static final String PA_RELATIONSHIP_INSTANCE_DELETED_SUCCESS_COUNT = "pa_rel_instance_deleted_exec_success_count";

    public static final String PA_RELATIONSHIP_INSTANCE_DELETED_ERROR_COUNT = "pa_rel_instance_deleted_exec_error_count";

    // Digital Action Event
    public static final String DIGITAL_ACTION_REQUEST_EXEC_TIME = "digital_action_exec_time";

    public static final String DIGITAL_ACTION_REQUEST_SUCCESS_COUNT = "digital_action_exec_success_count";

    public static final String DIGITAL_ACTION_REQUEST_ERROR_COUNT = "digital_action_exec_error_count";

    // Digital Twin State Computation

    public static final String DT_STATE_COMPUTATION_EXEC_TIME = "dt_state_computation_exec_time";

    public static final String DT_STATE_COMPUTATION_SUCCESS_COUNT = "dt_state_computation_exec_success_count";

    public static final String DT_STATE_COMPUTATION_ERROR_COUNT = "dt_state_computation_exec_error_count";

    public static String buildNamespace(String digitalTwinId, String dtModule){
        // Lowercase both DT id and dt module
        return String.format("%s.%s.%s", NAMESPACE_PREFIX, digitalTwinId.toLowerCase(), dtModule.toLowerCase().replace(" ", "_"));
    }

}

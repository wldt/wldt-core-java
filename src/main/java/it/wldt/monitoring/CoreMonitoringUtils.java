package it.wldt.monitoring;

public class CoreMonitoringUtils {

    //public static final String NAMESPACE_PREFIX = "dt";
    //public static final String DT_COMPONENT_MODEL_KEY = "dt_model";
    //public static final String DT_COMPONENT_STATE_KEY = "dt_state";
    //public static final String DT_COMPONENT_PHYSICAL_ADAPTER_KEY = "physical_adapter";

    // Physical Asset - Property Variation
    public static final String PT_PROPERTY_VARIATION_EXEC_TIME = "pt_property_variation_exec_time";

    public static final String PT_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT = "pt_property_variation_exec_success_count";

    public static final String PT_PROPERTY_VARIATION_EXEC_ERROR_COUNT = "pt_property_variation_exec_error_count";

    // Physical Asset - Property Variation
    public static final String PT_EVENT_NOTIFICATION_EXEC_TIME = "pt_event_notification_exec_time";

    public static final String PT_EVENT_NOTIFICATION_EXEC_SUCCESS_COUNT = "pt_event_notification_exec_success_count";

    public static final String PT_EVENT_NOTIFICATION_EXEC_ERROR_COUNT = "pt_event_notification_exec_error_count";

    // Physical Asset - Relationship Instance Created
    public static final String PT_RELATIONSHIP_INSTANCE_CREATED_EXEC_TIME = "pt_rel_instance_created_exec_time";

    public static final String PT_RELATIONSHIP_INSTANCE_CREATED_SUCCESS_COUNT = "pt_rel_instance_created_exec_success_count";

    public static final String PT_RELATIONSHIP_INSTANCE_CREATED_ERROR_COUNT = "pt_rel_instance_created_exec_error_count";

    // Physical Asset - Relationship Instance Deleted
    public static final String PT_RELATIONSHIP_INSTANCE_DELETED_EXEC_TIME = "pt_rel_instance_deleted_exec_time";

    public static final String PT_RELATIONSHIP_INSTANCE_DELETED_SUCCESS_COUNT = "pt_rel_instance_deleted_exec_success_count";

    public static final String PT_RELATIONSHIP_INSTANCE_DELETED_ERROR_COUNT = "pt_rel_instance_deleted_exec_error_count";

    // Digital Action Event
    public static final String DIGITAL_ACTION_REQUEST_EXEC_TIME = "digital_action_exec_time";

    public static final String DIGITAL_ACTION_REQUEST_SUCCESS_COUNT = "digital_action_exec_success_count";

    public static final String DIGITAL_ACTION_REQUEST_ERROR_COUNT = "digital_action_exec_error_count";

    // Digital Twin State Computation
    public static final String DT_STATE_COMPUTATION_EXEC_TIME = "dt_state_computation_exec_time";

    public static final String DT_STATE_COMPUTATION_SUCCESS_COUNT = "dt_state_computation_exec_success_count";

    public static final String DT_STATE_COMPUTATION_ERROR_COUNT = "dt_state_computation_exec_error_count";

    // Physical Adapter
    public static final String PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_SUCCESS_COUNT = "pa_property_event_pub_success_count";

    public static final String PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_ERROR_COUNT = "pa_property_event_pub_error_count";

    public static final String PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_SUCCESS_COUNT = "pa_property_event_notification_pub_success_count";

    public static final String PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_ERROR_COUNT = "pa_property_event_notification_pub_error_count";

    public static final String PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_SUCCESS_COUNT = "pa_property_rel_created_event_pub_success_count";

    public static final String PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_ERROR_COUNT = "pa_property_rel_created_event_pub_error_count";

    public static final String PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_SUCCESS_COUNT = "pa_property_rel_deleted_event_pub_success_count";

    public static final String PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_ERROR_COUNT = "pa_property_rel_deleted_event_pub_error_count";

    public static final String PHYSICAL_ADAPTER_ACTION_COMPUTATION_EXEC_TIME = "pa_action_computation_exec_time";

    public static final String PHYSICAL_ADAPTER_ACTION_COMPUTATION_SUCCESS_COUNT = "pa_action_computation_exec_success_count";

    public static final String PHYSICAL_ADAPTER_ACTION_COMPUTATION_ERROR_COUNT = "pa_action_computation_exec_error_count";

    // Digital Adapter

    public static final String DIGITAL_ADAPTER_ACTION_EVENT_PUB_SUCCESS_COUNT = "da_action_event_pub_success_count";

    public static final String DIGITAL_ADAPTER_ACTION_EVENT_PUB_ERROR_COUNT = "da_action_event_pub_error_count";

    public static final String DIGITAL_ADAPTER_STATE_UPDATE_EXEC_TIME = "da_state_update_processing_exec_time";

    public static final String DIGITAL_ADAPTER_STATE_UPDATE_SUCCESS_COUNT = "da_state_update_processing_exec_success_count";

    public static final String DIGITAL_ADAPTER_STATE_UPDATE_ERROR_COUNT = "da_state_update_processing_exec_error_count";

    public static final String DIGITAL_EVENT_NOTIFICATION_EXEC_TIME = "da_event_notification_processing_exec_time";

    public static final String DIGITAL_EVENT_NOTIFICATION_SUCCESS_COUNT = "da_event_notification_processing_exec_success_count";

    public static final String DIGITAL_EVENT_NOTIFICATION_ERROR_COUNT = "da_event_notification_processing_exec_error_count";

    // Life Cycle

    public static final String LIFE_CYCLE_VALUE = "dt_lifecycle_value";

    //public static final String LIFE_CYCLE_VALUE_PUB_SUCCESS_COUNT = "dt_lifecycle_value_pub_success_count";

    //public static final String LIFE_CYCLE_VALUE_PUB_ERROR_COUNT = "dt_lifecycle_value_pub_error_count";

    public static String buildCoreNamespace(){
        // Lowercase both DT id and dt module
        return "core";
    }

}

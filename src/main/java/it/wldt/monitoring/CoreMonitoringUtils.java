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

    // Augmentation Function Manager

    public static final String AUGMENTATION_FUNCTION_HANDLER_COUNT = "af_handler_count";

    public static final String AUGMENTATION_FUNCTION_STATEFUL_RUNNING_COUNT = "af_stateful_running_count";

    // Augmentation Function

    public static final String AF_RESULT_SUCCESS_COUNT = "af_result_success_count";

    public static final String AF_RESULT_ERROR_COUNT = "af_result_error_count";

    public static final String AF_RESULT_EXEC_TIME = "af_result_exec_time";

    public static final String AF_ERROR_SUCCESS_COUNT = "af_error_success_count";

    public static final String AF_ERROR_ERROR_COUNT = "af_error_error_count";

    public static final String AF_ERROR_EXEC_TIME = "af_error_exec_time";

    public static final String AF_REGISTERED_SUCCESS_COUNT = "af_registered_success_count";

    public static final String AF_REGISTERED_ERROR_COUNT = "af_registered_error_count";

    public static final String AF_REGISTERED_EXEC_TIME = "af_registered_exec_time";

    public static final String AF_UNREGISTERED_SUCCESS_COUNT = "af_unregistered_success_count";

    public static final String AF_UNREGISTERED_ERROR_COUNT = "af_unregistered_error_count";

    public static final String AF_UNREGISTERED_EXEC_TIME = "af_unregistered_exec_time";

    // Augmentation Function - DT Model Level - Stateless Execute

    public static final String AF_STATELESS_EXEC_SUCCESS_COUNT = "af_stateless_exec_success_count";

    public static final String AF_STATELESS_EXEC_ERROR_COUNT = "af_stateless_exec_error_count";

    public static final String AF_LIST_REGISTERED_SUCCESS_COUNT = "af_list_registered_success_count";

    public static final String AF_LIST_REGISTERED_ERROR_COUNT = "af_list_registered_error_count";

    public static final String AF_LIST_REGISTERED_EXEC_TIME = "af_list_registered_exec_time";

    // Augmentation Function - DT Model Level - Stateful Start

    public static final String AF_STATEFUL_START_SUCCESS_COUNT = "af_stateful_start_success_count";

    public static final String AF_STATEFUL_START_ERROR_COUNT = "af_stateful_start_error_count";

    // Augmentation Function - DT Model Level - Stateful Stop

    public static final String AF_STATEFUL_STOP_SUCCESS_COUNT = "af_stateful_stop_success_count";

    public static final String AF_STATEFUL_STOP_ERROR_COUNT = "af_stateful_stop_error_count";

    // Augmentation Function - Function Level - Stateless Execution
    public static final String AF_FUNCTION_STATELESS_EXEC_TIME = "af_function_stateless_exec_time";

    public static final String AF_FUNCTION_STATELESS_EXEC_SUCCESS_COUNT = "af_function_stateless_exec_success_count";

    public static final String AF_FUNCTION_STATELESS_EXEC_ERROR_COUNT = "af_function_stateless_exec_error_count";

    // Augmentation Function - Handler Level - Stateful Running Count

    public static final String AF_HANDLER_STATEFUL_RUNNING_COUNT = "af_handler_stateful_running_count";

    // Augmentation Function - Handler Level - Stateful Start
    public static final String AF_FUNCTION_STATEFUL_START_SUCCESS_COUNT = "af_function_stateful_start_success_count";

    public static final String AF_FUNCTION_STATEFUL_START_ERROR_COUNT = "af_function_stateful_start_error_count";

    public static final String AF_FUNCTION_STATEFUL_START_EXEC_TIME = "af_function_stateful_start_exec_time";

    // Augmentation Function - Handler Level - Stateful Stop
    public static final String AF_FUNCTION_STATEFUL_STOP_SUCCESS_COUNT = "af_function_stateful_stop_success_count";

    public static final String AF_FUNCTION_STATEFUL_STOP_ERROR_COUNT = "af_function_stateful_stop_error_count";

    public static final String AF_FUNCTION_STATEFUL_STOP_EXEC_TIME = "af_function_stateful_stop_exec_time";

    // Augmentation Function - Handler Level - Query Execution
    public static final String AF_FUNCTION_QUERY_EXEC_TIME = "af_function_query_exec_time";

    public static final String AF_FUNCTION_QUERY_EXEC_SUCCESS_COUNT = "af_function_query_exec_success_count";

    public static final String AF_FUNCTION_QUERY_EXEC_ERROR_COUNT = "af_function_query_exec_error_count";

    // Augmentation Function - Handler Level - Registered Function Counts

    public static final String AF_HANDLER_REGISTERED_STATELESS_COUNT = "af_handler_registered_stateless_count";

    public static final String AF_HANDLER_REGISTERED_STATEFUL_COUNT = "af_handler_registered_stateful_count";

    // Augmentation Function - Handler Level - State Update Dispatch
    public static final String AF_FUNCTION_STATE_UPDATE_EXEC_TIME = "af_function_state_update_exec_time";

    public static final String AF_FUNCTION_STATE_UPDATE_SUCCESS_COUNT = "af_function_state_update_success_count";

    public static final String AF_FUNCTION_STATE_UPDATE_ERROR_COUNT = "af_function_state_update_error_count";

    //Storage

    public static final String STORAGE_QUERY_SUCCESS_COUNT = "af_storage_query_success_count";

    public static final String STORAGE_QUERY_ERROR_COUNT = "af_storage_query_error_count";

    public static final String STORAGE_QUERY_EXEC_TIME = "af_storage_query_exec_time";

    public static final String STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_SUCCESS_COUNT = "af_storage_write_pa_description_success_count";

    public static final String STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_ERROR_COUNT = "af_storage_write_pa_description_error_count";

    public static final String STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME = "af_storage_write_pa_description_exec_time";

    public static final String STORAGE_WRITE_DIGITAL_TWIN_STATE_SUCCESS_COUNT = "af_storage_write_dt_state_success_count";

    public static final String STORAGE_WRITE_DIGITAL_TWIN_STATE_ERROR_COUNT = "af_storage_write_dt_state_error_count";

    public static final String STORAGE_WRITE_DIGITAL_TWIN_STATE_EXEC_TIME = "af_storage_write_dt_state_exec_time";

    public static final String STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT = "af_storage_write_af_success_count";

    public static final String STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT = "af_storage_write_af_error_count";

    public static final String STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME = "af_storage_write_af_exec_time";

    public static final String STORAGE_WRITE_DIGITAL_EVENT_SUCCESS_COUNT = "af_storage_write_de_success_count";

    public static final String STORAGE_WRITE_DIGITAL_EVENT_ERROR_COUNT = "af_storage_write_de_error_count";

    public static final String STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME = "af_storage_write_de_exec_time";

    public static final String STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT = "af_storage_write_pe_success_count";

    public static final String STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT = "af_storage_write_pe_error_count";

    public static final String STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME = "af_storage_write_pe_exec_time";

    public static final String STORAGE_WRITE_LIFECYCLE_EVENT_SUCCESS_COUNT = "af_storage_write_lifecycle_event_success_count";

    public static final String STORAGE_WRITE_LIFECYCLE_EVENT_ERROR_COUNT = "af_storage_write_lifecycle_event_error_count";

    public static final String STORAGE_WRITE_LIFECYCLE_EVENT_EXEC_TIME = "af_storage_write_lifecycle_event_exec_time";

    public static String buildCoreNamespace(){
        // Lowercase both DT id and dt module
        return "core";
    }

}

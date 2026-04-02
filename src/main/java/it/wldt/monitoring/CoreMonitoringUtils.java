package it.wldt.monitoring;

public class CoreMonitoringUtils {

    public static final String NAMESPACE_PREFIX = "dt";

    public static final String DT_COMPONENT_MODEL_KEY = "dt_model";

    public static final String PA_PROPERTY_VARIATION_EXEC_TIME = "pa_property_variation_exec_time";

    public static final String PA_PROPERTY_VARIATION_EXEC_SUCCESS_COUNT = "pa_property_variation_exec_success_count";

    public static final String PA_PROPERTY_VARIATION_EXEC_ERROR_COUNT = "pa_property_variation_exec_error_count";

    public static String buildNamespace(String digitalTwinId, String dtModule){
        // Lowercase both DT id and dt module
        return String.format("%s.%s.%s", NAMESPACE_PREFIX, digitalTwinId.toLowerCase(), dtModule.toLowerCase().replace(" ", "_"));
    }

}

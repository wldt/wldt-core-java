package it.wldt.monitoring.prometheus;

import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryFieldNames;
import it.wldt.storage.query.filter.FilterFactory;
import it.wldt.storage.query.SortCriterion;
import it.wldt.storage.query.aggregation.AggregationFactory;
import it.wldt.storage.model.AggregationRecord;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;

/**
 * Minimal stateful augmentation function used only by the Prometheus monitoring example.
 * Sets a contextRequest with a Query so the handler executes a storage query on
 * START and STOP, populating the af_function_query_exec_* Prometheus metrics.
 */
public class PrometheusTestStatefulAugmentationFunction extends StatefulAugmentationFunction {

    public static final String FUNCTION_ID = "prometheus-test-stateful-function";

    public PrometheusTestStatefulAugmentationFunction() {
        super(FUNCTION_ID,
                "Prometheus Test Stateful Function",
                "Triggers query exec metrics on start/stop for Prometheus monitoring example.",
                "1.0.0");

        Query queryRequest = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC).limit(1).build();

        setContextRequest(new AugmentationFunctionContextRequest(queryRequest));
    }

    @Override
    protected void start(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        // no-op — query metrics are fired by the handler on START
    }

    @Override
    protected void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        // no-op — query metrics are fired by the handler on STOP
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        // no-op
    }

    @Override
    public void onQueryResultRefresh(Query queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException {
        // no-op
    }

    @Override
    public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> notification) throws AugmentationFunctionException {
        // no-op
    }
}

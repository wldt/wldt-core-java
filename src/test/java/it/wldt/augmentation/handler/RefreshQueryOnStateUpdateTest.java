package it.wldt.augmentation.handler;

import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResourceType;
import it.wldt.storage.query.QueryResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The query refresh on every state update is opt-in: disabled by default.
 */
public class RefreshQueryOnStateUpdateTest {

    private final List<String> events = new ArrayList<>();

    private class RecordingHandler extends DefaultAugmentationFunctionHandler {

        RecordingHandler() {
            super("test-handler");
        }

        @Override
        public void onStatefulAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, Query query) {
            events.add("refresh:" + augmentationFunctionId);
        }

        void notifyStateUpdate(StatefulAugmentationFunction function) {
            onStateUpdate(new ArrayList<>(Collections.singletonList(function)), new DigitalTwinState(), null, null);
        }
    }

    private StatefulAugmentationFunction function(AugmentationFunctionContextRequest contextRequest) {
        return new StatefulAugmentationFunction("fn", "fn", "test function", "1.0.0", contextRequest) {
            @Override
            protected void start(AugmentationFunctionRequest request) {}

            @Override
            protected void stop(AugmentationFunctionRequest request) {}

            @Override
            public void onStateUpdate(DigitalTwinState digitalTwinState) {
                events.add("state");
            }

            @Override
            public void onQueryResultRefresh(Query query, QueryResult<?> queryResult) {}

            @Override
            public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> notification) {}
        };
    }

    private static Query query() {
        return Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build();
    }

    @Test
    public void disabledByDefault() {
        AugmentationFunctionContextRequest request = new AugmentationFunctionContextRequest(query());
        assertFalse(request.isRefreshQueryOnStateUpdate());

        new RecordingHandler().notifyStateUpdate(function(request));

        assertEquals(Collections.singletonList("state"), events);
    }

    @Test
    public void enabledRefreshesBeforeNotifyingTheState() {
        AugmentationFunctionContextRequest request = new AugmentationFunctionContextRequest(query());
        request.setRefreshQueryOnStateUpdate(true);

        RecordingHandler handler = new RecordingHandler();
        handler.notifyStateUpdate(function(request));
        handler.notifyStateUpdate(function(request));

        assertEquals(java.util.Arrays.asList("refresh:fn", "state", "refresh:fn", "state"), events);
    }

    @Test
    public void enabledWithoutQueryDoesNothingExtra() {
        AugmentationFunctionContextRequest request = new AugmentationFunctionContextRequest();
        request.setRefreshQueryOnStateUpdate(true);

        new RecordingHandler().notifyStateUpdate(function(request));

        assertEquals(Collections.singletonList("state"), events);
    }
}

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
package it.wldt.augmentation.handler;

import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.state.*;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static it.wldt.core.state.DigitalTwinStateChange.Operation.*;
import static it.wldt.core.state.DigitalTwinStateChange.ResourceType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the combination of the trigger filter ({@link DigitalTwinStateChangeFilter}) and of the content filter
 * ({@link DigitalTwinStateFilter}) in the context request of stateful augmentation functions.
 */
public class CombinedStateFilterTest {

    private final List<DigitalTwinState> receivedStates = new ArrayList<>();

    @BeforeEach
    public void reset() {
        receivedStates.clear();
    }

    private class TestHandler extends DefaultAugmentationFunctionHandler {
        TestHandler() {
            super("test-handler");
        }

        void state(StatefulAugmentationFunction f, DigitalTwinState state, ArrayList<DigitalTwinStateChange> changes) {
            onStateUpdate(new ArrayList<>(Collections.singletonList(f)), state, null, changes);
        }
    }

    private StatefulAugmentationFunction function(AugmentationFunctionContextRequest request) {
        return new StatefulAugmentationFunction("fn", "fn", "test", "1.0.0", request) {
            @Override protected void start(AugmentationFunctionRequest r) {}
            @Override protected void stop(AugmentationFunctionRequest r) {}
            @Override public void onStateUpdate(DigitalTwinState s) { receivedStates.add(s); }
            @Override public void onQueryResultRefresh(Query q, QueryResult<?> r) {}
            @Override public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> n) {}
        };
    }

    private static DigitalTwinState fullState() throws Exception {
        Map<String, DigitalTwinStateProperty<?>> props = new HashMap<>();
        props.put("temp", new DigitalTwinStateProperty<>("temp", 1));
        props.put("hum", new DigitalTwinStateProperty<>("hum", 2));
        props.put("press", new DigitalTwinStateProperty<>("press", 3));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        return new DigitalTwinState(props, new HashMap<>(), events, new HashMap<>());
    }

    private static DigitalTwinStateChange valueChange(String property) throws Exception {
        return new DigitalTwinStateChange(OPERATION_UPDATE_VALUE, PROPERTY_VALUE, new DigitalTwinStateProperty<>(property, 1));
    }

    private static DigitalTwinStateChange propertyAdded(String property) throws Exception {
        return new DigitalTwinStateChange(OPERATION_ADD, PROPERTY, new DigitalTwinStateProperty<>(property, 1));
    }

    private static ArrayList<DigitalTwinStateChange> changes(DigitalTwinStateChange... c) {
        return new ArrayList<>(Arrays.asList(c));
    }

    private static DigitalTwinStateChangeFilter valueUpdates() {
        return DigitalTwinStateChangeFilter.builder()
                .operations(OPERATION_UPDATE_VALUE)
                .resourceTypes(PROPERTY_VALUE)
                .build();
    }

    @Test
    public void triggerFilterAloneKeepsCompleteState() throws Exception {
        TestHandler handler = new TestHandler();
        DigitalTwinState state = fullState();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest().withStateChangeFilters(valueUpdates()));

        handler.state(f, state, changes(propertyAdded("temp")));
        assertTrue(receivedStates.isEmpty());

        handler.state(f, state, changes(valueChange("press")));
        assertEquals(1, receivedStates.size());
        assertSame(state, receivedStates.get(0));
    }

    @Test
    public void contentFilterAloneTriggersOnlyOnAcceptedResources() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest().withPropertyNameFilter("temp", "hum"));

        handler.state(f, fullState(), changes(valueChange("press")));
        assertTrue(receivedStates.isEmpty());

        handler.state(f, fullState(), changes(valueChange("hum")));
        assertEquals(1, receivedStates.size());
        assertEquals(2, receivedStates.get(0).getPropertyList().get().size());
        assertFalse(receivedStates.get(0).containsProperty("press"));
    }

    @Test
    public void bothFiltersRequireTheSameChangeToMatch() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest()
                .withPropertyNameFilter("temp", "hum")
                .withStateChangeFilters(valueUpdates()));

        // Value update on one of the filtered properties: triggers (each property alone is enough)
        handler.state(f, fullState(), changes(valueChange("temp")));
        handler.state(f, fullState(), changes(valueChange("hum")));
        assertEquals(2, receivedStates.size());

        receivedStates.clear();

        // Value update on a non accepted property: rule matches but the content filter does not
        handler.state(f, fullState(), changes(valueChange("press")));
        // Accepted resource but not a value update: content filter matches but the rule does not
        handler.state(f, fullState(), changes(propertyAdded("temp")));
        // Two different changes, each satisfying only one of the filters: no trigger
        handler.state(f, fullState(), changes(valueChange("press"), propertyAdded("temp")));
        assertTrue(receivedStates.isEmpty());
    }

    @Test
    public void missingChangeListDependsOnTheTriggerFilter() throws Exception {
        TestHandler handler = new TestHandler();

        handler.state(function(new AugmentationFunctionContextRequest().withPropertyNameFilter("temp")), fullState(), null);
        assertEquals(1, receivedStates.size());

        receivedStates.clear();
        handler.state(function(new AugmentationFunctionContextRequest().withStateChangeFilters(valueUpdates())), fullState(), null);
        assertTrue(receivedStates.isEmpty());
    }

    @Test
    public void unsetCategoriesAcceptEverything() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest().withEventNameFilter("alarm"));

        // Properties are not filtered, so the property change triggers and the whole state is delivered
        handler.state(f, fullState(), changes(valueChange("press")));
        assertEquals(1, receivedStates.size());
        assertEquals(3, receivedStates.get(0).getPropertyList().get().size());
    }

    @Test
    public void filteredStateKeepsEvaluationInstantAndValuesViews() throws Exception {
        DigitalTwinState state = fullState();
        DigitalTwinState filtered = state.filter(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        assertEquals(state.getEvaluationInstant(), filtered.getEvaluationInstant());
        assertEquals(Collections.singleton("temp"), filtered.getValues().getPropertyValues().keySet());
        assertEquals(1, filtered.getDescription().getProperties().size());
        assertSame(state, state.filter(null));
        assertSame(state, state.filter(DigitalTwinStateFilter.all()));
    }
}

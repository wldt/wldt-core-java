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
import it.wldt.augmentation.context.DigitalTwinStateFilterUtils;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.state.*;
import java.time.Instant;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static it.wldt.core.state.DigitalTwinStateChange.Operation.*;
import static it.wldt.core.state.DigitalTwinStateChange.ResourceType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the trigger filter and the content filter applied to the state delivered to augmentation functions.
 */
public class StateFilterTest {

    private final List<DigitalTwinStateValues> receivedStates = new ArrayList<>();
    private final List<DigitalTwinStateDescription> receivedDescriptions = new ArrayList<>();
    private final List<String> receivedNotifications = new ArrayList<>();

    @BeforeEach
    public void reset() {
        receivedStates.clear();
        receivedDescriptions.clear();
        receivedNotifications.clear();
    }

    private class TestHandler extends DefaultAugmentationFunctionHandler {
        TestHandler() {
            super("test-handler");
        }

        void state(StatefulAugmentationFunction f, DigitalTwinStateValues values, ArrayList<DigitalTwinStateChange> changes) {
            notifyStateValuesUpdate(new ArrayList<>(Collections.singletonList(f)), values, changes);
        }

        void description(StatefulAugmentationFunction f, DigitalTwinStateDescription description, ArrayList<DigitalTwinStateChange> changes) {
            notifyStateDescriptionUpdate(new ArrayList<>(Collections.singletonList(f)), description, changes);
        }

        void notification(StatefulAugmentationFunction f, String key) {
            onEventNotificationReceived(new ArrayList<>(Collections.singletonList(f)),
                    new DigitalTwinStateEventNotification<>(key, "body", 0L));
        }
    }

    private StatefulAugmentationFunction function(AugmentationFunctionContextRequest request) {
        return new StatefulAugmentationFunction("fn", "fn", "test", "1.0.0", request) {
            @Override protected void start(AugmentationFunctionRequest r) {}
            @Override protected void stop(AugmentationFunctionRequest r) {}
            @Override public void onStateDescriptionUpdate(DigitalTwinStateDescription d) { receivedDescriptions.add(d); }
            @Override public void onStateValuesUpdate(DigitalTwinStateValues v) { receivedStates.add(v); }
            @Override public void onQueryResultRefresh(Query q, QueryResult<?> r) {}
            @Override public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> n) {
                receivedNotifications.add(n.getDigitalEventKey());
            }
        };
    }

    private static DigitalTwinState fullState() throws Exception {
        Map<String, DigitalTwinStateProperty<?>> props = new HashMap<>();
        props.put("temp", new DigitalTwinStateProperty<>("temp", 1));
        props.put("hum", new DigitalTwinStateProperty<>("hum", 2));
        Map<String, DigitalTwinStateAction> actions = new HashMap<>();
        actions.put("on", new DigitalTwinStateAction("on", "t", "c"));
        actions.put("off", new DigitalTwinStateAction("off", "t", "c"));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        events.put("info", new DigitalTwinStateEvent("info", "t", "string"));
        Map<String, DigitalTwinStateRelationship<?>> rels = new HashMap<>();
        rels.put("near", new DigitalTwinStateRelationship<>("near", "t"));
        rels.put("inside", new DigitalTwinStateRelationship<>("inside", "t"));
        DigitalTwinState state = new DigitalTwinState(props, actions, events, rels);
        DigitalTwinStateTestFactory.withDescriptionId(state, "1_0");
        DigitalTwinStateTestFactory.withLastActionInvocation(state, "on", Instant.now());
        DigitalTwinStateTestFactory.withLastEventNotification(state, "alarm", Instant.now());
        DigitalTwinStateTestFactory.withLastEventNotification(state, "info", Instant.now());
        return state;
    }

    private static DigitalTwinStateChange valueChange(String property) throws Exception {
        return new DigitalTwinStateChange(OPERATION_UPDATE_VALUE, PROPERTY_VALUE, new DigitalTwinStateProperty<>(property, 1));
    }

    private static ArrayList<DigitalTwinStateChange> changes(DigitalTwinStateChange... c) {
        return new ArrayList<>(Arrays.asList(c));
    }

    @Test
    public void noFilterSendsEveryUpdateWithFullState() throws Exception {
        DigitalTwinStateValues values = fullState().getValues();
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest());

        handler.state(f, values, null);
        handler.state(f, values, changes(valueChange("temp")));

        assertEquals(2, receivedStates.size());
        assertSame(values, receivedStates.get(0));
        assertSame(values, receivedStates.get(1));
    }

    @Test
    public void triggerFilterFiresOnlyOnMatchingChange() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest()
                .withStateChangeFilters(DigitalTwinStateChangeFilter.onPropertyValueUpdate("temp")));

        handler.state(f, fullState().getValues(), changes(valueChange("hum")));
        handler.state(f, fullState().getValues(), changes(new DigitalTwinStateChange(OPERATION_ADD, PROPERTY, new DigitalTwinStateProperty<>("temp", 1))));
        handler.state(f, fullState().getValues(), null);
        assertEquals(0, receivedStates.size());

        handler.state(f, fullState().getValues(), changes(valueChange("hum"), valueChange("temp")));
        assertEquals(1, receivedStates.size());
    }

    @Test
    public void multipleTriggerRulesAreOred() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest()
                .withStateChangeFilters(DigitalTwinStateChangeFilter.onPropertyValueUpdate("temp"),
                        DigitalTwinStateChangeFilter.builder().operations(OPERATION_REMOVE).resourceTypes(RELATIONSHIP_INSTANCE).build()));

        // The removal of a relationship instance is a values change (the structure does not change)
        handler.state(f, fullState().getValues(), changes(new DigitalTwinStateChange(OPERATION_REMOVE, RELATIONSHIP_INSTANCE, new DigitalTwinStateRelationshipInstance<>("near", "t", "k1"))));
        handler.state(f, fullState().getValues(), changes(valueChange("temp")));
        handler.state(f, fullState().getValues(), changes(valueChange("hum")));

        assertEquals(2, receivedStates.size());
    }

    @Test
    public void nameOnlyRuleMatchesAnyOperationAndType() throws Exception {
        DigitalTwinStateChangeFilter rule = DigitalTwinStateChangeFilter.onResources("near");
        assertTrue(rule.matches(new DigitalTwinStateChange(OPERATION_ADD, RELATIONSHIP, new DigitalTwinStateRelationship<>("near", "t"))));
        assertFalse(rule.matches(valueChange("temp")));
    }

    @Test
    public void contentFilterSelectsPerCategory() throws Exception {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest()
                .withPropertyNameFilter("temp")
                .withActionNameFilter()
                .withRelationshipNameFilter("near", "inside"));

        handler.state(f, fullState().getValues(), null);

        DigitalTwinStateValues got = receivedStates.get(0);
        assertEquals(1, got.getPropertyValues().size());
        assertTrue(got.getPropertyValues().containsKey("temp"));
        assertTrue(got.getLastActionInvocations().isEmpty());       // empty action filter removes the category
        assertEquals(2, got.getLastEventNotifications().size());    // events untouched (null filter)
        assertEquals(2, got.getRelationshipInstances().size());
    }

    @Test
    public void contentFilterOnStatelessContextHelper() throws Exception {
        DigitalTwinStateValues values = fullState().getValues();
        assertSame(values, DigitalTwinStateFilterUtils.filterValues(values, new AugmentationFunctionContextRequest()));
        assertSame(values, DigitalTwinStateFilterUtils.filterValues(values, null));

        DigitalTwinStateValues filtered = DigitalTwinStateFilterUtils.filterValues(values, new AugmentationFunctionContextRequest().withEventNameFilter("alarm"));
        assertEquals(1, filtered.getLastEventNotifications().size());
        assertEquals(2, filtered.getPropertyValues().size());
        assertEquals("1_0", filtered.getDescriptionId());
    }

    @Test
    public void descriptionIsNeverFiltered() throws Exception {
        TestHandler handler = new TestHandler();
        DigitalTwinStateDescription description = fullState().getDescription();

        // Content and trigger filters apply only to the values: the complete description is always delivered
        StatefulAugmentationFunction filtered = function(new AugmentationFunctionContextRequest()
                .withPropertyNameFilter("temp")
                .withActionNameFilter()
                .withStateChangeFilters(DigitalTwinStateChangeFilter.onPropertyValueUpdate("temp")));

        handler.description(filtered, description, changes(new DigitalTwinStateChange(OPERATION_ADD, PROPERTY, new DigitalTwinStateProperty<>("hum", 2))));

        assertEquals(1, receivedDescriptions.size());
        assertSame(description, receivedDescriptions.get(0));
        assertEquals(2, receivedDescriptions.get(0).getProperties().size());
        assertEquals(2, receivedDescriptions.get(0).getActions().size());
        assertEquals("1_0", receivedDescriptions.get(0).getId());
    }

    @Test
    public void eventNotificationsFilteredByName() {
        TestHandler handler = new TestHandler();
        StatefulAugmentationFunction f = function(new AugmentationFunctionContextRequest().withEventNameFilter("alarm"));

        handler.notification(f, "info");
        handler.notification(f, "alarm");

        assertEquals(Collections.singletonList("alarm"), receivedNotifications);
    }

    @Test
    public void observeFlagsSuppressPush() throws Exception {
        TestHandler handler = new TestHandler();
        AugmentationFunctionContextRequest request = new AugmentationFunctionContextRequest(false, false, null);
        assertFalse(request.isObserveState());
        assertFalse(request.isObserveEventNotifications());
        StatefulAugmentationFunction f = function(request);

        handler.state(f, fullState().getValues(), null);
        handler.description(f, fullState().getDescription(), null);
        handler.notification(f, "alarm");

        assertTrue(receivedStates.isEmpty());
        assertTrue(receivedDescriptions.isEmpty());
        assertTrue(receivedNotifications.isEmpty());
    }
}

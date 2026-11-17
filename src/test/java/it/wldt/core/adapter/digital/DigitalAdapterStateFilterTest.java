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
package it.wldt.core.adapter.digital;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.state.*;
import it.wldt.exception.WldtRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.wldt.core.state.DigitalTwinStateChange.Operation.*;
import static it.wldt.core.state.DigitalTwinStateChange.ResourceType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the three state callbacks of the Digital Adapter (onStateUpdate, onStateDescriptionUpdate,
 * onStateValuesUpdate) with and without a {@link DigitalTwinStateFilter}.
 */
public class DigitalAdapterStateFilterTest {

    private static class RecordingAdapter extends DigitalAdapter<Void> {

        final List<DigitalTwinState> states = new ArrayList<>();
        final List<DigitalTwinState> previousStates = new ArrayList<>();
        final List<List<DigitalTwinStateChange>> stateChanges = new ArrayList<>();
        final List<DigitalTwinStateDescription> descriptions = new ArrayList<>();
        final List<List<DigitalTwinStateChange>> descriptionChanges = new ArrayList<>();
        final List<DigitalTwinStateValues> values = new ArrayList<>();
        final List<List<DigitalTwinStateChange>> valueChanges = new ArrayList<>();

        RecordingAdapter() throws Exception {
            super("recording-adapter");
            setDigitalTwinId("state-filter-test-dt");
        }

        void observe(DigitalTwinStateFilter filter) throws Exception {
            observeDigitalTwinState(filter);
        }

        void clearFilter() {
            clearStateFilter();
        }

        void stopObserving() throws Exception {
            unObserveDigitalTwinState();
        }

        void deliver(DigitalTwinState state, DigitalTwinState previous, DigitalTwinStateChange... changes) throws Exception {
            WldtEvent<DigitalTwinState> event = new WldtEvent<>(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
            event.setBody(state);
            event.putMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_PREVIOUS_STATE, previous);
            event.putMetadata(DigitalTwinStateManager.DT_STATE_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
            onEvent(event);
        }

        @Override protected void onStateUpdate(DigitalTwinState n, DigitalTwinState p, ArrayList<DigitalTwinStateChange> c) { states.add(n); previousStates.add(p); stateChanges.add(c); }
        @Override protected void onStateDescriptionUpdate(DigitalTwinStateDescription d, List<DigitalTwinStateChange> c) { descriptions.add(d); descriptionChanges.add(c); }
        @Override protected void onStateValuesUpdate(DigitalTwinStateValues v, List<DigitalTwinStateChange> c) { values.add(v); valueChanges.add(c); }
        @Override protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> n) {}
        @Override public void onAdapterStart() {}
        @Override public void onAdapterStop() {}
        @Override public void onDigitalTwinSync(DigitalTwinState s) {}
        @Override public void onDigitalTwinUnSync(DigitalTwinState s) {}
        @Override public void onDigitalTwinCreate() {}
        @Override public void onDigitalTwinStart() {}
        @Override public void onDigitalTwinStop() {}
        @Override public void onDigitalTwinDestroy() {}
        @Override public void onWorkerStart() throws WldtRuntimeException {}
    }

    private RecordingAdapter adapter;
    private DigitalTwinState state;
    private DigitalTwinState previous;

    private static DigitalTwinState newState() throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", 20));
        properties.put("hum", new DigitalTwinStateProperty<>("hum", 40));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        return new DigitalTwinState(properties, new HashMap<>(), events, new HashMap<>());
    }

    @BeforeEach
    public void setUp() throws Exception {
        adapter = new RecordingAdapter();
        state = newState();
        previous = newState();
    }

    @AfterEach
    public void tearDown() throws Exception {
        adapter.stopObserving();
    }

    private static DigitalTwinStateChange valueChange(String property) throws Exception {
        return new DigitalTwinStateChange(OPERATION_UPDATE_VALUE, PROPERTY_VALUE, new DigitalTwinStateProperty<>(property, 1));
    }

    private static DigitalTwinStateChange eventAdded(String name) throws Exception {
        return new DigitalTwinStateChange(OPERATION_ADD, EVENT, new DigitalTwinStateEvent(name, "t", "string"));
    }

    private static DigitalTwinStateChange propertyAdded(String name) throws Exception {
        return new DigitalTwinStateChange(OPERATION_ADD, PROPERTY, new DigitalTwinStateProperty<>(name, 1));
    }

    @Test
    public void withoutFilterValueChangeNotifiesStateAndValuesOnly() throws Exception {
        adapter.deliver(state, previous, valueChange("temp"));

        assertEquals(1, adapter.states.size());
        assertSame(state, adapter.states.get(0));
        assertSame(previous, adapter.previousStates.get(0));
        assertEquals(0, adapter.descriptions.size());
        assertEquals(1, adapter.values.size());
        assertEquals(20, adapter.values.get(0).getPropertyValue("temp"));
        assertEquals(2, adapter.values.get(0).getPropertyValues().size());
    }

    @Test
    public void withoutFilterEventStructureChangeNotifiesStateAndDescriptionOnly() throws Exception {
        adapter.deliver(state, previous, eventAdded("alarm"));

        assertEquals(1, adapter.states.size());
        assertEquals(1, adapter.descriptions.size());
        assertEquals(0, adapter.values.size());
        assertEquals(1, adapter.descriptions.get(0).getEvents().size());
    }

    @Test
    public void propertyAddedNotifiesAllThreeCallbacks() throws Exception {
        adapter.deliver(state, previous, propertyAdded("temp"));

        assertEquals(1, adapter.states.size());
        assertEquals(1, adapter.descriptions.size());
        assertEquals(1, adapter.values.size());
    }

    @Test
    public void filterLimitsStateChangesAndValues() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        adapter.deliver(state, previous, valueChange("hum"), valueChange("temp"));

        assertEquals(1, adapter.states.size());
        DigitalTwinState delivered = adapter.states.get(0);
        assertEquals(1, delivered.getPropertyList().get().size());
        assertTrue(delivered.containsProperty("temp"));
        assertEquals(1, adapter.previousStates.get(0).getPropertyList().get().size());
        assertEquals(1, adapter.stateChanges.get(0).size());
        assertEquals(1, adapter.values.size());
        assertEquals(1, adapter.values.get(0).getPropertyValues().size());
        assertEquals(1, adapter.valueChanges.get(0).size());
    }

    @Test
    public void filterSkipsStateAndValuesWhenNoChangeIsAccepted() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        adapter.deliver(state, previous, valueChange("hum"));

        assertEquals(0, adapter.states.size());
        assertEquals(0, adapter.values.size());
    }

    @Test
    public void descriptionIsNeverFiltered() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp").withEventNames());

        // A structure change on a resource rejected by the filter: no state/values update but the description is notified
        adapter.deliver(state, previous, eventAdded("alarm"));

        assertEquals(0, adapter.states.size());
        assertEquals(0, adapter.values.size());
        assertEquals(1, adapter.descriptions.size());
        // Complete description, not limited by the filter
        assertEquals(2, adapter.descriptions.get(0).getProperties().size());
        assertEquals(1, adapter.descriptions.get(0).getEvents().size());
        assertEquals(1, adapter.descriptionChanges.get(0).size());
    }

    @Test
    public void clearFilterRestoresCompleteState() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"));
        adapter.clearFilter();

        adapter.deliver(state, previous, valueChange("hum"));

        assertEquals(1, adapter.states.size());
        assertSame(state, adapter.states.get(0));
    }

    @Test
    public void missingChangeListOnlyNotifiesState() throws Exception {
        WldtEvent<DigitalTwinState> event = new WldtEvent<>(DigitalTwinStateManager.getStatusUpdatesWldtEventMessageType());
        event.setBody(state);
        adapter.onEvent(event);

        assertEquals(1, adapter.states.size());
        assertEquals(0, adapter.values.size());
        assertEquals(0, adapter.descriptions.size());
    }
}

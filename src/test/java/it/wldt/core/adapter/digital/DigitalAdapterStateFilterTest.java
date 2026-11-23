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
 * Tests the state callbacks of the Digital Adapter (onStateDescriptionUpdate, onStateValuesUpdate): the description
 * is never filtered, the values are limited by the {@link DigitalTwinStateFilter} and by the
 * {@link DigitalTwinStateChangeFilter}, while the local copy of the state is always complete and up to date.
 */
public class DigitalAdapterStateFilterTest {

    private static final String DESCRIPTION_ID_0 = "1000_0";
    private static final String DESCRIPTION_ID_1 = "1000_1";

    private static class RecordingAdapter extends DigitalAdapter<Void> {

        final List<DigitalTwinStateDescription> descriptions = new ArrayList<>();
        final List<List<DigitalTwinStateChange>> descriptionChanges = new ArrayList<>();
        final List<DigitalTwinStateValues> values = new ArrayList<>();
        final List<List<DigitalTwinStateChange>> valueChanges = new ArrayList<>();
        final List<String> callbacksOrder = new ArrayList<>();

        RecordingAdapter() throws Exception {
            super("recording-adapter");
            setDigitalTwinId("state-filter-test-dt");
        }

        void observe(DigitalTwinStateFilter filter) throws Exception {
            observeDigitalTwinState(filter);
        }

        void observe(DigitalTwinStateFilter filter, DigitalTwinStateChangeFilter changeFilter) throws Exception {
            observeDigitalTwinState(filter, changeFilter);
        }

        void clearFilter() {
            clearStateFilter();
        }

        void stopObserving() throws Exception {
            unObserveDigitalTwinState();
        }

        DigitalTwinStateValues currentValues() {
            return getDigitalTwinStateValues();
        }

        DigitalTwinStateDescription currentDescription() {
            return getDigitalTwinStateDescription();
        }

        void deliverDescription(DigitalTwinStateDescription description, DigitalTwinStateChange... changes) throws Exception {
            WldtEvent<DigitalTwinStateDescription> event = new WldtEvent<>(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType());
            event.setBody(description);
            event.putMetadata(DigitalTwinStateManager.DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
            onEvent(event);
        }

        void deliverValues(DigitalTwinStateValues delta, DigitalTwinStateChange... changes) throws Exception {
            WldtEvent<DigitalTwinStateValues> event = new WldtEvent<>(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());
            event.setBody(delta);
            event.putMetadata(DigitalTwinStateManager.DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST, new ArrayList<>(Arrays.asList(changes)));
            onEvent(event);
        }

        void deliverValuesWithoutChanges(DigitalTwinStateValues delta) throws Exception {
            WldtEvent<DigitalTwinStateValues> event = new WldtEvent<>(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());
            event.setBody(delta);
            onEvent(event);
        }

        @Override protected void onStateDescriptionUpdate(DigitalTwinStateDescription d, List<DigitalTwinStateChange> c) { descriptions.add(d); descriptionChanges.add(c); callbacksOrder.add("description"); }
        @Override protected void onStateValuesUpdate(DigitalTwinStateValues v, List<DigitalTwinStateChange> c) { values.add(v); valueChanges.add(c); callbacksOrder.add("values"); }
        @Override protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> n) {}
        @Override public void onAdapterStart() {}
        @Override public void onAdapterStop() {}
        @Override public void onDigitalTwinSync(DigitalTwinStateDescription d, DigitalTwinStateValues v) {}
        @Override public void onDigitalTwinUnSync(DigitalTwinStateDescription d, DigitalTwinStateValues v) {}
        @Override public void onDigitalTwinCreate() {}
        @Override public void onDigitalTwinStart() {}
        @Override public void onDigitalTwinStop() {}
        @Override public void onDigitalTwinDestroy() {}
        @Override public void onWorkerStart() throws WldtRuntimeException {}
    }

    private RecordingAdapter adapter;

    /** Builds a state with the properties temp and hum (and the event alarm if requested) */
    private static DigitalTwinState newState(String descriptionId, int temp, boolean withAlarm) throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", temp));
        properties.put("hum", new DigitalTwinStateProperty<>("hum", 40));
        Map<String, DigitalTwinStateEvent> events = new HashMap<>();
        if (withAlarm)
            events.put("alarm", new DigitalTwinStateEvent("alarm", "t", "string"));
        DigitalTwinState state = new DigitalTwinState(properties, new HashMap<>(), events, new HashMap<>());
        return DigitalTwinStateTestFactory.withDescriptionId(state, descriptionId);
    }

    /** Values delta with only the listed properties */
    private static DigitalTwinStateValues delta(DigitalTwinState state, String... properties) {
        return state.getValues().filter(DigitalTwinStateFilter.all()
                .withPropertyNames(properties).withActionNames().withEventNames().withRelationshipNames());
    }

    private void sync(RecordingAdapter target) throws Exception {
        DigitalTwinState initial = newState(DESCRIPTION_ID_0, 20, false);
        target.onSync(initial.getDescription(), initial.getValues());
        // The deltas must be newer than the values used at synchronization time
        Thread.sleep(5);
    }

    @BeforeEach
    public void setUp() throws Exception {
        adapter = new RecordingAdapter();
        sync(adapter);
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
    public void syncInitializesTheLocalCopyOfTheState() {
        assertNotNull(adapter.currentDescription());
        assertEquals(DESCRIPTION_ID_0, adapter.currentDescription().getId());
        assertEquals(2, adapter.currentDescription().getProperties().size());
        assertEquals(20, adapter.currentValues().getPropertyValue("temp"));
        assertEquals(DESCRIPTION_ID_0, adapter.currentValues().getDescriptionId());
    }

    @Test
    public void withoutFilterValueChangeNotifiesCompleteValuesOnly() throws Exception {
        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 30, false);

        // The event carries only the changed value, the callback receives the complete values
        adapter.deliverValues(delta(updated, "temp"), valueChange("temp"));

        assertEquals(0, adapter.descriptions.size());
        assertEquals(1, adapter.values.size());
        assertEquals(30, adapter.values.get(0).getPropertyValue("temp"));
        assertEquals(40, adapter.values.get(0).getPropertyValue("hum"));
        assertEquals(2, adapter.values.get(0).getPropertyValues().size());
        assertEquals(DESCRIPTION_ID_0, adapter.values.get(0).getDescriptionId());
        assertEquals(1, adapter.valueChanges.get(0).size());
    }

    @Test
    public void descriptionChangeNotifiesTheCompleteDescriptionOnly() throws Exception {
        DigitalTwinState withAlarm = newState(DESCRIPTION_ID_1, 20, true);

        adapter.deliverDescription(withAlarm.getDescription(), eventAdded("alarm"));

        assertEquals(1, adapter.descriptions.size());
        assertEquals(0, adapter.values.size());
        assertEquals(DESCRIPTION_ID_1, adapter.descriptions.get(0).getId());
        assertEquals(1, adapter.descriptions.get(0).getEvents().size());
        assertEquals(1, adapter.descriptionChanges.get(0).size());
        // The local copy is aligned with the new description
        assertEquals(DESCRIPTION_ID_1, adapter.currentDescription().getId());
        assertEquals(DESCRIPTION_ID_1, adapter.currentValues().getDescriptionId());
    }

    @Test
    public void propertyAddedNotifiesDescriptionAndThenValues() throws Exception {
        Map<String, DigitalTwinStateProperty<?>> properties = new HashMap<>();
        properties.put("temp", new DigitalTwinStateProperty<>("temp", 20));
        properties.put("hum", new DigitalTwinStateProperty<>("hum", 40));
        properties.put("press", new DigitalTwinStateProperty<>("press", 1000));
        DigitalTwinState extended = DigitalTwinStateTestFactory.withDescriptionId(
                new DigitalTwinState(properties, new HashMap<>(), new HashMap<>(), new HashMap<>()), DESCRIPTION_ID_1);

        adapter.deliverDescription(extended.getDescription(), propertyAdded("press"));
        adapter.deliverValues(delta(extended, "press"), propertyAdded("press"));

        assertEquals(Arrays.asList("description", "values"), adapter.callbacksOrder);
        assertEquals(3, adapter.descriptions.get(0).getProperties().size());
        assertEquals(1000, adapter.values.get(0).getPropertyValue("press"));
        assertEquals(3, adapter.values.get(0).getPropertyValues().size());
    }

    @Test
    public void valuesReceivedBeforeTheirDescriptionAreProcessedAfterIt() throws Exception {
        DigitalTwinState extended = newState(DESCRIPTION_ID_1, 55, true);

        // The values refer to a description that the adapter does not know yet: they are kept
        adapter.deliverValues(delta(extended, "temp"), valueChange("temp"));
        assertEquals(0, adapter.values.size());
        assertEquals(20, adapter.currentValues().getPropertyValue("temp"));

        // When the description arrives it is applied first, then the waiting values
        adapter.deliverDescription(extended.getDescription(), eventAdded("alarm"));

        assertEquals(Arrays.asList("description", "values"), adapter.callbacksOrder);
        assertEquals(55, adapter.values.get(0).getPropertyValue("temp"));
        assertEquals(DESCRIPTION_ID_1, adapter.values.get(0).getDescriptionId());
    }

    @Test
    public void sameValueUpdateStillNotifiesWithRefreshedInstant() throws Exception {
        DigitalTwinState unchanged = newState(DESCRIPTION_ID_0, 20, false);
        java.time.Instant before = adapter.currentValues().getEvaluationInstant();

        // Empty delta (the value did not change) but the change list contains the update
        DigitalTwinStateValues emptyDelta = unchanged.getValues().filter(DigitalTwinStateFilter.all()
                .withPropertyNames().withActionNames().withEventNames().withRelationshipNames());
        adapter.deliverValues(emptyDelta, valueChange("temp"));

        assertEquals(1, adapter.values.size());
        assertEquals(20, adapter.values.get(0).getPropertyValue("temp"));
        assertTrue(adapter.values.get(0).getEvaluationInstant().isAfter(before));
    }

    @Test
    public void filterLimitsValuesAndChanges() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 30, false);
        adapter.deliverValues(delta(updated, "temp", "hum"), valueChange("hum"), valueChange("temp"));

        assertEquals(1, adapter.values.size());
        assertEquals(1, adapter.values.get(0).getPropertyValues().size());
        assertEquals(30, adapter.values.get(0).getPropertyValue("temp"));
        assertEquals(1, adapter.valueChanges.get(0).size());
    }

    @Test
    public void filterSkipsValuesWhenNoChangeIsAcceptedButKeepsTheLocalCopyUpdated() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 20, false);
        updated = DigitalTwinStateTestFactory.withDescriptionId(updated, DESCRIPTION_ID_0);
        adapter.deliverValues(delta(updated, "hum"), valueChange("hum"));

        assertEquals(0, adapter.values.size());
        // The local copy is never filtered
        assertEquals(2, adapter.currentValues().getPropertyValues().size());
    }

    @Test
    public void descriptionIsNeverFiltered() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp").withEventNames());

        // A structure change on a resource rejected by the filter: no values update but the description is notified
        DigitalTwinState withAlarm = newState(DESCRIPTION_ID_1, 20, true);
        adapter.deliverDescription(withAlarm.getDescription(), eventAdded("alarm"));

        assertEquals(0, adapter.values.size());
        assertEquals(1, adapter.descriptions.size());
        // Complete description, not limited by the filter
        assertEquals(2, adapter.descriptions.get(0).getProperties().size());
        assertEquals(1, adapter.descriptions.get(0).getEvents().size());
        assertEquals(1, adapter.descriptionChanges.get(0).size());
    }

    @Test
    public void triggerFilterRequiresAMatchingChange() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all(), DigitalTwinStateChangeFilter.onPropertyValueUpdate("temp"));

        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 30, false);
        adapter.deliverValues(delta(updated, "hum"), valueChange("hum"));
        assertEquals(0, adapter.values.size());

        adapter.deliverValues(delta(updated, "temp"), valueChange("temp"));
        assertEquals(1, adapter.values.size());
        // Complete values even with a trigger filter
        assertEquals(2, adapter.values.get(0).getPropertyValues().size());
    }

    @Test
    public void clearFilterRestoresCompleteValues() throws Exception {
        adapter.observe(DigitalTwinStateFilter.all().withPropertyNames("temp"), DigitalTwinStateChangeFilter.onPropertyValueUpdate("temp"));
        adapter.clearFilter();

        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 20, false);
        adapter.deliverValues(delta(updated, "hum"), valueChange("hum"));

        assertEquals(1, adapter.values.size());
        assertEquals(2, adapter.values.get(0).getPropertyValues().size());
    }

    @Test
    public void missingChangeListDoesNotNotifyValues() throws Exception {
        DigitalTwinState updated = newState(DESCRIPTION_ID_0, 30, false);
        adapter.deliverValuesWithoutChanges(delta(updated, "temp"));

        assertEquals(0, adapter.values.size());
        assertEquals(0, adapter.descriptions.size());
        // The local copy is updated anyway
        assertEquals(30, adapter.currentValues().getPropertyValue("temp"));
    }
}

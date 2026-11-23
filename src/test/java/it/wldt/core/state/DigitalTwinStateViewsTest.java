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
package it.wldt.core.state;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the description and values views of the {@link DigitalTwinState}.
 */
public class DigitalTwinStateViewsTest {

    private static DigitalTwinState buildState() throws Exception {
        DigitalTwinState state = new DigitalTwinState();
        state.createProperty(new DigitalTwinStateProperty<>("temp", 21.5));
        state.createProperty(new DigitalTwinStateProperty<>("hum", 40));
        state.enableAction(new DigitalTwinStateAction("on", "t", "c"));
        state.enableAction(new DigitalTwinStateAction("off", "t", "c"));
        state.registerEvent(new DigitalTwinStateEvent("alarm", "t", "string"));
        state.registerEvent(new DigitalTwinStateEvent("info", "t", "string"));
        state.createRelationship(new DigitalTwinStateRelationship<>("near", "t"));
        state.createRelationship(new DigitalTwinStateRelationship<>("inside", "t"));
        state.addRelationshipInstance("near", new DigitalTwinStateRelationshipInstance<>("near", "target-1", "k1"));
        return state;
    }

    private static <T> java.util.Set<String> keys(List<T> list, java.util.function.Function<T, String> f) {
        return list.stream().map(f).collect(Collectors.toSet());
    }

    @Test
    public void descriptionContainsStructureWithoutFilter() throws Exception {
        DigitalTwinState state = buildState();

        DigitalTwinStateDescription d = state.getDescription();

        assertEquals(state.getEvaluationInstant(), d.getEvaluationInstant());
        assertEquals(new HashSet<>(Arrays.asList("temp", "hum")), keys(d.getProperties(), DigitalTwinStateDescription.PropertyDescription::getKey));
        assertEquals(new HashSet<>(Arrays.asList("on", "off")), keys(d.getActions(), DigitalTwinStateDescription.ActionDescription::getKey));
        assertEquals(new HashSet<>(Arrays.asList("alarm", "info")), keys(d.getEvents(), DigitalTwinStateDescription.EventDescription::getKey));
        assertEquals(new HashSet<>(Arrays.asList("near", "inside")), keys(d.getRelationships(), DigitalTwinStateDescription.RelationshipDescription::getName));

        DigitalTwinStateDescription.PropertyDescription temp = d.getProperties().stream()
                .filter(p -> p.getKey().equals("temp")).findFirst().get();
        assertTrue(temp.isReadable());
        assertTrue(temp.isWritable());
    }

    @Test
    public void descriptionFilterSemantics() throws Exception {
        DigitalTwinState state = buildState();

        // null = whole category, empty = none, list = only listed names
        DigitalTwinStateFilter filter = DigitalTwinStateFilter.all()
                .withPropertyNames("temp")
                .withActionNames()
                .withEventNames("alarm", "missing");

        DigitalTwinStateDescription d = state.getDescription(filter);

        assertEquals(1, d.getProperties().size());
        assertEquals("temp", d.getProperties().get(0).getKey());
        assertTrue(d.getActions().isEmpty());
        assertEquals(1, d.getEvents().size());
        assertEquals(2, d.getRelationships().size());
    }

    @Test
    public void nullFilterIsEquivalentToNoFilter() throws Exception {
        DigitalTwinState state = buildState();

        assertEquals(2, state.getDescription(null).getProperties().size());
        assertEquals(2, state.getValues(null).getPropertyValues().size());
    }

    @Test
    public void valuesContainOnlyValuesAndRespectFilter() throws Exception {
        DigitalTwinState state = buildState();

        DigitalTwinStateValues all = state.getValues();
        assertEquals(state.getEvaluationInstant(), all.getEvaluationInstant());
        assertEquals(21.5, all.getPropertyValue("temp"));
        assertEquals(40, all.getPropertyValue("hum"));
        assertEquals(1, all.getRelationshipInstances().get("near").size());
        assertTrue(all.getRelationshipInstances().get("inside").isEmpty());

        DigitalTwinStateValues filtered = state.getValues(DigitalTwinStateFilter.all()
                .withPropertyNames("hum")
                .withRelationshipNames("inside"));
        assertEquals(1, filtered.getPropertyValues().size());
        assertEquals(40, filtered.getPropertyValue("hum"));
        assertEquals(1, filtered.getRelationshipInstances().size());
        assertTrue(filtered.getRelationshipInstances().containsKey("inside"));
    }

    @Test
    public void valuesSnapshotIsNotAlteredByLaterUpdates() throws Exception {
        DigitalTwinState state = buildState();

        DigitalTwinStateValues before = state.getValues();
        state.updatePropertyValue("temp", 30.0);
        DigitalTwinStateValues after = state.getValues();

        assertEquals(21.5, before.getPropertyValue("temp"));
        assertEquals(30.0, after.getPropertyValue("temp"));
        assertThrows(UnsupportedOperationException.class, () -> before.getPropertyValues().put("x", 1));
    }

    @Test
    public void lastTriggeredIsReportedOnlyOnceHappenedAndFiltered() throws Exception {
        DigitalTwinState state = buildState();
        assertTrue(state.getValues().getLastActionInvocations().isEmpty());
        assertTrue(state.getValues().getLastEventNotifications().isEmpty());

        Instant t = Instant.now();
        state.recordActionInvocation("on", t);
        state.recordEventNotification("alarm", t);

        DigitalTwinStateValues values = state.getValues();
        assertEquals(t, values.getLastActionInvocations().get("on"));
        assertFalse(values.getLastActionInvocations().containsKey("off"));
        assertEquals(t, values.getLastEventNotifications().get("alarm"));
        assertEquals(t, state.getLastActionInvocation("on").get());
        assertFalse(state.getLastEventNotification("info").isPresent());

        DigitalTwinStateValues filtered = state.getValues(DigitalTwinStateFilter.all().withActionNames("off").withEventNames());
        assertTrue(filtered.getLastActionInvocations().isEmpty());
        assertTrue(filtered.getLastEventNotifications().isEmpty());
    }

    @Test
    public void lastTriggeredIsCopiedWithTheState() throws Exception {
        DigitalTwinState state = buildState();
        Instant before = Instant.now();
        state.recordActionInvocation("on", before);

        DigitalTwinState copy = new DigitalTwinState(state);
        assertEquals(before, copy.getLastActionInvocation("on").get());

        // Every state owns its last-triggered information: what is recorded on one state after the copy
        // (e.g. by the transaction on the new state) does not alter the other one
        copy.recordActionInvocation("off", Instant.now());
        state.recordEventNotification("alarm", Instant.now());

        assertFalse(state.getLastActionInvocation("off").isPresent());
        assertFalse(copy.getLastEventNotification("alarm").isPresent());
    }

    @Test
    public void descriptionOfEmptyStateIsEmpty() {
        DigitalTwinState state = new DigitalTwinState();
        assertTrue(state.getDescription().isEmpty());
        assertTrue(state.getValues().isEmpty());
    }

    @Test
    public void filterKeepsEvaluationInstantAndLastTriggered() throws Exception {
        DigitalTwinState state = buildState();
        Instant now = Instant.now();
        state.recordActionInvocation("on", now);
        state.recordEventNotification("alarm", now);

        DigitalTwinState filtered = state.filter(DigitalTwinStateFilter.all().withPropertyNames("temp"));

        assertNotSame(state, filtered);
        assertEquals(state.getEvaluationInstant(), filtered.getEvaluationInstant());
        assertEquals(1, filtered.getPropertyList().get().size());
        assertEquals(now, filtered.getLastActionInvocation("on").get());
        assertEquals(now, filtered.getValues().getLastEventNotifications().get("alarm"));
        assertSame(state, state.filter(null));
        assertSame(state, state.filter(DigitalTwinStateFilter.all()));
    }

    @Test
    public void acceptedChangesKeepsOnlyAcceptedResources() throws Exception {
        DigitalTwinStateFilter filter = DigitalTwinStateFilter.all().withPropertyNames("temp");
        DigitalTwinStateChange temp = new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                DigitalTwinStateChange.ResourceType.PROPERTY_VALUE, new DigitalTwinStateProperty<>("temp", 1));
        DigitalTwinStateChange hum = new DigitalTwinStateChange(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE,
                DigitalTwinStateChange.ResourceType.PROPERTY_VALUE, new DigitalTwinStateProperty<>("hum", 1));

        assertEquals(Arrays.asList(temp), filter.acceptedChanges(Arrays.asList(temp, hum)));
        assertTrue(filter.acceptedChanges(null).isEmpty());
    }
}

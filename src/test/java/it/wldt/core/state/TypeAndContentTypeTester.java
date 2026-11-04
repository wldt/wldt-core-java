package it.wldt.core.state;

import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetEvent;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the separation between the semantic type and the content type (format) of the properties, events and
 * actions of both the Physical Asset Description and the Digital Twin State.
 */
public class TypeAndContentTypeTester {

    private static final String DT_ID = "dt-type-content-type";

    private static final String KEY = "accelerometer";
    private static final String SEMANTIC_TYPE = "com.example.accelerometer";
    private static final String CONTENT_TYPE = "string";

    // ------------------------------------------------------------------ DigitalTwinStateProperty

    @Test
    public void statePropertyKeepsTypeAndContentTypeSeparated() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(KEY, "1,2,3", SEMANTIC_TYPE, CONTENT_TYPE);

        assertEquals(KEY, property.getKey());
        assertEquals(SEMANTIC_TYPE, property.getType());
        assertEquals(CONTENT_TYPE, property.getContentType());
        assertTrue(property.isReadable());
        assertTrue(property.isWritable());
        assertTrue(property.isExposed());
    }

    @Test
    public void statePropertyFullConstructorSetsFlags() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<Integer> property = new DigitalTwinStateProperty<>(KEY, 7, SEMANTIC_TYPE, "int", false, false, false);

        assertEquals(SEMANTIC_TYPE, property.getType());
        assertEquals("int", property.getContentType());
        assertFalse(property.isReadable());
        assertFalse(property.isWritable());
        assertFalse(property.isExposed());
    }

    @Test
    public void statePropertyDefaultsToClassNameWhenNotProvided() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<Double> plain = new DigitalTwinStateProperty<>(KEY, 1.5);
        assertEquals(Double.class.getName(), plain.getType());
        assertEquals(Double.class.getName(), plain.getContentType());

        DigitalTwinStateProperty<Double> flags = new DigitalTwinStateProperty<>(KEY, 1.5, true, false);
        assertEquals(Double.class.getName(), flags.getType());
        assertEquals(Double.class.getName(), flags.getContentType());
        assertFalse(flags.isWritable());

        DigitalTwinStateProperty<Double> nulls = new DigitalTwinStateProperty<>(KEY, 1.5, null, null);
        assertEquals(Double.class.getName(), nulls.getType());
        assertEquals(Double.class.getName(), nulls.getContentType());

        DigitalTwinStateProperty<Double> onlyType = new DigitalTwinStateProperty<>(KEY, 1.5, SEMANTIC_TYPE, null);
        assertEquals(SEMANTIC_TYPE, onlyType.getType());
        assertEquals(Double.class.getName(), onlyType.getContentType());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedStatePropertyConstructorKeepsWorking() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(KEY, "x", SEMANTIC_TYPE);

        assertEquals(SEMANTIC_TYPE, property.getType());
        assertEquals(String.class.getName(), property.getContentType());
    }

    @Test
    public void statePropertySettersChangeBothFields() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<String> property = new DigitalTwinStateProperty<>(KEY, "x");

        property.setType(SEMANTIC_TYPE);
        property.setContentType("application/json");

        assertEquals(SEMANTIC_TYPE, property.getType());
        assertEquals("application/json", property.getContentType());
    }

    @Test
    public void statePropertyRejectsNullKeyOrValue() {
        assertThrows(WldtDigitalTwinStateException.class, () -> new DigitalTwinStateProperty<>(null, "x", SEMANTIC_TYPE, CONTENT_TYPE));
        assertThrows(WldtDigitalTwinStateException.class, () -> new DigitalTwinStateProperty<String>(KEY, null, SEMANTIC_TYPE, CONTENT_TYPE));
        assertThrows(WldtDigitalTwinStateException.class, () -> new DigitalTwinStateProperty<>(null, "x", SEMANTIC_TYPE, CONTENT_TYPE, true, true, true));
    }

    @Test
    public void statePropertyEqualsAndHashCodeIncludeContentType() throws WldtDigitalTwinStateException {
        DigitalTwinStateProperty<String> a = new DigitalTwinStateProperty<>(KEY, "x", SEMANTIC_TYPE, CONTENT_TYPE);
        DigitalTwinStateProperty<String> same = new DigitalTwinStateProperty<>(KEY, "x", SEMANTIC_TYPE, CONTENT_TYPE);
        DigitalTwinStateProperty<String> otherContentType = new DigitalTwinStateProperty<>(KEY, "x", SEMANTIC_TYPE, "application/json");
        DigitalTwinStateProperty<String> otherType = new DigitalTwinStateProperty<>(KEY, "x", "other.type", CONTENT_TYPE);

        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
        assertNotEquals(a, otherContentType);
        assertNotEquals(a, otherType);
        assertTrue(a.toString().contains("contentType='" + CONTENT_TYPE + "'"));
        assertTrue(a.toString().contains("type='" + SEMANTIC_TYPE + "'"));
    }

    // ------------------------------------------------------------------ DigitalTwinStateEvent

    @Test
    public void stateEventKeepsTypeAndContentTypeSeparated() {
        DigitalTwinStateEvent event = new DigitalTwinStateEvent("overheating", "iot.demo.overheating", "application/json");

        assertEquals("overheating", event.getKey());
        assertEquals("iot.demo.overheating", event.getType());
        assertEquals("application/json", event.getContentType());

        event.setContentType("string");
        event.setType("iot.demo.alarm");
        assertEquals("string", event.getContentType());
        assertEquals("iot.demo.alarm", event.getType());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedStateEventConstructorLeavesContentTypeNull() {
        DigitalTwinStateEvent event = new DigitalTwinStateEvent("overheating", "iot.demo.overheating");

        assertEquals("iot.demo.overheating", event.getType());
        assertNull(event.getContentType());
    }

    @Test
    public void stateEventEqualsHandlesNullsAndIncludesContentType() {
        DigitalTwinStateEvent a = new DigitalTwinStateEvent("k", "t", "string");
        DigitalTwinStateEvent same = new DigitalTwinStateEvent("k", "t", "string");
        DigitalTwinStateEvent otherContentType = new DigitalTwinStateEvent("k", "t", "application/json");

        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
        assertNotEquals(a, otherContentType);

        // no NullPointerException when type or contentType are not set
        assertEquals(new DigitalTwinStateEvent(), new DigitalTwinStateEvent());
        assertNotEquals(new DigitalTwinStateEvent("k", null, null), a);
        assertTrue(a.toString().startsWith("DigitalTwinStateEvent{"));
        assertTrue(a.toString().contains("contentType='string'"));
    }

    // ------------------------------------------------------------------ DigitalTwinStateAction

    @Test
    public void stateActionAlreadyHadBothAndKeepsThemSeparated() throws WldtDigitalTwinStateException {
        DigitalTwinStateAction action = new DigitalTwinStateAction("setRate", "iot.demo.set-sampling-rate", "application/json");

        assertEquals("iot.demo.set-sampling-rate", action.getType());
        assertEquals("application/json", action.getContentType());
        assertNotEquals(action, new DigitalTwinStateAction("setRate", "iot.demo.set-sampling-rate", "string"));
    }

    // ------------------------------------------------------------------ Physical Asset Description

    @Test
    public void physicalAssetPropertyEqualsAndHashCodeIncludeTypeAndContentType() {
        PhysicalAssetProperty<String> a = new PhysicalAssetProperty<>(KEY, SEMANTIC_TYPE, CONTENT_TYPE, "x");
        PhysicalAssetProperty<String> same = new PhysicalAssetProperty<>(KEY, SEMANTIC_TYPE, CONTENT_TYPE, "x");

        assertEquals(a, same);
        assertEquals(a.hashCode(), same.hashCode());
        assertNotEquals(a, new PhysicalAssetProperty<>(KEY, SEMANTIC_TYPE, "application/json", "x"));
        assertNotEquals(a, new PhysicalAssetProperty<>(KEY, "other.type", CONTENT_TYPE, "x"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void deprecatedPhysicalAssetPropertyConstructorKeepsWorking() {
        PhysicalAssetProperty<String> property = new PhysicalAssetProperty<>(KEY, SEMANTIC_TYPE, "x");

        assertEquals(SEMANTIC_TYPE, property.getType());
        assertEquals(String.class.getName(), property.getContentType());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void physicalAssetEventEqualsDoesNotFailWithoutContentType() {
        PhysicalAssetEvent legacy = new PhysicalAssetEvent("overheating", "iot.demo.overheating");
        PhysicalAssetEvent legacySame = new PhysicalAssetEvent("overheating", "iot.demo.overheating");
        PhysicalAssetEvent full = new PhysicalAssetEvent("overheating", "iot.demo.overheating", "string");

        assertNull(legacy.getContentType());
        assertEquals(legacy, legacySame);       // used to throw NullPointerException
        assertNotEquals(legacy, full);
        assertNotEquals(full, legacy);
        assertEquals(full, new PhysicalAssetEvent("overheating", "iot.demo.overheating", "string"));
    }

    @Test
    public void physicalAssetActionEqualsDoesNotFailWithNulls() {
        PhysicalAssetAction a = new PhysicalAssetAction("setRate", "iot.demo.set-sampling-rate", null);

        assertEquals(a, new PhysicalAssetAction("setRate", "iot.demo.set-sampling-rate", null));
        assertNotEquals(a, new PhysicalAssetAction("setRate", "iot.demo.set-sampling-rate", "string"));
    }

    // ------------------------------------------------------------------ Digital Twin State flow

    @Test
    public void typeAndContentTypeSurviveStateTransactions() throws Exception {
        DigitalTwinStateManager manager = new DigitalTwinStateManager(DT_ID);

        manager.startStateTransaction();
        manager.createProperty(new DigitalTwinStateProperty<>(KEY, "1,2,3", SEMANTIC_TYPE, CONTENT_TYPE));
        manager.registerEvent(new DigitalTwinStateEvent("overheating", "iot.demo.overheating", "application/json"));
        manager.enableAction(new DigitalTwinStateAction("setRate", "iot.demo.set-sampling-rate", "application/json"));
        manager.commitStateTransaction();

        DigitalTwinStateProperty<?> created = manager.getDigitalTwinState().getProperty(KEY).get();
        assertEquals(SEMANTIC_TYPE, created.getType());
        assertEquals(CONTENT_TYPE, created.getContentType());

        DigitalTwinStateEvent event = manager.getDigitalTwinState().getEvent("overheating").get();
        assertEquals("iot.demo.overheating", event.getType());
        assertEquals("application/json", event.getContentType());

        DigitalTwinStateAction action = manager.getDigitalTwinState().getAction("setRate").get();
        assertEquals("iot.demo.set-sampling-rate", action.getType());
        assertEquals("application/json", action.getContentType());

        // updating only the value keeps both type and content type
        manager.startStateTransaction();
        manager.updatePropertyValue(new DigitalTwinStateProperty<>(KEY, "4,5,6"));
        manager.commitStateTransaction();

        DigitalTwinStateProperty<?> updated = manager.getDigitalTwinState().getProperty(KEY).get();
        assertEquals("4,5,6", updated.getValue());
        assertEquals(SEMANTIC_TYPE, updated.getType());
        assertEquals(CONTENT_TYPE, updated.getContentType());

        // updating the whole property replaces type and content type
        manager.startStateTransaction();
        manager.updateProperty(new DigitalTwinStateProperty<>(KEY, "7", "iot.demo.accelerometer.v2", "application/json"));
        manager.commitStateTransaction();

        DigitalTwinStateProperty<?> replaced = manager.getDigitalTwinState().getProperty(KEY).get();
        assertEquals("iot.demo.accelerometer.v2", replaced.getType());
        assertEquals("application/json", replaced.getContentType());

        // a copy of the state (e.g., the one sent to the observers) keeps both fields
        DigitalTwinState copy = new DigitalTwinState(manager.getDigitalTwinState());
        assertEquals("iot.demo.accelerometer.v2", copy.getProperty(KEY).get().getType());
        assertEquals("application/json", copy.getProperty(KEY).get().getContentType());
        assertEquals("application/json", copy.getEvent("overheating").get().getContentType());
    }
}

package it.wldt.core.state;

import java.lang.reflect.Field;

/**
 * Test-only backdoor to the complete {@link DigitalTwinState} held by a {@link DigitalTwinStateManager}.
 * <p>
 * The manager does not expose the complete state anymore (only its description and its values separately, see
 * {@link DigitalTwinStateManager#getDigitalTwinStateDescription()} and
 * {@link DigitalTwinStateManager#getDigitalTwinStateValues()}). The CRUD tests of the state resources need to inspect
 * the resources themselves (not their description or values) to verify what a transaction produced, so they read
 * the internal state through reflection.
 */
public final class StateManagerTestAccess {

    private StateManagerTestAccess() {
    }

    /**
     * @param manager the state manager
     * @return the complete internal state of the manager (for test assertions only)
     */
    public static DigitalTwinState state(DigitalTwinStateManager manager) {
        try {
            Field field = DigitalTwinStateManager.class.getDeclaredField("digitalTwinState");
            field.setAccessible(true);
            return (DigitalTwinState) field.get(manager);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read the internal state of the Digital Twin State Manager", e);
        }
    }
}

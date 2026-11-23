package it.wldt.core.state;

import org.junit.jupiter.api.Test;

import static it.wldt.core.state.DigitalTwinStateChange.Operation.*;
import static it.wldt.core.state.DigitalTwinStateChange.ResourceType.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the classification of the state changes in description changes (structure) and values changes, that is
 * decided on the operation first and then on the type of the resource.
 */
public class DigitalTwinStateChangeClassificationTest {

    private static DigitalTwinStateChange change(DigitalTwinStateChange.Operation operation,
                                                 DigitalTwinStateChange.ResourceType type) throws Exception {
        DigitalTwinStateResource resource;
        switch (type) {
            case ACTION:
                resource = new DigitalTwinStateAction("a", "t", "ct");
                break;
            case EVENT:
                resource = new DigitalTwinStateEvent("e", "t", "ct");
                break;
            case RELATIONSHIP:
                resource = new DigitalTwinStateRelationship<>("r", "t");
                break;
            case RELATIONSHIP_INSTANCE:
                resource = new DigitalTwinStateRelationshipInstance<>("r", "target", "k");
                break;
            default:
                resource = new DigitalTwinStateProperty<>("p", 1);
        }
        return new DigitalTwinStateChange(operation, type, resource);
    }

    private static void check(DigitalTwinStateChange.Operation operation,
                              DigitalTwinStateChange.ResourceType type,
                              boolean description,
                              boolean values) throws Exception {
        DigitalTwinStateChange c = change(operation, type);
        assertEquals(description, DigitalTwinStateFilter.isDescriptionChange(c), "isDescriptionChange " + operation + " " + type);
        assertEquals(values, DigitalTwinStateFilter.isValueChange(c), "isValueChange " + operation + " " + type);
    }

    @Test
    public void propertyAndRelationshipDefinitions() throws Exception {
        for (DigitalTwinStateChange.ResourceType type : new DigitalTwinStateChange.ResourceType[]{PROPERTY, RELATIONSHIP}) {
            // The addition and the update carry a value, so they produce both a description and a values update
            check(OPERATION_ADD, type, true, true);
            check(OPERATION_UPDATE, type, true, true);
            // The removal is a description change only: the consumers drop the value when they apply the description
            check(OPERATION_REMOVE, type, true, false);
        }
    }

    @Test
    public void actionAndEventDefinitionsCarryNoValue() throws Exception {
        for (DigitalTwinStateChange.ResourceType type : new DigitalTwinStateChange.ResourceType[]{ACTION, EVENT}) {
            check(OPERATION_ADD, type, true, false);
            check(OPERATION_UPDATE, type, true, false);
            check(OPERATION_REMOVE, type, true, false);
            // The trigger updates the last invocation / notification, it is a value
            check(OPERATION_UPDATE_VALUE, type, false, true);
        }
    }

    @Test
    public void updateValueIsNeverADescriptionChange() throws Exception {
        for (DigitalTwinStateChange.ResourceType type : DigitalTwinStateChange.ResourceType.values())
            assertFalse(DigitalTwinStateFilter.isDescriptionChange(change(OPERATION_UPDATE_VALUE, type)), "UPDATE_VALUE " + type);

        check(OPERATION_UPDATE_VALUE, PROPERTY_VALUE, false, true);
        check(OPERATION_UPDATE_VALUE, PROPERTY, false, true);
    }

    @Test
    public void relationshipInstancesAreValuesBothWhenAddedAndRemoved() throws Exception {
        check(OPERATION_ADD, RELATIONSHIP_INSTANCE, false, true);
        check(OPERATION_REMOVE, RELATIONSHIP_INSTANCE, false, true);
    }

    @Test
    public void nullChangeIsNeither() {
        assertFalse(DigitalTwinStateFilter.isDescriptionChange(null));
        assertFalse(DigitalTwinStateFilter.isValueChange(null));
    }
}

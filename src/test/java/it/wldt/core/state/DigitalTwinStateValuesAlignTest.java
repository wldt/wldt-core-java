package it.wldt.core.state;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the evaluation instant of the values aligned to a new description.
 */
public class DigitalTwinStateValuesAlignTest {

    private static DigitalTwinStateDescription description(String id, Instant instant) {
        return new DigitalTwinStateDescription(id, instant,
                Collections.<DigitalTwinStateDescription.PropertyDescription>emptyList(),
                Collections.<DigitalTwinStateDescription.ActionDescription>emptyList(),
                Collections.<DigitalTwinStateDescription.EventDescription>emptyList(),
                Collections.<DigitalTwinStateDescription.RelationshipDescription>emptyList());
    }

    @Test
    public void alignedValuesTakeTheInstantOfTheStructuralChange() {
        DigitalTwinStateValues values = DigitalTwinStateValues.empty("1_0");
        Instant later = values.getEvaluationInstant().plusSeconds(10);

        DigitalTwinStateValues aligned = values.alignTo(description("1_1", later));

        assertEquals("1_1", aligned.getDescriptionId());
        assertEquals(later, aligned.getEvaluationInstant());
    }

    @Test
    public void alignedValuesNeverGoBackInTime() {
        DigitalTwinStateValues values = DigitalTwinStateValues.empty("1_1");
        Instant earlier = values.getEvaluationInstant().minusSeconds(10);

        DigitalTwinStateValues aligned = values.alignTo(description("1_0", earlier));

        assertEquals(values.getEvaluationInstant(), aligned.getEvaluationInstant());
    }
}

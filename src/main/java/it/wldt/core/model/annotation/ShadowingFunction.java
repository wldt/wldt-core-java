package it.wldt.core.model.annotation;

import java.lang.annotation.*;

/**
 * Marks a method as part of the Digital Twin Shadowing Function.
 * <p>
 * Methods annotated with {@code @ShadowingFunction} define how the Digital Twin
 * reacts to physical asset variations and digital action requests, implementing
 * the core shadowing behavior that maintains synchronization between the physical
 * asset and its digital representation.
 * <p>
 * This annotation serves as formal metadata for:
 * <ul>
 *   <li>Documentation generation tools</li>
 *   <li>Runtime introspection and validation</li>
 *   <li>IDE support and code navigation</li>
 *   <li>Metrics and observability instrumentation</li>
 * </ul>
 * 
 * Example Usage
 * <pre>{@code
 * @ShadowingFunction(
 *     value = ShadowingType.PHYSICAL_PROPERTY_VARIATION,
 *     description = "Updates DT state when temperature sensor reports new values"
 * )
 * protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> event) {
 *     // Shadowing implementation
 * }
 * }</pre>
 * 
 * @see ShadowingType
 * @see it.wldt.core.model.DigitalTwinModel
 * @author Marco Picone
 * @since 0.4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ShadowingFunction {
    
    /**
     * The type of shadowing operation this method handles.
     * <p>
     * Specifies which category of event triggers this callback, enabling
     * tools to categorize and process shadowing functions appropriately.
     * 
     * @return the shadowing operation type
     */
    ShadowingType value();
    
    /**
     * Optional human-readable description of the shadowing behavior.
     * <p>
     * Provides additional context about the specific logic implemented in
     * this shadowing function, useful for documentation and debugging.
     * 
     * @return description of the shadowing logic, or empty string if not specified
     */
    String description() default "";
}
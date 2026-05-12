package it.wldt.core.model.annotation;

/**
 * Enumeration of shadowing operation types in a Digital Twin.
 * <p>
 * Defines the different categories of events that trigger the shadowing function,
 * distinguishing between variations originating from the physical asset and
 * actions requested from the digital world.
 *
 * @author Marco Picone
 * @since 0.4.0
 */
public enum ShadowingType {

    /**
     * Shadowing operation triggered by physical asset property variations.
     * <p>
     * Invoked when a property value changes on the physical asset, requiring
     * the Digital Twin to update its internal state accordingly.
     */
    PHYSICAL_PROPERTY_VARIATION,

    /**
     * Shadowing operation triggered by physical asset event notifications.
     * <p>
     * Invoked when the physical asset emits an event that the Digital Twin
     * should process and potentially propagate to digital adapters.
     */
    PHYSICAL_EVENT_NOTIFICATION,

    /**
     * Shadowing operation triggered by physical relationship establishment.
     * <p>
     * Invoked when a new relationship is created on the physical asset,
     * requiring the Digital Twin to mirror this relationship in its state.
     */
    PHYSICAL_RELATIONSHIP_ESTABLISHED,

    /**
     * Shadowing operation triggered by physical relationship deletion.
     * <p>
     * Invoked when an existing relationship is removed from the physical asset,
     * requiring the Digital Twin to update its relationship model.
     */
    PHYSICAL_RELATIONSHIP_DELETED,

    /**
     * Shadowing operation triggered by digital action requests.
     * <p>
     * Invoked when an external digital application requests an action to be
     * performed, which the Digital Twin must process and potentially forward
     * to the physical asset.
     */
    DIGITAL_ACTION_REQUEST,

    /**
     * Shadowing operation triggered by the periodic or event-driven computation of an Augmentation Function.
     * <p>
     * Invoked when the Digital Twin performs an Augmentation Function computation, which may produce new properties
     * events or relationships that need to be integrated into the DT model.
     */
    AUGMENTATION_FUNCTION_RESULT,

    /**
     * Shadowing operation triggered by an error during the execution of an Augmentation Function.
     * <p>
     * Invoked when an Augmentation Function encounters an error during its execution, which may require
     * the Digital Twin to handle the error and potentially update its state or notify digital consumers.
     */
    AUGMENTATION_FUNCTION_ERROR,

    /**
     * Shadowing operation triggered by the availability of an Augmentation Function.
     * <p>
     * Invoked when an Augmentation Function becomes available for execution, which may require the Digital Twin
     * to potentially trigger its execution if it is relevant to the current state of the Digital Twin.
     */
    AUGMENTATION_FUNCTION_AVAILABLE,

    /**
     * Shadowing operation triggered by the unavailability of an Augmentation Function.
     * <p>
     * Invoked when an Augmentation Function becomes unavailable for execution, which may require the Digital Twin
     * to handle this change in availability and potentially update its state or notify digital consumers.
     */
    AUGMENTATION_FUNCTION_UNAVAILABLE,

    /**
     * Shadowing operation triggered by the availability of the list of Augmentation Functions.
     * <p>
     * Invoked when the Digital Twin receives the list of available Augmentation Functions, which may require
     * the Digital Twin to potentially trigger the execution of relevant functions based on the current state of the
     * Digital Twin and the functions' applicability.
     */
    AUGMENTATION_FUNCTION_LIST_AVAILABLE
}
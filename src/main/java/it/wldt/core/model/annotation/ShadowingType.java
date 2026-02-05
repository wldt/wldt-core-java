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
    DIGITAL_ACTION_REQUEST
}
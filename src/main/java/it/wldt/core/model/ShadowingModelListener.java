package it.wldt.core.model;

import it.wldt.core.state.DigitalTwinState;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class implement the shadowing process (also known as replication of digitalization) responsible to keep the
 * Digital Twin State synchronized with that of the corresponding physical resource
 * according to what is defined by the Model. It handles:
 *  - Physical Asset Description Management
 *  - Digital Twin State Management
 *  - Life Cycle Management
 *  - Incoming and outgoing events of both Physical and Digital Adapters
 */
public interface ShadowingModelListener {

    public void onShadowingSync(DigitalTwinState digitalTwinState);

    public void onShadowingOutOfSync(DigitalTwinState digitalTwinState);

}

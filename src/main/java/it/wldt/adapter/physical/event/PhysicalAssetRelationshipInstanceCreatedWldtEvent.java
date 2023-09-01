package it.wldt.adapter.physical.event;

import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.exception.EventBusException;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * A WLDT event describing the creation/presence of a new physical relationship type detected
 * on the physical asset connected to a Physical Adapter
 */
public class PhysicalAssetRelationshipInstanceCreatedWldtEvent<T> extends PhysicalAssetWldtEvent<PhysicalAssetRelationshipInstance<T>> {

    public static final String EVENT_BASIC_TYPE = "dt.physical.event.relationship.created";

    public PhysicalAssetRelationshipInstanceCreatedWldtEvent(PhysicalAssetRelationshipInstance<T> body) throws EventBusException {
        super(body.getRelationship().getName(), body);
    }

    @Override
    protected String getBasicEventType() {
        return EVENT_BASIC_TYPE;
    }

}

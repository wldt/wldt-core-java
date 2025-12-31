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

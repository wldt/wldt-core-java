/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
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

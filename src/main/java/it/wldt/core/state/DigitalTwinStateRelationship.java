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
package it.wldt.core.state;

import java.util.*;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Structures and describes a Relationship in the Digital Twins's State.
 * This is just the description of the relationships while the effective values/instances are described through the
 * other class DigitalTwinStateRelationshipInstance
 */
public class DigitalTwinStateRelationship<T> extends DigitalTwinStateResource {

    private final String name;

    private final String type;

    private final Map<String, DigitalTwinStateRelationshipInstance<T>> instances = new HashMap<>();

    public DigitalTwinStateRelationship(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<DigitalTwinStateRelationshipInstance<?>> getInstances() {
        return new ArrayList<>(instances.values());
    }

    public boolean containsInstance(String instanceKey){
        return instances.containsKey(instanceKey);
    }

    public DigitalTwinStateRelationshipInstance<T> getInstance(String instanceKey){
        return instances.get(instanceKey);
    }

    public void addInstance(DigitalTwinStateRelationshipInstance<?> i){
        this.instances.put(i.getKey(), (DigitalTwinStateRelationshipInstance<T>) i);
    }

    public void removeInstance(String instanceKey){
        if(containsInstance(instanceKey))
            this.instances.remove(instanceKey);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateRelationship{");
        sb.append("name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", instances=").append(instances);
        sb.append('}');
        return sb.toString();
    }
}

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
 * Contributors:
 *         Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *         Samuele Burattini (samuele.burattini@unibo.it)
 */
package it.wldt.adapter.physical;

import java.util.ArrayList;
import java.util.List;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * The Physical Asset Description (PAD) is used to describe the list of properties, actions and relationships of a Physical Asset
 * Each Physical Adapter generates a dedicated PAD associated to its perspective on the Physical Assets and its capabilities to read data and execute actions
 * It is a responsibility of the DT to handle multiple descriptions in order to build the digital replica
 * It will be used by the DT to handle the shadowing process and keep the digital replica synchronized with the physical counterpart
 *
 * A Physical Asset Description is responsible to describe and map the following aspect of the physical world
 * according to its capabilities and adopted protocols of the associated Physical Adapter:
 *      - Properties
 *      - Actions
 *      - Events
 *      - Relationships
 */
public class PhysicalAssetDescription {

    private List<PhysicalAssetAction> actions = new ArrayList<>();

    private List<PhysicalAssetProperty<?>> properties = new ArrayList<>();

    private List<PhysicalAssetEvent> events = new ArrayList<>();

    private List<PhysicalAssetRelationship<?>> relationships = new ArrayList<>();

    public PhysicalAssetDescription() {
    }

    public PhysicalAssetDescription(List<PhysicalAssetAction> actions, List<PhysicalAssetProperty<?>> properties, List<PhysicalAssetEvent> events) {
        this.actions = actions;
        this.properties = properties;
        this.events = events;
    }

    public PhysicalAssetDescription(List<PhysicalAssetAction> actions, List<PhysicalAssetProperty<?>> properties, List<PhysicalAssetEvent> events, List<PhysicalAssetRelationship<?>> relationships) {
        this.actions = actions;
        this.properties = properties;
        this.events = events;
        this.relationships = relationships;
    }

    public List<PhysicalAssetAction> getActions() {
        return actions;
    }

    public void setActions(List<PhysicalAssetAction> actions) {
        this.actions = actions;
    }

    public List<PhysicalAssetProperty<?>> getProperties() {
        return properties;
    }

    public void setProperties(List<PhysicalAssetProperty<?>> properties) {
        this.properties = properties;
    }

    public List<PhysicalAssetEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PhysicalAssetEvent> events) {
        this.events = events;
    }

    public List<PhysicalAssetRelationship<?>> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<PhysicalAssetRelationship<?>> relationships) {
        this.relationships = relationships;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetDescription{");
        sb.append("actions=").append(actions);
        sb.append(", properties=").append(properties);
        sb.append(", events=").append(events);
        sb.append(", relationships=").append(relationships);
        sb.append('}');
        return sb.toString();
    }
}

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of the <b>structure</b> of a {@link DigitalTwinState}: which properties, actions, events and
 * relationships are defined and how (key/name, type, content type, access flags), without any value.
 * <p>
 * Use {@link DigitalTwinState#getDescription()} or {@link DigitalTwinState#getDescription(DigitalTwinStateFilter)}
 * to obtain it. The values are available through {@link DigitalTwinStateValues}.
 */
public final class DigitalTwinStateDescription {

    private final Instant evaluationInstant;
    private final List<PropertyDescription> properties;
    private final List<ActionDescription> actions;
    private final List<EventDescription> events;
    private final List<RelationshipDescription> relationships;

    DigitalTwinStateDescription(Instant evaluationInstant,
                                List<PropertyDescription> properties,
                                List<ActionDescription> actions,
                                List<EventDescription> events,
                                List<RelationshipDescription> relationships) {
        this.evaluationInstant = evaluationInstant;
        this.properties = Collections.unmodifiableList(new ArrayList<>(properties));
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
        this.relationships = Collections.unmodifiableList(new ArrayList<>(relationships));
    }

    /**
     * @return the evaluation instant of the state this description has been extracted from
     */
    public Instant getEvaluationInstant() {
        return evaluationInstant;
    }

    /**
     * @return the (unmodifiable) description of the properties, never null
     */
    public List<PropertyDescription> getProperties() {
        return properties;
    }

    /**
     * @return the (unmodifiable) description of the actions, never null
     */
    public List<ActionDescription> getActions() {
        return actions;
    }

    /**
     * @return the (unmodifiable) description of the events, never null
     */
    public List<EventDescription> getEvents() {
        return events;
    }

    /**
     * @return the (unmodifiable) description of the relationships, never null
     */
    public List<RelationshipDescription> getRelationships() {
        return relationships;
    }

    /**
     * @return true if the description contains no resource at all
     */
    public boolean isEmpty() {
        return properties.isEmpty() && actions.isEmpty() && events.isEmpty() && relationships.isEmpty();
    }

    @Override
    public String toString() {
        return "DigitalTwinStateDescription{properties=" + properties + ", actions=" + actions +
                ", events=" + events + ", relationships=" + relationships +
                ", evaluationInstant=" + evaluationInstant + '}';
    }

    /**
     * Description of a Digital Twin State property (no value).
     */
    public static final class PropertyDescription {
        private final String key;
        private final String type;
        private final String contentType;
        private final boolean readable;
        private final boolean writable;
        private final boolean exposed;

        PropertyDescription(String key, String type, String contentType, boolean readable, boolean writable, boolean exposed) {
            this.key = key;
            this.type = type;
            this.contentType = contentType;
            this.readable = readable;
            this.writable = writable;
            this.exposed = exposed;
        }

        /** @return the property key */
        public String getKey() { return key; }
        /** @return the property type */
        public String getType() { return type; }
        /** @return the content type of the property value */
        public String getContentType() { return contentType; }
        /** @return true if the property is readable */
        public boolean isReadable() { return readable; }
        /** @return true if the property is writable */
        public boolean isWritable() { return writable; }
        /** @return true if the property is exposed */
        public boolean isExposed() { return exposed; }

        @Override
        public String toString() {
            return "PropertyDescription{key='" + key + "', type='" + type + "', contentType='" + contentType +
                    "', readable=" + readable + ", writable=" + writable + ", exposed=" + exposed + '}';
        }
    }

    /**
     * Description of a Digital Twin State action.
     */
    public static final class ActionDescription {
        private final String key;
        private final String type;
        private final String contentType;
        private final boolean exposed;

        ActionDescription(String key, String type, String contentType, boolean exposed) {
            this.key = key;
            this.type = type;
            this.contentType = contentType;
            this.exposed = exposed;
        }

        /** @return the action key */
        public String getKey() { return key; }
        /** @return the action type */
        public String getType() { return type; }
        /** @return the content type of the action input */
        public String getContentType() { return contentType; }
        /** @return true if the action is exposed */
        public boolean isExposed() { return exposed; }

        @Override
        public String toString() {
            return "ActionDescription{key='" + key + "', type='" + type + "', contentType='" + contentType +
                    "', exposed=" + exposed + '}';
        }
    }

    /**
     * Description of a Digital Twin State event.
     */
    public static final class EventDescription {
        private final String key;
        private final String type;
        private final String contentType;

        EventDescription(String key, String type, String contentType) {
            this.key = key;
            this.type = type;
            this.contentType = contentType;
        }

        /** @return the event key */
        public String getKey() { return key; }
        /** @return the event type */
        public String getType() { return type; }
        /** @return the content type of the event notification body */
        public String getContentType() { return contentType; }

        @Override
        public String toString() {
            return "EventDescription{key='" + key + "', type='" + type + "', contentType='" + contentType + "'}";
        }
    }

    /**
     * Description of a Digital Twin State relationship (no instances).
     */
    public static final class RelationshipDescription {
        private final String name;
        private final String type;

        RelationshipDescription(String name, String type) {
            this.name = name;
            this.type = type;
        }

        /** @return the relationship name */
        public String getName() { return name; }
        /** @return the relationship type */
        public String getType() { return type; }

        @Override
        public String toString() {
            return "RelationshipDescription{name='" + name + "', type='" + type + "'}";
        }
    }
}

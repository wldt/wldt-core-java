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

import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Objects;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 20/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic property associated to the Digital Twin State.
 * Each property is associated to a Key and a Value. Furthermore, it can also be associated to a type
 * to identify its nature and data structure. By default, it is associated to the type of the
 * Class (e.g., java.lang.String) but it can be directly changed by the developer
 * to associate it to a specific ontology or data type.
 *
 * @param <T> the type of the value associated to the property
 */
public class DigitalTwinStateProperty<T> extends DigitalTwinStateResource {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateProperty.class);

    /**
     * Key uniquely identifying the property in the Digital Twin State
     */
    private String key;

    /**
     * Value of the property for the target Digital Twin State
     */
    private T value;

    /**
     * Type of the Property. By default, it is associated to the type of the Class (e.g., java.lang.String) but it
     * can be directly changed by the developer to associate it to a specific ontology or data type.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network. In that case, the field can be used to de-serialize the object and understand
     * the property type
     */
    private String type = null;

    /**
     * Identify if the property is readable by external modules
     */
    private boolean readable = true;

    /**
     * Identify if the property is writable by external modules
     */
    private boolean writable = true;

    /**
     * Identify if the property is exposed to external modules or if is it just for internal purposes
     */
    private boolean exposed = true;

    private DigitalTwinStateProperty() {
    }

    public DigitalTwinStateProperty(String key, T value) throws WldtDigitalTwinStateException {

        if(key == null || value == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateProperty ! Key or Value = Null !");

        this.key = key;
        this.value = value;
        this.type = value.getClass().getName();
    }

    public DigitalTwinStateProperty(String key, T value, String type) throws WldtDigitalTwinStateException {

        if(key == null || value == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateProperty ! Key or Value = Null !");

        this.key = key;
        this.value = value;

        if(type == null)
            this.type = value.getClass().getName();
        else
            this.type = type;
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable) throws WldtDigitalTwinStateException {
        this(key, value);
        this.readable = readable;
        this.writable = writable;
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable, boolean exposed) throws WldtDigitalTwinStateException {
        this(key, value);
        this.readable = readable;
        this.writable = writable;
        this.exposed = exposed;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void setValueObject(Object valueObject){
        if(this.value.getClass().equals(valueObject.getClass()))
            this.value = (T) valueObject;
    }

    public boolean isReadable() {
        return readable;
    }

    public void setReadable(boolean readable) {
        this.readable = readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public void setWritable(boolean writable) {
        this.writable = writable;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DigitalTwinStateProperty)) return false;
        DigitalTwinStateProperty<?> that = (DigitalTwinStateProperty<?>) o;
        return readable == that.readable && writable == that.writable && exposed == that.exposed && Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, readable, writable, exposed);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinStateProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", type='").append(type).append('\'');
        sb.append(", readable=").append(readable);
        sb.append(", writable=").append(writable);
        sb.append(", exposed=").append(exposed);
        sb.append('}');
        return sb.toString();
    }
}

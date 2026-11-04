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
 * Each property is associated to a Key and a Value. Furthermore, it is described by two separate fields:
 * <ul>
 *     <li><b>type</b>: the semantic type of the property, i.e. what the property represents
 *     (e.g., "iot.demo.accelerometer" or "iot.demo.temperature"), typically taken from an ontology or from
 *     the application domain;</li>
 *     <li><b>contentType</b>: the format used to represent the value (e.g., "string", "application/json"
 *     or "java.lang.Double"). By default, it is the name of the Class of the value (e.g., java.lang.String).</li>
 * </ul>
 * The same pair is available in the {@code PhysicalAssetProperty} of the Physical Asset Description.
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
     * Semantic type of the Property: what the property represents (e.g., "iot.demo.accelerometer"), typically associated
     * to a specific ontology or application domain. It does not describe how the value is represented, that is the
     * role of the contentType. If it is not provided, it is set to the name of the Class of the value
     * (e.g., java.lang.String) for backward compatibility.
     */
    private String type = null;

    /**
     * Content-Type of the Property: the format used to represent its value (e.g., "string", "application/json").
     * If it is not provided, it is set to the name of the Class of the value (e.g., java.lang.String), like in
     * the PhysicalAssetProperty.
     */
    private String contentType = null;

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

    /**
     * Creates a property with the default type and contentType, both set to the name of the Class of the value.
     * @param key uniquely identifying the property in the Digital Twin State
     * @param value current value of the property
     * @throws WldtDigitalTwinStateException if key or value are null
     */
    public DigitalTwinStateProperty(String key, T value) throws WldtDigitalTwinStateException {
        this(key, value, null, null, true, true, true);
    }

    /**
     * Creates a property with a semantic type and a content type.
     * @param key uniquely identifying the property in the Digital Twin State
     * @param value current value of the property
     * @param type semantic type of the property (e.g., "iot.demo.accelerometer"). If null, the Class name of the value is used
     * @param contentType format of the value (e.g., "string"). If null, the Class name of the value is used
     * @throws WldtDigitalTwinStateException if key or value are null
     */
    public DigitalTwinStateProperty(String key, T value, String type, String contentType) throws WldtDigitalTwinStateException {
        this(key, value, type, contentType, true, true, true);
    }

    /**
     * Creates a property with a semantic type and a content type and with all the access flags.
     * @param key uniquely identifying the property in the Digital Twin State
     * @param value current value of the property
     * @param type semantic type of the property (e.g., "iot.demo.accelerometer"). If null, the Class name of the value is used
     * @param contentType format of the value (e.g., "string"). If null, the Class name of the value is used
     * @param readable true if the property is readable by external modules
     * @param writable true if the property is writable by external modules
     * @param exposed true if the property is exposed to external modules
     * @throws WldtDigitalTwinStateException if key or value are null
     */
    public DigitalTwinStateProperty(String key, T value, String type, String contentType, boolean readable, boolean writable, boolean exposed) throws WldtDigitalTwinStateException {

        if(key == null || value == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateProperty ! Key or Value = Null !");

        this.key = key;
        this.value = value;
        this.type = type != null ? type : value.getClass().getName();
        this.contentType = contentType != null ? contentType : value.getClass().getName();
        this.readable = readable;
        this.writable = writable;
        this.exposed = exposed;
    }

    /**
     * Creates a property with only the type. The contentType is set to the Class name of the value.
     * @param key uniquely identifying the property in the Digital Twin State
     * @param value current value of the property
     * @param type type of the property
     * @throws WldtDigitalTwinStateException if key or value are null
     * @deprecated the constructor with only the type will not be supported anymore from version 0.9.0.
     * Use {@link #DigitalTwinStateProperty(String, Object, String, String)} to set both the semantic type and the contentType.
     */
    @Deprecated
    public DigitalTwinStateProperty(String key, T value, String type) throws WldtDigitalTwinStateException {
        this(key, value, type, null, true, true, true);
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable) throws WldtDigitalTwinStateException {
        this(key, value, null, null, readable, writable, true);
    }

    public DigitalTwinStateProperty(String key, T value, boolean readable, boolean writable, boolean exposed) throws WldtDigitalTwinStateException {
        this(key, value, null, null, readable, writable, exposed);
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

    /**
     * @return the semantic type of the property
     */
    public String getType(){
        return this.type;
    }

    /**
     * @param type the semantic type of the property (e.g., "iot.demo.accelerometer")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the content type of the property, i.e. the format used to represent its value
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType the content type of the property (e.g., "string")
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DigitalTwinStateProperty)) return false;
        DigitalTwinStateProperty<?> that = (DigitalTwinStateProperty<?>) o;
        return readable == that.readable && writable == that.writable && exposed == that.exposed && Objects.equals(key, that.key) && Objects.equals(value, that.value) && Objects.equals(type, that.type) && Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value, type, contentType, readable, writable, exposed);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DigitalTwinStateProperty{");
        sb.append("key='").append(key).append('\'');
        sb.append(", value=").append(value);
        sb.append(", type='").append(type).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append(", readable=").append(readable);
        sb.append(", writable=").append(writable);
        sb.append(", exposed=").append(exposed);
        sb.append('}');
        return sb.toString();
    }
}

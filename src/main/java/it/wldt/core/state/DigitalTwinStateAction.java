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
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 20/06/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class define a generic action associated to the Digital Twin State.
 * Each action is by a key, an action type and a content type used to identify the expected input required by the action.
 * The type of the can be directly changed by the developer to associate it to a specific ontology or data type.
 */
public class DigitalTwinStateAction extends DigitalTwinStateResource{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateAction.class);

    /**
     * Key uniquely identifying the action in the Digital Twin State
     */
    private String key;

    /**
     * Type of the Action. By default, it is associated to the type of the Class T (e.g., java.lang.String) but it
     * can be directly changed by the developer to associate it to a specific ontology or data type.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network. In that case, the field can be used to de-serialize the object and understand
     * the property type
     */
    private String type = null;

    /**
     * Identify the expected contentType for the Action Input
     */
    private String contentType = null;

    /**
     * Identify if the action is exposed to external modules or if is it just for internal purposes
     */
    private boolean exposed = true;

    private DigitalTwinStateAction() {
    }

    public DigitalTwinStateAction(String key, String type, String contentType) throws WldtDigitalTwinStateException {

        if(key == null || type == null || contentType == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateAction ! Key, Type or ContentType = Null !");

        this.key = key;
        this.type = type;
        this.contentType = contentType;
    }

    public DigitalTwinStateAction(String key, String type, String contentType, boolean exposed) throws WldtDigitalTwinStateException {

        if(key == null || type == null || contentType == null)
            throw new WldtDigitalTwinStateException("Error creating DigitalTwinStateAction ! Key, Type or ContentType = Null !");

        this.key = key;
        this.type = type;
        this.contentType = contentType;
        this.exposed = exposed;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalTwinStateAction that = (DigitalTwinStateAction) o;
        return key.equals(that.key) && type.equals(that.type) && contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, contentType);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DigitalTwinStateAction{");
        sb.append("key='").append(key).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", contentType='").append(contentType).append('\'');
        sb.append(", exposed=").append(exposed);
        sb.append('}');
        return sb.toString();
    }
}

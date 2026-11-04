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
 * Each action is identified by a key and described by two separate fields:
 * <ul>
 *     <li><b>type</b>: the semantic type of the action, i.e. what the action does
 *     (e.g., "iot.demo.set-sampling-rate"), typically taken from an ontology or from the application domain;</li>
 *     <li><b>contentType</b>: the format of the input required by the action (e.g., "string" or "application/json").</li>
 * </ul>
 * The same pair is available in the {@code PhysicalAssetAction} of the Physical Asset Description.
 */
public class DigitalTwinStateAction extends DigitalTwinStateResource{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateAction.class);

    /**
     * Key uniquely identifying the action in the Digital Twin State
     */
    private String key;

    /**
     * Semantic type of the Action: what the action does (e.g., "iot.demo.set-sampling-rate"), typically associated to
     * a specific ontology or application domain. It does not describe the format of the input, that is the role
     * of the contentType.
     * Furthermore, it can be useful if the event management system will be extended to the event-base communication
     * between DTs over the network.
     */
    private String type = null;

    /**
     * Content-Type of the Action: the format of the expected input (e.g., "string", "application/json")
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
        return Objects.equals(key, that.key) && Objects.equals(type, that.type) && Objects.equals(contentType, that.contentType);
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

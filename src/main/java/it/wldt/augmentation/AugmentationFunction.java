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
package it.wldt.augmentation;

import it.wldt.exception.AugmentationFunctionException;

import java.util.List;

public abstract class AugmentationFunction {

    /**
     * The unique id of the augmentation function.
     */
    private String id;

    /**
     * The name of the augmentation function.
     */
    private String name;

    /**
     * The description of the augmentation function.
     */
    private String description;

    /**
     * The version of the augmentation function.
     */
    private String version;

    /**
     * The type of the augmentation function.
     */
    private AugmentationFunctionType type;

    /**
     * The context request of the augmentation function.
     */
    private AugmentationFunctionContextRequest contextRequest;

    /**
     * Constructor of the AugmentationFunction class with all the parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param description the description of the augmentation function
     * @param version the version of the augmentation function
     * @param type the type of the augmentation function
     * @param contextRequest the context request of the augmentation function
     */
    public AugmentationFunction(String id,
                                String name,
                                String description,
                                String version,
                                AugmentationFunctionType type,
                                AugmentationFunctionContextRequest contextRequest) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.type = type;
        this.contextRequest = contextRequest;
    }

    /**
     * Constructor of the AugmentationFunction class with minimum parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param type the type of the augmentation function
     */
    public AugmentationFunction(String id,
                                String name,
                                AugmentationFunctionType type) {
        this(id, name, null, null, type, new AugmentationFunctionContextRequest());
    }

    protected abstract List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) throws AugmentationFunctionException;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public AugmentationFunctionType getType() {
        return type;
    }

    public void setType(AugmentationFunctionType type) {
        this.type = type;
    }

    public AugmentationFunctionContextRequest getContextRequest() {
        return contextRequest;
    }

    public void setContextRequest(AugmentationFunctionContextRequest contextRequest) {
        this.contextRequest = contextRequest;
    }
}

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

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 12/02/2026
 * This Enum define the type of augmentation function associate in particular to the type execution
 * working in Stateless or Stateful mode. In Stateless mode the augmentation function is executed one shot,
 * while in Stateful mode the augmentation function is executed in a loop until the end of the execution
 * or once it is explicitly stopped.
 */
public enum AugmentationFunctionResultType {

    PROPERTY_RESULT("PROPERTY_RESULT"),
    RELATIONSHIP_RESULT("RELATIONSHIP_RESULT"),
    RELATIONSHIP_INSTANCE_RESULT("RELATIONSHIP_INSTANCE_RESULT"),
    EVENT_RESULT("EVENT_RESULT"),
    GENERIC_RESULT("GENERIC_RESULT");

    private String value;

    private AugmentationFunctionResultType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

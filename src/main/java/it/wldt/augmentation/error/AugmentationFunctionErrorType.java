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
package it.wldt.augmentation.error;

/**
 * Enum representing the type of an error that can occur during the execution of an augmentation function.
 * <p>
 * This enum defines different levels of error severity, which can be used to categorize and handle errors
 * appropriately based on their type. The error types can include informational messages, warnings, errors,
 * and critical errors, allowing for a structured approach to error handling and logging in the context of augmentation functions.
 * </p>
 */
public enum AugmentationFunctionErrorType {

    INFO("info"),
    WARNING("warning"),
    ERROR("error"),
    CRITICAL("critical");

    private String value;

    private AugmentationFunctionErrorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

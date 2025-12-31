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
package it.wldt.log;

/**
 * Default implementation of the WldtLoggerFactory interface.
 * This factory creates instances of WldtDefaultLogger.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class WldtDefaultLoggerFactory implements WldtLoggerFactory {

    /**
     * Creates a new instance of WldtDefaultLoggerFactory.
     */
    @Override
    public WldtLogger getLogger(Class<?> clazz) {
        return new WldtDefaultLogger(clazz);
    }
}

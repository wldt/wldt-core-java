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
 * WldtLoggerProvider is a utility class that provides a way to obtain a logger instance.
 * It allows for the use of a custom logger factory, enabling flexibility in logging implementations.
 * By default, it uses the WldtDefaultLoggerFactory.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class WldtLoggerProvider {

    /**
     * The default logger factory used to create logger instances.
     * It can be replaced with a custom factory using the setFactory method.
     * This allows for different logging implementations to be used throughout the application.
     */
    private static WldtLoggerFactory factory = new WldtDefaultLoggerFactory();

    /**
     * Sets a custom logger factory to be used by the WldtLoggerProvider.
     * This allows for different logging implementations to be used throughout the application.
     *
     * @param customFactory the custom logger factory to set
     */
    public static void setFactory(WldtLoggerFactory customFactory) {
        factory = customFactory;
    }

    /**
     * Gets a logger instance for the specified class.
     * This method uses the currently set logger factory to create the logger.
     *
     * @param clazz the class for which the logger is requested
     * @return a WldtLogger instance for the specified class
     */
    public static WldtLogger getLogger(Class<?> clazz) {
        return factory.getLogger(clazz);
    }
}

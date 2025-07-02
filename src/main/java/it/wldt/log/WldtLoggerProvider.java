/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
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

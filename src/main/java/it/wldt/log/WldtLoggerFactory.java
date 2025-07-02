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
 * WldtLoggerFactory interface for creating loggers.
 * This interface allows for the creation of loggers for specific classes.
 * Implementations can provide custom logging mechanisms.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public interface WldtLoggerFactory {

    /**
     * Creates a logger for the specified class.
     *
     * @param clazz the class for which the logger is to be created
     * @return a WldtLogger instance for the specified class
     */
    WldtLogger getLogger(Class<?> clazz);
}

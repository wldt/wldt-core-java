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
 * This interface defines the methods for logging messages at different levels.
 * It provides methods for trace, debug, info, warn, and error logging,
 * allowing for flexible and structured logging in applications.
 * Implementations of this interface should provide the actual logging functionality,
 * such as writing logs to a file, console, or external logging service.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public interface WldtLogger {

    /**
     * Gets the name of the logger.
     * This name is typically the fully qualified class name of the logger,
     * which helps in identifying the source of the log messages.
     * @return the name of the logger
     */
    String getName();

    void trace(String msg);
    void trace(String format, Object arg);
    void trace(String format, Object arg1, Object arg2);
    void trace(String format, Object... arguments);
    void trace(String msg, Throwable t);
    boolean isTraceEnabled();

    void debug(String msg);
    void debug(String format, Object arg);
    void debug(String format, Object arg1, Object arg2);
    void debug(String format, Object... arguments);
    void debug(String msg, Throwable t);
    boolean isDebugEnabled();

    void info(String msg);
    void info(String format, Object arg);
    void info(String format, Object arg1, Object arg2);
    void info(String format, Object... arguments);
    void info(String msg, Throwable t);
    boolean isInfoEnabled();

    void warn(String msg);
    void warn(String format, Object arg);
    void warn(String format, Object arg1, Object arg2);
    void warn(String format, Object... arguments);
    void warn(String msg, Throwable t);
    boolean isWarnEnabled();

    void error(String msg);
    void error(String format, Object arg);
    void error(String format, Object arg1, Object arg2);
    void error(String format, Object... arguments);
    void error(String msg, Throwable t);
    boolean isErrorEnabled();
}

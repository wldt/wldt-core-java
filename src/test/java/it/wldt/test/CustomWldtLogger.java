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
package it.wldt.test;

import it.wldt.log.WldtLogger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * CustomWldtLogger is a simple implementation of the WldtLogger interface.
 * Used to test the logging functionality in the WLDT (WebLogic Development Tools).
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class CustomWldtLogger implements WldtLogger {

    /**
     * The name of the logger, typically the simple name of the class it is logging for.
     */
    private final String name;

    /**
     * Date format used for logging timestamps.
     * The format is "yyyy-MM-dd HH:mm:ss.SSS".
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Default constructor for WldtDefaultLogger.
     * Initializes the logger with the class name of the caller.
     */
    public CustomWldtLogger(Class<?> clazz) {
        this.name = clazz.getSimpleName();
    }

    private void log(String level, String msg) {
        String timestamp = dateFormat.format(new Date());
        System.out.printf("CUSTOM-LOGGER: %s [%s] [%s] %s%n", timestamp, level, name, msg);
    }

    private void log(String level, String format, Object... args) {
        String message = String.format(format.replace("{}", "%s"), args);
        log(level, message);
    }

    private void log(String level, String msg, Throwable t) {
        log(level, msg);
        if (t != null) t.printStackTrace(System.out);
    }

    public String getName() { return name; }

    public void trace(String msg) { log("TRACE", msg); }
    public void trace(String format, Object arg) { log("TRACE", format, arg); }
    public void trace(String format, Object arg1, Object arg2) { log("TRACE", format, arg1, arg2); }
    public void trace(String format, Object... args) { log("TRACE", format, args); }
    public void trace(String msg, Throwable t) { log("TRACE", msg, t); }
    public boolean isTraceEnabled() { return true; }

    public void debug(String msg) { log("DEBUG", msg); }
    public void debug(String format, Object arg) { log("DEBUG", format, arg); }
    public void debug(String format, Object arg1, Object arg2) { log("DEBUG", format, arg1, arg2); }
    public void debug(String format, Object... args) { log("DEBUG", format, args); }
    public void debug(String msg, Throwable t) { log("DEBUG", msg, t); }
    public boolean isDebugEnabled() { return true; }

    public void info(String msg) { log("INFO", msg); }
    public void info(String format, Object arg) { log("INFO", format, arg); }
    public void info(String format, Object arg1, Object arg2) { log("INFO", format, arg1, arg2); }
    public void info(String format, Object... args) { log("INFO", format, args); }
    public void info(String msg, Throwable t) { log("INFO", msg, t); }
    public boolean isInfoEnabled() { return true; }

    public void warn(String msg) { log("WARN", msg); }
    public void warn(String format, Object arg) { log("WARN", format, arg); }
    public void warn(String format, Object arg1, Object arg2) { log("WARN", format, arg1, arg2); }
    public void warn(String format, Object... args) { log("WARN", format, args); }
    public void warn(String msg, Throwable t) { log("WARN", msg, t); }
    public boolean isWarnEnabled() { return true; }

    public void error(String msg) { log("ERROR", msg); }
    public void error(String format, Object arg) { log("ERROR", format, arg); }
    public void error(String format, Object arg1, Object arg2) { log("ERROR", format, arg1, arg2); }
    public void error(String format, Object... args) { log("ERROR", format, args); }
    public void error(String msg, Throwable t) { log("ERROR", msg, t); }
    public boolean isErrorEnabled() { return true; }
}

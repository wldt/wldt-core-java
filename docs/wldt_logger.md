# 📝 Logging in WLDT


The WLDT logging layer provides a flexible and extensible way to handle logging within your digital twin applications. It's designed to allow developers to easily switch between different logging implementations without modifying core application code.
The core components of the WLDT logging layer are:

* `WldtLogger`: An interface defining the standard logging methods (e.g., `info`, `debug`, `error`).
* `WldtLoggerFactory`: An interface for creating `WldtLogger` instances.
* `WldtLoggerProvider`: A utility class that provides a static method to get `WldtLogger` instances and allows setting a custom `WldtLoggerFactory`.
* `WldtDefaultLogger`: A default implementation of `WldtLogger` that prints log messages to the console.
* `WldtDefaultLoggerFactory`: A default implementation of `WldtLoggerFactory` that creates `WldtDefaultLogger` instances.

---

## WldtLogger Interface

The `WldtLogger` interface defines a set of methods for logging messages at different levels of severity.

**Methods:**

* `getName()`: Returns the name of the logger, typically the fully qualified class name.
* `trace(String msg)`, `trace(String format, Object arg)`, `trace(String format, Object arg1, Object arg2)`, `trace(String format, Object... arguments)`, `trace(String msg, Throwable t)`: Methods for logging **trace** level messages. These are typically fine-grained informational events that are most useful for debugging an application.
* `isTraceEnabled()`: Checks if the trace level is enabled.
* `debug(String msg)`, `debug(String format, Object arg)`, `debug(String format, Object arg1, Object arg2)`, `debug(String format, Object... arguments)`, `debug(String msg, Throwable t)`: Methods for logging **debug** level messages. These are typically used for debugging purposes during development.
* `isDebugEnabled()`: Checks if the debug level is enabled.
* `info(String msg)`, `info(String format, Object arg)`, `info(String format, Object arg1, Object arg2)`, `info(String format, Object... arguments)`, `info(String msg, Throwable t)`: Methods for logging **info** level messages. These provide general information about the application's progress.
* `isInfoEnabled()`: Checks if the info level is enabled.
* `warn(String msg)`, `warn(String format, Object arg)`, `warn(String format, Object arg1, Object arg2)`, `warn(String format, Object... arguments)`, `warn(String msg, Throwable t)`: Methods for logging **warn** level messages. These indicate potential issues or unexpected events that do not prevent the application from continuing.
* `isWarnEnabled()`: Checks if the warn level is enabled.
* `error(String msg)`, `error(String format, Object arg)`, `error(String format, Object arg1, Object arg2)`, `error(String format, Object... arguments)`, `error(String msg, Throwable t)`: Methods for logging **error** level messages. These indicate serious problems that prevent the application from functioning correctly.
* `isErrorEnabled()`: Checks if the error level is enabled.

---

## WldtLoggerFactory Interface

The `WldtLoggerFactory` interface is responsible for creating instances of `WldtLogger`.

**Method:**

* `getLogger(Class<?> clazz)`: This method takes a `Class<?>` object as input and returns a `WldtLogger` instance associated with that class. This allows logs to be categorized by their source class.

---

## WldtLoggerProvider Class

The `WldtLoggerProvider` acts as the entry point for obtaining logger instances.

**Key Features:**

* **Default Factory**: By default, it uses `WldtDefaultLoggerFactory` to create `WldtDefaultLogger` instances.
* **Custom Factory Support**: It allows you to set a custom `WldtLoggerFactory` to integrate with different logging frameworks or implement custom logging behavior.

**Methods:**

* `public static void setFactory(WldtLoggerFactory customFactory)`: Use this method to **replace the default logger factory** with your own custom implementation. This should typically be called once at the application's startup.
* `public static WldtLogger getLogger(Class<?> clazz)`: This static method is used to **obtain a logger instance** for a specific class. It delegates the logger creation to the currently set `WldtLoggerFactory`.

---

## WldtDefaultLogger Class

The `WldtDefaultLogger` is a basic, console-based implementation of the `WldtLogger` interface.

**Features:**

* **Console Output**: Logs messages to the standard output (`System.out`).
* **Timestamping**: Each log message is prefixed with a timestamp in the format "yyyy-MM-dd HH:mm:ss.SSS".
* **Log Level Indication**: Includes the log level (TRACE, DEBUG, INFO, WARN, ERROR) in each message.
* **Class Name**: Shows the simple name of the class that requested the logger.

---

## WldtDefaultLoggerFactory Class

The `WldtDefaultLoggerFactory` is the default factory implementation that creates `WldtDefaultLogger` instances.

**Method:**

* `public WldtLogger getLogger(Class<?> clazz)`: This method simply returns a new `WldtDefaultLogger` instance, initialized with the provided class.

---

## ⚙️ How to Create a Custom Logger and Custom Logger Factory

The WLDT logging layer is designed to be extensible, allowing developers to integrate their preferred logging frameworks (e.g., Log4j, SLF4j, Logback) or implement entirely custom logging logic.

### 1. Create a Custom Logger

To create a custom logger, you need to implement the `WldtLogger` interface. This allows you to define how log messages are handled (e.g., written to a file, sent to a remote server, or integrated with an existing logging framework).

Here's an example of a `MyCustomLogger` that could integrate with a third-party logging framework:

```java
package it.wldt.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCustomLogger implements WldtLogger {

    private final Logger slf4jLogger;

    public MyCustomLogger(Class<?> clazz) {
        this.slf4jLogger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public String getName() {
        return slf4jLogger.getName();
    }

    @Override
    public void trace(String msg) {
        slf4jLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        slf4jLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        slf4jLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        slf4jLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        slf4jLogger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return slf4jLogger.isTraceEnabled();
    }

    @Override
    public void debug(String msg) {
        slf4jLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        slf4jLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        slf4jLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        slf4jLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        slf4jLogger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return slf4jLogger.isDebugEnabled();
    }

    @Override
    public void info(String msg) {
        slf4jLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        slf4jLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        slf4jLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        slf4jLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        slf4jLogger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return slf4jLogger.isInfoEnabled();
    }

    @Override
    public void warn(String msg) {
        slf4jLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        slf4jLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        slf4jLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        slf4jLogger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        slf4jLogger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return slf4jLogger.isWarnEnabled();
    }

    @Override
    public void error(String msg) {
        slf4jLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        slf4jLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        slf4jLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        slf4jLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        slf4jLogger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return slf4jLogger.isErrorEnabled();
    }
}
```

### 2. Create a Custom Logger Factory

Once you have your custom logger, you need a custom logger factory to provide instances of it. 
This factory will implement the `WldtLoggerFactory` interface.
Here's an example of a MyCustomLoggerFactory that produces MyCustomLogger instances:

```java
package it.wldt.log;

public class MyCustomLoggerFactory implements WldtLoggerFactory {

    @Override
    public WldtLogger getLogger(Class<?> clazz) {
        return new MyCustomLogger(clazz);
    }
}
```

### 3. Set the Custom Logger Factory

Finally, to use your custom logger and factory, you need to instruct the `WldtLoggerProvider` to use your `MyCustomLoggerFactory`. 
This should be done once at the beginning of your application's lifecycle, before any loggers are requested.

```java
import it.wldt.log.WldtLoggerProvider;
import it.wldt.log.MyCustomLoggerFactory; // Your custom factory
import it.wldt.log.WldtLogger;

public class MyApplication {

    public static void main(String[] args) {
        // Set the custom logger factory
        WldtLoggerProvider.setFactory(new MyCustomLoggerFactory());

        // Now, any logger obtained will be an instance of MyCustomLogger
        WldtLogger logger = WldtLoggerProvider.getLogger(MyApplication.class);
        logger.info("This message will be logged using MyCustomLogger!");
        logger.error("An error occurred!", new RuntimeException("Something went wrong"));
    }
}
```

By following these steps, you can seamlessly integrate custom logging solutions into your WLDT digital twin applications, 
ensuring that your logging infrastructure meets your specific project requirements.
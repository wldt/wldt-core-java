package it.wldt.test;

import it.wldt.log.WldtLoggerProvider;
import it.wldt.log.WldtLogger;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project test-generic-logger
 * @created 21/06/2025 - 20:53
 */
public class LoggerTestMain {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(LoggerTestMain.class);

    public static void main(String[] args) {

        logger.info("Doing something...");
        logger.debug("Example value: {}", 42);
        logger.error("Error Example", new RuntimeException("Simulated Error"));
    }

}

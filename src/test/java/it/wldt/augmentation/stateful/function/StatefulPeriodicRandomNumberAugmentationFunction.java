package it.wldt.augmentation.stateful.function;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class StatefulPeriodicRandomNumberAugmentationFunction extends StatefulAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulPeriodicRandomNumberAugmentationFunction.class);

    private Timer timer;

    private TimerTask timerTask;

    private static final long AUGMENTATION_FUNCTION_TIME_MS = 1000;

    public static final int AUGMENTATION_FUNCTION_EXECUTION_COUNT = 5;

    public static final String FUNCTION_ID = "periodic-random-number-augmentation-function";

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public StatefulPeriodicRandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "Random Number Augmentation Function",
                "This augmentation function generates a random number",
                "1.0.0");
    }

    private AugmentationFunctionResult<Double> generateNewAugmentationFunctionResult() throws AugmentationFunctionException {

        Long startTimestamp = System.currentTimeMillis();
        // Generate a random number
        double randomNumber = Math.random();

        Long endTimestamp = System.currentTimeMillis();

        AugmentationFunctionResultMetrics augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );

        // Create an AugmentationFunctionResult with the random number
        AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                "randomNumber",
                randomNumber,
                augmentationFunctionResultMetrics,
                null
        );

        logger.debug("StatefulPeriodicRandomNumberAugmentationFunction -> Generated random number: {}", result);

        return result;
    }

    /**
     * Create a timer task with a configurable period in milliseconds
     * to generate a new random number and notify the result listener.
     * @param initialDelayMs the initial delay in milliseconds before the first execution of the task
     * @param periodMs the period in milliseconds to generate a new random number
     */
    private void createTimerTask(long initialDelayMs, long periodMs) {

        if (periodMs <= 0) {
            throw new IllegalArgumentException("periodMs must be greater than 0");
        }

        // To be sure stop any existing timer task before creating a new one
        stopTimerTask();

        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    AugmentationFunctionResult<Double> result = generateNewAugmentationFunctionResult();
                    notifyResult(new AugmentationFunctionResultList(result));
                } catch (AugmentationFunctionException e) {
                    logger.error("Error generating new augmentation function result: {}", e.getMessage());
                }

            }
        };

        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(this.timerTask, initialDelayMs, periodMs);
    }

    /**
     * Stop and cancel the Timer Task
     */
    private void stopTimerTask() {
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }

        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
    }

    @Override
    public void start(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try{
            // Get the period in milliseconds from the context parameters, default to 1000msif not present
            logger.info("Starting the StatefulPeriodicRandomNumberAugmentationFunction with period {} ms", AUGMENTATION_FUNCTION_TIME_MS);
            createTimerTask(0, AUGMENTATION_FUNCTION_TIME_MS);
        } catch (Exception e) {
            logger.error("Error starting the StatefulPeriodicRandomNumberAugmentationFunction: {}", e.getMessage());
            throw new AugmentationFunctionException(String.format("Error starting the StatefulPeriodicRandomNumberAugmentationFunction error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try{
            logger.info("Stopping the StatefulPeriodicRandomNumberAugmentationFunction");
            stopTimerTask();
        } catch (Exception e) {
            logger.error("Error stopping the StatefulPeriodicRandomNumberAugmentationFunction: {}", e.getMessage());
            throw new AugmentationFunctionException(String.format("Error stopping the StatefulPeriodicRandomNumberAugmentationFunction error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        logger.debug("Received state update: {}", digitalTwinState);
    }

    @Override
    public void onQueryResultRefresh(QueryRequest queryRequest, QueryResult<?> queryResult) throws AugmentationFunctionException {
        logger.debug("Received query result refresh: queryRequest={}, queryResult={}", queryRequest, queryResult);
    }

    @Override
    public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws AugmentationFunctionException {
        logger.debug("Received event notification: {}", digitalTwinStateEventNotification);
    }
}

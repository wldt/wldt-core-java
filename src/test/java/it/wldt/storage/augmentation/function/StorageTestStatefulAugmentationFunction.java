package it.wldt.storage.augmentation.function;

import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
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

public class StorageTestStatefulAugmentationFunction extends StatefulAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageTestStatefulAugmentationFunction.class);

    public static final String FUNCTION_ID = "storage-test-stateful-function";

    public static final String RESULT_KEY = "storage-test-stateful-result-key";

    public static final int EXECUTION_COUNT = 5;

    public static final long SLEEP_BETWEEN_EXECUTIONS_MS = 500;

    private Timer timer;

    private TimerTask timerTask;

    private int executionCounter = 0;

    public StorageTestStatefulAugmentationFunction() {
        super(FUNCTION_ID,
                "Storage Test Stateful Augmentation Function",
                "Stateful augmentation function for storage testing. Produces EXECUTION_COUNT results.",
                "1.0.0");
    }

    private AugmentationFunctionResult<String> generateResult(int index) throws AugmentationFunctionException {
        Long startTimestamp = System.currentTimeMillis();
        String resultValue = "storage-test-stateful-result-value-" + index;
        Long endTimestamp = System.currentTimeMillis();

        AugmentationFunctionResultMetrics metrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );

        return new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                RESULT_KEY,
                resultValue,
                metrics,
                null
        );
    }

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
    protected void start(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try {
            logger.info("Starting StorageTestStatefulAugmentationFunction with {} executions, {} ms between each",
                    EXECUTION_COUNT, SLEEP_BETWEEN_EXECUTIONS_MS);

            stopTimerTask();
            executionCounter = 0;

            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (executionCounter < EXECUTION_COUNT) {
                            AugmentationFunctionResult<String> result = generateResult(executionCounter);
                            notifyResult(Collections.singletonList(result));
                            executionCounter++;
                            logger.debug("StorageTestStatefulAugmentationFunction -> Produced result {}/{}", executionCounter, EXECUTION_COUNT);
                        } else {
                            stopTimerTask();
                            logger.info("StorageTestStatefulAugmentationFunction -> All {} executions completed", EXECUTION_COUNT);
                        }
                    } catch (AugmentationFunctionException e) {
                        logger.error("Error generating result: {}", e.getMessage());
                    }
                }
            };

            this.timer = new Timer(true);
            this.timer.scheduleAtFixedRate(this.timerTask, 0, SLEEP_BETWEEN_EXECUTIONS_MS);

        } catch (Exception e) {
            throw new AugmentationFunctionException(
                    String.format("Error starting StorageTestStatefulAugmentationFunction: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    protected void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try {
            logger.info("Stopping StorageTestStatefulAugmentationFunction");
            stopTimerTask();
        } catch (Exception e) {
            throw new AugmentationFunctionException(
                    String.format("Error stopping StorageTestStatefulAugmentationFunction: %s", e.getLocalizedMessage()));
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


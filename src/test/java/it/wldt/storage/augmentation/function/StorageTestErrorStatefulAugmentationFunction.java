package it.wldt.storage.augmentation.function;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.augmentation.function.StatefulAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

import java.util.Timer;
import java.util.TimerTask;

public class StorageTestErrorStatefulAugmentationFunction extends StatefulAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageTestErrorStatefulAugmentationFunction.class);

    public static final String FUNCTION_ID = "storage-test-error-stateful-function";

    public static final String ERROR_MESSAGE = "Storage test stateful error";

    public static final AugmentationFunctionErrorType ERROR_TYPE = AugmentationFunctionErrorType.CRITICAL;

    public static final int ERROR_COUNT = 3;

    public static final long SLEEP_BETWEEN_ERRORS_MS = 300;

    private Timer timer;

    private TimerTask timerTask;

    private int errorCounter = 0;

    public StorageTestErrorStatefulAugmentationFunction() {
        super(FUNCTION_ID,
                "Storage Test Error Stateful Augmentation Function",
                "Stateful augmentation function for storage error testing. Emits ERROR_COUNT errors.",
                "1.0.0");
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
            logger.info("Starting StorageTestErrorStatefulAugmentationFunction with {} errors, {} ms between each",
                    ERROR_COUNT, SLEEP_BETWEEN_ERRORS_MS);

            stopTimerTask();
            errorCounter = 0;

            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (errorCounter < ERROR_COUNT) {
                        AugmentationFunctionError error = new AugmentationFunctionError(ERROR_TYPE, ERROR_MESSAGE);
                        notifyError(error);
                        errorCounter++;
                        logger.debug("StorageTestErrorStatefulAugmentationFunction -> Notified error {}/{}", errorCounter, ERROR_COUNT);
                    } else {
                        stopTimerTask();
                        logger.info("StorageTestErrorStatefulAugmentationFunction -> All {} errors emitted", ERROR_COUNT);
                    }
                }
            };

            this.timer = new Timer(true);
            this.timer.scheduleAtFixedRate(this.timerTask, 0, SLEEP_BETWEEN_ERRORS_MS);

        } catch (Exception e) {
            throw new AugmentationFunctionException(
                    String.format("Error starting StorageTestErrorStatefulAugmentationFunction: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    protected void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try {
            logger.info("Stopping StorageTestErrorStatefulAugmentationFunction");
            stopTimerTask();
        } catch (Exception e) {
            throw new AugmentationFunctionException(
                    String.format("Error stopping StorageTestErrorStatefulAugmentationFunction: %s", e.getLocalizedMessage()));
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


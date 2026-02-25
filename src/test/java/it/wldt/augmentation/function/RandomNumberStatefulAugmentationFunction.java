package it.wldt.augmentation.function;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class RandomNumberStatefulAugmentationFunction extends StatefulAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(RandomNumberStatefulAugmentationFunction.class);

    public static final String FUNCTION_ID = "random-number-stateful-augmentation-function";

    private TimerTask timerTask;

    private Timer timer;

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public RandomNumberStatefulAugmentationFunction() {
        super(FUNCTION_ID,
                "Stateful Random Number Augmentation Function",
                "This Stateful augmentation function generates a random number between 0 and 100.",
                "1.0.0");

        // Initialize the timer task to generate random numbers periodically
    }

    private void createTimerTask() {

        // Create a timer task that runs every 5 seconds
        timerTask = new TimerTask() {
            @Override
            public void run() {
                try {

                    // Generate a random number between 0 and 1
                    double randomNumber = Math.random();

                    // Create an AugmentationFunctionResult with the random number
                    AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                            AugmentationFunctionResultType.GENERIC_RESULT,
                            "randomNumber",
                            randomNumber,
                            null
                    );

                    // Notify the result to the listeners
                    notifyResult(Collections.singletonList(result));

                    logger.debug("RandomNumberAugmentationFunction -> Generated random number: {}", result);

                } catch (Exception e) {
                    logger.error("Error running the augmentation function: {}", e.getMessage());
                }
            }
        };

        // Schedule the timer task to run every 5 seconds
        timer = new Timer();
        timer.schedule(timerTask, 0, 5000);
    }

    private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public boolean start(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try{
            createTimerTask();
            return true;
        } catch (Exception e) {
            logger.error("Error starting the augmentation function: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean stop(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try {
            cancelTimerTask();
            return true;
        } catch (Exception e) {
            logger.error("Error stopping the augmentation function: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        logger.info("Received state update: {}", digitalTwinState);
    }

    @Override
    public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws AugmentationFunctionException {
        logger.info("Received event notification: {}", digitalTwinStateEventNotification);
    }
}

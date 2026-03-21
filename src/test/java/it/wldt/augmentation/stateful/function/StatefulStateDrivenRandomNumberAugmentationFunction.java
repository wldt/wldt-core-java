package it.wldt.augmentation.stateful.function;

import it.wldt.augmentation.context.AugmentationFunctionContext;
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

import java.util.*;

public class StatefulStateDrivenRandomNumberAugmentationFunction extends StatefulAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulStateDrivenRandomNumberAugmentationFunction.class);

    public static final String FUNCTION_ID = "state-driven-random-number-augmentation-function";

    private List<AugmentationFunctionResult<?>> lastResultList;

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public StatefulStateDrivenRandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "Random Number Augmentation Function",
                "This augmentation function generates a random number",
                "1.0.0");

        // Init result list
        this.lastResultList = new ArrayList<>();
    }

    private AugmentationFunctionResult<Double> generateNewAugmentationFunctionResult() throws AugmentationFunctionException {

        Long startTimestamp = System.currentTimeMillis();

        // Generate a random number
        double randomNumber = Math.random();

        Long endTimestamp = System.currentTimeMillis();

        AugmentationFunctionResultMetrics augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(startTimestamp, endTimestamp);

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

    @Override
    public void start(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try{
            // Get the period in milliseconds from the context parameters, default to 1000msif not present
            logger.info("Starting the StatefulPeriodicRandomNumberAugmentationFunction ...");

        } catch (Exception e) {
            logger.error("Error starting the StatefulPeriodicRandomNumberAugmentationFunction: {}", e.getMessage());
            throw new AugmentationFunctionException(String.format("Error starting the StatefulPeriodicRandomNumberAugmentationFunction error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void stop(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        try{
            logger.info("Stopping the StatefulPeriodicRandomNumberAugmentationFunction");
        } catch (Exception e) {
            logger.error("Error stopping the StatefulPeriodicRandomNumberAugmentationFunction: {}", e.getMessage());
            throw new AugmentationFunctionException(String.format("Error stopping the StatefulPeriodicRandomNumberAugmentationFunction error: %s", e.getLocalizedMessage()));
        }
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        try {

            logger.debug("Received state update: {}", digitalTwinState);

            Long startTimestamp = System.currentTimeMillis();
            // React to the new State generating a new random number
            AugmentationFunctionResult<Double> result = generateNewAugmentationFunctionResult();
            Long endTimestamp = System.currentTimeMillis();

            AugmentationFunctionResultMetrics augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(startTimestamp, endTimestamp);

            // Add the new value to the list of results
            this.lastResultList.add(result);

            // Compute the average value of obtained random results
            double averageRandomNumber = this.lastResultList.stream()
                    .filter(r -> r.getType() == AugmentationFunctionResultType.GENERIC_RESULT)
                    .mapToDouble(r -> (Double) r.getValue())
                    .average()
                    .orElse(0.0);

            // Create a new AugmentationFunctionResult with the average value
            AugmentationFunctionResult<Double> averageResult = new AugmentationFunctionResult<>(
                    AugmentationFunctionResultType.GENERIC_RESULT,
                    "averageRandomNumber",
                    averageRandomNumber,
                    augmentationFunctionResultMetrics,
                    null
            );

            // Notify the result to the handler with the two new results
            this.notifyResult(Arrays.asList(result, averageResult));

        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

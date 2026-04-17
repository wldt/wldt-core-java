package it.wldt.augmentation.stateless.function;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.List;

public class RandomNumberAugmentationFunction extends StatelessAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(RandomNumberAugmentationFunction.class);

    public static final String FUNCTION_ID = "random-number-augmentation-function";

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public RandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "Random Number Augmentation Function",
                "This augmentation function generates a random number between 0 and 100.",
                "1.0.0");
    }

    @Override
    public AugmentationFunctionResultList run(AugmentationFunctionRequest request) throws AugmentationFunctionException {

        Long startTimestamp = System.currentTimeMillis();
        // Generate a random number between 0 and 1
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

        logger.debug("RandomNumberAugmentationFunction -> Generated random number: {}", result);

        return new AugmentationFunctionResultList(result);
    }
}

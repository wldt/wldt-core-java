package it.wldt.augmentation.function;

import it.wldt.augmentation.*;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.List;

public class RandomNumberAugmentationFunction extends AugmentationFunction {

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
                "1.0.0",
                AugmentationFunctionType.STATELESS,
                new AugmentationFunctionContextRequest(true, true, null));
    }

    /**
     * Constructor of the AugmentationFunction class with all the parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param description the description of the augmentation function
     * @param version the version of the augmentation function
     * @param type the type of the augmentation function
     * @param contextRequest the context request of the augmentation function
     */
    public RandomNumberAugmentationFunction(String id, String name, String description, String version, AugmentationFunctionType type, AugmentationFunctionContextRequest contextRequest) {
        super(id, name, description, version, type, contextRequest);
    }

    @Override
    protected List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) throws AugmentationFunctionException {
        // Generate a random number between 0 and 1
        double randomNumber = Math.random();

        // Create an AugmentationFunctionResult with the random number
        AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                "randomNumber",
                randomNumber,
                null
        );

        logger.debug("RandomNumberAugmentationFunction -> Generated random number: {}", result);

        return Collections.singletonList(result);
    }
}

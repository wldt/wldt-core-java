package it.wldt.augmentation.stateless.function;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.List;

public class RandomStringAugmentationFunction extends StatelessAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(RandomStringAugmentationFunction.class);

    public static final String FUNCTION_ID = "random-string-augmentation-function";

    public static final int RANDOM_STRING_LENGTH = 10;

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public RandomStringAugmentationFunction() {
        super(FUNCTION_ID,
                "Random String Augmentation Function",
                "This augmentation function generates a random String of target length.",
                "1.0.0");
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            randomString.append(characters.charAt(index));
        }
        return randomString.toString();
    }

    @Override
    public List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) throws AugmentationFunctionException {
        // Generate a random string of fixed length (e.g., 10 characters)
        String randomString = generateRandomString(RANDOM_STRING_LENGTH);

        logger.debug("RandomNumberAugmentationFunction -> Generated random String: {}", randomString);

        AugmentationFunctionResult<String> result = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                "randomString",
                randomString,
                null
        );

        return Collections.singletonList(result);
    }
}

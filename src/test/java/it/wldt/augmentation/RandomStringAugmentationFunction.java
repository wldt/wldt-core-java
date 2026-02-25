package it.wldt.augmentation;

import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.List;

public class RandomStringAugmentationFunction extends AugmentationFunction{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(RandomStringAugmentationFunction.class);

    public static final String RANDOM_NUMBER_AUGMENTATION_FUNCTION_ID = "random-string-augmentation-function";

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
        super(RANDOM_NUMBER_AUGMENTATION_FUNCTION_ID,
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
    public RandomStringAugmentationFunction(String id, String name, String description, String version, AugmentationFunctionType type, AugmentationFunctionContextRequest contextRequest) {
        super(id, name, description, version, type, contextRequest);
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
    protected List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) throws AugmentationFunctionException {
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

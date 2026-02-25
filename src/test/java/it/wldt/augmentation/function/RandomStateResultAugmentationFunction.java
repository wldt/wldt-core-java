package it.wldt.augmentation.function;

import it.wldt.augmentation.*;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.ArrayList;
import java.util.List;

public class RandomStateResultAugmentationFunction extends AugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(RandomStateResultAugmentationFunction.class);

    public static final String FUNCTION_ID = "random-state-result-function";

    public static final int RANDOM_STRING_LENGTH = 10;

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public RandomStateResultAugmentationFunction() {
        super(FUNCTION_ID,
                "Randome State Result Augmentation Function",
                "This augmentation function generates a group of State variables with random values, including a random string property, a random number event, a random string relationship, and a random string relationship instance.",
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
    public RandomStateResultAugmentationFunction(String id, String name, String description, String version, AugmentationFunctionType type, AugmentationFunctionContextRequest contextRequest) {
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

    private static double generateRandomNumber() {
        return Math.random();
    }

    @Override
    protected List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) throws AugmentationFunctionException {

        // Empty List for Augmentation Function Result
        List<AugmentationFunctionResult<?>> results = new ArrayList<>();

        // Create an example of Property Result
        AugmentationFunctionResult<String> propertyResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.PROPERTY_RESULT,
                "randomStringProperty",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        );

        // Add the Property Result to the results list
        results.add(propertyResult);

        // Create an example of Event Result
        AugmentationFunctionResult<Double> eventResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.EVENT_RESULT,
                "randomNumberEvent",
                generateRandomNumber(),
                null
        );

        // Add the Event Result to the results list
        results.add(eventResult);

        // Create a new Relationship Type
        AugmentationFunctionResult<String> relationshipResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_RESULT,
                "randomStringRelationship",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        );

        // Add the Relationship Type Result to the results list
        results.add(relationshipResult);

        // Create a Relationship Instance Result
        AugmentationFunctionResult<String> relationshipInstanceResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_INSTANCE_RESULT,
                "randomStringRelationshipInstance",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        );

        // Add the Relationship Instance Result to the results list
        results.add(relationshipInstanceResult);

        // Return the list of results
        return results;

    }
}

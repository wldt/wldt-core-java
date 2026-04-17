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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RandomStateResultAugmentationFunction extends StatelessAugmentationFunction {

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

    private static double generateRandomNumber() {
        return Math.random();


    }

    @Override
    public AugmentationFunctionResultList run(AugmentationFunctionRequest request) throws AugmentationFunctionException {

        // Empty List for Augmentation Function Result
        AugmentationFunctionResultList results = new AugmentationFunctionResultList();

        long startTimestamp = System.currentTimeMillis();
        String randomString = generateRandomString(RANDOM_STRING_LENGTH);
        long endTimestamp = System.currentTimeMillis();
        AugmentationFunctionResultMetrics augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );
        // Create an example of Property Result
        AugmentationFunctionResult<String> propertyResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.PROPERTY_RESULT,
                "randomStringProperty",
                randomString,
                augmentationFunctionResultMetrics,
                null
        );

        // Add the Property Result to the results list
        results.add(propertyResult);


        startTimestamp = System.currentTimeMillis();
        double randomNumber = generateRandomNumber();
        endTimestamp = System.currentTimeMillis();
        augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );
        // Create an example of Event Result
        AugmentationFunctionResult<Double> eventResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.EVENT_RESULT,
                "randomNumberEvent",
                randomNumber,
                augmentationFunctionResultMetrics,
                null
        );

        // Add the Event Result to the results list
        results.add(eventResult);

        startTimestamp = System.currentTimeMillis();
        randomString = generateRandomString(RANDOM_STRING_LENGTH);
        endTimestamp = System.currentTimeMillis();
        augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );
        // Create a new Relationship Type
        AugmentationFunctionResult<String> relationshipResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_RESULT,
                "randomStringRelationship",
                randomString,
                augmentationFunctionResultMetrics,
                null
        );

        // Add the Relationship Type Result to the results list
        results.add(relationshipResult);

        startTimestamp = System.currentTimeMillis();
        randomString = generateRandomString(RANDOM_STRING_LENGTH);
        endTimestamp = System.currentTimeMillis();
        augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );
        // Create a Relationship Instance Result
        AugmentationFunctionResult<String> relationshipInstanceResult = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_INSTANCE_RESULT,
                "randomStringRelationshipInstance",
                randomString,
                augmentationFunctionResultMetrics,
                null
        );

        // Add the Relationship Instance Result to the results list
        results.add(relationshipInstanceResult);

        // Return the list of results
        return results;

    }
}

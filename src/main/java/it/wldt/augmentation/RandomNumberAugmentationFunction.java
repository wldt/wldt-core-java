package it.wldt.augmentation;

import it.wldt.exception.AugmentationFunctionException;

import java.util.Collections;
import java.util.List;

public class RandomNumberAugmentationFunction extends AugmentationFunction{

    /**
     * * Constructor of the AugmentationFunction class with minimum parameters.
     * *
     * * @param id the unique id of the augmentation function
     * * @param name the name of the augmentation function
     * * @param type the type of the augmentation function
     *
     */
    public RandomNumberAugmentationFunction(String id, String name, AugmentationFunctionType type) {
        super(id, name, type);
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

        return Collections.singletonList(result);
    }
}

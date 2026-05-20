package it.wldt.augmentation.stateless.function;

import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.exception.AugmentationFunctionException;

public class ThrowingStatelessAugmentationFunction extends StatelessAugmentationFunction {

    // Same ID as RandomNumberAugmentationFunction so GenericResultAugmentationDigitalTwinModel finds it
    public static final String FUNCTION_ID = RandomNumberAugmentationFunction.FUNCTION_ID;

    public ThrowingStatelessAugmentationFunction() {
        super(FUNCTION_ID,
                "Throwing Stateless Augmentation Function",
                "Always throws an exception — used for error-path metric tests.",
                "1.0.0");
    }

    @Override
    protected AugmentationFunctionResultList run(AugmentationFunctionRequest request) throws AugmentationFunctionException {
        throw new AugmentationFunctionException("Intentional test error from ThrowingStatelessAugmentationFunction");
    }
}

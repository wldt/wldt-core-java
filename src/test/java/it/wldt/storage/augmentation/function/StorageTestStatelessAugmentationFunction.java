package it.wldt.storage.augmentation.function;

import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultMetrics;
import it.wldt.augmentation.result.AugmentationFunctionResultType;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Collections;
import java.util.List;

public class StorageTestStatelessAugmentationFunction extends StatelessAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageTestStatelessAugmentationFunction.class);

    public static final String FUNCTION_ID = "storage-test-stateless-function";

    public static final String RESULT_KEY = "storage-test-result-key";

    public static final String RESULT_VALUE = "storage-test-result-value";

    public StorageTestStatelessAugmentationFunction() {
        super(FUNCTION_ID,
                "Storage Test Stateless Augmentation Function",
                "Stateless augmentation function for storage testing. Produces one GENERIC_RESULT.",
                "1.0.0");
    }

    @Override
    protected List<AugmentationFunctionResult<?>> run(AugmentationFunctionRequest request) throws AugmentationFunctionException {

        Long startTimestamp = System.currentTimeMillis();
        Long endTimestamp = System.currentTimeMillis();

        AugmentationFunctionResultMetrics augmentationFunctionResultMetrics = new AugmentationFunctionResultMetrics(
                startTimestamp,
                endTimestamp
        );

        AugmentationFunctionResult<String> result = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                RESULT_KEY,
                RESULT_VALUE,
                augmentationFunctionResultMetrics,
                null
        );

        logger.debug("StorageTestStatelessAugmentationFunction -> Produced result with key: {} value: {}", RESULT_KEY, RESULT_VALUE);

        return Collections.singletonList(result);
    }
}


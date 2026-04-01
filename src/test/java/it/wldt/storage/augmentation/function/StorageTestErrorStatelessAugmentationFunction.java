package it.wldt.storage.augmentation.function;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.error.AugmentationFunctionErrorType;
import it.wldt.augmentation.function.StatelessAugmentationFunction;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.ArrayList;
import java.util.List;

public class StorageTestErrorStatelessAugmentationFunction extends StatelessAugmentationFunction {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StorageTestErrorStatelessAugmentationFunction.class);

    public static final String FUNCTION_ID = "storage-test-error-stateless-function";

    public static final String ERROR_MESSAGE = "Storage test stateless error";

    public static final AugmentationFunctionErrorType ERROR_TYPE = AugmentationFunctionErrorType.ERROR;

    public StorageTestErrorStatelessAugmentationFunction() {
        super(FUNCTION_ID,
                "Storage Test Error Stateless Augmentation Function",
                "Stateless augmentation function for storage error testing. Emits one error.",
                "1.0.0");
    }

    @Override
    protected List<AugmentationFunctionResult<?>> run(AugmentationFunctionRequest request) throws AugmentationFunctionException {

        AugmentationFunctionError error = new AugmentationFunctionError(ERROR_TYPE, ERROR_MESSAGE);
        notifyError(error);

        logger.debug("StorageTestErrorStatelessAugmentationFunction -> Notified error: type={}, message={}", ERROR_TYPE, ERROR_MESSAGE);

        return new ArrayList<>();
    }
}


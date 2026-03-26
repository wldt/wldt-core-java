package it.wldt.augmentation.listener;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.storage.query.QueryRequest;

import java.util.List;

/**
 * Interface representing a listener for stateful augmentation functions. This listener is designed to handle events
 * related to the execution of stateful augmentation functions, such as receiving results, handling errors, and
 * refreshing query results. Implementing this interface allows for better management of the execution flow of
 * stateful augmentation functions, enabling the system to respond appropriately to different outcomes of the
 * function execution, such as successful completion, errors, or the need to refresh query results based on changes
 * in the underlying data or context.
 */
public interface StatefulAugmentationListener {

    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList);

    public void onStatefulAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError);

    public void onStatefulAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, QueryRequest queryRequest);
}

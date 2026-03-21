package it.wldt.augmentation.listener;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.storage.query.QueryRequest;

import java.util.List;

public interface StatefulAugmentationListener {

    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList);

    public void onStatefulAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError);

    public void onStatefulAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, QueryRequest queryRequest);
}

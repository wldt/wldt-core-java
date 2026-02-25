package it.wldt.augmentation.listener;

import it.wldt.augmentation.result.AugmentationFunctionResult;

import java.util.List;

public interface StatefulAugmentationResultListener {

    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList);

}

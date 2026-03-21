package it.wldt.augmentation.listener;

import it.wldt.augmentation.error.AugmentationFunctionError;

public interface StatelessAugmentationListener {

    public void onStatelessAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError);

}

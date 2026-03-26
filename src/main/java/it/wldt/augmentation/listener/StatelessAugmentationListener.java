package it.wldt.augmentation.listener;

import it.wldt.augmentation.error.AugmentationFunctionError;

/**
 * Interface representing a listener for stateless augmentation functions. This listener is designed to handle events
 * related to the execution of stateless augmentation functions, specifically focusing on handling errors that may occur
 * during the execution process.
 */
public interface StatelessAugmentationListener {

    public void onStatelessAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError);

}

package it.wldt.storage;

import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.process.shadowing.DemoDigitalTwinModel;

import java.util.List;

/**
 * Custom DigitalTwinModel for augmentation storage tests.
 * Extends DemoDigitalTwinModel and adds logic to execute/start augmentation functions
 * when they are dynamically registered (after DT start).
 *
 * - Stateless functions are executed immediately via executeAugmentationFunction()
 * - Stateful functions are started immediately via startAugmentationFunction()
 */
public class AugmentationStorageDigitalTwinModel extends DemoDigitalTwinModel {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(AugmentationStorageDigitalTwinModel.class);

    public AugmentationStorageDigitalTwinModel() {
        super();
    }

    /**
     * Called when a new augmentation function is dynamically registered after DT start.
     * Automatically triggers execution (for stateless) or start (for stateful).
     */
    @Override
    protected void onAugmentationNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        try {
            logger.info("AugmentationStorageDigitalTwinModel -> New Augmentation Function Available: handlerId={}, functionId={}, type={}",
                    handlerId, augmentationFunction.getId(), augmentationFunction.getType());

            if (augmentationFunction.getType() == AugmentationFunctionType.STATELESS) {
                logger.info("Executing stateless augmentation function: {}", augmentationFunction.getId());
                this.executeAugmentationFunction(augmentationFunction.getId());
            } else if (augmentationFunction.getType() == AugmentationFunctionType.STATEFUL) {
                logger.info("Starting stateful augmentation function: {}", augmentationFunction.getId());
                this.startAugmentationFunction(augmentationFunction.getId());
            }
        } catch (Exception e) {
            logger.error("Error handling new augmentation function: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Called at DT Model startup to notify all Augmentation Functions already registered.
     * Automatically triggers execution (for stateless) or start (for stateful).
     */
    @Override
    protected void onAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList) {
        try {
            logger.info("AugmentationStorageDigitalTwinModel -> Augmentation Function List Available: handlerId={}, count={}",
                    handlerId, augmentationFunctionList.size());

            for (AugmentationFunction augmentationFunction : augmentationFunctionList) {
                if (augmentationFunction.getType() == AugmentationFunctionType.STATELESS) {
                    logger.info("Executing stateless augmentation function from list: {}", augmentationFunction.getId());
                    this.executeAugmentationFunction(augmentationFunction.getId());
                } else if (augmentationFunction.getType() == AugmentationFunctionType.STATEFUL) {
                    logger.info("Starting stateful augmentation function from list: {}", augmentationFunction.getId());
                    this.startAugmentationFunction(augmentationFunction.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error handling augmentation function list: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}


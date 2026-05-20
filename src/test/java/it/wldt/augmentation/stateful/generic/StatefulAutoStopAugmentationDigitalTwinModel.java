package it.wldt.augmentation.stateful.generic;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.augmentation.result.AugmentationFunctionResultList;
import it.wldt.augmentation.stateful.function.StatefulPeriodicRandomNumberAugmentationFunction;
import it.wldt.core.model.DigitalTwinModel;
import it.wldt.core.state.*;
import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.utils.SharedTestMetrics;
import it.wldt.process.physical.DemoPhysicalAdapter;

import java.util.List;
import java.util.Map;

/**
 * Variant of StatefulGenericResultAugmentationDigitalTwinModel that automatically calls
 * stopAugmentationFunction() after receiving the first result. Used to test DT Model level
 * stop metrics (AF_STATEFUL_STOP_SUCCESS_COUNT) without manual test intervention.
 */
public class StatefulAutoStopAugmentationDigitalTwinModel extends DigitalTwinModel {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatefulAutoStopAugmentationDigitalTwinModel.class);

    private boolean isShadowed = false;
    private boolean stopCalled = false;
    private final boolean autoStop;

    /**
     * @param autoStop when true, calls stopAugmentationFunction() after receiving the first result.
     *                 Use false when only the shadowing/start/state-update/callback metrics are under test.
     */
    public StatefulAutoStopAugmentationDigitalTwinModel(boolean autoStop) {
        super("dummy-shadowing-function-auto-stop");
        this.autoStop = autoStop;
    }

    public StatefulAutoStopAugmentationDigitalTwinModel() {
        this(false);
    }

    @Override
    protected void onCreate() {
        logger.debug("Shadowing Function - onCreate()");
    }

    @Override
    protected void onStart() {
        logger.debug("Shadowing Function - onStart()");
    }

    @Override
    protected void onStop() {
        logger.debug("Shadowing Function - onStop()");
    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        try {
            logger.debug("StatefulAutoStopAugmentationDigitalTwinModel - onDigitalTwinBound()");
            if (!isShadowed) {
                isShadowed = true;
                for (Map.Entry<String, PhysicalAssetDescription> entry : adaptersPhysicalAssetDescriptionMap.entrySet()) {
                    PhysicalAssetDescription pad = entry.getValue();
                    if (pad.getProperties() != null && !pad.getProperties().isEmpty()) {
                        for (PhysicalAssetProperty<?> prop : pad.getProperties()) {
                            this.digitalTwinStateManager.startStateTransaction();
                            this.digitalTwinStateManager.createProperty(
                                    new DigitalTwinStateProperty<>(prop.getKey(), prop.getInitialValue()));
                            this.digitalTwinStateManager.commitStateTransaction();
                            this.observePhysicalAssetProperty(prop);
                        }
                    }
                    if (pad.getEvents() != null && !pad.getEvents().isEmpty())
                        this.observePhysicalAssetEvents(pad.getEvents());
                }
                notifyShadowingSync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {}

    @Override
    protected void onPhysicalAdapterBidingUpdate(String adapterId, PhysicalAssetDescription adapterPhysicalAssetDescription) {}

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {
        try {
            if (physicalPropertyEventMessage != null
                    && physicalPropertyEventMessage.getPhysicalPropertyId() != null
                    && this.digitalTwinStateManager.getDigitalTwinState().containsProperty(physicalPropertyEventMessage.getPhysicalPropertyId())) {
                this.digitalTwinStateManager.startStateTransaction();
                this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(
                        physicalPropertyEventMessage.getPhysicalPropertyId(),
                        physicalPropertyEventMessage.getBody()));
                this.digitalTwinStateManager.commitStateTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {}

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipWldtEvent) {}

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipWldtEvent) {}

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {}

    @Override
    protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId,
                                                     String augmentationFunctionId,
                                                     AugmentationFunctionResultList augmentationFunctionResult) {
        SharedTestMetrics.getInstance().addAugmentationFunctionResultNotification(
                this.digitalTwinStateManager.getDigitalTwinId(),
                augmentationFunctionHandlerId,
                augmentationFunctionId,
                augmentationFunctionResult);

        for (AugmentationFunctionResult<?> result : augmentationFunctionResult)
            logger.info("Augmentation Function Result Received: {}", result);

        // Auto-stop after first result — only when constructed with autoStop=true
        if (autoStop && !stopCalled) {
            stopCalled = true;
            try {
                this.stopAugmentationFunction(augmentationFunctionId);
            } catch (Exception e) {
                logger.error("Error calling stopAugmentationFunction: {}", e.getLocalizedMessage());
            }
        }
    }

    @Override
    protected void onAugmentationNewFunctionAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        SharedTestMetrics.getInstance().addAugmentationFunctionRegistrationCallback(
                this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
    }

    @Override
    protected void onAugmentationFunctionUnAvailable(String handlerId, AugmentationFunction augmentationFunction) {
        SharedTestMetrics.getInstance().addAugmentationFunctionUnRegistrationCallback(
                this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
    }

    @Override
    protected void onAugmentationFunctionListAvailable(String handlerId, List<AugmentationFunction> augmentationFunctionList) {
        try {
            for (AugmentationFunction augmentationFunction : augmentationFunctionList) {
                SharedTestMetrics.getInstance().addAugmentationFunctionRegistrationCallback(
                        this.digitalTwinStateManager.getDigitalTwinId(), handlerId, augmentationFunction);
                if (augmentationFunction.getId().equals(StatefulPeriodicRandomNumberAugmentationFunction.FUNCTION_ID)
                        && augmentationFunction.getType().equals(it.wldt.augmentation.function.AugmentationFunctionType.STATEFUL))
                    this.startAugmentationFunction(StatefulPeriodicRandomNumberAugmentationFunction.FUNCTION_ID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAugmentationFunctionError(String handlerId, String functionId, AugmentationFunctionError augmentationFunctionError) {
        logger.error("Augmentation Function Error - HandlerId: {} AugmentationFunctionError: {}", handlerId, augmentationFunctionError);
        SharedTestMetrics.getInstance().addAugmentationFunctionErrorNotification(
                this.digitalTwinStateManager.getDigitalTwinId(), handlerId, functionId, augmentationFunctionError);
    }
}

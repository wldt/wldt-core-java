package it.wldt.core.event;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;

public class EventManager {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    /**
     *
     * @param digitalTwinId
     * @param publisherId
     * @param event
     * @throws EventBusException
     */
    private static void publishEvent(String digitalTwinId,
                                          String publisherId,
                                          WldtEvent<?> event) throws EventBusException {

        WldtEventBus.getInstance().publishEvent(digitalTwinId, publisherId, event);
    }

    /**
     *
     * @param digitalTwinId
     * @param publisherId
     * @param adapterId
     * @param physicalAssetDescription
     */
    public static void notifyPhysicalAdapterPadAvailable(String digitalTwinId,
                                                         String publisherId,
                                                         String adapterId,
                                                         PhysicalAssetDescription physicalAssetDescription){

        String targetEventType = WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_AVAILABLE;

        try {

            WldtEvent<PhysicalAssetDescription> event = new WldtEvent<>(
                    targetEventType,
                    physicalAssetDescription,
                    new HashMap<String, Object>() {{
                        put(WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_EVENT_METADATA_ADAPTER_ID, adapterId);
                    }});

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing PAD Available Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param digitalTwinId
     * @param publisherId
     * @param adapterId
     * @param physicalAssetDescription
     */
    public static void notifyPhysicalAdapterPadUpdated(String digitalTwinId,
                                                         String publisherId,
                                                         String adapterId,
                                                         PhysicalAssetDescription physicalAssetDescription){

        String targetEventType = WldtEventTypes.PHYSICAL_ASSET_DESCRIPTION_UPDATED;

        try {

            WldtEvent<PhysicalAssetDescription> event = new WldtEvent<>(
                    targetEventType,
                    physicalAssetDescription,
                    new HashMap<String, Object>() {{
                        put("adapter_id", adapterId);
                    }});

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing PAD Updated Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param digitalTwinId
     * @param publisherId
     * @param lifeCycleState
     */
    public static void publishLifeCycleEvent(String digitalTwinId,
                                             String publisherId,
                                             LifeCycleState lifeCycleState){

        String targetEventType = WldtEventTypes.DT_LIFE_CYCLE_EVENT_TYPE;

        try {

            WldtEvent<String> event = new WldtEvent<>(
                    targetEventType,
                    lifeCycleState.getValue());

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing Life Cycle Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }

    }

}

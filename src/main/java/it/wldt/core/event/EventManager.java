package it.wldt.core.event;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.exception.EventBusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;

public class EventManager {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    private static void publishEvent(String digitalTwinId,
                                          String publisherId,
                                          WldtEvent<?> event) throws EventBusException {

        WldtEventBus.getInstance().publishEvent(digitalTwinId, publisherId, event);
    }

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
                        put("adapter_id", adapterId);
                    }});

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing PAD Available Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

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
            logger.error("Error Publishing Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

}

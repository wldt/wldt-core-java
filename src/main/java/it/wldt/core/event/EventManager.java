/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.core.event;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleState;
import it.wldt.exception.EventBusException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;
import java.util.HashMap;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Project: WLDT Framework
 * Event Manager class used centralize and simplify the event management in the WLDT Framework.
 * The Event Manager class provides a set of static methods to publish events associated to a target digital twin
 * and publisher (e.g., the physical adapter of the twin).
 */
public class EventManager {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(EventManager.class);

    /**
     * Publishes an Event associated to a target digital twin and publisher
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param event Event to be published
     * @throws EventBusException EventBus Exception in case of error
     */
    private static void publishEvent(String digitalTwinId,
                                          String publisherId,
                                          WldtEvent<?> event) throws EventBusException {

        WldtEventBus.getInstance().publishEvent(digitalTwinId, publisherId, event);
    }

    /**
     * Notifies that a new Physical Asset Description (PAD) is available from a Physical Adapter associated
     * to a Digital Twin. The notification generates a Physical Asset Description Available Event.
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param adapterId Adapter Id
     * @param physicalAssetDescription Physical Asset Description
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
     * Publishes a Physical Asset Description Updated Event associated to a target digital twin and publisher
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param adapterId Adapter Id
     * @param physicalAssetDescription Physical Asset Description
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
     * Publishes a Life Cycle Event associated to a target digital twin and publisher
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param lifeCycleState Life Cycle State
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

    /**
     * Publishes a Storage Query Request Event
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param request Query Request
     */
    public static void publishStorageQueryRequest(String digitalTwinId,
                                                  String publisherId,
                                                  QueryRequest request){

        String targetEventType = WldtEventTypes.STORAGE_QUERY_REQUEST_EVENT_TYPE;

        try {

            WldtEvent<QueryRequest> event = new WldtEvent<>(
                    targetEventType,
                    request);

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing Storage Query Request Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

    /**
     * Publishes a Storage Query Result Event
     * @param digitalTwinId Digital Twin Id
     * @param publisherId Publisher Id
     * @param result Query Result
     */
    public static void publishStorageQueryResult(String digitalTwinId,
                                                 String publisherId,
                                                 QueryResult<?> result){

        String targetEventType = String.format("%s.%s",
                WldtEventTypes.STORAGE_QUERY_RESULT_EVENT_TYPE,
                result.getOriginalRequest().getRequestId());

        try {

            WldtEvent<QueryResult<?>> event = new WldtEvent<>(
                    targetEventType,
                    result);

            publishEvent(digitalTwinId, publisherId, event);

        }catch (Exception e){
            logger.error("Error Publishing Storage Query Result Event ! DT-Id: {} Event-Type: {} Error: {}",
                    digitalTwinId,
                    targetEventType,
                    e.getLocalizedMessage());
        }
    }

}

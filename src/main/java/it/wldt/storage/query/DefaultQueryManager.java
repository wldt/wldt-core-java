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
package it.wldt.storage.query;

import it.wldt.exception.StorageException;
import it.wldt.storage.WldtStorage;
import it.wldt.storage.model.state.DigitalTwinStateRecord;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * Default Query Manager Class
 * This class is responsible to manage the query request received by the Query Engine.
 * The class is responsible to handle the query request and return the query result to the caller.
 * This class extends the QueryManager class and implements the default behavior for the query management.
 */
public class DefaultQueryManager extends QueryManager{

    public DefaultQueryManager() {
        super();
    }

    /**
     * The method has been designed to return the desired storage object from the storage map to be used for the query management.
     * Recall the default implementation of the method for the query manager.
     * In the default implementation, the method returns the first storage object available in the storage map.
     *
     * The behavior can be changed by overriding the method in the custom query manager implementation.
     * In the custom implementation, the method can be used to select different storage according to the query.
     *
     * @param queryRequest Query Request Object, that can be used to select different storage according to the query
     * @param storageMap   Storage Map containing the storage objects
     * @return Desired Storage Object to be used for the query management
     */
    @Override
    public Optional<WldtStorage> getTargetStorage(QueryRequest queryRequest, Map<String, WldtStorage> storageMap) {
        return super.getTargetStorage(queryRequest, storageMap);
    }

    /**
     * Handle Life Cycle Event Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handleLifeCycleEventQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.LAST_VALUE)){
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(wldtStorage.getLastLifeCycleState()),
                        1);
            }
            else if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Life Cycle Event in the Time Range
                List<?> result = wldtStorage.getLifeCycleStateInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Life Cycle Event in the Sample Range
                List<?> result = wldtStorage.getLifeCycleStateInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Life Cycle Event Count
                int result = wldtStorage.getLifeCycleStateCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Life Cycle Event Query Request Type !");

        } catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Physical Relationship Instance Deleted Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handlePhysicalRelationshipInstanceDeletedNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Physical Relationship Instance Deleted Notification in the Time Range
                List<?> result = wldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Physical Relationship Instance Deleted Notification in the Sample Range
                List<?> result = wldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Physical Relationship Instance Deleted Notification Count
                int result = wldtStorage.getPhysicalAssetRelationshipInstanceDeletedNotificationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Physical Relationship Instance Deleted Notification Query Request Type !");

        } catch (Exception e) {
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Physical Relationship Instance Created Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handlePhysicalRelationshipInstanceCreatedNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Physical Relationship Instance Created Notification in the Time Range
                List<?> result = wldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Physical Relationship Instance Created Notification in the Sample Range
                List<?> result = wldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Physical Relationship Instance Created Notification Count
                int result = wldtStorage.getPhysicalAssetRelationshipInstanceCreatedNotificationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Physical Relationship Instance Created Notification Query Request Type !");

        } catch (Exception e) {
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Updated Pad Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handleUpdatedPadNotification(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Updated Pad Notification in the Time Range
                List<?> result = wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Updated Pad Notification in the Sample Range
                List<?> result = wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Updated Pad Notification Count
                int result = wldtStorage.getUpdatedPhysicalAssetDescriptionNotificationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Updated Pad Notification Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle New Pad Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handleNewPadNotification(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the New Pad Notification in the Time Range
                List<?> result = wldtStorage.getNewPhysicalAssetDescriptionNotificationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the New Pad Notification in the Sample Range
                List<?> result = wldtStorage.getNewPhysicalAssetDescriptionNotificationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the New Pad Notification Count
                int result = wldtStorage.getNewPhysicalAssetDescriptionNotificationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid New Pad Notification Query Request Type !");

        } catch (Exception e) {
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Digital Action Request Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handleDigitalActionRequestQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try{

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Digital Action Request in the Time Range
                List<?> result = wldtStorage.getDigitalActionRequestInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Digital Action Request in the Sample Range
                List<?> result = wldtStorage.getDigitalActionRequestInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Digital Action Request Count
                int result = wldtStorage.getDigitalActionRequestCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Digital Action Request Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Physical Action Request Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handlePhysicalActionRequestQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try {

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Physical Action Request in the Time Range
                List<?> result = wldtStorage.getPhysicalAssetActionRequestInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Physical Action Request in the Sample Range
                List<?> result = wldtStorage.getPhysicalAssetActionRequestInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Physical Action Request Count
                int result = wldtStorage.getPhysicalAssetActionRequestCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Physical Action Request Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Physical Asset Event Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handlePhysicalAssetEventNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try{

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Physical Asset Event Notification in the Time Range
                List<?> result = wldtStorage.getPhysicalAssetEventNotificationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Physical Asset Event Notification in the Sample Range
                List<?> result = wldtStorage.getPhysicalAssetEventNotificationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Physical Asset Event Notification Count
                int result = wldtStorage.getPhysicalAssetEventNotificationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Physical Asset Event Notification Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Physical Asset Property Variation Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handlePhysicalAssetPropertyVariationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        try{

            if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Physical Asset Property Variation in the Time Range
                List<?> result = wldtStorage.getPhysicalAssetPropertyVariationInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if(queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Physical Asset Property Variation in the Sample Range
                List<?> result = wldtStorage.getPhysicalAssetPropertyVariationInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Physical Asset Property Variation Count
                int result = wldtStorage.getPhysicalAssetPropertyVariationCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Physical Asset Property Variation Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Digital Twin State Query Request
     *
     * @param queryRequest Query Request Object
     * @param storage      Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     * @throws StorageException Storage Exception
     */
    @Override
    public QueryResult<?> handleStateQuery(QueryRequest queryRequest, WldtStorage storage) throws StorageException {
        try{

            if(queryRequest.getRequestType().equals(QueryRequestType.LAST_VALUE))
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(storage.getLastDigitalTwinState()),
                        1);
            else if(queryRequest.getRequestType().equals(QueryRequestType.TIME_RANGE)){

                // Get the Digital Twin State in the Time Range
                List<DigitalTwinStateRecord> result = storage.getDigitalTwinStateInTimeRange(
                        queryRequest.getStartTimestampMs(),
                        queryRequest.getEndTimestampMs());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.SAMPLE_RANGE)){

                // Get the Digital Twin State in the Sample Range
                List<DigitalTwinStateRecord> result = storage.getDigitalTwinStateInRange(
                        queryRequest.getStartIndex(),
                        queryRequest.getEndIndex());

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        result,
                        result.size());
            } else if (queryRequest.getRequestType().equals(QueryRequestType.COUNT)){

                // Get the Digital Twin State Count
                int result = storage.getDigitalTwinStateCount();

                // Return the Query Result
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(result),
                        1);

            } else
                return new QueryResult<>(queryRequest, false, "Invalid Digital Twin State Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Storage Stats Query
     *
     * @param queryRequest Query Request Object
     * @param storage      Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     * @throws StorageException Storage Exception
     */
    @Override
    public QueryResult<?> handleStorageStatsQuery(QueryRequest queryRequest, WldtStorage storage) throws StorageException {
        try {

            if (queryRequest.getRequestType().equals(QueryRequestType.LAST_VALUE)){
                return new QueryResult<>(queryRequest,
                        true,
                        null,
                        Collections.singletonList(storage.getStorageStats()),
                        1);
            } else
                return new QueryResult<>(queryRequest, false, "Invalid Digital Twin State Query Request Type !");

        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }
}

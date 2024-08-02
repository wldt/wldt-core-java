package it.wldt.storage.query;

import it.wldt.exception.StorageException;
import it.wldt.storage.WldtStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.Optional;

public class QueryManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryManager.class);

    /**
     * Default implementation of the method for the query manager.
     * The method has been designed to return the desired storage object from the storage map to be used for the query management.
     * In the default implementation, the method returns the first storage object available in the storage map.
     * @param queryRequest Query Request Object, that can be used to select different storage according to the query
     * @param storageMap Storage Map containing the storage objects
     * @return Desired Storage Object to be used for the query management
     */
    public Optional<WldtStorage> getTargetStorage(QueryRequest queryRequest, Map<String, WldtStorage> storageMap) {
        if(!storageMap.isEmpty()) {
            WldtStorage targetStorage = storageMap.entrySet().iterator().next().getValue();
            if(targetStorage != null)
                return Optional.of(targetStorage);
            else
                return Optional.empty();
        }
        else
            return Optional.empty();
    }

    /**
     * Handle Query Request allowing its management through the storage map and the associated storage objects
     *
     * @param queryRequest Query Request Object
     * @param storageMap   Storage Map containing the storage objects to be used for the query management
     */
    public QueryResult<?> handleQuery(QueryRequest queryRequest, Map<String, WldtStorage> storageMap) {

        try{

            logger.info("Handling Query Request: {}", queryRequest);
            Optional<WldtStorage> storageOptional = getTargetStorage(queryRequest, storageMap);

            if(storageOptional.isPresent()) {

                logger.info("Storage Available for the Query Request ! Storage Id: {}", storageOptional.get().getStorageId());

                // Digital Twin State Query Request
                if(queryRequest.getResourceType().equals(QueryResourceType.DIGITAL_TWIN_STATE))
                    return handleStateQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.DIGITAL_TWIN_STATE_CHANGE_LIST))
                    return handleStateChangeListQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION))
                    return handlePhysicalAssetPropertyVariationQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION))
                    return handlePhysicalAssetEventNotificationQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.PHYSICAL_ACTION_REQUEST))
                    return handlePhysicalActionRequestQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.DIGITAL_ACTION_REQUEST))
                    return handleDigitalActionRequestQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.NEW_PAD_NOTIFICATION))
                    return handleNewPadNotification(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.UPDATED_PAD_NOTIFICATION))
                    return handleUpdatedPadNotification(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION))
                    return handlePhysicalRelationshipInstanceCreatedNotificationQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION))
                    return handlePhysicalRelationshipInstanceDeletedNotificationQuery(queryRequest, storageOptional.get());
                else if(queryRequest.getResourceType().equals(QueryResourceType.LIFE_CYCLE_EVENT))
                    return handleLifeCycleEventQuery(queryRequest, storageOptional.get());
                else
                    return new QueryResult<>(queryRequest, false, "Invalid Query Request Type !");
            }
            else
                return new QueryResult<>(queryRequest, false, "No Storage Available for the Query Request !");
        }catch (Exception e){
            return new QueryResult<>(queryRequest, false, e.getMessage());
        }
    }

    /**
     * Handle Life Cycle Event Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handleLifeCycleEventQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Physical Relationship Instance Deleted Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handlePhysicalRelationshipInstanceDeletedNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Physical Relationship Instance Created Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handlePhysicalRelationshipInstanceCreatedNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Updated Pad Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handleUpdatedPadNotification(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle New Pad Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handleNewPadNotification(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Digital Action Request Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handleDigitalActionRequestQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException{
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }


    /**
     * Handle Physical Action Request Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handlePhysicalActionRequestQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException{
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Physical Asset Event Notification Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handlePhysicalAssetEventNotificationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException{
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Physical Asset Property Variation Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    private QueryResult<?> handlePhysicalAssetPropertyVariationQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException{
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Digital Twin State Change List Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    public QueryResult<?> handleStateChangeListQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

    /**
     * Handle Digital Twin State Query Request
     *
     * @param queryRequest Query Request Object
     * @param storage      Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     * @throws StorageException Storage Exception
     */
    public QueryResult<?> handleStateQuery(QueryRequest queryRequest, WldtStorage storage) throws StorageException{
        return new QueryResult<>(queryRequest, false, "Query not supported by the current implementation !");
    }

}

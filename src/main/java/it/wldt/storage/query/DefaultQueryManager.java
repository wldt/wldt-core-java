package it.wldt.storage.query;

import it.wldt.exception.StorageException;
import it.wldt.storage.WldtStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class DefaultQueryManager implements IQueryManager{

    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryManager.class);

    /**
     * Return the desired storage object from the storage map to be used for the query management
     * @param storageMap Storage Map containing the storage objects
     * @return Desired Storage Object to be used for the query management
     */
    private Optional<WldtStorage> getDesiredStorage(Map<String, WldtStorage> storageMap) {
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
    @Override
    public QueryResult<?> handleQuery(QueryRequest queryRequest, Map<String, WldtStorage> storageMap) {

        try{
            logger.info("Handling Query Request: {}", queryRequest);
            Optional<WldtStorage> storageOptional = getDesiredStorage(storageMap);

            if(storageOptional.isPresent()) {

                logger.info("Storage Available for the Query Request ! Storage Id: {}", storageOptional.get().getStorageId());

                // Digital Twin State Query Request
                if(queryRequest.getResourceType().equals(QueryResourceType.DIGITAL_TWIN_STATE))
                    return handleDigitalTwinStateQuery(queryRequest, storageOptional.get());
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
     * Handle Digital Twin State Query Request
     *
     * @param queryRequest Query Request Object
     * @param storage      Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     * @throws StorageException Storage Exception
     */
    public QueryResult<?> handleDigitalTwinStateQuery(QueryRequest queryRequest, WldtStorage storage) throws StorageException {

        if(queryRequest.getRequestType().equals(QueryRequestType.LAST_VALUE))
            return new QueryResult<>(queryRequest,
                    true,
                    null,
                    Collections.singletonList(storage.getLastDigitalTwinState()),
                    1);
        else
            return new QueryResult<>(queryRequest, false, "Invalid Digital Twin State Query Request Type !");
    }
}

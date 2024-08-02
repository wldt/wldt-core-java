package it.wldt.storage.query;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.exception.StorageException;
import it.wldt.storage.WldtStorage;
import it.wldt.storage.model.state.DigitalTwinStateRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DefaultQueryManager extends QueryManager{

    private static final Logger logger = LoggerFactory.getLogger(DefaultQueryManager.class);

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
     * Handle Digital Twin State Change List Query Request
     *
     * @param queryRequest Query Request Object
     * @param wldtStorage  Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     */
    @Override
    public QueryResult<?> handleStateChangeListQuery(QueryRequest queryRequest, WldtStorage wldtStorage) throws StorageException {
        return super.handleStateChangeListQuery(queryRequest, wldtStorage);
    }

    /**
     * Handle Digital Twin State Query Request
     *
     * @param queryRequest Query Request Object
     * @param storage      Storage Object to be used for the query management
     * @return Query Result Object containing the query result
     * @throws StorageException Storage Exception
     */
    public QueryResult<?> handleStateQuery(QueryRequest queryRequest, WldtStorage storage) throws StorageException {

        if(queryRequest.getRequestType().equals(QueryRequestType.LAST_VALUE))
            return new QueryResult<>(queryRequest,
                    true,
                    null,
                    Collections.singletonList(storage.getLastDigitalTwinStateVariation()),
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
    }
}

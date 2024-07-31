package it.wldt.storage.query;

import it.wldt.storage.WldtStorage;
import java.util.Map;

public interface IQueryManager {

    /**
     * Handle Query Request allowing its management through the storage map and the associated storage objects
     *
     * @param queryRequest
     * @param storageMap
     */
    public QueryResult<?> handleQuery(QueryRequest queryRequest, Map<String, WldtStorage> storageMap);

}

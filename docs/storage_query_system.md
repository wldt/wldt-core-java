# Storage Layer

A new storage layer has been integrated into the core WLDT library, enabling Digital Twins (DTs) to store data related to the evolution of their state, generated events, and any variations involving properties, events, actions, relationships, and life cycle. The Storage Layer consists of two main components:

- **Storage Manager**: This is the central component of the storage system, facilitating the structured and modular storage and retrieval of information. It allows developers to create and utilize various storage systems (e.g., in-memory, file-based, or DBMS) simultaneously. The Storage Layer is accessible in both read and write modes internally by the DT's Model, and in read-only mode via the Query System by Digital Adapters and Augmentation Functions.
- **Query System**: To delegate and encapsulate the responsibility of data storage within the DT's model, a query system has been integrated. This system enables Digital Adapters and Augmentation Functions to retrieve stored data (with filters, sorting, paging and aggregations) and use or expose it according to their specific logic and implementation.

The storage layer is designed for easy extension, allowing developers to create and share new storage layers (e.g., using Redis, MySQL, or MongoDB). The provided in-memory implementation serves _only_ for basic development and testing purposes. Similarly, the Query Manager can be extended and customized by developers to implement additional query management features or to enhance the default functionalities provided by the library.

## Storage Manager

The main module of the Storage Layer is the one associated to Storage Capabilities and it is composed by two main classes: `StorageManager` and `WldStorage` with the following characteristics and main methods:

**Note**: In the WLDT storage layer, event observation within the Digital Twin is linked not only to the asynchronous reception of data, but also to the actual persistence of that data within the target storage module.

- `StorageManager`: The StorageManager class is a class that represents the storage manager for a DigitalTwin.  It is responsible for managing the storage of the data related to the DigitalTwin. It is an observer of the `WldtEventBus`, and it is able to save the data in the available storages. The class extends a `DigitalTwinWorker`, in order to allow the component to work in a structure and integrated way on a different thread that the core of a DT can coordinate starting and stopping it when required. The manager allow the usage of different storage systems at the same time in order to allow the developers to memorize the information accordingly to their need in the right storage system at the same time (e.g., REDIS for quick cached information and MongDB for historical data). Main associated methods are:
    - `putStorage(WldtStorage storage)`: Add a new WldtStorage to the StorageManager
    - `getStorageIdList()`: Returns the list of id of the WldtStorage in the StorageManager
    - `isStorageAvailable(String storageId)`: Checks if a target Storage Id is available in the Storage Manager
    - `getStorage(String storageId)`: Get the target WldtStorage by id from the Storage Manager
    - `removeStorage(String storageId)`: Remove an existing WldtStorage by id from the StorageManager

- `WldtStorage`: Defines an abstract class allowing the Digital Twin developer to implement its internal storage system for the Digital Twin instance. The class is split in a **write side** (one `save*` method for each type of information) and a **read side** made of a **single entry point**, `execute(Query)`, that accepts any query built with the [Query System](#query-system) (see below).
    - The class defines methods for the management of:
        * Digital Twin State storage with the associated change list;
        * Digital Twin State Event Notification list;
        * Life Cycle State storage;
        * Physical Asset Description storage;
        * Physical Asset Property Variation storage;
        * Physical Asset Relationship Instance storage;
        * Digital Action Request storage;
        * Physical Asset Action Request storage;
        * Physical Asset Event Notification storage;
        * Augmentation Function Error, Request, Result, Registration and Unregistration storage;
        * Query execution on all the stored information through `execute(Query)`;
    * Each WldtStorage instance can be configured (using the right constructor method) to:
        * Observe all Wldt events (`stateEvents`,  `physicalAssetEvents`,  `physicalAssetActionEvents`,  `physicalAssetDescriptionEvents`,  `digitalActionEvents`,  `lifeCycleEvents`, `augmentationFunctionEvents`)
        * Filter only for specific class of events
        * Once the WldtStorage has been properly configured to receive target events the `StorageManager` automatically save information of interest for that specific storage. For example we can have a `StorageA` (e.g, REDIS) configured to receive all the generated events and a `StorageB` (e.g., MongoDB) in charge of saving only DT's state variation over time.
    * The default implementation of the `WldtStorage` is the class `DefaultWldtStorage`. This class provides a simple storage solution for digital twin states,  digital twin state changes, physical asset events, and digital twin events. The class provides **ONLY** a memory based approach: every type of information is kept, in arrival order, in a thread-safe list together with its storage timestamp (so several events generated in the same millisecond are **all** stored), and queries are evaluated in memory by the `InMemoryQueryEvaluator`. More advanced solutions should be implemented for production oriented Digital Twins, for example using external storage and memorization solutions (see [Implementing a Custom Storage](#implementing-a-custom-storage)).
    * Methods available and implemented by WldtStorage implementations are the following grouped by categories:
        * **Digital Twin State**:
            * `saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList)`: Save a new computed instance of the DT State in the Storage together with the list of the changes with respect  to the previous state
        * **Digital Twin State Event Notification**:
            * `saveDigitalTwinStateEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification)`: Save the Digital Twin State Event Notification
        * **Life Cycle State Variation**:
            * `saveLifeCycleState(LifeCycleStateVariation lifeCycleStateVariation)`: Save the LifeCycleState of the Digital Twin
        * **Physical Asset Event Notification**:
            * `savePhysicalAssetEventNotification(PhysicalAssetEventNotification physicalAssetEventNotification)`: Save the Physical Asset Event Notification
        * **Physical Action Request**:
            * `savePhysicalAssetActionRequest(PhysicalAssetActionRequest physicalAssetActionRequest)`: Save Physical Asset Action Request
        * **Digital Action Request**:
            * `saveDigitalActionRequest(DigitalActionRequest digitalActionRequest)`: Save a Digital Action Request
        * **Physical Asset Description (PAD) Notification**
            * `saveNewPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification)`: Save a new Physical Asset Description Available
            * `saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification)`: Save the updated Physical Asset Description Notification
        * **Physical Asset Property Variation**:
            * `savePhysicalAssetPropertyVariation(PhysicalAssetPropertyVariation physicalAssetPropertyVariation)`: Save the Physical Asset Property Variation
        * **Physical Asset Relationship Instance Notification**
            * `savePhysicalAssetRelationshipInstanceCreatedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation)`: Save the Physical Asset Relationship Instance Created Event
            * `savePhysicalAssetRelationshipInstanceDeletedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation)`: Save the Physical Asset Relationship Instance Deleted Event
        * **Augmentation Function**:
            * `saveAugmentationFunctionError(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionError augmentationFunctionError)`: Save the Augmentation Function Error
            * `saveAugmentationFunctionRequest(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionRequest augmentationFunctionRequest)`: Save the Augmentation Function Request
            * `saveAugmentationFunctionResult(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionResult<?> augmentationFunctionResult)`: Save the Augmentation Function Result
            * `saveAugmentationFunctionRegistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType)`: Save the Augmentation Function Registration
            * `saveAugmentationFunctionUnregistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType)`: Save the Augmentation Function Unregistration
        * **Reading data**:
            * `QueryResult<?> execute(Query query)`: Executes a [Query](#building-a-query) on the storage and returns the matching **records** (or the aggregated values). It is the only read method: counts, last values, time ranges, filters by key, sorting, paging and aggregations are all expressed through the `Query` object instead of dedicated `getXxx` methods.
            * `getStorageStats()`: Returns the number of stored records and the time range covered for each type of information


Some examples of usage for the Storage Layer are the following:

Lets' create a new Digital Twin with a single Storage in charge of *automatically* observe and store all the event generated and going through the target DT instance

```java
// Create the Digital Twin Engine
DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();  

// Create a new Digital Twin with a Demo Shadowing Function
DigitalTwin digitalTwin = new DigitalTwin(TEST_DIGITAL_TWIN_ID, new DemoShadowingFunction());

// Physical Adapter Configuration  
DemoPhysicalAdapter physicalAdapter = new DemoPhysicalAdapter(...);  
digitalTwin.addPhysicalAdapter(physicalAdapter);

// Digital Adapter Configuration
digitalAdapter = new DemoDigitalAdapter(...);  
digitalTwin.addDigitalAdapter(digitalAdapter);

// Create a new WldtStorage instance using the default implementation and observing all the events  
DefaultWldtStorage myStorage = new DefaultWldtStorage("test_storage", true)

// Add the new Default Storage Instance to the Digital Twin Storage Manager 
digitalTwin.getStorageManager().putStorage(myStorage);

// Add the Twin to the Engine  
digitalTwinEngine.addDigitalTwin(digitalTwin);  
  
// Start the Digital Twin  
digitalTwinEngine.startDigitalTwin(TEST_DIGITAL_TWIN_ID);
```

Now let's **suppose** to have two additional implementation of the WldtStorage class supporting Redis and MongDB and called `RedisWldtStorage` and `MongoDbWldtStorage`.
We would like to use Redis to *automatically* observe all the events and MongoDb only to store DT's state and life cycle variations.

```java
[...]

// Create a new RedisWldtStorage instance using the default implementation and observing all the events  
RedisWldtStorage myRedisStorage = new RedisWldtStorage("redis_storage", true);
myRedisStorage.setRedisConfiguration(myRedisConfiguration);

// Add the new Redis Storage Instance to the Digital Twin Storage Manager 
digitalTwin.getStorageManager().putStorage(myRedisStorage);

// Create a new MongoDbWldtStorage instance using the default implementation and observing only State and LifeCycle Events
MongoDbWldtStorage myMongoDbStorage = new MongoDbWldtStorage("mongo_db_storage", true, false, false, false, false, true, false);
myMongoDbStorage.setMongoDbConfiguration(myMongoDbConfiguration);

// Add the new MongoDb Storage Instance to the Digital Twin Storage Manager 
digitalTwin.getStorageManager().putStorage(myRedisStorage);

[...]
```

Within the `ShadowingFunction` it is possible to have the reference to the `StorageManager` in order to access available Storage in both reading and writing mode.
This is an example of how to retrieve an available WldtStorage through its id and the use it to read the Properties variations of the last 5 minutes:

```java
String TARGET_STORAGE_ID = "test_storage";

if(this.storageManager.isStorageAvailable(TARGET_STORAGE_ID)){

    // Access the Storage Manager to read from the target storage
    WldtStorage targetStorage = this.storageManager.getStorage(TARGET_STORAGE_ID);

    // Get the current time in milliseconds
    long endTime = System.currentTimeMillis();

    // Get the Time in the last 5 minutes
    long startTime = endTime - (5 * 60 * 1000);

    // Query the Physical Asset Property Variations of the last 5 minutes
    Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
            .where(and(gte(QueryFieldNames.TIMESTAMP, startTime), lte(QueryFieldNames.TIMESTAMP, endTime)))
            .build();

    QueryResult<?> queryResult = targetStorage.execute(query);

    if(queryResult.isSuccessful()) {
        for(PhysicalAssetPropertyVariationRecord propertyVariationRecord : queryResult.as(PhysicalAssetPropertyVariationRecord.class).getItems()){
            logger.info("Property Variation Record: {}", propertyVariationRecord);
            [...]
        }
    }
}
```


**Note**: The `StorageManager`, as previously described, can automatically store DT-related events based on the configuration and setup of each `WldtStorage` instance added to the manager. However, since the `ShadowingFunction` has direct access to the `StorageManager` in both read and write modes, manual handling of data storage is also possible. To achieve this, you can disable automatic storage by setting it to `false` for specific event types or for all event types. This allows you to manually manage the storage of information within the `ShadowingFunction`.

## Query System

Given the library's goal of maximizing modularity and decoupling responsibilities among the available components, the **Query System** has been introduced. This system allows components external to the core responsibilities of the Digital Twin (e.g., Digital Adapters and Augmentation Functions) to retrieve stored data and use or expose it according to their specific logic and implementation. For instance, an HTTP Digital Adapter could expose stored information about a DT's state variations over time, or a Monitoring Adapter could use available storage instances to retrieve events for a deeper understanding of the target DT instance's behavior. The query system has been implemented entirely through dedicated events in order to maximize the decoupling of the solution and supports at the same time both **synchronous** and **asynchronous** queries.

A query is a **declarative description** of what to read (resource, filter, sorting, paging, aggregation) that does not depend on the storage technology: the storage translates it into its own language (e.g., a SQL `SELECT`, a MongoDB `find`, or a plain Java evaluation for the in-memory storage) and executes it where the data are, so only the needed records are returned. The **output of a query is always made of records**: instances of the record classes of the storage (e.g., `PhysicalAssetPropertyVariationRecord`) or, for aggregations, `AggregationRecord`.

### How it works

```
Query ──► QueryExecutor ──(event)──► StorageManager ──► QueryManager ──► WldtStorage.execute(query)
                                                                                  │
QueryResult (records) ◄──(event)── StorageManager ◄───────────────────────────────┘
```

1. The caller (Digital Adapter, Augmentation Function, or any component) builds a `Query` and gives it to a `QueryExecutor`.
2. The `QueryExecutor` publishes the query on the event bus and waits for the result with the same request id (synchronously or through a listener).
3. The `StorageManager` receives the query and asks the `QueryManager` for the target storage (by default the first available storage, see [Query Manager](#query-manager)) and calls its `execute(Query)` method.
4. The `QueryResult` is published back on the event bus and delivered to the caller.

The classes of the Query System are organized in the package `it.wldt.storage.query`, where the main ones are directly available, and in three sub-packages grouping the classes serving the same scope:

```
it.wldt.storage.query
├── Query, QueryResult, QueryResourceType, QueryFieldNames, SortCriterion
├── QueryExecutor, IQueryResultListener
├── QueryManager, DefaultQueryManager
├── filter/        Filter, ComparisonFilter, AndFilter, OrFilter, NotFilter,
│                  FilterOperator, FilterVisitor, FilterFactory
├── aggregation/   Aggregation, AggregationGroupBy, AggregationMetric,
│                  AggregationFunction, AggregationPart, AggregationFactory
└── inmemory/      InMemoryQueryEvaluator
```

The main classes associated to the Query System are the following:

- `Query`: Immutable description of a query, created with the builder `Query.on(resourceType)`. It contains the resource to query, an optional filter, the sorting, the paging (`limit` and `offset`), an optional aggregation and an optional timeout.
- `QueryResourceType`: This Enum represents the Query Resource Type used to specify the type of resource to be queried on the storage system. The supported resource types map those available and managed by the storage manager, together with the record class returned by a plain query on that resource:

| `QueryResourceType` | Returned record |
|---|---|
| `PHYSICAL_ASSET_PROPERTY_VARIATION` | `PhysicalAssetPropertyVariationRecord` |
| `PHYSICAL_ASSET_EVENT_NOTIFICATION` | `PhysicalAssetEventNotificationRecord` |
| `PHYSICAL_ACTION_REQUEST` | `PhysicalAssetActionRequestRecord` |
| `DIGITAL_ACTION_REQUEST` | `DigitalActionRequestRecord` |
| `DIGITAL_TWIN_STATE` | `DigitalTwinStateRecord` |
| `DIGITAL_TWIN_STATE_EVENT_NOTIFICATION` | `DigitalTwinStateEventNotificationRecord` |
| `NEW_PAD_NOTIFICATION`, `UPDATED_PAD_NOTIFICATION` | `PhysicalAssetDescriptionNotificationRecord` |
| `PHYSICAL_RELATIONSHIP_INSTANCE_CREATED_NOTIFICATION`, `PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_NOTIFICATION` | `PhysicalRelationshipInstanceVariationRecord` |
| `LIFE_CYCLE_EVENT` | `LifeCycleVariationRecord` |
| `AUGMENTATION_FUNCTION_ERROR` | `AugmentationFunctionErrorRecord` |
| `AUGMENTATION_FUNCTION_REQUEST` | `AugmentationFunctionRequestRecord` |
| `AUGMENTATION_FUNCTION_RESULT` | `AugmentationFunctionResultRecord` |
| `AUGMENTATION_FUNCTION_REGISTRATION` | `AugmentationFunctionRegistrationRecord` |
| `AUGMENTATION_FUNCTION_UNREGISTRATION` | `AugmentationFunctionUnregistrationRecord` |
| `STORAGE_STATS` | `StorageStats` (a single record with the statistics of the storage) |

- `QueryFieldNames`: Constants with the names of the fields that can be used in filters, sorting and aggregations.
- `FilterFactory` and `AggregationFactory`: Static helper methods to build filters and aggregations in a readable way.
- `QueryResult`: The result of a query: the list of records, the query status (successful or not) with the error message (if any), the optional total number of matches and the original query.
- `QueryExecutor`: This class represents the Query Executor used to execute queries on the storage system supporting both synchronous and asynchronous query execution. Internally is implemented through an event-based mechanism to handle the query request and response.
- `IQueryResultListener`: This interface represents the Query Result Listener used to receive the query results of asynchronous queries.
- `QueryManager`: This class represents the Query Manager responsible to select the storage that executes the query. It has been designed to be extended by the user to implement the desired storage selection logic (e.g., as with the `DefaultQueryManager`).

### Building a query

A query is created with `Query.on(resourceType)` and refined with the following optional methods, that can be combined and called in any order before `build()`:

| Method | Description |
|---|---|
| `where(Filter filter)` | Selects the records matching the filter (all the records if not set). See [Filters](#filters). |
| `orderBy(String field, SortCriterion.Direction direction)` | Sorts the records. It can be called several times to sort on more fields. **Default**: `timestamp` ascending. |
| `limit(int limit)` | Maximum number of records returned. |
| `offset(int offset)` | Number of records to skip (use it together with `limit` for paging). |
| `aggregate(AggregationPart... parts)` | Turns the query into an aggregation (group by and metrics). See [Aggregations](#aggregations). |
| `withTotal()` | Asks the storage to also return the total number of matching records, ignoring `limit` and `offset` (see `QueryResult.getTotal()`). |
| `timeout(long timeoutMs)` | How long the caller waits for the result. See [Executing queries](#executing-queries). |

```java
// All the DT states stored, from the oldest to the newest (default sorting)
Query allStates = Query.on(QueryResourceType.DIGITAL_TWIN_STATE).build();

// The last stored state of the Digital Twin
Query lastState = Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
        .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
        .limit(1)
        .build();
```

A `Query` is **immutable**: it can be stored and executed many times (e.g., from a periodic task) and every execution gets its own request id.

### Filters

A filter is a tree of conditions built with the static methods of `FilterFactory` (`it.wldt.storage.query.filter`), that is convenient to import statically:

```java
import static it.wldt.storage.query.filter.FilterFactory.*;
```

| Method | Meaning |
|---|---|
| `eq(field, value)`, `ne(field, value)` | equal / not equal |
| `gt(field, value)`, `gte(field, value)` | greater than / greater than or equal |
| `lt(field, value)`, `lte(field, value)` | less than / less than or equal |
| `in(field, value1, value2, ...)` | the field value is one of the given values |
| `exists(field)` | the field is present (not null) |
| `and(filter1, filter2, ...)` | all the filters must match |
| `or(filter1, filter2, ...)` | at least one filter must match |
| `not(filter)` | the filter must not match |

Notes about fields and values:

- Fields are identified by name. The names are the properties of the records (e.g., `propertykey`, `body`, `augmentationFunctionId`) and the class `QueryFieldNames` provides the constants for the most common ones. The special field `QueryFieldNames.TIMESTAMP` (`"timestamp"`) is the **storage timestamp** of the record and is available on every resource.
- Fields inside maps can be reached with a dotted path, e.g. `variationMetadata.unit`.
- Numbers are compared by value (an `Integer` filter value matches a `Double` field), and enum values (e.g., a `LifeCycleState`) can be given either as the enum or as its name.

Main fields available for each resource:

| Resource | Fields |
|---|---|
| `PHYSICAL_ASSET_PROPERTY_VARIATION` | `timestamp`, `propertykey`, `body`, `variationMetadata` |
| `PHYSICAL_ASSET_EVENT_NOTIFICATION` | `timestamp`, `eventkey`, `body`, `metadata` |
| `PHYSICAL_ACTION_REQUEST`, `DIGITAL_ACTION_REQUEST` | `timestamp`, `requestTimestamp`, `actionkey`, `requestBody`, `requestMetadata` |
| `DIGITAL_TWIN_STATE` | `timestamp`, `currentState`, `stateChangeList` |
| `DIGITAL_TWIN_STATE_EVENT_NOTIFICATION` | `timestamp`, `eventKey`, `body` |
| `NEW_PAD_NOTIFICATION`, `UPDATED_PAD_NOTIFICATION` | `timestamp`, `notificationTimestamp`, `adapterId`, `physicalAssetDescription` |
| `PHYSICAL_RELATIONSHIP_INSTANCE_*_NOTIFICATION` | `timestamp`, `variationTimestamp`, `instanceKey`, `instanceTargetId`, `relationshipName`, `relationshipType`, `metadata` |
| `LIFE_CYCLE_EVENT` | `timestamp`, `lifeCycleState` |
| `AUGMENTATION_FUNCTION_ERROR` | `timestamp`, `augmentationFunctionId`, `augmentationFunctionHandlerId`, `errorId`, `requestId`, `errorType`, `message`, `metadata` |
| `AUGMENTATION_FUNCTION_REQUEST` | `timestamp`, `augmentationFunctionId`, `augmentationFunctionHandlerId`, `requestId`, `type` |
| `AUGMENTATION_FUNCTION_RESULT` | `timestamp`, `augmentationFunctionId`, `augmentationFunctionHandlerId`, `requestId`, `key`, `type`, `value`, `metadata` |
| `AUGMENTATION_FUNCTION_REGISTRATION`, `AUGMENTATION_FUNCTION_UNREGISTRATION` | `timestamp`, `augmentationFunctionId`, `augmentationFunctionHandlerId`, `type` |

```java
// The temperature variations of the last hour, coming from a Celsius sensor
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .where(and(
                eq(QueryFieldNames.PROPERTY_KEY, "temperature"),
                eq("variationMetadata.unit", "C"),
                gte(QueryFieldNames.TIMESTAMP, System.currentTimeMillis() - 3_600_000)))
        .build();
```

### Aggregations

An aggregation computes values on the matching records directly in the storage instead of returning all of them. It is described with the helpers of `AggregationFactory` (`it.wldt.storage.query.aggregation`):

```java
import static it.wldt.storage.query.aggregation.AggregationFactory.*;
```

| Method | Meaning |
|---|---|
| `groupBy(field1, field2, ...)` | Creates one result for each distinct combination of the given fields (optional: without it the result is a single record for all the matching records) |
| `count()` | Number of records |
| `sum(field)`, `avg(field)` | Sum / average of the numeric values of the field |
| `min(field)`, `max(field)` | Minimum / maximum value of the field |

Each metric can be renamed with `.as("alias")`, otherwise the default name is used (e.g., `count`, `avg_body`). The `filter`, `orderBy`, `limit` and `offset` of the query are applied: the filter selects the records to aggregate, while the sorting (on a group field or on a metric alias) and the paging are applied to the resulting groups.

Each group is returned as an `AggregationRecord` with the typed getters `getString(name)`, `getLong(name)` and `getDouble(name)`, where `name` is a group field or a metric alias. An aggregation without `groupBy` always returns one record (e.g., a `count` on no matching record returns `0`).

### Results

`QueryResult` is the object returned by the executor. It never throws an exception: any problem (invalid query, no storage available, timeout, storage error) is reported with a failed result.

| Method | Description |
|---|---|
| `isSuccessful()` | `true` if the query has been executed correctly |
| `getErrorMessage()` | The error message if the query failed |
| `getItems()` | The list of records (empty if the query failed or nothing matched) |
| `first()`, `last()` | First / last record, `null` if the result is empty |
| `getTotal()` | Total number of matching records ignoring `limit` and `offset`, only if the query was built with `withTotal()` (otherwise `null`) |
| `as(Class<R>)` | The same result typed with the expected record class (a checked cast of every record) |
| `getOriginalQuery()` | The query that has generated the result |

```java
QueryResult<?> queryResult = queryExecutor.syncQueryExecute(query);

if(queryResult.isSuccessful()) {
    for(PhysicalAssetPropertyVariationRecord record : queryResult.as(PhysicalAssetPropertyVariationRecord.class).getItems())
        logger.info("{} -> {}", record.getPropertykey(), record.getBody());
} else
    logger.error("Query failed: {}", queryResult.getErrorMessage());
```

### Executing queries

A `QueryExecutor` is created with the id of the Digital Twin and its own id, and executes queries synchronously or asynchronously:

```java
QueryExecutor queryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "query-executor");

Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
        .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
        .limit(1)
        .build();

// Synchronous: waits for the result
QueryResult<?> queryResult = queryExecutor.syncQueryExecute(query);
DigitalTwinStateRecord lastState = queryResult.as(DigitalTwinStateRecord.class).first();
```

Following the same approach an asynchronous query can be executed as follows:

```java
// Asynchronous: the listener is notified when the result is available
queryExecutor.asyncQueryExecute(query, new IQueryResultListener() {
    @Override
    public void onQueryResult(QueryResult<?> queryResult) {
        [...]
    }
});
```

**Timeout**: the caller waits for the result for **5 seconds** by default (`QueryExecutor.DEFAULT_QUERY_TIMEOUT_MS`); after that a failed result (with a timeout error message) is returned. Heavy queries can use a longer timeout, either for all the queries of an executor or for a single query:

```java
// Default timeout of an executor
QueryExecutor heavyQueryExecutor = new QueryExecutor(TEST_DIGITAL_TWIN_ID, "heavy-query-executor", 30_000);

// Timeout of a single query (it overrides the executor default)
Query heavyQuery = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .aggregate(groupBy(QueryFieldNames.PROPERTY_KEY), count().as("n"))
        .timeout(60_000)
        .build();
```

The class `DigitalAdapter` has an internal reference to a `QueryExecutor`, accessible through `this.queryExecutor`, in order to simplify the interaction with the query system directly from an adapter without creating a new executor, like in the following example where it is used in the `invokeAction` callback:

```java
public <T> void invokeAction(String actionKey, T body){
    try {

        // Query the last Digital Twin State stored
        Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
                .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
                .limit(1)
                .build();

        // Send the Query to the Storage Manager for the target DT
        QueryResult<?> queryResult = this.queryExecutor.syncQueryExecute(query);

        if(queryResult.isSuccessful() && queryResult.first() instanceof DigitalTwinStateRecord)
            logger.info("LAST DT STATE: {}", ((DigitalTwinStateRecord) queryResult.first()).getCurrentState());
        else
            logger.error("QUERY FAILED OR INVALID RESULT: {}", queryResult.getErrorMessage());

        logger.info("INVOKING ACTION: {} BODY: {}", actionKey, body);
        publishDigitalActionWldtEvent(actionKey, body);
    } catch (EventBusException e) {
        e.printStackTrace();
    }
}
```

### Query examples

The following examples go from the simplest to the most complete queries. The returned records are the ones listed in the table of the resources.

**Last value**: the most recent `temperature` variation (`PhysicalAssetPropertyVariationRecord`):

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .where(eq(QueryFieldNames.PROPERTY_KEY, "temperature"))
        .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
        .limit(1)
        .build();

PhysicalAssetPropertyVariationRecord last = queryExecutor.syncQueryExecute(query)
        .as(PhysicalAssetPropertyVariationRecord.class).first();

last.getTimestamp();          // 1731240000000
last.getPropertykey();        // "temperature"
last.getBody();               // 21.5
last.getVariationMetadata();  // {unit=C}
```

**Time range**: all the DT states stored between two instants, oldest first (`DigitalTwinStateRecord`):

```java
Query query = Query.on(QueryResourceType.DIGITAL_TWIN_STATE)
        .where(and(gte(QueryFieldNames.TIMESTAMP, from), lt(QueryFieldNames.TIMESTAMP, to)))
        .build();

List<DigitalTwinStateRecord> states = queryExecutor.syncQueryExecute(query)
        .as(DigitalTwinStateRecord.class).getItems();
```

**Key, time range, sorting and limit**: the 100 most recent `temperature` variations in a time window. The filter is applied by the storage, so only 100 records are returned:

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .where(and(eq(QueryFieldNames.PROPERTY_KEY, "temperature"),
                   gte(QueryFieldNames.TIMESTAMP, from),
                   lt(QueryFieldNames.TIMESTAMP, to)))
        .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
        .limit(100)
        .build();
```

**Several keys, value and metadata**: `temperature` or `humidity` variations above 30, measured in Celsius:

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .where(and(in(QueryFieldNames.PROPERTY_KEY, "temperature", "humidity"),
                   gt(QueryFieldNames.BODY, 30.0),
                   eq("variationMetadata.unit", "C")))
        .build();
```

**OR and NOT**: the errors of two augmentation functions, excluding the ones of type `INFO` (`AugmentationFunctionErrorRecord`):

```java
Query query = Query.on(QueryResourceType.AUGMENTATION_FUNCTION_ERROR)
        .where(and(
                or(eq(QueryFieldNames.FUNCTION_ID, "anomaly-detector"), eq(QueryFieldNames.FUNCTION_ID, "forecaster")),
                not(eq(QueryFieldNames.ERROR_TYPE, AugmentationFunctionErrorType.INFO))))
        .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
        .limit(50)
        .build();
```

**Count**: the number of `overheat` events (an `AggregationRecord`):

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_EVENT_NOTIFICATION)
        .where(eq(QueryFieldNames.EVENT_KEY, "overheat"))
        .aggregate(count().as("n"))
        .build();

AggregationRecord result = queryExecutor.syncQueryExecute(query).as(AggregationRecord.class).first();
long overheatEvents = result.getLong("n");   // 42
```

**Group by**: how many variations have been received for each property, most frequent first:

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .aggregate(groupBy(QueryFieldNames.PROPERTY_KEY), count().as("n"))
        .orderBy("n", SortCriterion.Direction.DESC)
        .build();

for(AggregationRecord group : queryExecutor.syncQueryExecute(query).as(AggregationRecord.class).getItems())
    logger.info("{} -> {}", group.getString(QueryFieldNames.PROPERTY_KEY), group.getLong("n"));
// temperature -> 1200, humidity -> 980, pressure -> 15
```

**Statistics per key**: count, average, minimum and maximum of each property in a time window, computed by the storage:

```java
Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
        .where(and(in(QueryFieldNames.PROPERTY_KEY, "temperature", "humidity"),
                   gte(QueryFieldNames.TIMESTAMP, from), lt(QueryFieldNames.TIMESTAMP, to)))
        .aggregate(groupBy(QueryFieldNames.PROPERTY_KEY),
                   count().as("n"), avg(QueryFieldNames.BODY).as("avg"),
                   min(QueryFieldNames.BODY).as("min"), max(QueryFieldNames.BODY).as("max"))
        .build();

AggregationRecord temperature = queryExecutor.syncQueryExecute(query).as(AggregationRecord.class).first();
temperature.getString(QueryFieldNames.PROPERTY_KEY);  // "temperature"
temperature.getLong("n");                             // 600
temperature.getDouble("avg");                         // 22.4
```

**Paging**: reading a big resource page by page. Using the last timestamp read as lower bound (instead of an `offset`) keeps every page fast and stable even while new data are being stored:

```java
long lastTimestamp = 0;
List<PhysicalAssetPropertyVariationRecord> page;
do {
    Query query = Query.on(QueryResourceType.PHYSICAL_ASSET_PROPERTY_VARIATION)
            .where(and(eq(QueryFieldNames.PROPERTY_KEY, "temperature"), gt(QueryFieldNames.TIMESTAMP, lastTimestamp)))
            .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.ASC)
            .limit(500)
            .build();

    page = queryExecutor.syncQueryExecute(query).as(PhysicalAssetPropertyVariationRecord.class).getItems();
    // process the page ...
    if(!page.isEmpty())
        lastTimestamp = page.get(page.size() - 1).getTimestamp();
} while(page.size() == 500);
```

**Page with total**: the last 20 digital action requests together with how many have been stored in total:

```java
QueryResult<DigitalActionRequestRecord> result = queryExecutor.syncQueryExecute(
        Query.on(QueryResourceType.DIGITAL_ACTION_REQUEST)
                .orderBy(QueryFieldNames.TIMESTAMP, SortCriterion.Direction.DESC)
                .limit(20)
                .withTotal()
                .build())
        .as(DigitalActionRequestRecord.class);

result.getItems().size();   // 20
result.getTotal();          // 3480
```

**Storage statistics**: number of records and time range for each type of information (`StorageStats`):

```java
StorageStats stats = queryExecutor.syncQueryExecute(Query.on(QueryResourceType.STORAGE_STATS).build())
        .as(StorageStats.class).first();
```

### Query modes of previous versions

Before the introduction of the `Query` object, queries were described by a request type. The equivalent queries are the following:

| Previous request type | Equivalent query |
|---|---|
| `TIME_RANGE` | `where(and(gte(TIMESTAMP, start), lte(TIMESTAMP, end)))` |
| `SAMPLE_RANGE` (indices `a` to `b`) | `offset(a).limit(b - a + 1)` |
| `LAST_VALUE` | `orderBy(TIMESTAMP, DESC).limit(1)` |
| `COUNT` | `aggregate(count().as("count"))` |

### Query Manager

The `QueryManager` is in charge of choosing the storage that executes a query among the ones available in the `StorageManager`. The default implementation (`DefaultQueryManager`) uses the **first available storage** and adds the monitoring metrics of the executed queries. The behavior can be changed by extending the class and overriding `getTargetStorage(Query query, Map<String, WldtStorage> storageMap)`, for example to send the queries on the state variations to a storage and the others to a different one, and by setting it with `StorageManager.setQueryManager(...)`.

### Threading and performance

The `StorageManager` receives all the events and the queries of a Digital Twin on **a single event thread**, and calls the `save*` methods and `execute(Query)` of the storages one after the other. This keeps the order of the data, but means that a long query delays the storing of the data that arrive in the meantime. Some suggestions to keep queries cheap:

- Filter as much as possible (by key and by time) and use `limit`, so the storage returns only what is needed.
- Prefer an aggregation (`count`, `avg`, `groupBy`, ...) to reading many records to compute a value.
- Use a longer `timeout` only for the queries that need it.
- A query is skipped by the `StorageManager` if its timeout is already expired when it is received, since nobody is waiting for its result any more.

A storage implementation that needs to read and write in parallel (e.g., a database with a read replica) can manage its own threads inside its implementation: the library does not impose any thread model on the storage.

## Implementing a Custom Storage

A new storage (e.g., based on Redis, MySQL or MongoDB) is created extending `WldtStorage` and implementing:

- the `save*` methods, called by the `StorageManager` when the observed events arrive;
- `execute(Query query)`, that must **translate the query into the native language of the storage** and return a `QueryResult`:
    - a plain query returns the records of the queried resource (the record classes of `it.wldt.storage.model`);
    - a query with an aggregation returns one `AggregationRecord` for each group;
    - `QueryResourceType.STORAGE_STATS` returns a single `StorageStats`;
    - if `withTotal()` has been requested, the total number of matches (ignoring `limit` and `offset`) is added to the result;
    - problems must be reported with `QueryResult.failure(query, message)` instead of throwing exceptions.

A storage that does not have a query engine can simply delegate to the `InMemoryQueryEvaluator` (this is what `DefaultWldtStorage` does), passing the list of `(timestamp, record)` entries of the queried resource:

```java
@Override
public QueryResult<?> execute(Query query) throws StorageException {
    return InMemoryQueryEvaluator.evaluate(query, recordsOf(query.getResource()));
}
```

A storage backed by a database should instead push filter, sorting, paging and aggregation to the database. The filter tree can be visited with a `FilterVisitor` to produce the native filter, for example a parametrized SQL `WHERE` clause:

```java
public class SqlWhereBuilder implements FilterVisitor<String> {

    private final List<Object> parameters = new ArrayList<>();

    public List<Object> getParameters() {
        return parameters;
    }

    @Override
    public String visitComparison(ComparisonFilter comparison) {
        String column = comparison.getField();   // map the field name to the column here
        switch (comparison.getOperator()) {
            case EQ:  parameters.add(comparison.getValue()); return column + " = ?";
            case NE:  parameters.add(comparison.getValue()); return column + " <> ?";
            case GT:  parameters.add(comparison.getValue()); return column + " > ?";
            case GTE: parameters.add(comparison.getValue()); return column + " >= ?";
            case LT:  parameters.add(comparison.getValue()); return column + " < ?";
            case LTE: parameters.add(comparison.getValue()); return column + " <= ?";
            case IN:
                Collection<?> values = (Collection<?>) comparison.getValue();
                parameters.addAll(values);
                return column + " IN (" + String.join(",", Collections.nCopies(values.size(), "?")) + ")";
            case EXISTS: return column + " IS NOT NULL";
            default: throw new IllegalArgumentException("Unsupported operator: " + comparison.getOperator());
        }
    }

    @Override
    public String visitAnd(AndFilter and) {
        return and.getChildren().stream().map(child -> "(" + child.accept(this) + ")").collect(Collectors.joining(" AND "));
    }

    @Override
    public String visitOr(OrFilter or) {
        return or.getChildren().stream().map(child -> "(" + child.accept(this) + ")").collect(Collectors.joining(" OR "));
    }

    @Override
    public String visitNot(NotFilter not) {
        return "NOT (" + not.getChild().accept(this) + ")";
    }
}
```

**Note**: `execute(Query)` is called on the event thread of the `StorageManager`, one call at a time together with the `save*` calls (see [Threading and performance](#threading-and-performance)). A storage that wants to serve queries concurrently, or without delaying the storing of new data, has to manage its own threads internally.

---

---

## Storage Monitoring Integration

When `withStorageMonitoring()` is included in `MonitoringInterfaceConfiguration`, `StorageManager`, `WldtStorage`, and `DefaultQueryManager` are instrumented automatically. No developer action is required beyond the configuration step.

```java
MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
        .withStorageMonitoring()
        .build();

digitalTwin.getMonitoringInterface().setConfiguration(config);
digitalTwin.getMonitoringInterface().setHandler(new MyMonitoringHandler());
```

### What is tracked

21 metrics are registered under the `"core"` namespace, covering:

- **Query operations** — success count, error count, and execution time for every query handled by `DefaultQueryManager`
- **Write operations** — success count, error count, and write time for each entity type: DT state, Physical Asset Description, augmentation function results, digital events, physical events, and lifecycle events

For the full list of metric names and types, see [STORAGE metrics](monitoring.md#storage-metrics) in the monitoring documentation.

### Per-storage identification

Each `WldtStorage` instance contributes its own metric slot, keyed by the storage ID. When multiple storage implementations are registered on the same DT, metrics from each can be distinguished in `MonitoringInterfaceHandler` callbacks via the `"storage_id"` metadata tag, exposed as the constant `WldtStorage.METRIC_METADATA_STORAGE_ID_KEY`.

```java
@Override
public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
    if (component == WldtMetricComponent.STORAGE) {
        String storageId = (String) metric.getMetadata()
                .get(WldtStorage.METRIC_METADATA_STORAGE_ID_KEY);
        // route metric to the right Prometheus label, etc.
    }
}
```

Direct lookup by storage ID:

```java
Optional<WldtMetric> m = monitoringInterface.getMetric(
        "core.storage_query_success_count", storageId);
```

# Augmentation Function

In this page, we provide a comprehensive overview of the Augmentation Function framework in WLDT, covering its
architecture, implementation, and operational aspects.

The documentation explores the hierarchical structure of the augmentation system, detailing how the Digital Twin Kernel,
Augmentation Manager, Function Handlers, and individual Augmentation Functions interact to extend Digital Twin
capabilities beyond basic shadowing functions. We examine the two main categories of
Augmentation Functions—**Stateless** and **Stateful**—explaining their characteristics,
lifecycle management, state handling, and typical use cases.

The invocation mechanisms are covered in detail, including direct execution for stateless
functions and continuous operation modes (push and polling) for stateful functions, showing how
the Digital Twin Model triggers function execution through the Augmentation Manager. We describe
how augmentation results are structured using Augmentation Result
and how the Digital Twin Model processes these results.

Practical guidance is provided for implementing custom Augmentation Functions, including the base abstract class
structure, context handling, result production, and lifecycle callbacks.
Finally, we cover the technical details of implementing Augmentation Function Handlers,
which manage execution, lifecycle, and result publication, including handler registration,
context provisioning, and coordination with the Augmentation Manager.

The documentation is structured in the following subsections:

- [Architecture & Components](#augmentation-function---architecture--components): Overview of the hierarchical augmentation architecture, including DT Kernel, Augmentation Manager, Function Handlers, and data flow patterns
- [Function Types](#augmentation-function-types): Detailed comparison of Stateless and Stateful augmentation functions, their characteristics, execution models, and use cases
- [Registration & Discovery](#registering-and-unregistering-augmentation-functions): Process for registering functions, lifecycle synchronization, dynamic updates, and discovery callbacks available in the Digital Twin Model
- [Function Invocation](#augmentation-function-invocation): Methods for executing stateless functions and starting/stopping stateful functions from the Digital Twin Model
- [Result Structure](#augmentation-function-results): Format and types of augmentation function results, including property, event, relationship, and generic result categories
- [Implementation Guide](#augmentation-function-implementation): Practical examples and patterns for implementing both stateless and stateful augmentation functions with code samples

## Augmentation Function - Architecture & Components

The WLDT augmentation architecture is organized in a hierarchical structure that enables scalable and
modular management of augmentation functions within the Digital Twin Kernel.

![aug_function_arch.png](../images/aug_function_arch.png)

### DT Kernel Layer

The **DT Kernel** contains the core components of the Digital Twin:

- **Model**: The Digital Twin Model component that implements the shadowing function and manages the DT state
- **Augmentation Manager**: Central coordinator for all augmentation capabilities

The **Model** and **Augmentation Manager** interact bidirectionally:
- **Model → Augmentation Manager**: Sends queries and uses augmentation functions
- **Augmentation Manager → Model**: Returns results from augmentation functions

### Augmentation Manager

The **Augmentation Manager** acts as the central orchestrator, providing:

- **Function Handler Management**: Manages multiple Augmentation Function Handlers (`[1]`, `[...]`, `[n]`)
- **Result Aggregation**: Collects results from all handlers and forwards them to the Model
- **Lifecycle Control**: Coordinates the lifecycle of all registered augmentation functions

**Interactions**:
- **Manages**: Controls multiple Augmentation Function Handlers
- **Results**: Receives results from handlers and publishes them to the Model

### Augmentation Function Handler Layer

Multiple **Augmentation Function Handlers** (`[1]`, `[...]`, `[n]`) exist, 
each responsible for managing one or more augmentation functions.

Each handler:
- **Manages**: Controls the execution and lifecycle of its assigned functions (both stateless and stateful)
- **Handles**: Coordinates execution requests and context provisioning
- **Results**: Collects and forwards results to the Augmentation Manager

### Function Layer

The bottom layer contains the actual **Augmentation Functions** (`f1`, `f2`, etc.):

- **Function f1**: Represents stateless or stateful augmentation functions
- **Function f2**: Represents stateful augmentation functions with internal loops
- Multiple instances of the same function type can exist across different handlers

**Interactions**:
- **Results**: Functions produce results that flow upward through their handler
- **Handles**: Functions are controlled and invoked by their respective handlers

---

### Data Flow

### Downward Flow (Control & Context)

1. **Model** sends queries and invocation requests to **Augmentation Manager**
2. **Augmentation Manager** delegates to appropriate **Function Handlers**
3. **Function Handlers** invoke and manage **Functions** with context provisioning

### Upward Flow (Results)

1. **Functions** produce results and send them to their **Function Handler**
2. **Function Handlers** forward results to the **Augmentation Manager**
3. **Augmentation Manager** aggregates and delivers results to the **Model**

---

## Augmentation Function Types

Augmentation Functions in WLDT are distinguished into two main types based on internal state management: 
**Stateless** and **Stateful**. Both types interact with the **Augmentation Function Handler** 
(implemented by the Augmentation Manager), which coordinates execution, lifecycle management, and result publication.

![aug_function_types.png](../images/aug_function_types.png)

### Stateless Function

**Stateless Functions** (e.g., `f1`) are functions that do not maintain memory between successive executions. 
Each invocation is completely independent from previous ones.

**Characteristics**:
- **Execution**: Invoked via `execute` call from the Digital Twin Model through the Handler
- **Lifecycle**: No `start/stop` management required
- **State**: No persistent internal state
- **Output**: Produce a single list of `results` per execution
- **Use Cases**: Ideal for instantaneous calculations, validations, threshold checking

**Flow**:

1. The Digital Twin Model send an execution request for the target Augmentation Function 
2. The associated Handler receives the request and invokes `execute` on the stateless function with the target context
3. The function computes the result based only on the received context
4. The result is immediately returned to the Handler
5. The Handler publishes the result (`results` upward)
6. The result is received by the Digital Twin Model

### Stateful Function

**Stateful Functions** (e.g., `f2`) maintain internal state between 
successive executions, accumulating a history of results (`result 1`, `result [...]`, `result n`).

**Characteristics**:

- **Execution**: Managed via `start/stop` triggered from the model and managed by the Handler
- **Lifecycle**: Continuous lifecycle with explicit initialization and termination
- **State**: Maintain history of results and/or internal state
- **Output**: Produce results asynchronously and continuously
- **Update**: Receives automatic updates from the Digital Twin core associated to new State and/or Notification 
events in order to be used for its internal computation
- **Use Cases**: Ideal for pattern recognition, trend analysis, adaptive AI models

**Flow**:

1. The Digital Twin Model send a start request for the target Augmentation Function
1. The Handler receives the requests and invokes `start` on the stateful function
3. The function initializes its internal state and starts the **function loop**
4. The function will receive:
    - New Digital Twin State
    - Digital Twin Event Notifications
5. The function can also send requests for a context computation update
6. Results are produced according to the function logic (`result 1`, `result [...]`, `result n`)
7. The Handler receives and publishes results as they are generated
8. When necessary, the Model through the Handler can invoke `stop` to terminate the function

**Important Note**: Result production is not necessarily synchronized with 
the reception of new contexts or notifications. 
The function autonomously decides when and how to generate output based on its internal logic.

---

## Augmentation Function Handler

The **Augmentation Function Handler** acts as a central coordinator for all augmentation functions, providing:

- **Execution Management**: Invocation of `execute` for stateless, management of `start/stop` for stateful
- **Context Provisioning**: Construction and provision of `AugmentationContext` to functions
- **Result Management**: Collection and publication of results to the Digital Twin Model
- **Lifecycle Management**: Control of the lifecycle of stateful functions

The Handler abstracts the complexity of managing different function types, 
offering a uniform interface to the Digital Twin Model.

## Registering and Unregistering Augmentation Functions

![aug_function_registration.png](../images/aug_function_registration.png)

This section describes the interaction procedure between a Digital Twin and its Augmentation Functions, covering registration,
lifecycle synchronization, and dynamic updates with augmentation functions registered and unregistered at runtime as
illustrated in the following sequence diagram:

![augmentation_function_registration.png](../images/augmentation_function_registration.png)

The procedure is designed around **loose coupling** and **event-driven reactivity**. 
The Digital Twin Model never polls for changes; instead, it subscribes once to the Event Bus and reacts to 
events as they occur. The catch-up mechanism at synchronization time guarantees consistency between the 
DTM state and any registrations that happened before the Digital Twin was active.

The main involved phases are:

### Phase 1 — Pre-Start Registration

Before the Digital Twin is started, the Developer retrieves an `AugmentationFunctionHandler` from the `AugmentationManager` and 
registers the first Augmentation Function. The handler publishes a `RegistrationEvent` on the Event Bus, 
but since no subscribers are active yet, the event is silently ignored.

### Phase 2 — Digital Twin Initialization and Synchronization

The Developer starts the Digital Twin, which initializes the `Digital Twin Model`. 
During initialization, the DTM subscribes to `RegistrationEvent` and `UnregistrationEvent` 
notifications on the Event Bus, making it ready to react to future changes.

The DTM then evolves through its lifecycle until it reaches the **Synchronized** state. 
At this point it performs a catch-up procedure: it queries the Augmentation Manager to 
retrieve all registered handlers, then fetches the list of already-registered Augmentation Functions from each handler, 
generating an internal notification for each one. This ensures that any functions registered before the 
Digital Twin started are properly acknowledged.

### Phase 3 — Dynamic Registration by the Developer

Once the system is running, the Developer can register additional Augmentation Functions at any time. 
When a new function is registered on the handler, a `RegistrationEvent` is published on the Event Bus and the 
DTM receives it immediately, keeping its internal state up to date without requiring a restart.

### Phase 4 — Dynamic Unregistration by the Developer

Augmentation Functions can also be removed at runtime. When the Developer unregisters a function, 
the handler publishes an `UnregistrationEvent` on the Event Bus. The DTM receives the notification and 
updates its internal state, ensuring it no longer references the removed function.

### Phase 5 — Self-Registration by an External Augmentation Function (NOT YET IMPLEMENTED)

An External Augmentation Function can autonomously register itself by communicating directly with its
designated `AugmentationFunctionHandler`. The registration flow is identical to the developer-driven case:
the handler publishes a `RegistrationEvent` on the Event Bus, and the DTM is notified and updates its state accordingly.

### Phase 6 — Dynamic Unregistration by an External Augmentation Function (NOT YET IMPLEMENTED)

Augmentation Functions can also be removed at runtime. When the Developer unregisters a function,
the handler publishes an `UnregistrationEvent` on the Event Bus. The DTM receives the notification and
updates its internal state, ensuring it no longer references the removed function.

## Augmentation Function Discovery

The `DigitalTwinModel` provides callback methods to discover and track 
the availability of Augmentation Functions throughout the Digital Twin lifecycle. 
These callbacks enable the Model to react to function registration, unregistration, and availability changes.

### Discovery Callbacks

#### onAugmentationNewFunctionAvailable

```java
protected void onAugmentationNewFunctionAvailable(String handlerId, 
                                                  AugmentationFunction augmentationFunction)
```

Invoked when a new Augmentation Function has been registered and becomes available for use.

**Parameters**:
- `handlerId`: The identifier of the Augmentation Function Handler that registered the function
- `augmentationFunction`: The Augmentation Function that became available

---

#### onAugmentationFunctionUnAvailable

```java
protected void onAugmentationFunctionUnAvailable(String handlerId, 
                                                 AugmentationFunction augmentationFunction)
```

Invoked when an Augmentation Function has been unregistered and is no longer available.

**Parameters**:
- `handlerId`: The identifier of the Augmentation Function Handler that unregistered the function
- `augmentationFunction`: The Augmentation Function that became unavailable

---

#### onAugmentationFunctionListAvailable

```java
protected void onAugmentationFunctionListAvailable(String handlerId, 
                                                   List<AugmentationFunction> augmentationFunctionList)
```

Invoked at Digital Twin Model startup to notify 
all Augmentation Functions already registered and available. 
This method is called once per Handler with its associated list of registered functions.

**Parameters**:
- `handlerId`: The identifier of the Augmentation Function Handler
- `augmentationFunctionList`: List of Augmentation Functions registered in the Handler

---

#### Synchronization State Dependency

Augmentation Function availability notifications are only possible 
when the Digital Twin is in the **Sync state**. If the DT is not synchronized, 
its state is inconsistent, making augmentation function execution unfeasible.

**Behavior**:
- **Functions registered at DT creation**: Notified step-by-step during the first Synchronization phase
- **Functions registered after DT creation**: Immediately notified if DT is in Sync state, otherwise queued until next Synchronization phase

#### Duplicate Notifications

The Digital Twin Model should handle potential **duplicate callbacks** for the same Augmentation Function. 
Since the DT lifecycle evolution is unpredictable, a function may be notified as available multiple times.

**Example scenario**:
1. Augmentation Function registered while DT is in Sync → notified as available
2. DT goes out of sync, then returns to sync → function notified again as available

This ensures the Model is always aware of available functions and can handle them accordingly.

---

### Override Augmentation Function Discovery Methods

All discovery callbacks provide **default implementations** that log the event but perform no action. 
These methods are **not abstract**, allowing Digital Twin Models 
to optionally override them based on specific requirements.

**Example override**:

```java
@Override
protected void onAugmentationNewFunctionAvailable(String handlerId, 
                                                  AugmentationFunction augmentationFunction) {
    logger.info("New augmentation function available: {} from handler: {}", 
                augmentationFunction.getDescription().getId(), 
                handlerId);
    
    // Custom logic: auto-start stateful functions
    if (augmentationFunction.getDescription().getFunctionType() == FunctionType.STATEFUL) {
        try {
            this.startAugmentationFunction(handlerId, 
                                          augmentationFunction.getDescription().getId());
        } catch (Exception e) {
            logger.error("Failed to start stateful function", e);
        }
    }
}
```

---

## Augmentation Function Invocation

The `DigitalTwinModel` class provides several methods to interact with 
both **Stateless** and **Stateful** augmentation functions during runtime.

### Execute Augmentation Function (Stateless)

```java
protected void executeAugmentationFunction(String augmentationFunctionId) 
    throws EventBusException, AugmentationFunctionException
```

Triggers the execution of a **stateless** augmentation function by its identifier. The function executes once and returns a result immediately.

---

### Start Augmentation Function (Stateful)

```java
protected void startAugmentationFunction(String augmentationFunctionHandlerId, 
                                        String augmentationFunctionId) 
    throws EventBusException, AugmentationFunctionException
```

Initiates a **stateful** augmentation function, starting its internal loop and enabling continuous operation. The function begins processing and producing results asynchronously.

---

### Stop Augmentation Function (Stateful)

```java
protected void stopAugmentationFunction(String augmentationFunctionHandlerId, 
                                       String augmentationFunctionId) 
    throws EventBusException, AugmentationFunctionException
```

Terminates a running **stateful** augmentation function, stopping its internal loop and cleaning up resources.

---

### Example 1: Executing Stateless Function on Physical Property Changes

Execute an augmentation function each time a physical property variation is received:

```java
@Override
protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {
    try {

        // Implementation of the management of a new Physical Event in the Digital Twin Model
        //[...]
        
        // Execute augmentation function for each received physical event notification
        this.executeAugmentationFunction(RandomNumberAugmentationFunction.FUNCTION_ID);
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

---

### Example 2: Starting Stateful Function After Digital Twin Binding

Start a stateful augmentation function once the Digital Twin has completed its binding phase:

```java
@Override
protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
    try {
        
        // Implementation of the biding phase in the Model
        //[...]
        
        // Start stateful augmentation function after DT initialization
        this.startAugmentationFunction(
            StatefulPeriodicRandomNumberAugmentationFunction.FUNCTION_ID
        );
        
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

## Augmentation Function Results

Augmentation Functions produce results encapsulated in the `AugmentationFunctionResult<T>` class, 
which provides a flexible and type-safe way to return computed outputs to the Digital Twin Model.

The `AugmentationFunctionResult<T>` is a generic container that structures the 
output of augmentation function executions.

### Structure

```java
public class AugmentationFunctionResult<T> {
    private AugmentationFunctionResultType type;
    private String key;
    private T value;
    private Map<String, Object> metadata;
}
```

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `type` | `AugmentationFunctionResultType` | Categorizes the result type (PROPERTY_RESULT, EVENT_RESULT, RELATIONSHIP_RESULT, RELATIONSHIP_INSTANCE_RESULT, GENERIC_RESULT) |
| `key` | `String` | Identifier for the result (e.g., property name, event key, relationship name) |
| `value` | `T` | The actual computed value, generic type allows flexibility |
| `metadata` | `Map<String, Object>` | Optional metadata providing additional context (e.g., confidence score, timestamp, source) |

---

### AugmentationFunctionResultType Enum

The result type enum defines the categories of outputs that augmentation functions can produce:

```java
public enum AugmentationFunctionResultType {
    PROPERTY_RESULT,
    EVENT_RESULT,
    RELATIONSHIP_RESULT,
    RELATIONSHIP_INSTANCE_RESULT,
    GENERIC_RESULT
}
```

#### Result Type Descriptions

| Type | Description | Digital Twin Component | Example Use Case |
|------|-------------|------------------------|------------------|
| **PROPERTY_RESULT** | Suggests new or updated property values | DT State Properties | Predicted temperature, calculated efficiency, derived metrics |
| **EVENT_RESULT** | Suggests event notifications or registrations | DT State Events | Anomaly detected, threshold exceeded, pattern recognized |
| **RELATIONSHIP_RESULT** | Suggests new relationship type declarations | DT State Relationships | New relationship type discovered between DT types |
| **RELATIONSHIP_INSTANCE_RESULT** | Suggests concrete relationship instances | DT State Relationship Instances | Specific connection discovered to another DT instance |
| **GENERIC_RESULT** | Unstructured or custom results | Model-defined handling | Analysis data, recommendations, metrics, custom insights |

---

### Constructor

```java
public AugmentationFunctionResult(AugmentationFunctionResultType type,
                                  String key,
                                  T value,
                                  Map<String, Object> metadata)
```

Creates a new augmentation function result with all required and optional fields.

---

### Result Production in Augmentation Functions

Augmentation functions can produce **multiple results** in a single execution, returned as a `List<AugmentationFunctionResult<?>>`. This allows a function to suggest multiple property updates, events, or relationships simultaneously.

#### Example: Multi-Result

```java
  List<AugmentationFunctionResult<?>> results = new ArrayList<>();
  
  // Result 1: Predicted failure probability as property
  results.add(new AugmentationFunctionResult<>(
      AugmentationFunctionResultType.PROPERTY_RESULT,
      "predicted_failure_probability",
      0.78,
      Map.of("confidence", 0.92, "horizon_hours", 48)
  ));
  
  // Result 2: Maintenance recommendation as event
  results.add(new AugmentationFunctionResult<>(
      AugmentationFunctionResultType.EVENT_RESULT,
      "maintenance_required",
      Map.of("urgency", "HIGH", "estimated_hours", 48),
      Map.of("timestamp", System.currentTimeMillis())
  ));
  
  // Result 3: Generic analytics
  results.add(new AugmentationFunctionResult<>(
      AugmentationFunctionResultType.GENERIC_RESULT,
      "degradation_analysis",
      Map.of("trend", "increasing", "rate", 0.05),
      Map.of("samples_analyzed", 1000)
  ));
```

---

### Processing Results in Digital Twin Model

The Digital Twin Model receives augmentation function 
results through the `onAugmentationFunctionResultEvent` callback method, 
which provides a **list of results** from a single function (both for `Stateful` and `Stateless`).

### Callback Method Signature

```java
protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId,
                                                 String augmentationFunctionId,
                                                 List<AugmentationFunctionResult<?>> augmentationFunctionResult)
```

**Parameters**:
- `augmentationFunctionHandlerId`: The ID of the handler managing the function
- `augmentationFunctionId`: The ID of the function that produced the results
- `augmentationFunctionResult`: List of results produced by the function execution

---

### Basic Implementation Example

```java
@Override
protected void onAugmentationFunctionResultEvent(String augmentationFunctionHandlerId,
                                                 String augmentationFunctionId,
                                                 List<AugmentationFunctionResult<?>> augmentationFunctionResult) {
    
    logger.info("Received {} results from function: {} (handler: {})",
                augmentationFunctionResult.size(),
                augmentationFunctionId,
                augmentationFunctionHandlerId);
    
    // Iterate over the augmentation function results
    for(AugmentationFunctionResult<?> result : augmentationFunctionResult) {
        logger.info("Processing result: {}", result);
        
        // Process based on result type
        switch (result.getType()) {
            case PROPERTY_RESULT:
                handlePropertyResult(result);
                break;
            case EVENT_RESULT:
                handleEventResult(result);
                break;
            case RELATIONSHIP_RESULT:
                handleRelationshipResult(result);
                break;
            case RELATIONSHIP_INSTANCE_RESULT:
                handleRelationshipInstanceResult(result);
                break;
            case GENERIC_RESULT:
                handleGenericResult(result);
                break;
        }
    }
}
```

### Example 1: PROPERTY_RESULT (Predicted Value)

```java
AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
    AugmentationFunctionResultType.PROPERTY_RESULT,
    "predicted_temperature",
    85.5,
    Map.of(
        "confidence", 0.92,
        "horizon_minutes", 10,
        "timestamp", System.currentTimeMillis()
    )
);
```

---

### Example 2: EVENT_RESULT (Anomaly Detection)

```java
AugmentationFunctionResult<Map<String, Object>> result = new AugmentationFunctionResult<>(
    AugmentationFunctionResultType.EVENT_RESULT,
    "temperature_anomaly_detected",
    Map.of(
        "anomaly", true,
        "severity", "HIGH",
        "temperature", 95.0,
        "threshold", 50.0
    ),
    Map.of(
        "detector_version", "1.2.3",
        "detection_time", System.currentTimeMillis()
    )
);
```

---

### Example 3: RELATIONSHIP_RESULT (New Relationship Type)

```java
AugmentationFunctionResult<Map<String, Object>> result = new AugmentationFunctionResult<>(
    AugmentationFunctionResultType.RELATIONSHIP_RESULT,
    "energySuppliedBy",
    Map.of(
        "description", "Indicates energy supply relationship",
        "bidirectional", false
    ),
    Map.of(
        "discovered_at", System.currentTimeMillis(),
        "discovery_method", "network_topology_analysis"
    )
);
```

---

### Example 4: RELATIONSHIP_INSTANCE_RESULT (Concrete Connection)

```java
AugmentationFunctionResult<String> result = new AugmentationFunctionResult<>(
    AugmentationFunctionResultType.RELATIONSHIP_INSTANCE_RESULT,
    "connectedTo",
    "machine-002-dt-id",
    Map.of(
        "discovered_at", System.currentTimeMillis(),
        "confidence", 0.95,
        "connection_type", "physical"
    )
);
```

---

### Example 5: GENERIC_RESULT (Custom Analytics)

```java
AugmentationFunctionResult<Map<String, Object>> result = new AugmentationFunctionResult<>(
    AugmentationFunctionResultType.GENERIC_RESULT,
    "performance_analysis",
    Map.of(
        "efficiency", 0.78,
        "uptime_percentage", 99.2,
        "energy_consumption_kwh", 150.5,
        "recommendation", "Schedule maintenance within 48 hours"
    ),
    Map.of(
        "analysis_period_hours", 24,
        "samples_analyzed", 1440
    )
);
```

---

### Key Features

- **Multi-Result Support**: Functions can return multiple results in a single execution via `List<AugmentationFunctionResult<?>>`
- **Type Safety**: Generic `<T>` allows strongly-typed values while maintaining flexibility
- **Categorization**: Five distinct result types enable structured processing based on DT component
- **Batch Processing**: The callback provides all results at once, enabling efficient batch operations (e.g., single state transaction)
- **Extensibility**: `metadata` map allows arbitrary additional context without schema changes
- **Traceability**: Results include handler and function IDs, plus optional metadata for full provenance
- **Model Autonomy**: The Digital Twin Model retains full control over whether and how to apply results

---

## Augmentation Function Implementation

## Augmentation Function Implementation

WLDT provides two abstract base classes for implementing augmentation functions: `StatelessAugmentationFunction` and `StatefulAugmentationFunction`. Both classes define the structure and lifecycle methods that developers must implement to create custom augmentation capabilities.

---

### Stateless Augmentation Function

The `StatelessAugmentationFunction` class is designed for functions that perform independent computations without maintaining state between executions.

#### Base Class Structure

```java
public abstract class StatelessAugmentationFunction extends AugmentationFunction {
    
    protected StatelessAugmentationFunction(String id, 
                                           String name, 
                                           String description, 
                                           String version) {
        super(id, name, description, version);
    }
    
    /**
     * Main execution method that must be implemented by concrete functions.
     * Called each time the function is invoked.
     * 
     * @param context The augmentation function context containing DT state and metadata
     * @return List of augmentation function results
     * @throws AugmentationFunctionException if execution fails
     */
    public abstract List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) 
        throws AugmentationFunctionException;
}
```

#### Key Characteristics

- **Single Method**: Only `run()` needs to be implemented
- **No Lifecycle**: No `start()` or `stop()` methods required
- **Context Provided**: Receives `AugmentationFunctionContext` with current DT state on each invocation
- **Immediate Results**: Returns results synchronously via `List<AugmentationFunctionResult<?>>`

---

### Stateful Augmentation Function

The `StatefulAugmentationFunction` class is designed for functions that maintain internal state, run continuously, and can react to DT state changes or events.

#### Base Class Structure

```java
public abstract class StatefulAugmentationFunction extends AugmentationFunction {
    
    protected StatefulAugmentationFunction(String id, 
                                          String name, 
                                          String description, 
                                          String version) {
        super(id, name, description, version);
    }
    
    /**
     * Called when the function is started. Initialize internal state and resources here.
     * 
     * @param context The initial augmentation function context
     * @throws AugmentationFunctionException if initialization fails
     */
    public abstract void start(AugmentationFunctionContext context) 
        throws AugmentationFunctionException;
    
    /**
     * Called when the function is stopped. Clean up resources here.
     * 
     * @param context The final augmentation function context
     * @throws AugmentationFunctionException if cleanup fails
     */
    public abstract void stop(AugmentationFunctionContext context) 
        throws AugmentationFunctionException;
    
    /**
     * Called automatically when the Digital Twin state is updated.
     * 
     * @param digitalTwinState The new Digital Twin state
     * @throws AugmentationFunctionException if processing fails
     */
    public abstract void onStateUpdate(DigitalTwinState digitalTwinState) 
        throws AugmentationFunctionException;
    
    /**
     * Called automatically when a Digital Twin event is notified.
     * 
     * @param digitalTwinStateEventNotification The event notification
     * @throws AugmentationFunctionException if processing fails
     */
    public abstract void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) 
        throws AugmentationFunctionException;
    
    /**
     * Notify results asynchronously to the Augmentation Manager.
     * Call this method whenever the function produces results.
     * 
     * @param results List of augmentation function results to publish
     */
    protected void notifyResult(List<AugmentationFunctionResult<?>> results);
}
```

#### Key Characteristics

- **Lifecycle Management**: Implements `start()` and `stop()` for initialization and cleanup
- **Push Notifications**: Receives automatic callbacks via `onStateUpdate()` and `onEventNotificationReceived()`
- **Asynchronous Results**: Produces results via `notifyResult()` at any time, not necessarily synchronized with notifications
- **Internal State**: Can maintain history, timers, or any internal data structures

---

### Implementation Examples

#### Example 1: Simple Stateless Function (Random Number Generator)

A basic stateless function that generates a random number on each invocation:

```java
public class RandomNumberAugmentationFunction extends StatelessAugmentationFunction {

    public static final String FUNCTION_ID = "random-number-augmentation-function";

    public RandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "Random Number Augmentation Function",
                "This augmentation function generates a random number between 0 and 100.",
                "1.0.0");
    }

    @Override
    public List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) 
            throws AugmentationFunctionException {
        
        // Generate a random number between 0 and 1
        double randomNumber = Math.random();

        // Create a generic result with the random number
        AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.GENERIC_RESULT,
                "randomNumber",
                randomNumber,
                null
        );

        return Collections.singletonList(result);
    }
}
```

**Use Case**: Generate random data for testing or simulation purposes on-demand.

---

#### Example 2: Multi-Result Stateless Function (State Component Generator)

A stateless function that produces multiple results of different types in a single execution:

```java
public class RandomStateResultAugmentationFunction extends StatelessAugmentationFunction {

    public static final String FUNCTION_ID = "random-state-result-function";
    public static final int RANDOM_STRING_LENGTH = 10;

    public RandomStateResultAugmentationFunction() {
        super(FUNCTION_ID,
                "Random State Result Augmentation Function",
                "Generates multiple state components with random values",
                "1.0.0");
    }

    @Override
    public List<AugmentationFunctionResult<?>> run(AugmentationFunctionContext context) 
            throws AugmentationFunctionException {

        List<AugmentationFunctionResult<?>> results = new ArrayList<>();

        // Property Result: Random string property
        results.add(new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.PROPERTY_RESULT,
                "randomStringProperty",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        ));

        // Event Result: Random number event
        results.add(new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.EVENT_RESULT,
                "randomNumberEvent",
                Math.random(),
                null
        ));

        // Relationship Result: New relationship type
        results.add(new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_RESULT,
                "randomStringRelationship",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        ));

        // Relationship Instance Result: Concrete relationship
        results.add(new AugmentationFunctionResult<>(
                AugmentationFunctionResultType.RELATIONSHIP_INSTANCE_RESULT,
                "randomStringRelationshipInstance",
                generateRandomString(RANDOM_STRING_LENGTH),
                null
        ));

        return results;
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            randomString.append(characters.charAt(index));
        }
        return randomString.toString();
    }
}
```

**Use Case**: Demonstrate how a single augmentation function can suggest multiple changes across different DT state components simultaneously.

---

#### Example 3: Periodic Stateful Function (Timer-Based)

A stateful function that generates results periodically using an internal timer (polling mode):

```java
public class StatefulPeriodicRandomNumberAugmentationFunction extends StatefulAugmentationFunction {

    private static final long AUGMENTATION_FUNCTION_TIME_MS = 1000;
    public static final String FUNCTION_ID = "periodic-random-number-augmentation-function";
    
    private Timer timer;
    private TimerTask timerTask;

    public StatefulPeriodicRandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "Periodic Random Number Augmentation Function",
                "Generates random numbers periodically",
                "1.0.0");
    }

    @Override
    public void start(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try {
            logger.info("Starting periodic function with period {} ms", AUGMENTATION_FUNCTION_TIME_MS);
            createTimerTask(0, AUGMENTATION_FUNCTION_TIME_MS);
        } catch (Exception e) {
            throw new AugmentationFunctionException("Error starting function: " + e.getMessage());
        }
    }

    @Override
    public void stop(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try {
            logger.info("Stopping periodic function");
            stopTimerTask();
        } catch (Exception e) {
            throw new AugmentationFunctionException("Error stopping function: " + e.getMessage());
        }
    }

    private void createTimerTask(long initialDelayMs, long periodMs) {
        stopTimerTask(); // Ensure no existing timer
        
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                // Generate random number
                double randomNumber = Math.random();
                
                AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                        AugmentationFunctionResultType.GENERIC_RESULT,
                        "randomNumber",
                        randomNumber,
                        null
                );
                
                // Notify result asynchronously
                notifyResult(Collections.singletonList(result));
            }
        };

        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(this.timerTask, initialDelayMs, periodMs);
    }

    private void stopTimerTask() {
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }
        if (this.timer != null) {
            this.timer.cancel();
            this.timer.purge();
            this.timer = null;
        }
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) {
        logger.debug("Received state update: {}", digitalTwinState);
        // No action needed - timer handles result generation
    }

    @Override
    public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> notification) {
        logger.debug("Received event notification: {}", notification);
        // No action needed - timer handles result generation
    }
}
```

**Use Case**: Continuous monitoring or periodic forecasting where results are generated at fixed intervals regardless of DT state changes.

---

#### Example 4: State-Driven Stateful Function (Push Mode with History)

A stateful function that reacts to state changes and maintains internal history for computing aggregated results:

```java
public class StatefulStateDrivenRandomNumberAugmentationFunction extends StatefulAugmentationFunction {

    public static final String FUNCTION_ID = "state-driven-random-number-augmentation-function";
    
    private List<AugmentationFunctionResult<?>> lastResultList;

    public StatefulStateDrivenRandomNumberAugmentationFunction() {
        super(FUNCTION_ID,
                "State-Driven Random Number Augmentation Function",
                "Generates random numbers on state changes and computes running average",
                "1.0.0");
        
        this.lastResultList = new ArrayList<>();
    }

    @Override
    public void start(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try {
            logger.info("Starting state-driven augmentation function");
            // Initialize internal state if needed
        } catch (Exception e) {
            throw new AugmentationFunctionException("Error starting function: " + e.getMessage());
        }
    }

    @Override
    public void stop(AugmentationFunctionContext context) throws AugmentationFunctionException {
        try {
            logger.info("Stopping state-driven augmentation function");
            // Cleanup resources
        } catch (Exception e) {
            throw new AugmentationFunctionException("Error stopping function: " + e.getMessage());
        }
    }

    @Override
    public void onStateUpdate(DigitalTwinState digitalTwinState) throws AugmentationFunctionException {
        try {
            logger.debug("Received state update: {}", digitalTwinState);

            // Generate new random number in response to state change
            double randomNumber = Math.random();
            AugmentationFunctionResult<Double> result = new AugmentationFunctionResult<>(
                    AugmentationFunctionResultType.GENERIC_RESULT,
                    "randomNumber",
                    randomNumber,
                    null
            );

            // Add to internal history (stateful behavior)
            this.lastResultList.add(result);

            // Compute average from history
            double averageRandomNumber = this.lastResultList.stream()
                    .filter(r -> r.getType() == AugmentationFunctionResultType.GENERIC_RESULT)
                    .mapToDouble(r -> (Double) r.getValue())
                    .average()
                    .orElse(0.0);

            // Create aggregated result
            AugmentationFunctionResult<Double> averageResult = new AugmentationFunctionResult<>(
                    AugmentationFunctionResultType.GENERIC_RESULT,
                    "averageRandomNumber",
                    averageRandomNumber,
                    null
            );

            // Notify both results asynchronously
            this.notifyResult(Arrays.asList(result, averageResult));

        } catch (Exception e) {
            throw new AugmentationFunctionException("Error processing state update: " + e.getMessage());
        }
    }

    @Override
    public void onEventNotificationReceived(DigitalTwinStateEventNotification<?> notification) {
        logger.debug("Received event notification: {}", notification);
        // Could react to specific events if needed
    }
}
```

**Use Case**: Real-time analytics that compute running statistics (averages, trends) based on each state update, demonstrating both state-driven execution and internal state management.

---

### Key Implementation Patterns

#### Stateless Pattern
1. **Constructor**: Initialize function metadata (id, name, description, version)
2. **run()**: Implement computation logic and return results immediately
3. **No State**: Avoid instance variables that persist across invocations

#### Stateful Polling Pattern
1. **Constructor**: Initialize function metadata
2. **start()**: Initialize internal state and start timer/thread
3. **stop()**: Cancel timers and cleanup resources
4. **Timer/Thread**: Generate results periodically using `notifyResult()`
5. **Callbacks**: Optionally react to state/event notifications if needed

#### Stateful Push Pattern
1. **Constructor**: Initialize function metadata and internal state structures
2. **start()**: Initialize any resources needed
3. **stop()**: Cleanup resources
4. **onStateUpdate()**: React to state changes, compute results, call `notifyResult()`
5. **onEventNotificationReceived()**: React to events if relevant

---

### Best Practices

- **Constructor**: Always call `super()` with complete metadata (id, name, description, version)
- **Error Handling**: Wrap logic in try-catch and throw `AugmentationFunctionException` with descriptive messages
- **Logging**: Use WLDT logger for debugging and operational visibility
- **Resource Cleanup**: Always clean up timers, threads, and connections in `stop()`
- **Asynchronous Results**: In stateful functions, results can be produced at any time via `notifyResult()`
- **Result Lists**: Always return or notify a `List<AugmentationFunctionResult<?>>`, even for single results
- **Metadata**: Include meaningful metadata in results (timestamps, confidence scores, etc.)
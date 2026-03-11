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

### Important Considerations

#### Synchronization State Dependency

Augmentation Function availability notifications are only possible 
when the Digital Twin is in the **Sync state**. If the DT is not synchronized, 
its state is inconsistent, making augmentation function execution unfeasible.

**Behavior**:
- **Functions registered at DT creation**: Notified step-by-step during the first Synchronization phase
- **Functions registered after DT creation**: Immediately notified if DT is in Sync state, otherwise queued until next Synchronization phase

#### Duplicate Notifications

The Digital Twin Model should handle potential **duplicate callbacks** for the same Augmentation Function. Since the DT lifecycle evolution is unpredictable, a function may be notified as available multiple times.

**Example scenario**:
1. Augmentation Function registered while DT is in Sync → notified as available
2. DT goes out of sync, then returns to sync → function notified again as available

This ensures the Model is always aware of available functions and can handle them accordingly.

#### Querying Available Functions

At any time, the Digital Twin Model can **query the Augmentation Manager** to 
retrieve the currently available Augmentation Functions, independently of callback notifications.
This provides flexibility in handling function discovery programmatically.

---

### Default Implementation

All discovery callbacks provide **default implementations** that log the event but perform no action. These methods are **not abstract**, allowing Digital Twin Models to optionally override them based on specific requirements.

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

### Best Practices

- **Handle duplicates gracefully**: Implement idempotent logic in discovery callbacks to manage repeated notifications
- **Check DT state**: Verify the Digital Twin synchronization state before executing augmentation functions discovered via callbacks
- **Query when needed**: Use direct queries to the Augmentation Manager for deterministic function availability checks
- **Optional override**: Only implement discovery callbacks if your Digital Twin Model needs to react to function availability changes

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

....

## Augmentation Function Implementation
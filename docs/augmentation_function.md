# Augmentation Function

...

![aug_function_arch.png](../images/aug_function_arch.png)

....

## Augmentation Function Types

....

![aug_function_types.png](../images/aug_function_types.png)

....

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

**Phase 1 — Pre-Start Registration**

Before the Digital Twin is started, the Developer retrieves an `AugmentationFunctionHandler` from the `AugmentationManager` and 
registers the first Augmentation Function. The handler publishes a `RegistrationEvent` on the Event Bus, 
but since no subscribers are active yet, the event is silently ignored.

**Phase 2 — Digital Twin Initialization and Synchronization**

The Developer starts the Digital Twin, which initializes the `Digital Twin Model`. 
During initialization, the DTM subscribes to `RegistrationEvent` and `UnregistrationEvent` 
notifications on the Event Bus, making it ready to react to future changes.

The DTM then evolves through its lifecycle until it reaches the **Synchronized** state. 
At this point it performs a catch-up procedure: it queries the Augmentation Manager to 
retrieve all registered handlers, then fetches the list of already-registered Augmentation Functions from each handler, 
generating an internal notification for each one. This ensures that any functions registered before the 
Digital Twin started are properly acknowledged.

**Phase 3 — Dynamic Registration by the Developer**

Once the system is running, the Developer can register additional Augmentation Functions at any time. 
When a new function is registered on the handler, a `RegistrationEvent` is published on the Event Bus and the 
DTM receives it immediately, keeping its internal state up to date without requiring a restart.

**Phase 4 — Self-Registration by an External Augmentation Function**

An External Augmentation Function can autonomously register itself by communicating directly with its 
designated `AugmentationFunctionHandler`. The registration flow is identical to the developer-driven case: 
the handler publishes a `RegistrationEvent` on the Event Bus, and the DTM is notified and updates its state accordingly.

**Phase 5 — Dynamic Unregistration**

Augmentation Functions can also be removed at runtime. When the Developer unregisters a function, 
the handler publishes an `UnregistrationEvent` on the Event Bus. The DTM receives the notification and 
updates its internal state, ensuring it no longer references the removed function.

## Augmentation Function Invocation

....

## Augmentation Function Results

....

## Augmentation Function Implementation
# WLDT & Digital Twins Monitoring

## Event-Bus Monitoring

...

## Digital Twin Monitoring Interface

The WLDT monitoring system is built around three collaborating classes: `MonitoringInterface`, `MonitoringInterfaceHandler`, and `WldtMetricRegistry`. 
Metrics are **registered once** and then **updated in-place** as new measurements arrive. 
Each metric instance accumulates type-specific supporting fields (delta, min/max, cumulative counts) across all updates.

---

### Core Classes

#### `MonitoringInterface`

The central hub. Each `DigitalTwin` instance owns one `MonitoringInterface` which is automatically injected into all DT components (Model, Adapters, Augmentation Functions, Storage).

It exposes two primary methods used by framework components:

- **`notifyMetric(WldtMetric metric)`** — the standard push path. On the **first push** for a given metric name, the metric is registered and `MonitoringInterfaceHandler.onMetricRegistered()` is called with an empty snapshot; if the metric was constructed with an initial value, `onMetricUpdated()` also fires immediately after. On **every subsequent push** for the same name the stored live instance is mutated in-place and `MonitoringInterfaceHandler.onMetricUpdated()` is called.
- **`trackCustomMetric(WldtMetric metric)`** — same pipeline for developer-defined custom metrics; bypasses per-component flag gating; metric component must be `WldtMetricComponent.CUSTOM`.

Additional utility methods:

| Method | Purpose |
|---|---|
| `registerMetric(WldtMetric)` | Pre-register a metric bypassing per-component flag gating; fires `onMetricRegistered` (and `onMetricUpdated` if initialized); useful at DT startup to declare metrics before measurements are available |
| `deregisterMetric(String fullName)` | Remove a metric; next push starts fresh |
| `getMetric(String fullName)` | Query the live instance by full name (`namespace.name`) |
| `getAllMetrics()` | Snapshot of all registered live instances |
| `isMetricRegistered(String fullName)` | Check presence |
| `isActive()` | Returns `true` if both configuration and handler are set |
| `isActive(WldtMetricComponent)` | Returns `true` if active and the given component's flag is enabled |
| `setConfiguration(MonitoringInterfaceConfiguration)` | Configure per-component enable flags |
| `setHandler(MonitoringInterfaceHandler)` | Attach the developer callback implementation |

Obtaining and configuring the interface on a `DigitalTwin`:

```java
MonitoringInterfaceConfiguration config = new MonitoringInterfaceConfiguration.Builder()
        .withDtModelMonitoring()
        .withPhysicalAdapterMonitoring()
        .withDigitalAdapterMonitoring()
        .withCustomNamespace("myapp.dt")
        .build();

digitalTwin.getMonitoringInterface().setConfiguration(config);
digitalTwin.getMonitoringInterface().setHandler(new MyMonitoringHandler());
```

#### `MonitoringInterfaceConfiguration`

Immutable, builder-based. Controls which DT components have monitoring enabled. Custom metrics always bypass flag gating.

| Builder method | Enables |
|---|---|
| `withDtModelMonitoring()` | DT Model (Shadowing Function) |
| `withPhysicalAdapterMonitoring()` | Physical Adapter |
| `withDigitalAdapterMonitoring()` | Digital Adapter |
| `withAugmentationMonitoring()` | Augmentation Functions |
| `withStorageMonitoring()` | Storage layer |
| `withAllMonitoring()` | All of the above |
| `withCustomNamespace(String)` | Namespace prefix for custom metrics (default: `"custom"`) |

#### `MonitoringInterfaceHandler`

Abstract class with two callbacks. Developers extend it and override whichever callbacks they need — both have no-op default implementations.

```java
public abstract class MonitoringInterfaceHandler {

    /** Fires once when a metric name is first observed.
     *  The metric is an empty (uninitialized) snapshot of the live type — it carries
     *  identity fields (namespace, name, component) but no measured value. */
    public void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {}

    /** Fires on every update for an already-registered metric, including the initial
     *  update if the metric was constructed with a starting value.
     *  The metric is an independent snapshot copy of the live instance taken at callback time. */
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {}
}
```

The `component` parameter identifies which DT component emitted the metric. Use it together with `instanceof` to access typed fields:

##### Metric snapshots in handler callbacks

`onMetricRegistered` always receives an **empty snapshot** (`emptySnapshot()`) — the metric exists but carries no measured value yet. This callback is intended for setup (e.g., creating the corresponding Prometheus counter before any data arrives).

`onMetricUpdated` receives an **independent copy** of the live metric (`copy()`) capturing all fields at that instant: the current value, delta, min/max, cumulative counters, and `lastUpdatedMs`. Subsequent pushes that mutate the live registry entry do not affect the copy held by the handler.

If a metric is constructed with an initial value, both callbacks fire on the first push — `onMetricRegistered` first (empty snapshot), then `onMetricUpdated` (snapshot of the initial value).

```java
public class MyHandler extends MonitoringInterfaceHandler {

    private final List<WldtMetric> history = new ArrayList<>();

    @Override
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
        // Safe: metric is a snapshot — adding it to a list captures its state
        // at this exact moment, unaffected by any subsequent push.
        history.add(metric);
    }
}
```

If you need the **current live value** of a metric at any time outside a callback, use `MonitoringInterface.getMetric(fullName)` which returns the live registry instance directly.

```java
public class MyHandler extends MonitoringInterfaceHandler {

    @Override
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
        if (component == WldtMetricComponent.DT_MODEL) {
            if (metric instanceof WldtCounter) {
                WldtCounter c = (WldtCounter) metric;
                if (c.isDeltaAvailable())
                    prometheusCounter.inc(c.getDelta());
            }
            if (metric instanceof WldtTimer) {
                WldtTimer t = (WldtTimer) metric;
                prometheusHistogram.observe(t.getDurationSeconds());
            }
        }
    }
}
```

#### `WldtMetricRegistry`

Internal store (not used directly by developers). Maintains the live mutable metric instance for each registered name in a `ConcurrentHashMap`. Key operations:

- `register(WldtMetric)` — stores initial instance
- `registerIfAbsent(WldtMetric)` — atomic; returns `true` if newly registered (used internally by `MonitoringInterface`)
- `update(WldtMetric incoming)` — looks up the stored live instance, calls its typed `update()` method with the value from `incoming`, returns the mutated live instance

---

### Metric Types

All metrics extend `WldtMetric` and carry:
- `digitalTwinId` — the id of the `DigitalTwin` instance that owns this metric (first constructor parameter)
- `namespace` — logical grouping prefix (e.g. `"core"` for framework metrics, or `"myapp.sensor"` for custom metrics)
- `name` — identifier within the namespace
- `component` — `WldtMetricComponent` enum value (routing and flag gating)
- `timestampMs` — epoch ms at construction
- `lastUpdatedMs` — epoch ms of the most recent mutation
- `isInitialized()` — `false` for metrics constructed without an initial value; `true` once a value has been set
- `emptySnapshot()` — returns a new uninitialized copy of the same type (used internally for `onMetricRegistered` callbacks)
- **Metadata** — arbitrary key-value tags: `addMetadata(String key, Object value)`, `removeMetadata(String key)`, `setMetadata(Map)`, `getMetadata()`. Useful for tagging metrics with context (e.g. function id, adapter id).

The **full name** used for registry lookup is `namespace + "." + name`.

All metric constructors accept `digitalTwinId` as their first parameter. Constructors come in two flavors:
- **Uninitialized** (`digitalTwinId, namespace, name, component`) — registers the metric slot without a starting value; `isInitialized()` returns `false` until the first `update()` call.
- **Initialized** (`digitalTwinId, namespace, name, component, initialValue`) — sets a starting value immediately; `isInitialized()` returns `true`.

Both flavors have an additional overload accepting a `Map<String, Object> metadata` as final parameter.

---

#### `WldtCounter` — Monotonically Increasing Counter

Models discrete occurrences that can only increase (total events processed, errors encountered, messages sent). Tracks `delta` (last increment) and `totalIncrements` across all mutations.

**Mutation methods:**
- `update(long newAbsoluteValue)` — set new absolute cumulative value; must be ≥ current value
- `increment()` — add 1
- `increment(long amount)` — add `amount` (must be > 0)

**Supporting fields:** `getValue()`, `getDelta()` (null until first mutation), `isDeltaAvailable()`, `getTotalIncrements()`

**Example:**

```java
// --- Registration (first push) ---
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace(); // returns "core"

monitoringInterface.notifyMetric(
        new WldtCounter(dtId, namespace, "events_processed", WldtMetricComponent.DT_MODEL, 0L));
// → fires onMetricRegistered(DT_MODEL, emptySnapshot)
// → fires onMetricUpdated(DT_MODEL, snapshot{value=0}) because metric is initialized

// --- Update (absolute value) ---
monitoringInterface.notifyMetric(
        new WldtCounter(dtId, namespace, "events_processed", WldtMetricComponent.DT_MODEL, 5L));
// → fires onMetricUpdated(DT_MODEL, snapshot{value=5, delta=5})

// --- Update (increment directly on the stored instance) ---
WldtCounter stored = (WldtCounter) monitoringInterface
        .getMetric(namespace + ".events_processed").get();
stored.increment(3L);
// liveCounter.getValue() == 8, liveCounter.getDelta() == 3L
```

---

#### `WldtUpDownCounter` — Bidirectional Counter

Models discrete entity counts that can increase or decrease (active connections, registered functions, connected adapters). Tracks signed `delta`, `peakValue`, and `troughValue`.

**Mutation methods:**
- `update(long newAbsoluteValue)` — signed delta = new − old
- `increment()` / `increment(long amount)` — add to current value
- `decrement()` / `decrement(long amount)` — subtract from current value

**Supporting fields:** `getValue()`, `getDelta()`, `isDeltaAvailable()`, `getPeakValue()`, `getTroughValue()`, `getTotalUpdates()`

**Example:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// --- Registration ---
monitoringInterface.notifyMetric(
        new WldtUpDownCounter(dtId, namespace, "active_connections",
                WldtMetricComponent.PHYSICAL_ADAPTER, 0L));
// → onMetricRegistered fired, then onMetricUpdated(value=0)

// --- Update via absolute value ---
monitoringInterface.notifyMetric(
        new WldtUpDownCounter(dtId, namespace, "active_connections",
                WldtMetricComponent.PHYSICAL_ADAPTER, 3L));
// → onMetricUpdated fired
//   delta == +3, peakValue == 3

// --- Decrement directly on the stored instance ---
WldtUpDownCounter stored = (WldtUpDownCounter) monitoringInterface
        .getMetric(namespace + ".active_connections").get();
stored.decrement(1L);
// value == 2, delta == -1, troughValue == 2
```

---

#### `WldtGauge` — Point-in-Time Observed Value

Models a continuously observed numeric value with no direction constraint (queue depth, sensor temperature, CPU usage, state property count). Tracks `previousValue`, signed `delta`, `minObserved`, and `maxObserved`.

**Mutation methods:**
- `update(double newValue)` — records new observation; updates all supporting fields

**Supporting fields:** `getValue()`, `getPreviousValue()` (null before first update), `getDelta()` (null before first update), `getMinObserved()`, `getMaxObserved()`, `getUpdateCount()`

**Example:**

```java
String dtId = "my-digital-twin";

// --- Registration ---
monitoringInterface.notifyMetric(
        new WldtGauge(dtId, "myapp.sensor", "temperature",
                WldtMetricComponent.CUSTOM, 20.0));
// → onMetricRegistered fired, then onMetricUpdated(value=20.0)
//   minObserved == maxObserved == 20.0, previousValue == null

// --- Update ---
monitoringInterface.notifyMetric(
        new WldtGauge(dtId, "myapp.sensor", "temperature",
                WldtMetricComponent.CUSTOM, 23.5));
// → onMetricUpdated fired
//   value == 23.5, previousValue == 20.0, delta == +3.5
//   maxObserved == 23.5

// --- Query supporting fields ---
WldtGauge g = (WldtGauge) monitoringInterface
        .getMetric("myapp.sensor.temperature").get();
System.out.println("Range: " + g.getMinObserved() + " – " + g.getMaxObserved());
```

---

#### `WldtTimer` — Duration Measurement

Records the elapsed time of operations in milliseconds. Accumulates `minDurationMs`, `maxDurationMs`, `totalDurationMs`, and `observationCount` across all recordings.

**Mutation methods:**
- `update(long durationMs)` — record a new observation; updates min/max/total/count
- `updateSince(long startMs)` — equivalent to `update(System.currentTimeMillis() - startMs)`

**Supporting fields:** `getDurationMs()` (last observation), `getDurationSeconds()`, `getMinDurationMs()`, `getMaxDurationMs()`, `getTotalDurationMs()`, `getObservationCount()`, `getMeanDurationMs()`

**Example:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// --- Registration (first measurement) ---
long startMs = System.currentTimeMillis();
// ... operation ...
monitoringInterface.notifyMetric(
        new WldtTimer(dtId, namespace, "processing_latency_ms",
                WldtMetricComponent.DT_MODEL,
                System.currentTimeMillis() - startMs));
// → onMetricRegistered fired, then onMetricUpdated
//   min == max == total == firstDuration, observationCount == 1

// --- Update (subsequent measurements via absolute duration) ---
startMs = System.currentTimeMillis();
// ... next operation ...
monitoringInterface.notifyMetric(
        new WldtTimer(dtId, namespace, "processing_latency_ms",
                WldtMetricComponent.DT_MODEL,
                System.currentTimeMillis() - startMs));
// → onMetricUpdated fired with the live timer

// --- Update directly on the stored instance ---
WldtTimer timer = (WldtTimer) monitoringInterface
        .getMetric(namespace + ".processing_latency_ms").get();
timer.updateSince(startMs);
// observationCount++, min/max/mean recalculated

// --- Read statistics ---
System.out.printf("Latency — last: %dms  min: %dms  max: %dms  mean: %.1fms%n",
        timer.getDurationMs(), timer.getMinDurationMs(),
        timer.getMaxDurationMs(), timer.getMeanDurationMs());
```

---

#### `WldtHistogram` — Statistical Distribution

Aggregates observations into a statistical summary. Supports two accumulation modes: single-value observations via `observe(double)` and pre-aggregated windows via `update(count, sum, min, max)`. Tracks per-window fields and cumulative fields across all windows.

**Mutation methods:**
- `observe(double value)` — add one observation; `totalCount++`, `totalSum += value`, update global min/max
- `update(long count, double sum, double min, double max)` — merge a pre-aggregated window into the cumulative state

**Per-window accessors:** `getCount()`, `getSum()`, `getMin()`, `getMax()`, `getMean()`

**Cumulative accessors:** `getTotalCount()`, `getTotalSum()`, `getGlobalMin()`, `getGlobalMax()`, `getWindowCount()`, `getGlobalMean()`

**Example:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// --- Registration (first window) ---
monitoringInterface.notifyMetric(
        new WldtHistogram(dtId, namespace, "message_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER,
                10L, 1200.0, 80.0, 160.0));
// → onMetricRegistered fired, then onMetricUpdated
//   count==10, mean==120, globalMin==80, globalMax==160, windowCount==1

// --- Update via single observations ---
WldtHistogram h = (WldtHistogram) monitoringInterface
        .getMetric(namespace + ".message_size_bytes").get();
h.observe(95.0);
h.observe(200.0);
// totalCount==12, windowCount==3
// globalMax updated to 200.0 if larger than previous globalMax

// --- Update via pre-aggregated window ---
monitoringInterface.notifyMetric(
        new WldtHistogram(dtId, namespace, "message_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER,
                5L, 600.0, 60.0, 180.0));
// → onMetricUpdated fired
//   previous window fields replaced; cumulative fields merged

// --- Read global statistics ---
System.out.printf("Messages — total: %d  global mean: %.1f bytes%n",
        h.getTotalCount(), h.getGlobalMean());
```

---

### `WldtMetricComponent` — Component Routing Enum

| Value | DT Component |
|---|---|
| `DT_MODEL` | Digital Twin Model (Shadowing Function) |
| `PHYSICAL_ADAPTER` | Physical Adapter |
| `DIGITAL_ADAPTER` | Digital Adapter |
| `AUGMENTATION` | Augmentation Function |
| `STORAGE` | Storage layer |
| `CUSTOM` | Developer-defined custom metric |

Custom metrics (`CUSTOM`) always bypass per-component flag gating and reach the handler regardless of the `MonitoringInterfaceConfiguration` flags set.

---

### Convenience Mutation Methods

`MonitoringInterface` exposes a set of high-level methods that let developers update an 
already-registered metric by specifying only the **namespace**, the **name**, and the **new value**. 
The library handles the registry lookup, type validation, casting, mutation, and handler notification internally. 
Any error (metric not found, wrong type, invalid value) is logged and 
silently swallowed — metric updates never affect the normal execution of the program.

#### Method Reference

| Method | Target type | Description |
|---|---|---|
| `increaseCounter(ns, name)` | `WldtCounter` / `WldtUpDownCounter` | Increments by 1 (singleton) |
| `increaseCounter(ns, name, instanceId)` | `WldtCounter` / `WldtUpDownCounter` | Increments by 1 (per-instance) |
| `increaseCounter(ns, name, amount)` | `WldtCounter` / `WldtUpDownCounter` | Increments by `amount` (singleton) |
| `increaseCounter(ns, name, amount, instanceId)` | `WldtCounter` / `WldtUpDownCounter` | Increments by `amount` (per-instance) |
| `updateCounter(ns, name, value)` | `WldtCounter` / `WldtUpDownCounter` | Sets absolute value (singleton; `WldtCounter` requires value ≥ current) |
| `updateCounter(ns, name, value, instanceId)` | `WldtCounter` / `WldtUpDownCounter` | Sets absolute value (per-instance) |
| `decreaseCounter(ns, name)` | `WldtUpDownCounter` | Decrements by 1 (singleton) |
| `decreaseCounter(ns, name, instanceId)` | `WldtUpDownCounter` | Decrements by 1 (per-instance) |
| `decreaseCounter(ns, name, amount)` | `WldtUpDownCounter` | Decrements by `amount` (singleton) |
| `decreaseCounter(ns, name, amount, instanceId)` | `WldtUpDownCounter` | Decrements by `amount` (per-instance) |
| `updateGauge(ns, name, value)` | `WldtGauge` | Sets a new observed value (singleton) |
| `updateGauge(ns, name, value, instanceId)` | `WldtGauge` | Sets a new observed value (per-instance) |
| `updateTimer(ns, name, durationMs)` | `WldtTimer` | Records an absolute duration (singleton) |
| `updateTimer(ns, name, durationMs, instanceId)` | `WldtTimer` | Records an absolute duration (per-instance) |
| `updateTimerSince(ns, name, startMs)` | `WldtTimer` | Records `now − startMs` as duration (singleton) |
| `updateTimerSince(ns, name, startMs, instanceId)` | `WldtTimer` | Records `now − startMs` as duration (per-instance) |
| `histogramObservation(ns, name, value)` | `WldtHistogram` | Adds a single observation (singleton) |
| `histogramObservation(ns, name, value, instanceId)` | `WldtHistogram` | Adds a single observation (per-instance) |
| `histogramObservation(ns, name, count, sum, min, max)` | `WldtHistogram` | Merges a pre-aggregated window (singleton) |
| `histogramObservation(ns, name, count, sum, min, max, instanceId)` | `WldtHistogram` | Merges a pre-aggregated window (per-instance) |

> **Note:** calling `decreaseCounter` on a `WldtCounter` (which is monotonically increasing) logs an error and has no effect. Use `WldtUpDownCounter` for metrics that can go down.

> **Instance ID:** pass `null` for singleton metrics. For multi-instance components (adapters, augmentation functions, storage instances), pass the component's ID so that each instance has its own metric slot in the registry — retrievable via `getMetric(fullName, instanceId)`.

#### Examples

**Counter:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// Register once
monitoringInterface.registerMetric(
        new WldtCounter(dtId, namespace, "events_processed", WldtMetricComponent.DT_MODEL, 0L));

// Later — increment by 1
monitoringInterface.increaseCounter(namespace, "events_processed");

// Or increment by a specific amount
monitoringInterface.increaseCounter(namespace, "events_processed", 5L);

// Or set an absolute value (must be >= current)
monitoringInterface.updateCounter(namespace, "events_processed", 100L);
```

**UpDownCounter:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

monitoringInterface.registerMetric(
        new WldtUpDownCounter(dtId, namespace, "active_connections",
                WldtMetricComponent.PHYSICAL_ADAPTER, 0L));

// New connection established
monitoringInterface.increaseCounter(namespace, "active_connections");

// Connection closed
monitoringInterface.decreaseCounter(namespace, "active_connections");
```

**Gauge:**

```java
String dtId = "my-digital-twin";

monitoringInterface.registerMetric(
        new WldtGauge(dtId, "myapp.sensor", "temperature", WldtMetricComponent.CUSTOM, 20.0));

// Record a new reading
monitoringInterface.updateGauge("myapp.sensor", "temperature", 23.5);
```

**Timer:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

monitoringInterface.registerMetric(
        new WldtTimer(dtId, namespace, "processing_latency_ms", WldtMetricComponent.DT_MODEL, 0L));

// Record an already-measured duration
monitoringInterface.updateTimer(namespace, "processing_latency_ms", elapsedMs);

// Or let the library compute the elapsed time from a start timestamp
long startMs = System.currentTimeMillis();
// ... operation ...
monitoringInterface.updateTimerSince(namespace, "processing_latency_ms", startMs);
```

**Histogram:**

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

monitoringInterface.registerMetric(
        new WldtHistogram(dtId, namespace, "message_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER, 1L, 120.0, 120.0, 120.0));

// Add individual observations
monitoringInterface.histogramObservation(namespace, "message_size_bytes", 95.0);
monitoringInterface.histogramObservation(namespace, "message_size_bytes", 142.0);

// Or merge a pre-aggregated window
monitoringInterface.histogramObservation(namespace, "message_size_bytes",
        10L, 1150.0, 80.0, 160.0);
```

---

### Fluent Mutation API

Every mutation method on a registered metric returns `this`, allowing calls to be chained on the same instance. 
This is useful when multiple updates need to be applied in sequence, or when the result should be passed immediately to `notifyMetric`.

#### Chaining multiple updates on a stored instance

Retrieve the live instance once and apply updates in a single expression:

```java
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// Counter — chain two increments
((WldtCounter) monitoringInterface.getMetric(namespace + ".events").get())
        .increment(3L)
        .increment(2L);
// value += 5 in one line

// UpDownCounter — increment then decrement
((WldtUpDownCounter) monitoringInterface.getMetric(namespace + ".connections").get())
        .increment(5L)
        .decrement(2L);
// net change: +3

// Histogram — record several observations without intermediate variables
((WldtHistogram) monitoringInterface.getMetric(namespace + ".msg_size").get())
        .observe(42.0)
        .observe(55.0)
        .observe(38.0);
```

#### Updating and notifying in one expression

Because mutation methods return the metric instance, you can update a stored metric and pass it directly to `notifyMetric` 
without a temporary variable:

```java
WldtGauge temperature = (WldtGauge) monitoringInterface
        .getMetric("myapp.sensor.temperature").get();

// Update the stored instance and notify in one line
monitoringInterface.notifyMetric(temperature.update(24.1));

// Same pattern with a timer — record elapsed time and notify immediately
String namespace = CoreMonitoringUtils.buildCoreNamespace();
WldtTimer latency = (WldtTimer) monitoringInterface
        .getMetric(namespace + ".processing_latency_ms").get();

monitoringInterface.notifyMetric(latency.updateSince(startMs));
```

#### Inline registration and first update

The fluent return also simplifies the registration-then-first-update pattern when using `registerMetric()` followed by an immediate mutation before the first `notifyMetric` call:

```java
String dtId = "my-digital-twin";
String namespace = CoreMonitoringUtils.buildCoreNamespace();

// Register a counter at zero, then immediately set an initial real value
WldtCounter errors = new WldtCounter(dtId, namespace, "errors", WldtMetricComponent.DT_MODEL, 0L);
monitoringInterface.registerMetric(errors);

// Later, increment and notify without a separate lookup
monitoringInterface.notifyMetric(errors.increment());
```

---

### Framework-Native Metrics

When a component's monitoring flag is enabled via `MonitoringInterfaceConfiguration`, the WLDT framework automatically registers and tracks a standard set of metrics. All framework-native metrics use the namespace `"core"` (returned by `CoreMonitoringUtils.buildCoreNamespace()`).

The full metric name is `core.<metric_name>` and `CoreMonitoringUtils` exposes all names as public string constants.

#### DT_MODEL metrics (Shadowing Function)

Requires `withDtModelMonitoring()` in the configuration.

| Metric name (constant) | Type | Tracks |
|---|---|---|
| `pt_property_variation_exec_time` | `WldtTimer` | Execution time of PA property variation processing |
| `pt_property_variation_exec_success_count` | `WldtCounter` | Successful PA property variation handlers |
| `pt_property_variation_exec_error_count` | `WldtCounter` | Failed PA property variation handlers |
| `pt_event_notification_exec_time` | `WldtTimer` | Execution time of PA event notification processing |
| `pt_event_notification_exec_success_count` | `WldtCounter` | Successful PA event notification handlers |
| `pt_event_notification_exec_error_count` | `WldtCounter` | Failed PA event notification handlers |
| `pt_rel_instance_created_exec_time` | `WldtTimer` | Execution time of relationship-created handlers |
| `pt_rel_instance_created_exec_success_count` | `WldtCounter` | Successful relationship-created handlers |
| `pt_rel_instance_created_exec_error_count` | `WldtCounter` | Failed relationship-created handlers |
| `pt_rel_instance_deleted_exec_time` | `WldtTimer` | Execution time of relationship-deleted handlers |
| `pt_rel_instance_deleted_exec_success_count` | `WldtCounter` | Successful relationship-deleted handlers |
| `pt_rel_instance_deleted_exec_error_count` | `WldtCounter` | Failed relationship-deleted handlers |
| `digital_action_exec_time` | `WldtTimer` | Execution time of digital action request processing |
| `digital_action_exec_success_count` | `WldtCounter` | Successful digital action handlers |
| `digital_action_exec_error_count` | `WldtCounter` | Failed digital action handlers |
| `dt_state_computation_exec_time` | `WldtTimer` | Time taken for full DT state computation |
| `dt_state_computation_exec_success_count` | `WldtCounter` | Successful state computations |
| `dt_state_computation_exec_error_count` | `WldtCounter` | Failed state computations |
| `af_stateless_exec_success_count` | `WldtCounter` | Stateless augmentation function invocations that succeeded |
| `af_stateless_exec_error_count` | `WldtCounter` | Stateless augmentation function invocations that failed |
| `af_stateful_start_success_count` | `WldtCounter` | Successful stateful AF start requests |
| `af_stateful_start_error_count` | `WldtCounter` | Failed stateful AF start requests |
| `af_stateful_stop_success_count` | `WldtCounter` | Successful stateful AF stop requests |
| `af_stateful_stop_error_count` | `WldtCounter` | Failed stateful AF stop requests |
| `af_list_registered_success_count` | `WldtCounter` | Successful AF list-registered calls |
| `af_list_registered_error_count` | `WldtCounter` | Failed AF list-registered calls |
| `af_list_registered_exec_time` | `WldtTimer` | Time taken to query registered AFs |

#### PHYSICAL_ADAPTER metrics

Requires `withPhysicalAdapterMonitoring()` in the configuration.

| Metric name | Type | Tracks |
|---|---|---|
| `pa_property_event_pub_success_count` | `WldtCounter` | Property variation events published successfully |
| `pa_property_event_pub_error_count` | `WldtCounter` | Property variation event publication failures |
| `pa_property_event_notification_pub_success_count` | `WldtCounter` | Event notification messages published successfully |
| `pa_property_event_notification_pub_error_count` | `WldtCounter` | Event notification publication failures |
| `pa_property_rel_created_event_pub_success_count` | `WldtCounter` | Relationship-created events published successfully |
| `pa_property_rel_created_event_pub_error_count` | `WldtCounter` | Relationship-created event publication failures |
| `pa_property_rel_deleted_event_pub_success_count` | `WldtCounter` | Relationship-deleted events published successfully |
| `pa_property_rel_deleted_event_pub_error_count` | `WldtCounter` | Relationship-deleted event publication failures |
| `pa_action_computation_exec_time` | `WldtTimer` | Time taken to compute a physical action |
| `pa_action_computation_exec_success_count` | `WldtCounter` | Successful physical action computations |
| `pa_action_computation_exec_error_count` | `WldtCounter` | Failed physical action computations |

> **Per-instance tracking:** each Physical Adapter instance has its own metric slot, keyed by the adapter ID. Use `METRIC_METADATA_PHYSICAL_ADAPTER_ID_KEY` (`"pa_id"`) from `PhysicalAdapter` to identify the source adapter in handler callbacks, and `getMetric(fullName, adapterId)` for direct lookup.

#### DIGITAL_ADAPTER metrics

Requires `withDigitalAdapterMonitoring()` in the configuration.

| Metric name | Type | Tracks |
|---|---|---|
| `da_action_event_pub_success_count` | `WldtCounter` | Digital action events published successfully |
| `da_action_event_pub_error_count` | `WldtCounter` | Digital action event publication failures |
| `da_state_update_processing_exec_time` | `WldtTimer` | Time to process incoming DT state updates |
| `da_state_update_processing_exec_success_count` | `WldtCounter` | Successful state update processing |
| `da_state_update_processing_exec_error_count` | `WldtCounter` | Failed state update processing |
| `da_event_notification_processing_exec_time` | `WldtTimer` | Time to process incoming DT event notifications |
| `da_event_notification_processing_exec_success_count` | `WldtCounter` | Successful event notification processing |
| `da_event_notification_processing_exec_error_count` | `WldtCounter` | Failed event notification processing |

> **Per-instance tracking:** each Digital Adapter instance has its own metric slot, keyed by the adapter ID. Use `METRIC_METADATA_DIGITAL_ADAPTER_ID_KEY` (`"da_id"`) from `DigitalAdapter` to identify the source adapter in handler callbacks, and `getMetric(fullName, adapterId)` for direct lookup.

#### AUGMENTATION metrics

Requires `withAugmentationMonitoring()` in the configuration.

| Metric name | Type | Tracks |
|---|---|---|
| `af_handler_count` | `WldtUpDownCounter` | Current number of registered `AugmentationFunctionHandler` instances |
| `af_stateful_running_count` | `WldtUpDownCounter` | Current number of stateful AFs in the running state (manager-level) |
| `af_handler_registered_stateless_count` | `WldtUpDownCounter` | Number of stateless functions registered in a handler |
| `af_handler_registered_stateful_count` | `WldtUpDownCounter` | Number of stateful functions registered in a handler |
| `af_handler_stateful_running_count` | `WldtUpDownCounter` | Number of stateful functions currently running in a handler |
| `af_result_success_count` | `WldtCounter` | Successful AF result dispatches |
| `af_result_error_count` | `WldtCounter` | Failed AF result dispatches |
| `af_result_exec_time` | `WldtTimer` | Time to dispatch AF results |
| `af_error_success_count` | `WldtCounter` | Successful AF error dispatches |
| `af_error_error_count` | `WldtCounter` | Failed AF error dispatches |
| `af_error_exec_time` | `WldtTimer` | Time to dispatch AF errors |
| `af_registered_success_count` | `WldtCounter` | Successful AF registration events |
| `af_registered_error_count` | `WldtCounter` | Failed AF registration events |
| `af_registered_exec_time` | `WldtTimer` | Time to process AF registration events |
| `af_unregistered_success_count` | `WldtCounter` | Successful AF unregistration events |
| `af_unregistered_error_count` | `WldtCounter` | Failed AF unregistration events |
| `af_unregistered_exec_time` | `WldtTimer` | Time to process AF unregistration events |
| `af_function_stateless_exec_time` | `WldtTimer` | Execution time of a stateless function invocation |
| `af_function_stateless_exec_success_count` | `WldtCounter` | Successful stateless function executions |
| `af_function_stateless_exec_error_count` | `WldtCounter` | Failed stateless function executions |
| `af_function_stateful_start_exec_time` | `WldtTimer` | Time to start a stateful function |
| `af_function_stateful_start_success_count` | `WldtCounter` | Successful stateful function starts |
| `af_function_stateful_start_error_count` | `WldtCounter` | Failed stateful function starts |
| `af_function_stateful_stop_exec_time` | `WldtTimer` | Time to stop a stateful function |
| `af_function_stateful_stop_success_count` | `WldtCounter` | Successful stateful function stops |
| `af_function_stateful_stop_error_count` | `WldtCounter` | Failed stateful function stops |
| `af_function_query_exec_time` | `WldtTimer` | Time taken by storage queries issued from a function |
| `af_function_query_exec_success_count` | `WldtCounter` | Successful storage queries from a function |
| `af_function_query_exec_error_count` | `WldtCounter` | Failed storage queries from a function |
| `af_function_state_update_exec_time` | `WldtTimer` | Time to dispatch a DT state update to a stateful function |
| `af_function_state_update_success_count` | `WldtCounter` | Successful state update dispatches to functions |
| `af_function_state_update_error_count` | `WldtCounter` | Failed state update dispatches to functions |
| `dt_lifecycle_value` | `WldtUpDownCounter` | Current DT lifecycle state as numeric ordinal |

> **Per-instance tracking:** handler-level metrics (`af_handler_registered_*`, `af_handler_stateful_running_count`) are tracked per handler, keyed by the handler ID. Use `AugmentationFunctionHandler.METRIC_METADATA_AF_HANDLER_ID_KEY` (`"af_handler_id"`) in handler callbacks to identify the source handler, and `getMetric(fullName, handlerId)` for direct lookup. Function-level metrics (`af_function_*`) are tracked per function instance using the function ID — use `AugmentationFunction.METRIC_METADATA_AF_FUNCTION_ID_KEY` (`"af_function_id"`) and `getMetric(fullName, functionId)` accordingly.

#### STORAGE metrics

Requires `withStorageMonitoring()` in the configuration. `StorageManager`, `WldtStorage`, and `DefaultQueryManager` are all instrumented automatically — no developer action required.

| Metric name | Type | Tracks |
|---|---|---|
| `storage_query_success_count` | `WldtCounter` | Successful storage queries |
| `storage_query_error_count` | `WldtCounter` | Failed storage queries |
| `storage_query_exec_time` | `WldtTimer` | Query execution time |
| `storage_write_pa_description_success_count` | `WldtCounter` | Successful PA description write operations |
| `storage_write_pa_description_error_count` | `WldtCounter` | Failed PA description write operations |
| `storage_write_pa_description_exec_time` | `WldtTimer` | PA description write time |
| `storage_write_dt_state_success_count` | `WldtCounter` | Successful DT state write operations |
| `storage_write_dt_state_error_count` | `WldtCounter` | Failed DT state write operations |
| `storage_write_dt_state_exec_time` | `WldtTimer` | DT state write time |
| `storage_write_af_success_count` | `WldtCounter` | Successful augmentation function result write operations |
| `storage_write_af_error_count` | `WldtCounter` | Failed augmentation function result write operations |
| `storage_write_af_exec_time` | `WldtTimer` | Augmentation function result write time |
| `storage_write_de_success_count` | `WldtCounter` | Successful digital event write operations |
| `storage_write_de_error_count` | `WldtCounter` | Failed digital event write operations |
| `storage_write_de_exec_time` | `WldtTimer` | Digital event write time |
| `storage_write_pe_success_count` | `WldtCounter` | Successful physical event write operations |
| `storage_write_pe_error_count` | `WldtCounter` | Failed physical event write operations |
| `storage_write_pe_exec_time` | `WldtTimer` | Physical event write time |
| `storage_write_lifecycle_event_success_count` | `WldtCounter` | Successful lifecycle event write operations |
| `storage_write_lifecycle_event_error_count` | `WldtCounter` | Failed lifecycle event write operations |
| `storage_write_lifecycle_event_exec_time` | `WldtTimer` | Lifecycle event write time |

> **Per-instance tracking:** each `WldtStorage` instance has its own metric slot, keyed by the storage ID. Use `WldtStorage.METRIC_METADATA_STORAGE_ID_KEY` (`"storage_id"`) in handler callbacks to identify which storage produced the metric, and `getMetric("core.storage_query_success_count", storageId)` for direct lookup.

---

### Augmentation Function Monitoring Integration

Augmentation functions participate in the monitoring system through the `AugmentationFunction` base class, which provides built-in monitoring support.

#### Automatic injection

When a function is registered with an `AugmentationFunctionHandler`, the handler automatically injects the `MonitoringInterface` by calling `setMonitoringInterface(monitoringInterface, digitalTwinId)`. Developers do not call this method directly.

After injection, three protected fields are available to subclasses:

| Field | Type | Value |
|---|---|---|
| `monitoringInterface` | `MonitoringInterface` | The shared monitoring interface of the owning DT |
| `digitalTwinId` | `String` | The owning DT's unique id |
| `metricsNamespace` | `String` | Set to `CoreMonitoringUtils.buildCoreNamespace()` (`"core"`) |

#### Registering custom function-level metrics

Override `handleMetricsRegistration()` to register function-specific metrics at injection time. This method is called automatically after `setMonitoringInterface()` completes.

```java
public class MyStatelessFunction extends StatelessAugmentationFunction {

    private static final String MY_METRIC = "my_custom_exec_time";

    public MyStatelessFunction(String id) {
        super(id, "MyFunction", AugmentationFunctionType.STATELESS);
    }

    @Override
    protected void handleMetricsRegistration() {
        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.AUGMENTATION)) {
            Map<String, Object> meta = new HashMap<>();
            meta.put(METRIC_METADATA_AF_FUNCTION_ID_KEY, getId());
            monitoringInterface.registerMetric(
                    new WldtTimer(digitalTwinId, metricsNamespace, MY_METRIC,
                            WldtMetricComponent.AUGMENTATION, meta));
        }
    }

    @Override
    public AugmentationFunctionResult execute(AugmentationFunctionRequest request,
                                              AugmentationFunctionContext context) {
        long start = System.currentTimeMillis();
        // ... function logic ...
        monitoringInterface.updateTimerSince(metricsNamespace, MY_METRIC, start);
        return new AugmentationFunctionResult(...);
    }
}
```

#### Tagging metrics with function id

`AugmentationFunction.METRIC_METADATA_AF_FUNCTION_ID_KEY` (`"af_function_id"`) is a standard metadata key for tagging function-level metrics with the function's unique id. Use it when registering custom metrics so that metrics from different function instances can be distinguished in the handler.

```java
Map<String, Object> meta = Collections.singletonMap(
        AugmentationFunction.METRIC_METADATA_AF_FUNCTION_ID_KEY, getId());

monitoringInterface.registerMetric(
        new WldtTimer(digitalTwinId, metricsNamespace, "my_exec_time",
                WldtMetricComponent.AUGMENTATION, meta));
```

In the handler, retrieve the tag from the snapshot:

```java
@Override
public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {
    if (component == WldtMetricComponent.AUGMENTATION) {
        String functionId = (String) metric.getMetadata()
                .get(AugmentationFunction.METRIC_METADATA_AF_FUNCTION_ID_KEY);
        // use functionId to route the metric to the right Prometheus label, etc.
    }
}
```

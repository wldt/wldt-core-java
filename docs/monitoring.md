# WLDT & Digital Twins Monitoring

## Event-Bus Monitoring

...

## Digital Twin Monitoring Interface

The WLDT monitoring system is built around three collaborating classes: `MonitoringInterface`, `MonitoringInterfaceHandler`, and `WldtMetricRegistry`. 
Metrics are **registered once** with an initial value and then **updated in-place** as new measurements arrive. 
Each metric instance accumulates type-specific supporting fields (delta, min/max, cumulative counts) across all updates.

---

### Core Classes

#### `MonitoringInterface`

The central hub. Each `DigitalTwin` instance owns one `MonitoringInterface` which is automatically injected into all DT components (Model, Adapters, Augmentation Functions, Storage).

It exposes two primary methods used by framework components:

- **`notifyMetric(WldtMetric metric)`** — the standard push path. On the **first push** for a given metric name the metric is registered and `MonitoringInterfaceHandler.onMetricRegistered()` is called. On **every subsequent push** for the same name the stored live instance is mutated in-place and `MonitoringInterfaceHandler.onMetricUpdated()` is called.
- **`trackCustomMetric(WldtMetric metric)`** — same pipeline for developer-defined custom metrics; bypasses per-component flag gating; metric component must be `WldtMetricComponent.CUSTOM`.

Additional utility methods:

| Method | Purpose |
|---|---|
| `registerMetric(WldtMetric)` | Pre-register a metric without firing any callback (useful at startup) |
| `deregisterMetric(String fullName)` | Remove a metric; next push starts fresh |
| `getMetric(String fullName)` | Query the live instance by full name (`namespace.name`) |
| `getAllMetrics()` | Snapshot of all registered live instances |
| `isMetricRegistered(String fullName)` | Check presence |
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

    /** Fires once when a metric name is first observed. */
    public void onMetricRegistered(WldtMetricComponent component, WldtMetric metric) {}

    /** Fires on every subsequent push for an already-registered metric.
     *  The metric is a snapshot copy of the live instance taken at callback time. */
    public void onMetricUpdated(WldtMetricComponent component, WldtMetric metric) {}
}
```

The `component` parameter identifies which DT component emitted the metric. Use it together with `instanceof` to access typed fields:

##### Metric snapshots in handler callbacks

Both `onMetricRegistered` and `onMetricUpdated` receive a **snapshot copy** of the metric, not a reference to the live registry instance.

`MonitoringInterface` calls `metric.copy()` — defined on every metric type — immediately before invoking each callback. The copy captures all fields at that instant: the current value, delta, min/max, cumulative counters, and `lastUpdatedMs`. From that point on, the snapshot and the live instance are fully independent objects — subsequent pushes that mutate the live registry entry do not affect the copy held by the handler.

This guarantee is important when a handler stores metrics for later inspection (e.g., in a list or a metrics sink), because without it the stored reference would silently reflect future mutations rather than the value at the time of the callback.

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
- `namespace` — logical grouping prefix (e.g. `"wldt.internal"` or `"myapp.sensor"`)
- `name` — identifier within the namespace
- `component` — `WldtMetricComponent` enum value (routing and flag gating)
- `timestampMs` — epoch ms at construction
- `lastUpdatedMs` — epoch ms of the most recent mutation

The **full name** used for registry lookup is `namespace + "." + name`.

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
String namespace = CoreMonitoringUtils.buildNamespace(digitalTwinId, "my_component");

monitoringInterface.notifyMetric(
        new WldtCounter(namespace, "events_processed", WldtMetricComponent.DT_MODEL, 0L));
// → fires onMetricRegistered(DT_MODEL, counter)
//   counter.getValue() == 0, counter.getDelta() == null

// --- Update (absolute value) ---
monitoringInterface.notifyMetric(
        new WldtCounter(namespace, "events_processed", WldtMetricComponent.DT_MODEL, 5L));
// → fires onMetricUpdated(DT_MODEL, liveCounter)
//   liveCounter.getValue() == 5, liveCounter.getDelta() == 5L

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
// --- Registration ---
monitoringInterface.notifyMetric(
        new WldtUpDownCounter(namespace, "active_connections",
                WldtMetricComponent.PHYSICAL_ADAPTER, 0L));
// → onMetricRegistered fired, delta == null

// --- Update via absolute value ---
monitoringInterface.notifyMetric(
        new WldtUpDownCounter(namespace, "active_connections",
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
// --- Registration ---
monitoringInterface.notifyMetric(
        new WldtGauge("myapp.sensor", "temperature",
                WldtMetricComponent.CUSTOM, 20.0));
// → onMetricRegistered fired
//   minObserved == maxObserved == 20.0, previousValue == null

// --- Update ---
monitoringInterface.notifyMetric(
        new WldtGauge("myapp.sensor", "temperature",
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

Records the elapsed time of operations in milliseconds. The constructor records the first observation. Accumulates `minDurationMs`, `maxDurationMs`, `totalDurationMs`, and `observationCount` across all recordings.

**Mutation methods:**
- `update(long durationMs)` — record a new observation; updates min/max/total/count
- `updateSince(long startMs)` — equivalent to `update(System.currentTimeMillis() - startMs)`

**Supporting fields:** `getDurationMs()` (last observation), `getDurationSeconds()`, `getMinDurationMs()`, `getMaxDurationMs()`, `getTotalDurationMs()`, `getObservationCount()`, `getMeanDurationMs()`

**Example:**

```java
// --- Registration (first measurement) ---
long startMs = System.currentTimeMillis();
// ... operation ...
monitoringInterface.notifyMetric(
        new WldtTimer(namespace, "processing_latency_ms",
                WldtMetricComponent.DT_MODEL,
                System.currentTimeMillis() - startMs));
// → onMetricRegistered fired
//   min == max == total == firstDuration, observationCount == 1

// --- Update (subsequent measurements via absolute duration) ---
startMs = System.currentTimeMillis();
// ... next operation ...
monitoringInterface.notifyMetric(
        new WldtTimer(namespace, "processing_latency_ms",
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
// --- Registration (first window) ---
monitoringInterface.notifyMetric(
        new WldtHistogram(namespace, "message_size_bytes",
                WldtMetricComponent.PHYSICAL_ADAPTER,
                10L, 1200.0, 80.0, 160.0));
// → onMetricRegistered fired
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
        new WldtHistogram(namespace, "message_size_bytes",
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
| `increaseCounter(ns, name)` | `WldtCounter` / `WldtUpDownCounter` | Increments by 1 |
| `increaseCounter(ns, name, amount)` | `WldtCounter` / `WldtUpDownCounter` | Increments by `amount` |
| `decreaseCounter(ns, name)` | `WldtUpDownCounter` | Decrements by 1 |
| `decreaseCounter(ns, name, amount)` | `WldtUpDownCounter` | Decrements by `amount` |
| `updateGauge(ns, name, value)` | `WldtGauge` | Sets a new observed value |
| `updateTimer(ns, name, durationMs)` | `WldtTimer` | Records an absolute duration |
| `updateTimerSince(ns, name, startMs)` | `WldtTimer` | Records `now − startMs` as duration |
| `histogramObservation(ns, name, value)` | `WldtHistogram` | Adds a single observation |
| `histogramObservation(ns, name, count, sum, min, max)` | `WldtHistogram` | Merges a pre-aggregated window |

> **Note:** calling `decreaseCounter` on a `WldtCounter` (which is monotonically increasing) logs an error and has no effect. Use `WldtUpDownCounter` for metrics that can go down.

#### Examples

**Counter:**

```java
// Register once
monitoringInterface.registerMetric(
        new WldtCounter(namespace, "events_processed", WldtMetricComponent.DT_MODEL, 0L));

// Later — increment by 1
monitoringInterface.increaseCounter(namespace, "events_processed");

// Or increment by a specific amount
monitoringInterface.increaseCounter(namespace, "events_processed", 5L);
```

**UpDownCounter:**

```java
monitoringInterface.registerMetric(
        new WldtUpDownCounter(namespace, "active_connections",
                WldtMetricComponent.PHYSICAL_ADAPTER, 0L));

// New connection established
monitoringInterface.increaseCounter(namespace, "active_connections");

// Connection closed
monitoringInterface.decreaseCounter(namespace, "active_connections");
```

**Gauge:**

```java
monitoringInterface.registerMetric(
        new WldtGauge("myapp.sensor", "temperature", WldtMetricComponent.CUSTOM, 20.0));

// Record a new reading
monitoringInterface.updateGauge("myapp.sensor", "temperature", 23.5);
```

**Timer:**

```java
monitoringInterface.registerMetric(
        new WldtTimer(namespace, "processing_latency_ms", WldtMetricComponent.DT_MODEL, 0L));

// Record an already-measured duration
monitoringInterface.updateTimer(namespace, "processing_latency_ms", elapsedMs);

// Or let the library compute the elapsed time from a start timestamp
long startMs = System.currentTimeMillis();
// ... operation ...
monitoringInterface.updateTimerSince(namespace, "processing_latency_ms", startMs);
```

**Histogram:**

```java
monitoringInterface.registerMetric(
        new WldtHistogram(namespace, "message_size_bytes",
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
// Counter — chain two increments
((WldtCounter) monitoringInterface.getMetric(ns + ".events").get())
        .increment(3L)
        .increment(2L);
// value += 5 in one line

// UpDownCounter — increment then decrement
((WldtUpDownCounter) monitoringInterface.getMetric(ns + ".connections").get())
        .increment(5L)
        .decrement(2L);
// net change: +3

// Histogram — record several observations without intermediate variables
((WldtHistogram) monitoringInterface.getMetric(ns + ".msg_size").get())
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
WldtTimer latency = (WldtTimer) monitoringInterface
        .getMetric(ns + ".processing_latency_ms").get();

monitoringInterface.notifyMetric(latency.updateSince(startMs));
```

#### Inline registration and first update

The fluent return also simplifies the registration-then-first-update pattern when using `registerMetric()` followed by an immediate mutation before the first `notifyMetric` call:

```java
// Register a counter at zero, then immediately set an initial real value
WldtCounter errors = new WldtCounter(ns, "errors", WldtMetricComponent.DT_MODEL, 0L);
monitoringInterface.registerMetric(errors);

// Later, increment and notify without a separate lookup
monitoringInterface.notifyMetric(errors.increment());
```

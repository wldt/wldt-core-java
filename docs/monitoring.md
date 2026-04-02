# WLDT & Digital Twins Monitoring

## Event-Bus Monitoring

...

## Digital Twin Monitoring Interface

Version 0.8.0 introduces the **Monitoring Interface** — a built-in observability layer for Digital Twin instances.
This release provides a push-based, per-component metric system with OpenTelemetry-inspired types, an internal metric
registry with automatic delta computation, and a clean developer extension point. The monitoring system is designed to
integrate naturally with external backends such as Prometheus, Grafana, and OpenTelemetry without introducing any external
dependencies in the core library.

Key Updates:

- **Monitoring Interface**: New push-based observability system, unique per `DigitalTwin` instance, injected automatically into all internal components by the `DigitalTwinKernel`
- **WLDT Metric Types**: Five typed metric classes (`WldtCounter`, `WldtUpDownCounter`, `WldtGauge`, `WldtTimer`, `WldtHistogram`) extending a common `WldtMetric` base, with semantics inspired by OpenTelemetry
- **Metric Registry**: Internal `WldtMetricRegistry` with lazy registration, automatic delta computation for counters, and developer query support
- **Developer Extension Point**: `WldtMonitoringHandler` abstract class with per-component callbacks and no-op defaults, allowing partial implementation without boilerplate

---

## Monitoring Interface

The Monitoring Interface introduces a structured observability layer for WLDT Digital Twins. It enables developers to receive push notifications for metrics emitted by internal DT components — such as the `DigitalTwinModel` (Shadowing Function), the Event Bus, Physical and Digital Adapters, Augmentation Functions, and the Storage layer — without modifying internal library code.

The system follows a **push model**: each time a measurable event occurs inside a DT component, the library pushes an enriched metric to the developer's handler immediately. No polling, no background threads, no periodic aggregation — raw values are delivered on every event and aggregation is left to the developer's chosen backend.

### Architecture Overview

The Monitoring Interface is composed of four collaborating elements:

- `MonitoringConfiguration` defines which components are monitored via per-component flags (all disabled by default) and sets the namespace prefix for custom developer metrics.
- `MonitoringInterface` is the concrete library class that acts as the central hub. It is instantiated once per `DigitalTwin`, injected into all internal components by the `DigitalTwinKernel`, and is responsible for flag gating, registry management, delta computation, and routing.
- `WldtMetricRegistry` is the internal registry that tracks the last known value of every metric, performs lazy auto-registration on the first push, computes deltas for counter types, and exposes query methods to the developer.
- `WldtMonitoringHandler` is the abstract class that developers extend to define what happens with each received metric. It declares one callback per DT component with default no-op implementations, so developers only override what they need.

The `MonitoringInterface` is **unique per `DigitalTwin` instance**. Multiple DTs running concurrently in the same `DigitalTwinEngine` each own a separate `MonitoringInterface` with an independent registry and handler — metrics from different twins never mix.

### Registration and Lifecycle

The developer registers the `MonitoringInterface` on the `DigitalTwin` before starting the engine, using the same pattern already established for `StorageManager`:

```java
MonitoringConfiguration config = new MonitoringConfiguration.Builder()
    .withDtModelMonitoring()
    .withEventBusMonitoring()
    .withPhysicalAdapterMonitoring()
    .withCustomNamespace("myapp.sensor")
    .build();

MonitoringInterface monitoring = new MonitoringInterface(
    config,
    new MyMonitoringHandler(),
    WldtLoggerProvider.getLogger(MyMonitoringHandler.class)
);

DigitalTwin dt = new DigitalTwin("my-dt-id", new MyDigitalTwinModel());
dt.setMonitoringInterface(monitoring);

DigitalTwinEngine engine = new DigitalTwinEngine();
engine.addDigitalTwin(dt);
engine.startAll();
```

The `DigitalTwinKernel` injects the `MonitoringInterface` into all active internal components during DT startup. Components that receive a `null` reference (i.e. monitoring is not configured) simply skip all metric push calls with no overhead.

### Delta Computation

For `WldtCounter` and `WldtUpDownCounter`, the `WldtMetricRegistry` automatically computes the delta between the current push and the previously registered value before dispatching the metric to the handler. This relieves developers from maintaining their own tracking state when integrating with backends like Prometheus that expect incremental updates.

- On the **first push** for a given metric name, `getDelta()` returns `null` — no previous value is available.
- On **all subsequent pushes**, `getDelta()` returns the signed difference from the previous value. For `WldtCounter` the delta is always non-negative (monotonic). For `WldtUpDownCounter` the delta may be positive or negative.

```java
@Override
public void onDigitalTwinModelMetric(WldtMetric metric) {
    if (metric instanceof WldtCounter) {
        WldtCounter c = (WldtCounter) metric;
        // getDelta() is null on first push, non-negative Long on subsequent pushes
        if (c.isDeltaAvailable())
            prometheusCounter.inc(c.getDelta());
    }
}
```

### Custom Metrics

Developers can push their own metrics through the same pipeline via `MonitoringInterface.trackCustomMetric()`. Custom metrics must use `WldtMetricComponent.CUSTOM` and the configured custom namespace. They bypass per-component flag gating and are always forwarded to `WldtMonitoringHandler.onCustomMetric()`. They also benefit from delta computation if they are `WldtCounter` or `WldtUpDownCounter` instances.

```java
monitoring.trackCustomMetric(
    new WldtGauge("myapp.sensor", "room.temperature",
                  WldtMetricComponent.CUSTOM, 21.5)
);
```

---

### WLDT Metric Types

The following tables provide a reference for the monitoring types introduced in the WLDT monitoring system. The first table covers all metric types and infrastructure classes. The second maps each metric type to its OpenTelemetry and Prometheus equivalents to guide developer integrations.

The metric type system is inspired by the OpenTelemetry semantic model but introduces **no external dependencies**. Developers who want to export metrics to OTel or Prometheus write a bridge in their `WldtMonitoringHandler` implementation. This approach is consistent with the existing WLDT logging design philosophy.

#### Metric Types & Infrastructure Classes

| WLDT Type | Java Type | Description |
|---|---|---|
| `WldtMetric` | `abstract class` | Base class for all metric types. Carries common metadata: namespace, name, component, timestampMs. Acts as an escape hatch for composite or non-standard metrics not covered by the typed subclasses |
| `WldtCounter` | `class` | Monotonically increasing counter. Models events processed, errors, messages sent. Increment only, never reset. Carries an optional `delta` field computed by the registry before dispatch |
| `WldtUpDownCounter` | `class` | Counter for discrete countable entities that can increase and decrease. Models connected adapters, registered functions. Semantically distinct from `WldtGauge` — represents a discrete count, not a continuous observation. Carries an optional signed `delta` field computed by the registry before dispatch |
| `WldtGauge` | `class` | Continuously observed numeric value that freely rises and falls. Models queue depth, memory usage, CPU load, sensor readings. No delta — each value is an independent point-in-time observation |
| `WldtTimer` | `class` | Duration measurement in milliseconds. Models processing latency, round-trip time. Includes a `since(startMs)` factory method for convenient inline measurement. Maps to OTel `LongHistogram` or Prometheus `Summary`/`Histogram` |
| `WldtHistogram` | `class` | Distribution of observed samples carrying count, sum, min, max. Exposes a computed `getMean()` method. Models message size distribution, value spread over time. No delta — the distribution is already a self-contained aggregate |
| `WldtMetricComponent` | `enum` | Identifies the DT component that emitted the metric. Values: `DT_MODEL`, `EVENT_BUS`, `PHYSICAL_ADAPTER`, `DIGITAL_ADAPTER`, `AUGMENTATION`, `STORAGE`, `CUSTOM`. Used by `MonitoringInterface` for flag gating and callback routing |
| `MonitoringConfiguration` | `class` | Immutable configuration holding per-component enable flags and the custom metric namespace. All flags default to `false`. Built via the nested `Builder`. Provides `withAllMonitoring()` as a convenience method and `isAnyMonitoringEnabled()` for kernel-level optimisation |
| `WldtMetricRegistry` | `class` | Internal registry tracking the last known value of every active metric. Performs lazy auto-registration on the first push, computes and injects delta for counter types, and exposes `getMetric()`, `getAllMetrics()`, `isRegistered()` query methods to the developer via `MonitoringInterface`. Thread-safe via `ConcurrentHashMap` |
| `WldtMonitoringHandler` | `abstract class` | Developer extension point. Declares one callback per DT component with default no-op implementations. Developers override only the callbacks relevant to their use case. Callbacks may be invoked concurrently — implementations must handle shared state in a thread-safe manner |
| `MonitoringInterface` | `final class` | Concrete library class and central hub of the monitoring system. Instantiated by the developer, registered on `DigitalTwin`, and injected into internal components by `DigitalTwinKernel`. Responsible for flag gating, registry management, delta injection, and routing. Composes `WldtMonitoringHandler`, `WldtMetricRegistry`, and `WldtLogger`. Not intended to be extended |

#### OpenTelemetry & Prometheus Mapping

The WLDT metric type system is designed so that three out of five typed metrics map mechanically and completely to both OpenTelemetry and Prometheus. `WldtTimer` requires a one-line implementation choice in the handler. `WldtMetric` (generic) is intentionally outside any standard and serves only as an escape hatch.

| WLDT Type | OTel Equivalent | Prometheus Equivalent | Match | Notes |
|---|---|---|---|---|
| `WldtCounter` | `LongCounter` / `DoubleCounter` | `Counter` | Full | Identical semantics. Increment only. Use `getDelta()` to drive `Counter.inc()` without additional tracking. Example: `wldt.dt_model.events_processed` |
| `WldtUpDownCounter` | `LongUpDownCounter` | `Gauge` | Full | OTel has a dedicated type. Prometheus maps it to `Gauge` with no relevant semantic loss. Use `getDelta()` to drive `Gauge.inc()` / `Gauge.dec()` based on sign. Example: `wldt.physical_adapter.connected_count` |
| `WldtGauge` | `LongGauge` / `ObservableGauge` | `Gauge` | Full | Continuously observed point-in-time value. Use `getValue()` directly with `Gauge.set()`. Example: `wldt.event_bus.queue_depth` |
| `WldtTimer` | `LongHistogram` with unit `ms` | `Summary` or `Histogram` | Partial | OTel has no dedicated Timer type — duration is modelled as a `Histogram`. On the Prometheus side the developer chooses between `Summary` (server-side quantiles) and `Histogram` (client-side buckets) in the handler implementation. Example: `wldt.dt_model.processing_latency_ms` |
| `WldtHistogram` | `LongHistogram` / `DoubleHistogram` | `Histogram` | Full | count, sum, min, max map directly to native OTel and Prometheus fields. Example: `wldt.physical_adapter.message_size_bytes` |
| `WldtMetric` (generic) | None | None | No standard | Escape hatch for composite or non-standard metrics. Export logic is entirely delegated to the developer's handler implementation |
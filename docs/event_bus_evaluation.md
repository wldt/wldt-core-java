# WLDT Event Bus — Architecture and Strategy Evaluation

## 1. Event Bus Concept

`WldtEventBus` is the internal publish-subscribe backbone of the WLDT framework. Every component — `PhysicalAdapter`, `DigitalTwinModel`, `DigitalAdapter` — communicates exclusively through it, without holding direct references to one another. This decoupling lets the runtime swap, pause, or restart any component independently.

Events are scoped to a **Digital Twin ID**: a publisher announces which DT an event belongs to, and subscribers receive only events from DTs they have registered interest in. A single bus instance services the entire engine.

### WldtEvent

```java
WldtEvent<T>
  id          // unique event identifier
  type        // topic string (e.g. "physical.property.update")
  body        // typed payload T
  metadata    // Map<String,String> — arbitrary key/value pairs
  timestamp   // wall-clock time at creation
```

### Main API Methods

| Method | Description |
|--------|-------------|
| `WldtEventBus.getInstance()` | Returns the process-wide singleton (double-checked locking with `volatile`). |
| `publishEvent(dtId, pubId, event)` | Dispatches `event` to all subscribers registered for `dtId` whose filter matches `event.getType()`. |
| `subscribe(dtId, subId, filter, listener)` | Registers `listener` to receive events matching `filter` for the given `dtId`. |
| `unSubscribe(dtId, subId, filter, listener)` | Removes a previously registered subscription. |
| `setStrategy(strategy)` | Swaps the active dispatch strategy at runtime; calls `shutdown()` on the replaced strategy. |
| `matchWildCardType(eventType, filterType)` | Utility — delegates to `WldtEventFilter.matchWildCardType`. |

---

## 2. Strategy Pattern

`WldtEventBus` is a **facade**: it holds a reference to a `WldtEventBusStrategy` and forwards every operation to it. The bus itself contains no dispatch logic.

```java
public interface WldtEventBusStrategy {
    void publishEvent(String dtId, String pubId, WldtEvent<?> event, IWldtEventLogger log)
        throws EventBusException;

    void subscribe(String dtId, String subId, WldtEventFilter filter,
                   WldtEventListener listener, IWldtEventLogger log)
        throws EventBusException;

    void unSubscribe(String dtId, String subId, WldtEventFilter filter,
                     WldtEventListener listener, IWldtEventLogger log)
        throws EventBusException;

    default void shutdown() {}   // release threads / queues
}
```

Swapping strategies at runtime:

```java
WldtEventBus.getInstance().setStrategy(new PerDtAsyncStrategy());
```

The old strategy's `shutdown()` is called automatically. No caller code changes.

```mermaid
classDiagram
    class WldtEventBus {
        -volatile WldtEventBusStrategy strategy
        +getInstance() WldtEventBus
        +publishEvent(dtId, pubId, event)
        +subscribe(dtId, subId, filter, listener)
        +unSubscribe(dtId, subId, filter, listener)
        +setStrategy(strategy)
    }
    class WldtEventBusStrategy {
        <<interface>>
        +publishEvent()
        +subscribe()
        +unSubscribe()
        +shutdown()
    }
    class OldDeprecatedStrategy {
        <<deprecated>>
    }
    class PerDtAsyncStrategy
    class PerDtQueuedStrategy
    class PerTopicPerSubscriberStrategy
    class DefaultEventBusStrategy {
        <<default>>
    }

    WldtEventBus o-- WldtEventBusStrategy
    WldtEventBusStrategy <|.. OldDeprecatedStrategy
    WldtEventBusStrategy <|.. PerDtAsyncStrategy
    WldtEventBusStrategy <|.. PerDtQueuedStrategy
    WldtEventBusStrategy <|.. PerTopicPerSubscriberStrategy
    WldtEventBusStrategy <|.. DefaultEventBusStrategy
```

---

## 3. Wildcard Topic Management

`WldtEventFilter` is an `ArrayList<String>` that holds topic names or wildcard patterns. Two match modes:

**Exact match** — `filter.contains(eventType)` checks list membership directly.

**Wildcard match** — a filter entry ending in `.*` is a prefix wildcard. `matchWildCardType` strips the trailing `.*` and tests whether the incoming event type contains the prefix:

```java
// WldtEventFilter.java
public static boolean matchWildCardType(String eventType, String filterType) {
    if (isWildCardType(filterType)) {
        String targetType = filterType.replace("." + MULTI_LEVEL_WILDCARD_VALUE, "");
        return eventType.contains(targetType);   // ⚠ uses contains(), not startsWith()
    }
    return false;
}
```

> **Known limitation:** `contains()` matches anywhere in the string, not just at the start of the namespace hierarchy. A filter `physical.*` would incorrectly match `my.physical.property` in addition to `physical.temperature`. A correct implementation would use `eventType.startsWith(prefix + ".")` where `prefix` is the stripped filter.

`matchEventType` applies both checks in sequence: exact first, then iterates all wildcard entries if no exact match is found.

---

## 4. Strategy Implementations

### 4.1 OldDeprecatedStrategy *(deprecated)*

> **Deprecated since WLDT 0.6.0.** Use `DefaultEventBusStrategy` instead. `OldDeprecatedStrategy` is retained for backward compatibility and for completeness in the experiment suite. New deployments should not select it.

#### Idea

The original, simplest implementation. A single Java `synchronized` lock serializes all three operations (`publishEvent`, `subscribe`, `unSubscribe`). When an event is published, the publisher's thread acquires the lock, iterates all matching subscribers, and calls `onEvent()` on each — still holding the lock.

```mermaid
flowchart LR
    subgraph pub [Publishers]
        P1[Publisher 1]
        P2[Publisher 2]
    end

    subgraph bus [OldDeprecatedStrategy]
        L{{"synchronized lock"}}
        D["sequential onEvent calls\n(in publisher thread)"]
    end

    subgraph subs [Subscribers]
        S1["Subscriber 1\nonEvent()"]
        S2["Subscriber 2\nonEvent()"]
    end

    P1 -->|publishEvent| L
    P2 -->|publishEvent| L
    L --> D
    D --> S1
    D --> S2
```

#### Features

- Zero additional threads — no executor or queue overhead.
- Strict global ordering across all publishers and subscribers for a given DT.
- Minimal memory footprint (`HashMap` of `ArrayList`).
- Correct for all standard WLDT use cases where event rates are low and subscriber processing is fast.

#### Negative Aspects

- **Publisher blocking:** `publishEvent()` does not return until every subscriber has finished processing. If a subscriber sleeps 200 ms, the publisher is delayed by 200 ms × number-of-subscribers.
- **No concurrency:** the single lock prevents any parallel publish operations, even for unrelated DTs.
- **Throughput ceiling:** effective publish rate is bounded by `1 / (numSubscribers × subscriberProcessingTime)` when the subscriber is slower than the publish rate.
- **Lock contention:** with multiple concurrent publisher threads, all threads queue up on the same monitor, adding serialization overhead proportional to subscriber execution time.

---

### 4.2 PerDtAsyncStrategy

#### Idea

Each Digital Twin gets its own **single-threaded executor** (`ExecutorService`). `publishEvent()` returns immediately after snapshotting the subscriber list (from `CopyOnWriteArrayList`) and submitting a dispatch task to the DT's executor. The consumer thread delivers events to subscribers sequentially in FIFO order.

```mermaid
flowchart LR
    subgraph pub [Publishers]
        P1[Publisher 1]
        P2[Publisher 2]
    end

    subgraph bus [PerDtAsyncStrategy — one executor per DT]
        Snap["COW snapshot\nat publish time"]
        EQ[("Executor\ntask queue")]
        CT["Consumer Thread\nsingle-threaded executor"]
        D["sequential\nfan-out"]
    end

    subgraph subs [Subscribers]
        S1["Subscriber 1\nonEvent()"]
        S2["Subscriber 2\nonEvent()"]
    end

    P1 -->|publishEvent| Snap
    P2 -->|publishEvent| Snap
    Snap -->|submit| EQ
    EQ -->|take| CT
    CT --> D
    D --> S1
    D --> S2
```

#### Features

- Publisher is never blocked by subscriber processing.
- Strict FIFO ordering per DT (guaranteed by the single consumer thread).
- DTs are fully parallel — different DTs dispatch concurrently.
- Subscriber list is read at publish time (snapshot), giving a consistent view even if subscriptions change concurrently.
- Low resource overhead: one thread per active DT.

#### Negative Aspects

- **Sequential fan-out:** all subscribers for a DT share one consumer thread. A slow subscriber delays all subsequent events and all other subscribers — similar to OldDeprecatedStrategy, but the publisher is not blocked.
- **Queue accumulation:** if the subscriber processes events slower than they arrive (`processingMs > inter_arrival_ms`), the queue grows unboundedly and E2E latency increases proportionally to `N × (processingMs − inter_arrival_ms)` for the N-th event.
- **Stale snapshot:** subscriber state captured at publish time; a subscriber added after `publishEvent()` but before the consumer picks up the task will not receive that event.
- **No backpressure:** the internal executor queue is unbounded; memory can grow without limit under sustained overload.

---

### 4.3 PerDtQueuedStrategy

#### Idea

Each Digital Twin owns an explicit **`LinkedBlockingQueue<DeliveryTask>`** and a configurable thread pool of consumer threads. Publishers call `queue.put()` and return immediately. Consumer threads call `queue.take()` and read subscriber state at **delivery time**, not at publish time. Queue capacity and thread pool size are constructor parameters.

```mermaid
flowchart LR
    subgraph pub [Publishers]
        P1[Publisher 1]
        P2[Publisher 2]
    end

    subgraph bus [PerDtQueuedStrategy — one queue + pool per DT]
        Q[("LinkedBlockingQueue\nDeliveryTask")]
        CT["Consumer Thread(s)\nreads subscriber map\nat delivery time"]
        D["sequential\nfan-out"]
    end

    subgraph subs [Subscribers]
        S1["Subscriber 1\nonEvent()"]
        S2["Subscriber 2\nonEvent()"]
    end

    P1 -->|put| Q
    P2 -->|put| Q
    Q -->|take| CT
    CT --> D
    D --> S1
    D --> S2
```

#### Features

- Publisher never blocks (unless bounded queue is full — backpressure path).
- Subscriber state resolved at delivery time: a subscriber registered after `publishEvent()` but before the consumer picks up the event will receive it.
- Configurable bounded queue capacity for explicit backpressure control.
- Configurable thread pool size: `threadPoolSize=1` → strict FIFO; `threadPoolSize>1` → concurrent dispatch with no ordering guarantee.
- DTs are fully independent — different DTs use different queues and pools.

#### Negative Aspects

- **Sequential fan-out (default config):** same as PerDtAsync — with one consumer thread, subscribers are called sequentially; a slow subscriber delays all others and all subsequent events.
- **Queue saturation:** identical unbounded growth risk as PerDtAsync when the subscriber is slower than the publisher.
- **No subscriber isolation:** subscribers share the DT consumer thread(s); one slow subscriber blocks all others for that DT.
- **Ordering not guaranteed** with `threadPoolSize > 1`.

---

### 4.4 PerTopicPerSubscriberStrategy

#### Idea

The most granular strategy. Each topic and each subscriber get their own dedicated thread:

- One **topic-reader thread** per (DT, topic) pair — reads from a `LinkedBlockingQueue<EnqueuedEvent>` and fans out to subscriber queues.
- One **subscriber-processor thread** per subscriber ID — drains that subscriber's `PriorityBlockingQueue<TimestampedDelivery>` (ordered by publish-time nanoseconds + monotonic sequence).

A `Set<String> dispatched` inside each topic-reader dispatch prevents double-delivery when a subscriber matches both an exact topic and a wildcard filter.

```mermaid
flowchart LR
    subgraph pub [Publishers]
        P1[Publisher 1]
        P2[Publisher 2]
    end

    subgraph tq [Topic Queues — one per topic per DT]
        TQA[("TopicQueue\ntopic-A")]
        TQB[("TopicQueue\ntopic-B")]
    end

    subgraph tr [Topic Readers — one thread per topic per DT]
        TRA["TopicReader\ntopic-A"]
        TRB["TopicReader\ntopic-B"]
    end

    subgraph sq [Subscriber Queues — one per subscriber ID]
        SQ1[("PriorityQueue\nsub-1\nordered by nanos")]
        SQ2[("PriorityQueue\nsub-2\nordered by nanos")]
    end

    subgraph sp [Subscriber Processors — one thread per subscriber ID]
        SP1["SubProcessor\nsub-1"]
        SP2["SubProcessor\nsub-2"]
    end

    subgraph subs [Subscribers]
        S1["Subscriber 1\nonEvent()"]
        S2["Subscriber 2\nonEvent()"]
    end

    P1 -->|put| TQA
    P1 -->|put| TQB
    P2 -->|put| TQA
    P2 -->|put| TQB

    TQA -->|take| TRA
    TQB -->|take| TRB

    TRA -->|offer| SQ1
    TRA -->|offer| SQ2
    TRB -->|offer| SQ1
    TRB -->|offer| SQ2

    SQ1 -->|take| SP1
    SQ2 -->|take| SP2

    SP1 --> S1
    SP2 --> S2
```

#### Features

- **Publisher isolation:** `publishEvent()` returns immediately after `queue.put()`, never blocked by subscriber processing.
- **Subscriber isolation:** each subscriber runs in its own thread — a slow subscriber cannot delay delivery to any other subscriber (O(1) latency per subscriber regardless of fan-out count).
- **Temporal ordering:** within a topic, events arrive at each subscriber's priority queue in strict publish-time order. Events from different topics are merged by publish-time nanos + sequence number.
- **Wildcard laziness:** topic queues for wildcard-only subscribers are created on first publish, avoiding upfront thread creation for topics that may never be used.
- **Double-delivery prevention:** `dispatched` set ensures a subscriber matched by both exact and wildcard entries receives the event exactly once.
- **Maximum throughput** with many subscribers: fan-out is parallel, not sequential.

#### Negative Aspects

- **Thread count:** O(topics × DTs) topic-reader threads + O(subscribers) subscriber-processor threads. For 100 topics × 10 DTs + 50 subscribers, that is ~1050 daemon threads — feasible for JVM but non-trivial.
- **Queue saturation still possible:** if a subscriber processes events slower than they arrive, its `PriorityBlockingQueue` grows unboundedly. The priority queue also has O(log n) insertion vs. O(1) for `LinkedBlockingQueue`, adding overhead under high load.
- **Wildcard resolution at publish time:** wildcard subscriber lookup happens in the topic-reader thread on every event delivery — O(subscribers) scan.
- **Cross-topic ordering is best-effort:** events from different topics dispatched by different topic-reader threads may arrive in subscriber queues with clock skew when two publishers publish within the same nanosecond window.
- **Higher cold-start cost:** subscribe() eagerly creates per-topic queues and threads for non-wildcard filters.
- **Complex shutdown:** must drain and stop O(topics × DTs + subscribers) thread pools.

---

### 4.5 DefaultEventBusStrategy *(default)*

> This is the **default strategy** used by `WldtEventBus` when no explicit `setStrategy()` call is made. It was designed as the production-ready default for the framework after experimental validation confirmed it outperforms all other strategies across the majority of WLDT workload profiles.

#### Idea

Combines a **per-DT bounded queue** for publisher isolation with **per-subscriber dedicated threads** for subscriber isolation. It is a two-stage pipeline: the DT consumer fan-outs into per-subscriber queues, and each subscriber has an independent processor thread that invokes `onEvent()`.

```mermaid
flowchart LR
    subgraph pub [Publishers]
        P1[Publisher 1]
        P2[Publisher 2]
    end

    subgraph dt_stage [DT Stage — one queue + consumer pool per DT]
        DQ[("LinkedBlockingQueue\nDeliveryTask\n[per DT]")]
        DC["DT Consumer Thread(s)\nreads subscriber map\nat delivery time"]
    end

    subgraph sub_stage [Subscriber Stage — one queue + thread per subscriber]
        SQ1[("LinkedBlockingQueue\nSubscriberDelivery\n[sub-0]")]
        SQ2[("LinkedBlockingQueue\nSubscriberDelivery\n[sub-1]")]
        SP1["Sub Processor\nThread\n[sub-0]"]
        SP2["Sub Processor\nThread\n[sub-1]"]
    end

    subgraph subs [Subscribers]
        S1["Subscriber 1\nonEvent()"]
        S2["Subscriber 2\nonEvent()"]
    end

    P1 -->|put| DQ
    P2 -->|put| DQ
    DQ -->|take| DC
    DC -->|offer| SQ1
    DC -->|offer| SQ2
    SQ1 -->|take| SP1
    SQ2 -->|take| SP2
    SP1 --> S1
    SP2 --> S2
```

Delivery pipeline per Digital Twin:

```
publishEvent()
  → LinkedBlockingQueue<DeliveryTask> [dtId]       (bounded if configured — backpressure path)
    → DT consumer thread(s)                        (configurable pool, drains the DT queue)
      → resolves exact + wildcard subscribers      (dispatched-set dedup, state read at delivery time)
      → fan-out → LinkedBlockingQueue<SubscriberDelivery> [subscriberId]
        → subscriber processor thread              (one per subscriber, blocked on take())
          → onEvent() callback
```

Constructors:

```java
new DefaultEventBusStrategy()                          // threadPoolSize=1, unbounded queue
new DefaultEventBusStrategy(int threadPoolSize)        // unbounded queue
new DefaultEventBusStrategy(int threadPoolSize, int queueCapacity)  // backpressure enabled
```

#### Features

- **Publisher isolation:** `publishEvent()` returns immediately after `queue.put()`; never blocked by subscriber processing.
- **Subscriber isolation:** each subscriber runs in its own dedicated thread — a slow subscriber cannot delay delivery to any other subscriber.
- **FIFO ordering:** events arrive at subscriber queues in the same order they were placed in the DT queue. Because the DT queue is already ordered, a `LinkedBlockingQueue` (not `PriorityBlockingQueue`) suffices for subscriber queues — O(1) enqueue/dequeue vs. O(log n).
- **Backpressure:** configurable bounded DT queue — `put()` blocks when full, giving the publisher natural back-pressure without unbounded memory growth.
- **Late-subscription delivery:** subscriber map is read at DT-consumer delivery time, not at publish time — a subscriber registered between `publishEvent()` and DT-consumer pickup may receive the event.
- **Double-delivery prevention:** `dispatched` set per delivery task prevents a subscriber matched by both exact and wildcard filters from receiving the event twice.
- **Thread count:** O(DTs) DT consumer threads + O(subscribers) subscriber-processor threads — significantly lower than `PerTopicPerSubscriberStrategy` for multi-topic workloads.
- **Wildcard dedup:** same `dispatched`-set mechanism as `PerTopicPerSubscriberStrategy`, without the per-topic thread overhead.

#### Configuration guidance

| Parameter | Recommended value | Effect |
|-----------|------------------|--------|
| `threadPoolSize` | 1 (default) | Strict FIFO per DT |
| `threadPoolSize` > 1 | Match CPU cores | Concurrent DT dispatch — no ordering guarantee |
| `queueCapacity` | Integer.MAX_VALUE (default) | No backpressure; queue grows freely |
| `queueCapacity` = N | e.g., 1000–10000 | `publishEvent()` blocks when DT queue reaches N |

#### Negative Aspects

- **Asynchronous delivery:** `onEvent()` is not called in the publisher thread, so `EventBusCorrectnessTest` assertions must use `CountDownLatch` or target `OldDeprecatedStrategy` explicitly if synchronous-delivery semantics are required for testing.
- **Thread proliferation with many subscribers:** O(subscribers) processor threads. For DTs with hundreds of subscribers, consider `PerDtQueuedStrategy` instead.
- **Not suited for strict cross-subscriber ordering** with `threadPoolSize > 1`: the DT consumer pool dispatches events concurrently, so subscriber queues may receive events out of order across concurrent consumer threads.

---

## 5. Strategy Comparison

### Qualitative Summary

| Property | OldDeprecated (Sync) | PerDtAsync | PerDtQueued | PerTopicPerSub | **Default** |
|----------|:--------------------:|:----------:|:-----------:|:--------------:|:-----------:|
| Publisher blocked by subscriber | Yes | No | No | No | **No** |
| Subscriber isolated from others | No | No | No | **Yes** | **Yes** |
| FIFO ordering guaranteed | Yes (global lock) | Yes (per DT) | Yes (1 consumer) | Yes (per topic) | Yes (per DT + per sub) |
| Cross-topic ordering | Yes (global) | Yes (per DT) | Yes (per DT) | Best-effort | Yes (per DT) |
| Backpressure support | N/A | No | **Yes** (bounded queue) | No | **Yes** (bounded DT queue) |
| Subscriber state at deliver time | At publish time | At publish time | **At delivery time** | **At delivery time** | **At delivery time** |
| Thread overhead | None | 1 per DT | N per DT | O(topics×DTs + subs) | O(DTs + subs) |
| Memory overhead | Minimal | Low | Low | Moderate | **Low** |
| Wildcard double-delivery safe | Yes | Yes | Yes | **Yes** (dispatched set) | **Yes** (dispatched set) |
| Sub-queue data structure | N/A | N/A | N/A | PriorityBlockingQueue O(log n) | **LinkedBlockingQueue O(1)** |

### Performance Characteristics (Empirical)

All scenarios used 4 publishers, 4 subscribers, 10 msg/s baseline, 100 KB payload unless stated. Results are approximate representative values from the experiment suite.

**Scenario: Subscriber processing time variation (10–100 ms)**

| Processing time | OldDepr. p50 | PerDtAsync p50 | PerDtQueued p50 | PerTopicPerSub p50 | **Default p50** |
|-----------------|:------------:|:--------------:|:---------------:|:------------------:|:---------------:|
| 1 ms | ~1 ms | ~0.3 ms | ~0.3 ms | ~0.3 ms | ~0.3 ms |
| 10 ms | ~10 ms | ~0.3 ms | ~0.3 ms | ~0.3 ms | ~0.3 ms |
| 100 ms | ~100 ms | queued | queued | ~1 ms | ~1 ms |

*OldDeprecated p50 equals subscriber processing time because the publisher blocks. Async/Queued decouple the publisher but the shared consumer thread queues up. PerTopicPerSub and Default absorb multi-subscriber fan-out in parallel.*

**Scenario: Subscriber count variation (1–10, 4 publishers)**

| Subscribers | OldDepr. p50 | PerDtAsync p50 | PerDtQueued p50 | PerTopicPerSub p50 | **Default p50** |
|-------------|:------------:|:--------------:|:---------------:|:------------------:|:---------------:|
| 1 | ~1 ms | ~1 ms | ~1 ms | ~1 ms | ~1 ms |
| 4 | ~4 ms | ~4 ms | ~4 ms | ~1 ms | ~1 ms |
| 10 | ~10 ms | ~10 ms | ~10 ms | ~1 ms | ~1 ms |

*Default and PerTopicPerSub are the only strategies where fan-out latency is O(1) per subscriber rather than O(N).*

**Scenario: Topic count variation (2–100, 4 pub × 4 sub, 200 KB, 10 msg/s)**

All strategies show flat latency as topic count scales from 2 to 100. The routing layer is not the bottleneck — subscriber processing capacity is. Default and PerTopicPerSub maintain their ~4× advantage over Async/Queued at all topic counts due to parallel subscriber threads.

### Selection Guide

| Use Case | Recommended Strategy |
|----------|----------------------|
| Testing synchronous event delivery (no latches needed) | `OldDeprecatedStrategy` |
| Low event rate, correctness-critical, single subscriber | `OldDeprecatedStrategy` |
| Moderate rate, publisher decoupling, few subscribers | `PerDtAsyncStrategy` |
| Need bounded queue / backpressure, late-subscription delivery | `PerDtQueuedStrategy` |
| Strict temporal cross-topic ordering required | `PerTopicPerSubscriberStrategy` |
| General purpose — publisher isolation + subscriber isolation | `DefaultEventBusStrategy` *(default)* |
| Need to tune at runtime without changing callers | Any — swap via `setStrategy()` |

---

## 6. Subscriber Execution Models

### 6.1 Overview

The bus strategy controls **how events reach subscribers** — but once `onEvent()` is invoked, the strategy's job is done. What happens inside `onEvent()` is entirely the subscriber's responsibility, and this choice has a direct impact on the throughput and latency visible to the rest of the system.

WLDT supports two subscriber execution models, each implemented as a `WldtEventListener`. They can be assigned independently to each subscriber — there is no requirement that all subscribers in the same DT use the same model. A single DT can mix sequential and parallel subscribers on different topics based on their processing requirements.

| Model | `onEvent()` behavior | Dispatch thread impact |
|-------|----------------------|------------------------|
| **Sequential** | Processes inline; returns only after work is done | Blocked for the full `processingMs` |
| **Parallel** | Submits work to a fixed thread pool; returns immediately | Returns in microseconds regardless of `processingMs` |

> **Important:** E2E delivery latency (measured from `publishEvent()` to entry of `onEvent()`) is unaffected by the subscriber model. The model only affects how long the dispatch thread is held and how much concurrent processing capacity a subscriber can absorb.

---

### 6.2 Sequential Subscriber

#### Concept

The simplest model. The `onEvent()` callback runs all processing inline in whatever thread the bus strategy dispatched on — the publisher thread (OldDeprecated), the DT consumer thread (Async/Queued/Default), or the per-subscriber processor thread (PerTopicPerSub). The next event for this subscriber cannot begin until the current one's processing is fully complete.

```mermaid
flowchart LR
    subgraph dt [Dispatch Thread — strategy consumer or publisher thread]
        direction LR
        E1["Event N dequeued"]
        BLOCKED["⏸ dispatch blocked\nfor processingMs"]
        E2["Event N+1 dequeued"]
    end

    subgraph seq [Sequential Subscriber]
        OE["onEvent()"]
        WORK["inline processing\n[processingMs]"]
        RET["return"]
    end

    E1 -->|"call"| OE
    OE --> WORK
    WORK --> RET
    RET -->|"unblocks"| BLOCKED
    BLOCKED --> E2
```

#### Characteristics

- Zero additional threads or memory allocation per event.
- Strict per-subscriber ordering: events are always processed in the order they arrived.
- Simple error handling — exceptions propagate naturally to the caller.
- Best choice when processing is fast (sub-millisecond in-memory operations) or when strict ordering is a correctness requirement (e.g., DT state machine transitions).
- Becomes the bottleneck when `processingMs > inter_arrival_ms`: the dispatch thread saturates and queue depth grows unboundedly.

---

### 6.3 Parallel Subscriber

#### Concept

`onEvent()` submits the actual processing work to a per-subscriber `ExecutorService` (fixed thread pool of N threads) and returns immediately. The dispatch thread is released after a microsecond-scale handoff, regardless of how long processing takes. Up to N events per subscriber can be processed concurrently.

```mermaid
flowchart LR
    subgraph dt [Dispatch Thread]
        direction LR
        E1["Event N"]
        E2["Event N+1"]
        E3["Event N+2"]
    end

    subgraph par [Parallel Subscriber]
        OE["onEvent()\nsubmit → return immediately"]

        subgraph pool [Thread Pool — N threads]
            T1["Thread 1\nprocessing event N"]
            T2["Thread 2\nprocessing event N+1"]
            TN["Thread N\nprocessing event N+2"]
        end
    end

    E1 -->|"non-blocking call"| OE
    E2 -->|"non-blocking call"| OE
    E3 -->|"non-blocking call"| OE
    OE -->|"submit"| T1
    OE -->|"submit"| T2
    OE -->|"submit"| TN
```

#### Characteristics

- Dispatch thread overhead per event is O(1) — queue submission only.
- Throughput scales linearly with pool size up to the publish rate: `max_throughput = pool_size / processingMs`.
- Saturation point: pool size must satisfy `pool_size ≥ ceil(arrival_rate × processingMs)` to avoid queue buildup.
- Processing order within the subscriber is **not guaranteed** — pool threads race. Use when events are independent (analytics, logging, storage writes, external notifications).
- Requires careful shutdown: the pool must be explicitly terminated after the last event is processed.

---

### 6.4 Mixed-Subscriber Topologies in a Digital Twin

In a production Digital Twin, different components consume events for fundamentally different reasons. There is no requirement that all subscribers use the same execution model — the bus treats them identically. The execution model is chosen per subscriber based on the role that subscriber plays in the DT lifecycle.

```mermaid
flowchart TD
    subgraph pub [Publishers]
        PA[PhysicalAdapter\nphysical.property.*\nphysical.event.*]
        MA[ModelAdapter\ndt.state.*]
    end

    subgraph bus [WldtEventBus]
        B["dispatch\nany strategy"]
    end

    subgraph seq [Sequential Subscribers\nonEvent blocks dispatch thread]
        SS1["StateManager\ntopic: physical.property.*\nSequential — in-memory update\nProcessing: &lt;1 ms\nOrdering: required"]
        SS2["StateMachine\ntopic: physical.event.*\nSequential — transition logic\nProcessing: &lt;1 ms\nOrdering: required"]
    end

    subgraph par [Parallel Subscribers\nonEvent returns immediately]
        PS1["StorageWriter\ntopic: dt.state.*\nParallel pool=8 — I/O bound\nProcessing: 20-100 ms\nOrdering: not required"]
        PS2["AnalyticsPipeline\ntopic: physical.*\nParallel pool=4 — compute bound\nProcessing: 10-50 ms\nOrdering: not required"]
        PS3["NotificationService\ntopic: physical.event.*\nParallel pool=2 — outbound HTTP\nProcessing: 50-500 ms\nOrdering: not required"]
    end

    PA --> B
    MA --> B
    B -->|"⏸ blocks"| SS1
    B -->|"⏸ blocks"| SS2
    B -->|"▶ non-blocking"| PS1
    B -->|"▶ non-blocking"| PS2
    B -->|"▶ non-blocking"| PS3
```

#### Design Rationale

**Sequential subscribers should be used when:**
- Processing is fast enough that blocking the dispatch thread has negligible impact (`processingMs` ≪ `1 / arrival_rate`).
- Event ordering is a correctness invariant — e.g., a DT state machine that must process property updates in arrival order to remain consistent.
- The subscriber is the sole consumer for its topic and will never become the throughput bottleneck.

**Parallel subscribers should be used when:**
- Processing is slow relative to the event arrival rate (I/O, HTTP calls, database writes, heavy computation).
- Events are independent of one another — ordering violations are acceptable or the subscriber handles deduplication internally.
- The subscriber needs to absorb bursts without stalling the bus dispatch thread.

> **Interaction with DefaultEventBusStrategy and PerTopicPerSubscriberStrategy:** with these strategies, each subscriber already runs in its own dedicated thread, so a sequential subscriber only blocks its own processor thread — not other subscribers. This makes the sequential/parallel choice less critical for fan-out scenarios: you can use sequential everywhere and still achieve per-subscriber parallelism at the bus level.

#### Subscriber Model Selection Per DT Role

| DT Component | Typical Role | Recommended Model | Pool Size |
|---|---|---|---|
| `DigitalTwinModel` (shadow function) | In-memory state update | Sequential | — |
| State machine / event handler | Transition logic | Sequential | — |
| `StorageManager` / persistence | DB / file write | Parallel | 4–16 (I/O bound) |
| Analytics / aggregation | Compute-heavy pipeline | Parallel | CPU count |
| External notification | HTTP / MQTT outbound | Parallel | 2–8 |
| Audit / debug logger | Low-priority logging | Sequential | — |

---

### 6.5 Interaction with Bus Strategies

The subscriber execution model interacts differently with each bus strategy:

```mermaid
flowchart LR
    subgraph default_s [OldDeprecated Strategy\n+ Sequential Sub]
        D_P[Publisher] -->|"acquires lock"| D_L{{"lock"}}
        D_L -->|"calls onEvent\nblocks"| D_S["Sub\nprocessing\n[processingMs]"]
        D_S -->|"return\nreleases lock"| D_P
    end
```

```mermaid
flowchart LR
    subgraph default_p [OldDeprecated Strategy\n+ Parallel Sub]
        DP_P[Publisher] -->|"acquires lock"| DP_L{{"lock"}}
        DP_L -->|"calls onEvent\nreturns in µs"| DP_S["Sub\nonEvent()"]
        DP_S -->|"submit"| DP_POOL[("Thread Pool")]
        DP_S -->|"immediate return\nreleases lock"| DP_P
    end
```

```mermaid
flowchart LR
    subgraph async_s [PerDtAsync or PerDtQueued\n+ Sequential Sub]
        AS_P[Publisher] -->|"put — non-blocking"| AS_Q[("DT Queue")]
        AS_Q -->|"take"| AS_C["Consumer Thread"]
        AS_C -->|"calls onEvent\nblocks consumer"| AS_S["Sub\nprocessing\n[processingMs]"]
        AS_S -->|"return\nconsumer free"| AS_C
    end
```

```mermaid
flowchart LR
    subgraph async_p [PerDtAsync or PerDtQueued\n+ Parallel Sub]
        AP_P[Publisher] -->|"put — non-blocking"| AP_Q[("DT Queue")]
        AP_Q -->|"take"| AP_C["Consumer Thread"]
        AP_C -->|"calls onEvent\nreturns in µs"| AP_S["Sub\nonEvent()"]
        AP_S -->|"submit"| AP_POOL[("Thread Pool")]
        AP_S -->|"immediate return\nconsumer immediately\nprocesses next event"| AP_C
    end
```

```mermaid
flowchart LR
    subgraph ptps [PerTopicPerSub or Default\n+ either model]
        PP_P[Publisher] -->|"put"| PP_TQ[("DT/TopicQueue")]
        PP_TQ -->|"take"| PP_TR["DT Consumer /\nTopicReader"]
        PP_TR -->|"offer"| PP_SQ[("SubQueue")]
        PP_SQ -->|"take"| PP_SP["SubProcessor\nThread"]
        PP_SP -->|"Sequential: blocks SubProcessor\nParallel: submits to pool"| PP_S["Sub\nonEvent()"]
        PP_S -.->|"Parallel only"| PP_POOL[("Thread Pool")]
    end
```

**Summary of interactions:**

| Strategy | Sequential subscriber effect | Parallel subscriber effect |
|---|---|---|
| OldDeprecated | Publisher thread blocks for `processingMs`; lock held the entire time | Publisher releases lock in µs; processing runs concurrently with next publish |
| PerDtAsync | Consumer thread stalls; queue grows if rate > 1/processingMs | Consumer thread drains queue continuously; pool absorbs processing backlog |
| PerDtQueued | Same as Async | Same as Async |
| PerTopicPerSub | SubProcessor thread stalls for this subscriber only; other subs unaffected | SubProcessor submits to pool and immediately pulls next event; highest possible throughput |
| **Default** | SubProcessor thread stalls for this subscriber only; other subs unaffected | SubProcessor submits to pool and immediately pulls next event |

---

### 6.6 Empirical Results — Scenario 9

**Setup:** 4 publishers × 4 subscribers, 100 msg/s per publisher, 500 KB payload, 10 ms processing per event. All five bus strategies tested. X-axis: subscriber thread pool size [2, 4, 8, 10, 14, 16, 18, 20].

| Pool size | Throughput (all strategies) | E2E p50 | Notes |
|:---------:|:---------------------------:|:-------:|-------|
| 2 | ~550 msg/s | ~0.01–0.08 ms | Pool under-provisioned: 2 threads × 100 events/s = 200 cap < 400 input |
| 4 | ~1 111 msg/s | ~0.01–0.07 ms | Pool matched: 4 × 100 = 400 = input rate |
| 8 | ~1 592 msg/s | ~0.04–0.05 ms | Pool surplus: 8 × 100 = 800 > 400; wall time = publish window |
| 10–20 | ~1 590–1 600 msg/s | ~0.01–0.08 ms | Hard plateau — publish rate is the bottleneck |

**Key observations:**

1. **All bus strategies converge completely** with parallel subscriber — the dispatch model becomes irrelevant because `onEvent()` returns in microseconds regardless of strategy.

2. **Linear throughput scaling** from pool=2 to pool=4: doubling threads doubles capacity when the pool is the bottleneck.

3. **Plateau at pool≥8** — the ceiling is set by the publisher side (`4 pubs × 100 msg/s × 4 subs = 1 600 msg/s`), not by the subscriber pool. Adding threads beyond the saturation point yields no benefit.

4. **E2E p50 drops to sub-millisecond** for all strategies — from tens of seconds (sequential at 10 ms proc) to 0.01–0.08 ms — because the metric captures arrival time at `onEvent()` entry, before pool submission. The bus delivers events immediately; only processing is deferred.

The saturation formula for pool sizing:
```
pool_size_min = ceil(arrival_rate_per_sub × processingMs / 1000)
             = ceil(400 msg/s × 10 ms / 1000)
             = ceil(4) = 4 threads
```

---

## 7. Experimental Validation

The `EventBusExperimentTest` suite characterizes all five strategies across 10 scenarios, each varying a single dimension while holding the rest fixed. Strategies tested: `OldDeprecatedStrategy`, `PerDtAsyncStrategy`, `PerDtQueuedStrategy`, `PerTopicPerSubscriberStrategy`, `DefaultEventBusStrategy`.

### How to run

```bash
# Run the full experiment suite
./gradlew test --tests "it.wldt.core.event.EventBusExperimentTest"

# Outputs go to:
#   metrics/event-bus/<RUN_ID>/experiments/scenario_N_*.csv

# Generate comparison graphs after the run
python metrics/event-bus/analyze.py
# or for a specific run:
python metrics/event-bus/analyze.py <RUN_ID>
```

**Fixed baseline** (unless the scenario varies them):

| Parameter | Value |
|-----------|-------|
| Message rate | 10 msg/s per publisher |
| Event size | 100 KB |
| Subscriber processing | 1 ms |

---

### 7.1 Scenario 1 — Message Rate Variation

**Varied:** publisher message rate ∈ {1, 10, 20, 50, 100, 200, 400} msg/s per publisher

**Fixed:** 4 publishers, 8 subscribers, 100 KB payload, 1 ms processing

**What it reveals:** How each strategy handles increasing traffic intensities. `OldDeprecatedStrategy` is expected to block on subscriber processing at high rates and drop effective throughput. Async/Queued strategies decouple publisher speed but accumulate queue depth when subscriber processing can't keep up. `DefaultEventBusStrategy` and `PerTopicPerSubscriberStrategy` isolate each subscriber independently and sustain the highest delivered message rates under load.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 1 — Message Rate Variation](../images/scenario_1_rate_variation.png)

---

### 7.2 Scenario 2 — Event Size Variation

**Varied:** event payload size ∈ {10, 100, 1 000, 5 000} bytes

**Fixed:** 10 msg/s, 1 publisher, 1 subscriber, 1 ms processing

**What it reveals:** Throughput sensitivity to object size — allocation pressure, GC behavior, and in-memory copy cost as events grow. With a single subscriber the fan-out differences between strategies are eliminated; this scenario isolates raw payload-size impact. All strategies should degrade similarly, though lock-based `OldDeprecatedStrategy` may show higher p99 jitter due to holding the monitor during payload handling.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 2 — Event Size Variation](../images/scenario_2_size_variation.png)

---

### 7.3 Scenario 3 — Subscriber Processing Time Variation

**Varied:** subscriber processing time ∈ {10, 20, 30, 40, 50, 60, 70, 80, 100} ms

**Fixed:** 4 publishers, 4 subscribers, 10 msg/s, 100 KB payload

**What it reveals:** The most diagnostic scenario for architectural differences. `OldDeprecatedStrategy` p50 equals `processingMs` because the publisher blocks for each subscriber in sequence. Async/Queued strategies decouple the publisher but eventually saturate their single consumer thread (queue grows unboundedly when `processingMs > inter_arrival_ms`). `DefaultEventBusStrategy` and `PerTopicPerSubscriberStrategy` maintain sub-millisecond E2E p50 regardless of subscriber processing time because each subscriber runs on an independent thread.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 3 — Subscriber Processing Time Variation](../images/scenario_3_processing_time_variation.png)

---

### 7.4 Scenario 4 — Publisher Count Variation

**Varied:** number of concurrent publishers ∈ {1, 2, 4, 6, 8, 10}

**Fixed:** 1 subscriber, 10 msg/s per publisher, 100 KB, 1 ms processing

**What it reveals:** Publisher fan-in cost under increasing contention. All publishers target the same DT ID and subscriber. `OldDeprecatedStrategy` serializes all publishers on a single monitor — lock contention grows with publisher count. Async/Queued and Default strategies use a single DT queue that absorbs concurrent puts without lock contention beyond the CAS on the queue head.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 4 — Publisher Count Variation](../images/scenario_4_publisher_count_variation.png)

---

### 7.5 Scenario 5 — Subscriber Count Variation

**Varied:** number of subscribers ∈ {1, 2, 4, 6, 8, 10}

**Fixed:** 4 publishers, 10 msg/s per publisher, 100 KB, 1 ms processing

**What it reveals:** Fan-out cost as subscriber count increases. `OldDeprecatedStrategy` and single-threaded strategies (Async, Queued) call `onEvent()` sequentially — E2E p50 scales as O(N × processingMs). `DefaultEventBusStrategy` and `PerTopicPerSubscriberStrategy` dispatch to each subscriber's independent queue and thread — E2E p50 stays constant regardless of subscriber count.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 5 — Subscriber Count Variation](../images/scenario_5_subscriber_count_variation.png)

---

### 7.6 Scenario 6 — Large Payload × Rate Variation

**Varied:** publisher message rate ∈ {1, 5, 10, 20, 50, 100} msg/s

**Fixed:** 4 publishers, 4 subscribers, 500 KB payload, 1 ms processing

**What it reveals:** Delivery throughput under large payload sizes at increasing rates. At 100 msg/s × 500 KB = 50 MB/s effective data rate, all strategies face GC pressure and memory bandwidth limits. No artificial processing delay — this scenario isolates raw delivery capacity. Strategies with per-subscriber queues (Default, PerTopicPerSub) show lower p99 variability under burst because the DT queue acts as a shock absorber before fan-out.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 6 — Large Payload Rate Variation](../images/scenario_6_large_payload_rate_variation.png)

---

### 7.7 Scenario 7 — High Rate × Event Size Variation

**Varied:** event payload size ∈ {100 KB, 200 KB, 500 KB, 750 KB, 1 MB}

**Fixed:** 100 msg/s per publisher, 4 publishers, 4 subscribers, 1 ms processing

**What it reveals:** How each strategy degrades as per-message data volume grows at a sustained high rate. At 100 msg/s × 1 MB = 100 MB/s the JVM heap allocation rate becomes the dominant bottleneck regardless of strategy. Differences between strategies emerge at intermediate sizes (200–500 KB) where allocation pressure is significant but not yet GC-catastrophic. `OldDeprecatedStrategy` shows highest p99 because holding the lock during payload handling prevents other publishers from proceeding.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 7 — High Rate Size Variation](../images/scenario_7_high_rate_size_variation.png)

---

### 7.8 Scenario 8 — Topic Count Variation

**Varied:** number of topics ∈ {2, 4, 8, 10, 20, 40, 80, 100}

**Fixed:** 10 msg/s per publisher, 4 publishers, 4 subscribers, 200 KB, 1 ms processing

**What it reveals:** Routing overhead as topic count scales. `PerTopicPerSubscriberStrategy` creates one thread per (topic × DT) pair — at 100 topics this means 100 topic-reader threads. The other strategies use a single DT queue regardless of topic count (O(1) routing overhead). The key question is whether `PerTopicPerSubscriberStrategy`'s thread explosion degrades JVM performance relative to `DefaultEventBusStrategy`'s flat per-subscriber thread model.

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 8 — Topic Count Variation](../images/scenario_8_topic_count_variation.png)

---

### 7.9 Scenario 9 — Parallel Subscriber Pool Size Variation

**Varied:** subscriber thread pool size ∈ {2, 4, 8, 10, 14, 16, 18, 20}

**Fixed:** 100 msg/s per publisher, 4 publishers, 4 subscribers, 500 KB, 10 ms processing (parallel subscribers)

**What it reveals:** Intra-subscriber parallelism and interaction between the bus dispatch model and the parallel subscriber pool. Each subscriber uses `onEvent() → pool.submit()`, so the bus dispatch thread is always released in microseconds. The ceiling is determined by `pool_size ≥ ceil(arrival_rate × processingMs)` — below the threshold, throughput scales linearly with pool size; above it, all strategies plateau at the publish rate. Strategies converge at sufficient pool size because the bus becomes irrelevant when the subscriber is the bottleneck.

Saturation formula:
```
pool_size_min = ceil(arrival_rate_per_sub × processingMs / 1000)
             = ceil(400 msg/s × 10 ms / 1000) = 4 threads
```

> *Graph generated by `python analyze.py` after running the test suite.*
>
> ![Scenario 9 — Parallel Subscriber Pool Size Variation](../images/scenario_9_parallel_subscriber_pool_variation.png)

---

### 7.10 Scenario 10 — Mixed Publishers & Mixed Subscribers (Time-Windowed)

**Setup:**
- 4 publishers at independent rates: {100, 50, 25, 10} Hz (total: 185 Hz)
- 2 sequential subscribers + 2 parallel subscribers (pool size = 16 each)
- Payload: 500 KB per event
- Processing: Uniform[10, 100] ms per event
- Duration: 5 minutes (300 seconds)
- Metric aggregation: 10-second tumbling windows

**What it reveals:** Sustained-load behavior under realistic heterogeneous conditions. Sequential subscribers block their dispatch thread for 10–100 ms per event, introducing queue pressure that grows over time when the arrival rate exceeds processing capacity. Parallel subscribers absorb the same load without blocking the dispatch thread. The time-window view exposes:

- Whether latency is stable or grows monotonically (queue saturation)
- How well each strategy isolates sequential subscribers from parallel ones
- Rate differences between the fast publisher (100 Hz) and slow publisher (10 Hz) reflected in per-subscriber delivery rates
- Out-of-order delivery under load (strategies without per-subscriber threads may reorder events from different publisher rates)

`DefaultEventBusStrategy` is the expected winner: sequential subscribers run on their own threads (queue pressure stays local), parallel subscribers return immediately from `onEvent()`, and both subscriber types are isolated from each other. `OldDeprecatedStrategy` is expected to saturate early due to the publisher blocking for every event.

> *One PNG is generated per strategy by `python analyze.py`, showing 4 time-series panels (p50, p99, message rate, out-of-order %) per subscriber.*
>
> **OldDeprecated:**
> ![Scenario 10 — OldDeprecated](../images/scenario_10_old_deprecated.png)
>
> **PerDtAsync:**
> ![Scenario 10 — PerDtAsync](../images/scenario_10_perdt_async.png)
>
> **PerDtQueued:**
> ![Scenario 10 — PerDtQueued](../images/scenario_10_perdt_queued.png)
>
> **PerTopicPerSubscriber:**
> ![Scenario 10 — PerTopicPerSubscriber](../images/scenario_10_per_topic_per_subscriber.png)
>
> **Default:**
> ![Scenario 10 — Default](../images/scenario_10_default.png)

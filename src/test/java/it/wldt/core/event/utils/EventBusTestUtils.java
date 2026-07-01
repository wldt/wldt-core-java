package it.wldt.core.event.utils;

import it.wldt.core.event.*;
import it.wldt.exception.EventBusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * Shared utilities for EventBus test isolation, metrics output, and experiment execution.
 *
 * RUN_ID is a single timestamp shared across all test classes in one JVM session,
 * so every test run produces one timestamped folder containing subfolders per experiment type.
 *
 * Folder layout:
 *   <metrics-dir>/<RUN_ID>/experiments/   ← EventBusExperimentTest CSVs + graphs
 *
 * CSV output root is configurable at runtime:
 *   ./gradlew test -Devent.bus.metrics.dir=/path/to/output
 * Default: metrics/event-bus (relative to working directory)
 *
 * Subscriber execution models:
 *   Sequential (subscriberThreadPoolSize == 1): onEvent() calls busySleepMs inline;
 *     the strategy's consumer thread (or publisher thread for Default) is blocked
 *     for the full processing time before the next event is dispatched.
 *   Parallel (subscriberThreadPoolSize > 1): onEvent() submits processing to a
 *     per-subscriber fixed thread pool and returns immediately, allowing the strategy's
 *     consumer thread to pick up the next event without waiting.
 */
public final class EventBusTestUtils {

    private static final String METRICS_DIR_PROP    = "event.bus.metrics.dir";
    private static final String DEFAULT_METRICS_DIR = "metrics/event-bus";
    private static final DateTimeFormatter TS_FMT   = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Shared run ID — computed once at class-load time for the whole JVM session. */
    public static final String RUN_ID = LocalDateTime.now().format(TS_FMT);

    // ── Strategy registry ──────────────────────────────────────────────────────

    public static final String   STRATEGY_DEFAULT                   = "default";
    public static final String   STRATEGY_ASYNC                     = "perdt_async";
    public static final String   STRATEGY_QUEUED                    = "perdt_queued";
    public static final String   STRATEGY_TOPIC_SUBSCRIBER          = "per_topic_per_subscriber";
    public static final String   STRATEGY_OLD_DEPRECATED            = "old_deprecated";
    public static final String[] STRATEGY_NAMES                     = {
            STRATEGY_OLD_DEPRECATED, STRATEGY_ASYNC, STRATEGY_QUEUED,
            STRATEGY_TOPIC_SUBSCRIBER, STRATEGY_DEFAULT };

    public static WldtEventBusStrategy createStrategy(String name) {
        switch (name) {
            case STRATEGY_OLD_DEPRECATED:   return new OldDeprecatedStrategy();
            case STRATEGY_ASYNC:            return new PerDtAsyncStrategy();
            case STRATEGY_QUEUED:           return new PerDtQueuedStrategy();
            case STRATEGY_TOPIC_SUBSCRIBER: return new PerTopicPerSubscriberStrategy();
            case STRATEGY_DEFAULT:          return new DefaultEventBusStrategy();
            default: throw new IllegalArgumentException("Unknown strategy: " + name);
        }
    }

    private EventBusTestUtils() {}

    // ── Bus lifecycle ──────────────────────────────────────────────────────────

    /**
     * Shuts down the current strategy (releases executor threads etc.) then
     * nulls the WldtEventBus singleton so the next test starts fresh.
     */
    public static void resetEventBus() throws Exception {
        Field instanceField = WldtEventBus.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        WldtEventBus current = (WldtEventBus) instanceField.get(null);
        if (current != null) {
            Field stratField = WldtEventBus.class.getDeclaredField("strategy");
            stratField.setAccessible(true);
            ((WldtEventBusStrategy) stratField.get(current)).shutdown();
        }
        instanceField.set(null, null);
    }

    // ── CSV helpers ────────────────────────────────────────────────────────────

    /** Returns: <metrics-dir>/<RUN_ID>/<testType>/<name>.csv */
    public static Path csvPath(String testType, String name) {
        String dir = System.getProperty(METRICS_DIR_PROP, DEFAULT_METRICS_DIR);
        return Paths.get(dir, RUN_ID, testType, name + ".csv");
    }

    /** Writes a key-value summary CSV (header: metric,value). Creates parent directories. */
    public static void writeSummaryCsv(Path file, Map<String, Object> metrics) throws IOException {
        Files.createDirectories(file.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println("metric,value");
            for (Map.Entry<String, Object> e : metrics.entrySet())
                w.printf("%s,%s%n", e.getKey(), e.getValue());
        }
    }

    // ── Listener factories ─────────────────────────────────────────────────────

    private static final WldtEventListener NO_OP = new WldtEventListener() {
        @Override public void onEventSubscribed(String t)   {}
        @Override public void onEventUnSubscribed(String t) {}
        @Override public void onEvent(WldtEvent<?> e)       {}
    };

    public static WldtEventListener noOpListener() { return NO_OP; }

    public static WldtEventListener countingListener(AtomicInteger counter) {
        return new WldtEventListener() {
            @Override public void onEventSubscribed(String t)   {}
            @Override public void onEventUnSubscribed(String t) {}
            @Override public void onEvent(WldtEvent<?> e) { counter.incrementAndGet(); }
        };
    }

    public static WldtEventListener countingAndLatchListener(AtomicInteger counter, CountDownLatch latch) {
        return new WldtEventListener() {
            @Override public void onEventSubscribed(String t)   {}
            @Override public void onEventUnSubscribed(String t) {}
            @Override public void onEvent(WldtEvent<?> e) {
                counter.incrementAndGet();
                latch.countDown();
            }
        };
    }

    public static WldtEventListener capturingListener(WldtEvent<?>[] capture) {
        return new WldtEventListener() {
            @Override public void onEventSubscribed(String t)   {}
            @Override public void onEventUnSubscribed(String t) {}
            @Override public void onEvent(WldtEvent<?> e) { capture[0] = e; }
        };
    }

    // ── Experiment data model ──────────────────────────────────────────────────

    /**
     * Event body carrying the send timestamp, publisher id, and a fixed-size opaque payload.
     * sendNanos is captured immediately before publishEvent() to measure true E2E delay.
     * publisherId enables per-source out-of-order detection at each subscriber.
     */
    public static final class TimestampedPayload {
        public final long   sendNanos;
        public final String publisherId;
        public final byte[] data;

        public TimestampedPayload(long sendNanos, String publisherId, byte[] data) {
            this.sendNanos   = sendNanos;
            this.publisherId = publisherId;
            this.data        = data;
        }
    }

    /**
     * Parameters for one experiment run (one strategy × one parameter point).
     *
     * subscriberThreadPoolSize controls the subscriber execution model:
     *   1  → Sequential: onEvent() processes inline, blocking the strategy's dispatch thread.
     *   >1 → Parallel:   onEvent() submits work to a per-subscriber fixed thread pool and
     *                    returns immediately, decoupling processing from dispatch.
     */
    public static final class ExperimentConfig {
        public final String strategyName;
        public final int    numPublishers;
        public final int    numSubscribers;
        public final int    numTopics;                 // distinct event topics; publishers round-robin
        public final int    msgRatePerPublisher;       // target msg/sec per publisher (0 = unlimited)
        public final int    eventSizeBytes;
        public final long   subscriberProcessingMs;    // simulated subscriber work per event
        public final int    messagesPerPublisher;      // total messages each publisher thread sends
        public final int    subscriberThreadPoolSize;  // 1 = sequential, >1 = parallel pool per subscriber
        public final String paramName;                 // CSV label for the varied parameter
        public final String paramValue;                // CSV value for the varied parameter

        /** Backward-compatible: numTopics=1, subscriberThreadPoolSize=1 (sequential). */
        public ExperimentConfig(String strategyName,
                                int numPublishers, int numSubscribers,
                                int msgRatePerPublisher, int eventSizeBytes,
                                long subscriberProcessingMs, int messagesPerPublisher,
                                String paramName, String paramValue) {
            this(strategyName, numPublishers, numSubscribers, 1,
                 msgRatePerPublisher, eventSizeBytes, subscriberProcessingMs,
                 messagesPerPublisher, 1, paramName, paramValue);
        }

        /** Backward-compatible: subscriberThreadPoolSize=1 (sequential). */
        public ExperimentConfig(String strategyName,
                                int numPublishers, int numSubscribers, int numTopics,
                                int msgRatePerPublisher, int eventSizeBytes,
                                long subscriberProcessingMs, int messagesPerPublisher,
                                String paramName, String paramValue) {
            this(strategyName, numPublishers, numSubscribers, numTopics,
                 msgRatePerPublisher, eventSizeBytes, subscriberProcessingMs,
                 messagesPerPublisher, 1, paramName, paramValue);
        }

        /** Full constructor. */
        public ExperimentConfig(String strategyName,
                                int numPublishers, int numSubscribers, int numTopics,
                                int msgRatePerPublisher, int eventSizeBytes,
                                long subscriberProcessingMs, int messagesPerPublisher,
                                int subscriberThreadPoolSize,
                                String paramName, String paramValue) {
            this.strategyName            = strategyName;
            this.numPublishers           = numPublishers;
            this.numSubscribers          = numSubscribers;
            this.numTopics               = numTopics;
            this.msgRatePerPublisher     = msgRatePerPublisher;
            this.eventSizeBytes          = eventSizeBytes;
            this.subscriberProcessingMs  = subscriberProcessingMs;
            this.messagesPerPublisher    = messagesPerPublisher;
            this.subscriberThreadPoolSize = subscriberThreadPoolSize;
            this.paramName               = paramName;
            this.paramValue              = paramValue;
        }

        public int totalMessages() { return numPublishers * messagesPerPublisher; }
    }

    /**
     * Metrics collected from one experiment run.
     */
    public static final class ExperimentResult {
        public final ExperimentConfig config;
        public final int     msgsPublished;
        public final int     msgsDelivered;   // across ALL subscribers
        public final long    totalWallMs;
        public final double  e2eP50Ms;
        public final double  e2eP95Ms;
        public final double  e2eP99Ms;
        public final double  msgRatePerSec;
        public final double  throughputMbps;
        public final boolean timedOut;
        /** Average out-of-order delivery % across all subscribers (per-publisher source tracking). */
        public final double  outOfOrderPct;

        ExperimentResult(ExperimentConfig config,
                         int msgsPublished, int msgsDelivered, long totalWallMs,
                         long e2eP50Ns, long e2eP95Ns, long e2eP99Ns,
                         double msgRatePerSec, double throughputMbps, boolean timedOut,
                         double outOfOrderPct) {
            this.config         = config;
            this.msgsPublished  = msgsPublished;
            this.msgsDelivered  = msgsDelivered;
            this.totalWallMs    = totalWallMs;
            this.e2eP50Ms       = e2eP50Ns / 1_000_000.0;
            this.e2eP95Ms       = e2eP95Ns / 1_000_000.0;
            this.e2eP99Ms       = e2eP99Ns / 1_000_000.0;
            this.msgRatePerSec  = msgRatePerSec;
            this.throughputMbps = throughputMbps;
            this.timedOut       = timedOut;
            this.outOfOrderPct  = outOfOrderPct;
        }
    }

    public static final String EXPERIMENT_CSV_HEADER =
            "param_name,param_value,strategy,num_publishers,num_subscribers,num_topics," +
            "msg_rate_per_pub,event_size_bytes,processing_time_ms,subscriber_pool_size," +
            "msgs_published,msgs_delivered," +
            "e2e_p50_ms,e2e_p95_ms,e2e_p99_ms," +
            "msg_rate_per_sec,throughput_mbps,total_wall_ms,out_of_order_pct";

    public static void writeExperimentCsv(Path file, List<ExperimentResult> results) throws IOException {
        Files.createDirectories(file.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println(EXPERIMENT_CSV_HEADER);
            for (ExperimentResult r : results) {
                w.printf(Locale.US, "%s,%s,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.2f,%.6f,%d,%.2f%n",
                        r.config.paramName, r.config.paramValue, r.config.strategyName,
                        r.config.numPublishers, r.config.numSubscribers, r.config.numTopics,
                        r.config.msgRatePerPublisher, r.config.eventSizeBytes,
                        r.config.subscriberProcessingMs, r.config.subscriberThreadPoolSize,
                        r.msgsPublished, r.msgsDelivered,
                        r.e2eP50Ms, r.e2eP95Ms, r.e2eP99Ms,
                        r.msgRatePerSec, r.throughputMbps, r.totalWallMs,
                        r.outOfOrderPct);
            }
        }
    }

    // ── Experiment runner ──────────────────────────────────────────────────────

    /**
     * Executes one experiment: resets bus, injects strategy, subscribes listeners,
     * runs publisher threads with rate pacing, and collects E2E latency, throughput,
     * and out-of-order delivery metrics.
     *
     * Sub-0 records arrival timestamps for E2E delay measurement (captured at the start
     * of onEvent(), before any processing, regardless of subscriber execution model).
     * All subscribers track per-publisher out-of-order delivery (sendNanos regression).
     * outOfOrderPct is the average across all subscribers.
     * deliveryLatch ensures all subscriber processing completes before metrics are computed.
     *
     * Subscriber execution model (set via ExperimentConfig.subscriberThreadPoolSize):
     *   Sequential (pool=1): onEvent() processes inline — strategy dispatch thread blocks.
     *   Parallel   (pool>1): onEvent() submits to per-subscriber pool — dispatch thread
     *                        returns immediately and picks up the next event.
     */
    @SuppressWarnings("unchecked")
    public static ExperimentResult runExperiment(ExperimentConfig cfg) throws Exception {
        resetEventBus();
        WldtEventBus bus = WldtEventBus.getInstance();
        bus.setStrategy(createStrategy(cfg.strategyName));

        final String dtId  = "exp-dt";
        final int    total = cfg.totalMessages();

        // Build topic names and a single filter covering all of them.
        // Publishers round-robin across topics at the same overall rate.
        final String[] topicNames = new String[cfg.numTopics];
        for (int t = 0; t < cfg.numTopics; t++)
            topicNames[t] = "exp-event-" + t;

        WldtEventFilter filter = new WldtEventFilter();
        for (String topic : topicNames)
            filter.add(topic);

        CountDownLatch deliveryLatch = new CountDownLatch(total * cfg.numSubscribers);
        AtomicInteger  arrivalIdx   = new AtomicInteger(0);
        long[]         e2eNanos     = new long[total];

        // Per-subscriber out-of-order tracking.
        // lastNanosByPub[s] is accessed only from the strategy's dispatch thread for subscriber s
        // (DefaultStrategy: publisher thread under lock; Async/Queued: single consumer thread;
        //  PerTopicPerSub: dedicated subscriber-processor thread) — HashMap is safe.
        final Map<String, Long>[] lastNanosByPub = new HashMap[cfg.numSubscribers];
        final AtomicInteger[]     subDelivered   = new AtomicInteger[cfg.numSubscribers];
        final AtomicInteger[]     subOutOfOrder  = new AtomicInteger[cfg.numSubscribers];
        for (int s = 0; s < cfg.numSubscribers; s++) {
            lastNanosByPub[s] = new HashMap<>();
            subDelivered[s]   = new AtomicInteger(0);
            subOutOfOrder[s]  = new AtomicInteger(0);
        }

        // Create per-subscriber parallel processing pools (null = sequential).
        // onEvent() metadata tracking always runs inline; only busySleepMs is offloaded.
        final ExecutorService[] subPools = new ExecutorService[cfg.numSubscribers];
        if (cfg.subscriberThreadPoolSize > 1) {
            for (int s = 0; s < cfg.numSubscribers; s++) {
                final int si = s;
                subPools[si] = Executors.newFixedThreadPool(
                        cfg.subscriberThreadPoolSize,
                        r -> { Thread t = new Thread(r, "sub-pool-" + si); t.setDaemon(true); return t; });
            }
        }

        // Sub-0: captures E2E delay + out-of-order, then processes (sequential or parallel)
        bus.subscribe(dtId, "sub-0", filter, new WldtEventListener() {
            @Override public void onEventSubscribed(String t) {}
            @Override public void onEventUnSubscribed(String t) {}
            @Override public void onEvent(WldtEvent<?> e) {
                long arrivalNs = System.nanoTime();
                TimestampedPayload p = (TimestampedPayload) e.getBody();
                int i = arrivalIdx.getAndIncrement();
                if (i < e2eNanos.length)
                    e2eNanos[i] = arrivalNs - p.sendNanos;
                Long last = lastNanosByPub[0].get(p.publisherId);
                if (last != null && p.sendNanos < last)
                    subOutOfOrder[0].incrementAndGet();
                lastNanosByPub[0].put(p.publisherId, p.sendNanos);
                subDelivered[0].incrementAndGet();
                if (subPools[0] != null) {
                    subPools[0].submit(() -> { busySleepMs(cfg.subscriberProcessingMs); deliveryLatch.countDown(); });
                } else {
                    busySleepMs(cfg.subscriberProcessingMs);
                    deliveryLatch.countDown();
                }
            }
        });

        // Sub-1..N: track out-of-order + process (sequential or parallel)
        for (int s = 1; s < cfg.numSubscribers; s++) {
            final int si = s;
            bus.subscribe(dtId, "sub-" + s, filter, new WldtEventListener() {
                @Override public void onEventSubscribed(String t) {}
                @Override public void onEventUnSubscribed(String t) {}
                @Override public void onEvent(WldtEvent<?> e) {
                    TimestampedPayload p = (TimestampedPayload) e.getBody();
                    Long last = lastNanosByPub[si].get(p.publisherId);
                    if (last != null && p.sendNanos < last)
                        subOutOfOrder[si].incrementAndGet();
                    lastNanosByPub[si].put(p.publisherId, p.sendNanos);
                    subDelivered[si].incrementAndGet();
                    if (subPools[si] != null) {
                        subPools[si].submit(() -> { busySleepMs(cfg.subscriberProcessingMs); deliveryLatch.countDown(); });
                    } else {
                        busySleepMs(cfg.subscriberProcessingMs);
                        deliveryLatch.countDown();
                    }
                }
            });
        }

        byte[]         sharedData = new byte[cfg.eventSizeBytes];
        AtomicInteger  published  = new AtomicInteger(0);
        long intervalNs = cfg.msgRatePerPublisher > 0
                ? 1_000_000_000L / cfg.msgRatePerPublisher : 0L;

        // One queue per publisher: producer enqueues at target rate, publisher drains into bus.
        // sendNanos is stamped at creation so E2E includes queueing wait (visible for Default strategy).
        LinkedBlockingQueue<WldtEvent<?>>[] sendQueues = new LinkedBlockingQueue[cfg.numPublishers];
        for (int p = 0; p < cfg.numPublishers; p++)
            sendQueues[p] = new LinkedBlockingQueue<>();

        CountDownLatch producerDone  = new CountDownLatch(cfg.numPublishers);
        CountDownLatch pubDone       = new CountDownLatch(cfg.numPublishers);
        ExecutorService producerPool = Executors.newFixedThreadPool(cfg.numPublishers);
        ExecutorService pubPool      = Executors.newFixedThreadPool(cfg.numPublishers);
        long expStart = System.nanoTime();

        for (int p = 0; p < cfg.numPublishers; p++) {
            final int pubIdx = p;
            producerPool.submit(() -> {
                try {
                    final String pubId = "pub-" + pubIdx;
                    long nextDeadline = System.nanoTime() + intervalNs;
                    for (int i = 0; i < cfg.messagesPerPublisher; i++) {
                        String topic = topicNames[i % cfg.numTopics];
                        TimestampedPayload payload =
                                new TimestampedPayload(System.nanoTime(), pubId, sharedData);
                        try {
                            sendQueues[pubIdx].offer(new WldtEvent<>(topic, payload));
                        } catch (Exception ignored) {}
                        if (intervalNs > 0) {
                            awaitDeadline(nextDeadline);
                            nextDeadline += intervalNs;
                        }
                    }
                } finally {
                    producerDone.countDown();
                }
            });
        }

        for (int p = 0; p < cfg.numPublishers; p++) {
            final int pubIdx = p;
            pubPool.submit(() -> {
                try {
                    final String pubId = "pub-" + pubIdx;
                    int sent = 0;
                    while (sent < cfg.messagesPerPublisher) {
                        try {
                            WldtEvent<?> event = sendQueues[pubIdx].poll(200, TimeUnit.MILLISECONDS);
                            if (event == null) continue;
                            bus.publishEvent(dtId, pubId, event);
                            published.incrementAndGet();
                            sent++;
                        } catch (EventBusException ex) {
                            sent++;
                        }
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    pubDone.countDown();
                }
            });
        }

        producerDone.await();
        pubDone.await();
        boolean timedOut = !deliveryLatch.await(120, TimeUnit.SECONDS);
        long totalWallMs = (System.nanoTime() - expStart) / 1_000_000L;

        producerPool.shutdownNow();
        pubPool.shutdownNow();
        for (ExecutorService pool : subPools)
            if (pool != null) pool.shutdownNow();

        int msgsDelivered = (total * cfg.numSubscribers) - (int) deliveryLatch.getCount();
        int measured      = Math.min(arrivalIdx.get(), total);

        long[] sorted = Arrays.copyOf(e2eNanos, measured);
        Arrays.sort(sorted);
        long p50 = sorted.length > 0 ? sorted[(int) (sorted.length * 0.50)] : 0L;
        long p95 = sorted.length > 0 ? sorted[Math.min((int) (sorted.length * 0.95), sorted.length - 1)] : 0L;
        long p99 = sorted.length > 0 ? sorted[Math.min((int) (sorted.length * 0.99), sorted.length - 1)] : 0L;

        double wallSec        = totalWallMs / 1000.0;
        double msgRatePerSec  = wallSec > 0 ? msgsDelivered / wallSec : 0.0;
        double throughputMbps = wallSec > 0
                ? (double) published.get() * cfg.eventSizeBytes * 8.0 / 1_000_000.0 / wallSec
                : 0.0;

        // Average out-of-order % across all subscribers
        double totalOoPct   = 0.0;
        int    subsWithData = 0;
        for (int s = 0; s < cfg.numSubscribers; s++) {
            int delivered = subDelivered[s].get();
            if (delivered > 0) {
                totalOoPct += (double) subOutOfOrder[s].get() / delivered * 100.0;
                subsWithData++;
            }
        }
        double avgOutOfOrderPct = subsWithData > 0 ? totalOoPct / subsWithData : 0.0;

        return new ExperimentResult(cfg, published.get(), msgsDelivered, totalWallMs,
                p50, p95, p99, msgRatePerSec, throughputMbps, timedOut, avgOutOfOrderPct);
    }

    // ── Pacing utilities ───────────────────────────────────────────────────────

    /** Sub-millisecond deadline wait: park for most of the interval, spin for the last ms. */
    private static void awaitDeadline(long deadlineNs) {
        long remaining = deadlineNs - System.nanoTime();
        if (remaining > 2_000_000L)
            LockSupport.parkNanos(remaining - 1_000_000L);
        while (System.nanoTime() < deadlineNs)
            Thread.onSpinWait();
    }

    private static void busySleepMs(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Scenario 10 data model ─────────────────────────────────────────────────

    /**
     * Configuration for the time-windowed, mixed-subscriber experiment (Scenario 10).
     *
     * Publishers are assigned independent rates (Hz). Sequential subscribers block the
     * strategy's dispatch thread inside onEvent(); parallel subscribers submit work to
     * a fixed thread pool and return immediately.
     *
     * The experiment runs for durationMs wall-clock time (not message-count driven).
     * Metrics are aggregated in windowSec-second tumbling windows.
     */
    public static final class Scenario10Config {
        public final String  strategyName;
        public final int[]   publisherRatesHz;       // one rate (Hz) per publisher
        public final int     numSequentialSubs;
        public final int     numParallelSubs;
        public final int     parallelSubPoolSize;    // thread pool size for each parallel subscriber
        public final int     eventSizeBytes;
        public final int     processingMinMs;        // inclusive lower bound for per-event processing
        public final int     processingMaxMs;        // inclusive upper bound for per-event processing
        public final long    durationMs;             // total experiment wall time
        public final int     windowSec;              // tumbling aggregation window size

        public Scenario10Config(String strategyName, int[] publisherRatesHz,
                                int numSequentialSubs, int numParallelSubs, int parallelSubPoolSize,
                                int eventSizeBytes, int processingMinMs, int processingMaxMs,
                                long durationMs, int windowSec) {
            this.strategyName        = strategyName;
            this.publisherRatesHz    = publisherRatesHz;
            this.numSequentialSubs   = numSequentialSubs;
            this.numParallelSubs     = numParallelSubs;
            this.parallelSubPoolSize = parallelSubPoolSize;
            this.eventSizeBytes      = eventSizeBytes;
            this.processingMinMs     = processingMinMs;
            this.processingMaxMs     = processingMaxMs;
            this.durationMs          = durationMs;
            this.windowSec           = windowSec;
        }

        public int numPublishers()   { return publisherRatesHz.length; }
        public int numSubscribers()  { return numSequentialSubs + numParallelSubs; }
        public int totalRateHz()     { int s = 0; for (int r : publisherRatesHz) s += r; return s; }
    }

    /** Per-event record captured at onEvent() entry — used for time-window aggregation. */
    private static final class EventRecord {
        final long    sendNanos;
        final long    arrivalNanos;
        final boolean outOfOrder;

        EventRecord(long sendNanos, long arrivalNanos, boolean outOfOrder) {
            this.sendNanos    = sendNanos;
            this.arrivalNanos = arrivalNanos;
            this.outOfOrder   = outOfOrder;
        }
    }

    /** Aggregated metrics for one tumbling window, one subscriber, one strategy. */
    public static final class WindowMetrics {
        public final int    windowIndex;
        public final long   windowStartSec;
        public final long   windowEndSec;
        public final String strategy;
        public final String subscriberId;
        public final String subscriberType;  // "sequential" | "parallel"
        public final int    msgsDelivered;
        public final double e2eP50Ms;
        public final double e2eP95Ms;
        public final double e2eP99Ms;
        public final double msgRatePerSec;
        public final double throughputMbps;
        public final double outOfOrderPct;

        public WindowMetrics(int windowIndex, long windowStartSec, long windowEndSec,
                             String strategy, String subscriberId, String subscriberType,
                             int msgsDelivered, double e2eP50Ms, double e2eP95Ms, double e2eP99Ms,
                             double msgRatePerSec, double throughputMbps, double outOfOrderPct) {
            this.windowIndex    = windowIndex;
            this.windowStartSec = windowStartSec;
            this.windowEndSec   = windowEndSec;
            this.strategy       = strategy;
            this.subscriberId   = subscriberId;
            this.subscriberType = subscriberType;
            this.msgsDelivered  = msgsDelivered;
            this.e2eP50Ms       = e2eP50Ms;
            this.e2eP95Ms       = e2eP95Ms;
            this.e2eP99Ms       = e2eP99Ms;
            this.msgRatePerSec  = msgRatePerSec;
            this.throughputMbps = throughputMbps;
            this.outOfOrderPct  = outOfOrderPct;
        }
    }

    public static final String SCENARIO10_CSV_HEADER =
            "window_start_sec,window_end_sec,strategy,subscriber_id,subscriber_type," +
            "msgs_delivered,e2e_p50_ms,e2e_p95_ms,e2e_p99_ms," +
            "msg_rate_per_sec,throughput_mbps,out_of_order_pct";

    public static void writeScenario10Csv(Path file, List<WindowMetrics> metrics) throws IOException {
        Files.createDirectories(file.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println(SCENARIO10_CSV_HEADER);
            for (WindowMetrics m : metrics) {
                w.printf(Locale.US, "%d,%d,%s,%s,%s,%d,%.3f,%.3f,%.3f,%.2f,%.6f,%.2f%n",
                        m.windowStartSec, m.windowEndSec, m.strategy,
                        m.subscriberId, m.subscriberType, m.msgsDelivered,
                        m.e2eP50Ms, m.e2eP95Ms, m.e2eP99Ms,
                        m.msgRatePerSec, m.throughputMbps, m.outOfOrderPct);
            }
        }
    }

    // ── Metadata writers ──────────────────────────────────────────────────────

    /** Writes <csv-stem>_meta.json alongside the CSV with the actual fixed parameters used. */
    public static void writeExperimentMeta(Path csvFile, List<ExperimentResult> results) throws IOException {
        if (results.isEmpty()) return;
        ExperimentConfig first = results.get(0).config;
        Path metaFile = csvFile.resolveSibling(
                csvFile.getFileName().toString().replace(".csv", "_meta.json"));
        Files.createDirectories(metaFile.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(metaFile))) {
            w.println("{");
            w.printf("  \"varied_param\": \"%s\",%n",     first.paramName);
            w.printf("  \"num_publishers\": %d,%n",        first.numPublishers);
            w.printf("  \"num_subscribers\": %d,%n",       first.numSubscribers);
            w.printf("  \"num_topics\": %d,%n",            first.numTopics);
            w.printf("  \"msg_rate_per_pub\": %d,%n",      first.msgRatePerPublisher);
            w.printf("  \"event_size_bytes\": %d,%n",      first.eventSizeBytes);
            w.printf("  \"processing_time_ms\": %d,%n",    first.subscriberProcessingMs);
            w.printf("  \"subscriber_pool_size\": %d,%n",  first.subscriberThreadPoolSize);
            w.printf("  \"messages_per_publisher\": %d%n", first.messagesPerPublisher);
            w.println("}");
        }
    }

    /** Writes <csv-stem>_meta.json for Scenario 10 alongside the CSV. */
    public static void writeScenario10Meta(Path csvFile, Scenario10Config cfg) throws IOException {
        Path metaFile = csvFile.resolveSibling(
                csvFile.getFileName().toString().replace(".csv", "_meta.json"));
        Files.createDirectories(metaFile.getParent());
        StringBuilder rates = new StringBuilder("[");
        for (int i = 0; i < cfg.publisherRatesHz.length; i++) {
            if (i > 0) rates.append(", ");
            rates.append(cfg.publisherRatesHz[i]);
        }
        rates.append("]");
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(metaFile))) {
            w.println("{");
            w.printf("  \"publisher_rates_hz\": %s,%n",     rates);
            w.printf("  \"num_sequential_subs\": %d,%n",    cfg.numSequentialSubs);
            w.printf("  \"num_parallel_subs\": %d,%n",      cfg.numParallelSubs);
            w.printf("  \"parallel_sub_pool_size\": %d,%n", cfg.parallelSubPoolSize);
            w.printf("  \"event_size_bytes\": %d,%n",       cfg.eventSizeBytes);
            w.printf("  \"processing_min_ms\": %d,%n",      cfg.processingMinMs);
            w.printf("  \"processing_max_ms\": %d,%n",      cfg.processingMaxMs);
            w.printf("  \"duration_ms\": %d,%n",            cfg.durationMs);
            w.printf("  \"window_sec\": %d%n",              cfg.windowSec);
            w.println("}");
        }
    }

    // ── Scenario 10 runner ─────────────────────────────────────────────────────

    /**
     * Runs one strategy configuration of Scenario 10.
     *
     * Publishers are started with independent rates and run freely for durationMs.
     * Each event's processing time is drawn independently from Uniform[processingMinMs, processingMaxMs].
     * Sequential subscribers block the dispatch thread inside onEvent(); parallel subscribers
     * submit work to their pool and return immediately.
     *
     * E2E latency is captured at the START of onEvent() (bus delivery time, before processing).
     * WindowMetrics are assembled from EventRecords after the experiment, grouped into
     * windowSec-second tumbling windows aligned to the experiment start.
     *
     * Events arriving after durationMs (from strategy queues draining post-publisher-stop)
     * are excluded from analysis.
     */
    @SuppressWarnings("unchecked")
    public static List<WindowMetrics> runScenario10Experiment(Scenario10Config cfg) throws Exception {
        resetEventBus();
        WldtEventBus bus = WldtEventBus.getInstance();
        bus.setStrategy(createStrategy(cfg.strategyName));

        final String        dtId   = "exp-dt";
        final String        topic  = "exp-event-0";
        final WldtEventFilter filter = new WldtEventFilter();
        filter.add(topic);

        final int numSubs = cfg.numSubscribers();

        // Per-subscriber record queues — ConcurrentLinkedQueue is append-only from one thread
        // (onEvent is always dispatched from a single thread per subscriber in all strategies).
        final ConcurrentLinkedQueue<EventRecord>[] records    = new ConcurrentLinkedQueue[numSubs];
        final Map<String, Long>[]                  lastNanos  = new HashMap[numSubs];
        for (int s = 0; s < numSubs; s++) {
            records[s]   = new ConcurrentLinkedQueue<>();
            lastNanos[s] = new HashMap<>();
        }

        // Per-subscriber parallel processing pools (null = sequential)
        final ExecutorService[] subPools = new ExecutorService[numSubs];
        for (int s = cfg.numSequentialSubs; s < numSubs; s++) {
            final int si = s;
            subPools[si] = Executors.newFixedThreadPool(
                    cfg.parallelSubPoolSize,
                    r -> { Thread t = new Thread(r, "sub-pool-" + si); t.setDaemon(true); return t; });
        }

        // Register subscribers
        for (int s = 0; s < numSubs; s++) {
            final int si = s;
            bus.subscribe(dtId, "sub-" + s, filter, new WldtEventListener() {
                @Override public void onEventSubscribed(String t)   {}
                @Override public void onEventUnSubscribed(String t) {}
                @Override public void onEvent(WldtEvent<?> e) {
                    long arrivalNs = System.nanoTime();
                    TimestampedPayload p = (TimestampedPayload) e.getBody();
                    Long last = lastNanos[si].get(p.publisherId);
                    boolean oo = last != null && p.sendNanos < last;
                    lastNanos[si].put(p.publisherId, p.sendNanos);
                    records[si].add(new EventRecord(p.sendNanos, arrivalNs, oo));
                    int procMs = ThreadLocalRandom.current().nextInt(
                            cfg.processingMinMs, cfg.processingMaxMs + 1);
                    if (subPools[si] != null) {
                        subPools[si].submit(() -> busySleepMs(procMs));
                    } else {
                        busySleepMs(procMs);
                    }
                }
            });
        }

        // Start publishers — each with its own independent rate
        // Producer enqueues at target rate; publisher drains into bus.
        // sendNanos stamped at creation so E2E includes queueing wait for Default strategy.
        final byte[]        sharedData = new byte[cfg.eventSizeBytes];
        final AtomicBoolean running    = new AtomicBoolean(true);
        final long expStartNanos       = System.nanoTime();

        LinkedBlockingQueue<WldtEvent<?>>[] sendQueues10 = new LinkedBlockingQueue[cfg.numPublishers()];
        for (int p = 0; p < cfg.numPublishers(); p++)
            sendQueues10[p] = new LinkedBlockingQueue<>();

        final ExecutorService producerPool10 = Executors.newFixedThreadPool(cfg.numPublishers());
        final ExecutorService pubPool        = Executors.newFixedThreadPool(cfg.numPublishers());

        for (int p = 0; p < cfg.numPublishers(); p++) {
            final int  pubIdx     = p;
            final long intervalNs = 1_000_000_000L / cfg.publisherRatesHz[pubIdx];
            producerPool10.submit(() -> {
                String pubId        = "pub-" + pubIdx;
                long   nextDeadline = System.nanoTime() + intervalNs;
                while (running.get()) {
                    try {
                        TimestampedPayload payload =
                                new TimestampedPayload(System.nanoTime(), pubId, sharedData);
                        sendQueues10[pubIdx].offer(new WldtEvent<>(topic, payload));
                    } catch (Exception ignored) {}
                    if (!running.get()) break;
                    awaitDeadline(nextDeadline);
                    nextDeadline += intervalNs;
                }
            });
        }

        for (int p = 0; p < cfg.numPublishers(); p++) {
            final int pubIdx = p;
            pubPool.submit(() -> {
                String pubId = "pub-" + pubIdx;
                while (running.get() || !sendQueues10[pubIdx].isEmpty()) {
                    try {
                        WldtEvent<?> event = sendQueues10[pubIdx].poll(10, TimeUnit.MILLISECONDS);
                        if (event == null) continue;
                        bus.publishEvent(dtId, pubId, event);
                    } catch (Exception ignored) {}
                }
            });
        }

        // Run experiment for durationMs, then stop
        Thread.sleep(cfg.durationMs);
        running.set(false);
        producerPool10.shutdownNow();
        producerPool10.awaitTermination(2, TimeUnit.SECONDS);
        pubPool.shutdownNow();
        pubPool.awaitTermination(5, TimeUnit.SECONDS);

        // Brief drain — let events already queued in strategy queues reach onEvent()
        Thread.sleep(2_000);
        final long expEndNanos = expStartNanos + cfg.durationMs * 1_000_000L;

        // Shutdown subscriber pools
        for (ExecutorService pool : subPools)
            if (pool != null) pool.shutdownNow();

        // ── Time-window aggregation ────────────────────────────────────────────
        final int maxWindows = (int)(cfg.durationMs / (cfg.windowSec * 1000L));
        List<WindowMetrics> result = new ArrayList<>();

        for (int s = 0; s < numSubs; s++) {
            String subType = s < cfg.numSequentialSubs ? "sequential" : "parallel";

            // Bucket records into tumbling windows
            TreeMap<Integer, List<EventRecord>> byWindow = new TreeMap<>();
            for (EventRecord rec : records[s]) {
                if (rec.arrivalNanos > expEndNanos) continue;
                long elapsedMs = (rec.arrivalNanos - expStartNanos) / 1_000_000L;
                if (elapsedMs < 0) continue;
                int w = (int)(elapsedMs / (cfg.windowSec * 1000L));
                if (w >= maxWindows) w = maxWindows - 1;
                byWindow.computeIfAbsent(w, k -> new ArrayList<>()).add(rec);
            }

            for (Map.Entry<Integer, List<EventRecord>> entry : byWindow.entrySet()) {
                int w     = entry.getKey();
                List<EventRecord> wRecs = entry.getValue();

                long[] lats = new long[wRecs.size()];
                int    ooCount = 0;
                for (int i = 0; i < wRecs.size(); i++) {
                    EventRecord r = wRecs.get(i);
                    lats[i] = r.arrivalNanos - r.sendNanos;
                    if (r.outOfOrder) ooCount++;
                }
                Arrays.sort(lats);

                long p50Ns = lats.length > 0 ? lats[(int)(lats.length * 0.50)] : 0L;
                long p95Ns = lats.length > 0 ? lats[Math.min((int)(lats.length * 0.95), lats.length - 1)] : 0L;
                long p99Ns = lats.length > 0 ? lats[Math.min((int)(lats.length * 0.99), lats.length - 1)] : 0L;

                double rate       = (double) wRecs.size() / cfg.windowSec;
                double throughput = rate * cfg.eventSizeBytes * 8.0 / 1_000_000.0;
                double ooPct      = wRecs.isEmpty() ? 0.0 : (double) ooCount / wRecs.size() * 100.0;

                result.add(new WindowMetrics(
                        w, (long) w * cfg.windowSec, (long)(w + 1) * cfg.windowSec,
                        cfg.strategyName, "sub-" + s, subType,
                        wRecs.size(),
                        p50Ns / 1_000_000.0, p95Ns / 1_000_000.0, p99Ns / 1_000_000.0,
                        rate, throughput, ooPct));
            }
        }

        return result;
    }
}

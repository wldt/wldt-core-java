package it.wldt.core.event;

import it.wldt.core.event.utils.EventBusTestUtils;
import it.wldt.core.event.utils.EventBusTestUtils.ExperimentConfig;
import it.wldt.core.event.utils.EventBusTestUtils.ExperimentResult;
import it.wldt.core.event.utils.EventBusTestUtils.Scenario10Config;
import it.wldt.core.event.utils.EventBusTestUtils.WindowMetrics;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Experiment-based EventBus characterization suite.
 *
 * Each scenario varies one parameter while holding others fixed and runs all three strategies
 * in sequence. Results are written to a single CSV per scenario for analysis and graphing.
 *
 * Fixed baseline values (unless the scenario varies them):
 *   publisher message rate : 20 msg/s
 *   event size             : 100 bytes
 *   subscriber processing  : 10 ms
 *   publishers             : 1
 *   subscribers            : 1
 *
 * Run all experiments:
 *   ./gradlew test --tests "it.wldt.core.event.EventBusExperimentTest"
 *
 * CSVs appear in:
 *   metrics/event-bus/<RUN_ID>/experiments/scenario_N_*.csv
 *
 * Run the analyzer after tests to generate comparison graphs:
 *   python metrics/event-bus/analyze.py
 */
@Tag("experiment")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventBusExperimentTest {

    // Baseline parameters (held fixed when not the varied dimension)
    private static final int  FIXED_RATE    = 10;   // msg/sec per publisher
    private static final int  FIXED_SIZE    = 100000;  // event payload bytes
    private static final long FIXED_PROC_MS = 1L;  // subscriber processing time

    /**
     * Message count per publisher: enough samples for stable percentiles without
     * making very-low-rate runs prohibitively slow.
     */
    private static int msgCount(int rate) {
        if (rate <= 2)  return 10;
        if (rate <= 10) return 30;
        return Math.min(rate * 3, 300);
    }

    // ── Scenario 1: Message rate variation ────────────────────────────────────

    /**
     * Varies publisher message rate: [1, 10, 20, 50, 100] msg/s per publisher.
     * Shows how each strategy handles different traffic intensities.
     * Expected: Default blocks on slow subscribers; Async/Queued decouple publisher speed.
     */
    @Test
    @Order(1)
    void scenario1_messageRateVariation() throws Exception {
        int[] rates = {1, 10, 20, 50, 100, 200, 400};
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int rate : rates) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 8, rate, FIXED_SIZE, FIXED_PROC_MS, msgCount(rate),
                        "msg_rate_per_pub", String.valueOf(rate));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S1", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_1_rate_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 1 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 2: Event size variation ──────────────────────────────────────

    /**
     * Varies event payload size: [10, 100, 1000, 5000] bytes.
     * Shows throughput sensitivity to object size (allocation + GC pressure).
     * Note: no serialization occurs — size represents nominal message weight.
     */
    @Test
    @Order(2)
    void scenario2_eventSizeVariation() throws Exception {
        int[] sizes = {10, 100, 1000, 5000};
        int msgs = msgCount(FIXED_RATE);
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int size : sizes) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 1, 1, FIXED_RATE, size, FIXED_PROC_MS, msgs,
                        "event_size_bytes", String.valueOf(size));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S2", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_2_size_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 2 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 3: Subscriber processing time variation ──────────────────────

    /**
     * Varies subscriber processing time: [1, 10, 50, 100, 200] ms.
     * Reveals the key architectural difference between strategies:
     *   Default (sync): publisher blocks for the full processing time per event.
     *   Async/Queued:   publisher never blocks; queue absorbs the backlog.
     * At processingMs=200 with rate=20 msg/s, Default strategy drops to ~5 effective msg/s.
     */
    @Test
    @Order(3)
    void scenario3_subscriberProcessingTimeVariation() throws Exception {
        long[] procTimes = {10L, 20L, 30L, 40L, 50L, 60L, 70L, 80L, 100L};
        int msgs = msgCount(FIXED_RATE);
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (long proc : procTimes) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 4, FIXED_RATE, FIXED_SIZE, proc, msgs,
                        "processing_time_ms", String.valueOf(proc));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S3", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_3_processing_time_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 3 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 4: Publisher count variation ─────────────────────────────────

    /**
     * Varies number of concurrent publishers: [1, 2, 4, 6, 8, 10].
     * All publishers target the same DT ID, so events are dispatched to one subscriber.
     * Shows lock contention scaling for Default vs. per-DT queue fan-in for Async/Queued.
     */
    @Test
    @Order(4)
    void scenario4_publisherCountVariation() throws Exception {
        int[] pubCounts = {1, 2, 4, 6, 8, 10};
        int msgs = msgCount(FIXED_RATE);
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int pubs : pubCounts) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, pubs, 1, FIXED_RATE, FIXED_SIZE, FIXED_PROC_MS, msgs,
                        "num_publishers", String.valueOf(pubs));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S4", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_4_publisher_count_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 4 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 5: Subscriber count variation ────────────────────────────────

    /**
     * Varies number of subscribers: [1, 2, 4, 6, 8, 10] with 4 concurrent publishers.
     * Each subscriber simulates 10 ms of processing per event.
     * Shows fan-out cost: Default blocks the publisher for numSubs × processingMs per event;
     * Async/Queued dispatch to all subscribers concurrently from the consumer thread.
     */
    @Test
    @Order(5)
    void scenario5_subscriberCountVariation() throws Exception {
        int[] subCounts = {1, 2, 4, 6, 8, 10};
        int msgs = msgCount(FIXED_RATE);
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int subs : subCounts) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, subs, FIXED_RATE, FIXED_SIZE, FIXED_PROC_MS, msgs,
                        "num_subscribers", String.valueOf(subs));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S5", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_5_subscriber_count_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 5 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 6: Large payload × rate variation ────────────────────────────

    /**
     * Varies publisher message rate: [1, 5, 10, 20, 50, 100] msg/s with a fixed 500 KB payload.
     * Stresses throughput capacity: at 100 msg/s × 500 KB = 50 MB/s effective data rate.
     * No subscriber processing delay — shows raw delivery throughput under large payloads.
     */
    @Test
    @Order(6)
    void scenario6_largePayloadRateVariation() throws Exception {
        int[] rates    = {1, 5, 10, 20, 50, 100};
        int   size     = 500_000; // 500 KB
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int rate : rates) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 4, rate, size, FIXED_PROC_MS, msgCount(rate),
                        "msg_rate_per_pub", String.valueOf(rate));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S6", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_6_large_payload_rate_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 6 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 7: High rate × message size variation ────────────────────────

    /**
     * Fixes publisher rate at 100 msg/s and varies payload size: [100 KB, 200 KB, 500 KB, 750 KB, 1 MB].
     * Shows how each strategy degrades as per-message data volume grows at a sustained high rate.
     * No subscriber processing delay — isolates the payload-size impact on delivery latency.
     */
    @Test
    @Order(7)
    void scenario7_highRateSizeVariation() throws Exception {
        int[] sizes = {100_000, 200_000, 500_000, 750_000, 1_000_000};
        int   rate  = 100; // msg/s
        int   msgs  = msgCount(rate); // 300 messages → 3 seconds at 100 msg/s
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int size : sizes) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 4, rate, size, FIXED_PROC_MS, msgs,
                        "event_size_bytes", String.valueOf(size));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S7", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_7_high_rate_size_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 7 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 8: Topic count variation ────────────────────────────────────

    /**
     * Fixes rate=100 msg/s per publisher, 4 publishers, 4 subscribers, 200 KB payload, 10 ms processing.
     * Varies number of topics: [2, 4, 8, 10, 20, 40, 80, 100].
     * All publishers and subscribers operate on all topics; overall rate is divided equally.
     * Stresses per-topic queue/thread creation in PerTopicPerSubscriberStrategy vs. flat
     * per-DT routing in the other strategies.
     */
    @Test
    @Order(8)
    void scenario8_topicCountVariation() throws Exception {
        int[] topics = {2, 4, 8, 10, 20, 40, 80, 100};
        int   rate   = 10;  // msg/s per publisher (overall — divided across topics)
        int   size   = 200_000; // 200 KB
        int   msgs   = msgCount(rate); // 300 total messages per publisher across all topics
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int numTopics : topics) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 4, numTopics,
                        rate, size, FIXED_PROC_MS, msgs,
                        "num_topics", String.valueOf(numTopics));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S8", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_8_topic_count_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 8 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 9: Parallel subscriber thread pool size variation ───────────

    /**
     * Fixes rate=100 msg/s per publisher, 4 publishers, 4 subscribers, 500 KB payload,
     * and 10 ms of simulated processing per event. Varies the subscriber thread pool size:
     * [2, 4, 8, 10, 14, 16, 18, 20].
     *
     * Each subscriber uses a fixed thread pool (ParallelSubscriber mode): onEvent() submits
     * the processing work to the pool and returns immediately, decoupling the strategy's
     * dispatch thread from subscriber processing latency.
     *
     * With N pool threads per subscriber, up to N events can be processed concurrently
     * within a single subscriber. This shows how intra-subscriber parallelism scales
     * throughput when publish rate × processing time > 1 (i.e. the subscriber is a bottleneck).
     *
     * All four bus strategies are tested so that the interaction between the bus dispatch
     * model and the parallel subscriber pool can be observed.
     */
    @Test
    @Order(9)
    void scenario9_parallelSubscriberPoolSizeVariation() throws Exception {
        int[] poolSizes = {2, 4, 8, 10, 14, 16, 18, 20};
        int   rate      = 100;        // msg/s per publisher
        int   size      = 500_000;    // 500 KB
        int   msgs      = msgCount(rate);
        List<ExperimentResult> results = new ArrayList<>();

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            for (int poolSize : poolSizes) {
                ExperimentConfig cfg = new ExperimentConfig(
                        strat, 4, 4, 1,
                        rate, size, FIXED_PROC_MS, msgs,
                        poolSize,
                        "subscriber_pool_size", String.valueOf(poolSize));
                ExperimentResult r = EventBusTestUtils.runExperiment(cfg);
                results.add(r);
                printResult("S9", r);
            }
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_9_parallel_subscriber_pool_variation");
        EventBusTestUtils.writeExperimentCsv(csv, results);
        EventBusTestUtils.writeExperimentMeta(csv, results);
        System.out.println("Scenario 9 CSV: " + csv.toAbsolutePath());
    }

    // ── Scenario 10: Mixed publishers + mixed subscribers, time-windowed ─────

    /**
     * 4 publishers at [100, 50, 25, 10] Hz; 2 sequential + 2 parallel subscribers.
     * Payload: 500 KB. Subscriber processing: Uniform[10, 100] ms per event.
     * Duration: 5 minutes. Metrics aggregated in 10-second tumbling windows.
     *
     * Shows how each bus strategy handles:
     *   - Mixed arrival rates from heterogeneous publishers.
     *   - Sequential subscribers blocking the dispatch thread (back-pressure builds over time).
     *   - Parallel subscribers returning immediately (processing in background pool).
     * The time axis reveals how queue depth, latency, and message rate evolve under sustained load.
     */
    @Test
    @Order(10)
    void scenario10_mixedPublishersAndSubscribers() throws Exception {
        List<WindowMetrics> allResults = new ArrayList<>();
        Scenario10Config lastCfg = null;

        for (String strat : EventBusTestUtils.STRATEGY_NAMES) {
            Scenario10Config cfg = new Scenario10Config(
                    strat,
                    new int[]{100, 50, 25, 10},  // publisher rates Hz
                    2, 2, 16,                     // 2 sequential, 2 parallel, pool=16
                    500_000,                      // 500 KB payload
                    10, 100,                      // processing [10, 100] ms
                    300_000L, 10);                // 5 min duration, 10-sec windows
            lastCfg = cfg;

            System.out.printf("[S10|%s] Starting 5-minute experiment...%n", strat);
            List<WindowMetrics> results = EventBusTestUtils.runScenario10Experiment(cfg);
            allResults.addAll(results);
            printScenario10Summary("S10", strat, results);
        }

        Path csv = EventBusTestUtils.csvPath("experiments", "scenario_10_mixed_publishers_subscribers");
        EventBusTestUtils.writeScenario10Csv(csv, allResults);
        if (lastCfg != null) EventBusTestUtils.writeScenario10Meta(csv, lastCfg);
        System.out.println("Scenario 10 CSV: " + csv.toAbsolutePath());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void printResult(String scenario, ExperimentResult r) {
        System.out.printf("[%s|%s] %s=%s pool=%d pub=%d sub=%d | p50=%.1fms p99=%.1fms | %.1fmsg/s %.4fMbit/s oo=%.1f%%%s%n",
                scenario, r.config.strategyName,
                r.config.paramName, r.config.paramValue,
                r.config.subscriberThreadPoolSize,
                r.config.numPublishers, r.config.numSubscribers,
                r.e2eP50Ms, r.e2eP99Ms,
                r.msgRatePerSec, r.throughputMbps,
                r.outOfOrderPct,
                r.timedOut ? " [TIMEOUT]" : "");
    }

    private static void printScenario10Summary(String scenario, String strat, List<WindowMetrics> windows) {
        if (windows.isEmpty()) {
            System.out.printf("[%s|%s] No data%n", scenario, strat);
            return;
        }
        Map<String, List<WindowMetrics>> bySub = new LinkedHashMap<>();
        for (WindowMetrics w : windows)
            bySub.computeIfAbsent(w.subscriberId, k -> new ArrayList<>()).add(w);

        for (Map.Entry<String, List<WindowMetrics>> e : bySub.entrySet()) {
            List<WindowMetrics> ws = e.getValue();
            double sumP50 = 0, sumRate = 0;
            for (WindowMetrics w : ws) { sumP50 += w.e2eP50Ms; sumRate += w.msgRatePerSec; }
            System.out.printf("[%s|%s] %s(%s) windows=%d avgP50=%.1fms avgRate=%.1fmsg/s%n",
                    scenario, strat, e.getKey(),
                    ws.get(0).subscriberType, ws.size(),
                    sumP50 / ws.size(), sumRate / ws.size());
        }
    }
}

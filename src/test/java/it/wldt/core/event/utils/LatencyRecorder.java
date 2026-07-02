package it.wldt.core.event.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Non-intrusive latency recorder for EventBus characterization tests.
 *
 * Publisher hot path  : recordSend(i) before publishEvent(), recordReturn(i) after.
 * Subscriber hot path : recordArrival(i) inside onEvent() using the event's Integer body as index.
 *
 * Latency computation (per event):
 *   - if arrivalNanos[i] > 0  →  arrivalNanos[i] - sendNanos[i]  (end-to-end, preferred)
 *   - otherwise               →  returnNanos[i]  - sendNanos[i]  (publisher-side round-trip)
 *
 * This lets the same recorder work for both sync strategies (arrival ≈ return) and async
 * strategies (arrival lags return by the executor queue depth).
 */
public final class LatencyRecorder {

    private final long[] sendNanos;
    private final long[] returnNanos;
    private final long[] arrivalNanos;
    private final int capacity;
    private volatile long startNano;
    private volatile long stopNano;
    private int count;

    public LatencyRecorder(int capacity) {
        this.capacity    = capacity;
        this.sendNanos   = new long[capacity];
        this.returnNanos = new long[capacity];
        this.arrivalNanos = new long[capacity];
    }

    public void start()  { startNano = System.nanoTime(); }
    public void stop()   { stopNano  = System.nanoTime(); }

    /** Call immediately before publishEvent(). */
    public void recordSend(int idx)    { sendNanos[idx]    = System.nanoTime(); }

    /** Call immediately after publishEvent() returns. Updates count. */
    public void recordReturn(int idx)  { returnNanos[idx]  = System.nanoTime(); count = idx + 1; }

    /** Call inside onEvent() with the event's Integer body as the index. */
    public void recordArrival(int idx) { arrivalNanos[idx] = System.nanoTime(); }

    public int getCount() { return count; }

    /** Returns the latency at percentile p (0–100) in nanoseconds. */
    public long percentileNs(double p) {
        if (count == 0) return 0L;
        long[] sorted = buildSortedLatencies();
        int idx = (int) Math.min((long) (count * p / 100.0), (long) (count - 1));
        return sorted[idx];
    }

    public double throughputEventsPerSec() {
        long wallNs = stopNano - startNano;
        return count == 0 || wallNs <= 0 ? 0.0 : (double) count / (wallNs / 1_000_000_000.0);
    }

    public long wallClockMs() {
        return (stopNano - startNano) / 1_000_000L;
    }

    public void writeCsv(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(file))) {
            w.println("event_index,latency_ns,elapsed_ms");
            for (int i = 0; i < count; i++) {
                long latencyNs = latency(i);
                long refNano   = arrivalNanos[i] > 0 ? arrivalNanos[i] : returnNanos[i];
                long elapsedMs = (refNano - startNano) / 1_000_000L;
                w.printf("%d,%d,%d%n", i, latencyNs, elapsedMs);
            }
        }
    }

    public void printSummary(String label, Path csvPath) {
        System.out.printf("%n=== %s (%,d events) ===%n", label, count);
        System.out.printf("  Throughput : %,.0f events/sec%n", throughputEventsPerSec());
        System.out.printf("  p50        : %,d ns%n",  percentileNs(50));
        System.out.printf("  p95        : %,d ns%n",  percentileNs(95));
        System.out.printf("  p99        : %,d ns%n",  percentileNs(99));
        System.out.printf("  p99.9      : %,d ns%n",  percentileNs(99.9));
        System.out.printf("  max        : %,d ns%n",  percentileNs(100));
        System.out.printf("  wall clock : %,d ms%n",  wallClockMs());
        if (csvPath != null) System.out.printf("  CSV        : %s%n", csvPath.toAbsolutePath());
    }

    private long latency(int i) {
        return arrivalNanos[i] > 0
                ? arrivalNanos[i] - sendNanos[i]
                : returnNanos[i]  - sendNanos[i];
    }

    private long[] buildSortedLatencies() {
        long[] lat = new long[count];
        for (int i = 0; i < count; i++) lat[i] = latency(i);
        Arrays.sort(lat);
        return lat;
    }
}

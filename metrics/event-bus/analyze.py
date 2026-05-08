#!/usr/bin/env python3
"""
EventBus experiment analyzer — multi-strategy comparison.

Usage:
    python analyze.py                    # analyze the latest run
    python analyze.py 20260506_130645    # analyze a specific run

Graphs are saved in each experiment-type subfolder:
    <run>/latency/graphs/
    <run>/throughput/graphs/
    <run>/contention/graphs/
"""

import csv
import glob
import sys
from pathlib import Path

try:
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    import numpy as np
    HAS_DEPS = True
except ImportError:
    HAS_DEPS = False

BASE_DIR = Path(__file__).parent

# ── Strategy palette ──────────────────────────────────────────────────────────
STRATEGIES = {
    "default":                 {"label": "Default (Sync)",           "color": "#2196F3", "ls": "-"},
    "perdt_async":             {"label": "Per-DT Async",             "color": "#FF9800", "ls": "--"},
    "perdt_queued":            {"label": "Per-DT Queued",            "color": "#4CAF50", "ls": "-."},
    "per_topic_per_subscriber":{"label": "Per-Topic Per-Subscriber", "color": "#9C27B0", "ls": ":"},
}
STRATEGY_ORDER = list(STRATEGIES.keys())


# ─── I/O helpers ─────────────────────────────────────────────────────────────

def find_latest_run():
    runs = sorted(
        [d for d in BASE_DIR.iterdir() if d.is_dir() and d.name[0].isdigit()],
        key=lambda d: d.name,
    )
    if not runs:
        raise FileNotFoundError(f"No run directories found in {BASE_DIR}")
    return runs[-1]


def read_latency_csv(path):
    """Returns (latencies_ns: list[int], elapsed_ms: list[int])."""
    with open(path, newline="") as f:
        rows = list(csv.DictReader(f))
    return [int(r["latency_ns"]) for r in rows], [int(r["elapsed_ms"]) for r in rows]


def read_summary_csv(path):
    """Returns {metric: float} from a key-value CSV; non-numeric rows are skipped."""
    result = {}
    with open(path, newline="") as f:
        for r in csv.DictReader(f):
            try:
                result[r["metric"]] = float(r["value"])
            except (ValueError, KeyError):
                pass
    return result


def discover(directory, prefix):
    """Return {strategy: Path} for all <prefix>_<strategy>.csv files in directory."""
    found = {}
    for strat in STRATEGY_ORDER:
        p = directory / f"{prefix}_{strat}.csv"
        if p.exists():
            found[strat] = p
    return found


# ─── Statistics ──────────────────────────────────────────────────────────────

def percentile(data, p):
    if not data:
        return 0.0
    idx = min(int(len(data) * p / 100.0), len(data) - 1)
    return sorted(data)[idx]


def rolling_mean(data, window):
    out, s = [], 0
    for i, v in enumerate(data):
        s += v
        if i >= window:
            s -= data[i - window]
        out.append(s / min(i + 1, window))
    return out


def print_stats(label, lats_ns):
    if not lats_ns:
        return
    lats_us = [v / 1000.0 for v in lats_ns]
    print(f"\n  {label}")
    print(f"    count  : {len(lats_us):,}")
    print(f"    p50    : {percentile(lats_us, 50):.2f} µs")
    print(f"    p95    : {percentile(lats_us, 95):.2f} µs")
    print(f"    p99    : {percentile(lats_us, 99):.2f} µs")
    print(f"    p99.9  : {percentile(lats_us, 99.9):.2f} µs")
    print(f"    max    : {percentile(lats_us, 100):.2f} µs")


def save_fig(path):
    plt.tight_layout()
    plt.savefig(path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


# ─── Latency analysis ────────────────────────────────────────────────────────

def analyze_latency(latency_dir, graphs_dir):
    graphs_dir.mkdir(parents=True, exist_ok=True)

    # ── 1. Baseline CDF + time-series (all strategies overlaid) ──────────────
    baseline_files = discover(latency_dir, "latency_single_subscriber")
    if baseline_files:
        for strat, fpath in baseline_files.items():
            lats_ns, _ = read_latency_csv(fpath)
            print_stats(f"latency_single_subscriber [{strat}]", lats_ns)

        if HAS_DEPS:
            fig, axes = plt.subplots(1, 2, figsize=(13, 5))

            ax = axes[0]
            for strat, fpath in baseline_files.items():
                cfg = STRATEGIES[strat]
                lats_ns, _ = read_latency_csv(fpath)
                s = sorted([v / 1000.0 for v in lats_ns])
                n = len(s)
                ax.plot(s, [i / n * 100 for i in range(n)],
                        lw=1.5, color=cfg["color"], ls=cfg["ls"], label=cfg["label"])
                for p, alpha, lbl in [(50, 0.6, "p50"), (99, 0.9, "p99")]:
                    v = percentile(s, p)
                    ax.axvline(v, color=cfg["color"], ls=":", alpha=alpha, lw=0.8)
            ax.set_xlabel("End-to-end Latency (µs)")
            ax.set_ylabel("Cumulative %")
            ax.set_title("Single Subscriber — Latency CDF")
            ax.legend()
            ax.grid(True, alpha=0.3)

            ax = axes[1]
            for strat, fpath in baseline_files.items():
                cfg = STRATEGIES[strat]
                lats_ns, elapsed = read_latency_csv(fpath)
                lats_us = [v / 1000.0 for v in lats_ns]
                window = max(10, len(lats_us) // 100)
                rm = rolling_mean(lats_us, window)
                ax.plot(elapsed[:len(rm)], rm, lw=1.2,
                        color=cfg["color"], ls=cfg["ls"],
                        label=f"{cfg['label']} (rolling avg w={window})")
            ax.set_xlabel("Elapsed (ms)")
            ax.set_ylabel("Latency (µs)")
            ax.set_title("Single Subscriber — Latency over Time")
            ax.legend(fontsize=8)
            ax.grid(True, alpha=0.3)

            save_fig(graphs_dir / "latency_baseline.png")

    # ── 2. Subscriber count — p99 lines per strategy ──────────────────────────
    count_data = {strat: {} for strat in STRATEGY_ORDER}
    for strat in STRATEGY_ORDER:
        for n in [1, 5, 10, 20]:
            p = latency_dir / f"latency_subscriber_count_{n}_{strat}.csv"
            if p.exists():
                lats_ns, _ = read_latency_csv(p)
                count_data[strat][n] = lats_ns
                print_stats(f"latency_subscriber_count_{n} [{strat}]", lats_ns)

    has_count = any(count_data[s] for s in STRATEGY_ORDER)
    if has_count and HAS_DEPS:
        fig, axes = plt.subplots(1, 2, figsize=(13, 5))
        ns = [1, 5, 10, 20]

        for ax_idx, pct in enumerate([50, 99]):
            ax = axes[ax_idx]
            for strat in STRATEGY_ORDER:
                cfg = STRATEGIES[strat]
                vals = [percentile([v / 1000.0 for v in count_data[strat].get(n, [0])], pct)
                        for n in ns]
                ax.plot(ns, vals, marker="o", lw=1.5,
                        color=cfg["color"], ls=cfg["ls"], label=cfg["label"])
            ax.set_xlabel("Number of Subscribers")
            ax.set_ylabel(f"p{pct} Latency (µs)")
            ax.set_title(f"Delivery p{pct} Latency vs Subscriber Count")
            ax.legend()
            ax.grid(True, alpha=0.3)

        save_fig(graphs_dir / "subscriber_count_impact.png")

    # ── 3. Exact vs Wildcard — CDF per strategy ───────────────────────────────
    exact_files    = discover(latency_dir, "latency_exact_match")
    wildcard_files = discover(latency_dir, "latency_wildcard_match")
    if (exact_files or wildcard_files) and HAS_DEPS:
        fig, ax = plt.subplots(figsize=(9, 5))
        for strat in STRATEGY_ORDER:
            cfg = STRATEGIES[strat]
            if strat in exact_files:
                e_ns, _ = read_latency_csv(exact_files[strat])
                e_us = sorted([v / 1000.0 for v in e_ns])
                ax.plot(e_us, [i / len(e_us) * 100 for i in range(len(e_us))],
                        lw=1.5, color=cfg["color"], ls="-",
                        label=f"{cfg['label']} — exact")
            if strat in wildcard_files:
                w_ns, _ = read_latency_csv(wildcard_files[strat])
                w_us = sorted([v / 1000.0 for v in w_ns])
                ax.plot(w_us, [i / len(w_us) * 100 for i in range(len(w_us))],
                        lw=1.5, color=cfg["color"], ls="--",
                        label=f"{cfg['label']} — wildcard")
        ax.set_xlabel("Latency (µs)")
        ax.set_ylabel("Cumulative %")
        ax.set_title("Exact vs Wildcard Subscription — Latency CDF")
        ax.legend(fontsize=8)
        ax.grid(True, alpha=0.3)
        save_fig(graphs_dir / "exact_vs_wildcard.png")


# ─── Throughput analysis ─────────────────────────────────────────────────────

def analyze_throughput(throughput_dir, graphs_dir):
    graphs_dir.mkdir(parents=True, exist_ok=True)

    # ── 1. Single publisher — publisher-side latency histogram + CDF ──────────
    single_files = discover(throughput_dir, "throughput_single_publisher")
    single_tp = {}

    for strat, fpath in single_files.items():
        lats_ns, elapsed = read_latency_csv(fpath)
        lats_us = [v / 1000.0 for v in lats_ns]
        total_ms = elapsed[-1] if elapsed else 1
        tp = len(lats_ns) / (total_ms / 1000.0) if total_ms > 0 else 0
        single_tp[strat] = tp
        print_stats(f"throughput_single_publisher [{strat}]", lats_ns)
        print(f"    throughput : {tp:,.0f} events/sec")

    if single_files and HAS_DEPS:
        fig, axes = plt.subplots(1, 2, figsize=(13, 5))

        ax = axes[0]
        for strat, fpath in single_files.items():
            cfg = STRATEGIES[strat]
            lats_ns, _ = read_latency_csv(fpath)
            lats_us = [v / 1000.0 for v in lats_ns]
            ax.hist(lats_us, bins=60, alpha=0.6,
                    color=cfg["color"], edgecolor="none", label=cfg["label"])
        ax.set_xlabel("Publisher-side Latency (µs)")
        ax.set_ylabel("Event count")
        ax.set_title("Single Publisher — Latency Histogram")
        ax.legend()
        ax.grid(True, axis="y", alpha=0.3)

        ax = axes[1]
        for strat, fpath in single_files.items():
            cfg = STRATEGIES[strat]
            lats_ns, _ = read_latency_csv(fpath)
            s = sorted([v / 1000.0 for v in lats_ns])
            n = len(s)
            ax.plot(s, [i / n * 100 for i in range(n)],
                    lw=1.5, color=cfg["color"], ls=cfg["ls"], label=cfg["label"])
        ax.set_xlabel("Publisher-side Latency (µs)")
        ax.set_ylabel("Cumulative %")
        ax.set_title("Single Publisher — Latency CDF")
        ax.legend()
        ax.grid(True, alpha=0.3)

        save_fig(graphs_dir / "single_publisher.png")

    # ── 2. Concurrent publishers — throughput grouped bars ────────────────────
    # agg_tp[strat][num_pub] = throughput
    agg_tp  = {s: {} for s in STRATEGY_ORDER}
    agg_p99 = {s: {} for s in STRATEGY_ORDER}

    for strat in STRATEGY_ORDER:
        for np_count in [2, 4, 8]:
            summary_p = throughput_dir / f"concurrent_summary_pub{np_count}_{strat}.csv"
            pattern   = str(throughput_dir / f"throughput_concurrent_pub{np_count}_publisher*_{strat}.csv")
            files     = glob.glob(pattern)
            if not files and not summary_p.exists():
                continue

            all_ns = []
            for f in files:
                ns, _ = read_latency_csv(f)
                all_ns.extend(ns)

            if summary_p.exists():
                s = read_summary_csv(summary_p)
                agg_tp[strat][np_count] = s.get("throughput_events_per_sec", 0)
            elif all_ns:
                total_ms = max((read_latency_csv(f)[1] or [0])[-1] for f in files)
                agg_tp[strat][np_count] = len(all_ns) / (total_ms / 1000.0) if total_ms > 0 else 0

            if all_ns:
                agg_p99[strat][np_count] = percentile([v / 1000.0 for v in all_ns], 99)

            print(f"\n  concurrent_pub{np_count} [{strat}]")
            print(f"    throughput : {agg_tp[strat].get(np_count, 0):,.0f} events/sec")
            if all_ns:
                print(f"    p99        : {agg_p99[strat].get(np_count, 0):.2f} µs")

        # Include single-publisher in concurrent chart
        if strat in single_tp:
            agg_tp[strat][1] = single_tp[strat]
            sp = single_files.get(strat)
            if sp:
                ns, _ = read_latency_csv(sp)
                agg_p99[strat][1] = percentile([v / 1000.0 for v in ns], 99)

    has_concurrent = any(agg_tp[s] for s in STRATEGY_ORDER)
    if has_concurrent and HAS_DEPS:
        pub_counts = sorted({c for s in STRATEGY_ORDER for c in agg_tp[s]})
        x = np.arange(len(pub_counts))
        n_strats = len(STRATEGY_ORDER)
        width = 0.8 / n_strats

        fig, axes = plt.subplots(1, 2, figsize=(13, 5))

        ax = axes[0]
        for i, strat in enumerate(STRATEGY_ORDER):
            cfg = STRATEGIES[strat]
            vals = [agg_tp[strat].get(c, 0) for c in pub_counts]
            offset = (i - (n_strats - 1) / 2.0) * width
            bars = ax.bar(x + offset, vals, width,
                          label=cfg["label"], color=cfg["color"], alpha=0.85)
            ax.bar_label(bars, labels=[f"{v:,.0f}" for v in vals],
                         padding=2, fontsize=7)
        ax.set_xticks(x)
        ax.set_xticklabels([str(c) for c in pub_counts])
        ax.set_xlabel("Number of Concurrent Publishers")
        ax.set_ylabel("Total Throughput (events/sec)")
        ax.set_title("Throughput vs Publisher Count\n(strategy comparison)")
        ax.legend()
        ax.grid(True, axis="y", alpha=0.3)

        ax = axes[1]
        for i, strat in enumerate(STRATEGY_ORDER):
            cfg = STRATEGIES[strat]
            vals = [agg_p99[strat].get(c, 0) for c in pub_counts]
            offset = (i - (n_strats - 1) / 2.0) * width
            bars = ax.bar(x + offset, vals, width,
                          label=cfg["label"], color=cfg["color"], alpha=0.85)
            ax.bar_label(bars, labels=[f"{v:.1f}µs" for v in vals],
                         padding=2, fontsize=7)
        ax.set_xticks(x)
        ax.set_xticklabels([str(c) for c in pub_counts])
        ax.set_xlabel("Number of Concurrent Publishers")
        ax.set_ylabel("p99 Latency (µs)")
        ax.set_title("p99 Latency vs Publisher Count\n(strategy comparison)")
        ax.legend()
        ax.grid(True, axis="y", alpha=0.3)

        save_fig(graphs_dir / "concurrent_publishers_impact.png")

    # ── 3. CDF overlay per publisher count, one line per strategy ─────────────
    for np_count in [2, 4, 8]:
        cdf_series = {}
        for strat in STRATEGY_ORDER:
            pattern = str(throughput_dir / f"throughput_concurrent_pub{np_count}_publisher*_{strat}.csv")
            files = glob.glob(pattern)
            if files:
                all_ns = []
                for f in files:
                    ns, _ = read_latency_csv(f)
                    all_ns.extend(ns)
                cdf_series[strat] = sorted([v / 1000.0 for v in all_ns])

        if len(cdf_series) >= 1 and HAS_DEPS:
            fig, ax = plt.subplots(figsize=(9, 5))
            for strat, s in cdf_series.items():
                cfg = STRATEGIES[strat]
                ax.plot(s, [i / len(s) * 100 for i in range(len(s))],
                        lw=1.5, color=cfg["color"], ls=cfg["ls"], label=cfg["label"])
            ax.set_xlabel("Publisher-side Latency (µs)")
            ax.set_ylabel("Cumulative %")
            ax.set_title(f"Latency CDF — {np_count} Concurrent Publishers")
            ax.legend()
            ax.grid(True, alpha=0.3)
            save_fig(graphs_dir / f"concurrent_pub{np_count}_latency_cdf.png")


# ─── Contention analysis ─────────────────────────────────────────────────────

def analyze_contention(contention_dir, graphs_dir, throughput_dir=None):
    graphs_dir.mkdir(parents=True, exist_ok=True)

    summary_files = discover(contention_dir, "contention_summary")
    if not summary_files:
        print("  No contention_summary_<strategy>.csv found, skipping")
        return

    summaries = {}
    for strat, fpath in summary_files.items():
        s = read_summary_csv(fpath)
        summaries[strat] = s
        print(f"\n  contention_summary [{strat}]")
        print(f"    total published  : {s.get('total_published', 0):,.0f}")
        print(f"    total received   : {s.get('total_received',  0):,.0f}")
        print(f"    sub/unsub cycles : {s.get('sub_unsub_cycles', 0):,.0f}")
        print(f"    throughput       : {s.get('throughput_events_per_sec', 0):,.0f} events/sec")
        print(f"    wall clock       : {s.get('wall_clock_ms', 0):,.0f} ms")

    if not HAS_DEPS:
        return

    n_strats = len(summaries)
    width = 0.35

    fig, axes = plt.subplots(1, 2, figsize=(13, 5))

    # ── Correctness: published vs received per strategy ───────────────────────
    ax = axes[0]
    strats = [s for s in STRATEGY_ORDER if s in summaries]
    x = np.arange(len(strats))
    pub_vals = [summaries[s].get("total_published", 0) for s in strats]
    rec_vals = [summaries[s].get("total_received",  0) for s in strats]
    bars_pub = ax.bar(x - width / 2, pub_vals, width, label="Published", color="#90CAF9", alpha=0.9)
    bars_rec = ax.bar(x + width / 2, rec_vals, width, label="Received",
                      color=["#4CAF50" if p == r else "#F44336"
                             for p, r in zip(pub_vals, rec_vals)], alpha=0.9)
    ax.bar_label(bars_pub, labels=[f"{v:,.0f}" for v in pub_vals], padding=3, fontsize=8)
    ax.bar_label(bars_rec, labels=[f"{v:,.0f}" for v in rec_vals], padding=3, fontsize=8)
    ax.set_xticks(x)
    ax.set_xticklabels([STRATEGIES[s]["label"] for s in strats])
    ax.set_ylabel("Event count")
    ax.set_title("Contention — Delivery Correctness per Strategy")
    ax.legend()
    ax.grid(True, axis="y", alpha=0.3)

    # ── Throughput: contention vs baseline per strategy ───────────────────────
    ax = axes[1]
    bar_groups, group_labels = [], []

    for strat in strats:
        cfg = STRATEGIES[strat]
        contention_tp = summaries[strat].get("throughput_events_per_sec", 0)
        group_vals = [contention_tp]
        group_lbls = ["Contention"]

        if throughput_dir is not None:
            sp = throughput_dir / f"throughput_single_publisher_{strat}.csv"
            if sp.exists():
                ns, elapsed = read_latency_csv(sp)
                total_ms = elapsed[-1] if elapsed else 1
                tp_single = len(ns) / (total_ms / 1000.0) if total_ms > 0 else 0
                group_vals.insert(0, tp_single)
                group_lbls.insert(0, "Single pub")

        bar_groups.append((strat, group_vals, group_lbls))

    # Flatten into a grouped bar layout
    all_labels, all_vals, all_colors = [], [], []
    x_ticks, x_labels = [], []
    xi = 0
    for strat, vals, lbls in bar_groups:
        cfg = STRATEGIES[strat]
        for v, lbl in zip(vals, lbls):
            all_labels.append(lbl)
            all_vals.append(v)
            all_colors.append(cfg["color"])
        x_center = xi + (len(vals) - 1) / 2.0
        x_ticks.append(x_center)
        x_labels.append(cfg["label"])
        xi += len(vals) + 1  # gap between strategy groups

    x_pos = list(range(len(all_vals)))
    # Re-position with gaps
    pos = []
    xi = 0
    for strat, vals, lbls in bar_groups:
        for j in range(len(vals)):
            pos.append(xi)
            xi += 1
        xi += 1  # gap

    x_ticks_real = []
    xi = 0
    for strat, vals, lbls in bar_groups:
        x_ticks_real.append(xi + (len(vals) - 1) / 2.0)
        xi += len(vals) + 1

    bars = ax.bar(pos, all_vals, color=all_colors, alpha=0.85)
    ax.bar_label(bars, labels=[f"{v:,.0f}" for v in all_vals], padding=3, fontsize=8)
    ax.set_xticks(x_ticks_real)
    ax.set_xticklabels(x_labels)
    ax.set_ylabel("Throughput (events/sec)")
    ax.set_title("Throughput: Baseline vs Under Contention")
    ax.grid(True, axis="y", alpha=0.3)

    # Add sub-label annotations
    for xi_pos, lbl in zip(pos, all_labels):
        ax.text(xi_pos, -max(all_vals) * 0.05, lbl,
                ha="center", va="top", fontsize=7, color="gray")

    save_fig(graphs_dir / "contention_summary.png")


# ─── Experiment analysis ──────────────────────────────────────────────────────

def analyze_experiments(run_dir: Path) -> None:
    """Reads scenario CSVs from <run>/experiments/ and writes one comparison graph per scenario."""
    exp_dir = run_dir / "experiments"
    if not exp_dir.exists():
        return

    print("\n[experiments]")

    # (csv_name, param_col, x_label, fixed_params_label)
    scenarios = [
        ("scenario_1_rate_variation",
         "msg_rate_per_pub",   "Message Rate [msg/s]",
         "size=100 B  |  proc=10 ms  |  1 publisher  |  1 subscriber"),
        ("scenario_2_size_variation",
         "event_size_bytes",   "Event Size [bytes]",
         "rate=20 msg/s  |  proc=10 ms  |  1 publisher  |  1 subscriber"),
        ("scenario_3_processing_time_variation",
         "processing_time_ms", "Subscriber Processing Time [ms]",
         "rate=20 msg/s  |  size=100 B  |  1 publisher  |  1 subscriber"),
        ("scenario_4_publisher_count_variation",
         "num_publishers",     "Number of Publishers",
         "rate=20 msg/s  |  size=100 B  |  proc=10 ms  |  1 subscriber"),
        ("scenario_5_subscriber_count_variation",
         "num_subscribers",    "Number of Subscribers",
         "rate=20 msg/s  |  size=100 B  |  proc=10 ms  |  4 publishers"),
        ("scenario_6_large_payload_rate_variation",
         "msg_rate_per_pub",   "Message Rate [msg/s]",
         "size=500 KB  |  proc=10 ms  |  4 publishers  |  4 subscribers"),
        ("scenario_7_high_rate_size_variation",
         "event_size_bytes",   "Event Size [bytes]",
         "rate=100 msg/s  |  proc=10 ms  |  4 publishers  |  4 subscribers"),
        ("scenario_8_topic_count_variation",
         "num_topics",         "Number of Topics",
         "rate=100 msg/s  |  size=200 KB  |  proc=10 ms  |  4 publishers  |  4 subscribers"),
        ("scenario_9_parallel_subscriber_pool_variation",
         "subscriber_pool_size", "Subscriber Thread Pool Size [threads]",
         "rate=100 msg/s  |  size=500 KB  |  proc=10 ms  |  4 publishers  |  4 subscribers  |  parallel subscriber"),
    ]

    for csv_name, param_col, x_label, fixed_label in scenarios:
        csv_path = exp_dir / f"{csv_name}.csv"
        if not csv_path.exists():
            continue

        # Parse CSV
        rows = []
        with open(csv_path, newline="") as f:
            for row in csv.DictReader(f):
                try:
                    rows.append({
                        "strategy":       row["strategy"],
                        "param":          float(row["param_value"]),
                        "e2e_p50_ms":     float(row["e2e_p50_ms"]),
                        "e2e_p99_ms":     float(row["e2e_p99_ms"]),
                        "msg_rate":       float(row["msg_rate_per_sec"]),
                        "throughput":     float(row["throughput_mbps"]),
                        "out_of_order":   float(row.get("out_of_order_pct", 0)),
                    })
                except (ValueError, KeyError):
                    continue

        if not rows:
            continue

        # Print per-strategy summary
        by_strat = {}
        for row in rows:
            by_strat.setdefault(row["strategy"], []).append(row)

        for strat in STRATEGY_ORDER:
            if strat not in by_strat:
                continue
            cfg  = STRATEGIES[strat]
            data = sorted(by_strat[strat], key=lambda r: r["param"])
            print(f"\n  {csv_name} [{cfg['label']}]")
            for d in data:
                print(f"    {param_col}={d['param']:.0f} | "
                      f"p50={d['e2e_p50_ms']:.2f}ms p99={d['e2e_p99_ms']:.2f}ms | "
                      f"{d['msg_rate']:.1f} msg/s  {d['throughput']:.4f} Mbit/s | "
                      f"out-of-order={d['out_of_order']:.1f}%")

        if not HAS_DEPS:
            continue

        fig, axes = plt.subplots(1, 4, figsize=(20, 5))
        scenario_title = " ".join(w.capitalize() for w in csv_name.replace("_", " ").split())
        fig.suptitle(
            f"EventBus Strategy Comparison — {scenario_title}\n"
            f"Fixed: {fixed_label}",
            fontsize=11,
        )

        for strat in STRATEGY_ORDER:
            if strat not in by_strat:
                continue
            cfg  = STRATEGIES[strat]
            data = sorted(by_strat[strat], key=lambda r: r["param"])
            xs   = [d["param"] for d in data]

            axes[0].plot(xs, [d["e2e_p50_ms"] for d in data],
                         label=f"{cfg['label']} p50", color=cfg["color"], ls="-",  marker="o", ms=5)
            axes[0].plot(xs, [d["e2e_p99_ms"] for d in data],
                         label=f"{cfg['label']} p99", color=cfg["color"], ls="--", marker="s", ms=5)

            axes[1].plot(xs, [d["msg_rate"]   for d in data],
                         label=cfg["label"], color=cfg["color"], ls=cfg["ls"], marker="o", ms=5)

            axes[2].plot(xs, [d["throughput"] for d in data],
                         label=cfg["label"], color=cfg["color"], ls=cfg["ls"], marker="o", ms=5)

            axes[3].plot(xs, [d["out_of_order"] for d in data],
                         label=cfg["label"], color=cfg["color"], ls=cfg["ls"], marker="o", ms=5)

        axes[0].set(title="E2E Delay", xlabel=x_label, ylabel="Delay [ms]")
        axes[0].legend(fontsize=7)
        axes[0].grid(True, alpha=0.3)

        axes[1].set(title="Message Rate", xlabel=x_label, ylabel="Messages [msg/s]")
        axes[1].legend(fontsize=8)
        axes[1].grid(True, alpha=0.3)

        axes[2].set(title="Throughput", xlabel=x_label, ylabel="Throughput [Mbit/s]")
        axes[2].legend(fontsize=8)
        axes[2].grid(True, alpha=0.3)

        axes[3].set(title="Out-of-Order Delivery", xlabel=x_label, ylabel="Out-of-Order [%]")
        axes[3].legend(fontsize=8)
        axes[3].grid(True, alpha=0.3)

        plt.tight_layout()
        out_path = exp_dir / f"{csv_name}.png"
        fig.savefig(out_path, dpi=150, bbox_inches="tight")
        plt.close(fig)
        print(f"  Saved: {out_path}")


# ─── Scenario 10: Mixed publishers + mixed subscribers, time-windowed ────────

# Per-subscriber color/linestyle: blue tones = sequential, red/amber = parallel
_SUB_STYLES = {
    "sub-0": ("#1565C0", "-",  "sub-0 (sequential)"),
    "sub-1": ("#42A5F5", "--", "sub-1 (sequential)"),
    "sub-2": ("#BF360C", "-",  "sub-2 (parallel)"),
    "sub-3": ("#FF8F00", "--", "sub-3 (parallel)"),
}


def analyze_scenario10(run_dir: Path) -> None:
    """Time-windowed analysis for Scenario 10 — one figure per bus strategy."""
    exp_dir  = run_dir / "experiments"
    csv_path = exp_dir / "scenario_10_mixed_publishers_subscribers.csv"
    if not csv_path.exists():
        return

    print("\n[scenario 10 — mixed publishers & subscribers]")

    rows = []
    with open(csv_path, newline="") as f:
        for row in csv.DictReader(f):
            try:
                rows.append({
                    "t_start":  int(row["window_start_sec"]),
                    "strategy": row["strategy"],
                    "sub_id":   row["subscriber_id"],
                    "sub_type": row["subscriber_type"],
                    "msgs":     int(row["msgs_delivered"]),
                    "p50":      float(row["e2e_p50_ms"]),
                    "p95":      float(row["e2e_p95_ms"]),
                    "p99":      float(row["e2e_p99_ms"]),
                    "rate":     float(row["msg_rate_per_sec"]),
                    "oo":       float(row["out_of_order_pct"]),
                })
            except (ValueError, KeyError):
                continue

    if not rows:
        return

    # Group: strategy → sub_id → [rows]
    by_strat = {}
    for r in rows:
        by_strat.setdefault(r["strategy"], {}).setdefault(r["sub_id"], []).append(r)

    for strat in STRATEGY_ORDER:
        if strat not in by_strat:
            continue
        strat_cfg = STRATEGIES[strat]
        by_sub    = by_strat[strat]

        print(f"\n  scenario_10 [{strat_cfg['label']}]")
        for sub_id, sub_rows in sorted(by_sub.items()):
            sub_type = sub_rows[0]["sub_type"]
            n        = len(sub_rows)
            avg_p50  = sum(r["p50"]  for r in sub_rows) / n
            avg_p99  = sum(r["p99"]  for r in sub_rows) / n
            avg_rate = sum(r["rate"] for r in sub_rows) / n
            print(f"    {sub_id} ({sub_type}): {n} windows | "
                  f"avg-p50={avg_p50:.1f}ms  avg-p99={avg_p99:.1f}ms | {avg_rate:.1f} msg/s")

        if not HAS_DEPS:
            continue

        sub_ids = sorted(by_sub.keys())
        metrics = [
            ("p50",  "E2E Latency p50 [ms]"),
            ("p99",  "E2E Latency p99 [ms]"),
            ("rate", "Message Rate [msg/s]"),
            ("oo",   "Out-of-Order [%]"),
        ]

        fig, axes = plt.subplots(2, 2, figsize=(16, 10))
        fig.suptitle(
            f"Scenario 10 — Mixed Publishers & Subscribers\n"
            f"Strategy: {strat_cfg['label']}  |  "
            f"publishers: 100/50/25/10 Hz  |  2 sequential + 2 parallel subscribers  |  "
            f"500 KB payload  |  proc ∈ [10, 100] ms",
            fontsize=10,
        )
        axes_flat = [axes[0][0], axes[0][1], axes[1][0], axes[1][1]]

        for ax, (metric, ylabel) in zip(axes_flat, metrics):
            for sub_id in sub_ids:
                if sub_id not in by_sub:
                    continue
                sub_rows = sorted(by_sub[sub_id], key=lambda r: r["t_start"])
                color, ls, label = _SUB_STYLES.get(sub_id, ("#888888", "-", sub_id))
                ax.plot(
                    [r["t_start"] for r in sub_rows],
                    [r[metric]    for r in sub_rows],
                    color=color, ls=ls, marker="o", ms=3, lw=1.5, label=label,
                )
            ax.set_xlabel("Experiment Time [s]")
            ax.set_ylabel(ylabel)
            ax.set_title(ylabel)
            ax.legend(fontsize=8)
            ax.grid(True, alpha=0.3)

        plt.tight_layout()
        out_path = exp_dir / f"scenario_10_{strat}.png"
        fig.savefig(out_path, dpi=150, bbox_inches="tight")
        plt.close(fig)
        print(f"  Saved: {out_path}")


# ─── Main ─────────────────────────────────────────────────────────────────────

def main():
    if len(sys.argv) > 1:
        run_dir = BASE_DIR / sys.argv[1]
        if not run_dir.is_dir():
            print(f"Error: run directory not found: {run_dir}", file=sys.stderr)
            sys.exit(1)
    else:
        run_dir = find_latest_run()

    print(f"Run: {run_dir.name}  ({run_dir})")

    if not HAS_DEPS:
        print("\nWARNING: matplotlib/numpy not installed — printing statistics only.")
        print("Install with: pip install matplotlib numpy\n")

    latency_dir    = run_dir / "latency"
    throughput_dir = run_dir / "throughput"
    contention_dir = run_dir / "contention"

    if latency_dir.exists():
        print("\n[latency]")
        analyze_latency(latency_dir, latency_dir / "graphs")
    else:
        print("\n[latency] no data")

    if throughput_dir.exists():
        print("\n[throughput]")
        analyze_throughput(throughput_dir, throughput_dir / "graphs")
    else:
        print("\n[throughput] no data")

    if contention_dir.exists():
        print("\n[contention]")
        analyze_contention(
            contention_dir,
            contention_dir / "graphs",
            throughput_dir=throughput_dir if throughput_dir.exists() else None,
        )
    else:
        print("\n[contention] no data")

    analyze_experiments(run_dir)
    analyze_scenario10(run_dir)

    print(f"\nDone. Graphs saved under: {run_dir}")


if __name__ == "__main__":
    main()

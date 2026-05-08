#!/usr/bin/env bash
# Run the EventBus experiment analyzer.
#
# Usage:
#   ./run_analysis.sh                    # analyze the latest run
#   ./run_analysis.sh 20260506_130645    # analyze a specific run folder
#
# Graphs are saved inside the run's per-experiment-type subfolders.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANALYZE="$SCRIPT_DIR/analyze.py"

# Locate Python 3 (python3, py, or python)
PYTHON=""
for candidate in python3 py python; do
    if command -v "$candidate" &>/dev/null; then
        if "$candidate" -c "import sys; sys.exit(0 if sys.version_info >= (3,8) else 1)" 2>/dev/null; then
            PYTHON="$candidate"
            break
        fi
    fi
done

if [ -z "$PYTHON" ]; then
    echo "Error: Python 3.8+ not found. Please install Python 3.8+." >&2
    exit 1
fi

# Install matplotlib and numpy if not available
if ! "$PYTHON" -c "import matplotlib, numpy" &>/dev/null 2>&1; then
    echo "Installing required packages: matplotlib numpy ..."
    "$PYTHON" -m pip install --quiet matplotlib numpy
fi

"$PYTHON" "$ANALYZE" "$@"

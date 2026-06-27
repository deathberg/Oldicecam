#!/usr/bin/env bash
# Re-run Python native analysis (LIEF + Capstone + xref scan).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 tools/native_deep_analyze.py
python3 tools/native_xref_scan.py
echo "Updated docs/native_analysis/"

#!/usr/bin/env bash
# Export all high-value Ghidra decompilations after vcplax import.
# Prefer export_batch.sh (sequential, lock-safe).
set -euo pipefail
exec "$(dirname "$0")/export_batch.sh"

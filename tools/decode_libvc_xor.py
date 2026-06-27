#!/usr/bin/env python3
"""Offline XOR decode for libvc.so hook target strings (testicecam2 / runtime pull).

Ghidra image base: 0x100000. Blob VAs from init() @ file+0x774b0.
Keys are per-string single bytes from LoadedLib ctx (runtime); brute-force included.

Usage:
  python3 tools/decode_libvc_xor.py re-workspace/runtime-pull/libvc.so
  python3 tools/decode_libvc_xor.py /path/to/libvc.so --keys 0xb9,0x9e,0xcf,0xba,0xe1,0x93
"""
from __future__ import annotations

import argparse
import sys
from pathlib import Path

GHIDRA_BASE = 0x100000

ENTRIES = [
    ("lib_name", 0x14AE86, 0x13, 0x08),
    ("sym_1", 0x14AE99, 0x9F, 0x7D),
    ("sym_2", 0x14AF38, 0x9D, 0x7F),
    ("sym_3", 0x14AFD5, 0x99, 0x29),
    ("sym_4", 0x14B06E, 0x34, 0x4F),
    ("sym_5", 0x14B0A2, 0x28, 0x44),  # init reads puVar7[0x44], not 0x51
]

# Verified on runtime pull jysdd4 (brute-force + cross-check with Ghidra init)
DEFAULT_KEYS = [0xB9, 0x9E, 0xCF, 0xBA, 0xE1, 0x93]


def va_to_off(va: int) -> int:
    return va - GHIDRA_BASE


def decode(blob: bytes, va: int, length: int, key_byte: int) -> str:
    off = va_to_off(va)
    out = bytearray()
    seed = 7
    for i in range(length):
        if off + i >= len(blob):
            break
        b = seed ^ blob[off + i]
        seed = (seed + 0x1F) & 0xFF
        ch = ((i + b - 0x11) ^ (key_byte + i)) & 0xFF
        if ch == 0:
            break
        out.append(ch)
    return out.decode("latin-1", errors="replace")


def score(s: bytes) -> int:
    if not s:
        return -999
    printable = sum(32 <= c < 127 for c in s)
    bonus = 0
    t = s.decode("latin-1", errors="replace")
    for kw in ("lib", ".so", "_ZN", "android", "Camera", "Buffer", "Graphic"):
        if kw in t:
            bonus += 30
    return printable + bonus


def brute_key(blob: bytes, va: int, length: int) -> tuple[int, str]:
    best: list[tuple[int, int, str]] = []
    for kb in range(256):
        d = decode(blob, va, length, kb).encode("latin-1", errors="replace")
        sc = score(d)
        if sc >= 15:
            best.append((sc, kb, d.decode("latin-1", errors="replace")))
    best.sort(reverse=True)
    if not best:
        return 0, ""
    return best[0][1], best[0][2]


def main() -> int:
    ap = argparse.ArgumentParser(description="Decode libvc XOR hook strings")
    ap.add_argument("libvc", type=Path, help="libvc.so path (APK native or runtime pull)")
    ap.add_argument(
        "--keys",
        help="Comma-separated hex key bytes (6 values). Default: verified runtime keys",
    )
    ap.add_argument("--brute", action="store_true", help="Brute-force keys instead of defaults")
    args = ap.parse_args()

    blob = args.libvc.read_bytes()
    keys: list[int | None]
    if args.keys:
        keys = [int(x.strip(), 16) for x in args.keys.split(",")]
        if len(keys) != len(ENTRIES):
            print("Expected 6 key bytes", file=sys.stderr)
            return 1
    elif args.brute:
        keys = [None] * len(ENTRIES)
    else:
        keys = list(DEFAULT_KEYS)

    print(f"# libvc: {args.libvc} ({len(blob)} bytes)")
    print(f"# Ghidra base 0x{GHIDRA_BASE:x}, init @ file+0x774b0\n")
    for i, (name, va, ln, koff) in enumerate(ENTRIES):
        kb = keys[i]
        if kb is None:
            kb, preview = brute_key(blob, va, ln)
            print(f"[brute] {name} keyOff=+0x{koff:x} => key=0x{kb:02x}")
        else:
            preview = decode(blob, va, ln, kb)
        print(f"{name} (keyOff=+0x{koff:x}, key=0x{kb:02x}):")
        print(f"  {preview}\n")

    print("# Hook chain (libvc init @ 0x774b0):")
    print("#   shadowhook_hook_sym_name(lib, sym1..3, FUN_77238/772e4/77370)")
    print("#   on failure: sym4/5 -> FUN_773fc/77408 (trampolines to orig)")
    print("#   pthread_create(FUN_78558) — worker waits for inject queue")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

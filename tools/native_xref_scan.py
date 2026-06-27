#!/usr/bin/env python3
"""Find ARM64 adrp/add references to rodata strings and scan TX immediate constants."""
from __future__ import annotations

import struct
from pathlib import Path

try:
    from capstone import CS_ARCH_ARM64, CS_MODE_ARM, Cs
except ImportError:
    raise SystemExit("capstone required")

VCPLAX = Path("/workspace/decompiled/raw/lib/arm64-v8a/vcplax.so")
LIBVC = Path("/workspace/decompiled/raw/lib/arm64-v8a/libvc.so")


def parse_elf64(path: Path):
    data = path.read_bytes()
    if data[:4] != b"\x7fELF":
        raise ValueError("not elf")
    e_phoff = struct.unpack_from("<Q", data, 0x20)[0]
    e_phentsize = struct.unpack_from("<H", data, 0x36)[0]
    e_phnum = struct.unpack_from("<H", data, 0x38)[0]
    segs = []
    for i in range(e_phnum):
        off = e_phoff + i * e_phentsize
        p_type, p_flags, p_offset, p_vaddr, p_paddr, p_filesz, p_memsz, p_align = struct.unpack_from(
            "<IIQQQQQQ", data, off
        )
        if p_type == 1:  # PT_LOAD
            segs.append((p_vaddr, p_offset, p_filesz, p_memsz))
    e_shoff = struct.unpack_from("<Q", data, 0x28)[0]
    e_shentsize = struct.unpack_from("<H", data, 0x3A)[0]
    e_shnum = struct.unpack_from("<H", data, 0x3C)[0]
    e_shstrndx = struct.unpack_from("<H", data, 0x3E)[0]
    sections = {}
    shstr_off = struct.unpack_from("<Q", data, e_shoff + e_shstrndx * e_shentsize + 0x18)[0]
    for i in range(e_shnum):
        so = e_shoff + i * e_shentsize
        sh_name, sh_type, sh_flags, sh_addr, sh_offset, sh_size = struct.unpack_from("<IIQQQQ", data, so)[:6]
        name_end = data.find(b"\x00", shstr_off + sh_name)
        name = data[shstr_off + sh_name : name_end].decode()
        sections[name] = (sh_addr, sh_offset, sh_size, sh_flags)
    return data, segs, sections


def file_off_to_va(sections, file_off: int) -> int | None:
    for name, (addr, off, size, _fl) in sections.items():
        if off <= file_off < off + size:
            return addr + (file_off - off)
    return None


def scan_adrp_refs(data: bytes, sections, target_va: int, text_name=".text") -> list[int]:
    if text_name not in sections:
        return []
    t_addr, t_off, t_size, _ = sections[text_name]
    md = Cs(CS_ARCH_ARM64, CS_MODE_ARM)
    hits = []
    code = data[t_off : t_off + t_size]
    # Scan 4-byte aligned instructions looking for adrp that resolves near target_va
    for i in range(0, len(code) - 4, 4):
        ins = next(md.disasm(code[i : i + 4], t_addr + i), None)
        if not ins or ins.mnemonic != "adrp":
            continue
        # capstone gives op_str like "x8, #0xfe5000"
        try:
            page = int(ins.op_str.split("#")[1], 0) & ~0xFFF
        except Exception:
            continue
        # check next few insns for add with low 12 bits
        for j in range(4, 16, 4):
            ins2 = next(md.disasm(code[i + j : i + j + 4], t_addr + i + j), None)
            if not ins2 or ins2.mnemonic != "add":
                continue
            parts = ins2.op_str.split("#")
            if len(parts) < 2:
                continue
            try:
                lo = int(parts[-1], 0)
            except Exception:
                continue
            if page + lo == target_va:
                hits.append(t_addr + i)
                break
    return hits[:30]


def scan_tx_constants(data: bytes, sections) -> dict[int, list[int]]:
    """Find mov w?, #11..25 in .text (rough onTransact case table hints)."""
    if ".text" not in sections:
        return {}
    t_addr, t_off, t_size, _ = sections[".text"]
    md = Cs(CS_ARCH_ARM64, CS_MODE_ARM)
    code = data[t_off : t_off + t_size]
    found: dict[int, list[int]] = {i: [] for i in range(11, 26)}
    for ins in md.disasm(code, t_addr):
        if ins.mnemonic in ("mov", "movz") and "#" in ins.op_str:
            try:
                val = int(ins.op_str.split("#")[1].split(",")[0], 0)
            except Exception:
                continue
            if 11 <= val <= 25 and len(found[val]) < 15:
                found[val].append(ins.address)
    return {k: v for k, v in found.items() if v}


def main():
    for label, path in [("vcplax", VCPLAX), ("libvc", LIBVC)]:
        print(f"\n======== {label} ========")
        data, segs, sections = parse_elf64(path)
        for needle in [b"com.xiaomi.vlive.IMyBinderService", b"libvc.so", b"libvc++.so", b"shadowhook_hook_sym_name"]:
            off = data.find(needle)
            if off < 0:
                continue
            va = file_off_to_va(sections, off)
            print(f"\nString {needle.decode()} @ file {hex(off)} va {hex(va) if va else '?'}")
            if va:
                refs = scan_adrp_refs(data, sections, va)
                print(f"  ADRP+ADD xref candidates: {len(refs)}")
                for r in refs[:8]:
                    print(f"    fn ~ {hex(r)}")

    data, _, sections = parse_elf64(VCPLAX)
    tx = scan_tx_constants(data, sections)
    print("\n======== vcplax TX immediates (mov #11-25) sample addresses ========")
    for code, addrs in sorted(tx.items()):
        print(f"  TX{code}: {', '.join(hex(a) for a in addrs[:5])}")


if __name__ == "__main__":
    main()

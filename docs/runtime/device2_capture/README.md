# Device-2 runtime capture (`ymc53v.zip`)

Raw runtime data pulled by the in-app RE Tool (`retool`) from a live rooted device running
`com.potplayer.music`. Analysis: [`../../RUNTIME_PROTOCOL_VERIFICATION.md`](../../RUNTIME_PROTOCOL_VERIFICATION.md).

## Files

| File | What |
|------|------|
| `README.txt` | original pull manifest (timestamp, target pkg) |
| `vcplax_cmdline.txt` | daemon argv → `/data/vcplax dataloader_managerhow` (masquerade service name) |
| `vcplax_status.txt` | `/proc/<pid>/status` (uid 0, root caps, threads, VM) |
| `vcplax_pid.txt` | daemon pid |
| `vcplax_maps.txt` | full `/proc/<pid>/maps` |
| `proc/maps_interesting.txt` | filtered maps (vcplax segments) |
| `vcplax_fd.txt` | open file descriptors |
| `service_list_grep.txt` / `system/service_list_full.txt` | ServiceManager listing (shows fake `dataloader_managerhow: []`) |
| `deployed/MISSING.txt` | which `/data` payload paths were absent at pull time |
| `deployed/ls_data_dirs.txt` | `/data/local/tmp` listing (frida, hook logs, camhook frames) |
| `apk_paths.txt` | (failed) package path query |

Sibling files in `../`: `binder_tx_capture_device2.log` (full onTransact trace),
`libvc_hook_symbols_device2.txt` (decoded hook targets), `service_list_device2.txt`.

## Omitted

The pulled native binaries (`data_vcplax.real` ~12 MB, `data_libvc.so.real`, `data_libvc++.so.real`,
`proc/vcplax_exe_dump` ~12 MB) are **not committed**: they are byte-identical (sha256) to the
natives already in `testicecam2.apk` and are stripped — see the verification report §5. Regenerate
locally by extracting the archive if the raw binaries are needed.

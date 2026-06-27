// libvc.so hook handlers — synthesized from runtime pull + capstone disasm (2026-06-27)
// Binary: re-workspace/runtime-pull/libvc.so (md5=8d5d93d30498ce6a398a91e3a0cc732d)
// Ghidra image base 0x100000

/*
 * GLOBAL STATE (file VA)
 *   PTR_DAT_0023a090 @ 0x13a090  → LoadedLib / hook config copied from vcplax at init
 *   Stream registry    @ 0x13cac0  → map<streamId, FrameSlot*>
 *   Inject queue lock  @ 0x13cad8  → mutex + pending NV12/YV12 frame blobs
 *   Orig trampolines   @ 0x13cb80..0x13cba0 (saved shadowhook originals)
 *
 * XOR targets (libcameraservice.so):
 *   sym_1..3: Camera3OutputStream::returnBufferCheckedLocked (3 ABI variants)
 *   sym_4:    Camera3Device::initializeCommonLocked  (fallback trampoline)
 *   sym_5:    Camera3Device::disconnect              (cleanup + orig call)
 */

// ─── buffer_prep_helper @ file+0x77150 ─────────────────────────────────────
// Called from each returnBuffer hook BEFORE orig/replacement handler.
// If GraphicBuffer lock succeeds (w1==0, w3&1): inject decoded frame via yuv_inject.

void buffer_prep_helper(void *camera3_stream_buffer, int lockFlags, void *a2,
                        int injectFlag, int formatHint) {
    if (!camera3_stream_buffer || lockFlags != 0)
        return;

    auto *slot = (uint8_t *)camera3_stream_buffer - 0x60;
    void *lockedBuf = nullptr;
    lock_graphic_buffer(slot, &lockedBuf);  // import @ PLT 0x12f710 / 0x12f530

    if (lockedBuf && (injectFlag & 1)) {
        int w = *(int32_t *)(slot + 0x38);
        int h = *(int32_t *)(slot + 0x40);
        yuv_inject_to_locked_buffer(&lockedBuf, w, h, formatHint, 0x11);
        unlock_if_needed(lockedBuf);
    }
    release_buffer_ref(lockedBuf);
}

// ─── returnBuffer hooks @ 0x77238 / 0x772e4 / 0x77370 ─────────────────────

int hook_returnBuffer_v1(/* Camera3OutputStream* */ void *self,
                         void *streamBuffer, int64_t ts, int64_t ts2,
                         int bi, int flag, void *vec, void *fence) {
    buffer_prep_helper(streamBuffer, 0, nullptr, flag, 0);
    void *handler = *(void **)(0x13cb80);  // adrp+0xb80 from 0x13c000
    if (!handler)
        return -1;
    return ((hook_fn_v1)handler)(self, streamBuffer, ts, ts2, bi & 1, flag, vec, fence);
}

// v2/v3: same pattern, handler @ 0x13cb88 / 0x13cb90, fewer args (bool + vector)

// ─── yuv_inject_to_locked_buffer @ file+0x76908 ──────────────────────────
// Core inject: maps (width,height) → FrameSlot, copies NV12/YV12 from queue into locked GB.

void yuv_inject_to_locked_buffer(void **lockedBufPtr, int w, int h, int format, int path) {
    FrameSlot *slot = lookup_stream_by_id(&g_stream_map, w, h);  // 0x7572c
    if (!slot)
        return;

    int ready = register_frame_for_inject(&g_inject_queue, &slot->queue_node,
                                          w, h, format);           // 0x786fc
    g_inject_active = ready & 1;  // byte @ 0x13c7d8

    if (!ready)
        return;

    if (path == 0x11) {
        copy_queued_frame_to_buffer(lockedBufPtr, slot->frame_blob);  // 0x759ec
        atomic_store_release(&slot->ready, 0);
        return;
    }

    // format branches: 0x32315659 (YV12), 0x7fa30c06, NV12 0x7fa3c004, etc.
    // Reads android_ycbcr / flex layout from locked buffer, memcpy planes from slot
    dispatch_format_copy(lockedBufPtr, slot, format, w, h);
    atomic_store_release(&slot->ready, 0);
}

// ─── hook_worker @ file+0x78558 ───────────────────────────────────────────
// pthread child: poll inject queue up to 50×200ms until vcplax posts first frame.

void *hook_worker(void *unused) {
    for (int i = 0; i < 0x32; i++) {
        if (try_publish_inject_queue(&g_inject_queue))  // 0x788f8
            break;
        usleep(200000);
    }
    return NULL;
}

// ─── hook_disconnect @ file+0x77408 ───────────────────────────────────────
// Walks g_stream_map (0x13cac0), releases FrameSlot refs, calls orig disconnect.

void hook_disconnect(void *device) {
    purge_stream_map(&g_stream_map);  // CAS walk 0x77444..0x77490
    void *orig = *(void **)(0x13cba0);
    ((void (*)(void *))orig)(device);
}

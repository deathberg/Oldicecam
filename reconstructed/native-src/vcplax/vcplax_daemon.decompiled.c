/*
 * vcplax — DECOMPILED native daemon (Ghidra 11.3.1, arm64-v8a).
 * testicecam2.apk / com.potplayer.music. Deployed as /data/vcplax, run as root.
 * Binder server for "com.xiaomi.vlive.IMyBinderService" (random masquerade name).
 *
 * Ghidra imageBase = 0x100000; subtract it for file VAs (e.g. FUN_0053f8b4 = file 0x43f8b4).
 * Below: only the app-specific binder + pipeline functions (FFmpeg/MediaCodec internals
 * are statically linked upstream code and are NOT reproduced here).
 *
 * ── onTransact (FUN_0053f8b4) ── dispatch + MediaContext field offsets (ctx = *(param_1+0x38)):
 *   0x0b PLAY_SOURCE  -> FUN_0053fedc (alloc 0x14c session, pipeline_start, spawn decode thread)
 *   0x0c             -> FUN_005402b0
 *   0x0d POLL_STATE  -> FUN_00540324 (writeNoException + 5x int32 counters)
 *   0x0e SET_MODE    -> FUN_005403d4
 *   0x0f GET_STATUS  -> FUN_00541f7c(ctx,0,-1)            (reply 5 = playing)
 *   0x10 AUTO_ROTATE -> ctx+0xa8 = bool                    (reply 6)
 *   0x11 SET_LOOP    -> ctx+0x98 = bool                    (reply 7)
 *   0x12 SET_ANGLE   -> validate {0,0x5a,0xb4,0x10e}; ctx+0xac=deg, ctx+0xa8=flag (reply 8)
 *   0x13 SET_MIRROR  -> ctx+0xa8=0; ctx+0xb0 = bool         (reply 9)
 *   0x14             -> FUN_00540624
 *   0x15             -> FUN_00540908
 *   0x16 SEEK_RANGE  -> readInt64 begin,end -> FUN_00541f7c (reply 12)  [real seek handler]
 *   0x17             -> FUN_00540ad0
 *   0x18 TRANSFORM   -> int mode + 4 float + int colorMode -> ctx+0x160..0x174 (reply 14)
 *   0x19 HARD_RECOV  -> ctx+0x44 ^= 1 (reply void)
 *   0x32/0x33        -> FUN_00540c98 / FUN_00540e38
 *   else             -> BBinder::onTransact
 *   These field offsets exactly match clone/include/MediaContext.h (validated).
 *
 * ── pipeline_start (FUN_00541108) ── the demux/decode setup (FFmpeg, identified):
 *   strncasecmp(uri,"rtmp://",7) -> mode 2 else 1 (ctx+0x18)
 *   FUN_00545618 = avformat_open_input   (proof: "avformat_alloc_context" string)
 *   FUN_00547354 = avformat_find_stream_info
 *   per stream: codec_type==0 (video) -> FUN_00671bd4 avcodec_find_decoder,
 *     FUN_0094a698 avcodec_alloc_context3, FUN_007464e4 avcodec_parameters_to_context,
 *     FUN_006d5cec avcodec_open2
 *   rotate: FUN_00544020 finds AV_PKT_DATA_DISPLAYMATRIX(5) or "rotate" tag -> ctx+0x80 (deg%360)
 *
 * NOTE: the addresses queried for "tx22/tx24/tx25" in older docs were stale and resolved
 * into FFmpeg helpers (FUN_0056259c = libavformat/flvenc.c writer; FUN_00545618 =
 * avformat_open_input; FUN_00544020 = side-data/displaymatrix finder). The REAL tx22/24/25
 * handlers are the *inline* cases in onTransact above. Those FFmpeg functions are kept here
 * only as evidence of the static FFmpeg link; they are not the binder handlers.
 */

// Targeted decompilation of vcplax.so (imageBase=00100000)

// ===== onTransact (query 0x43f8b4) =====
// FUN_0053f8b4 @ 0053f8b4 size=880

void FUN_0053f8b4(long param_1,uint param_2,String16 *param_3,undefined8 param_4)

{
  long lVar1;
  byte bVar2;
  undefined4 uVar3;
  undefined4 uVar4;
  int iVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  long lVar8;
  undefined1 uVar9;
  undefined4 uVar10;
  undefined4 uVar11;
  undefined4 uVar12;
  undefined4 uVar13;
  String16 aSStack_70 [8];
  long local_68;
  
  lVar1 = tpidr_el0;
  local_68 = *(long *)(lVar1 + 0x28);
  if (param_2 < 0x32) {
    android::String16::String16(aSStack_70,"com.xiaomi.vlive.IMyBinderService");
                    /* try { // try from 0053f90c to 0053f91b has its CatchHandler @ 0053fc20 */
    android::Parcel::enforceInterface(param_3,(IPCThreadState *)aSStack_70);
    android::String16::~String16(aSStack_70);
  }
  switch(param_2) {
  case 0xb:
    uVar6 = FUN_0053fedc(param_1,param_3,param_4);
    break;
  case 0xc:
    uVar6 = FUN_005402b0(param_1,param_3,param_4);
    break;
  case 0xd:
    uVar6 = FUN_00540324(param_1,param_3,param_4);
    break;
  case 0xe:
    uVar6 = FUN_005403d4(param_1,param_3,param_4);
    break;
  case 0xf:
    FUN_00541f7c(*(undefined8 *)(param_1 + 0x38),0,0xffffffffffffffff);
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x10:
    bVar2 = android::Parcel::readBool();
    *(byte *)(*(long *)(param_1 + 0x38) + 0xa8) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x11:
    bVar2 = android::Parcel::readBool();
    *(byte *)(*(long *)(param_1 + 0x38) + 0x98) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x12:
    iVar5 = android::Parcel::readInt32();
    lVar8 = *(long *)(param_1 + 0x38);
    uVar9 = 0;
    if (iVar5 < 0xb4) {
      if ((iVar5 != 0) && (iVar5 != 0x5a)) {
LAB_0053fbc8:
        iVar5 = 0;
        uVar9 = 1;
      }
    }
    else if ((iVar5 != 0xb4) && (iVar5 != 0x10e)) goto LAB_0053fbc8;
    *(int *)(lVar8 + 0xac) = iVar5;
    *(undefined1 *)(lVar8 + 0xa8) = uVar9;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x13:
    bVar2 = android::Parcel::readBool();
    lVar8 = *(long *)(param_1 + 0x38);
    *(undefined1 *)(lVar8 + 0xa8) = 0;
    *(byte *)(lVar8 + 0xb0) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x14:
    uVar6 = FUN_00540624(param_1,param_3,param_4);
    break;
  case 0x15:
    uVar6 = FUN_00540908(param_1,param_3,param_4);
    break;
  case 0x16:
    uVar6 = android::Parcel::readInt64();
    uVar7 = android::Parcel::readInt64();
    FUN_00541f7c(*(undefined8 *)(param_1 + 0x38),uVar6,uVar7);
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x17:
    uVar6 = FUN_00540ad0(param_1,param_3,param_4);
    break;
  case 0x18:
    uVar3 = android::Parcel::readInt32();
    uVar10 = android::Parcel::readFloat();
    uVar11 = android::Parcel::readFloat();
    uVar12 = android::Parcel::readFloat();
    uVar13 = android::Parcel::readFloat();
    uVar4 = android::Parcel::readInt32();
    lVar8 = *(long *)(param_1 + 0x38);
    *(undefined4 *)(lVar8 + 0x174) = uVar4;
    *(undefined4 *)(lVar8 + 0x160) = uVar3;
    *(undefined4 *)(lVar8 + 0x164) = uVar10;
    *(undefined4 *)(lVar8 + 0x168) = uVar11;
    *(undefined4 *)(lVar8 + 0x16c) = uVar12;
    *(undefined4 *)(lVar8 + 0x170) = uVar13;
    android::Parcel::writeNoException();
LAB_0053fbe8:
    android::Parcel::writeInt32((uint)param_4);
LAB_0053fbec:
    uVar6 = 0;
    break;
  case 0x19:
    *(byte *)(param_1 + 0x44) = *(byte *)(param_1 + 0x44) ^ 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbec;
  case 0x1a:
  case 0x1b:
  case 0x1c:
  case 0x1d:
  case 0x1e:
  case 0x1f:
  case 0x20:
  case 0x21:
  case 0x22:
  case 0x23:
  case 0x24:
  case 0x25:
  case 0x26:
  case 0x27:
  case 0x28:
  case 0x29:
  case 0x2a:
  case 0x2b:
  case 0x2c:
  case 0x2d:
  case 0x2e:
  case 0x2f:
  case 0x30:
  case 0x31:
switchD_0053f944_caseD_1a:
    uVar6 = android::BBinder::onTransact
                      ((uint)param_1,(Parcel *)(ulong)param_2,(Parcel *)param_3,(uint)param_4);
    break;
  case 0x32:
    uVar6 = FUN_00540c98(param_1,param_3,param_4);
    break;
  case 0x33:
    uVar6 = FUN_00540e38(param_1,param_3,param_4);
    break;
  default:
    if (param_2 != 0x5f4e5446) goto switchD_0053f944_caseD_1a;
    goto LAB_0053fbec;
  }
  if (*(long *)(lVar1 + 0x28) != local_68) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(uVar6);
  }
  return;
}


// ===== tx14_setmode (query 0x43fa08) =====
// FUN_0053f8b4 @ 0053f8b4 size=880

void FUN_0053f8b4(long param_1,uint param_2,String16 *param_3,undefined8 param_4)

{
  long lVar1;
  byte bVar2;
  undefined4 uVar3;
  undefined4 uVar4;
  int iVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  long lVar8;
  undefined1 uVar9;
  undefined4 uVar10;
  undefined4 uVar11;
  undefined4 uVar12;
  undefined4 uVar13;
  String16 aSStack_70 [8];
  long local_68;
  
  lVar1 = tpidr_el0;
  local_68 = *(long *)(lVar1 + 0x28);
  if (param_2 < 0x32) {
    android::String16::String16(aSStack_70,"com.xiaomi.vlive.IMyBinderService");
                    /* try { // try from 0053f90c to 0053f91b has its CatchHandler @ 0053fc20 */
    android::Parcel::enforceInterface(param_3,(IPCThreadState *)aSStack_70);
    android::String16::~String16(aSStack_70);
  }
  switch(param_2) {
  case 0xb:
    uVar6 = FUN_0053fedc(param_1,param_3,param_4);
    break;
  case 0xc:
    uVar6 = FUN_005402b0(param_1,param_3,param_4);
    break;
  case 0xd:
    uVar6 = FUN_00540324(param_1,param_3,param_4);
    break;
  case 0xe:
    uVar6 = FUN_005403d4(param_1,param_3,param_4);
    break;
  case 0xf:
    FUN_00541f7c(*(undefined8 *)(param_1 + 0x38),0,0xffffffffffffffff);
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x10:
    bVar2 = android::Parcel::readBool();
    *(byte *)(*(long *)(param_1 + 0x38) + 0xa8) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x11:
    bVar2 = android::Parcel::readBool();
    *(byte *)(*(long *)(param_1 + 0x38) + 0x98) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x12:
    iVar5 = android::Parcel::readInt32();
    lVar8 = *(long *)(param_1 + 0x38);
    uVar9 = 0;
    if (iVar5 < 0xb4) {
      if ((iVar5 != 0) && (iVar5 != 0x5a)) {
LAB_0053fbc8:
        iVar5 = 0;
        uVar9 = 1;
      }
    }
    else if ((iVar5 != 0xb4) && (iVar5 != 0x10e)) goto LAB_0053fbc8;
    *(int *)(lVar8 + 0xac) = iVar5;
    *(undefined1 *)(lVar8 + 0xa8) = uVar9;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x13:
    bVar2 = android::Parcel::readBool();
    lVar8 = *(long *)(param_1 + 0x38);
    *(undefined1 *)(lVar8 + 0xa8) = 0;
    *(byte *)(lVar8 + 0xb0) = bVar2 & 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x14:
    uVar6 = FUN_00540624(param_1,param_3,param_4);
    break;
  case 0x15:
    uVar6 = FUN_00540908(param_1,param_3,param_4);
    break;
  case 0x16:
    uVar6 = android::Parcel::readInt64();
    uVar7 = android::Parcel::readInt64();
    FUN_00541f7c(*(undefined8 *)(param_1 + 0x38),uVar6,uVar7);
    android::Parcel::writeNoException();
    goto LAB_0053fbe8;
  case 0x17:
    uVar6 = FUN_00540ad0(param_1,param_3,param_4);
    break;
  case 0x18:
    uVar3 = android::Parcel::readInt32();
    uVar10 = android::Parcel::readFloat();
    uVar11 = android::Parcel::readFloat();
    uVar12 = android::Parcel::readFloat();
    uVar13 = android::Parcel::readFloat();
    uVar4 = android::Parcel::readInt32();
    lVar8 = *(long *)(param_1 + 0x38);
    *(undefined4 *)(lVar8 + 0x174) = uVar4;
    *(undefined4 *)(lVar8 + 0x160) = uVar3;
    *(undefined4 *)(lVar8 + 0x164) = uVar10;
    *(undefined4 *)(lVar8 + 0x168) = uVar11;
    *(undefined4 *)(lVar8 + 0x16c) = uVar12;
    *(undefined4 *)(lVar8 + 0x170) = uVar13;
    android::Parcel::writeNoException();
LAB_0053fbe8:
    android::Parcel::writeInt32((uint)param_4);
LAB_0053fbec:
    uVar6 = 0;
    break;
  case 0x19:
    *(byte *)(param_1 + 0x44) = *(byte *)(param_1 + 0x44) ^ 1;
    android::Parcel::writeNoException();
    goto LAB_0053fbec;
  case 0x1a:
  case 0x1b:
  case 0x1c:
  case 0x1d:
  case 0x1e:
  case 0x1f:
  case 0x20:
  case 0x21:
  case 0x22:
  case 0x23:
  case 0x24:
  case 0x25:
  case 0x26:
  case 0x27:
  case 0x28:
  case 0x29:
  case 0x2a:
  case 0x2b:
  case 0x2c:
  case 0x2d:
  case 0x2e:
  case 0x2f:
  case 0x30:
  case 0x31:
switchD_0053f944_caseD_1a:
    uVar6 = android::BBinder::onTransact
                      ((uint)param_1,(Parcel *)(ulong)param_2,(Parcel *)param_3,(uint)param_4);
    break;
  case 0x32:
    uVar6 = FUN_00540c98(param_1,param_3,param_4);
    break;
  case 0x33:
    uVar6 = FUN_00540e38(param_1,param_3,param_4);
    break;
  default:
    if (param_2 != 0x5f4e5446) goto switchD_0053f944_caseD_1a;
    goto LAB_0053fbec;
  }
  if (*(long *)(lVar1 + 0x28) != local_68) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(uVar6);
  }
  return;
}


// ===== tx11_play (query 0x43fedc) =====
// FUN_0053fedc @ 0053fedc size=868

undefined8 FUN_0053fedc(long param_1,undefined8 param_2,int param_3)

{
  byte *pbVar1;
  char *pcVar2;
  char *pcVar3;
  long lVar4;
  undefined *puVar5;
  byte bVar6;
  byte bVar7;
  ulong uVar8;
  undefined8 *puVar9;
  size_t __n;
  long lVar10;
  void *__dest;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  ulong local_90;
  size_t local_88;
  void *local_80;
  String16 aSStack_78 [8];
  long local_70;
  long local_68;
  
  lVar4 = tpidr_el0;
  local_68 = *(long *)(lVar4 + 0x28);
  android::Parcel::writeNoException();
  uVar8 = FUN_0053ec64(&DAT_00c79c40,&DAT_00c79c10);
  if (((uVar8 & 1) == 0) || (uVar8 = FUN_0053ec64(&DAT_00c79c58,&DAT_00c79c28), (uVar8 & 1) == 0)) {
    android::Parcel::writeInt32(param_3);
  }
  else {
    puVar9 = (undefined8 *)operator_new(0x14c);
    uVar8 = (ulong)(*(byte *)(param_1 + 0x58) >> 1);
    lVar10 = param_1 + 0x59;
    if ((*(byte *)(param_1 + 0x58) & 1) != 0) {
      uVar8 = *(ulong *)(param_1 + 0x60);
      lVar10 = *(long *)(param_1 + 0x68);
    }
    puVar9[1] = 0;
    *puVar9 = 0;
    puVar9[3] = 0;
    puVar9[2] = 0;
    puVar9[5] = 0;
    puVar9[4] = 0;
    puVar9[7] = 0;
    puVar9[6] = 0;
    puVar9[9] = 0;
    puVar9[8] = 0;
    puVar9[0xb] = 0;
    puVar9[10] = 0;
    puVar9[0xd] = 0;
    puVar9[0xc] = 0;
    puVar9[0xf] = 0;
    puVar9[0xe] = 0;
    puVar9[0x11] = 0;
    puVar9[0x10] = 0;
    puVar9[0x13] = 0;
    puVar9[0x12] = 0;
    puVar9[0x15] = 0;
    puVar9[0x14] = 0;
    puVar9[0x17] = 0;
    puVar9[0x16] = 0;
    puVar9[0x19] = 0;
    puVar9[0x18] = 0;
    puVar9[0x1b] = 0;
    puVar9[0x1a] = 0;
    puVar9[0x1d] = 0;
    puVar9[0x1c] = 0;
    puVar9[0x1f] = 0;
    puVar9[0x1e] = 0;
    puVar9[0x21] = 0;
    puVar9[0x20] = 0;
    puVar9[0x23] = 0;
    puVar9[0x22] = 0;
    puVar9[0x25] = 0;
    puVar9[0x24] = 0;
    puVar9[0x27] = 0;
    puVar9[0x26] = 0;
    *(undefined8 *)((long)puVar9 + 0x144) = 0;
    *(undefined8 *)((long)puVar9 + 0x13c) = 0;
    __memcpy_chk(puVar9,lVar10,uVar8,0x14c);
    bVar7 = (byte)DAT_00c79c58;
    puVar5 = PTR_DAT_00c729c8;
    *(undefined4 *)(puVar9 + 0x29) = 0;
    uVar11 = *(undefined8 *)(puVar5 + 0x40);
    uVar13 = *(undefined8 *)(puVar5 + 0x58);
    uVar12 = *(undefined8 *)(puVar5 + 0x50);
    puVar9[0x22] = *(undefined8 *)(puVar5 + 0x48);
    puVar9[0x21] = uVar11;
    puVar9[0x24] = uVar13;
    puVar9[0x23] = uVar12;
    uVar11 = *(undefined8 *)(puVar5 + 0x60);
    uVar13 = *(undefined8 *)(puVar5 + 0x78);
    uVar12 = *(undefined8 *)(puVar5 + 0x70);
    puVar9[0x26] = *(undefined8 *)(puVar5 + 0x68);
    puVar9[0x25] = uVar11;
    puVar9[0x28] = uVar13;
    puVar9[0x27] = uVar12;
    bVar6 = (byte)DAT_00c79c40;
    uVar13 = *(undefined8 *)puVar5;
    uVar12 = *(undefined8 *)(puVar5 + 0x18);
    uVar11 = *(undefined8 *)(puVar5 + 0x10);
    puVar9[0x1a] = *(undefined8 *)(puVar5 + 8);
    puVar9[0x19] = uVar13;
    pcVar3 = DAT_00c79c68;
    puVar9[0x1c] = uVar12;
    puVar9[0x1b] = uVar11;
    uVar11 = *(undefined8 *)(puVar5 + 0x20);
    uVar13 = *(undefined8 *)(puVar5 + 0x38);
    uVar12 = *(undefined8 *)(puVar5 + 0x30);
    pcVar2 = DAT_00c79c50;
    if ((bVar6 & 1) == 0) {
      pcVar2 = (char *)((long)&DAT_00c79c40 + 1);
    }
    puVar9[0x1e] = *(undefined8 *)(puVar5 + 0x28);
    puVar9[0x1d] = uVar11;
    puVar9[0x20] = uVar13;
    puVar9[0x1f] = uVar12;
    if ((bVar7 & 1) == 0) {
      pcVar3 = (char *)((long)&DAT_00c79c58 + 1);
    }
                    /* try { // try from 00540010 to 0054001b has its CatchHandler @ 00540274 */
    FUN_005439c8(puVar9,0x14c,pcVar2,pcVar3);
    pcVar2 = DAT_00c79c50;
    if (((byte)DAT_00c79c40 & 1) == 0) {
      pcVar2 = (char *)((long)&DAT_00c79c40 + 1);
    }
    unlink(pcVar2);
    pcVar2 = DAT_00c79c68;
    if (((byte)DAT_00c79c58 & 1) == 0) {
      pcVar2 = (char *)((long)&DAT_00c79c58 + 1);
    }
    unlink(pcVar2);
                    /* try { // try from 00540044 to 0054004f has its CatchHandler @ 0054026c */
    android::Parcel::readString16();
                    /* try { // try from 00540050 to 0054005b has its CatchHandler @ 00540268 */
    android::String8::String8((String8 *)&local_90,aSStack_78);
                    /* try { // try from 00540060 to 00540067 has its CatchHandler @ 00540258 */
    FUN_00b8e2bc(param_1 + 0x20,local_90);
    android::String8::~String8((String8 *)&local_90);
                    /* try { // try from 00540070 to 00540077 has its CatchHandler @ 00540254 */
    bVar6 = android::Parcel::readBool();
                    /* try { // try from 00540078 to 005400bf has its CatchHandler @ 00540280 */
    bVar7 = android::Parcel::readBool();
    lVar10 = *(long *)(param_1 + 0x38);
    *(undefined4 *)(lVar10 + 0x28) = 0;
    FUN_00b8f1c8(lVar10 + 0xb4);
    *(undefined1 *)(lVar10 + 0x10c) = 0;
    FUN_00b8f1f4(lVar10 + 0xb4);
    FUN_00b8f13c(lVar10 + 0xdc);
    if (*(long *)(lVar10 + 0x20) != 0) {
      FUN_00b8f234((long *)(lVar10 + 0x20));
    }
    lVar10 = *(long *)(param_1 + 0x38);
    *(byte *)(lVar10 + 0xa8) = bVar6 & 1;
    *(byte *)(lVar10 + 0x98) = bVar7 & 1;
    pcVar2 = (char *)(param_1 + 0x21);
    if ((*(byte *)(param_1 + 0x20) & 1) != 0) {
      pcVar2 = *(char **)(param_1 + 0x30);
    }
    __n = strlen(pcVar2);
    if (0xffffffffffffffef < __n) {
      if (*(long *)(lVar4 + 0x28) == local_68) {
                    /* try { // try from 00540230 to 00540237 has its CatchHandler @ 0054027c */
                    /* WARNING: Subroutine does not return */
        FUN_00542b2c(&local_90);
      }
      goto LAB_005402ac;
    }
    if (__n < 0x17) {
      __dest = (void *)((ulong)&local_90 | 1);
      local_90 = CONCAT71(local_90._1_7_,(char)((int)__n << 1));
      if (__n != 0) goto LAB_0054014c;
    }
    else {
      uVar8 = (__n | 0xf) + 1;
                    /* try { // try from 00540134 to 0054013b has its CatchHandler @ 0054027c */
      __dest = operator_new(uVar8);
      local_90 = uVar8 | 1;
      local_88 = __n;
      local_80 = __dest;
LAB_0054014c:
      memmove(__dest,pcVar2,__n);
    }
    *(undefined1 *)((long)__dest + __n) = 0;
                    /* try { // try from 00540160 to 0054016b has its CatchHandler @ 0054023c */
    FUN_00541108(lVar10,&local_90);
    if ((local_90 & 1) != 0) {
      operator_delete(local_80);
    }
    lVar10 = *(long *)(param_1 + 0x38);
    if (((*(long *)(lVar10 + 0x30) != 0) && (*(long *)(lVar10 + 0x48) != 0)) &&
       (pbVar1 = (byte *)(lVar10 + 0x28), (*pbVar1 & 1) == 0)) {
      pbVar1[0] = 1;
      pbVar1[1] = 0;
      pbVar1[2] = 0;
      pbVar1[3] = 0;
                    /* try { // try from 005401a8 to 005401db has its CatchHandler @ 00540280 */
      local_70 = lVar10;
      FUN_0054137c(&local_90,&local_70);
      uVar8 = local_90;
      if (*(long *)(lVar10 + 0x20) != 0) {
                    /* WARNING: Subroutine does not return */
        FUN_00bb1f04();
      }
      local_90 = 0;
      *(ulong *)(lVar10 + 0x20) = uVar8;
      FUN_00b8f214(&local_90);
    }
    android::Parcel::writeInt32(param_3);
    android::String16::~String16(aSStack_78);
    operator_delete(puVar9);
  }
  if (*(long *)(lVar4 + 0x28) == local_68) {
    return 0;
  }
LAB_005402ac:
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}


// ===== tx24_transform (query 0x444024) =====
// FUN_00544020 @ 00544020 size=104

undefined8 FUN_00544020(long param_1,int param_2,undefined8 *param_3)

{
  long lVar1;
  long lVar2;
  long lVar3;
  long lVar4;
  
  lVar3 = 0;
  do {
    lVar1 = lVar3 + 0x18;
    if ((ulong)(*(uint *)(param_1 + 0xd0) & ((int)*(uint *)(param_1 + 0xd0) >> 0x1f ^ 0xffffffffU))
        * 0x18 + 0x18 == lVar1) {
      if (param_3 != (undefined8 *)0x0) {
        *param_3 = 0;
      }
      return 0;
    }
    lVar4 = *(long *)(param_1 + 200);
    lVar2 = lVar4 + lVar3;
    lVar3 = lVar1;
  } while (*(int *)(lVar2 + 0x10) != param_2);
  if (param_3 != (undefined8 *)0x0) {
    *param_3 = *(undefined8 *)(lVar4 + lVar1 + -0x10);
  }
  return *(undefined8 *)(lVar4 + lVar1 + -0x18);
}


// ===== tx22_seek (query 0x46279c) =====
// FUN_0056259c @ 0056259c size=3092

int FUN_0056259c(long param_1,long param_2)

{
  long lVar1;
  undefined4 uVar2;
  size_t sVar3;
  bool bVar4;
  int iVar5;
  uint uVar6;
  int iVar7;
  long lVar8;
  void *pvVar9;
  long *plVar10;
  long lVar11;
  undefined8 uVar12;
  uint uVar13;
  undefined *puVar14;
  undefined8 uVar15;
  uint extraout_w8;
  uint extraout_w8_00;
  uint extraout_w8_01;
  uint extraout_w8_02;
  uint uVar16;
  int extraout_w8_03;
  long lVar17;
  uint *puVar18;
  undefined8 uVar19;
  ulong uVar20;
  long lVar21;
  int iVar22;
  undefined4 *puVar23;
  int iVar24;
  double dVar25;
  undefined1 auVar26 [16];
  size_t local_80;
  void *local_78;
  int local_64;
  
  local_64 = *(int *)(param_2 + 0x20);
  lVar1 = *(long *)(param_1 + 0x18);
  uVar15 = *(undefined8 *)(param_1 + 0x20);
  local_78 = (void *)0x0;
  puVar18 = *(uint **)(*(long *)(*(long *)(param_1 + 0x30) + (long)*(int *)(param_2 + 0x24) * 8) +
                      0x10);
  uVar13 = 0x20;
  if ((*(uint *)(param_2 + 0x28) & 1) != 0) {
    uVar13 = 0x10;
  }
  lVar8 = FUN_0054b12c(uVar15,0,1);
  if ((*puVar18 != 1) || (*(int *)(param_2 + 0x20) != 0)) {
    uVar6 = puVar18[1];
    iVar7 = 2;
    if ((0xf < uVar6 - 0x5b) || ((1 << (ulong)(uVar6 - 0x5b & 0x1f) & 0x8003U) == 0)) {
      bVar4 = uVar6 - 0xa7 == 0x3a;
      if (((uVar6 - 0xa7 < 0x3b) && (FUN_005646b0(), uVar6 = extraout_w8, !bVar4)) ||
         (bVar4 = uVar6 == 0xc, bVar4)) {
LAB_00562664:
        iVar7 = 5;
      }
      else {
        FUN_00564638();
        uVar6 = extraout_w8_00;
        if (!bVar4) {
          if (extraout_w8_00 == 0x1b) goto LAB_00562664;
          iVar7 = 1;
        }
      }
    }
    if (((uVar6 == 0xc || uVar6 == 0x1b) || (uVar6 == 0xa7 || uVar6 == 0x15002)) || (uVar6 == 0xe1))
    {
LAB_005626d8:
      pvVar9 = (void *)FUN_009589d4(param_2,1,&local_80);
      sVar3 = local_80;
      iVar5 = 0;
      if ((pvVar9 != (void *)0x0) && (local_80 != 0)) {
        if ((local_80 == (long)(int)puVar18[6]) &&
           (iVar5 = memcmp(pvVar9,*(void **)(puVar18 + 4),local_80), iVar5 == 0)) {
          iVar5 = 0;
        }
        else {
          iVar5 = FUN_0065e194(puVar18,sVar3 & 0xffffffff);
          if (iVar5 < 0) {
            return iVar5;
          }
          memcpy(*(void **)(puVar18 + 4),pvVar9,local_80);
          FUN_00563e4c(param_1,puVar18,*(undefined8 *)(param_2 + 0x10));
        }
      }
      lVar17 = *(long *)(param_1 + 0x18);
      if (((*(int *)(lVar17 + 0x120) == 0) && (puVar18[1] - 0xa7 < 0x3b)) &&
         ((1L << ((ulong)(puVar18[1] - 0xa7) & 0x3f) & 0x400000000000041U) != 0)) {
        uVar2 = *(undefined4 *)(param_2 + 0x10);
        uVar19 = *(undefined8 *)(param_1 + 0x20);
        plVar10 = (long *)FUN_009591cc(*(undefined8 *)(puVar18 + 8),puVar18[10],0x16);
        if (plVar10 == (long *)0x0) {
          puVar23 = (undefined4 *)0x0;
        }
        else {
          puVar23 = (undefined4 *)*plVar10;
        }
        plVar10 = (long *)FUN_009591cc(*(undefined8 *)(puVar18 + 8),puVar18[10],0x14);
        if (plVar10 == (long *)0x0) {
          lVar21 = 0;
        }
        else {
          lVar21 = *plVar10;
        }
        FUN_005645d0(uVar19);
        lVar11 = FUN_00564580(uVar19);
        FUN_0054b8c0(uVar19,5);
        FUN_00564184(uVar19,uVar2);
        FUN_0054b8c0(uVar19,*(undefined4 *)(lVar17 + 8));
        uVar6 = puVar18[1];
        if ((uVar6 == 0xa7) || (uVar6 == 0xe1)) {
          FUN_0056477c();
          puVar14 = &DAT_0022d161;
          if (puVar18[1] != 0xe1) {
            puVar14 = &DAT_0021bcd8;
          }
LAB_00562868:
          FUN_005646f8(uVar19,puVar14);
        }
        else if (uVar6 == 0xad) {
          FUN_0056477c();
          puVar14 = &DAT_00231a10;
          goto LAB_00562868;
        }
        uVar12 = FUN_005645f8(uVar19);
        FUN_005645a8(uVar12,"colorInfo");
        uVar12 = FUN_0056462c();
        FUN_005645a8(uVar12,"colorConfig");
        uVar12 = FUN_0056462c();
        if (puVar18[0x1b] != 2 && puVar18[0x1b] < 0x13) {
          FUN_005645a8(uVar12,"transferCharacteristics");
          uVar12 = FUN_005645ec(puVar18[0x1b]);
        }
        if (puVar18[0x1c] != 2 && puVar18[0x1c] < 0x12) {
          FUN_005645a8(uVar12,"matrixCoefficients");
          uVar12 = FUN_005645ec(puVar18[0x1c]);
        }
        if (puVar18[0x1a] != 2 && puVar18[0x1a] < 0x17) {
          FUN_005645a8(uVar12,"colorPrimaries");
          uVar12 = FUN_005645ec(puVar18[0x1a]);
        }
        FUN_005645a8(uVar12,&DAT_001ca98d);
        uVar12 = FUN_005645d0(uVar19);
        if (puVar23 != (undefined4 *)0x0) {
          FUN_005645a8(uVar12,"hdrCll");
          uVar12 = FUN_0056462c();
          FUN_005645a8(uVar12,"maxFall");
          uVar12 = FUN_005645ec(puVar23[1]);
          FUN_005645a8(uVar12,"maxCLL");
          uVar12 = FUN_005645ec(*puVar23);
          FUN_005645a8(uVar12,&DAT_001ca98d);
          uVar12 = FUN_005645d0(uVar19);
        }
        if ((lVar21 != 0) && ((*(int *)(lVar21 + 0x50) != 0 || (*(int *)(lVar21 + 0x54) != 0)))) {
          FUN_005645a8(uVar12,"hdrMdcv");
          uVar12 = FUN_0056462c();
          if (*(int *)(lVar21 + 0x50) != 0) {
            FUN_005645a8(uVar12,&DAT_002290d5);
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 4));
            FUN_005645a8(uVar12,&DAT_002290da);
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0xc));
            FUN_005645a8(uVar12,"greenX");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x14));
            FUN_005645a8(uVar12,"greenY");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x1c));
            FUN_005645a8(uVar12,"blueX");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x24));
            FUN_005645a8(uVar12,"blueY");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x2c));
            FUN_005645a8(uVar12,"whitePointX");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x34));
            FUN_005645a8(uVar12,"whitePointY");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x3c));
          }
          if (*(int *)(lVar21 + 0x54) != 0) {
            FUN_005645a8(uVar12,"maxLuminance");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x4c));
            FUN_005645a8(uVar12,"minLuminance");
            uVar12 = FUN_0056458c(*(undefined4 *)(lVar21 + 0x44));
          }
          FUN_005645a8(uVar12,&DAT_001ca98d);
          uVar12 = FUN_005645d0(uVar19);
        }
        FUN_005645a8(uVar12,&DAT_001ca98d);
        FUN_005645d0(uVar19);
        lVar21 = FUN_0054b12c(uVar19,0,1);
        FUN_0054b12c(uVar19,lVar11,0);
        iVar22 = (int)(lVar21 - lVar11);
        FUN_0054b8c0(uVar19,iVar22 + -10);
        FUN_0054b51c(uVar19,(lVar21 - lVar11) + -3);
        FUN_0054b608(uVar19,iVar22 + 1);
        *(undefined4 *)(lVar17 + 0x120) = 1;
      }
    }
    else {
      if (uVar6 == 0xad) {
        if (*(long *)(param_2 + 8) != *(long *)(param_2 + 0x10)) {
          iVar7 = iVar7 + 3;
        }
        goto LAB_005626d8;
      }
      iVar5 = 0;
    }
    lVar17 = *(long *)(lVar1 + 0x28);
    uVar20 = *(ulong *)(param_2 + 0x10);
    if (lVar17 == -0x8000000000000000) {
      lVar17 = -uVar20;
      *(long *)(lVar1 + 0x28) = lVar17;
    }
    if ((long)(uVar20 + lVar17) < 0 == SBORROW8(uVar20,-lVar17)) {
      uVar6 = puVar18[1];
      bVar4 = uVar6 - 0xa7 == 0x3a;
      if ((((uVar6 - 0xa7 < 0x3b) && (FUN_005646b0(), uVar6 = extraout_w8_01, !bVar4)) ||
          (uVar6 == 0x1b || uVar6 == 0xc)) && (*(long *)(param_2 + 8) == -0x8000000000000000)) {
        uVar15 = 0x10;
        goto LAB_00562b60;
      }
      if ((*(byte *)(param_1 + 0x114) & 1) != 0) {
        FUN_005637fc(param_1,uVar20 & 0xffffffff);
        *(uint *)(param_1 + 0x114) = *(uint *)(param_1 + 0x114) & 0xfffffffe;
      }
      uVar20 = uVar20 & 0xffffffff;
      uVar19 = FUN_00b4cb18(uVar20,1000000,1000);
      if (((*(byte *)(param_2 + 0x28) & 1) == 0) ||
         ((*(long *)(lVar1 + 0xe0) != 0 && (*puVar18 != 0)))) {
        uVar12 = 2;
      }
      else {
        uVar12 = 1;
      }
      FUN_0054b8e8(uVar15,uVar19,uVar12);
      uVar6 = *puVar18;
      if (uVar6 - 2 < 2) {
        auVar26 = FUN_0054ae3c(uVar15,0x12);
        uVar6 = 0xffffffff;
      }
      else if (uVar6 == 1) {
        uVar6 = FUN_0056424c(param_1,puVar18);
        if (local_64 == 0) {
          FUN_00b4c0a0(0,0,"Assertion %s failed at %s:%d\n",&DAT_002290d0,"libavformat/flvenc.c",
                       0x429);
                    /* WARNING: Subroutine does not return */
          abort();
        }
        auVar26 = FUN_005646c8(uVar15);
      }
      else {
        if (uVar6 != 0) {
          return -0x16;
        }
        FUN_005645d0(uVar15);
        auVar26 = FUN_0065df40(&DAT_00239d34,puVar18[1]);
        uVar6 = auVar26._0_4_ | uVar13;
      }
      bVar4 = puVar18[1] == 0xc;
      if (bVar4) {
LAB_00562c94:
        if ((0 < (int)puVar18[6]) && (**(char **)(puVar18 + 4) != '\x01')) {
          auVar26 = FUN_0060532c(*(undefined8 *)(param_2 + 0x18),&local_78,&local_64);
LAB_00562d40:
          iVar5 = auVar26._0_4_;
          if (iVar5 < 0) {
            return iVar5;
          }
        }
LAB_00562d48:
        uVar16 = puVar18[1];
LAB_00562d4c:
        if ((uVar16 == 0x15023) &&
           (0xa0 < (long)(uVar20 - *(long *)(lVar1 + (long)*(int *)(param_2 + 0x24) * 8 + 0x100))))
        {
          FUN_005645e0(auVar26._0_8_,auVar26._8_8_,
                       "Warning: Speex stream has more than 8 frames per packet. Adobe Flash Player cannot handle this!\n"
                      );
        }
      }
      else {
        auVar26 = FUN_00564638();
        if (bVar4) {
          if (2 < *(int *)(param_2 + 0x20)) {
            if (0xffe < (((uint)(**(ushort **)(param_2 + 0x18) >> 8) |
                         (**(ushort **)(param_2 + 0x18) & 0xff00ff) << 8) << 0x10) >> 0x14) {
              if (*(long *)(*(long *)(*(long *)(param_1 + 0x30) + (long)*(int *)(param_2 + 0x24) * 8
                                     ) + 0x38) == 0) {
                FUN_00564674(auVar26._0_8_,auVar26._8_8_,
                             "Malformed AAC bitstream detected: use the audio bitstream filter \'aac_adtstoasc\' to fix it (\'-bsf:a aac_adtstoasc\' option with ffmpeg)\n"
                            );
                return -0x41444e49;
              }
              auVar26 = FUN_005645e0(auVar26._0_8_,auVar26._8_8_,"aac bitstream error\n");
            }
            goto LAB_00562d48;
          }
        }
        else {
          if (extraout_w8_02 != 0xad) {
            uVar16 = extraout_w8_02;
            if (extraout_w8_02 == 0x1b) goto LAB_00562c94;
            goto LAB_00562d4c;
          }
          if (0 < (int)puVar18[6]) {
            if (**(char **)(puVar18 + 4) == '\x01') goto LAB_00562d48;
            auVar26 = FUN_0058e7b4(*(undefined8 *)(param_2 + 0x18),&local_78,&local_64,0,0);
            goto LAB_00562d40;
          }
        }
      }
      lVar17 = lVar1 + (long)*(int *)(param_2 + 0x24) * 8;
      if (*(long *)(lVar17 + 0x100) < (long)uVar20) {
        *(ulong *)(lVar17 + 0x100) = uVar20;
      }
      iVar22 = local_64 + iVar7;
      if (0xffffff < iVar22) {
        FUN_00b4c0a0(param_1,0x10,"Too large packet with size %u >= %u\n",iVar22,0x1000000);
        iVar5 = -0x16;
        goto LAB_00563178;
      }
      FUN_0054b8c0(uVar15,iVar22);
      FUN_00564184(uVar15,uVar20);
      FUN_0054b8c0(uVar15,*(undefined4 *)(lVar1 + 8));
      if ((*puVar18 & 0xfffffffe) == 2) {
        lVar17 = FUN_00564580(uVar15);
        if (puVar18[1] == 0x17002) {
          uVar19 = FUN_005645f8(uVar15);
          FUN_00564644(uVar19,"onTextData");
          FUN_005646c8(uVar15);
          uVar19 = FUN_0054b608(uVar15,2);
          FUN_00564644(uVar19,&DAT_00217868);
          uVar19 = FUN_005645f8(uVar15);
          uVar19 = FUN_00564644(uVar19,&DAT_0020f4d1);
          FUN_00564644(uVar19,&DAT_001e08fb);
          uVar19 = FUN_005645f8(uVar15);
          uVar19 = FUN_00564644(uVar19,*(undefined8 *)(param_2 + 0x18));
          FUN_00564644(uVar19,&DAT_001ca98d);
          FUN_005645d0(uVar15);
        }
        else {
          pvVar9 = local_78;
          if (local_78 == (void *)0x0) {
            pvVar9 = *(void **)(param_2 + 0x18);
          }
          FUN_00564720(lVar17,pvVar9);
        }
        iVar7 = FUN_00564580(uVar15);
        iVar7 = iVar7 - (int)lVar17;
        FUN_005645c8(uVar15,lVar17 + -10);
        FUN_0054b8c0(uVar15,iVar7);
        FUN_0054b12c(uVar15,(long)(iVar7 + 7),1);
        auVar26 = FUN_0054b608(uVar15,iVar7 + 0xb);
      }
      else {
        uVar16 = puVar18[1];
        if ((uVar16 == 0xa7) || (uVar16 == 0xe1)) {
          FUN_0054ae3c(uVar15,uVar13 | 0x81);
          puVar14 = &DAT_0022d161;
          if (puVar18[1] != 0xe1) {
            puVar14 = &DAT_0021bcd8;
          }
          FUN_0054af58(uVar15,puVar14,4);
        }
        else if (uVar16 == 0xad) {
          lVar17 = *(long *)(param_2 + 8);
          lVar21 = *(long *)(param_2 + 0x10);
          uVar6 = 0x83;
          if (lVar17 != lVar21) {
            uVar6 = 0x81;
          }
          FUN_0054ae3c(uVar15,uVar6 | uVar13);
          FUN_005646f8(uVar15,&DAT_00231a10);
          if (lVar17 != lVar21) {
            FUN_00564658();
          }
        }
        else {
          FUN_0054ae3c(uVar15,uVar6);
        }
        uVar13 = puVar18[1];
        if (uVar13 == 0x5b) {
          FUN_00564738(uVar15);
          uVar13 = puVar18[1];
        }
        if (uVar13 == 0xc || uVar13 == 0x1b) {
          FUN_0054ae3c(uVar15,1);
          uVar19 = FUN_00564658();
        }
        else {
          bVar4 = uVar13 == 0x5c;
          if (bVar4) {
LAB_00562fd8:
            if (puVar18[6] == 0) {
              iVar22 = (int)*(undefined8 *)(puVar18 + 0x12);
              iVar24 = (int)((ulong)*(undefined8 *)(puVar18 + 0x12) >> 0x20);
              uVar20 = CONCAT44(iVar24 + 0xf,iVar22 + 0xf) & DAT_00237680;
              uVar13 = (int)(uVar20 >> 0x20) - iVar24 | ((int)uVar20 - iVar22) * 0x10;
            }
            else {
              uVar13 = (uint)**(byte **)(puVar18 + 4);
            }
          }
          else {
            uVar19 = FUN_00564638();
            if (!bVar4) {
              if (extraout_w8_03 != 0x6a) goto LAB_0056303c;
              goto LAB_00562fd8;
            }
            uVar13 = 1;
          }
          uVar19 = FUN_0054ae3c(uVar15,uVar13);
        }
LAB_0056303c:
        pvVar9 = local_78;
        if (local_78 == (void *)0x0) {
          pvVar9 = *(void **)(param_2 + 0x18);
        }
        FUN_00564720(uVar19,pvVar9);
        auVar26 = FUN_0054b608(uVar15,iVar7 + local_64 + 0xb);
        lVar17 = *(long *)(lVar1 + 0x28) + *(long *)(param_2 + 8) + *(long *)(param_2 + 0x40);
        lVar21 = *(long *)(lVar1 + 0x20);
        if (*(long *)(lVar1 + 0x20) <= lVar17) {
          lVar21 = lVar17;
        }
        *(long *)(lVar1 + 0x20) = lVar21;
      }
      if ((*(byte *)(lVar1 + 0xf8) >> 2 & 1) != 0) {
        if (*puVar18 == 1) {
          lVar17 = FUN_00564580(uVar15);
          *(long *)(lVar1 + 0x60) = (lVar17 - lVar8) + *(long *)(lVar1 + 0x60);
        }
        else if (*puVar18 == 0) {
          lVar17 = FUN_00564580(uVar15);
          *(long *)(lVar1 + 0x50) = (lVar17 - lVar8) + *(long *)(lVar1 + 0x50);
          dVar25 = (double)*(long *)(param_2 + 0x10) / DAT_00237888;
          *(double *)(lVar1 + 0x90) = dVar25;
          if ((*(byte *)(param_2 + 0x28) & 1) != 0) {
            *(double *)(lVar1 + 0xa0) = dVar25;
            *(long *)(lVar1 + 0xb0) = lVar8;
            auVar26 = FUN_00b4d270(0x18);
            plVar10 = auVar26._0_8_;
            if (plVar10 == (long *)0x0) {
              FUN_005645e0(0,auVar26._8_8_,"no mem for add keyframe index!\n");
              iVar5 = -0xc;
            }
            else {
              lVar17 = *(long *)(lVar1 + 0xc0);
              plVar10[1] = (long)dVar25;
              *plVar10 = lVar8;
              if (lVar17 == 0) {
                *(long **)(lVar1 + 0xd0) = plVar10;
                plVar10[2] = 0;
              }
              else {
                lVar8 = *(long *)(lVar1 + 200);
                *(long **)(lVar8 + 0x10) = plVar10;
                plVar10[2] = 0;
                plVar10 = *(long **)(lVar8 + 0x10);
              }
              iVar5 = 0;
              *(long *)(lVar1 + 0xc0) = lVar17 + 1;
              *(long **)(lVar1 + 200) = plVar10;
            }
          }
        }
        else {
          FUN_00564764(auVar26._0_8_,auVar26._8_8_,"par->codec_type is type = [%d]\n");
          FUN_00b4c0a0();
        }
      }
LAB_00563178:
      free(local_78);
      return iVar5;
    }
  }
  auVar26 = FUN_00564764();
  uVar15 = auVar26._8_8_;
  param_1 = auVar26._0_8_;
LAB_00562b60:
  FUN_00b4c0a0(param_1,uVar15);
  return -0x16;
}


// ===== tx25_recovery (query 0x4456c0) =====
// FUN_00545618 @ 00545618 size=1272

ulong FUN_00545618(undefined8 *param_1,undefined1 *param_2,long param_3,undefined8 *param_4)

{
  undefined1 *puVar1;
  undefined4 uVar2;
  int iVar3;
  uint uVar4;
  ulong uVar5;
  long lVar6;
  undefined8 uVar8;
  undefined8 extraout_x1;
  undefined8 extraout_x1_00;
  long lVar9;
  long *plVar10;
  long *plVar11;
  undefined1 auVar12 [16];
  long local_78;
  undefined8 local_70;
  uint local_64;
  undefined1 *local_60;
  undefined8 uStack_58;
  undefined8 local_50;
  undefined8 uStack_48;
  long *plVar7;
  
  plVar10 = (long *)*param_1;
  local_78 = 0;
  local_70 = 0;
  if ((plVar10 == (long *)0x0) && (plVar10 = (long *)FUN_00613834(), plVar10 == (long *)0x0)) {
    return 0xfffffff4;
  }
  if (*plVar10 == 0) {
    FUN_00b4c0a0(0,0x10,
                 "Input context has not been properly allocated by avformat_alloc_context() and is not NULL either\n"
                );
    return 0xffffffea;
  }
  if (param_3 != 0) {
    plVar10[1] = param_3;
  }
  if (param_4 != (undefined8 *)0x0) {
    FUN_00b41a7c(&local_70,*param_4,0);
  }
  plVar11 = plVar10 + 4;
  if (*plVar11 != 0) {
    *(uint *)(plVar10 + 0x10) = *(uint *)(plVar10 + 0x10) | 0x80;
  }
  uVar5 = FUN_00b50c54(plVar10,&local_70);
  if ((int)uVar5 < 0) {
LAB_005458b8:
    uVar5 = uVar5 & 0xffffffff;
    goto LAB_00545ab0;
  }
  puVar1 = &DAT_001ca98d;
  if (param_2 != (undefined1 *)0x0) {
    puVar1 = param_2;
  }
  lVar6 = FUN_00b4d438(puVar1);
  plVar10[0xb] = lVar6;
  if (lVar6 == 0) {
LAB_005458fc:
    uVar5 = 0xfffffff4;
    goto LAB_00545ab0;
  }
  lVar6 = *plVar11;
  local_50 = 0;
  uStack_48 = 0;
  uStack_58 = 0;
  local_64 = 0x19;
  local_60 = param_2;
  if (lVar6 == 0) {
    plVar7 = plVar10 + 1;
    if (*plVar7 == 0) {
      lVar6 = FUN_0054f164(&local_60,0,&local_64);
      *plVar7 = lVar6;
      uVar4 = local_64;
      if (lVar6 == 0) goto LAB_00545788;
      goto joined_r0x00545780;
    }
    if ((*(byte *)(*plVar7 + 0x10) & 1) == 0) {
LAB_00545788:
      uVar5 = (*(code *)plVar10[0x38])
                        (plVar10,plVar11,param_2,*(uint *)(plVar10 + 0x25) | 1,&local_70);
      if ((int)uVar5 < 0) goto LAB_005458b8;
      if (*plVar7 != 0) goto LAB_005457b4;
      lVar6 = plVar10[4];
      uVar2 = (undefined4)plVar10[0x29];
      goto LAB_00545748;
    }
    uVar4 = 0x19;
  }
  else {
    lVar9 = plVar10[1];
    *(uint *)(plVar10 + 0x10) = *(uint *)(plVar10 + 0x10) | 0x80;
    if (lVar9 == 0) {
      uVar2 = (undefined4)plVar10[0x29];
LAB_00545748:
      uVar4 = FUN_0054f1b8(lVar6,plVar10 + 1,param_2,plVar10,0,uVar2);
joined_r0x00545780:
      uVar5 = (ulong)uVar4;
      if ((int)uVar4 < 0) goto LAB_00545ab0;
    }
    else {
      if ((*(byte *)(lVar9 + 0x10) & 1) != 0) {
        FUN_0054ac8c(lVar6,plVar10 + 1,
                     "Custom AVIOContext makes no sense and will be ignored with AVFMT_NOFILE format.\n"
                    );
      }
LAB_005457b4:
      uVar4 = 0;
    }
  }
  *(uint *)((long)plVar10 + 0x144) = uVar4;
  if (((plVar10[0x2c] == 0) && (*plVar11 != 0)) && (*(long *)(*plVar11 + 0x98) != 0)) {
    lVar6 = FUN_00b4d438();
    plVar10[0x2c] = lVar6;
    if (lVar6 == 0) goto LAB_005458fc;
  }
  if (((plVar10[0x2d] == 0) && (*plVar11 != 0)) && (*(long *)(*plVar11 + 0xa0) != 0)) {
    lVar6 = FUN_00b4d438();
    plVar10[0x2d] = lVar6;
    if (lVar6 == 0) goto LAB_005458fc;
  }
  if ((plVar10[0x2b] != 0) &&
     (iVar3 = FUN_00b3d99c(*(undefined8 *)plVar10[1],plVar10[0x2b],0x2c), iVar3 < 1)) {
    FUN_00b4c0a0(plVar10,0x10,"Format not on whitelist \'%s\'\n",plVar10[0x2b]);
LAB_0054591c:
    uVar5 = 0xffffffea;
    goto LAB_00545ab0;
  }
  FUN_0054b51c(plVar10[4],plVar10[0x26]);
  lVar6 = plVar10[1];
  uVar8 = extraout_x1;
  if ((*(byte *)(lVar6 + 0x10) >> 1 & 1) != 0) {
    iVar3 = FUN_0065df00(param_2);
    if (iVar3 == 0) goto LAB_0054591c;
    lVar6 = plVar10[1];
    uVar8 = extraout_x1_00;
  }
  iVar3 = *(int *)(lVar6 + 0x3c);
  plVar10[0xd] = -0x8000000000000000;
  plVar10[0xc] = -0x8000000000000000;
  if (0 < iVar3) {
    auVar12 = FUN_00b4d3e8();
    uVar8 = auVar12._8_8_;
    plVar7 = auVar12._0_8_;
    plVar10[3] = (long)plVar7;
    if (plVar7 == (long *)0x0) goto LAB_005458fc;
    if (*(long *)(plVar10[1] + 0x28) != 0) {
      *plVar7 = *(long *)(plVar10[1] + 0x28);
      FUN_00b4ff60(plVar10[3]);
      auVar12 = FUN_00b50c54(plVar10[3],&local_70);
      uVar8 = auVar12._8_8_;
      uVar5 = auVar12._0_8_;
      if (auVar12._0_4_ < 0) goto LAB_005458b8;
    }
  }
  lVar6 = *plVar11;
  auVar12._8_8_ = uVar8;
  auVar12._0_8_ = lVar6;
  if (lVar6 != 0) {
    auVar12 = FUN_005a4864(lVar6,plVar10 + 0x4a,&DAT_001b56b8,&local_78);
  }
  if (*(long *)(plVar10[1] + 0x50) == 0) {
LAB_00545890:
    uVar8 = auVar12._0_8_;
    if (plVar10[0x18] == 0) {
      plVar10[0x18] = plVar10[0x4a];
      plVar10[0x4a] = 0;
    }
    else if (plVar10[0x4a] != 0) {
      FUN_0054ac8c(uVar8,auVar12._8_8_,
                   "Discarding ID3 tags because more suitable tags were found.\n");
      uVar8 = FUN_00b41a1c(plVar10 + 0x4a);
    }
    lVar6 = local_78;
    if (local_78 == 0) {
LAB_005459cc:
      uVar4 = FUN_0054e824(plVar10);
      if (-1 < (int)uVar4) {
        if ((*plVar11 != 0) && (plVar10[0x3f] == 0)) {
          lVar6 = FUN_0054aaf8();
          plVar10[0x3f] = lVar6;
        }
        *(undefined4 *)(plVar10 + 0x46) = 0;
        for (uVar5 = 0; uVar5 < *(uint *)((long)plVar10 + 0x2c); uVar5 = uVar5 + 1) {
          lVar6 = *(long *)(plVar10[6] + uVar5 * 8);
          if (*(int *)(lVar6 + 0x128) != 0) {
            if ((*(long *)(lVar6 + 0x340) != 0) &&
               (*(int *)(*(long *)(lVar6 + 0x108) + 0x18) != *(int *)(*(long *)(lVar6 + 0x10) + 4)))
            {
              FUN_0095a69c();
              *(undefined8 *)(lVar6 + 0x340) = 0;
            }
            iVar3 = FUN_007464e4(*(undefined8 *)(lVar6 + 0x108),*(undefined8 *)(lVar6 + 0x10));
            if (iVar3 < 0) break;
            uVar8 = FUN_00746084(*(undefined4 *)(*(long *)(lVar6 + 0x108) + 0x18));
            *(undefined4 *)(lVar6 + 0x128) = 0;
            *(undefined8 *)(lVar6 + 0x360) = uVar8;
          }
        }
        if (param_4 != (undefined8 *)0x0) {
          FUN_00b41a1c(param_4);
          *param_4 = local_70;
        }
        *param_1 = plVar10;
        return 0;
      }
    }
    else {
      uVar8 = FUN_0054abd8(uVar8,&DAT_001e9359);
      if (((((int)uVar8 != 0) && (uVar8 = FUN_0054abd8(uVar8,&DAT_001cf1d0), (int)uVar8 != 0)) &&
          (uVar8 = FUN_0054abd8(uVar8,&DAT_001d7bc2), (int)uVar8 != 0)) &&
         (iVar3 = FUN_0054abd8(uVar8,&DAT_001bde6e), iVar3 != 0)) {
        FUN_00b4c0a0(plVar10,0x30,"demuxer does not support additional id3 data, skipping\n");
LAB_005459c4:
        FUN_005a52f4(&local_78);
        goto LAB_005459cc;
      }
      uVar4 = FUN_005a53c0(plVar10,lVar6);
      if (((-1 < (int)uVar4) && (uVar4 = FUN_005a54a4(plVar10,local_78), -1 < (int)uVar4)) &&
         (uVar4 = FUN_005a5660(plVar10,local_78), -1 < (int)uVar4)) goto LAB_005459c4;
    }
    uVar5 = (ulong)uVar4;
  }
  else {
    auVar12 = FUN_0054ac98();
    if (-1 < auVar12._0_4_) goto LAB_00545890;
    uVar5 = auVar12._0_8_ & 0xffffffff;
    if ((*(byte *)(plVar10[1] + 0x40) & 1) == 0) goto LAB_00545ab0;
  }
  if (*(long *)(plVar10[1] + 0x60) != 0) {
    FUN_0054ac98();
  }
LAB_00545ab0:
  FUN_005a52f4(&local_78);
  FUN_00b41a1c(&local_70);
  if ((*plVar11 != 0) && (-1 < (char)plVar10[0x10])) {
    FUN_0054dd18(plVar11);
  }
  FUN_00543e64(plVar10);
  *param_1 = 0;
  return uVar5;
}


// ===== tx13_poll (query 0x540324) =====
// FUN_00540324 @ 00540324 size=176

undefined8 FUN_00540324(undefined8 param_1,undefined8 param_2,int param_3)

{
  android::Parcel::writeNoException();
  android::Parcel::writeInt32(param_3);
  android::Parcel::writeInt32(param_3);
  if (DAT_00c79bf8 != DAT_00c79c00) {
    DAT_00c79c00 = DAT_00c79bf8;
  }
  android::Parcel::writeInt32(param_3);
  if (DAT_00c79bfc != DAT_00c79c04) {
    DAT_00c79c04 = DAT_00c79bfc;
  }
  android::Parcel::writeInt32(param_3);
  android::Parcel::writeInt32(param_3);
  DAT_00c79c08 = 0;
  return 0;
}


// ===== pipeline_start (query 0x541108) =====
// FUN_00541108 @ 00541108 size=500

undefined8 FUN_00541108(byte *param_1)

{
  uint uVar1;
  int iVar2;
  long lVar3;
  undefined4 uVar4;
  byte *__s1;
  long lVar5;
  ulong uVar6;
  byte *pbVar7;
  byte *pbVar8;
  double dVar9;
  
  param_1[0x50] = 0;
  param_1[0x58] = 0xff;
  param_1[0x59] = 0xff;
  param_1[0x5a] = 0xff;
  param_1[0x5b] = 0xff;
  param_1[0x5c] = 0xff;
  param_1[0x5d] = 0xff;
  param_1[0x5e] = 0xff;
  param_1[0x5f] = 0xff;
  param_1[0x60] = 0xff;
  param_1[0x61] = 0xff;
  param_1[0x62] = 0xff;
  param_1[99] = 0xff;
  param_1[100] = 0xff;
  param_1[0x65] = 0xff;
  param_1[0x66] = 0xff;
  param_1[0x67] = 0xff;
  FUN_00b8e300();
  pbVar7 = param_1 + 0x48;
  if (*(long *)pbVar7 != 0) {
    FUN_0094a828(pbVar7);
    pbVar7[0] = 0;
    pbVar7[1] = 0;
    pbVar7[2] = 0;
    pbVar7[3] = 0;
    pbVar7[4] = 0;
    pbVar7[5] = 0;
    pbVar7[6] = 0;
    pbVar7[7] = 0;
  }
  pbVar8 = param_1 + 0x30;
  if (*(long *)pbVar8 != 0) {
    FUN_00545b10(pbVar8);
    pbVar8[0] = 0;
    pbVar8[1] = 0;
    pbVar8[2] = 0;
    pbVar8[3] = 0;
    pbVar8[4] = 0;
    pbVar8[5] = 0;
    pbVar8[6] = 0;
    pbVar8[7] = 0;
  }
  __s1 = *(byte **)(param_1 + 0x10);
  if ((*param_1 & 1) == 0) {
    __s1 = param_1 + 1;
  }
  iVar2 = strncasecmp((char *)__s1,"rtmp://",7);
  uVar4 = 1;
  if (iVar2 == 0) {
    uVar4 = 2;
  }
  *(undefined4 *)(param_1 + 0x18) = uVar4;
  iVar2 = FUN_00545618(pbVar8,__s1,0,0);
  if ((-1 < iVar2) && (iVar2 = FUN_00547354(*(undefined8 *)pbVar8,0), -1 < iVar2)) {
    param_1[0x38] = 0xff;
    param_1[0x39] = 0xff;
    param_1[0x3a] = 0xff;
    param_1[0x3b] = 0xff;
    uVar1 = *(uint *)(*(long *)(param_1 + 0x30) + 0x2c);
    if (uVar1 != 0) {
      lVar5 = *(long *)(*(long *)(param_1 + 0x30) + 0x30);
      uVar6 = 0;
      do {
        if (**(int **)(*(long *)(lVar5 + uVar6 * 8) + 0x10) == 0) {
          lVar5 = *(long *)(lVar5 + uVar6 * 8);
          *(int *)(param_1 + 0x38) = (int)uVar6;
          lVar5 = *(long *)(lVar5 + 0x10);
          uVar4 = *(undefined4 *)(lVar5 + 4);
          *(long *)(param_1 + 0x40) = lVar5;
          lVar5 = FUN_00671bd4(uVar4);
          if (lVar5 == 0) {
            return 0;
          }
          lVar3 = FUN_0094a698();
          *(long *)pbVar7 = lVar3;
          if (lVar3 == 0) {
            return 0;
          }
          iVar2 = FUN_007464e4(lVar3,*(undefined8 *)(param_1 + 0x40));
          if (iVar2 < 0) {
            return 0;
          }
          iVar2 = FUN_006d5cec(*(undefined8 *)pbVar7,lVar5,0);
          if (iVar2 < 0) {
            return 0;
          }
          lVar3 = *(long *)(*(long *)(*(long *)(param_1 + 0x30) + 0x30) +
                           (long)*(int *)(param_1 + 0x38) * 8);
          *(undefined8 *)(param_1 + 0x78) = *(undefined8 *)(*(long *)(param_1 + 0x48) + 0x74);
          lVar5 = FUN_00544020(lVar3,5,0);
          if (lVar5 == 0) {
            lVar5 = FUN_00b415ec(*(undefined8 *)(lVar3 + 0x50),"rotate",0,0);
            if ((lVar5 == 0) || (*(char **)(lVar5 + 8) == (char *)0x0)) {
              iVar2 = 0;
              goto LAB_005412f0;
            }
            iVar2 = atoi(*(char **)(lVar5 + 8));
          }
          else {
            dVar9 = (double)FUN_00b41ca8();
            iVar2 = (int)dVar9;
          }
          iVar2 = (iVar2 + 0x168) % 0x168;
LAB_005412f0:
          *(int *)(param_1 + 0x80) = iVar2;
          return 1;
        }
        uVar6 = uVar6 + 1;
      } while (uVar1 != uVar6);
    }
  }
  return 0;
}



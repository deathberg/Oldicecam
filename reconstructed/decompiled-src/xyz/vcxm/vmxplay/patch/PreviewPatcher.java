package xyz.vcxm.vmxplay.patch;

import android.app.Activity;
import android.app.Application;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/* loaded from: classes5.dex */
public final class PreviewPatcher {
    private static final int FIND_INTERVAL_MS = 200;
    private static final int FIND_TRIES = 40;
    private static final String TAG = "PreviewFix2";
    private static CameraDevice sCamera;
    private static Size sPreviewSize;
    private static CameraCaptureSession sSession;
    private static TextureView sTex;
    private static final AtomicBoolean sStarting = new AtomicBoolean(false);
    private static final AtomicBoolean sRunning = new AtomicBoolean(false);
    private static boolean sListenerSet = false;

    private PreviewPatcher() {
    }

    public static void attachToPreviewButton(final Activity act) {
        if (act == null) {
            return;
        }
        int idBtn = act.getResources().getIdentifier("camera_preview", "id", act.getPackageName());
        if (idBtn == 0) {
            Log.w(TAG, "camera_preview button id not found");
            return;
        }
        View btn = act.findViewById(idBtn);
        if (btn == null) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    PreviewPatcher.attachToPreviewButton(act);
                }
            }, 200L);
            return;
        }
        final View.OnClickListener old = getExistingOnClick(btn);
        View.OnClickListener chain = new View.OnClickListener() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                PreviewPatcher.lambda$attachToPreviewButton$1(old, act, view);
            }
        };
        btn.setOnClickListener(chain);
        act.getApplication().registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher.1
            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityResumed(Activity a2) {
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityPaused(Activity a2) {
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityStarted(Activity a2) {
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityStopped(Activity a2) {
                if (a2 == act) {
                    PreviewPatcher.stopPreview();
                }
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityCreated(Activity a2, Bundle b2) {
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivityDestroyed(Activity a2) {
                if (a2 == act) {
                    PreviewPatcher.stopPreview();
                    try {
                        a2.getApplication().unregisterActivityLifecycleCallbacks(this);
                    } catch (Throwable th) {
                    }
                }
            }

            @Override // android.app.Application.ActivityLifecycleCallbacks
            public void onActivitySaveInstanceState(Activity a2, Bundle b2) {
            }
        });
    }

    static /* synthetic */ void lambda$attachToPreviewButton$1(View.OnClickListener old, Activity act, View v2) {
        if (old != null) {
            try {
                old.onClick(v2);
            } catch (Throwable th) {
            }
        }
        startFromUserClick(act);
    }

    private static void startFromUserClick(final Activity act) {
        if (sRunning.get()) {
            Toast.makeText(act, "\u9884\u89c8\u5df2\u5f00\u542f", 0).show();
            return;
        }
        if (!sStarting.compareAndSet(false, true)) {
            return;
        }
        if (ContextCompat.checkSelfPermission(act, "android.permission.CAMERA") != 0) {
            ActivityCompat.requestPermissions(act, new String[]{"android.permission.CAMERA"}, 9921);
            Toast.makeText(act, "\u9700\u8981\u76f8\u673a\u6743\u9650\u4ee5\u663e\u793a\u9884\u89c8", 0).show();
            sStarting.set(false);
        } else {
            final Handler main = new Handler(Looper.getMainLooper());
            final int idOverlay = act.getResources().getIdentifier("preview_overlay", "id", act.getPackageName());
            final int idTex = act.getResources().getIdentifier("cameraTextureView", "id", act.getPackageName());
            Runnable finder = new Runnable() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher.2
                int tries = 0;

                @Override // java.lang.Runnable
                public void run() {
                    this.tries++;
                    TextureView tv = idTex != 0 ? (TextureView) act.findViewById(idTex) : null;
                    if (tv != null) {
                        View overlay = idOverlay != 0 ? act.findViewById(idOverlay) : null;
                        if ((overlay instanceof FrameLayout) && overlay.getVisibility() != 0) {
                            overlay.setVisibility(0);
                        }
                        PreviewPatcher.ensurePreviewForTextureView(act, tv);
                        return;
                    }
                    if (this.tries >= 40) {
                        PreviewPatcher.sStarting.set(false);
                        Log.w(PreviewPatcher.TAG, "cameraTextureView not found after retries");
                        Toast.makeText(act, "\u9884\u89c8\u754c\u9762\u672a\u5c31\u7eea", 0).show();
                        return;
                    }
                    main.postDelayed(this, 200L);
                }
            };
            main.post(finder);
        }
    }

    private static View.OnClickListener getExistingOnClick(View v2) {
        try {
            Field fInfo = View.class.getDeclaredField("mListenerInfo");
            fInfo.setAccessible(true);
            Object info = fInfo.get(v2);
            if (info == null) {
                return null;
            }
            Field fClick = info.getClass().getDeclaredField("mOnClickListener");
            fClick.setAccessible(true);
            Object l2 = fClick.get(info);
            if (l2 instanceof View.OnClickListener) {
                return (View.OnClickListener) l2;
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public static void stopPreview() {
        try {
            if (sSession != null) {
                sSession.stopRepeating();
                sSession.close();
            }
        } catch (Throwable th) {
        }
        try {
            if (sCamera != null) {
                sCamera.close();
            }
        } catch (Throwable th2) {
        }
        sSession = null;
        sCamera = null;
        sTex = null;
        sPreviewSize = null;
        sListenerSet = false;
        sRunning.set(false);
        sStarting.set(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void ensurePreviewForTextureView(final Activity a2, TextureView tv) {
        sTex = tv;
        if (sTex.isAvailable()) {
            startIfPossible(a2, sTex.getWidth(), sTex.getHeight());
        } else if (!sListenerSet) {
            sListenerSet = true;
            sTex.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher.3
                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    PreviewPatcher.startIfPossible(a2, width, height);
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    PreviewPatcher.configureTransform(a2, width, height);
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    PreviewPatcher.stopPreview();
                    return true;
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void startIfPossible(Activity a2, int vw, int vh) {
        try {
            if (sRunning.get()) {
                sStarting.set(false);
                return;
            }
            CameraManager cm = (CameraManager) a2.getSystemService("camera");
            String cameraId = null;
            Size best = null;
            String[] cameraIdList = cm.getCameraIdList();
            int length = cameraIdList.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String id = cameraIdList[i];
                CameraCharacteristics c2 = cm.getCameraCharacteristics(id);
                Integer facing = (Integer) c2.get(CameraCharacteristics.LENS_FACING);
                if (facing == null || facing.intValue() != 0) {
                    i++;
                } else {
                    StreamConfigurationMap map = (StreamConfigurationMap) c2.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if (map != null) {
                        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                        best = chooseSize(sizes, vw, vh);
                    }
                    cameraId = id;
                }
            }
            if (cameraId == null) {
                String[] ids = cm.getCameraIdList();
                if (ids.length == 0) {
                    sStarting.set(false);
                    Toast.makeText(a2, "\u65e0\u53ef\u7528\u76f8\u673a", 0).show();
                    return;
                }
                cameraId = ids[0];
            }
            if (best == null) {
                best = new Size(vw > 0 ? vw : 640, vh > 0 ? vh : 480);
            }
            final SurfaceTexture st = sTex.getSurfaceTexture();
            if (st == null) {
                sStarting.set(false);
                Log.w(TAG, "SurfaceTexture null");
            } else {
                st.setDefaultBufferSize(best.getWidth(), best.getHeight());
                configureTransform(a2, vw, vh);
                String useId = cameraId;
                cm.openCamera(useId, new CameraDevice.StateCallback() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher.4
                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onOpened(CameraDevice camera) {
                        PreviewPatcher.sCamera = camera;
                        try {
                            Surface surface = new Surface(st);
                            final CaptureRequest.Builder b2 = camera.createCaptureRequest(1);
                            b2.addTarget(surface);
                            camera.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() { // from class: xyz.vcxm.vmxplay.patch.PreviewPatcher.4.1
                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigured(CameraCaptureSession session) {
                                    try {
                                        PreviewPatcher.sSession = session;
                                        b2.set(CaptureRequest.CONTROL_AF_MODE, 4);
                                        b2.set(CaptureRequest.CONTROL_AE_MODE, 1);
                                        session.setRepeatingRequest(b2.build(), null, null);
                                        PreviewPatcher.sRunning.set(true);
                                        PreviewPatcher.sStarting.set(false);
                                    } catch (CameraAccessException e2) {
                                        Log.e(PreviewPatcher.TAG, "repeating failed", e2);
                                        PreviewPatcher.sStarting.set(false);
                                    }
                                }

                                @Override // android.hardware.camera2.CameraCaptureSession.StateCallback
                                public void onConfigureFailed(CameraCaptureSession session) {
                                    Log.e(PreviewPatcher.TAG, "onConfigureFailed");
                                    PreviewPatcher.sStarting.set(false);
                                }
                            }, null);
                        } catch (Throwable t2) {
                            Log.e(PreviewPatcher.TAG, "createSession failed", t2);
                            PreviewPatcher.sStarting.set(false);
                        }
                    }

                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onDisconnected(CameraDevice camera) {
                        PreviewPatcher.stopPreview();
                    }

                    @Override // android.hardware.camera2.CameraDevice.StateCallback
                    public void onError(CameraDevice camera, int error) {
                        Log.e(PreviewPatcher.TAG, "camera error " + error);
                        PreviewPatcher.stopPreview();
                    }
                }, (Handler) null);
            }
        } catch (Throwable t2) {
            sStarting.set(false);
            Log.e(TAG, "startIfPossible error", t2);
        }
    }

    private static Size chooseSize(Size[] sizes, int vw, int vh) {
        if (sizes == null || sizes.length == 0) {
            return new Size(640, 480);
        }
        Size best = sizes[0];
        int want = vw * vh;
        int bestDelta = Integer.MAX_VALUE;
        for (Size s2 : sizes) {
            int delta = Math.abs((s2.getWidth() * s2.getHeight()) - want);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = s2;
            }
        }
        return best;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void configureTransform(Activity a2, int viewWidth, int viewHeight) {
        if (sTex == null) {
            return;
        }
        int rotation = a2.getWindowManager().getDefaultDisplay().getRotation();
        Matrix m2 = new Matrix();
        float cx = viewWidth / 2.0f;
        float cy = viewHeight / 2.0f;
        if (rotation == 0 || rotation == 2) {
            m2.postRotate(90.0f, cx, cy);
        }
        m2.postScale(-1.0f, 1.0f, cx, cy);
        sTex.setTransform(m2);
    }
}

package com.xiaomi.vlive.ui.controller;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.FloatService;
import com.xiaomi.vlive.MediaProjectionForegroundService;
import com.xiaomi.vlive.R;
import com.xiaomi.vlive.config.AppConfigKeys;
import com.xiaomi.vlive.util.VliveBridge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Reconstructed core logic from {@code com.xiaomi.vlive.ui.controller.ControllerFragment}.
 * UI ids match {@code res/layout/fragment_controller.xml} from apktool decode.
 */
public class ControllerFragment extends Fragment {
    private App app;
    private EditText playFileInput;
    private CompoundButton floatingSwitch;
    private CompoundButton autoRotateSwitch;
    private CompoundButton loopSwitch;
    private CompoundButton autoColorSwitch;

    private MediaProjectionManager projectionManager;
    private MediaProjection projection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler captureHandler;
    private int monitorX = 55;
    private int monitorY = 380;
    private int screenW, screenH, densityDpi;
    private int lastDetectedColor;

    private ActivityResultLauncher<String> pickVideoLauncher;
    private ActivityResultLauncher<Intent> projectionLauncher;
    private ActivityResultLauncher<Intent> overlayPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (App) requireActivity().getApplication();

        pickVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) copyAndPlay(uri);
                });

        projectionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                        Toast.makeText(requireContext(), "你已拒绝授权，无法使用三色功能", Toast.LENGTH_LONG).show();
                        autoColorSwitch.setChecked(false);
                        return;
                    }
                    startScreenCapture(result.getResultCode(), result.getData());
                });

        overlayPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                r -> {
                    if (Settings.canDrawOverlays(requireContext())) {
                        app.getPrefs().edit().putBoolean(AppConfigKeys.FLOATING_TOOL, true).apply();
                        requireContext().startService(new Intent(requireContext(), FloatService.class));
                        floatingSwitch.setChecked(true);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_controller, container, false);
        playFileInput = root.findViewById(R.id.playfile);
        floatingSwitch = root.findViewById(R.id.switchfloatingwindow);
        autoRotateSwitch = root.findViewById(R.id.auto_rotate);
        loopSwitch = root.findViewById(R.id.play_loop);
        autoColorSwitch = root.findViewById(R.id.auto_color);
        RadioGroup fileType = root.findViewById(R.id.radioGroup_file_type);

        projectionManager = (MediaProjectionManager)
                requireContext().getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE);

        root.findViewById(R.id.selectsave).setOnClickListener(v -> pickVideoLauncher.launch("video/*"));
        root.findViewById(R.id.start_hook).setOnClickListener(v -> startCameraReplacement());
        root.findViewById(R.id.camera_preview).setOnClickListener(v -> {
            // PreviewPatcher attached in MainActivity
        });

        floatingSwitch.setOnCheckedChangeListener(this::onFloatingToggled);
        autoRotateSwitch.setOnCheckedChangeListener(this::onAutoRotateToggled);
        loopSwitch.setOnCheckedChangeListener(this::onLoopToggled);
        autoColorSwitch.setOnCheckedChangeListener(this::onAutoColorToggled);

        restorePrefs();
        return root;
    }

    private void restorePrefs() {
        int type = app.getPrefs().getInt(AppConfigKeys.PLAY_FILE_TYPE, 1);
        if (type == 1) {
            playFileInput.setText(app.getPrefs().getString(AppConfigKeys.PLAY_FILE_MP4, ""));
        } else {
            playFileInput.setText(app.getPrefs().getString(AppConfigKeys.PLAY_RTMP_URL,
                    "rtmp://ns8.indexforce.com/home/mystream"));
        }
        autoRotateSwitch.setChecked(app.getPrefs().getBoolean(AppConfigKeys.PLAY_AUTO_ROTATE, false));
        loopSwitch.setChecked(app.isLoopEnabled());
    }

    private void copyAndPlay(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            if (in == null) return;
            File out = new File(requireContext().getCacheDir(), "play.mp4");
            if (out.exists()) out.delete();
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
            in.close();
            fos.close();

            app.getPrefs().edit()
                    .putInt(AppConfigKeys.PLAY_FILE_TYPE, 1)
                    .putString(AppConfigKeys.PLAY_FILE_MP4, out.getAbsolutePath())
                    .apply();

            if (VliveBridge.setSource(out.getAbsolutePath(), 1)) {
                Toast.makeText(requireContext(), "播放文件更换成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "文件设置成功未播放", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "保存文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCameraReplacement() {
        String path;
        if (app.getPrefs().getInt(AppConfigKeys.PLAY_FILE_TYPE, 1) == 1) {
            path = app.getPrefs().getString(AppConfigKeys.PLAY_FILE_MP4, "");
        } else {
            path = playFileInput.getText().toString().trim();
            app.getPrefs().edit().putString(AppConfigKeys.PLAY_RTMP_URL, path).apply();
        }
        if (path.isEmpty()) {
            Toast.makeText(requireContext(), "请先选择视频或填写RTMP", Toast.LENGTH_SHORT).show();
            return;
        }
        VliveBridge.setSource(path, 1);
        try {
            VliveBridge.service().stopOrQuery();
        } catch (Exception ignored) {}
    }

    private void onFloatingToggled(CompoundButton button, boolean enabled) {
        if (!enabled) {
            requireContext().stopService(new Intent(requireContext(), FloatService.class));
            app.getPrefs().edit().putBoolean(AppConfigKeys.FLOATING_TOOL, false).apply();
            return;
        }
        if (!Settings.canDrawOverlays(requireContext())) {
            button.setChecked(false);
            overlayPermissionLauncher.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireContext().getPackageName())));
            return;
        }
        app.getPrefs().edit().putBoolean(AppConfigKeys.FLOATING_TOOL, true).apply();
        requireContext().startService(new Intent(requireContext(), FloatService.class));
    }

    private void onAutoRotateToggled(CompoundButton button, boolean enabled) {
        app.getPrefs().edit().putBoolean(AppConfigKeys.PLAY_AUTO_ROTATE, enabled).apply();
        VliveBridge.setAutoRotate(enabled);
    }

    private void onLoopToggled(CompoundButton button, boolean enabled) {
        app.setLoopEnabled(enabled);
        VliveBridge.setLoop(enabled);
    }

    private void onAutoColorToggled(CompoundButton button, boolean enabled) {
        if (!enabled) {
            stopScreenCapture();
            VliveBridge.applyAutoColor(0, app);
            return;
        }
        monitorX = app.getPrefs().getInt(AppConfigKeys.MONITOR_TARGET_X, 55);
        monitorY = app.getPrefs().getInt(AppConfigKeys.MONITOR_TARGET_Y, 380);
        requireContext().startForegroundService(
                new Intent(requireContext(), MediaProjectionForegroundService.class));
        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getRealSize(size);
        screenW = size.x;
        screenH = size.y;
        densityDpi = getResources().getDisplayMetrics().densityDpi;
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent());
    }

    private void startScreenCapture(int resultCode, Intent data) {
        projection = projectionManager.getMediaProjection(resultCode, data);
        HandlerThread thread = new HandlerThread("ScreenCaptureThread");
        thread.start();
        captureHandler = new Handler(thread.getLooper());
        imageReader = ImageReader.newInstance(screenW, screenH, android.graphics.ImageFormat.RGBA_8888, 2);
        virtualDisplay = projection.createVirtualDisplay(
                "ScreenCapture", screenW, screenH, densityDpi, 0,
                imageReader.getSurface(), null, captureHandler);
        imageReader.setOnImageAvailableListener(reader -> {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                if (image == null) return;
                int color = sampleColor(image, monitorX, monitorY);
                if (color != lastDetectedColor) {
                    if (VliveBridge.applyAutoColor(color, app)) {
                        lastDetectedColor = color;
                    }
                }
            } finally {
                if (image != null) image.close();
            }
        }, captureHandler);
    }

    private void stopScreenCapture() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        if (projection != null) {
            projection.stop();
            projection = null;
        }
        requireContext().stopService(new Intent(requireContext(), MediaProjectionForegroundService.class));
    }

    /** Reconstructed from {@code ControllerFragment.m1784L}. */
    static int sampleColor(Image image, int x, int y) {
        Image.Plane plane = image.getPlanes()[0];
        ByteBuffer buf = plane.getBuffer();
        int pixelStride = plane.getPixelStride();
        int rowStride = plane.getRowStride();
        int offset = y * rowStride + x * pixelStride;
        int r = buf.get(offset) & 0xFF;
        int g = buf.get(offset + 1) & 0xFF;
        int b = buf.get(offset + 2) & 0xFF;
        return (r << 16) | (g << 8) | b;
    }
}

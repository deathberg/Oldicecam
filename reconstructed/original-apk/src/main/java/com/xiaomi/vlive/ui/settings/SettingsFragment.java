package com.xiaomi.vlive.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.R;
import com.xiaomi.vlive.config.ActionRanges;
import com.xiaomi.vlive.config.AppConfigKeys;

/**
 * Reconstructed from {@code com.xiaomi.vlive.ui.settings.SettingsFragment}.
 * Edits ActionRange* prefs and MonitorTargetX/Y via full-screen touch overlay.
 */
public class SettingsFragment extends Fragment {
    private App app;
    private TextView coordLabel;
    private FrameLayout pickerOverlay;

    private static final int[][] ACTION_UI = {
            {R.id.actionRangebgin1, R.id.actionRangeend1, ActionRanges.ACTION_EYE},
            {R.id.actionRangebgin2, R.id.actionRangeend2, ActionRanges.ACTION_HEAD_UP},
            {R.id.actionRangebgin3, R.id.actionRangeend3, ActionRanges.ACTION_MOUTH},
            {R.id.actionRangebgin4, R.id.actionRangeend4, ActionRanges.ACTION_TURN_LEFT},
            {R.id.actionRangebgin5, R.id.actionRangeend5, ActionRanges.ACTION_CENTER},
            {R.id.actionRangebgin6, R.id.actionRangeend6, ActionRanges.ACTION_TURN_RIGHT},
            {R.id.actionRangebgin8, R.id.actionRangeend8, ActionRanges.ACTION_NOD},
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        app = (App) requireActivity().getApplication();
        coordLabel = root.findViewById(R.id.textxy);
        pickerOverlay = root.findViewById(R.id.rootLayout);

        for (int[] row : ACTION_UI) {
            EditText begin = root.findViewById(row[0]);
            EditText end = root.findViewById(row[1]);
            int actionId = row[2];
            begin.setText(String.valueOf(app.getActionRangeBegin(actionId, ActionRanges.defaultBegin(actionId))));
            end.setText(String.valueOf(app.getActionRangeEnd(actionId, ActionRanges.defaultEnd(actionId))));
        }

        updateCoordLabel();
        root.findViewById(R.id.savePlayTime).setOnClickListener(v -> saveActionRanges(root));
        root.findViewById(R.id.selectxy).setOnClickListener(v -> {
            pickerOverlay.setVisibility(View.VISIBLE);
            pickerOverlay.setOnTouchListener(this::onPickCoordinate);
        });
        return root;
    }

    private void saveActionRanges(View root) {
        for (int[] row : ACTION_UI) {
            EditText begin = root.findViewById(row[0]);
            EditText end = root.findViewById(row[1]);
            int actionId = row[2];
            try {
                app.setActionRangeBegin(actionId, Long.parseLong(begin.getText().toString().trim()));
                app.setActionRangeEnd(actionId, Long.parseLong(end.getText().toString().trim()));
            } catch (NumberFormatException ignored) {}
        }
    }

    private boolean onPickCoordinate(View v, MotionEvent e) {
        if (e.getAction() != MotionEvent.ACTION_UP) return true;
        int x = (int) e.getX();
        int y = (int) e.getY();
        app.getPrefs().edit()
                .putInt(AppConfigKeys.MONITOR_TARGET_X, x)
                .putInt(AppConfigKeys.MONITOR_TARGET_Y, y)
                .apply();
        pickerOverlay.setVisibility(View.GONE);
        updateCoordLabel();
        return true;
    }

    private void updateCoordLabel() {
        int x = app.getPrefs().getInt(AppConfigKeys.MONITOR_TARGET_X, 55);
        int y = app.getPrefs().getInt(AppConfigKeys.MONITOR_TARGET_Y, 380);
        coordLabel.setText("当前选择监测屏幕坐标: (" + x + ", " + y + ")");
    }
}

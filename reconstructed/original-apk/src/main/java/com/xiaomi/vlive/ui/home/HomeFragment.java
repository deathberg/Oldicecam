package com.xiaomi.vlive.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.xiaomi.vlive.App;
import com.xiaomi.vlive.R;
import com.xiaomi.vlive.util.RootShell;

import java.io.File;

/** Reconstructed from {@code com.xiaomi.vlive.ui.home.HomeFragment}. */
public class HomeFragment extends Fragment {

    private TextView contactView;
    private TextView noticeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        contactView = root.findViewById(R.id.contact);
        noticeView = root.findViewById(R.id.notice);

        App app = (App) requireActivity().getApplication();
        // Original observes LiveData f2593h / f2594i populated elsewhere (license/server)
        app.getMainHandler().post(() -> {
            // placeholder — original sets HTML from remote config
        });

        if (competingVirtualCamPresent()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("警告")
                    .setMessage("检测到系统存在其他虚拟相机\n卸载后也会有残留容易触发风控\n是否需要清理残留?")
                    .setPositiveButton("确定", (d, w) -> {
                        RootShell.exec("chattr -i /data/camera");
                        RootShell.exec("rm -r /data/camera");
                        RootShell.exec("rm -r /data/samera");
                    })
                    .setNegativeButton("取消", (d, w) -> d.dismiss())
                    .show();
        }
        return root;
    }

    private boolean competingVirtualCamPresent() {
        return new File("/data/camera/libshadowhook.so").exists()
                || new File("/data/samera/libshadowhook.so").exists();
    }

    private void bindHtml(TextView tv, String html) {
        tv.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

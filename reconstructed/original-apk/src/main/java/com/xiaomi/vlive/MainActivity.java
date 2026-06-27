package com.xiaomi.vlive;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import xyz.vcxm.vmxplay.patch.PreviewPatcher;

/**
 * Reconstructed from {@code com.xiaomi.vlive.MainActivity}.
 * Hosts Home / Controller / Settings via Navigation component.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreviewPatcher.attachToPreviewButton(this);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);
        if (host == null) throw new IllegalStateException("NavHostFragment missing");
        NavController nav = host.getNavController();
        NavigationUI.setupWithNavController(navView, nav);
    }
}

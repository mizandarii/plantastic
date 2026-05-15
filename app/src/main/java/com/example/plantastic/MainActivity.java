package com.example.plantastic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.plantastic.databinding.ActivityMainBinding;
import com.example.plantastic.notifications.CareNotificationManager;
import com.example.plantastic.notifications.CareNotificationWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request notification permission (Android 13+)
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    // Permission granted or denied; we can proceed either way
                }
        );

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Initialize notification system
        initializeNotifications();

        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                replaceFragment(new HomeFragment());
            } else if (id == R.id.nav_plants) {
                replaceFragment(new PlantsFragment());
            } else if (id == R.id.nav_enc) {
                replaceFragment(new EncyclopediaFragment());
            } else if (id == R.id.nav_settings) {
                replaceFragment(new SettingsFragment());
            } else if (id == R.id.nav_fav) {
                replaceFragment(new FavoritesFragment());
            }

            return true;
        });
    }

    private void initializeNotifications() {
        // Create notification channel
        CareNotificationManager.createNotificationChannel(this);

        // Schedule periodic check for due notifications (every 15 minutes)
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(
                CareNotificationWorker.class,
                15,
                TimeUnit.MINUTES
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "care_notification_check",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
        );
    }

    public void goToAddPlant(View view) {
        Intent intent = new Intent(this, AddPlantFragment.class);
        startActivity(intent);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}



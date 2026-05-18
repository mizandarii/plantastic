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

import com.example.plantastic.databinding.ActivityMainBinding;
import com.example.plantastic.notifications.CareNotificationManager;
import com.example.plantastic.notifications.CareReminderScheduler;
import android.content.pm.ApplicationInfo;
import android.util.Log;

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

        // If the activity was started from a notification with a plantId, open that plant detail.
        handleIntent(getIntent());

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

        // Debug: seed a test plant that uses the fast testing interval (1 minute)
        boolean isDebuggable = (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        if (isDebuggable) {
            new Thread(() -> {
                try {
                    com.example.plantastic.data.PlantasticDatabase db = com.example.plantastic.data.PlantasticDatabase.getInstance(this);
                    // Check for an existing test plant nickname
                    java.util.List<com.example.plantastic.data.entities.Taim> all = db.taimDao().getAll();
                    boolean found = false;
                    for (com.example.plantastic.data.entities.Taim t : all) {
                        if (t.nimi != null && t.nimi.contains("TEST - kastmine")) {
                            found = true; break;
                        }
                    }
                    if (!found) {
                        Log.d("DEBUG_SEED", "Test plant not found. Creating one...");
                        // Ensure a user exists
                        com.example.plantastic.data.entities.Kasutaja user = db.kasutajaDao().getFirstUser();
                        if (user == null) {
                            user = new com.example.plantastic.data.entities.Kasutaja();
                            user.kasutajanimi = "Primary User";
                            long uid = db.kasutajaDao().insert(user);
                            user.id = (int) uid;
                        }

                        // Create a simple sort and liik
                        com.example.plantastic.data.entities.TaimLiik liik = db.taimLiikDao().getByName("TestFamily");
                        int liikId;
                        if (liik == null) {
                            liik = new com.example.plantastic.data.entities.TaimLiik();
                            liik.nimetus = "TestFamily";
                            liik.ladinakeelne_nimetus = "Testus";
                            long lid = db.taimLiikDao().insert(liik);
                            liikId = (int) lid;
                        } else {
                            liikId = liik.id;
                        }

                        com.example.plantastic.data.entities.TaimSort sort = new com.example.plantastic.data.entities.TaimSort();
                        sort.api_taim_id = -1;
                        sort.nimetus = "TEST - Kastmis-sorted";
                        sort.ladinakeelne_nimetus = "Testus minimalis";
                        sort.liik_id = liikId;
                        sort.kastmisvajadus = 4; // test intensity -> 1 minute
                        sort.valgusnoudlikkus = 2;
                        long sortId = db.taimSortDao().insert(sort);

                        com.example.plantastic.data.entities.Taim taim = new com.example.plantastic.data.entities.Taim();
                        taim.nimi = "TEST - kastmine (1min)";
                        taim.sort_id = (int) sortId;
                        taim.kasutaja_id = user.id;
                        taim.kirjeldus = "Test plant for notification demo. Delete me.";
                        long taimId = db.taimDao().insert(taim);

                        // Ensure kastmine care type exists
                        com.example.plantastic.data.entities.HooldusTüüp kast = db.hooldusTüüpDao().getByName("Kastmine");
                        if (kast == null) {
                            kast = new com.example.plantastic.data.entities.HooldusTüüp();
                            kast.nimetus = "Kastmine";
                            long kid = db.hooldusTüüpDao().insert(kast);
                            kast.id = (int) kid;
                        }

                        // Insert a Teade scheduled for the fast testing interval
                        com.example.plantastic.data.entities.Teade teade = new com.example.plantastic.data.entities.Teade();
                        teade.taim_id = (int) taimId;
                        teade.hooldusTüüp_id = kast.id;
                        teade.aeg = System.currentTimeMillis() + (30 * 1000);
                        teade.kommentaar = "Test watering reminder (30 seconds)";
                        db.teadeDao().insert(teade);
                        CareReminderScheduler.scheduleReminder(
                                MainActivity.this,
                                (int) taimId,
                                kast.id,
                                teade.aeg
                        );
                        Log.d("DEBUG_SEED", "Test plant created with ID: " + taimId + ", reminder scheduled.");
                    } else {
                        Log.d("DEBUG_SEED", "Test plant already exists. Syncing reminder work for existing test plants...");
                        // If test plant exists from previous run, make sure reminder work is scheduled for each such plant
                        for (com.example.plantastic.data.entities.Taim t : all) {
                            try {
                                if (t.nimi != null && t.nimi.contains("TEST - kastmine")) {
                                    java.util.List<com.example.plantastic.data.entities.Teade> teades = db.teadeDao().getByTaimId(t.id);
                                    if (teades != null) {
                                        for (com.example.plantastic.data.entities.Teade teade : teades) {
                                            if (teade != null && teade.hooldusTüüp_id != null) {
                                                CareReminderScheduler.scheduleReminder(
                                                        MainActivity.this,
                                                        t.id,
                                                        teade.hooldusTüüp_id,
                                                        teade.aeg
                                                );
                                                Log.d("DEBUG_SEED", "Scheduled existing test plant reminder id=" + t.id + " type=" + teade.hooldusTüüp_id);
                                            }
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("DEBUG_SEED", "Failed to schedule notification for existing test plant", e);
                            }
                        }
                    }
                    CareReminderScheduler.syncUpcomingReminders(MainActivity.this, db);
                    runOnUiThread(() -> {
                        Fragment current = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                        if (current instanceof HomeFragment) {
                            ((HomeFragment) current).refreshReminders();
                            if (binding != null && binding.main != null) {
                                binding.main.postDelayed(() -> {
                                    Fragment delayedCurrent = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
                                    if (delayedCurrent instanceof HomeFragment) {
                                        ((HomeFragment) delayedCurrent).refreshReminders();
                                    }
                                }, 300L);
                            }
                        }
                    });
                } catch (Exception ex) {
                    Log.e("DEBUG_SEED", "Error during test plant seeding", ex);
                }
            }).start();
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    // Handle incoming intents that may contain a plantId to open
    private void handleIntent(Intent intent) {
        if (intent == null) return;
        int plantId = intent.getIntExtra("plantId", -1);
        if (plantId != -1) {
            // Open MyPlantFragment for this local plant id
            MyPlantFragment fragment = new MyPlantFragment();
            android.os.Bundle args = new android.os.Bundle();
            args.putInt("plantId", plantId);
            fragment.setArguments(args);
            replaceFragment(fragment);
        }
    }
}

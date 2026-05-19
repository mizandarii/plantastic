package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.TaimWithDetails;
import com.example.plantastic.data.entities.Teade;
import com.example.plantastic.notifications.CareActionHandler;
import com.example.plantastic.notifications.CareReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private PlantasticDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private LinearLayout sectionToday, sectionNext7Days, sectionLater;
    private LinearLayout containerToday, containerNext7Days, containerLater;
    private TextView emptyText;
    private View headerToday, headerWeek, headerLater;
    private ImageView toggleToday, toggleWeek, toggleLater;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = PlantasticDatabase.getInstance(requireContext());
        
        sectionToday = view.findViewById(R.id.sectionToday);
        sectionNext7Days = view.findViewById(R.id.sectionNext7Days);
        sectionLater = view.findViewById(R.id.sectionLater);
        containerToday = view.findViewById(R.id.containerToday);
        containerNext7Days = view.findViewById(R.id.containerNext7Days);
        containerLater = view.findViewById(R.id.containerLater);
        emptyText = view.findViewById(R.id.emptyText);
        headerToday = view.findViewById(R.id.headerToday);
        headerWeek = view.findViewById(R.id.headerWeek);
        headerLater = view.findViewById(R.id.headerLater);
        toggleToday = view.findViewById(R.id.toggleToday);
        toggleWeek = view.findViewById(R.id.toggleWeek);
        toggleLater = view.findViewById(R.id.toggleLater);

        // Today should be expanded by default; week and later collapsed (handled by layout). Hook toggles.
        if (headerToday != null && sectionToday != null) {
            headerToday.setOnClickListener(v -> {
                if (sectionToday.getVisibility() == View.VISIBLE) {
                    sectionToday.setVisibility(View.GONE);
                    if (toggleToday != null) toggleToday.setRotation(0f);
                } else {
                    sectionToday.setVisibility(View.VISIBLE);
                    if (toggleToday != null) toggleToday.setRotation(90f);
                }
            });
        }
        if (headerWeek != null && sectionNext7Days != null) {
            headerWeek.setOnClickListener(v -> {
                if (sectionNext7Days.getVisibility() == View.VISIBLE) {
                    sectionNext7Days.setVisibility(View.GONE);
                    if (toggleWeek != null) toggleWeek.setRotation(0f);
                } else {
                    sectionNext7Days.setVisibility(View.VISIBLE);
                    if (toggleWeek != null) toggleWeek.setRotation(90f);
                }
            });
        }
        if (headerLater != null && sectionLater != null) {
            headerLater.setOnClickListener(v -> {
                if (sectionLater.getVisibility() == View.VISIBLE) {
                    sectionLater.setVisibility(View.GONE);
                    if (toggleLater != null) toggleLater.setRotation(0f);
                } else {
                    sectionLater.setVisibility(View.VISIBLE);
                    if (toggleLater != null) toggleLater.setRotation(90f);
                }
            });
        }

        if (toggleToday != null) {
            toggleToday.setRotation(90f); // expanded by default
        }
        if (toggleWeek != null) {
            toggleWeek.setRotation(0f);
        }
        if (toggleLater != null) {
            toggleLater.setRotation(0f);
        }

        // Ensure only Today is visible by default
        if (sectionToday != null) sectionToday.setVisibility(View.VISIBLE);
        if (sectionNext7Days != null) sectionNext7Days.setVisibility(View.GONE);
        if (sectionLater != null) sectionLater.setVisibility(View.GONE);

        loadUpcomingCare();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcomingCare();
    }

    public void refreshReminders() {
        loadUpcomingCare();
    }

    private final android.content.BroadcastReceiver remindersUpdatedReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, android.content.Intent intent) {
            // refresh UI when reminders change
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> loadUpcomingCare());
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        try {
            android.content.IntentFilter filter = new android.content.IntentFilter(com.example.plantastic.notifications.CareReminderScheduler.ACTION_REMINDERS_UPDATED);
            // Use ContextCompat to handle RECEIVER_EXPORTED/NOT_EXPORTED flag requirements on Android 14+ (API 34)
            ContextCompat.registerReceiver(requireContext(), remindersUpdatedReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        } catch (Exception ignore) {}
    }

    @Override
    public void onStop() {
        try {
            if (getActivity() != null) {
                getActivity().unregisterReceiver(remindersUpdatedReceiver);
            }
        } catch (Exception ignore) {}
        super.onStop();
    }

    private void loadUpcomingCare() {
        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                long today_end = getEndOfDay(now);
                long next7days_end = now + (7L * 24 * 60 * 60 * 1000);

                // Fetch all upcoming notifications (include slight past buffer so items due "now" aren't missed)
                List<Teade> upcoming = db.teadeDao().getUpcoming(now - 1000);

                // Separate into time buckets: today, this week, and later
                List<Teade> today = new ArrayList<>();
                List<Teade> next7days = new ArrayList<>();
                List<Teade> later = new ArrayList<>();
                Map<Integer, TaimWithDetails> plantDetailsById = new HashMap<>();
                Map<Integer, String> careTypeNamesById = new HashMap<>();

                if (upcoming != null) {
                    Set<Integer> fetchedPlantIds = new HashSet<>();
                    Set<Integer> fetchedCareTypeIds = new HashSet<>();
                    for (Teade teade : upcoming) {
                        if (teade == null) {
                            continue;
                        }
                        if (teade != null && fetchedPlantIds.add(teade.taim_id)) {
                            try {
                                plantDetailsById.put(teade.taim_id, db.taimDao().getWithDetailsById(teade.taim_id));
                            } catch (Exception ignore) {
                                plantDetailsById.put(teade.taim_id, null);
                            }
                        }

                        if (teade.hooldusTüüp_id != null && fetchedCareTypeIds.add(teade.hooldusTüüp_id)) {
                            try {
                                com.example.plantastic.data.entities.HooldusTüüp type = db.hooldusTüüpDao().getById(teade.hooldusTüüp_id);
                                if (type != null && type.nimetus != null && !type.nimetus.trim().isEmpty()) {
                                    careTypeNamesById.put(teade.hooldusTüüp_id, type.nimetus);
                                } else {
                                    careTypeNamesById.put(teade.hooldusTüüp_id, "Kastmine");
                                }
                            } catch (Exception ignore) {
                                careTypeNamesById.put(teade.hooldusTüüp_id, "Kastmine");
                            }
                        }

                        if (teade.aeg <= today_end) {
                            today.add(teade);
                        } else if (teade.aeg <= next7days_end) {
                            next7days.add(teade);
                        } else {
                            later.add(teade);
                        }
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (containerToday != null) containerToday.removeAllViews();
                        if (containerNext7Days != null) containerNext7Days.removeAllViews();
                        if (containerLater != null) containerLater.removeAllViews();
                        sectionToday.setVisibility(View.GONE);
                        sectionNext7Days.setVisibility(View.GONE);
                        sectionLater.setVisibility(View.GONE);

                        boolean hasAny = false;
                        
                        if (!today.isEmpty()) {
                            sectionToday.setVisibility(View.VISIBLE);
                            populateContainer(containerToday, today, plantDetailsById, careTypeNamesById);
                            hasAny = true;
                        }

                        if (!next7days.isEmpty()) {
                            sectionNext7Days.setVisibility(View.VISIBLE);
                            populateContainer(containerNext7Days, next7days, plantDetailsById, careTypeNamesById);
                            hasAny = true;
                        }

                        if (!later.isEmpty()) {
                            sectionLater.setVisibility(View.VISIBLE);
                            populateContainer(containerLater, later, plantDetailsById, careTypeNamesById);
                            hasAny = true;
                        }

                        emptyText.setVisibility(hasAny ? View.GONE : View.VISIBLE);
                    });
                }
            } catch (Exception ex) {
                // Log or ignore
            }
        });
    }

    private void populateContainer(LinearLayout container, List<Teade> teades, Map<Integer, TaimWithDetails> plantDetailsById, Map<Integer, String> careTypeNamesById) {
        container.removeAllViews();
        
        for (Teade teade : teades) {
            View card = createCareCard(teade,
                    plantDetailsById != null ? plantDetailsById.get(teade.taim_id) : null,
                    careTypeNamesById != null ? careTypeNamesById.get(teade.hooldusTüüp_id) : null,
                    container);
            container.addView(card);
        }
    }

    private View createCareCard(Teade teade, TaimWithDetails twd, String careTypeName, ViewGroup parent) {
        // Inflate reusable plant card and adapt for notification usage
        View card = getLayoutInflater().inflate(R.layout.my_plant_card, parent, false);

        // Add extra margin for Home spacing (convert dp to px)
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        final int marginDp = 24;
        final float density = getResources().getDisplayMetrics().density;
        int marginPx = (int) (marginDp * density + 0.5f);
        lp.setMargins(0, 0, 0, marginPx);
        card.setLayoutParams(lp);

        // Bind basic views
        android.widget.ImageView img = card.findViewById(R.id.selectedPlantImage);
        android.widget.TextView nickname = card.findViewById(R.id.nickname);
        android.widget.TextView common = card.findViewById(R.id.common_name);
        android.widget.TextView careTypeTv = card.findViewById(R.id.care_type);
        android.widget.Button kastaBtn = card.findViewById(R.id.kastaButton);
        android.widget.ImageView edit = card.findViewById(R.id.edit);
        android.widget.ImageView del = card.findViewById(R.id.delete);
        android.widget.ImageView drop = card.findViewById(R.id.drop);

        String nickText = resolvePlantDisplayName(twd, teade);
        nickname.setText(nickText);
        nickname.setVisibility(View.VISIBLE);

        // The home notification card should only show the plant's image and name.
        common.setVisibility(View.GONE);

        // Load plant image if available, otherwise show default icon
        boolean loadedImage = false;
        if (twd != null && twd.fotos != null && !twd.fotos.isEmpty()) {
            try {
                String fotoPath = twd.fotos.get(0).foto;
                if (!fotoPath.trim().isEmpty()) {
                    com.bumptech.glide.Glide.with(card.getContext())
                            .load(fotoPath)
                            .centerCrop()
                            .into(img);
                    img.setVisibility(View.VISIBLE);
                    loadedImage = true;
                }
            } catch (Exception ignore) {
            }
        }
        if (!loadedImage) {
            // keep a pleasant default so cards don't look empty
            img.setImageResource(R.drawable.ic_flower);
            img.setVisibility(View.VISIBLE);
        }

        // Show care type and date
        String careType = careTypeName != null && !careTypeName.trim().isEmpty() ? careTypeName : "Kastmine";
        String dateStr = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()).format(new Date(teade.aeg));
        careTypeTv.setText(String.format(Locale.getDefault(), "%s - %s", careType, dateStr));
        careTypeTv.setVisibility(View.VISIBLE);

        // Hide edit/delete icons for notification presentation
        if (edit != null) edit.setVisibility(View.GONE);
        if (del != null) del.setVisibility(View.GONE);
        if (drop != null) drop.setVisibility(View.GONE);

        // Show kasta button and wire it to mark as watered and reschedule
        if (kastaBtn != null) {
            kastaBtn.setVisibility(View.VISIBLE);
            kastaBtn.setOnClickListener(v -> {
                kastaBtn.setEnabled(false);
                new Thread(() -> {
                    try {
                        CareActionHandler.handleKasta(requireContext(), teade.taim_id, teade.hooldusTüüp_id);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                loadUpcomingCare();
                                kastaBtn.setEnabled(true);
                                View root = getView();
                                if (root != null) {
                                    root.postDelayed(() -> {
                                        if (isAdded()) {
                                            loadUpcomingCare();
                                        }
                                    }, 250L);
                                }
                            });
                        }
                    } catch (Exception ex) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> kastaBtn.setEnabled(true));
                        }
                    }
                }).start();
            });
        }

        // Clicking free space opens plant detail
        card.setOnClickListener(v -> openPlantDetail(teade.taim_id));

        return card;
    }

    private String resolvePlantDisplayName(TaimWithDetails twd, Teade teade) {
        if (twd != null) {
            if (twd.taim != null && twd.taim.nimi != null && !twd.taim.nimi.trim().isEmpty()) {
                return twd.taim.nimi;
            }
            if (twd.sort != null && twd.sort.nimetus != null && !twd.sort.nimetus.trim().isEmpty()) {
                return twd.sort.nimetus;
            }
        }
        return String.format(Locale.getDefault(), "Taim #%d", teade.taim_id);
    }

    private long getEndOfDay(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }

    private void openPlantDetail(int plantId) {
        // Navigate to MyPlantFragment with the plantId
        Bundle args = new Bundle();
        args.putInt("plantId", plantId);
        MyPlantFragment fragment = new MyPlantFragment();
        fragment.setArguments(args);

        getParentFragmentManager().beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit();
    }
}

package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Teade;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {
    private PlantasticDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private LinearLayout sectionToday, sectionNext7Days;
    private LinearLayout containerToday, containerNext7Days;
    private TextView emptyText;
    private View headerToday, headerWeek;
    private TextView toggleToday, toggleWeek;

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
        // Runtime-safe lookup for these container IDs (works even if generated R is inconsistent)
        int containerTodayId = view.getResources().getIdentifier("containerToday", "id", requireContext().getPackageName());
        int containerNext7DaysId = view.getResources().getIdentifier("containerNext7Days", "id", requireContext().getPackageName());
        containerToday = containerTodayId != 0 ? view.findViewById(containerTodayId) : null;
        containerNext7Days = containerNext7DaysId != 0 ? view.findViewById(containerNext7DaysId) : null;
        emptyText = view.findViewById(R.id.emptyText);
        headerToday = view.findViewById(R.id.headerToday);
        headerWeek = view.findViewById(R.id.headerWeek);
        toggleToday = view.findViewById(R.id.toggleToday);
        toggleWeek = view.findViewById(R.id.toggleWeek);

        // Today should be expanded by default; week collapsed (handled by layout). Hook toggles.
        if (headerToday != null && sectionToday != null) {
            headerToday.setOnClickListener(v -> {
                if (sectionToday.getVisibility() == View.VISIBLE) {
                    sectionToday.setVisibility(View.GONE);
                    if (toggleToday != null) toggleToday.setText("▸");
                } else {
                    sectionToday.setVisibility(View.VISIBLE);
                    if (toggleToday != null) toggleToday.setText("▾");
                }
            });
        }
        if (headerWeek != null && sectionNext7Days != null) {
            headerWeek.setOnClickListener(v -> {
                if (sectionNext7Days.getVisibility() == View.VISIBLE) {
                    sectionNext7Days.setVisibility(View.GONE);
                    if (toggleWeek != null) toggleWeek.setText("▸");
                } else {
                    sectionNext7Days.setVisibility(View.VISIBLE);
                    if (toggleWeek != null) toggleWeek.setText("▾");
                }
            });
        }

        loadUpcomingCare();
    }

    private void loadUpcomingCare() {
        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                long today_end = getEndOfDay(now);
                long next7days_end = now + (7L * 24 * 60 * 60 * 1000);

                // Fetch all upcoming notifications (include slight past buffer so items due "now" aren't missed)
                List<Teade> upcoming = db.teadeDao().getUpcoming(now - 1000);

                // Separate into time buckets: today and this week
                List<Teade> today = new ArrayList<>();
                List<Teade> next7days = new ArrayList<>();

                if (upcoming != null) {
                    for (Teade teade : upcoming) {
                        if (teade.aeg <= today_end) {
                            today.add(teade);
                        } else if (teade.aeg <= next7days_end) {
                            next7days.add(teade);
                        }
                    }
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        boolean hasAny = false;
                        
                        if (!today.isEmpty()) {
                            sectionToday.setVisibility(View.VISIBLE);
                            populateContainer(containerToday, today);
                            hasAny = true;
                        }

                        if (!next7days.isEmpty()) {
                            sectionNext7Days.setVisibility(View.VISIBLE);
                            populateContainer(containerNext7Days, next7days);
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

    private void populateContainer(LinearLayout container, List<Teade> teades) {
        container.removeAllViews();
        
        for (Teade teade : teades) {
            View card = createCareCard(teade);
            container.addView(card);
        }
    }

    private View createCareCard(Teade teade) {
        // Inflate reusable plant card and adapt for notification usage
        View card = getLayoutInflater().inflate(R.layout.my_plant_card, null);

        // Bind basic views
        android.widget.ImageView img = card.findViewById(R.id.selectedPlantImage);
        android.widget.TextView nickname = card.findViewById(R.id.nickname);
        android.widget.TextView common = card.findViewById(R.id.common_name);
        android.widget.TextView careTypeTv = card.findViewById(R.id.care_type);
        android.widget.Button kastaBtn = card.findViewById(R.id.kastaButton);
        android.widget.ImageView edit = card.findViewById(R.id.edit);
        android.widget.ImageView del = card.findViewById(R.id.delete);
        android.widget.ImageView drop = card.findViewById(R.id.drop);

        // Populate plant info
        String plantName = getPlantNameForTeade(teade);
        nickname.setText(plantName != null ? plantName : "Unknown Plant");

        String sortName = "";
        try {
            com.example.plantastic.data.entities.TaimWithDetails twd = db.taimDao().getWithDetailsById(teade.taim_id);
            if (twd != null && twd.sort != null) sortName = twd.sort.nimetus != null ? twd.sort.nimetus : "";
            if (twd != null && twd.fotos != null && !twd.fotos.isEmpty()) {
                com.bumptech.glide.Glide.with(card).load(twd.fotos.get(0).foto).into(img);
            } else {
                img.setImageResource(R.drawable.ic_flower);
            }
        } catch (Exception ex) {
            img.setImageResource(R.drawable.ic_flower);
        }

        if (sortName != null && !sortName.isEmpty()) {
            common.setText(sortName);
            common.setVisibility(View.VISIBLE);
        } else {
            common.setVisibility(View.GONE);
        }

        // Show care type and date
        String careType = getCareTypeName(teade.hooldusTüüp_id);
        String dateStr = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(teade.aeg));
        careTypeTv.setText(careType + " - " + dateStr);
        careTypeTv.setVisibility(View.VISIBLE);

        // Hide edit/delete/drop icons for notification presentation
        if (edit != null) edit.setVisibility(View.GONE);
        if (del != null) del.setVisibility(View.GONE);
        if (drop != null) drop.setVisibility(View.GONE);

        // Show kasta button and wire it to mark as watered and reschedule
        if (kastaBtn != null) {
            kastaBtn.setVisibility(View.VISIBLE);
            kastaBtn.setOnClickListener(v -> {
                // perform watering similar to other fragments
                new Thread(() -> {
                    try {
                        // get or create kastmine type
                        com.example.plantastic.data.entities.HooldusTüüp kast = db.hooldusTüüpDao().getByName("Kastmine");
                        if (kast == null) {
                            kast = new com.example.plantastic.data.entities.HooldusTüüp();
                            kast.nimetus = "Kastmine";
                            long id = db.hooldusTüüpDao().insert(kast);
                            kast.id = (int) id;
                        }

                        com.example.plantastic.data.entities.HooldusAjalugu hist = new com.example.plantastic.data.entities.HooldusAjalugu();
                        hist.taim_id = teade.taim_id;
                        hist.hooldusTüüp_id = kast.id;
                        hist.aeg = System.currentTimeMillis();
                        hist.kommentaar = "Kastetud (via notification)";
                        db.hooldusAjaluguDao().insert(hist);

                        // reschedule next notification based on plant sort
                        com.example.plantastic.data.entities.TaimWithDetails twd2 = db.taimDao().getWithDetailsById(teade.taim_id);
                        if (twd2 != null && twd2.sort != null) {
                            long interval = getIntervalMillisFromWateringIntensity(twd2.sort.kastmisvajadus);
                            long next = System.currentTimeMillis() + interval;

                            com.example.plantastic.data.entities.Teade t = db.teadeDao().getByTaimAndType(teade.taim_id, kast.id);
                            if (t == null) {
                                t = new com.example.plantastic.data.entities.Teade();
                                t.taim_id = teade.taim_id;
                                t.hooldusTüüp_id = kast.id;
                                t.aeg = next;
                                t.kommentaar = "Watering reminder";
                                db.teadeDao().insert(t);
                            } else {
                                t.aeg = next;
                                db.teadeDao().update(t);
                            }
                        }

                        // Refresh UI
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(this::loadUpcomingCare);
                        }
                    } catch (Exception ex) {
                        // ignore
                    }
                }).start();
            });
        }

        // Clicking free space opens plant detail
        card.setOnClickListener(v -> openPlantDetail(teade.taim_id));

        return card;
    }

    private long getIntervalMillisFromWateringIntensity(int intensity) {
        switch (intensity) {
            case 0: return 365L * 24 * 60 * 60 * 1000; // yearly
            case 1: return 30L * 24 * 60 * 60 * 1000;  // monthly
            case 2: return 14L * 24 * 60 * 60 * 1000;  // bi-weekly
            case 3: return 7L * 24 * 60 * 60 * 1000;   // weekly
            case 4: return 30L * 1000;                 // testing - 30 seconds
            default: return 14L * 24 * 60 * 60 * 1000;
        }
    }

    private String getPlantNameForTeade(Teade teade) {
        try {
            com.example.plantastic.data.entities.Taim taim = db.taimDao().getById(teade.taim_id);
            return taim != null ? taim.nimi : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCareTypeName(Integer typeId) {
        if (typeId == null) return "Care";
        try {
            com.example.plantastic.data.entities.HooldusTüüp type = db.hooldusTüüpDao().getById(typeId);
            return type != null ? type.nimetus : "Care";
        } catch (Exception e) {
            return "Care";
        }
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
        
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}

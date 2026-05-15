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

    private LinearLayout sectionToday, sectionNext3Days, sectionNext7Days;
    private LinearLayout containerToday, containerNext3Days, containerNext7Days;
    private TextView emptyText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = PlantasticDatabase.getInstance(requireContext());

        sectionToday = view.findViewById(R.id.sectionToday);
        sectionNext3Days = view.findViewById(R.id.sectionNext3Days);
        sectionNext7Days = view.findViewById(R.id.sectionNext7Days);
        containerToday = view.findViewById(R.id.containerToday);
        containerNext3Days = view.findViewById(R.id.containerNext3Days);
        containerNext7Days = view.findViewById(R.id.containerNext7Days);
        emptyText = view.findViewById(R.id.emptyText);

        loadUpcomingCare();
    }

    private void loadUpcomingCare() {
        executorService.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                long today_end = getEndOfDay(now);
                long next3days_end = now + (3L * 24 * 60 * 60 * 1000);
                long next7days_end = now + (7L * 24 * 60 * 60 * 1000);

                // Fetch all upcoming notifications
                List<Teade> upcoming = db.teadeDao().getUpcoming(now);

                // Separate into time buckets
                List<Teade> today = new ArrayList<>();
                List<Teade> next3days = new ArrayList<>();
                List<Teade> next7days = new ArrayList<>();

                if (upcoming != null) {
                    for (Teade teade : upcoming) {
                        if (teade.aeg <= today_end) {
                            today.add(teade);
                        } else if (teade.aeg <= next3days_end) {
                            next3days.add(teade);
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

                        if (!next3days.isEmpty()) {
                            sectionNext3Days.setVisibility(View.VISIBLE);
                            populateContainer(containerNext3Days, next3days);
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
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 12, 12, 12);
        card.setBackgroundResource(R.drawable.search_view_bg);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 8);
        card.setLayoutParams(cardParams);

        // Plant name
        TextView plantNameText = new TextView(requireContext());
        plantNameText.setTextSize(16);
        plantNameText.setTextColor(requireContext().getColor(R.color.black));
        plantNameText.setTypeface(null, android.graphics.Typeface.BOLD);

        String plantName = getPlantNameForTeade(teade);
        plantNameText.setText(plantName != null ? plantName : "Unknown Plant");
        card.addView(plantNameText);

        // Care type and date
        TextView careText = new TextView(requireContext());
        careText.setTextSize(14);
        careText.setTextColor(requireContext().getColor(R.color.black));
        careText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        careText.setPadding(0, 8, 0, 0);

        String careType = getCareTypeName(teade.hooldusTüüp_id);
        String dateStr = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(teade.aeg));
        careText.setText(careType + " - " + dateStr);
        card.addView(careText);

        // Click listener to open plant detail
        card.setOnClickListener(v -> openPlantDetail(teade.taim_id));

        return card;
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

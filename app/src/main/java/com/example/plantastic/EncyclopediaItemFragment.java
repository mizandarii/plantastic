package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.plantastic.api.PerenualService;
import com.example.plantastic.api.PlantResponse;
import com.example.plantastic.api.PlantCareGuideResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EncyclopediaItemFragment extends Fragment {

    private static final String ARG_PLANT = "arg_plant";
    private static final String BASE_URL = "https://perenual.com/";
    private PlantResponse.PlantData plant;

    public static EncyclopediaItemFragment newInstance(PlantResponse.PlantData plant) {
        EncyclopediaItemFragment fragment = new EncyclopediaItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = (PlantResponse.PlantData) getArguments().getSerializable(ARG_PLANT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.encyclopedia_plant_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (plant == null) return;

        ImageView backButton = view.findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        ImageView plantImage = view.findViewById(R.id.plantImage);
        TextView commonNameText = view.findViewById(R.id.commonNameText);
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView careLevelText = view.findViewById(R.id.careLevel);
        TextView toxicToPetsText = view.findViewById(R.id.toxicToPets);

        commonNameText.setText(plant.getCommonName());
        if (plant.getScientificName() != null && !plant.getScientificName().isEmpty()) {
            descriptionText.setText("Scientific name: " + plant.getScientificName().get(0));
        }

        careLevelText.setText(plant.getCareLevel());
        toxicToPetsText.setText(plant.getPoisonousToPetsText());

        if (plant.getCareGuides() != null && !plant.getCareGuides().isEmpty()) {
            loadCareGuide(plant.getCareGuides(), descriptionText);
        }

        if (plant.getDefaultImage() != null) {
            Glide.with(this)
                    .load(plant.getDefaultImage().getThumbnail())
                    .placeholder(R.drawable.plant)
                    .into(plantImage);
        }

        // Sunlight level logic
        int level = plant.getSunlightLevel();
        ImageView sun1 = view.findViewById(R.id.sun1);
        ImageView sun2 = view.findViewById(R.id.sun2);
        ImageView sun3 = view.findViewById(R.id.sun3);
        ImageView sun4 = view.findViewById(R.id.sun4);

        if (sun1 != null) sun1.setAlpha(level >= 1 ? 1.0f : 0.3f);
        if (sun2 != null) sun2.setAlpha(level >= 2 ? 1.0f : 0.3f);
        if (sun3 != null) sun3.setAlpha(level >= 3 ? 1.0f : 0.3f);
        if (sun4 != null) sun4.setAlpha(level >= 4 ? 1.0f : 0.3f);

        // Watering logic (3 drops in the updated XML)
        int waterLevel = mapWateringToLevel(plant.getWatering());
        ImageView drop1 = view.findViewById(R.id.drop1);
        ImageView drop2 = view.findViewById(R.id.drop2);
        ImageView drop3 = view.findViewById(R.id.drop3);

        if (drop1 != null) drop1.setAlpha(waterLevel >= 1 ? 1.0f : 0.3f);
        if (drop2 != null) drop2.setAlpha(waterLevel >= 2 ? 1.0f : 0.3f);
        if (drop3 != null) drop3.setAlpha(waterLevel >= 3 ? 1.0f : 0.3f);
    }

    private void loadCareGuide(String url, TextView descriptionText) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        PerenualService service = retrofit.create(PerenualService.class);
        service.getCareGuide(url).enqueue(new Callback<PlantCareGuideResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlantCareGuideResponse> call, @NonNull Response<PlantCareGuideResponse> response) {
                if (!response.isSuccessful() || response.body() == null) return;

                List<PlantCareGuideResponse.CareGuideItem> guides = response.body().getData();
                if (guides.isEmpty()) return;

                PlantCareGuideResponse.CareGuideItem guide = guides.get(0);
                List<PlantCareGuideResponse.CareGuideSection> sections = guide.getSection();
                if (sections.isEmpty()) return;

                List<String> parts = new ArrayList<>();
                for (PlantCareGuideResponse.CareGuideSection section : sections) {
                    String type = section.getType();
                    String description = section.getDescription();
                    if (!type.isEmpty() && !description.isEmpty()) {
                        parts.add(type.substring(0, 1).toUpperCase() + type.substring(1) + ": " + description);
                    }
                }

                if (!parts.isEmpty() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> descriptionText.setText(String.join("\n\n", parts)));
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlantCareGuideResponse> call, @NonNull Throwable t) {
                // Keep existing fallback text if the care guide fetch fails.
            }
        });
    }

    private int mapWateringToLevel(String watering) {
        if (watering == null) return 3;
        switch (watering.toLowerCase()) {
            case "none": return 0;
            case "minimum": return 1;
            case "average": return 2;
            case "frequent": return 3;
            default: return 2;
        }
    }
}

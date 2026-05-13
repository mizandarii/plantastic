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
import com.example.plantastic.api.PlantResponse;

public class EncyclopediaItemFragment extends Fragment {

    private static final String ARG_PLANT = "arg_plant";
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
        TextView originText = view.findViewById(R.id.originText);

        commonNameText.setText(plant.getCommonName());
        
        // Use scientific name as description or family if needed, or if there's a real description in API (not in this version of PlantData)
        if (plant.getScientificName() != null && !plant.getScientificName().isEmpty()) {
            descriptionText.setText("Scientific name: " + plant.getScientificName().get(0));
        }

        originText.setText(plant.getFamily());

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

        // Watering logic (assuming 1-5 scale for drops as in XML)
        int waterLevel = mapWateringToLevel(plant.getWatering());
        ImageView drop1 = view.findViewById(R.id.drop1);
        ImageView drop2 = view.findViewById(R.id.drop2);
        ImageView drop3 = view.findViewById(R.id.drop3);
        ImageView drop4 = view.findViewById(R.id.drop4);
        ImageView drop5 = view.findViewById(R.id.drop5);

        if (drop1 != null) drop1.setAlpha(waterLevel >= 1 ? 1.0f : 0.3f);
        if (drop2 != null) drop2.setAlpha(waterLevel >= 2 ? 1.0f : 0.3f);
        if (drop3 != null) drop3.setAlpha(waterLevel >= 3 ? 1.0f : 0.3f);
        if (drop4 != null) drop4.setAlpha(waterLevel >= 4 ? 1.0f : 0.3f);
        if (drop5 != null) drop5.setAlpha(waterLevel >= 5 ? 1.0f : 0.3f);
    }

    private int mapWateringToLevel(String watering) {
        if (watering == null) return 3;
        switch (watering.toLowerCase()) {
            case "none": return 0;
            case "minimum": return 1;
            case "average": return 3;
            case "frequent": return 5;
            default: return 3;
        }
    }
}

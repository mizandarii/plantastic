package com.example.plantastic;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.plantastic.api.PerenualClient;
import com.example.plantastic.api.PerenualService;
import com.example.plantastic.api.PlantResponse;
import com.example.plantastic.api.PlantCareGuideResponse;
import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.LemmikTaim;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EncyclopediaItemFragment extends Fragment {

    private static final String ARG_PLANT = "arg_plant";
    private static final String ARG_PLANT_ID = "arg_plant_id";
    private static final String BASE_URL = "https://perenual.com/api/";
    private PlantResponse.PlantData plant;
    private int requestedPlantId = -1;
    private String apiKey;
    private PerenualService apiService;
    private PlantasticDatabase db;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    private ImageButton favoriteButton;
    private boolean isFavorite;

    public static EncyclopediaItemFragment newInstance(PlantResponse.PlantData plant) {
        EncyclopediaItemFragment fragment = new EncyclopediaItemFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PLANT, plant);
        fragment.setArguments(args);
        return fragment;
    }

    public static EncyclopediaItemFragment newInstanceById(int plantId) {
        EncyclopediaItemFragment fragment = new EncyclopediaItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PLANT_ID, plantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            plant = (PlantResponse.PlantData) getArguments().getSerializable(ARG_PLANT);
            requestedPlantId = getArguments().getInt(ARG_PLANT_ID, -1);
            if (plant != null) {
                requestedPlantId = plant.getId();
            }
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

        if (plant == null && requestedPlantId <= 0) return;

        ImageView backButton = view.findViewById(R.id.backButton);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        db = PlantasticDatabase.getInstance(requireContext());

        if (backButton != null) {
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        setupRetrofit();
        loadApiKey();
        loadFavoriteState();

        if (favoriteButton != null) {
            favoriteButton.setOnClickListener(v -> toggleFavorite());
        }

        // Render whatever we already have immediately, then upgrade to the full detail record.
        if (plant != null) {
            bindPlantData(view, plant);
        }

        if (apiKey != null && !apiKey.isEmpty() && apiService != null && requestedPlantId > 0) {
            apiService.getSpeciesDetails(requestedPlantId, apiKey).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<PlantResponse.PlantData> call, @NonNull Response<PlantResponse.PlantData> response) {
                    if (!isAdded() || response.body() == null || !response.isSuccessful()) return;
                    plant = response.body();
                    requestedPlantId = plant.getId();
                    bindPlantData(view, plant);
                }

                @Override
                public void onFailure(@NonNull Call<PlantResponse.PlantData> call, @NonNull Throwable t) {
                    Log.e("ENCYCLOPEDIA_ITEM", "Failed to load species details: " + t.getMessage());
                }
            });
        }
    }

    private void setupRetrofit() {
        apiService = PerenualClient.getService();
    }

    private void loadApiKey() {
        try {
            ApplicationInfo ai = requireContext().getPackageManager().getApplicationInfo(
                    requireContext().getPackageName(), PackageManager.GET_META_DATA);
            apiKey = ai.metaData.getString("perenual_api_key");
            if (apiKey != null) {
                apiKey = apiKey.replace("\"", "").trim();
            }
        } catch (Exception e) {
            Log.e("ENCYCLOPEDIA_ITEM", "Failed to load API key: " + e.getMessage());
            Toast.makeText(getContext(), "API key missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void bindPlantData(View view, PlantResponse.PlantData data) {
        if (data == null) return;

        ImageView plantImage = view.findViewById(R.id.plantImage);
        TextView commonNameText = view.findViewById(R.id.commonNameText);
        TextView scientificNameText = view.findViewById(R.id.scientificNameText);
        TextView descriptionText = view.findViewById(R.id.descriptionText);
        TextView careLevelText = view.findViewById(R.id.careLevel);
        TextView toxicToPetsText = view.findViewById(R.id.toxicToPets);

        commonNameText.setText(data.getCommonName());

        List<String> scientificNames = data.getScientificName();
        scientificNameText.setText(scientificNames.isEmpty() ? "Unknown" : scientificNames.get(0));

        String description = data.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            descriptionText.setText(description);
        } else {
            descriptionText.setText("Description unavailable.");
        }

        careLevelText.setText(data.getCareLevel());
        toxicToPetsText.setText(data.getPoisonousToPetsText());

        if (data.getDefaultImage() != null && data.getDefaultImage().getThumbnail() != null && !data.getDefaultImage().getThumbnail().isEmpty()) {
            Glide.with(this)
                    .load(data.getDefaultImage().getThumbnail())
                    .placeholder(R.drawable.plant)
                    .into(plantImage);
        }

        int level = data.getSunlightLevel();
        ImageView sun1 = view.findViewById(R.id.sun1);
        ImageView sun2 = view.findViewById(R.id.sun2);
        ImageView sun3 = view.findViewById(R.id.sun3);
        ImageView sun4 = view.findViewById(R.id.sun4);

        if (sun1 != null) sun1.setAlpha(level >= 1 ? 1.0f : 0.3f);
        if (sun2 != null) sun2.setAlpha(level >= 2 ? 1.0f : 0.3f);
        if (sun3 != null) sun3.setAlpha(level >= 3 ? 1.0f : 0.3f);
        if (sun4 != null) sun4.setAlpha(level >= 4 ? 1.0f : 0.3f);

        int waterLevel = data.getWateringLevel();
        ImageView drop1 = view.findViewById(R.id.drop1);
        ImageView drop2 = view.findViewById(R.id.drop2);
        ImageView drop3 = view.findViewById(R.id.drop3);

        if (drop1 != null) drop1.setAlpha(waterLevel >= 1 ? 1.0f : 0.3f);
        if (drop2 != null) drop2.setAlpha(waterLevel >= 2 ? 1.0f : 0.3f);
        if (drop3 != null) drop3.setAlpha(waterLevel >= 3 ? 1.0f : 0.3f);
    }

    private void loadFavoriteState() {
        if (db == null || requestedPlantId <= 0) return;

        dbExecutor.execute(() -> {
            try {
                Kasutaja user = db.kasutajaDao().getFirstUser();
                if (user == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> setFavoriteUi(false));
                    }
                    return;
                }

                LemmikTaim favorite = db.lemmikTaimDao().getById(requestedPlantId);
                boolean isFavorited = favorite != null && favorite.kasutaja_id == user.id;

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> setFavoriteUi(isFavorited));
                }
            } catch (Exception ex) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> setFavoriteUi(false));
                }
            }
        });
    }

    private void toggleFavorite() {
        if (db == null || requestedPlantId <= 0) return;

        dbExecutor.execute(() -> {
            try {
                Kasutaja user = db.kasutajaDao().getFirstUser();
                if (user == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "No user found", Toast.LENGTH_SHORT).show()
                        );
                    }
                    return;
                }

                LemmikTaim existing = db.lemmikTaimDao().getById(requestedPlantId);
                boolean shouldBeFavorite;

                if (existing != null && existing.kasutaja_id == user.id) {
                    db.lemmikTaimDao().delete(existing);
                    shouldBeFavorite = false;
                } else {
                    if (existing != null) {
                        // Replace stale favorite row if it belongs to a different user.
                        db.lemmikTaimDao().delete(existing);
                    }

                    LemmikTaim favorite = new LemmikTaim();
                    favorite.api_taim_id = requestedPlantId;
                    favorite.nimetus = (plant != null && plant.getCommonName() != null) ? plant.getCommonName() : "Unknown";
                    favorite.img_url = (plant != null && plant.getDefaultImage() != null) ? plant.getDefaultImage().getThumbnail() : null;
                    favorite.kasutaja_id = user.id;
                    db.lemmikTaimDao().insert(favorite);
                    shouldBeFavorite = true;
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> setFavoriteUi(shouldBeFavorite));
                }
            } catch (Exception ex) {
                Log.e("ENCYCLOPEDIA_ITEM", "Failed to toggle favorite", ex);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Could not update favorites", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void setFavoriteUi(boolean favorited) {
        isFavorite = favorited;
        if (favoriteButton == null) return;
        favoriteButton.setImageResource(favorited ? R.drawable.ic_heart : R.drawable.ic_heart_outline);
    }

    private void loadCareGuide(String url, TextView descriptionText) {
        PerenualService service = PerenualClient.getService();
        service.getCareGuide(url).enqueue(new Callback<>() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        favoriteButton = null;
    }

}

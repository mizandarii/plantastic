package com.example.plantastic;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantastic.api.PerenualService;
import com.example.plantastic.api.PlantAdapter;
import com.example.plantastic.api.PlantResponse;
import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Fotod;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.TaimLiik;
import com.example.plantastic.data.entities.TaimSort;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class AddPlantFragment extends Fragment {

    private String apiKey;
    private static final String BASE_URL = "https://perenual.com/";

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ScrollView detailsForm;
    private ProgressBar progressBar;
    private PlantAdapter adapter;
    private PerenualService apiService;

    private ImageView selectedPlantImage;
    private TextInputEditText editNickname, editSpecies;
    private Button btnSavePlant, btnCancel, btnTakePhoto;

    private Uri selectedImageUri;
    private PlantResponse.PlantData selectedPlantData;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    selectedPlantImage.setImageURI(selectedImageUri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_plant_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadApiKey();
        setupRetrofit();
        setupRecyclerView();
        setupSearch();
    }

    private void initViews(View view) {
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.recyclerView);
        detailsForm = view.findViewById(R.id.detailsForm);
        progressBar = view.findViewById(R.id.progressBar);
        
        selectedPlantImage = view.findViewById(R.id.selectedPlantImage);
        editNickname = view.findViewById(R.id.editNickname);
        editSpecies = view.findViewById(R.id.editSpecies);
        btnSavePlant = view.findViewById(R.id.btnSavePlant);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);

        btnCancel.setOnClickListener(v -> showSearchList());
        btnTakePhoto.setOnClickListener(v -> openGallery());
        btnSavePlant.setOnClickListener(v -> savePlant());
    }

    private void loadApiKey() {
        try {
            ApplicationInfo ai = requireContext().getPackageManager().getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA);
            apiKey = ai.metaData.getString("perenual_api_key");
            
            // Clean key from possible quotes or spaces from local.properties
            if (apiKey != null) {
                apiKey = apiKey.replace("\"", "").trim();
            }

            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) {
                Log.e("SEARCH_DEBUG", "API Key Error: " + apiKey);
                Toast.makeText(getContext(), "API Key missing. Sync Gradle after editing local.properties", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("API_ERROR", "Failed to load meta-data: " + e.getMessage());
        }
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PerenualService.class);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlantAdapter(new ArrayList<>(), this::showDetailsForm);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        if (searchView == null) return;
        searchView.onActionViewExpanded(); 
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() > 0) {
                    searchPlants(query.trim());
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // Manual search only to save API limits.
                return true;
            }
        });
    }

    private void showDetailsForm(PlantResponse.PlantData plant) {
        selectedPlantData = plant;
        searchView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        detailsForm.setVisibility(View.VISIBLE);

        editNickname.setText(plant.getCommonName());
        editSpecies.setText(plant.getCommonName());

        if (plant.getDefaultImage() != null) {
            Glide.with(this)
                    .load(plant.getDefaultImage().getThumbnail())
                    .into(selectedPlantImage);
        }
    }

    private void showSearchList() {
        detailsForm.setVisibility(View.GONE);
        searchView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void savePlant() {
        String nickname = editNickname.getText().toString();
        if (nickname.isEmpty()) {
            Toast.makeText(getContext(), "Please give your plant a nickname", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                PlantasticDatabase db = PlantasticDatabase.getInstance(requireContext());
                Kasutaja user = db.kasutajaDao().getFirstUser();
                int userId = (user != null) ? user.id : (int) db.kasutajaDao().insert(new Kasutaja() {{ kasutajanimi = "Primary User"; }});

                String familyName = (selectedPlantData.getFamily() != null && !selectedPlantData.getFamily().isEmpty()) 
                        ? selectedPlantData.getFamily() : "General";
                TaimLiik liik = db.taimLiikDao().getByName(familyName);
                int liikId;
                if (liik == null) {
                    liik = new TaimLiik();
                    liik.nimetus = familyName;
                    liik.ladinakeelne_nimetus = "Plantae";
                    liikId = (int) db.taimLiikDao().insert(liik);
                } else {
                    liikId = liik.id;
                }

                int wateringIntensity = mapWateringToIntensity(selectedPlantData.getWatering());
                int sunlightValue = mapSunlightToValue(selectedPlantData.getSunlight());

                TaimSort sort = new TaimSort();
                sort.nimetus = selectedPlantData.getCommonName();
                List<String> scientificNames = selectedPlantData.getScientificName();
                sort.ladinakeelne_nimetus = (scientificNames != null && !scientificNames.isEmpty()) 
                        ? scientificNames.get(0) : "Unknown";
                sort.liik_id = liikId;
                sort.kastmisvajadus = wateringIntensity;
                sort.valgusnoudlikkus = sunlightValue;
                long sortId = db.taimSortDao().insert(sort);

                Taim taim = new Taim();
                taim.nimi = nickname;
                taim.sort_id = (int) sortId;
                taim.kasutaja_id = userId;
                long taimId = db.taimDao().insert(taim);

                if (selectedImageUri != null) {
                    Fotod foto = new Fotod();
                    foto.taim_id = (int) taimId;
                    foto.foto = selectedImageUri.toString();
                    db.fotodDao().insert(foto);
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Plant Saved Successfully!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack(); 
                });
            } catch (Exception e) {
                Log.e("DB_ERROR", "Failed to save: ", e);
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private int mapWateringToIntensity(String watering) {
        if (watering == null) return 2; 
        switch (watering.toLowerCase()) {
            case "none": return 0;
            case "minimum": return 1;
            case "average": return 2;
            case "frequent": return 3;
            default: return 2;
        }
    }

    private int mapSunlightToValue(List<String> sunlight) {
        if (sunlight == null || sunlight.isEmpty()) return 3;
        String sun = sunlight.get(0).toLowerCase();
        if (sun.contains("full_sun") || sun.contains("full sun")) return 5;
        if (sun.contains("sun-part_shade") || (sun.contains("part") && sun.contains("sun"))) return 4;
        if (sun.contains("part_shade") || sun.contains("part shade")) return 2;
        if (sun.contains("full_shade") || sun.contains("full shade")) return 1;
        return 3;
    }

    private void searchPlants(String query) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) return;

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        Log.d("SEARCH_DEBUG", "Starting search for: " + query);

        apiService.searchPlants(apiKey, query).enqueue(new Callback<PlantResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlantResponse> call, @NonNull Response<PlantResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    List<PlantResponse.PlantData> results = response.body().getData();
                    adapter.setPlants(results);
                    if (results.isEmpty()) {
                        Toast.makeText(getContext(), "No plants found for '" + query + "'", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int code = response.code();
                    if (code == 429) {
                        Toast.makeText(getContext(), "API Limit Reached (100 calls). Wait until tomorrow.", Toast.LENGTH_LONG).show();
                    } else if (code == 401 || code == 403) {
                        Toast.makeText(getContext(), "Invalid API Key. Check local.properties.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Search failed: " + code, Toast.LENGTH_SHORT).show();
                    }
                    Log.e("SEARCH_ERROR", "Status: " + code + " Error: " + response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<PlantResponse> call, @NonNull Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (isAdded()) {
                    Log.e("SEARCH_ERROR", "Network failure: " + t.getMessage());
                    Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

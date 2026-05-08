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
import com.example.plantastic.data.entities.KastmisVajadusIntervall;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.TaimLiik;
import com.example.plantastic.data.entities.TaimSort;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

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
            Log.i("SEARCH_DEBUG", "API Key loaded: " + (apiKey != null ? "YES" : "NO"));
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

        searchView.setIconifiedByDefault(false);
        searchView.onActionViewExpanded(); 
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.i("SEARCH_DEBUG", "Query Submit: " + query);
                searchPlants(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() >= 1) { // Trigger earlier for testing
                    Log.i("SEARCH_DEBUG", "Query Change: " + newText);
                    searchPlants(newText);
                }
                return true;
            }
        });
    }

    private void showDetailsForm(PlantResponse.PlantData plant) {
        selectedPlantData = plant;
        searchView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
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
                int userId = (user == null) ? (int) db.kasutajaDao().insert(new Kasutaja() {{ kasutajanimi = "Primary User"; }}) : user.id;

                TaimLiik liik = db.taimLiikDao().getFirstLiik();
                int liikId = (liik == null) ? (int) db.taimLiikDao().insert(new TaimLiik() {{ nimetus = "General"; ladinakeelne_nimetus = "Plantae"; }}) : liik.id;

                KastmisVajadusIntervall interval = db.kastmisVajadusIntervallDao().getFirstInterval();
                int intervalId = (interval == null) ? (int) db.kastmisVajadusIntervallDao().insert(new KastmisVajadusIntervall() {{ paevad = 7; }}) : interval.id;

                TaimSort sort = new TaimSort();
                sort.nimetus = selectedPlantData.getCommonName();
                sort.ladinakeelne_nimetus = (selectedPlantData.getScientificName() != null && !selectedPlantData.getScientificName().isEmpty()) 
                        ? selectedPlantData.getScientificName().get(0) : "Unknown";
                sort.liik_id = liikId;
                sort.kastmisvajadus = intervalId;
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

    private void searchPlants(String query) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            Log.e("SEARCH_ERROR", "API Key is missing or invalid in Manifest: " + apiKey);
            return;
        }

        apiService.searchPlants(apiKey, query).enqueue(new Callback<PlantResponse>() {
            @Override
            public void onResponse(Call<PlantResponse> call, Response<PlantResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    adapter.setPlants(response.body().getData());
                    Log.i("SEARCH_DEBUG", "Results found: " + response.body().getData().size());
                } else {
                    Log.e("SEARCH_ERROR", "API Response Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PlantResponse> call, Throwable t) {
                if (isAdded()) {
                    Log.e("SEARCH_ERROR", "Network failure: " + t.getMessage());
                }
            }
        });
    }
}

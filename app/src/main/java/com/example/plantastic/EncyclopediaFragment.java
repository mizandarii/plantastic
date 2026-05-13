package com.example.plantastic;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.api.PerenualService;
import com.example.plantastic.api.PlantAdapter;
import com.example.plantastic.api.PlantResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EncyclopediaFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private List<PlantResponse.PlantData> plantList;
    private PerenualService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.encyclopedia_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        recyclerView = view.findViewById(R.id.plantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        plantList = new ArrayList<>();
        adapter = new PlantAdapter(plantList, plant -> {
            // Open the detail fragment when a plant is clicked
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, EncyclopediaItemFragment.newInstance(plant))
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);
        
        setupRetrofit();
        fetchPlants("a"); // Load some default plants
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://perenual.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(PerenualService.class);
    }

    private void fetchPlants(String query) {
        // Use a generic key or handle key loading as in AddPlantFragment
        // For now, using a placeholder logic. You should ensure apiKey is valid.
        String apiKey = "sk-0HqZ677fbc267597f7813"; // Ensure this matches your local.properties key
        
        apiService.searchPlants(apiKey, query).enqueue(new Callback<PlantResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlantResponse> call, @NonNull Response<PlantResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    plantList.clear();
                    plantList.addAll(response.body().getData());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlantResponse> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e("API_ERROR", t.getMessage());
                }
            }
        });
    }
}

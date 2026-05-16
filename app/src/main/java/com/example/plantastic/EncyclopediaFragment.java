package com.example.plantastic;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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

    private String apiKey;
    private static final String BASE_URL = "https://perenual.com/api/";

    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private PlantAdapter adapter;
    private PerenualService apiService;
    private LinearLayoutManager layoutManager;

    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private boolean isSearchMode = false;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.encyclopedia_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        loadApiKey();
        setupRetrofit();
        setupRecyclerView();
        setupSearch();
        loadSpeciesList(1);
    }

    private void initViews(View view) {
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.plantsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadApiKey() {
        try {
            ApplicationInfo ai = requireContext().getPackageManager().getApplicationInfo(
                    requireContext().getPackageName(), PackageManager.GET_META_DATA);
            apiKey = ai.metaData.getString("perenual_api_key");
            if (apiKey != null) {
                apiKey = apiKey.replace("\"", "").trim();
            }
            if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) {
                Log.e("ENCYCLOPEDIA_DEBUG", "API Key Error: " + apiKey);
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
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PlantAdapter(new ArrayList<>(), plant -> {
            // Open the detail fragment when a plant is clicked
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, EncyclopediaItemFragment.newInstance(plant))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        // Pagination: load more when near the bottom
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && !isLoading
                        && hasMorePages) {
                    currentPage++;
                    if (isSearchMode) {
                        searchPlants(currentSearchQuery);
                    } else {
                        loadSpeciesList(currentPage);
                    }
                }
            }
        });
    }

    private void setupSearch() {
        if (searchView == null) return;
        searchView.onActionViewExpanded();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.trim().length() > 0) {
                    isSearchMode = true;
                    currentSearchQuery = query.trim();
                    currentPage = 1;
                    hasMorePages = true;
                    adapter.setPlants(new ArrayList<>());
                    searchPlants(query.trim());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    private void loadSpeciesList(int page) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) return;

        isLoading = true;
        if (progressBar != null && page == 1) progressBar.setVisibility(View.VISIBLE);

        apiService.getSpeciesList(apiKey, page).enqueue(new Callback<PlantResponse>() {
            @Override
            public void onResponse(@NonNull Call<PlantResponse> call, @NonNull Response<PlantResponse> response) {
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    List<PlantResponse.PlantData> results = response.body().getData();
                    if (page == 1) {
                        adapter.setPlants(results);
                    } else {
                        List<PlantResponse.PlantData> currentPlants = new ArrayList<>(adapter.getPlants());
                        currentPlants.addAll(results);
                        adapter.setPlants(currentPlants);
                    }

                    if (results.isEmpty() || results.size() < 20) {
                        hasMorePages = false;
                    }

                    Log.d("EncyclopediaFragment", "Loaded page " + page + " with " + results.size() + " plants");
                } else {
                    int code = response.code();
                    if (isAdded()) {
                        if (code == 429) {
                            Toast.makeText(getContext(), "API Limit Reached (100 calls). Wait until tomorrow.", Toast.LENGTH_LONG).show();
                        } else if (code == 401 || code == 403) {
                            Toast.makeText(getContext(), "Invalid API Key. Check local.properties.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Load failed: " + code, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("ENCYCLOPEDIA_ERROR", "Fragment not attached; skipping Toast for load failure: " + code);
                    }
                    Log.e("ENCYCLOPEDIA_ERROR", "Status: " + code + " Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlantResponse> call, @NonNull Throwable t) {
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (isAdded()) {
                    Log.e("ENCYCLOPEDIA_ERROR", "Network failure: " + t.getMessage());
                    Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("ENCYCLOPEDIA_ERROR", "Fragment not attached; skipping Toast for network failure: " + t.getMessage());
                }
            }
        });
    }

    private void searchPlants(String query) {
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("{")) return;

        isLoading = true;
        if (progressBar != null && currentPage == 1) progressBar.setVisibility(View.VISIBLE);

        apiService.searchPlants(apiKey, query).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<PlantResponse> call, @NonNull Response<PlantResponse> response) {
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    List<PlantResponse.PlantData> results = response.body().getData();
                    if (currentPage == 1) {
                        adapter.setPlants(results);
                    } else {
                        List<PlantResponse.PlantData> currentPlants = new ArrayList<>(adapter.getPlants());
                        currentPlants.addAll(results);
                        adapter.setPlants(currentPlants);
                    }

                    if (results.isEmpty()) {
                        if (isAdded()) {
                            Toast.makeText(getContext(), "No plants found for '" + query + "'", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("ENCYCLOPEDIA_ERROR", "No plants found for '" + query + "' but fragment not attached");
                        }
                        hasMorePages = false;
                    } else if (results.size() < 20) {
                        hasMorePages = false;
                    }
                } else {
                    int code = response.code();
                    if (isAdded()) {
                        if (code == 429) {
                            Toast.makeText(getContext(), "API Limit Reached (100 calls). Wait until tomorrow.", Toast.LENGTH_LONG).show();
                        } else if (code == 401 || code == 403) {
                            Toast.makeText(getContext(), "Invalid API Key. Check local.properties.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Search failed: " + code, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w("ENCYCLOPEDIA_ERROR", "Fragment not attached; skipping Toast for search failure: " + code);
                    }
                    Log.e("ENCYCLOPEDIA_ERROR", "Status: " + code + " Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PlantResponse> call, @NonNull Throwable t) {
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (isAdded()) {
                    Log.e("ENCYCLOPEDIA_ERROR", "Network failure: " + t.getMessage());
                    Toast.makeText(getContext(), "Check internet connection", Toast.LENGTH_SHORT).show();
                } else {
                    Log.w("ENCYCLOPEDIA_ERROR", "Fragment not attached; skipping Toast for search network failure: " + t.getMessage());
                }
            }
        });
    }
}

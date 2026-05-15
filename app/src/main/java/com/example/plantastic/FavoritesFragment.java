package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesFragment extends Fragment {
    private PlantasticDatabase db;
    private FavoritePlantAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView emptyFavoritesText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = PlantasticDatabase.getInstance(requireContext());
        emptyFavoritesText = view.findViewById(R.id.emptyFavoritesText);

        RecyclerView recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoritePlantAdapter(favoritePlant -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, EncyclopediaItemFragment.newInstanceById(favoritePlant.api_taim_id))
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        if (db == null) return;

        executorService.execute(() -> {
            Kasutaja user = db.kasutajaDao().getFirstUser();
            if (user == null) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        adapter.setFavorites(java.util.Collections.emptyList());
                        emptyFavoritesText.setVisibility(View.VISIBLE);
                    });
                }
                return;
            }

            java.util.List<com.example.plantastic.data.entities.LemmikTaim> favorites =
                    db.lemmikTaimDao().getByKasutajaId(user.id);

            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                adapter.setFavorites(favorites);
                emptyFavoritesText.setVisibility(favorites == null || favorites.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }
}




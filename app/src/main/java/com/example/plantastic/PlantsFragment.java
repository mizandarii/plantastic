package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.HooldusAjalugu;
import com.example.plantastic.data.entities.HooldusTüüp;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.TaimWithDetails;
import com.example.plantastic.data.entities.Teade;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantsFragment extends Fragment implements MyPlantAdapter.OnPlantActionListener {

    private RecyclerView recyclerView;
    private MyPlantAdapter adapter;
    private PlantasticDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_plants_fragment, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton addPlantBtn = view.findViewById(R.id.btnAdd);
        if (addPlantBtn != null) {
            addPlantBtn.setOnClickListener(v -> {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, new AddPlantFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            });
        }

        db = PlantasticDatabase.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.myPlantsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new MyPlantAdapter(this);
        recyclerView.setAdapter(adapter);

        loadMyPlants();
    }

    private void loadMyPlants() {
        executorService.execute(() -> {
            // For demo purposes, we get the first user. 
            // In a real app, you'd have the logged-in user's ID.
            Kasutaja user = db.kasutajaDao().getFirstUser();
            if (user != null) {
                List<TaimWithDetails> myPlants = db.taimDao().getWithDetailsByUserId(user.id);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.setPlants(myPlants));
                }
            }
        });
    }

    @Override
    public void onPlantClick(TaimWithDetails plant) {
        navigateToPlantDetails(plant, false);
    }

    @Override
    public void onWaterClick(TaimWithDetails plant) {
        if (plant == null || plant.taim == null) return;

        executorService.execute(() -> {
            try {
                HooldusTüüp kastmineType = db.hooldusTüüpDao().getByName("Kastmine");
                if (kastmineType == null) {
                    HooldusTüüp createdType = new HooldusTüüp();
                    createdType.nimetus = "Kastmine";
                    long typeId = db.hooldusTüüpDao().insert(createdType);
                    createdType.id = (int) typeId;
                    kastmineType = createdType;
                }

                HooldusAjalugu history = new HooldusAjalugu();
                history.taim_id = plant.taim.id;
                history.hooldusTüüp_id = kastmineType.id;
                history.aeg = System.currentTimeMillis();
                history.kommentaar = "Kastetud";
                db.hooldusAjaluguDao().insert(history);

                if (plant.sort != null) {
                    scheduleNextNotification(plant, kastmineType.id);
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Kastetud", Toast.LENGTH_SHORT).show();
                        loadMyPlants();
                    });
                }
            } catch (Exception ex) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Kastmise salvestamine ebaõnnestus", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    @Override
    public void onEditClick(TaimWithDetails plant) {
        navigateToPlantDetails(plant, true);
    }

    @Override
    public void onDeleteClick(TaimWithDetails plant) {
        if (plant == null || plant.taim == null || getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete plant?")
                .setMessage("Are you sure you want to delete this plant? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePlant(plant))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToPlantDetails(TaimWithDetails plant, boolean startInEditMode) {
        MyPlantFragment detailFragment = new MyPlantFragment();
        Bundle args = new Bundle();
        args.putInt("plantId", plant.taim.id);
        args.putBoolean("startInEditMode", startInEditMode);
        detailFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, detailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void deletePlant(TaimWithDetails plant) {
        executorService.execute(() -> {
            try {
                db.taimDao().delete(plant.taim);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Plant deleted", Toast.LENGTH_SHORT).show();
                        loadMyPlants();
                    });
                }
            } catch (Exception ex) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Deleting plant failed", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void scheduleNextNotification(TaimWithDetails plant, int careTypeId) {
        long now = System.currentTimeMillis();
        long intervalMillis = getIntervalMillisFromWateringIntensity(plant.sort.kastmisvajadus);
        long nextNotificationTime = now + intervalMillis;

        Teade notification = db.teadeDao().getByTaimAndType(plant.taim.id, careTypeId);
        if (notification == null) {
            notification = new Teade();
            notification.taim_id = plant.taim.id;
            notification.hooldusTüüp_id = careTypeId;
            notification.aeg = nextNotificationTime;
            notification.kommentaar = "Watering reminder";
            db.teadeDao().insert(notification);
        } else {
            notification.aeg = nextNotificationTime;
            db.teadeDao().update(notification);
        }
    }

    private long getIntervalMillisFromWateringIntensity(int intensity) {
        switch (intensity) {
            case 0: return 365L * 24 * 60 * 60 * 1000;
            case 1: return 30L * 24 * 60 * 60 * 1000;
            case 2: return 14L * 24 * 60 * 60 * 1000;
            case 3: return 7L * 24 * 60 * 60 * 1000;
            case 4: return 5L * 60 * 1000;
            default: return 14L * 24 * 60 * 60 * 1000;
        }
    }
}

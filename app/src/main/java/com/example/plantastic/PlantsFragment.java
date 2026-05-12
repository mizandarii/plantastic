package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.TaimWithDetails;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlantsFragment extends Fragment {

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
        
        adapter = new MyPlantAdapter(plant -> {
            // Handle plant click if needed
        });
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
}

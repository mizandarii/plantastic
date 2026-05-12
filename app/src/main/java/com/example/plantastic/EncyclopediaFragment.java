package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.api.PlantAdapter;
import com.example.plantastic.api.PlantResponse;

import java.util.ArrayList;
import java.util.List;

public class EncyclopediaFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private List<PlantResponse.PlantData> plantList;

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
            // Handle plant click
        });

        recyclerView.setAdapter(adapter);
        
        // Note: You might want to trigger your API call here to populate plantList
    }
}

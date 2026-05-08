package com.example.plantastic.api;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.plantastic.R;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<PlantResponse.PlantData> plants;
    private OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onPlantClick(PlantResponse.PlantData plant);
    }

    public PlantAdapter(List<PlantResponse.PlantData> plants, OnPlantClickListener listener) {
        this.plants = plants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item_fragment, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantResponse.PlantData plant = plants.get(position);
        holder.commonName.setText(plant.getCommonName());
        
        if (plant.getScientificName() != null && !plant.getScientificName().isEmpty()) {
            holder.scientificName.setText(plant.getScientificName().get(0));
        }

        if (plant.getDefaultImage() != null) {
            Glide.with(holder.itemView.getContext())
                .load(plant.getDefaultImage().getThumbnail())
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.plantImage);
        }

        holder.itemView.setOnClickListener(v -> listener.onPlantClick(plant));
    }

    @Override
    public int getItemCount() {
        return plants != null ? plants.size() : 0;
    }

    public void setPlants(List<PlantResponse.PlantData> plants) {
        this.plants = plants;
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView commonName;
        TextView scientificName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            commonName = itemView.findViewById(R.id.commonNameText);
            scientificName = itemView.findViewById(R.id.scientificNameText);
        }
    }
}

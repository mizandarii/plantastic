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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_plant_card, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        PlantResponse.PlantData plant = plants.get(position);
        
        // Safety checks for views that might not be in the layout
        if (holder.nickname != null) holder.nickname.setVisibility(View.GONE);
        if (holder.dropButton != null) holder.dropButton.setVisibility(View.GONE);
        if (holder.editButton != null) holder.editButton.setVisibility(View.GONE);
        if (holder.deleteButton != null) holder.deleteButton.setVisibility(View.GONE);

        if (holder.commonName != null) {
            holder.commonName.setText(plant.getCommonName());
        }

        if (holder.commonName != null) {
            holder.commonName.setText(plant.getCommonName());
            holder.commonName.setVisibility(View.VISIBLE);
        }

        if (holder.plantImage != null) {
            if (plant.getDefaultImage() != null) {
                Glide.with(holder.itemView.getContext())
                        .load(plant.getDefaultImage().getThumbnail())
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.plantImage);
            } else {
                holder.plantImage.setImageResource(R.mipmap.ic_launcher);
            }
        }

        // Sunlight logic for the card (if icons exist)
        int level = plant.getSunlightLevel();
        if (holder.sun1 != null) holder.sun1.setAlpha(level >= 1 ? 1.0f : 0.3f);
        if (holder.sun2 != null) holder.sun2.setAlpha(level >= 2 ? 1.0f : 0.3f);
        if (holder.sun3 != null) holder.sun3.setAlpha(level >= 3 ? 1.0f : 0.3f);
        if (holder.sun4 != null) holder.sun4.setAlpha(level >= 4 ? 1.0f : 0.3f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlantClick(plant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return plants != null ? plants.size() : 0;
    }

    public void setPlants(List<PlantResponse.PlantData> plants) {
        this.plants = plants;
        notifyDataSetChanged();
    }

    public List<PlantResponse.PlantData> getPlants() {
        return plants;
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView nickname;
        TextView commonName;
        ImageView dropButton;
        ImageView editButton;
        ImageView deleteButton;
        
        ImageView sun1, sun2, sun3, sun4;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.selectedPlantImage);
            nickname = itemView.findViewById(R.id.nickname);
            commonName = itemView.findViewById(R.id.common_name);
            dropButton = itemView.findViewById(R.id.drop);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
            
            sun1 = itemView.findViewById(R.id.sun1);
            sun2 = itemView.findViewById(R.id.sun2);
            sun3 = itemView.findViewById(R.id.sun3);
            sun4 = itemView.findViewById(R.id.sun4);
        }
    }
}

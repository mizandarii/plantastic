package com.example.plantastic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantastic.data.entities.TaimWithDetails;

import java.util.ArrayList;
import java.util.List;

public class MyPlantAdapter extends RecyclerView.Adapter<MyPlantAdapter.MyPlantViewHolder> {

    private List<TaimWithDetails> plants = new ArrayList<>();
    private final OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onPlantClick(TaimWithDetails plant);
    }

    public MyPlantAdapter(OnPlantClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyPlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_plant_card, parent, false);
        return new MyPlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantViewHolder holder, int position) {
        TaimWithDetails plantWithDetails = plants.get(position);
        
        holder.nickname.setText(plantWithDetails.taim.nimi);
        holder.commonName.setVisibility(View.GONE);
        holder.scientificName.setVisibility(View.GONE);

        if (plantWithDetails.fotos != null && !plantWithDetails.fotos.isEmpty() && plantWithDetails.fotos.get(0).foto != null) {
            Glide.with(holder.itemView)
                    .load(plantWithDetails.fotos.get(0).foto)
                    .placeholder(R.drawable.ic_flower)
                    .error(R.drawable.ic_flower)
                    .into(holder.plantImage);
        } else {
            holder.plantImage.setImageResource(R.drawable.ic_flower);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlantClick(plantWithDetails);
            }
        });
        
        holder.editButton.setOnClickListener(v -> {
            // Handle edit
        });
        holder.deleteButton.setOnClickListener(v -> {
            // Handle delete
        });
    }

    @Override
    public int getItemCount() {
        return plants.size();
    }

    public void setPlants(List<TaimWithDetails> plants) {
        this.plants = plants;
        notifyDataSetChanged();
    }

    public static class MyPlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView nickname;
        TextView commonName;
        TextView scientificName;
        ImageView editButton;
        ImageView deleteButton;
        ImageView dropButton;

        public MyPlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.selectedPlantImage);
            nickname = itemView.findViewById(R.id.nickname);
            commonName = itemView.findViewById(R.id.common_name);
            scientificName = itemView.findViewById(R.id.scientific_name);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
            dropButton = itemView.findViewById(R.id.drop);
        }
    }
}

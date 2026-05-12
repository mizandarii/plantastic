package com.example.plantastic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plantastic.data.entities.TaimWithDetails;

import java.util.ArrayList;
import java.util.List;

public class MyPlantAdapter extends RecyclerView.Adapter<MyPlantAdapter.MyPlantViewHolder> {

    private List<TaimWithDetails> plants = new ArrayList<>();
    private OnPlantClickListener listener;

    public interface OnPlantClickListener {
        void onPlantClick(TaimWithDetails plant);
    }

    public MyPlantAdapter(OnPlantClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyPlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item_fragment, parent, false);
        return new MyPlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantViewHolder holder, int position) {
        TaimWithDetails plantWithDetails = plants.get(position);
        holder.commonName.setText(plantWithDetails.taim.nimi);
        
        if (plantWithDetails.sort != null) {
            holder.scientificName.setText(plantWithDetails.sort.nimetus);
        } else {
            holder.scientificName.setText("");
        }

        // Using placeholder for now since local DB plants don't have images in this simple setup
        holder.plantImage.setImageResource(R.drawable.ic_flower);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlantClick(plantWithDetails);
            }
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

    static class MyPlantViewHolder extends RecyclerView.ViewHolder {
        ImageView plantImage;
        TextView commonName;
        TextView scientificName;

        public MyPlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plantImage);
            commonName = itemView.findViewById(R.id.commonNameText);
            scientificName = itemView.findViewById(R.id.scientificNameText);
        }
    }
}

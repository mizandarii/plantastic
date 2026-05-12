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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_plant_card, parent, false);
        return new MyPlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPlantViewHolder holder, int position) {
        TaimWithDetails plantWithDetails = plants.get(position);
        
        // Use the nickname field for the user's custom name
        holder.nickname.setText(plantWithDetails.taim.nimi);

        if (plantWithDetails.sort != null) {
            // Use common_name for the sort name (e.g., "Nefroleep")
            holder.commonName.setText(plantWithDetails.sort.nimetus);
            // Use scientific_name for the scientific name
            holder.scientificName.setText(plantWithDetails.sort.ladinakeelne_nimetus);
        } else {
            holder.commonName.setText("");
            holder.scientificName.setText("");
        }

        // Using placeholder for now
        holder.plantImage.setImageResource(R.drawable.ic_flower);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPlantClick(plantWithDetails);
            }
        });
        
        // Hide edit/delete for now or set listeners if needed
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

    static class MyPlantViewHolder extends RecyclerView.ViewHolder {
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

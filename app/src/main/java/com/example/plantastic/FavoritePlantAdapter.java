package com.example.plantastic;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plantastic.data.entities.LemmikTaim;

import java.util.ArrayList;
import java.util.List;

public class FavoritePlantAdapter extends RecyclerView.Adapter<FavoritePlantAdapter.FavoritePlantViewHolder> {

    public interface OnFavoriteClickListener {
        void onFavoriteClick(LemmikTaim favoritePlant);
    }

    private final List<LemmikTaim> favorites = new ArrayList<>();
    private final OnFavoriteClickListener listener;

    public FavoritePlantAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoritePlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_plant_card, parent, false);
        return new FavoritePlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritePlantViewHolder holder, int position) {
        LemmikTaim favorite = favorites.get(position);

        holder.nickname.setVisibility(View.GONE);
        holder.dropButton.setVisibility(View.GONE);
        holder.editButton.setVisibility(View.GONE);
        holder.deleteButton.setVisibility(View.GONE);

        holder.commonName.setVisibility(View.VISIBLE);
        holder.commonName.setText((favorite.nimetus == null || favorite.nimetus.trim().isEmpty()) ? "Unknown" : favorite.nimetus);

        if (favorite.img_url != null && !favorite.img_url.trim().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(favorite.img_url)
                    .placeholder(R.drawable.ic_flower)
                    .error(R.drawable.ic_flower)
                    .into(holder.plantImage);
        } else {
            holder.plantImage.setImageResource(R.drawable.ic_flower);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(favorite);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void setFavorites(List<LemmikTaim> newFavorites) {
        favorites.clear();
        if (newFavorites != null) {
            favorites.addAll(newFavorites);
        }
        notifyDataSetChanged();
    }

    static class FavoritePlantViewHolder extends RecyclerView.ViewHolder {
        final ImageView plantImage;
        final TextView nickname;
        final TextView commonName;
        final ImageView editButton;
        final ImageView deleteButton;
        final ImageView dropButton;

        FavoritePlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.selectedPlantImage);
            nickname = itemView.findViewById(R.id.nickname);
            commonName = itemView.findViewById(R.id.common_name);
            editButton = itemView.findViewById(R.id.edit);
            deleteButton = itemView.findViewById(R.id.delete);
            dropButton = itemView.findViewById(R.id.drop);
        }
    }
}


package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// TaimLiik.java
@Entity(tableName = "taim_liik")
public class TaimLiik {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nimetus;

    @NonNull
    public String ladinakeelne_nimetus;
}
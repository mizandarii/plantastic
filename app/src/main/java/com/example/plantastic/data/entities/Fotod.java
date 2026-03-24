package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// Fotod.java
@Entity(tableName = "fotod",
        foreignKeys = {
                @ForeignKey(entity = Taim.class,
                        parentColumns = "id",
                        childColumns = "taim_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class Fotod {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String foto; // путь или URI

    public int taim_id;
}
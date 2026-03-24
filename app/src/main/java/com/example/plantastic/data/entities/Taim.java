package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// Taim.java
@Entity(tableName = "taim",
        foreignKeys = {
                @ForeignKey(entity = TaimSort.class,
                        parentColumns = "id",
                        childColumns = "sort_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Kasutaja.class,
                        parentColumns = "id",
                        childColumns = "kasutaja_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class Taim {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nimi;

    public int sort_id;

    public int kasutaja_id;
}
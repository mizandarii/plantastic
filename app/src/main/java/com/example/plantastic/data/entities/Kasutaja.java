package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Kasutaja.java
@Entity(tableName = "kasutaja")
public class Kasutaja {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String kasutajanimi;

    // Храним время уведомлений как timestamp
    public long teade_start;
    public long teade_aeg;
}
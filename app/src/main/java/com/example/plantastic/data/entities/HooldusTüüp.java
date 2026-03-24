package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// HooldusTüüp.java
@Entity(tableName = "hooldusTüüp")
public class HooldusTüüp {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String nimetus;
}
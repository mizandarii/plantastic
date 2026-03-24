package com.example.plantastic.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// Teade.java
@Entity(tableName = "teade",
        foreignKeys = {
                @ForeignKey(entity = Taim.class,
                        parentColumns = "id",
                        childColumns = "taim_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = HooldusTüüp.class,
                        parentColumns = "id",
                        childColumns = "hooldusTüüp_id",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Teade {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int taim_id;

    public long aeg;

    public int hooldusTüüp_id;

    public String kommentaar;
}

package com.example.plantastic.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// HooldusAjalugu.java
@Entity(tableName = "hooldusAjalugu",
        foreignKeys = {
                @ForeignKey(entity = HooldusTüüp.class,
                        parentColumns = "id",
                        childColumns = "hooldusTüüp_id",
                        onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Taim.class,
                        parentColumns = "id",
                        childColumns = "taim_id",
                        onDelete = ForeignKey.CASCADE)
        })
public class HooldusAjalugu {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int hooldusTüüp_id;

    public long aeg; // timestamp

    public int taim_id;

    public String kommentaar;
}
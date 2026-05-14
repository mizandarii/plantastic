package com.example.plantastic.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// LemmikTaim.java
@Entity(
        tableName = "lemmiktaimed",
        foreignKeys = {
                @ForeignKey(
                        entity = Kasutaja.class,
                        parentColumns = "id",
                        childColumns = "kasutaja_id",
                        onDelete = ForeignKey.CASCADE
                )
        }
)
public class LemmikTaim {
    @PrimaryKey
    public int api_taim_id;

    public String nimetus;

    public String img_url;

    public int kasutaja_id;
}


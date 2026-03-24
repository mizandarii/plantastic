package com.example.plantastic.data.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

// TaimSort.java
@Entity(tableName = "taim_sort",
        foreignKeys = {
                @ForeignKey(entity = TaimLiik.class,
                        parentColumns = "id",
                        childColumns = "liik_id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = KastmisVajadusIntervall.class,
                        parentColumns = "id",
                        childColumns = "kastmisvajadus",
                        onDelete = ForeignKey.SET_NULL)
        })
public class TaimSort {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String ladinakeelne_nimetus;

    @NonNull
    public String nimetus;

    public int liik_id;

    public int kastmisvajadus; // FK на KastmisvajadusInterval.id

    public String kirjeldus;

    public int valgusnoudlikkus; // 1–5
}
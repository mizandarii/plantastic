package com.example.plantastic.data.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TaimWithDetails {
    @Embedded
    public Taim taim;

    @Relation(
            parentColumn = "sort_id",
            entityColumn = "id"
    )
    public TaimSort sort;
}

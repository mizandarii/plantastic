package com.example.plantastic.data.entities;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class TaimWithDetails {
    @Embedded
    public Taim taim;

    @Relation(
            parentColumn = "sort_id",
            entityColumn = "id"
    )
    public TaimSort sort;

    @Relation(
            parentColumn = "id",
            entityColumn = "taim_id"
    )
    public List<Fotod> fotos;
}

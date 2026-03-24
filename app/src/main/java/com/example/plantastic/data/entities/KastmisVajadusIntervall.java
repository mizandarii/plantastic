package com.example.plantastic.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

// KastmisvajadusInterval.java
@Entity(tableName = "kastmisvajadus_interval")
public class KastmisVajadusIntervall {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int paevad;
}
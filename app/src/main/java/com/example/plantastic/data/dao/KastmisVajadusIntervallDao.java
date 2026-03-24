package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.KastmisVajadusIntervall;

@Dao
public interface KastmisVajadusIntervallDao {
    @Insert
    long insert(KastmisVajadusIntervall intervall);

    @Query("SELECT * FROM kastmisvajadus_interval WHERE id = :id")
    KastmisVajadusIntervall getById(int id);
}

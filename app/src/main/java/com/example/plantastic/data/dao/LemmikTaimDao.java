package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.LemmikTaim;

import java.util.List;

@Dao
public interface LemmikTaimDao {
    @Insert
    long insert(LemmikTaim lemmikTaim);

    @Delete
    void delete(LemmikTaim lemmikTaim);

    @Query("SELECT * FROM lemmiktaimed WHERE api_taim_id = :apiTaimId")
    LemmikTaim getById(int apiTaimId);

    @Query("SELECT * FROM lemmiktaimed WHERE kasutaja_id = :kasutajaId")
    List<LemmikTaim> getByKasutajaId(int kasutajaId);

    @Query("SELECT * FROM lemmiktaimed")
    List<LemmikTaim> getAll();
}


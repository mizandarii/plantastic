package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.TaimLiik;

@Dao
public interface TaimLiikDao {
    @Insert
    long insert(TaimLiik liik);

    @Query("SELECT * FROM taim_liik WHERE id = :id")
    TaimLiik getById(int id);
}

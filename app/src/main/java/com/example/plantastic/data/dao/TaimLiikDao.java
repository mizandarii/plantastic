package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.TaimLiik;

import java.util.List;

@Dao
public interface TaimLiikDao {
    @Insert
    long insert(TaimLiik liik);

    @Query("SELECT * FROM taim_liik WHERE id = :id")
    TaimLiik getById(int id);

    @Query("SELECT * FROM taim_liik WHERE nimetus = :nimetus LIMIT 1")
    TaimLiik getByName(String nimetus);

    @Query("SELECT * FROM taim_liik LIMIT 1")
    TaimLiik getFirstLiik();

    @Query("SELECT * FROM taim_liik")
    List<TaimLiik> getAll();
}

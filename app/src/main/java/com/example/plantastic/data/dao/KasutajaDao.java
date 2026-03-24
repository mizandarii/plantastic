package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantastic.data.entities.Kasutaja;

// KasutajaDao.java
@Dao
public interface KasutajaDao {
    @Insert
    long insert(Kasutaja kasutaja);

    @Update
    void update(Kasutaja kasutaja);

    @Delete
    void delete(Kasutaja kasutaja);

    @Query("SELECT * FROM kasutaja WHERE id = :id")
    Kasutaja getById(int id);
}
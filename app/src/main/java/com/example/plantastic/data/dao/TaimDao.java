package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantastic.data.entities.Taim;

import java.util.List;

// TaimDao.java
@Dao
public interface TaimDao {
    @Insert
    long insert(Taim taim);

    @Update
    void update(Taim taim);

    @Delete
    void delete(Taim taim);

    @Query("SELECT * FROM taim WHERE kasutaja_id = :userId")
    List<Taim> getByUserId(int userId);

    @Query("SELECT * FROM taim WHERE id = :id")
    Taim getById(int id);
}
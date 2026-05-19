package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.TaimWithDetails;

import java.util.List;

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

    @Transaction
    @Query("SELECT * FROM taim WHERE kasutaja_id = :userId")
    List<TaimWithDetails> getWithDetailsByUserId(int userId);

    @Query("SELECT * FROM taim WHERE id = :id")
    Taim getById(int id);

    @Transaction
    @Query("SELECT * FROM taim WHERE id = :id")
    TaimWithDetails getWithDetailsById(int id);

    @Transaction
    @Query("SELECT * FROM taim ORDER BY id DESC")
    List<TaimWithDetails> getAllWithDetails();

    @Query("SELECT * FROM taim")
    List <Taim> getAll();
}

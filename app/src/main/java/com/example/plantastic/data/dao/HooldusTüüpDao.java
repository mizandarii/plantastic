package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.HooldusTüüp;

@Dao
public interface HooldusTüüpDao {
    @Insert
    long insert(HooldusTüüp tuup);

    @Query("SELECT * FROM hooldusTüüp WHERE id = :id")
    HooldusTüüp getById(int id);
}

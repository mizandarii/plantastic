package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.HooldusAjalugu;

import java.util.List;

// HooldusAjaluguDao.java
@Dao
public interface HooldusAjaluguDao {
    @Insert
    long insert(HooldusAjalugu hooldus);

    @Query("SELECT * FROM hooldusAjalugu WHERE taim_id = :taimId ORDER BY aeg DESC")
    List<HooldusAjalugu> getByTaimId(int taimId);
}
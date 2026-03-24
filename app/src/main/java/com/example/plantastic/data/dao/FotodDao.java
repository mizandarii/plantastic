package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.Fotod;

import java.util.List;

// FotodDao.java
@Dao
public interface FotodDao {
    @Insert
    long insert(Fotod foto);

    @Query("SELECT * FROM fotod WHERE taim_id = :taimId")
    List<Fotod> getByTaimId(int taimId);

    @Delete
    void delete(Fotod foto);
}
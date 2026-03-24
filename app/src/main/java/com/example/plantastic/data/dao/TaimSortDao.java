package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.TaimSort;

@Dao
public interface TaimSortDao {
    @Insert
    long insert(TaimSort sort);

    @Query("SELECT * FROM taim_sort WHERE id = :id")
    TaimSort getById(int id);
}

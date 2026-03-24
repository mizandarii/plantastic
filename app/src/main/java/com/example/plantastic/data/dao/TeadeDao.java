package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.plantastic.data.entities.Teade;

import java.util.List;

// TeadeDao.java
@Dao
public interface TeadeDao {
    @Insert
    long insert(Teade teade);

    @Query("SELECT * FROM teade WHERE aeg >= :fromTime ORDER BY aeg ASC")
    List<Teade> getUpcoming(long fromTime);

    @Delete
    void delete(Teade teade);
}

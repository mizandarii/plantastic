package com.example.plantastic.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Query;

import com.example.plantastic.data.entities.Teade;

import java.util.List;

// TeadeDao.java
@Dao
public interface TeadeDao {
    @Insert
    long insert(Teade teade);

    @Update
    void update(Teade teade);

    @Query("SELECT * FROM teade WHERE taim_id = :taimId AND hooldusTüüp_id = :typeId ORDER BY aeg DESC, id DESC LIMIT 1")
    Teade getByTaimAndType(int taimId, Integer typeId);

    @Query("SELECT * FROM teade WHERE taim_id = :taimId AND hooldusTüüp_id = :typeId ORDER BY aeg DESC, id DESC LIMIT 1")
    Teade getLatestByTaimAndType(int taimId, Integer typeId);

    @Query("SELECT * FROM teade WHERE taim_id = :taimId")
    java.util.List<Teade> getByTaimId(int taimId);

    @Query("SELECT * FROM teade WHERE aeg >= :fromTime ORDER BY aeg ASC")
    List<Teade> getUpcoming(long fromTime);

    @Query("DELETE FROM teade WHERE taim_id = :taimId AND hooldusTüüp_id = :typeId")
    void deleteByTaimAndType(int taimId, Integer typeId);

    @Delete
    void delete(Teade teade);
}

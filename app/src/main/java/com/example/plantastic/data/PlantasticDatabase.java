package com.example.plantastic.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.plantastic.data.dao.FotodDao;
import com.example.plantastic.data.dao.HooldusAjaluguDao;
import com.example.plantastic.data.dao.KasutajaDao;
import com.example.plantastic.data.dao.TaimDao;
import com.example.plantastic.data.dao.TeadeDao;
import com.example.plantastic.data.dao.TaimLiikDao;
import com.example.plantastic.data.dao.TaimSortDao;
import com.example.plantastic.data.dao.KastmisVajadusIntervallDao;
import com.example.plantastic.data.dao.HooldusTüüpDao;
import com.example.plantastic.data.entities.Fotod;
import com.example.plantastic.data.entities.HooldusAjalugu;
import com.example.plantastic.data.entities.HooldusTüüp;
import com.example.plantastic.data.entities.KastmisVajadusIntervall;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.TaimLiik;
import com.example.plantastic.data.entities.TaimSort;
import com.example.plantastic.data.entities.Teade;

@Database(
        entities = {
                Kasutaja.class,
                Taim.class,
                TaimLiik.class,
                TaimSort.class,
                HooldusTüüp.class,
                HooldusAjalugu.class,
                Teade.class,
                Fotod.class,
                KastmisVajadusIntervall.class
        },
        version = 3,
        exportSchema = false
)
public abstract class PlantasticDatabase extends RoomDatabase {
    private static PlantasticDatabase INSTANCE;

    public abstract KasutajaDao kasutajaDao();
    public abstract TaimDao taimDao();
    public abstract HooldusAjaluguDao hooldusAjaluguDao();
    public abstract TeadeDao teadeDao();
    public abstract FotodDao fotodDao();
    public abstract TaimLiikDao taimLiikDao();
    public abstract TaimSortDao taimSortDao();
    public abstract KastmisVajadusIntervallDao kastmisVajadusIntervallDao();
    public abstract HooldusTüüpDao hooldusTüüpDao();

    public static synchronized PlantasticDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    PlantasticDatabase.class,
                    "plantastic_db"
            ).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }
}

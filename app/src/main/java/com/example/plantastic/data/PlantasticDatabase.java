package com.example.plantastic.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.plantastic.data.dao.*;
import com.example.plantastic.data.entities.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        version = 9,
        exportSchema = false
)
public abstract class PlantasticDatabase extends RoomDatabase {

    private static volatile PlantasticDatabase INSTANCE;
    private static final ExecutorService SEED_EXECUTOR = Executors.newSingleThreadExecutor();

    public abstract KasutajaDao kasutajaDao();
    public abstract TaimDao taimDao();
    public abstract HooldusAjaluguDao hooldusAjaluguDao();
    public abstract TeadeDao teadeDao();
    public abstract FotodDao fotodDao();
    public abstract TaimLiikDao taimLiikDao();
    public abstract TaimSortDao taimSortDao();
    public abstract KastmisVajadusIntervallDao kastmisVajadusIntervallDao();
    public abstract HooldusTüüpDao hooldusTüüpDao();

    public static PlantasticDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (PlantasticDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    PlantasticDatabase.class,
                                    "plantastic_db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();

                    // Run seeding off the caller thread and only after INSTANCE is available.
                    final PlantasticDatabase dbInstance = INSTANCE;
                    SEED_EXECUTOR.execute(() -> DatabaseSeeder.seed(dbInstance));
                }
            }
        }
        return INSTANCE;
    }
}
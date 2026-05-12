package com.example.plantastic.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.plantastic.data.dao.*;
import com.example.plantastic.data.entities.*;

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
        version = 7,
        exportSchema = false
)
public abstract class PlantasticDatabase extends RoomDatabase {

    private static volatile PlantasticDatabase INSTANCE;

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
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // seed only ONCE at first creation
                                    DatabaseSeeder.seed(context);
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
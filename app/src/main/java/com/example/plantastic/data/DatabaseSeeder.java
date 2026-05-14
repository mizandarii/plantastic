package com.example.plantastic.data;

import android.util.Log;

import com.example.plantastic.data.entities.*;

import java.util.List;

public class DatabaseSeeder {

    private static volatile boolean seeded = false;
    private static final Object SEED_LOCK = new Object();

    public static void seed(PlantasticDatabase db) {
        synchronized (SEED_LOCK) {
            if (seeded) return;
            seeded = true;
        }

        try {
            // USER
            Kasutaja user = db.kasutajaDao().getFirstUser();
            if (user == null) {
                user = new Kasutaja();
                user.kasutajanimi = "Test User";
                user.teade_on = false;
                user.teade_start = 8 * 60;
                user.teade_aeg = 22 * 60;

                long userId = db.kasutajaDao().insert(user);
                user.id = (int) userId;
            }

            // LIIGID + SORDID
            if (db.taimLiikDao().getAll().isEmpty()) {
                seedSpeciesAndSorts(db);
            }

            // HOOLDUS Tüübid (seed default care types if empty)
            try {
                if (db.hooldusTüüpDao().getAll().isEmpty()) {
                    Log.d("DatabaseSeeder", "Seeding default hooldus tüübid");
                    HooldusTüüp kasting = new HooldusTüüp();
                    kasting.nimetus = "Kastmine";
                    db.hooldusTüüpDao().insert(kasting);

                    HooldusTüüp vagetamine = new HooldusTüüp();
                    vagetamine.nimetus = "Väetamine";
                    db.hooldusTüüpDao().insert(vagetamine);

                    HooldusTüüp puhastus = new HooldusTüüp();
                    puhastus.nimetus = "Lehtede puhastamine";
                    db.hooldusTüüpDao().insert(puhastus);
                }
            } catch (Exception ex) {
                Log.w("DatabaseSeeder", "Failed to seed hooldus tüübid: " + ex.getMessage());
            }

            // TEST TAIMED
            List<Taim> plants = db.taimDao().getAll();
            if (plants.isEmpty()) {
                TaimSort sort = db.taimSortDao().getFirst();

                if (sort != null) {
                    Taim t1 = new Taim();
                    t1.nimi = "Minu Nefroleep";
                    t1.sort_id = sort.id;
                    t1.kasutaja_id = user.id;
                    db.taimDao().insert(t1);

                    Taim t2 = new Taim();
                    t2.nimi = "Minu Aaloe";
                    t2.sort_id = sort.id;
                    t2.kasutaja_id = user.id;
                    db.taimDao().insert(t2);
                }
            }

            // Kastmis intervallid (watering intervals)
            try {
                if (db.kastmisVajadusIntervallDao().getFirstInterval() == null) {
                    Log.d("DatabaseSeeder", "Seeding default kastmis intervallid");
                    int[] days = new int[]{1, 3, 7, 14, 30, 60};
                    for (int d : days) {
                        KastmisVajadusIntervall iv = new KastmisVajadusIntervall();
                        iv.paevad = d;
                        db.kastmisVajadusIntervallDao().insert(iv);
                    }
                }
            } catch (Exception ex) {
                Log.w("DatabaseSeeder", "Failed to seed kastmis intervallid: " + ex.getMessage());
            }
        } catch (Exception ex) {
            synchronized (SEED_LOCK) {
                seeded = false;
            }
            Log.e("DatabaseSeeder", "Seeding failed", ex);
        }
    }

    private static void seedSpeciesAndSorts(PlantasticDatabase db) {

        TaimLiik fern = new TaimLiik();
        fern.nimetus = "Sõnajalg";
        fern.ladinakeelne_nimetus = "Polypodiopsida";
        long fernId = db.taimLiikDao().insert(fern);

        TaimSort nephrolepis = new TaimSort();
        nephrolepis.nimetus = "Nefroleep";
        nephrolepis.ladinakeelne_nimetus = "Nephrolepis exaltata";
        nephrolepis.liik_id = (int) fernId;
        nephrolepis.kastmisvajadus = 3;
        nephrolepis.valgusnoudlikkus = 2;
        nephrolepis.kirjeldus = "Armastab niiskust ja poolvarju.";
        db.taimSortDao().insert(nephrolepis);

        TaimLiik cactus = new TaimLiik();
        cactus.nimetus = "Kaktus";
        cactus.ladinakeelne_nimetus = "Cactaceae";
        long cactusId = db.taimLiikDao().insert(cactus);

        TaimSort aloe = new TaimSort();
        aloe.nimetus = "Aaloe";
        aloe.ladinakeelne_nimetus = "Aloe vera";
        aloe.liik_id = (int) cactusId;
        aloe.kastmisvajadus = 1;
        aloe.valgusnoudlikkus = 5;
        aloe.kirjeldus = "Vajab palju päikest ja vähe vett.";
        db.taimSortDao().insert(aloe);
    }
}
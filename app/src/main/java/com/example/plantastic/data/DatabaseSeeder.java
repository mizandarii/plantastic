package com.example.plantastic.data;

import android.content.Context;

import com.example.plantastic.data.entities.*;

import java.util.List;

public class DatabaseSeeder {

    private static volatile boolean seeded = false;

    public static void seed(Context context) {
        if (seeded) return;
        seeded = true;

        PlantasticDatabase db = PlantasticDatabase.getInstance(context);

        // USER
        Kasutaja user = db.kasutajaDao().getFirstUser();
        if (user == null) {
            user = new Kasutaja();
            user.kasutajanimi = "Test User";
            user.teade_start = System.currentTimeMillis();
            user.teade_aeg = 3600000;

            long userId = db.kasutajaDao().insert(user);
            user.id = (int) userId;
        }

        // HOOLDUSTÜÜBID
        if (db.hooldusTüüpDao().getAll().isEmpty()) {
            String[] types = {"Kastmine", "Väetamine", "Puhastamine", "Ümberistutamine"};

            for (String t : types) {
                HooldusTüüp type = new HooldusTüüp();
                type.nimetus = t;
                db.hooldusTüüpDao().insert(type);
            }
        }

        // LIIGID + SORDID
        if (db.taimLiikDao().getAll().isEmpty()) {
            seedSpeciesAndSorts(db);
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
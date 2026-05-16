package com.example.plantastic.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.HooldusAjalugu;
import com.example.plantastic.data.entities.HooldusTüüp;
import com.example.plantastic.data.entities.TaimWithDetails;
import com.example.plantastic.data.entities.Teade;

public class KastaNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "KastaNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taimId = intent.getIntExtra("taim_id", -1);
        int hooldusTypeId = intent.getIntExtra("hooldus_type_id", 1);

        if (taimId == -1) return;

        new Thread(() -> {
            try {
                PlantasticDatabase db = PlantasticDatabase.getInstance(context);

                HooldusTüüp kast = db.hooldusTüüpDao().getById(hooldusTypeId);
                if (kast == null) {
                    kast = db.hooldusTüüpDao().getByName("Kastmine");
                }
                if (kast == null) {
                    HooldusTüüp c = new HooldusTüüp();
                    c.nimetus = "Kastmine";
                    long id = db.hooldusTüüpDao().insert(c);
                    c.id = (int) id;
                    kast = c;
                }

                // insert care history
                HooldusAjalugu history = new HooldusAjalugu();
                history.taim_id = taimId;
                history.hooldusTüüp_id = kast.id;
                history.aeg = System.currentTimeMillis();
                history.kommentaar = "Kastetud (notification)";
                db.hooldusAjaluguDao().insert(history);

                // schedule next Teade
                TaimWithDetails twd = db.taimDao().getWithDetailsById(taimId);
                if (twd != null && twd.sort != null) {
                    long interval = 14L * 24 * 60 * 60 * 1000; // default
                    switch (twd.sort.kastmisvajadus) {
                        case 0: interval = 365L * 24 * 60 * 60 * 1000L; break;
                        case 1: interval = 30L * 24 * 60 * 60 * 1000L; break;
                        case 2: interval = 14L * 24 * 60 * 60 * 1000L; break;
                        case 3: interval = 7L * 24 * 60 * 60 * 1000L; break;
                        case 4: interval = 30L * 1000L; break; // testing 30 seconds
                    }

                    long next = System.currentTimeMillis() + interval;
                    Teade t = db.teadeDao().getByTaimAndType(taimId, kast.id);
                    if (t == null) {
                        t = new Teade();
                        t.taim_id = taimId;
                        t.hooldusTüüp_id = kast.id;
                        t.aeg = next;
                        t.kommentaar = "Watering reminder";
                        db.teadeDao().insert(t);
                    } else {
                        t.aeg = next;
                        db.teadeDao().update(t);
                    }
                }

                Log.d(TAG, "Plant marked watered via notification: " + taimId);
            } catch (Exception ex) {
                Log.e(TAG, "Error handling kasta action", ex);
            }
        }).start();
    }
}


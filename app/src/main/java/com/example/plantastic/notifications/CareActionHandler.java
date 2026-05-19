package com.example.plantastic.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.HooldusAjalugu;
import com.example.plantastic.data.entities.HooldusTüüp;
import com.example.plantastic.data.entities.TaimWithDetails;
import com.example.plantastic.data.entities.Teade;

public final class CareActionHandler {
    private static final String TAG = "CareActionHandler";
    private static final Object LOCK = new Object();

    private CareActionHandler() {
    }

    public static boolean handleKasta(Context context, int taimId, Integer hooldusTypeId) {
        synchronized (LOCK) {
            try {
                Context appContext = context.getApplicationContext();
                PlantasticDatabase db = PlantasticDatabase.getInstance(appContext);

                HooldusTüüp kast = resolveKastmineType(db, hooldusTypeId);
                if (kast == null) {
                    Log.w(TAG, "Could not resolve Kastmine care type for taimId=" + taimId);
                    return false;
                }

                long now = System.currentTimeMillis();

                HooldusAjalugu history = new HooldusAjalugu();
                history.taim_id = taimId;
                history.hooldusTüüp_id = kast.id;
                history.aeg = now;
                history.kommentaar = "Kastetud (notification)";
                db.hooldusAjaluguDao().insert(history);

                TaimWithDetails twd = db.taimDao().getWithDetailsById(taimId);
                long interval = twd != null && twd.sort != null
                        ? CareReminderScheduler.getIntervalMillisFromWateringIntensity(twd.sort.kastmisvajadus)
                        : CareReminderScheduler.getIntervalMillisFromWateringIntensity(2);

                Teade latest = db.teadeDao().getLatestByTaimAndType(taimId, kast.id);
                long previousAeg = latest != null ? latest.aeg : -1L;
                long baseTime = now;
                if (latest != null && latest.aeg > baseTime) {
                    baseTime = latest.aeg;
                }
                long next = baseTime + interval;

                // Keep only one reminder row per plant+care type.
                db.teadeDao().deleteByTaimAndType(taimId, kast.id);

                Teade teade = new Teade();
                teade.taim_id = taimId;
                teade.hooldusTüüp_id = kast.id;
                teade.aeg = next;
                teade.kommentaar = "Watering reminder";
                db.teadeDao().insert(teade);

                CareNotificationManager.cancelCareNotification(appContext, taimId);
                CareReminderScheduler.scheduleReminder(appContext, taimId, kast.id, next);
                // Extra logging and a small debug confirmation notification so it's
                // immediately obvious on-device that the Kasta action ran and what
                // the next scheduled time is. This helps diagnose cases where the
                // work is scheduled but never appears later.
                try {
                    android.util.Log.d(TAG, "Kast scheduled; posting debug confirmation notification for taimId=" + taimId + " nextAeg=" + next);
                    CareNotificationManager.createNotificationChannel(appContext);
                    android.app.NotificationManager nm = appContext.getSystemService(android.app.NotificationManager.class);
                    if (nm != null) {
                        androidx.core.app.NotificationCompat.Builder b = new androidx.core.app.NotificationCompat.Builder(appContext, "care_notifications")
                                .setSmallIcon(com.example.plantastic.R.drawable.ic_launcher_foreground)
                                .setContentTitle("Kast: scheduled")
                                .setContentText("Next at: " + java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(next)))
                                .setAutoCancel(true)
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW);
                        nm.notify( (int)(100000 + taimId), b.build());
                    }
                } catch (Exception ignore) {}
                try {
                    Intent _i = new Intent(CareReminderScheduler.ACTION_REMINDERS_UPDATED);
                    _i.setPackage(appContext.getPackageName());
                    appContext.sendBroadcast(_i);
                } catch (Exception ignore) {
                }

                Log.d(TAG,
                        "Kast schedule update taimId=" + taimId
                                + " previousAeg=" + previousAeg
                                + " baseTime=" + baseTime
                                + " intervalMs=" + interval
                                + " nextAeg=" + next);
                return true;
            } catch (Exception ex) {
                Log.e(TAG, "Failed to handle kast action", ex);
                return false;
            }
        }
    }

    private static HooldusTüüp resolveKastmineType(PlantasticDatabase db, Integer preferredTypeId) {
        try {
            if (preferredTypeId != null) {
                HooldusTüüp byId = db.hooldusTüüpDao().getById(preferredTypeId);
                if (byId != null && byId.nimetus != null && byId.nimetus.trim().equalsIgnoreCase("Kastmine")) {
                    return byId;
                }
            }

            HooldusTüüp byName = db.hooldusTüüpDao().getByName("Kastmine");
            if (byName != null) {
                return byName;
            }

            HooldusTüüp created = new HooldusTüüp();
            created.nimetus = "Kastmine";
            long id = db.hooldusTüüpDao().insert(created);
            created.id = (int) id;
            return created;
        } catch (Exception ex) {
            Log.e(TAG, "Failed to resolve Kastmine care type", ex);
            return null;
        }
    }
}

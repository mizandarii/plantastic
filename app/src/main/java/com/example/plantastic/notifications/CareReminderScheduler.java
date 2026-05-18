package com.example.plantastic.notifications;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Teade;

import java.util.List;
import java.util.concurrent.TimeUnit;

public final class CareReminderScheduler {
    private static final String TAG = "CareReminderScheduler";
    private static final int NOTIFICATION_BASE_ID = 10_000;
    public static final String ACTION_REMINDERS_UPDATED = "com.example.plantastic.REMINDERS_UPDATED";

    private CareReminderScheduler() {
    }

    public static int getNotificationId(int taimId) {
        return NOTIFICATION_BASE_ID + taimId;
    }

    public static String getWorkName(int taimId, int careTypeId) {
        return "care_reminder_" + taimId + "_" + careTypeId;
    }

    public static long getIntervalMillisFromWateringIntensity(int intensity) {
        switch (intensity) {
            case 0: return 365L * 24 * 60 * 60 * 1000;
            case 1: return 30L * 24 * 60 * 60 * 1000;
            case 2: return 14L * 24 * 60 * 60 * 1000;
            case 3: return 7L * 24 * 60 * 60 * 1000;
            case 4: return 30L * 1000; // testing - 30 seconds
            default: return 14L * 24 * 60 * 60 * 1000;
        }
    }

    public static void scheduleReminder(@NonNull Context context, int taimId, int careTypeId, long triggerAtMillis) {
        Context appContext = context.getApplicationContext();
        long delay = Math.max(0L, triggerAtMillis - System.currentTimeMillis());

        Data input = new Data.Builder()
                .putInt("taim_id", taimId)
                .putInt("hooldus_type_id", careTypeId)
                .build();

        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CareNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(input)
                .addTag(getWorkName(taimId, careTypeId))
                .build();

        WorkManager.getInstance(appContext).enqueueUniqueWork(
                getWorkName(taimId, careTypeId),
                ExistingWorkPolicy.REPLACE,
                work
        );

        Log.d(TAG, "Scheduled reminder taimId=" + taimId + " careTypeId=" + careTypeId + " delayMs=" + delay);
    }

    public static void syncUpcomingReminders(@NonNull Context context, @NonNull PlantasticDatabase db) {
        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            try {
                List<Teade> teades = db.teadeDao().getUpcoming(0L);
                if (teades == null) {
                    return;
                }
                for (Teade teade : teades) {
                    if (teade == null || teade.hooldusTüüp_id == null) {
                        continue;
                    }
                    scheduleReminder(appContext, teade.taim_id, teade.hooldusTüüp_id, teade.aeg);
                }
                // notify UI that reminders have been synced/updated
                try {
                    android.content.Intent _i = new android.content.Intent(ACTION_REMINDERS_UPDATED);
                    _i.setPackage(appContext.getPackageName());
                    appContext.sendBroadcast(_i);
                } catch (Exception ignore) {}
            } catch (Exception ex) {
                Log.e(TAG, "Failed to sync reminders", ex);
            }
        }).start();
    }
}


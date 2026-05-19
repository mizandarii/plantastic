package com.example.plantastic.notifications;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Teade;

import java.util.Calendar;
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

    private static int getAlarmRequestCode(int taimId, int careTypeId) {
        return getWorkName(taimId, careTypeId).hashCode();
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

    private static long normalizeMinutes(long rawValue, long fallback) {
        long minutes = rawValue;
        if (minutes < 0) {
            return fallback;
        }

        if (minutes > 24 * 60 - 1) {
            if (minutes > 24L * 60L * 60L) {
                minutes = minutes / 60000L;
            } else {
                minutes = minutes / 60L;
            }
        }

        return minutes % (24 * 60);
    }

    public static long adjustToAllowedNotificationTime(@NonNull Context context, long candidateMillis) {
        try {
            Context appContext = context.getApplicationContext();
            PlantasticDatabase db = PlantasticDatabase.getInstance(appContext);
            Kasutaja user = db.kasutajaDao().getFirstUser();
            if (user == null || !user.teade_on) {
                return candidateMillis;
            }

            long startMinutes = normalizeMinutes(user.teade_start, 8 * 60L);
            long endMinutes = normalizeMinutes(user.teade_aeg, 22 * 60L);
            if (startMinutes == endMinutes) {
                return candidateMillis;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(candidateMillis);
            int candidateMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

            boolean wrapsMidnight = startMinutes > endMinutes;
            boolean withinWindow = wrapsMidnight
                    ? candidateMinutes >= startMinutes || candidateMinutes <= endMinutes
                    : candidateMinutes >= startMinutes && candidateMinutes <= endMinutes;
            if (withinWindow) {
                return candidateMillis;
            }

            int adjustedDayOffset = 0;
            if (!wrapsMidnight) {
                if (candidateMinutes > endMinutes) {
                    adjustedDayOffset = 1;
                }
            }

            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(Calendar.HOUR_OF_DAY, (int) (startMinutes / 60L));
            cal.set(Calendar.MINUTE, (int) (startMinutes % 60L));
            if (adjustedDayOffset > 0) {
                cal.add(Calendar.DAY_OF_YEAR, adjustedDayOffset);
            }
            return cal.getTimeInMillis();
        } catch (Exception ex) {
            Log.w(TAG, "Failed to adjust notification time; using original candidate", ex);
            return candidateMillis;
        }
    }

    public static void scheduleReminder(@NonNull Context context, int taimId, int careTypeId, long triggerAtMillis) {
        Context appContext = context.getApplicationContext();
        long adjustedTriggerAtMillis = adjustToAllowedNotificationTime(appContext, triggerAtMillis);
        long delay = Math.max(0L, adjustedTriggerAtMillis - System.currentTimeMillis());

        Intent alarmIntent = new Intent(appContext, CareReminderAlarmReceiver.class)
                .putExtra("taim_id", taimId)
                .putExtra("hooldus_type_id", careTypeId)
                .putExtra("scheduled_aeg", adjustedTriggerAtMillis);
        android.app.PendingIntent alarmPi = android.app.PendingIntent.getBroadcast(
                appContext,
                getAlarmRequestCode(taimId, careTypeId),
                alarmIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );
        android.app.AlarmManager alarmManager = appContext.getSystemService(android.app.AlarmManager.class);
        if (alarmManager != null) {
            alarmManager.cancel(alarmPi);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                // More reliable for short reminder delays when app process gets reclaimed.
                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, adjustedTriggerAtMillis, alarmPi);
            } else {
                alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, adjustedTriggerAtMillis, alarmPi);
            }
        }

        Data input = new Data.Builder()
                .putInt("taim_id", taimId)
                .putInt("hooldus_type_id", careTypeId)
                .putLong("scheduled_aeg", adjustedTriggerAtMillis)
                .build();

        long fallbackWorkerAt = adjustedTriggerAtMillis + 5000L;
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(CareNotificationWorker.class)
                .setInitialDelay(Math.max(0L, fallbackWorkerAt - System.currentTimeMillis()), TimeUnit.MILLISECONDS)
                .setInputData(input)
                .addTag(getWorkName(taimId, careTypeId))
                .build();

        WorkManager.getInstance(appContext).enqueueUniqueWork(
                getWorkName(taimId, careTypeId),
                ExistingWorkPolicy.REPLACE,
                work
        );

        Log.d(TAG, "Scheduled reminder taimId=" + taimId + " careTypeId=" + careTypeId
                + " requestedAt=" + triggerAtMillis
                + " adjustedAt=" + adjustedTriggerAtMillis
                + " delayMs=" + delay
                + " workerFallbackAt=" + fallbackWorkerAt);
    }


    public static void cancelScheduledReminder(@NonNull Context context, int taimId, int careTypeId) {
        Context appContext = context.getApplicationContext();
        try {
            Intent alarmIntent = new Intent(appContext, CareReminderAlarmReceiver.class)
                    .putExtra("taim_id", taimId)
                    .putExtra("hooldus_type_id", careTypeId);
            android.app.PendingIntent alarmPi = android.app.PendingIntent.getBroadcast(
                    appContext,
                    getAlarmRequestCode(taimId, careTypeId),
                    alarmIntent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );
            android.app.AlarmManager alarmManager = appContext.getSystemService(android.app.AlarmManager.class);
            if (alarmManager != null) {
                alarmManager.cancel(alarmPi);
            }
        } catch (Exception ex) {
            Log.w(TAG, "Failed to cancel alarm for taimId=" + taimId + " careTypeId=" + careTypeId, ex);
        }

        try {
            WorkManager.getInstance(appContext).cancelUniqueWork(getWorkName(taimId, careTypeId));
        } catch (Exception ex) {
            Log.w(TAG, "Failed to cancel worker for taimId=" + taimId + " careTypeId=" + careTypeId, ex);
        }
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


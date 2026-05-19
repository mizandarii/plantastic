package com.example.plantastic.notifications;

import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.Teade;

public class CareReminderAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "CareReminderAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                Context appContext = context.getApplicationContext();
                if (!CareNotificationManager.areCareNotificationsEnabled(appContext)) {
                    Log.d(TAG, "App notification switch is off; skipping alarm delivery");
                    return;
                }

                int taimId = intent.getIntExtra("taim_id", -1);
                int careTypeId = intent.getIntExtra("hooldus_type_id", -1);
                long scheduledAeg = intent.getLongExtra("scheduled_aeg", -1L);
                long now = System.currentTimeMillis();
                Log.d(TAG, "Alarm fired taimId=" + taimId + " careTypeId=" + careTypeId + " scheduledAeg=" + scheduledAeg + " now=" + now);

                if (taimId == -1 || careTypeId == -1) {
                    return;
                }

                PlantasticDatabase db = PlantasticDatabase.getInstance(appContext);
                Teade teade = db.teadeDao().getByTaimAndType(taimId, careTypeId);
                if (teade == null) {
                    Log.d(TAG, "No matching teade row found; skipping alarm for taimId=" + taimId + " careTypeId=" + careTypeId);
                    return;
                }

                // Ignore stale alarm intents if the reminder has been moved since this alarm was scheduled.
                if (scheduledAeg > 0 && teade.aeg != scheduledAeg) {
                    Log.d(TAG, "Stale alarm ignored taimId=" + taimId + " careTypeId=" + careTypeId
                            + " scheduledAeg=" + scheduledAeg + " currentTeadeAeg=" + teade.aeg);
                    return;
                }

                long allowedTriggerAt = CareReminderScheduler.adjustToAllowedNotificationTime(appContext, teade.aeg);
                if (allowedTriggerAt > now) {
                    Log.d(TAG, "Alarm outside allowed window; rescheduling taimId=" + taimId + " careTypeId=" + careTypeId
                            + " allowedTriggerAt=" + allowedTriggerAt + " now=" + now);
                    CareReminderScheduler.scheduleReminder(appContext, taimId, careTypeId, allowedTriggerAt);
                    return;
                }

                if (teade.aeg > now + 500L) {
                    // Guard against early alarm delivery and make sure we still hit the exact due reminder.
                    Log.d(TAG, "Alarm fired early; rescheduling to teade.aeg=" + teade.aeg + " now=" + now);
                    CareReminderScheduler.scheduleReminder(appContext, taimId, careTypeId, teade.aeg);
                    return;
                }

                Taim taim = db.taimDao().getById(taimId);
                if (taim == null) {
                    Log.d(TAG, "Plant not found for alarm taimId=" + taimId);
                    return;
                }

                String careType = "Care";
                try {
                    com.example.plantastic.data.entities.HooldusTüüp type = db.hooldusTüüpDao().getById(careTypeId);
                    if (type != null && type.nimetus != null && !type.nimetus.trim().isEmpty()) {
                        careType = type.nimetus;
                    }
                } catch (Exception ignore) {
                }

                CareNotificationManager.showCareNotification(appContext, taimId, careTypeId, taim.nimi, careType);
                CareReminderScheduler.cancelScheduledReminder(appContext, taimId, careTypeId);
                Log.d(TAG, "Alarm notification sent taimId=" + taimId + " careTypeId=" + careTypeId + " teadeAeg=" + teade.aeg);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to handle care reminder alarm", ex);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}



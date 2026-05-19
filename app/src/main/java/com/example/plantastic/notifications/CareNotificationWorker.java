package com.example.plantastic.notifications;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Taim;
import com.example.plantastic.data.entities.Teade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CareNotificationWorker extends Worker {
    private static final String TAG = "CareNotificationWorker";
    
    public CareNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            if (!CareNotificationManager.areCareNotificationsEnabled(getApplicationContext())) {
                Log.d(TAG, "App notification switch is off; skipping notification delivery");
                return Result.success();
            }

            PlantasticDatabase db = PlantasticDatabase.getInstance(getApplicationContext());
            long now = System.currentTimeMillis();
            int inputPlantId = getInputData().getInt("taim_id", -1);
            int inputCareTypeId = getInputData().getInt("hooldus_type_id", -1);
            long scheduledAeg = getInputData().getLong("scheduled_aeg", -1L);

            // Diagnostic log: dump input payload and current time so we can trace WorkManager deliveries
            Log.d(TAG, "doWork start: now=" + now + " inputPlantId=" + inputPlantId + " inputCareTypeId=" + inputCareTypeId + " scheduledAeg=" + scheduledAeg);

            if (inputPlantId != -1 && inputCareTypeId != -1) {
                Teade teade = db.teadeDao().getByTaimAndType(inputPlantId, inputCareTypeId);
                if (teade != null) {
                    int typeId = teade.hooldusTüüp_id != null ? teade.hooldusTüüp_id : inputCareTypeId;

                    if (scheduledAeg > now) {
                        CareReminderScheduler.scheduleReminder(
                                getApplicationContext(),
                                teade.taim_id,
                                typeId,
                                scheduledAeg
                        );
                        Log.d(TAG, "Reminder still pending (rescheduled) taimId=" + teade.taim_id
                                + " careTypeId=" + typeId
                                + " scheduledAeg=" + scheduledAeg
                                + " now=" + now);
                        return Result.success();
                    }

                    Taim taim = db.taimDao().getById(teade.taim_id);
                    String careType = getCareTypeName(db, teade.hooldusTüüp_id);
                    if (taim != null) {
                        CareNotificationManager.showCareNotification(
                                getApplicationContext(),
                                taim.id,
                                typeId,
                                taim.nimi,
                                careType
                        );
                        CareReminderScheduler.cancelScheduledReminder(getApplicationContext(), teade.taim_id, typeId);
                        Log.d(TAG, "Notification sent for scheduled work: " + taim.nimi
                                + " originalTriggerAt=" + teade.aeg
                                + " scheduledAeg=" + scheduledAeg);
                    }
                }
                return Result.success();
            }

            // Get all notifications that are due
            List<Teade> dueNotifications = db.teadeDao().getUpcoming(now - 1000); // slight buffer
            
            Set<Integer> sentPlantIds = new HashSet<>();
            
            if (dueNotifications != null) {
                for (Teade teade : dueNotifications) {
                    // Only send one notification per plant to avoid spam
                    long allowedTriggerAt = CareReminderScheduler.adjustToAllowedNotificationTime(getApplicationContext(), teade.aeg);
                    if (allowedTriggerAt > now) {
                        Log.d(TAG, "Deferring teade for taimId=" + teade.taim_id + " allowedTriggerAt=" + allowedTriggerAt + " now=" + now);
                        CareReminderScheduler.scheduleReminder(
                                getApplicationContext(),
                                teade.taim_id,
                                teade.hooldusTüüp_id != null ? teade.hooldusTüüp_id : 1,
                                allowedTriggerAt
                        );
                        continue;
                    }

                    if (!sentPlantIds.contains(teade.taim_id) && teade.aeg <= now) {
                        Taim taim = db.taimDao().getById(teade.taim_id);
                        String careType = getCareTypeName(db, teade.hooldusTüüp_id);

                        if (taim != null) {
                            int typeId = teade.hooldusTüüp_id != null ? teade.hooldusTüüp_id : 1;
                            CareNotificationManager.showCareNotification(
                                    getApplicationContext(),
                                    taim.id,
                                    typeId,
                                    taim.nimi,
                                    careType
                            );
                            CareReminderScheduler.cancelScheduledReminder(getApplicationContext(), teade.taim_id, typeId);
                            sentPlantIds.add(teade.taim_id);
                            Log.d(TAG, "Notification sent for plant: " + taim.nimi
                                    + " triggerAt=" + teade.aeg
                                    + " allowedTriggerAt=" + allowedTriggerAt);
                        }
                    }
                }
            }
            
            return Result.success();
        } catch (Exception ex) {
            Log.e(TAG, "Error checking notifications", ex);
            return Result.retry();
        }
    }

    private String getCareTypeName(PlantasticDatabase db, Integer typeId) {
        if (typeId == null) return "Care";
        try {
            com.example.plantastic.data.entities.HooldusTüüp type = db.hooldusTüüpDao().getById(typeId);
            return type != null ? type.nimetus : "Care";
        } catch (Exception e) {
            return "Care";
        }
    }
}

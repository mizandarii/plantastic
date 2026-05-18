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

            if (inputPlantId != -1 && inputCareTypeId != -1) {
                Teade teade = db.teadeDao().getByTaimAndType(inputPlantId, inputCareTypeId);
                if (teade != null) {
                    if (teade.aeg > now) {
                        CareReminderScheduler.scheduleReminder(
                                getApplicationContext(),
                                teade.taim_id,
                                teade.hooldusTüüp_id,
                                teade.aeg
                        );
                        return Result.success();
                    }

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
                        Log.d(TAG, "Notification sent for scheduled work: " + taim.nimi);
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
                            sentPlantIds.add(teade.taim_id);
                            Log.d(TAG, "Notification sent for plant: " + taim.nimi);
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

package com.example.plantastic.notifications;

import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Teade;

public class SnoozeNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeNotificationReceiver";
    private static final long SNOOZE_DURATION_MS = 10 * 1000; // 10 seconds for testing
    private static final Object LOCK = new Object();

    @Override
    public void onReceive(Context context, Intent intent) {
        int taimId = intent.getIntExtra("taim_id", -1);
        int hooldusTypeId = intent.getIntExtra("hooldus_type_id", 1);

        if (taimId != -1) {
            PendingResult pendingResult = goAsync();
            new Thread(() -> {
                try {
                    Context appContext = context.getApplicationContext();
                    PlantasticDatabase db = PlantasticDatabase.getInstance(appContext);

                    synchronized (LOCK) {
                        Teade latest = db.teadeDao().getLatestByTaimAndType(taimId, hooldusTypeId);
                        long previousAeg = latest != null ? latest.aeg : -1L;
                        long now = System.currentTimeMillis();
                        long baseTime = now;
                        if (latest != null && latest.aeg > baseTime) {
                            baseTime = latest.aeg;
                        }
                        long snoozedAt = baseTime + SNOOZE_DURATION_MS;

                        db.teadeDao().deleteByTaimAndType(taimId, hooldusTypeId);
                        Teade teade = new Teade();
                        teade.taim_id = taimId;
                        teade.hooldusTüüp_id = hooldusTypeId;
                        teade.aeg = snoozedAt;
                        teade.kommentaar = "Watering reminder (snoozed)";
                        db.teadeDao().insert(teade);

                        CareNotificationManager.cancelCareNotification(appContext, taimId);
                        CareReminderScheduler.scheduleReminder(appContext, taimId, hooldusTypeId, snoozedAt);
                        Log.d(TAG,
                                "Snooze schedule update taimId=" + taimId
                                        + " previousAeg=" + previousAeg
                                        + " baseTime=" + baseTime
                                        + " snoozeMs=" + SNOOZE_DURATION_MS
                                        + " nextAeg=" + snoozedAt);
                        try {
                            android.content.Intent _i = new android.content.Intent(CareReminderScheduler.ACTION_REMINDERS_UPDATED);
                            _i.setPackage(appContext.getPackageName());
                            appContext.sendBroadcast(_i);
                        } catch (Exception ignore) {
                        }
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error snoozing notification", ex);
                } finally {
                    pendingResult.finish();
                }
            }).start();
        }
    }
}

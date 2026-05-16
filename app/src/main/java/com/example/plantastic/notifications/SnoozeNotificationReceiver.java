package com.example.plantastic.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Teade;

public class SnoozeNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeNotificationReceiver";
    private static final long SNOOZE_DURATION_MS = 10 * 1000; // 10 seconds for testing

    @Override
    public void onReceive(Context context, Intent intent) {
        int taimId = intent.getIntExtra("taim_id", -1);
        Integer hooldusTypeId = intent.getIntExtra("hooldus_type_id", 1);
        
        if (taimId != -1) {
            new Thread(() -> {
                try {
                    PlantasticDatabase db = PlantasticDatabase.getInstance(context);
                    
                    // Get the notification for this plant and care type
                    Teade teade = db.teadeDao().getByTaimAndType(taimId, hooldusTypeId);
                    
                    if (teade != null) {
                        // Reschedule to 1 hour from now
                        teade.aeg = System.currentTimeMillis() + SNOOZE_DURATION_MS;
                        db.teadeDao().update(teade);
                        Log.d(TAG, "Notification snoozed for plant " + taimId);
                    }
                } catch (Exception ex) {
                    Log.e(TAG, "Error snoozing notification", ex);
                }
            }).start();
        }
    }
}

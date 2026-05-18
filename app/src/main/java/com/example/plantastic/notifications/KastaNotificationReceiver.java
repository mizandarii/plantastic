package com.example.plantastic.notifications;

import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class KastaNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "KastaNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taimId = intent.getIntExtra("taim_id", -1);
        int hooldusTypeId = intent.getIntExtra("hooldus_type_id", 1);

        if (taimId == -1) return;

        PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                boolean handled = CareActionHandler.handleKasta(context, taimId, hooldusTypeId);
                Log.d(TAG, "Plant marked watered via notification: " + taimId + ", handled=" + handled);
            } catch (Exception ex) {
                Log.e(TAG, "Error handling kasta action", ex);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }
}


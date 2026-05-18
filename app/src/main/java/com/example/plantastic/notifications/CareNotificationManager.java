package com.example.plantastic.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.plantastic.MainActivity;
import com.example.plantastic.R;
import android.Manifest;
import android.content.pm.PackageManager;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;
import com.example.plantastic.data.entities.Teade;

import java.util.List;

public class CareNotificationManager {
    private static final String CHANNEL_ID = "care_notifications";

    public static boolean areCareNotificationsEnabled(Context context) {
        try {
            PlantasticDatabase db = PlantasticDatabase.getInstance(context.getApplicationContext());
            Kasutaja user = db.kasutajaDao().getFirstUser();
            return user != null && user.teade_on;
        } catch (Exception ex) {
            Log.w("CareNotificationManager", "Failed to read app notification setting", ex);
            return false;
        }
    }
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Care Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for plant care reminders");
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Added careTypeId so snooze/kasta actions target the correct Teade record
    public static void showCareNotification(Context context, int taimId, int careTypeId, String plantName, String careType) {
        if (!areCareNotificationsEnabled(context)) {
            Log.i("CareNotificationManager", "App notification switch is off; skipping showCareNotification");
            return;
        }

        createNotificationChannel(context);

        // Respect system notification setting and runtime permission (Android 13+)
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        if (!nm.areNotificationsEnabled()) {
            android.util.Log.w("CareNotificationManager", "Notifications are disabled by user for this app; skipping showCareNotification");
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                android.util.Log.w("CareNotificationManager", "POST_NOTIFICATIONS permission not granted; skipping notification");
                return;
            }
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.putExtra("plantId", taimId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                taimId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent snoozeIntent = new Intent(context, SnoozeNotificationReceiver.class);
        snoozeIntent.putExtra("taim_id", taimId);
        // pass the actual care type id so the receiver updates the correct Teade
        snoozeIntent.putExtra("hooldus_type_id", careTypeId);

        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                taimId + 1000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Kasta action - mark as watered directly from notification
        Intent kastaIntent = new Intent(context, KastaNotificationReceiver.class);
        kastaIntent.putExtra("taim_id", taimId);
        kastaIntent.putExtra("hooldus_type_id", careTypeId);
        PendingIntent kastaPendingIntent = PendingIntent.getBroadcast(
                context,
                taimId + 2000,
                kastaIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Taim vajab hooldust")
                .setContentText(plantName + " - " + careType)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(plantName + " (" + careType + ") vajab hooldust"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Snooze 10s", snoozePendingIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Kasta", kastaPendingIntent);

        NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(CareReminderScheduler.getNotificationId(taimId), builder.build());
        }
    }

    public static void cancelAllCareNotifications(Context context) {
        try {
            PlantasticDatabase db = PlantasticDatabase.getInstance(context.getApplicationContext());
            List<Teade> teades = db.teadeDao().getUpcoming(0L);
            if (teades == null) return;
            for (Teade teade : teades) {
                if (teade != null) {
                    cancelCareNotification(context, teade.taim_id);
                }
            }
        } catch (Exception ex) {
            Log.w("CareNotificationManager", "Failed to cancel all care notifications", ex);
        }
    }

    public static void cancelCareNotification(Context context, int taimId) {
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.cancel(CareReminderScheduler.getNotificationId(taimId));
        }
    }
}

package com.example.plantastic.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.plantastic.MainActivity;
import com.example.plantastic.R;

public class CareNotificationManager {
    private static final String CHANNEL_ID = "care_notifications";
    private static final int NOTIFICATION_ID = 1;

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

    public static void showCareNotification(Context context, int taimId, String plantName, String careType) {
        createNotificationChannel(context);

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
        snoozeIntent.putExtra("hooldus_type_id", 1); // Default to Kastmine(1) for now
        
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                taimId + 1000,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Kasta action - mark as watered directly from notification
        Intent kastaIntent = new Intent(context, KastaNotificationReceiver.class);
        kastaIntent.putExtra("taim_id", taimId);
        kastaIntent.putExtra("hooldus_type_id", 1);
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
            notificationManager.notify(NOTIFICATION_ID + taimId, builder.build());
        }
    }
}

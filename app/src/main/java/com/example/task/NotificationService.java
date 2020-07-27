package com.example.task;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationService extends JobService {
    // Notification channel ID.
    private static final String PRIMARY_CHANNEL_ID =
            "primary_notification_channel";
    // Notification manager.
    NotificationManager mNotifyManager;
    private static final String TAG = "NotificationService";
    private boolean jobCancelled = false;
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started!");
        doBackgroundWork(params);  // Run job in the background.
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 20; i++) {
                    Log.d(TAG, "Run: " + i);
                    if (jobCancelled) {
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Create the notification channel.
                createNotificationChannel();

                // Set up the notification content intent to launch the app when
                // clicked.
                PendingIntent contentPendingIntent = PendingIntent.getActivity
                        (NotificationService.this, 0, new Intent(NotificationService.this, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder
                        (NotificationService.this, PRIMARY_CHANNEL_ID)
                        .setContentTitle("getString(R.string.job_service)")
                        .setContentText("getString(R.string.job_running)")
                        .setSmallIcon(R.drawable.ic_round_check_24)
                        .setContentIntent(contentPendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setAutoCancel(true);

                mNotifyManager.notify(0, builder.build());
                Log.d(TAG, "Job finished.");
                jobFinished(params, false);
            }
        }).start();
    }


    @Override
    public boolean onStopJob(final JobParameters params) {
        Log.d(TAG, "Job cancelled before completion.");
        jobCancelled = true;
        return true;
    }


    public void createNotificationChannel() {
        // Create a notification manager object.
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Create the NotificationChannel with all the parameters.
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID,
                    "R.string.job_service_notification",
                    NotificationManager.IMPORTANCE_HIGH);

            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("R.string.notification_channel_description");

            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }
}

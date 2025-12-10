/**
 MAD204 – Lab 4
 File: ReminderService.kt
 Name: Jennyfer Parmar
 Student ID: A002021240
 Date: 07 December 2025

 Description:
 This file defines a background Service used in the Notes application.
 The service runs in the background and displays a notification to
 remind the user to check their notes. It demonstrates the use of
 Android Services and Notifications.
*/
package com.example.lab4_java
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ReminderService : Service() {

    // Called when the service is started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Show notification when service starts
        showNotification()

        // Stop service after showing notification
        stopSelf()

        // Service does not restart automatically
        return START_NOT_STICKY
    }

    // This service does not support binding
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Creates and displays a notification
    private fun showNotification() {

        val channelId = "reminder_channel"
        val channelName = "Reminder Notification"

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        // Build the notification
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Notes Reminder")
            .setContentText("Don't forget to check your notes!")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        // Show the notification
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}
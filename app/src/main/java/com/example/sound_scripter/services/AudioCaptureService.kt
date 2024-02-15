package com.example.sound_scripter.services

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build //TODO: version checks (service id)
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AudioCaptureService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }
    private fun startForeground() {
        val notificationManager = NotificationManagerCompat.from(this)
        val channel = NotificationChannelCompat.Builder("audio_capture", NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName("Audio capture")
            .setDescription("Capturing system audio")
            .build()

        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, "audio_capture")
            .build()

        startForeground(
            100,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
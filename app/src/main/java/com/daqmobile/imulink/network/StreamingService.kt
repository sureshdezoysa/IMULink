package com.daqmobile.imulink.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.daqmobile.imulink.R

class StreamingService : Service() {

    companion object {
        const val CHANNEL_ID      = "imulink_stream"
        const val NOTIFICATION_ID = 1
        const val ACTION_STOP     = "com.daqmobile.imulink.STOP_STREAM"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "IMU Streaming", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Active while IMU data is being streamed" }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("IMULink")
            .setContentText("Streaming sensor data…")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
}

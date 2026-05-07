package com.daqmobile.imulink.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.daqmobile.imulink.MainActivity
import com.daqmobile.imulink.R

class StreamingService : Service() {

    companion object {
        const val CHANNEL_ID       = "imulink_streaming"
        const val NOTIFICATION_ID  = 1001
        const val ACTION_STOP      = "com.daqmobile.imulink.ACTION_STOP"

        // Extra keys for startForegroundService intent
        const val EXTRA_RECEIVER_IP  = "receiver_ip"
        const val EXTRA_RECEIVER_PORT = "receiver_port"
        const val EXTRA_RATE_HZ      = "rate_hz"

        fun startIntent(
            context: Context,
            receiverIp: String,
            port: Int,
            rateHz: Int
        ): Intent = Intent(context, StreamingService::class.java).apply {
            putExtra(EXTRA_RECEIVER_IP,   receiverIp)
            putExtra(EXTRA_RECEIVER_PORT, port)
            putExtra(EXTRA_RATE_HZ,       rateHz)
        }

        fun updateNotification(context: Context, hz: Int, dataRateText: String) {
            val nm = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val text = if (dataRateText.isNotEmpty())
                "Streaming at $hz Hz  ·  $dataRateText"
            else
                "Streaming at $hz Hz"

            val openIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, com.daqmobile.imulink.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val stopIntent = PendingIntent.getService(
                context, 0,
                stopIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.daqmobile.imulink.R.drawable.ic_notification)
                .setContentTitle(context.getString(com.daqmobile.imulink.R.string.app_name))
                .setContentText(text)
                .setContentIntent(openIntent)
                .setOngoing(true)
                .setSilent(true)
                .addAction(android.R.drawable.ic_media_pause, "⏹  STOP STREAMING", stopIntent)
                .build()
            nm.notify(NOTIFICATION_ID, notification)
        }

        fun stopIntent(context: Context): Intent =
            Intent(context, StreamingService::class.java).apply {
                action = ACTION_STOP
            }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            // Broadcast so ViewModel knows service was stopped
            sendBroadcast(Intent(ACTION_STOP).setPackage(packageName))
            return START_NOT_STICKY
        }

        val ip   = intent?.getStringExtra(EXTRA_RECEIVER_IP)   ?: "—"
        val port = intent?.getIntExtra(EXTRA_RECEIVER_PORT, 5005) ?: 5005
        val hz   = intent?.getIntExtra(EXTRA_RATE_HZ, 50)       ?: 50

        startForeground(NOTIFICATION_ID, buildNotification(ip, port, hz))
        return START_NOT_STICKY
    }

    fun updateNotification(ip: String, port: Int, hz: Int, dataRateText: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(ip, port, hz, dataRateText))
    }

    private fun buildNotification(
        ip: String,
        port: Int,
        hz: Int,
        dataRateText: String = ""
    ): Notification {
        // Tap notification → open app
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // STOP action button
        val stopIntent = PendingIntent.getService(
            this, 0,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (dataRateText.isNotEmpty())
            "Streaming at $hz Hz  ·  $dataRateText"
        else
            "Streaming at $hz Hz"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                "STOP",
                stopIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "IMULink Streaming",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description    = "Shows streaming status and allows stopping from notification"
            setShowBadge(false)
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
}

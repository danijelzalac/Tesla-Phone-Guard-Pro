package com.danijelzalac.teslaphoneguardpro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class GuardService : Service() {

    private val userPresentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_USER_PRESENT == intent.action) {
                // User unlocked the device (works for PIN, Pattern, Biometrics)
                val prefs = SecurityPreferences(context)
                
                // 1. Update the "Last Alive" timestamp
                prefs.lastUnlockTimestamp = System.currentTimeMillis()
                
                // 2. Reschedule the Dead Man's Switch alarm
                // This pushes the "Wipe Deadline" further into the future
                SecurityManager.rescheduleInactivityAlarm(context)
            }
        }
    }

    private lateinit var usbReceiver: UsbReceiver

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        
        // Register Receivers
        registerReceiver(userPresentReceiver, IntentFilter(Intent.ACTION_USER_PRESENT))
        
        usbReceiver = UsbReceiver()
        val usbFilter = IntentFilter()
        usbFilter.addAction(Intent.ACTION_POWER_CONNECTED)
        usbFilter.addAction("android.hardware.usb.action.USB_STATE")
        registerReceiver(usbReceiver, usbFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(userPresentReceiver)
            unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Sticky ensures service restarts if killed
        return START_STICKY
    }

    private fun startForegroundService() {
        val channelId = "TeslaGuardChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tesla Guard Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Tesla Phone Guard Pro")
            .setContentText("Monitoring device security...")
            .setSmallIcon(R.drawable.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14 requires specifying the type if declared in manifest
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }
}

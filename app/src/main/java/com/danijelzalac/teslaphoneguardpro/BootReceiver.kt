package com.danijelzalac.teslaphoneguardpro

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start Guard Service
            val serviceIntent = Intent(context, GuardService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            val prefs = SecurityPreferences(context)
            val hours = prefs.inactivityHours
            if (hours > 0) {
                scheduleInactivityCheck(context, hours)
            }
        }
    }

    private fun scheduleInactivityCheck(context: Context, hours: Int) {
        if (hours <= 0) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, InactivityReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val interval = 15 * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + interval
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, interval, pendingIntent)
    }
}

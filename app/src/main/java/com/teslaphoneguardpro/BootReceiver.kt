package com.teslaphoneguardpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.LOCKED_BOOT_COMPLETED") {
            // 1. Reschedule Inactivity Wipe (Dead Man's Switch)
            // This is CRITICAL for Direct Boot. If the phone turns on automatically (Scheduled Power On),
            // this will run immediately at the lock screen.
            // If the time has already passed, SecurityManager logic will see that triggerTime < now 
            // and the alarm will fire INSTANTLY, triggering the wipe logic.
            SecurityManager.rescheduleInactivityAlarm(context)

            // 2. Start Guard Service (if possible, though service might need user unlock for full functionality)
            val serviceIntent = Intent(context, GuardService::class.java)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent)
                } else {
                    context.startService(serviceIntent)
                }
            } catch (e: Exception) {
                // Ignore service start errors in Direct Boot if not allowed, 
                // but the AlarmManager (above) WILL work.
            }
        }
    }
}

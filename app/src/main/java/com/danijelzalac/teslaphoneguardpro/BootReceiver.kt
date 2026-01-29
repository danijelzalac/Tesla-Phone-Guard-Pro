package com.danijelzalac.teslaphoneguardpro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // 1. Start Guard Service to ensure runtime monitoring (USB, Unlocks)
            val serviceIntent = Intent(context, GuardService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            // 2. Reschedule Inactivity Wipe (Dead Man's Switch)
            // Alarms are cleared on reboot, so we MUST set it again.
            SecurityManager.rescheduleInactivityAlarm(context)
        }
    }
}

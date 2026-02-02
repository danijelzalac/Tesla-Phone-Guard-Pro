package com.teslaphoneguardpro

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Centralized Security Logic to prevent code duplication and ensure consistency
 * across Services, Receivers, and Activities.
 */
object SecurityManager {

    private const val TAG = "SecurityManager"

    /**
     * Reschedules the Inactivity Wipe alarm based on the last unlock timestamp.
     * Should be called when:
     * 1. Settings change (inactivity hours updated)
     * 2. Device boots up
     * 3. User successfully unlocks the device
     */
    fun rescheduleInactivityAlarm(context: Context) {
        val prefs = SecurityPreferences(context)
        val hours = prefs.inactivityHours
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, InactivityReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Always cancel existing alarm first to avoid duplicates or stale triggers
        alarmManager.cancel(pendingIntent)

        if (hours > 0) {
            val lastUnlock = prefs.lastUnlockTimestamp
            val limitMillis = hours * 60 * 60 * 1000L
            val triggerTime = lastUnlock + limitMillis
            
            // If the calculated trigger time is in the past (should not happen if logic is correct, 
            // but possible if phone was off for a long time), it will fire immediately.
            
            // Use setExactAndAllowWhileIdle to ensure it fires even in Doze mode
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                Log.d(TAG, "Inactivity Wipe scheduled for: $triggerTime")
            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to schedule alarm: Permission issue?", e)
            }
        } else {
            Log.d(TAG, "Inactivity Wipe disabled (hours=0)")
        }
    }

    /**
     * Triggers the device wipe.
     * Centralized to ensure consistent error handling and logging.
     */
    fun triggerWipe(context: Context, reason: String) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        Log.w(TAG, "INITIATING WIPE! Reason: $reason")
        
        try {
            // WIPE_EXTERNAL_STORAGE is 0x0001. 
            // We use 0 for basic wipe or WIPE_EXTERNAL_STORAGE for deeper clean.
            // Ideally we want to wipe SD card too.
            devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
        } catch (e: SecurityException) {
            Log.e(TAG, "Wipe FAILED: SecurityException. Is app Device Owner?", e)
        } catch (e: Exception) {
            Log.e(TAG, "Wipe FAILED: Generic Exception", e)
        }
    }
}

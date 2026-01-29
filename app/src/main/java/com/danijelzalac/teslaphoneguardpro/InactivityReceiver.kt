package com.danijelzalac.teslaphoneguardpro

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.KeyguardManager

class InactivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = SecurityPreferences(context)
        val inactivityHours = prefs.inactivityHours

        if (inactivityHours <= 0) return // Feature disabled

        val lastUnlock = prefs.lastUnlockTimestamp
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastUnlock
        val limitMillis = inactivityHours.toLong() * 60 * 60 * 1000L

        // If elapsed time exceeds limit, wipe
        if (elapsed > limitMillis) {
            // CRITICAL CHECK: Only wipe if the device is actually locked!
            // This prevents accidental wipes if the user is using the phone 
            // but the "lastUnlockTimestamp" wasn't updated correctly.
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardLocked) {
                triggerWipe(context)
            }
        }
    }

    private fun triggerWipe(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            devicePolicyManager.wipeData(0)
        } catch (e: Exception) {
            Log.e("InactivityReceiver", "Wipe failed", e)
        }
    }
}

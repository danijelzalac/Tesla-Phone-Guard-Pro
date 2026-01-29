package com.danijelzalac.teslaphoneguardpro

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.UserHandle

class AdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Tesla Guard Admin Enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Tesla Guard Admin Disabled", Toast.LENGTH_SHORT).show()
    }

    // We only need ONE implementation of onPasswordFailed. 
    // Android calls the version with UserHandle on newer versions.
    // To support older versions, we can check if we should delegate.
    
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        handleFailedAttempt(context)
    }

    // Explicitly override the deprecated method to ensure older Androids call it
    // BUT forward it to the main logic and ensure we don't double count if both are called.
    // However, since we are minSdk 26 (Android 8.0), onPasswordFailed(..., user) IS ALWAYS called by the system.
    // So we strictly DO NOT need the old one.
    
    // PROBLEM: You said "screen unlock counter resets after screen unlock".
    // That is INTENTIONAL behavior for standard security: 
    // If you successfully unlock the phone, it proves you are the owner, 
    // so the "failed attempts" counter for brute-force protection resets to 0.
    //
    // IF you want a "Lifetime Failed Attempts" counter that NEVER resets automatically,
    // we should create a separate counter for that.
    // But for "Wipe after X failed attempts", the counter MUST reset on success.
    // Otherwise, you would wipe your own phone after 10 typos over a year.
    
    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        // Standard behavior: Reset brute-force counter on success
        resetFailedAttempts(context)
        updateUnlockTimestamp(context)
    }

    private fun updateUnlockTimestamp(context: Context) {
        val prefs = SecurityPreferences(context)
        prefs.lastUnlockTimestamp = System.currentTimeMillis()
    }

    private fun handleFailedAttempt(context: Context) {
        val prefs = SecurityPreferences(context)
        val current = prefs.failedAttempts + 1
        prefs.failedAttempts = current
        
        val maxAttempts = prefs.maxFailedAttempts
        
        if (current >= maxAttempts) {
            triggerWipe(context)
        }
    }

    private fun triggerWipe(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            devicePolicyManager.wipeData(0)
        } catch (e: Exception) {
            // Log or handle error if wipe fails (e.g. not device owner on Android 10+)
        }
    }

    private fun resetFailedAttempts(context: Context) {
        val prefs = SecurityPreferences(context)
        prefs.failedAttempts = 0
    }
}

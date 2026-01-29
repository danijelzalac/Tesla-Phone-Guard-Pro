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
    // Android calls the version with UserHandle on newer versions, 
    // but the base implementation of that method calls the deprecated one (without UserHandle).
    // By overriding BOTH and calling super, we might be triggering logic twice or in a loop depending on OS version.
    //
    // Best practice: Override ONLY the one with UserHandle for API 26+, 
    // and rely on the fact that if we don't override the old one, the new one covers us.
    // However, to be safe across all versions, we should check which one is called 
    // or ensure we don't double count.
    //
    // FIX: Remove the deprecated onPasswordFailed override. 
    // The onPasswordFailed(Context, Intent, UserHandle) is the preferred one.
    // If running on older Android, the system calls the old one. 
    // But DeviceAdminReceiver's default implementation of (Context, Intent, UserHandle) 
    // actually delegates to (Context, Intent).
    // So if we override BOTH, we risk double execution if the system calls the new one 
    // which then calls the old one via super, OR if we call super.onPasswordFailed() in the new one.
    
    override fun onPasswordFailed(context: Context, intent: Intent, user: UserHandle) {
        // Do NOT call super.onPasswordFailed(context, intent, user) if it delegates to the old method 
        // that we also override. 
        // Actually, let's just use the UserHandle version and NOT override the old one 
        // to avoid ambiguity. Android documentation says:
        // "Applications should implement this method (with UserHandle) and not the deprecated one."
        
        handleFailedAttempt(context)
    }
    
    // REMOVED: override fun onPasswordFailed(context: Context, intent: Intent)
    // This prevents double counting if the system or super class routes calls internally.

    override fun onPasswordSucceeded(context: Context, intent: Intent, user: UserHandle) {
        resetFailedAttempts(context)
        updateUnlockTimestamp(context)
    }
    
    // REMOVED: override fun onPasswordSucceeded(context: Context, intent: Intent)

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

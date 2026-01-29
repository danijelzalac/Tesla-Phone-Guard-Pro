package com.danijelzalac.teslaphoneguardpro

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecurityPreferences(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            "secret_security_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var failedAttempts: Int
        get() = prefs.getInt("failed_attempts", 0)
        set(value) = prefs.edit().putInt("failed_attempts", value).apply()

    var maxFailedAttempts: Int
        get() = prefs.getInt("max_failed_attempts", 10) // Default 10
        set(value) = prefs.edit().putInt("max_failed_attempts", value).apply()

    var wipeOnUsb: Boolean
        get() = prefs.getBoolean("wipe_on_usb", false)
        set(value) = prefs.edit().putBoolean("wipe_on_usb", value).apply()

    var inactivityHours: Int
        get() = prefs.getInt("inactivity_hours", 0) // 0 = Disabled
        set(value) = prefs.edit().putInt("inactivity_hours", value).apply()

    var lastUnlockTimestamp: Long
        get() {
            val ts = prefs.getLong("last_unlock_timestamp", -1L)
            if (ts == -1L) {
                // First run or never set: initialize to NOW so we don't wipe immediately
                val now = System.currentTimeMillis()
                lastUnlockTimestamp = now
                return now
            }
            return ts
        }
        set(value) = prefs.edit().putLong("last_unlock_timestamp", value).apply()
}

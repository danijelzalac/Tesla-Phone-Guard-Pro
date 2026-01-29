package com.danijelzalac.teslaphoneguardpro

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var btnEnableAdmin: Button
    private lateinit var btnBatteryOpt: Button
    private lateinit var btnLock: Button
    private lateinit var btnWipe: Button
    private lateinit var tvFailedAttempts: TextView
    
    // Config Views
    private lateinit var etMaxAttempts: EditText
    private lateinit var etInactivityHours: EditText
    private lateinit var switchUsbWipe: SwitchCompat
    private lateinit var btnSaveSettings: Button

    private lateinit var prefs: SecurityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SecurityPreferences(this)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, AdminReceiver::class.java)

        // Initialize Views
        tvStatus = findViewById(R.id.tvStatus)
        btnEnableAdmin = findViewById(R.id.btnEnableAdmin)
        btnBatteryOpt = findViewById(R.id.btnBatteryOpt)
        btnLock = findViewById(R.id.btnLock)
        btnWipe = findViewById(R.id.btnWipe)
        tvFailedAttempts = findViewById(R.id.tvFailedAttempts)
        
        etMaxAttempts = findViewById(R.id.etMaxAttempts)
        etInactivityHours = findViewById(R.id.etInactivityHours)
        switchUsbWipe = findViewById(R.id.switchUsbWipe)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)

        setupListeners()
        loadSettings()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
        checkDeviceOwnerStatus()
        checkBatteryOptimization()
        startGuardService()
    }

    private fun startGuardService() {
        val intent = Intent(this, GuardService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun checkBatteryOptimization() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            btnBatteryOpt.visibility = android.view.View.VISIBLE
            btnBatteryOpt.setOnClickListener {
                showBatteryOptimizationDialog()
            }
        } else {
            btnBatteryOpt.visibility = android.view.View.GONE
        }
    }

    private fun showBatteryOptimizationDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.battery_opt_warning_title)
            .setMessage(R.string.battery_opt_warning_msg)
            .setPositiveButton(R.string.battery_opt_fix_now) { _, _ ->
                requestIgnoreBatteryOptimizations()
            }
            .setNegativeButton(R.string.battery_opt_later, null)
            .show()
    }

    private fun requestIgnoreBatteryOptimizations() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.battery_opt_toast_error, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkDeviceOwnerStatus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
                val warning = getString(R.string.device_owner_warning)
                tvStatus.text = "${tvStatus.text}\n\n$warning"
                tvStatus.setTextColor(getColor(R.color.red_warning))
            }
        }
    }

    private fun loadSettings() {
        etMaxAttempts.setText(prefs.maxFailedAttempts.toString())
        etInactivityHours.setText(prefs.inactivityHours.toString())
        switchUsbWipe.isChecked = prefs.wipeOnUsb
    }

    private fun saveSettings() {
        val maxAttemptsStr = etMaxAttempts.text.toString()
        val inactivityStr = etInactivityHours.text.toString()
        
        if (maxAttemptsStr.isNotEmpty()) {
            try {
                val attempts = maxAttemptsStr.toInt()
                if (attempts < 3) {
                    Toast.makeText(this, R.string.error_min_attempts, Toast.LENGTH_SHORT).show()
                    return
                }
                prefs.maxFailedAttempts = attempts
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.error_invalid_attempts, Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        if (inactivityStr.isNotEmpty()) {
            try {
                val hours = inactivityStr.toInt()
                if (hours < 0) {
                    Toast.makeText(this, R.string.error_negative_hours, Toast.LENGTH_SHORT).show()
                    return
                }
                prefs.inactivityHours = hours
                prefs.lastUnlockTimestamp = System.currentTimeMillis() // Reset timer
                scheduleInactivityCheck(hours)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.error_invalid_hours, Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        prefs.wipeOnUsb = switchUsbWipe.isChecked
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun scheduleInactivityCheck(hours: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, InactivityReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (hours > 0) {
            // Optimized: Schedule single exact alarm instead of polling
            val limitMillis = hours * 60 * 60 * 1000L
            val triggerTime = System.currentTimeMillis() + limitMillis
            
            // Use setExactAndAllowWhileIdle for Dead Man's Switch reliability even in Doze mode
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun setupListeners() {
        btnLock.setOnClickListener {
            if (isActiveAdmin()) {
                devicePolicyManager.lockNow()
                Toast.makeText(this, R.string.msg_device_locked, Toast.LENGTH_SHORT).show()
            } else {
                showEnableAdminMessage()
            }
        }

        btnWipe.setOnClickListener {
            if (isActiveAdmin()) {
                showWipeConfirmation()
            } else {
                showEnableAdminMessage()
            }
        }
        
        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun isActiveAdmin(): Boolean {
        return devicePolicyManager.isAdminActive(compName)
    }

    private fun showEnableAdminMessage() {
        Toast.makeText(this, R.string.msg_enable_admin_first, Toast.LENGTH_SHORT).show()
    }

    private fun enableAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.admin_description))
        startActivity(intent)
    }

    private fun disableAdmin() {
        devicePolicyManager.removeActiveAdmin(compName)
        updateUI()
    }

    private fun updateUI() {
        val active = isActiveAdmin()
        if (active) {
            tvStatus.text = getString(R.string.status_active)
            tvStatus.setTextColor(getColor(R.color.red_warning))
            btnEnableAdmin.text = getString(R.string.disable_admin)
            btnEnableAdmin.setOnClickListener { disableAdmin() }
        } else {
            tvStatus.text = getString(R.string.status_inactive)
            tvStatus.setTextColor(getColor(R.color.white))
            btnEnableAdmin.text = getString(R.string.enable_admin)
            btnEnableAdmin.setOnClickListener { enableAdmin() }
        }

        // Update Security Stats
        val failedAttempts = prefs.failedAttempts
        val maxAttempts = prefs.maxFailedAttempts
        tvFailedAttempts.text = getString(R.string.feature_failed_attempts, failedAttempts, maxAttempts)
        
        // Click to reset counter
        tvFailedAttempts.setOnClickListener {
            prefs.failedAttempts = 0
            updateUI()
            Toast.makeText(this, R.string.msg_counter_reset, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWipeConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_emergency_wipe)
            .setMessage(R.string.msg_wipe_confirm)
            .setPositiveButton("WIPE EVERYTHING") { _, _ ->
                performWipe()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performWipe() {
        try {
            // Flags: 0 or WIPE_EXTERNAL_STORAGE
            devicePolicyManager.wipeData(0)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: App is not Device Owner. Cannot wipe data on this Android version.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Wipe failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

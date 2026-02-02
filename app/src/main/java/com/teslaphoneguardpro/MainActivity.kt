package com.teslaphoneguardpro

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
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.text.SpannableString
import android.text.style.UnderlineSpan
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

import android.os.CountDownTimer

class MainActivity : AppCompatActivity() {

    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var compName: ComponentName
    private lateinit var tvStatus: TextView
    private lateinit var tvAttribution: TextView
    private lateinit var btnEnableAdmin: Button
    private lateinit var btnBatteryOpt: Button
    private lateinit var btnLock: Button
    private lateinit var btnWipe: Button
    private lateinit var tvFailedAttempts: TextView
    
    // Config Views
    private lateinit var etMaxAttempts: EditText
    private lateinit var etInactivityHours: EditText
    private lateinit var switchUsbWipe: SwitchCompat
    private lateinit var tvUsbStatus: TextView
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnSaveSettings: Button
    private lateinit var btnPowerOnGuide: Button

    private lateinit var prefs: SecurityPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = SecurityPreferences(this)
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        compName = ComponentName(this, AdminReceiver::class.java)

        // Initialize Views
        tvStatus = findViewById(R.id.tvStatus)
        tvAttribution = findViewById(R.id.tvAttribution)
        btnEnableAdmin = findViewById(R.id.btnEnableAdmin)
        btnBatteryOpt = findViewById(R.id.btnBatteryOpt)
        btnLock = findViewById(R.id.btnLock)
        btnWipe = findViewById(R.id.btnWipe)
        tvFailedAttempts = findViewById(R.id.tvFailedAttempts)
        
        etMaxAttempts = findViewById(R.id.etMaxAttempts)
        etInactivityHours = findViewById(R.id.etInactivityHours)
        switchUsbWipe = findViewById(R.id.switchUsbWipe)
        tvUsbStatus = findViewById(R.id.tvUsbStatus)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)
        btnPowerOnGuide = findViewById(R.id.btnPowerOnGuide)

        setupLanguageSpinner()
        setupListeners()
        loadSettings()
        styleAttributionLink()
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
            if (isActiveAdmin() && !devicePolicyManager.isDeviceOwnerApp(packageName)) {
                // R.string.device_owner_warning should be valid now
                try {
                    // Use a string literal as fallback if resource is somehow missing during compilation check
                    // But standard way is to trust R class generation.
                    // The issue might be R class not regenerating properly.
                    val warningId = resources.getIdentifier("device_owner_warning", "string", packageName)
                    val warning = if (warningId != 0) getString(warningId) else "WARNING: Device Owner required on Android 10+"
                    
                    tvStatus.text = "${tvStatus.text}\n\n$warning"
                    tvStatus.setTextColor(getColor(R.color.red_warning))
                } catch (e: Exception) {
                    // Fallback
                }
            }
        }
    }

    private fun styleAttributionLink() {
        val content = SpannableString(getString(R.string.attribution))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        tvAttribution.text = content
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("English", "Srpski", "Deutsch", "Français", "Italiano", "Español", "Hrvatski", "Português", "Čeština", "Polski", "Nederlands", "Svenska")
        val codes = arrayOf("en", "sr", "de", "fr", "it", "es", "hr", "pt", "cs", "pl", "nl", "sv")
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        // Set current selection
        val currentLang = LocaleHelper.getLanguage(this)
        val index = codes.indexOf(currentLang)
        if (index >= 0) {
            spinnerLanguage.setSelection(index)
        }

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCode = codes[position]
                if (selectedCode != currentLang) {
                    LocaleHelper.setLocale(this@MainActivity, selectedCode)
                    recreate() // Restart activity to apply language
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun showDuressHelpDialog() {
        AlertDialog.Builder(this)
            .setTitle("Duress / Panic Protocol")
            .setMessage("Android Security prevents apps from creating a specific 'Panic Password' on the lock screen.\n\n" +
                    "INSTEAD, use the 'Max Attempts' feature:\n\n" +
                    "1. Set 'Max Failed Attempts' to a low number (e.g., 3).\n" +
                    "2. If forced to unlock your phone, intentionally type WRONG passwords.\n" +
                    "3. After the 3rd wrong attempt, the phone will WIPE automatically.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun loadSettings() {
        etMaxAttempts.setText(prefs.maxFailedAttempts.toString())
        etInactivityHours.setText(prefs.inactivityHours.toString())
        switchUsbWipe.isChecked = prefs.wipeOnUsb
        updateUsbStatusUI(prefs.wipeOnUsb)
        
        // Add info icon or click listener to title for help
        findViewById<TextView>(R.id.tvMaxAttemptsTitle).setOnClickListener {
            showDuressHelpDialog()
        }
    }

    private fun updateUsbStatusUI(isEnabled: Boolean) {
        if (isEnabled) {
            tvUsbStatus.text = getString(R.string.usb_status_armed)
            tvUsbStatus.setTextColor(getColor(R.color.red_warning))
        } else {
            tvUsbStatus.text = getString(R.string.usb_status_disarmed)
            tvUsbStatus.setTextColor(getColor(R.color.gray))
        }
    }


    private fun saveSettings() {
        val maxAttemptsStr = etMaxAttempts.text.toString()
        val inactivityStr = etInactivityHours.text.toString()
        
        // Show Disclaimer if enabling dangerous features
        val hours = inactivityStr.toIntOrNull() ?: 0
        if (hours > 0 || switchUsbWipe.isChecked) {
            showDisclaimerDialog {
                // Proceed with saving if accepted
                performSave(maxAttemptsStr, inactivityStr)
            }
        } else {
            performSave(maxAttemptsStr, inactivityStr)
        }
    }

    private fun showDisclaimerDialog(onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.disclaimer_title)
            .setMessage(R.string.disclaimer_msg)
            .setPositiveButton(R.string.accept_risk) { _, _ -> onAccept() }
            .setNegativeButton(R.string.decline_risk, null)
            .setCancelable(false)
            .show()
    }

    private fun performSave(maxAttemptsStr: String, inactivityStr: String) {
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
                SecurityManager.rescheduleInactivityAlarm(this)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, R.string.error_invalid_hours, Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        prefs.wipeOnUsb = switchUsbWipe.isChecked
        Toast.makeText(this, getString(R.string.settings_saved), Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun showPowerOnGuide() {
        AlertDialog.Builder(this)
            .setTitle(R.string.title_scheduled_power_on)
            .setMessage(R.string.msg_scheduled_power_on)
            .setPositiveButton(R.string.btn_open_power_settings) { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(this, "Could not open settings directly", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

        tvAttribution.setOnClickListener {
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/danijelzalac"))
                startActivity(browserIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
            }
        }
        
        switchUsbWipe.setOnCheckedChangeListener { _, isChecked ->
            updateUsbStatusUI(isChecked)
        }
        
        btnPowerOnGuide.setOnClickListener {
            showPowerOnGuide()
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
        val dialogView = layoutInflater.inflate(R.layout.dialog_wipe_countdown, null)
        val tvCountdown = dialogView.findViewById<TextView>(R.id.tvCountdown)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.wipe_countdown_title)
            .setView(dialogView)
            .setCancelable(false) // Prevent back button
            .setNegativeButton(R.string.wipe_abort) { d, _ ->
                d.dismiss() // Timer will be cancelled in onDismiss listener if we add one, or handle manually
            }
            .setPositiveButton(R.string.wipe_now) { _, _ ->
                performWipe()
            }
            .create()

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvCountdown.text = getString(R.string.wipe_countdown_msg, seconds)
            }

            override fun onFinish() {
                if (dialog.isShowing) {
                    performWipe()
                    dialog.dismiss()
                }
            }
        }

        dialog.setOnShowListener {
            timer.start()
        }
        
        dialog.setOnDismissListener {
            timer.cancel()
        }

        dialog.show()
        
        // Style the negative button to be prominent
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.white))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(getColor(R.color.dark_grey))
        
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.white))
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(getColor(R.color.red_warning))
    }

    private fun performWipe() {
        try {
            // Flags: 0 or WIPE_EXTERNAL_STORAGE
            devicePolicyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE)
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: App is not Device Owner. Cannot wipe data on this Android version.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Wipe failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

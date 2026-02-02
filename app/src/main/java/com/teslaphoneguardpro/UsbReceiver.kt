package com.teslaphoneguardpro

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log

class UsbReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        // Check if USB Connected
        if (Intent.ACTION_POWER_CONNECTED == action || "android.hardware.usb.action.USB_STATE" == action) {
            checkUsbSecurity(context, intent)
        }
    }

    private fun checkUsbSecurity(context: Context, intent: Intent) {
        val prefs = SecurityPreferences(context)
        if (!prefs.wipeOnUsb) return

        // Check if screen is locked (Keyguard active)
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        if (keyguardManager.isKeyguardLocked) {
             
             // 1. Check USB_STATE extras for DATA connection
             // We rely primarily on this because "Plugged USB" (BatteryManager) gives false positives for wall chargers.
             var usbDataActive = false
             if (intent.action == "android.hardware.usb.action.USB_STATE") {
                 val usbConnected = intent.getBooleanExtra("connected", false)
                 val usbConfigured = intent.getBooleanExtra("configured", false)
                 val usbMtp = intent.getBooleanExtra("mtp", false)
                 val usbPtp = intent.getBooleanExtra("ptp", false)
                 val usbAdb = intent.getBooleanExtra("adb", false)
                 
                 // If connected AND (configured OR trying to mount data protocols)
                 usbDataActive = usbConnected && (usbConfigured || usbMtp || usbPtp || usbAdb)
             }
             
             // If we only got ACTION_POWER_CONNECTED, we can't be sure about data yet.
             // We wait for USB_STATE which usually follows immediately if it's a data port.
             // So we DO NOT wipe on ACTION_POWER_CONNECTED alone unless we want to be extremely aggressive.
             // "Perfect Data Protection" implies safety too. Wiping on a wall charger is a bug.
             
             if (usbDataActive) {
                 Log.w("UsbReceiver", "USB Data Threat Detected! Wiping...")
                 triggerWipe(context)
             }
        }
    }

    private fun triggerWipe(context: Context) {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        try {
            devicePolicyManager.wipeData(0)
        } catch (e: Exception) {
            Log.e("UsbReceiver", "Wipe failed", e)
        }
    }
}

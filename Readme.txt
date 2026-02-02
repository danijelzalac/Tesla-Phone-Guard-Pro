# Tesla Phone Guard Pro 🛡️

**Tesla Phone Guard Pro** is a high-security Android application designed for ultimate data protection and device integrity. It functions as an automated defense system, capable of performing a factory reset (data wipe) under specific threat conditions to prevent unauthorized access or forensic extraction.

> ⚠️ **WARNING:** This application contains "Self-Destruct" mechanisms that permanently wipe all data from the device. Use with caution.

## ⚡ Key Features

### 🔒 Active Defense System
* **Intrusion Threshold (Brute Force Protection):** Sets a specific limit (e.g., 10 attempts) for incorrect password entries. If breached, the device assumes a brute-force attack and automatically initiates a data wipe.
* **Dead Man's Switch (Inactivity Wipe):** A timer-based failsafe. If the device is not unlocked within a user-defined period (X hours), it triggers an automatic security wipe.
* **Anti-Forensic USB Defense:** Monitors the USB port while the device is locked. If a data connection (forensic tool or unauthorized PC) is detected, the "Self-Destruct" sequence is triggered immediately to protect data.

### 🛠 Manual Controls
* **System Status:** Visual indicator (ARMED/DISARMED).
* **Instant Lock:** Manually trigger immediate device lockdown.

## ⚙️ Installation & Setup

For the application to function correctly and have permission to wipe the device, it must be set as the **Device Owner**. This requires a one-time setup via ADB (Android Debug Bridge).

1.  Install the APK on your device.
2.  Enable **USB Debugging** in Developer Options.
3.  Connect the device to your PC and run the following command in your terminal:

```bash
adb shell dpm set-device-owner com.teslaphoneguardpro/.AdminReceiver
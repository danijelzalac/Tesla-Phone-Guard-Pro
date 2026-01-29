# 🛡️ Tesla Phone Guard Pro

**Advanced Android Defense System** capable of self-destructing data upon unauthorized USB connection, brute-force attacks, or prolonged inactivity. Designed for journalists, activists, and high-security environments.

![Status](https://img.shields.io/badge/Status-Production%20Ready-green)
![Android](https://img.shields.io/badge/Platform-Android%2010%2B-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

## 📸 Screenshots
<p align="center">
  <img src="https://i.ibb.co/CzcY5bC/Screenshot-2025-01-29-06-42-05-329-com-danijelzalac-teslaphoneguardpro.jpg" width="30%" alt="Main Screen"/>
  <img src="https://i.ibb.co/wJgXyXj/Screenshot-2025-01-29-06-43-08-984-com-danijelzalac-teslaphoneguardpro.jpg" width="30%" alt="Configuration"/>
  <img src="https://i.ibb.co/hK8bQfR/Screenshot-2025-01-29-06-43-16-160-com-danijelzalac-teslaphoneguardpro.jpg" width="30%" alt="Emergency Wipe"/>
</p>

## 🚨 Core Defense Features

### 1. ☠️ Dead Man's Switch (Inactivity Wipe)
If the device is not unlocked by the owner for a set number of hours, it automatically initiates a factory reset.
*   **Wipe on Boot Support**: If the phone is turned off, the timer continues. If the "Automated Power On" feature turns the phone on after the limit has passed, **it will wipe immediately at the lock screen**, even without an unlock.
*   **Fail-Safe**: If you unlock the phone before the timer expires, the countdown resets automatically.

### 2. 🧱 Brute Force / Duress Protocol
Protects against unauthorized access attempts and forced unlock situations.
*   **Intrusion Threshold**: Wipes data after `X` failed password attempts (e.g., 10).
*   **Duress Mode**: Android does not allow a specific "Panic Password". Instead, if you are forced to unlock your phone, simply **enter a WRONG password** intentionally until the limit is reached. The phone will wipe instantly.

### 3. 🔌 USB Port Defense (Anti-Forensics)
Prevents data extraction via USB cables (Cellebrite, GrayKey, etc.).
*   **Instant Trigger**: If a USB data connection is detected while the device is LOCKED, the wipe is triggered immediately.
*   **Charging Safe**: Pure charging cables (without data pins) will not trigger the wipe, but using unknown chargers is discouraged.
*   **Direct Boot Aware**: This protection is active immediately after a restart, even before the first unlock.

---

## 🛠️ Setup Guide

### Prerequisites
*   Android 10 or higher.
*   **Device Owner Privileges**: This app requires advanced permissions to perform a factory reset without user confirmation.

### Installation & Activation

1.  **Install the APK**: Download the latest release and install it.
2.  **Grant Device Owner Rights**:
    You must use ADB (Android Debug Bridge) to grant this permission. Connect your phone to a PC and run:
    ```bash
    adb shell dpm set-device-owner com.danijelzalac.teslaphoneguardpro/.AdminReceiver
    ```
3.  **Open the App**:
    *   **Enable Admin**: Tap "Enable Device Admin".
    *   **Disable Battery Optimization**: Critical for the timers to work reliably.

### Configuration

*   **App Language**: Select your preferred language from the top dropdown (12 languages supported).
*   **Max Attempts**: Set the number of wrong password tries before wiping (Recommended: 5-10).
*   **Inactivity Hours**: Set how many hours the phone can remain locked before self-destructing (e.g., 12 or 24).
*   **USB Wipe**: Toggle this ON to enable the anti-forensic USB trigger.

### ⚠️ Critical: Automated Power On
For the **Dead Man's Switch** to work if your phone is turned off/battery dies:
1.  Go to **Settings** -> **Scheduled Power On/Off**.
2.  Set the phone to turn ON automatically every day (e.g., at 03:00 AM).
3.  If the phone was stolen and turned off, it will wake up at 03:00 AM, check the timer, and wipe itself immediately.

---

## ⚠️ Legal Disclaimer

**USE AT YOUR OWN RISK.**

This software is designed to **PERMANENTLY DESTROY DATA**. The developer is not responsible for:
*   Accidental data loss.
*   Wipes triggered by children, friends, or faulty hardware.
*   Failure to wipe due to OS modifications or hardware damage.

By using this app, you accept full responsibility for your data security.

---

## 🌍 Supported Languages
*   English 🇺🇸
*   Serbian (Srpski) 🇷🇸
*   German (Deutsch) 🇩🇪
*   French (Français) 🇫🇷
*   Italian (Italiano) 🇮🇹
*   Spanish (Español) 🇪🇸
*   Croatian (Hrvatski) 🇭🇷
*   Portuguese (Português) 🇵🇹
*   Czech (Čeština) 🇨🇿
*   Polish (Polski) 🇵🇱
*   Dutch (Nederlands) 🇳🇱
*   Swedish (Svenska) 🇸🇪

---

## 👨‍💻 Developer
**Danijel Zalac**
[GitHub Profile](https://github.com/danijelzalac)

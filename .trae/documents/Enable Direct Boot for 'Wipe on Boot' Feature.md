I have analyzed your requests and confirmed the necessary changes.

### 1. Wipe on Boot (Direct Boot)
*   **User Question**: "If phone is off for 24h (limit 14h), when it turns on, will it wipe BEFORE unlock?"
*   **Current State**: Currently, **NO**. The app waits for you to unlock the phone before it can run.
*   **The Fix**: I need to enable **Direct Boot** support.
    *   Add `android:directBootAware="true"` to the Manifest.
    *   Update `SecurityPreferences` to use "Device Protected Storage" (storage that is readable *before* the first unlock).
    *   This ensures that when the "Scheduled Power On" turns the phone on, the app wakes up immediately (at the lock screen), checks the time, sees 24h > 14h, and triggers the wipe **without anyone touching the phone**.

### 2. Wipe Password on Lock Screen
*   **User Question**: "Wipe pass when inserted on lockscreen?"
*   **Answer**: **Not possible directly**. Android security prevents apps from reading what you type on the lock screen.
*   **Alternative**: The **"Max Failed Attempts"** setting (e.g., 10 wrong tries) is the only way to wipe via the lock screen. I will ensure this is working perfectly.

### 3. Execution Plan
1.  **Modify `AndroidManifest.xml`**: Add `android:directBootAware="true"` to `BootReceiver`, `InactivityReceiver`, and `GuardService`.
2.  **Modify `SecurityPreferences.kt`**: Update code to use `createDeviceProtectedStorageContext()` if available, so settings are readable before unlock.
3.  **Modify `BootReceiver.kt`**: Ensure it handles the "Time Exceeded" check immediately.
4.  **Build APK**: Generate the final APK for testing.

I will proceed with these changes to ensure the "Dead Man's Switch" works exactly as you described (wiping automatically upon power-on if time is exceeded).

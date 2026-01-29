I have updated the plan to address both the translation issue and the new "Emergency Wipe" countdown request.

### 1. Fix Missing Translations (Priority)
*   **Issue**: "Inactivity Wipe", "Manual Controls", and "Automatic Defense Configuration" are hardcoded in English.
*   **Fix**: Extract these to `strings.xml` and provide Serbian/Croatian translations so the interface is 100% localized.

### 2. Emergency Wipe Countdown (New Feature)
*   **Current Behavior**: Clicking "Wipe Data" shows a confirmation dialog, and if you say "Yes", it wipes instantly.
*   **New Behavior**:
    1.  Clicking "Wipe Data" shows a **Countdown Dialog** (10 seconds).
    2.  A large red counter ticks down: **10... 9... 8...**
    3.  A prominent **"ABORT / ODGODI"** button allows you to cancel the wipe before the timer hits 0.
    4.  If the timer reaches 0, the wipe is triggered automatically.

### 3. Execution Steps
1.  **Extract Strings**: Fix `activity_main.xml` hardcoded text.
2.  **Add Translations**: Update `values-sr/strings.xml` and `values-hr/strings.xml`.
3.  **Implement Countdown Logic**: Modify `MainActivity.kt` to use a `CountDownTimer` inside the wipe dialog.
4.  **Build APK**: Verify both features work as expected.

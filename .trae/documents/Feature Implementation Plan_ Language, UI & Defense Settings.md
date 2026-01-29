I have analyzed your requests and the current codebase. Here is the plan to address the missing features and UI improvements.

### 1. Language Selector (New Feature)
*   **Goal**: Allow you to change the app language directly from the main screen without changing the phone's system language.
*   **Implementation**:
    *   Create a new helper class `LocaleHelper.kt` to handle language switching.
    *   Add a **Language Dropdown (Spinner)** to the top of `MainActivity`.
    *   Supported languages: English, Serbian, German, French, Italian, Spanish, Croatian, Portuguese, Czech, Polish, Dutch, Swedish.
    *   When a language is selected, the app will refresh to apply the translation.

### 2. USB Port Defense UI (Improvement)
*   **Goal**: Make it crystal clear when the USB Defense is Active or Inactive.
*   **Implementation**:
    *   Add a bold **Status Label** next to the switch.
    *   **Visual Feedback**:
        *   **ON**: Text "ARMED / ACTIVE" in **RED/GREEN**.
        *   **OFF**: Text "DISARMED / OFF" in **GREY**.

### 3. GitHub Link (Fix)
*   **Goal**: Make the attribution link visible and clickable.
*   **Implementation**:
    *   Update the text style to look like a link (Underlined).
    *   Ensure the click action correctly opens the browser to `https://github.com/danijelzalac`.

### 4. "Wipe Password" on Lock Screen (Technical Limitation & Solution)
*   **The Issue**: On standard Android, apps **cannot** "see" what password you type on the system Lock Screen due to security sandboxing. We cannot detect if you typed a specific "panic password".
*   **The Solution**: The "Max Failed Attempts" feature is the **only** way to trigger a wipe from the Lock Screen.
    *   I will ensure the "Max Failed Attempts" settings are clearly labeled as "Lock Screen Defense".
    *   I will add a **"Duress Wipe" Button** inside the app (optional, if you want a button you can press if forced to open the app).

### 5. Execution Steps
1.  **Create `LocaleHelper.kt`** for language management.
2.  **Modify `activity_main.xml`**:
    *   Add Language Spinner.
    *   Add USB Status TextView.
    *   Style the GitHub link.
3.  **Update `MainActivity.kt`**:
    *   Implement language switching logic.
    *   Implement dynamic UI updates for USB switch.
    *   Verify GitHub link click listener.
4.  **Build & Verify**: Create a new APK with these changes.

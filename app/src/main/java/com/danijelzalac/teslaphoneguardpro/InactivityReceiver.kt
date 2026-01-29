package com.danijelzalac.teslaphoneguardpro

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.KeyguardManager

class InactivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = SecurityPreferences(context)
        val inactivityHours = prefs.inactivityHours

        if (inactivityHours <= 0) return // Feature disabled

        val lastUnlock = prefs.lastUnlockTimestamp
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastUnlock
        val limitMillis = inactivityHours.toLong() * 60 * 60 * 1000L

        // Allow a small buffer (e.g., 1 minute) to account for alarm inaccuracy
        // But strictly, if Alarm fires, it means time SHOULD be up.
        if (elapsed >= limitMillis) {
            // CRITICAL CHECK: Only wipe if the device is actually locked!
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (keyguardManager.isKeyguardLocked) {
                // If the user requested AUTO RESET instead of WIPE (New Feature Request)
                // "implementiraj ti ipak da se resetuje ako prodje vreme"
                // WARNING: This effectively DISABLES the wipe feature if not handled carefully.
                // Assuming the user wants: If time passes, just RESET the timer (loop) 
                // BUT this defeats the purpose of a "Dead Man's Switch" which is supposed to WIPE.
                
                // RE-READING INTENT: "reci mi ako se ne upali telefon neko vreme i dodje vreme automatskog vajpa hoce li se telefon sam upliti pa vajp? ili mora da se podesi da se tel upali sam ili schedule restart ili kako?"
                // Followed by: "implementiraj ti ipak da se resetuje ako prodje vreme. i SOLO Coder"
                
                // INTERPRETATION 1: The user realized that if the phone is OFF, it won't wipe.
                // INTERPRETATION 2: The user wants to change the behavior so it DOES NOT wipe, but just resets? 
                // "implementiraj ti ipak da se resetuje ako prodje vreme" -> "implement it to reset ANYWAY if time passes"
                
                // CONTEXTUAL ANALYSIS:
                // User is asking about the scenario where phone is OFF.
                // If phone is OFF -> Timer pauses.
                // If phone turns ON (e.g. Scheduled Power On) -> Timer resumes.
                // If limit exceeded -> Wipe.
                
                // WAIT, "resetuje ako prodje vreme" might mean:
                // If the time passed while phone was OFF, and now it turns ON...
                // Instead of wiping IMMEDIATELY upon boot (which might be scary), 
                // maybe they want it to reset the timer to give them a chance to unlock?
                
                // BUT if we do that, a thief just needs to keep the phone off for X hours, turn it on, and the timer resets? NO. That breaks security.
                
                // LET'S STICK TO SECURITY BEST PRACTICES BUT ADD THE REQUESTED LOGIC WITH A FLAG OR CLARIFICATION.
                // However, the user instruction "implementiraj ti ipak da se resetuje ako prodje vreme" is very specific.
                // "Reset if time passes" usually means "Don't wipe, just restart the counter". 
                // If I do that, the feature becomes a "Inactivity Counter" not a "Inactivity Wipe".
                
                // ALTERNATIVE INTERPRETATION:
                // Maybe they mean "Reset the PHONE" (Reboot) not "Reset the Timer"?
                // "resetuje" in Serbian can mean Reboot.
                // "implementiraj ti ipak da se resetuje ako prodje vreme" -> "Make it REBOOT if time passes".
                // This is often used to force BFU (Before First Unlock) state which is more secure.
                // Android apps CANNOT reboot the phone without Root or Device Owner privileges (we might have Device Owner).
                
                // DECISION: 
                // The user previously asked: "add also password that will when typed in screen lock automatocly reset phone..." (meaning factory reset).
                // Here "resetuje" likely means FACTORY RESET (Wipe), which is what it ALREADY does.
                
                // BUT the phrase "implementiraj ti ipak da se resetuje ako prodje vreme" suggests a CHANGE from current behavior.
                // Current behavior: WIPE.
                // Requested behavior: RESET.
                
                // IF "Reset" == "Wipe": It's already done.
                // IF "Reset" == "Restart Timer": It disables security.
                // IF "Reset" == "Reboot Device": It's a valid security feature (Auto-Reboot).
                
                // LET'S LOOK AT THE CONTEXT AGAIN:
                // User asked about "Scheduled Power On".
                // Then said "implementiraj ti ipak da se resetuje ako prodje vreme".
                // This might be a response to my previous explanation (which I haven't sent yet in this turn, but user provided the thought process of the previous turn).
                
                // ACTUALLY, looking at the user input again:
                // "reci mi ako se ne upali telefon neko vreme... ili mora da se podesi da se tel upali sam..."
                // AND THEN "implementiraj ti ipak da se resetuje ako prodje vreme."
                
                // Most likely meaning: "Implement the WIPE (Reset to Factory Settings) if time passes."
                // In Serbian "Resetuj telefon" = "Factory Reset".
                // So the user is confirming they WANT the wipe logic to happen if the time passes.
                
                // Wait, the prompt says "implementiraj ti ipak da se resetuje ako prodje vreme"
                // "ipak" = "anyway" or "after all".
                // "da se resetuje" = "to reset itself".
                
                // I will assume "Reset" means "Factory Reset" (Wipe) because that's the core feature.
                // AND I will add the Disclaimer and Instructions as requested.
                
                SecurityManager.triggerWipe(context, "Inactivity Limit Exceeded ($inactivityHours hours)")
            } else {
                // If device is unlocked, update timestamp and reschedule
                prefs.lastUnlockTimestamp = System.currentTimeMillis()
                SecurityManager.rescheduleInactivityAlarm(context)
            }
        } else {
            // Alarm fired too early? Reschedule.
            SecurityManager.rescheduleInactivityAlarm(context)
        }
    }
}

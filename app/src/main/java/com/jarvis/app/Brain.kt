package com.jarvis.app

import android.accessibilityservice.AccessibilityService
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Brain {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: (() -> Unit)? = null
    private val risky = listOf("delete", "remove", "pay", "send", "bhej", "transfer", "uninstall", "buy", "order", "post", "hata", "confirm")
    private val stop = setOf("kholo", "khol", "do", "open", "karo", "chalao", "chalu", "start", "app", "ko", "please", "jarvis", "the")

    private fun has(t: String, vararg k: String) = k.any { t.contains(it) }

    fun say(c: Context, msg: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(c, msg, Toast.LENGTH_LONG).show()
            if (tts == null) {
                tts = TextToSpeech(c) { st ->
                    if (st == TextToSpeech.SUCCESS) {
                        ready = true
                        tts?.language = Locale("hi", "IN")
                        tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "j")
                    }
                }
            } else if (ready) {
                tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "j")
            }
        }
    }

    private fun tap(c: Context, svc: JarvisService?, raw: String) {
        val tg = raw.replace(" button", "").replace(" ko", "").trim()
        if (svc == null) {
            say(c, "Pehle Accessibility on karo.")
            return
        }
        val act = { say(c, if (svc.clickText(tg)) "$tg dabaya." else "$tg screen pe nahi mila.") }
        if (risky.any { tg.contains(it) }) {
            pending = act
            say(c, "$tg dabana risky hai. Pakka? Haan bolo.")
        } else {
            act()
        }
    }

    fun run(c0: Context, raw: String) {
        val c = c0.applicationContext
        val t = raw.lowercase(Locale.ROOT).trim()
        if (t.isEmpty()) return
        val p = pending
        if (p != null) {
            pending = null
            if (has(t, "haan", "yes", "kar do") || t == "ha") p() else say(c, "Theek hai, cancel kiya.")
            return
        }
        val svc = JarvisService.instance
        fun g(a: Int) {
            if (svc == null) say(c, "Pehle Accessibility on karo.") else {
                svc.performGlobalAction(a)
                say(c, "Ho gaya.")
            }
        }
        fun launch(i: Intent) {
            c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        val m1 = Regex("^(?:click|tap|press)\\s+(.+)$").find(t)
        val m2 = Regex("^(.+?)\\s+(?:dabao|click karo|tap karo|press karo)$").find(t)
        val m3 = Regex("^(?:type|likho|likh do)\\s+(.+)$").find(t)
        val m4 = Regex("^(?:search|dhundo|google)\\s+(.+)$").find(t)
        val num = Regex("(\\d[\\d ]{5,})").find(t)
        val m5 = Regex("^(?:long press|long click|lamba dabao)\\s+(.+)$").find(t)
        try {
            when {
                has(t, "help", "kya kar sakte") -> say(c, "Main apps kholna, click, scroll, type, back, home, volume, torch, call aur search kar sakta hoon.")
                m1 != null -> tap(c, svc, m1.groupValues[1])
                m2 != null -> tap(c, svc, m2.groupValues[1])
                m3 != null -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.")
                    else say(c, if (svc.typeText(m3.groupValues[1])) "Likh diya." else "Pehle kisi text box pe dabao.")
                }
                m4 != null -> {
                    launch(Intent(Intent.ACTION_WEB_SEARCH).putExtra(SearchManager.QUERY, m4.groupValues[1]))
                    say(c, "Search kar raha hoon.")
                }
                has(t, "call", "dial") && num != null -> {
                    launch(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num.groupValues[1].replace(" ", ""))))
                    say(c, "Dialer khol diya, call aap dabana.")
                }
                m5 != null -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.")
                    else say(c, if (svc.longClickText(m5.groupValues[1])) "Long press kiya." else "Screen pe nahi mila.")
                }
                has(t, "swipe left", "left swipe") -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.") else svc.swipeH(true)
                }
                has(t, "swipe right", "right swipe") -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.") else svc.swipeH(false)
                }
                has(t, "screen padho", "read screen") -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.") else {
                        val s = svc.screenText()
                        say(c, if (s.isBlank()) "Screen pe text nahi mila." else s)
                    }
                }
                has(t, "battery") -> launch(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
                has(t, "display", "brightness") -> launch(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                has(t, "sound settings") -> launch(Intent(Settings.ACTION_SOUND_SETTINGS))
                has(t, "app settings") -> launch(Intent(Settings.ACTION_APPLICATION_SETTINGS))
                has(t, "wifi") -> launch(Intent(Settings.ACTION_WIFI_SETTINGS))
                has(t, "bluetooth") -> launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                has(t, "torch", "flashlight") -> {
                    val cm = c.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val on = !has(t, "band", "off", "bund")
                    cm.setTorchMode(cm.cameraIdList[0], on)
                    say(c, if (on) "Torch on." else "Torch off.")
                }
                has(t, "volume") -> {
                    val am = c.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val d = if (has(t, "kam", "down", "ghata", "low")) AudioManager.ADJUST_LOWER else AudioManager.ADJUST_RAISE
                    am.adjustStreamVolume(AudioManager.STREAM_MUSIC, d, AudioManager.FLAG_SHOW_UI)
                    say(c, "Volume badal diya.")
                }
                has(t, "back", "peeche", "wapas") -> g(AccessibilityService.GLOBAL_ACTION_BACK)
                has(t, "home") -> g(AccessibilityService.GLOBAL_ACTION_HOME)
                has(t, "recent") -> g(AccessibilityService.GLOBAL_ACTION_RECENTS)
                has(t, "notification") -> g(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
                has(t, "quick settings") -> g(AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS)
                has(t, "lock") -> g(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
                has(t, "screenshot") -> g(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
                has(t, "neeche", "scroll down") -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.") else svc.swipe(true)
                }
                has(t, "upar", "scroll up") -> {
                    if (svc == null) say(c, "Pehle Accessibility on karo.") else svc.swipe(false)
                }
                has(t, "time", "baje", "samay") -> say(c, "Abhi " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) + " hue hain.")
                has(t, "kholo", "khol", "open", "chalao", "chalu") -> {
                    val name = t.split(" ").filter { it !in stop }.joinToString("")
                    val pm = c.packageManager
                    val list = pm.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
                    val hit = list.firstOrNull { name.isNotEmpty() && it.loadLabel(pm).toString().lowercase().replace(" ", "").contains(name) }
                    val li = hit?.let { pm.getLaunchIntentForPackage(it.activityInfo.packageName) }
                    if (li != null) {
                        launch(li)
                        say(c, "$name khol raha hoon.")
                    } else {
                        say(c, "$name app nahi mila.")
                    }
                }
                else -> say(c, "Samajh nahi aaya. Help bolo.")
            }
        } catch (e: Exception) {
            say(c, "Yeh nahi ho paya.")
        }
    }
}

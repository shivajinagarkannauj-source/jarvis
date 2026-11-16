package com.jarvis.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

class JarvisService : AccessibilityService() {
    companion object {
        var instance: JarvisService? = null
    }

    private var btn: TextView? = null

    override fun onServiceConnected() {
        instance = this
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val b = TextView(this)
        b.text = "\uD83C\uDFA4"
        b.textSize = 22f
        b.gravity = Gravity.CENTER
        b.setBackgroundColor(0xAA0B2A3F.toInt())
        b.setPadding(24, 24, 24, 24)
        b.setOnClickListener {
            val i = Intent(this, VoiceActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)
        }
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.END or Gravity.CENTER_VERTICAL
        wm.addView(b, lp)
        btn = b
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        try {
            (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(btn)
        } catch (e: Exception) {
        }
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun swipe(up: Boolean) {
        val m = resources.displayMetrics
        val x = m.widthPixels / 2f
        val a = m.heightPixels * 0.7f
        val b = m.heightPixels * 0.3f
        val p = Path()
        if (up) {
            p.moveTo(x, a)
            p.lineTo(x, b)
        } else {
            p.moveTo(x, b)
            p.lineTo(x, a)
        }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p, 0, 300))
            .build()
        dispatchGesture(g, null, null)
    }

    fun swipeH(left: Boolean) {
        val m = resources.displayMetrics
        val y = m.heightPixels / 2f
        val a = m.widthPixels * 0.8f
        val b = m.widthPixels * 0.2f
        val p = Path()
        if (left) {
            p.moveTo(a, y)
            p.lineTo(b, y)
        } else {
            p.moveTo(b, y)
            p.lineTo(a, y)
        }
        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p, 0, 300))
            .build()
        dispatchGesture(g, null, null)
    }

    fun longClickText(t: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (n in root.findAccessibilityNodeInfosByText(t)) {
            var c: AccessibilityNodeInfo? = n
            while (c != null) {
                if (c.isLongClickable) return c.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                c = c.parent
            }
        }
        return false
    }

    fun screenText(): String {
        val root = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        fun walk(n: AccessibilityNodeInfo?) {
            if (n == null || sb.length > 600) return
            val s = n.text
            if (s != null && s.isNotBlank()) sb.append(s).append(". ")
            for (i in 0 until n.childCount) walk(n.getChild(i))
        }
        walk(root)
        return sb.toString()
    }

    fun clickText(t: String): Boolean {
        val root = rootInActiveWindow ?: return false
        for (n in root.findAccessibilityNodeInfosByText(t)) {
            var c: AccessibilityNodeInfo? = n
            while (c != null) {
                if (c.isClickable) return c.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                c = c.parent
            }
        }
        return false
    }

    fun typeText(t: String): Boolean {
        val f = rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return false
        val a = Bundle()
        a.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, t)
        return f.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, a)
    }
}

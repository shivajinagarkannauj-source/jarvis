package com.jarvis.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(40, 90, 40, 40)
        val info = TextView(this)
        info.textSize = 16f
        info.text = "JARVIS\n\n1) Neeche button se Accessibility mein Jarvis ON karo.\n2) Screen ke kinare mic button dikhega. Kisi bhi app mein usse dabao aur bolo.\n\nExamples: WhatsApp kholo, neeche scroll karo, back, home, volume badhao, torch on, click Search, type hello, call 9876543210"
        val acc = Button(this)
        acc.text = "Accessibility settings kholo"
        acc.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        val et = EditText(this)
        et.hint = "Ya yahan likhkar command do"
        val go = Button(this)
        go.text = "Chalao"
        go.setOnClickListener {
            Brain.run(applicationContext, et.text.toString())
            et.setText("")
        }
        root.addView(info)
        root.addView(acc)
        root.addView(et)
        root.addView(go)
        setContentView(root)
    }
}

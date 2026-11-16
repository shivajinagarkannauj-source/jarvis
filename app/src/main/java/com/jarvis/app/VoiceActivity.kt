package com.jarvis.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent

class VoiceActivity : Activity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye...")
        try {
            startActivityForResult(i, 1)
        } catch (e: Exception) {
            Brain.say(applicationContext, "Voice support nahi mila.")
            finish()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(r: Int, res: Int, d: Intent?) {
        super.onActivityResult(r, res, d)
        val text = d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        val app = applicationContext
        finish()
        if (res == RESULT_OK && text != null) {
            Handler(Looper.getMainLooper()).postDelayed({ Brain.run(app, text) }, 900)
        }
    }
}

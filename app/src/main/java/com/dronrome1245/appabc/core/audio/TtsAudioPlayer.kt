package com.dronrome1245.appabc.core.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsAudioPlayer(context: Context) : TextToSpeechWrapper, TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ru", "RU"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsAudioPlayer", "Language is not supported")
            } else {
                isInitialized = true
            }
        } else {
            Log.e("TtsAudioPlayer", "Initialization failed")
        }
    }

    override fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

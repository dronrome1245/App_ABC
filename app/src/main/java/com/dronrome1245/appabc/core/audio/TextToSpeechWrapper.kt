package com.dronrome1245.appabc.core.audio

/**
 * Interface for Text-to-Speech operations.
 */
interface TextToSpeechWrapper {
    fun speak(text: String)
    fun stop()
    fun release()
}

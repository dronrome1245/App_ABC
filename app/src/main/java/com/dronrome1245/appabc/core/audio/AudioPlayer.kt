package com.dronrome1245.appabc.core.audio

/**
 * Single audio boundary used by UI/ViewModels.
 * Implementations may use local assets, TTS, or another Android audio API.
 */
interface AudioPlayer {
    fun playLetterSound(letter: Char)
    fun playFeedback(isCorrect: Boolean)
    fun stop()
    fun release()
}

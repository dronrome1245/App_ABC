package com.dronrome1245.appabc.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * D020 audio strategy: local pre-recorded asset first, system TTS fallback.
 */
class HybridAudioPlayer(
    context: Context,
    private val tts: TextToSpeechWrapper,
    private val spokenNameProvider: (Char) -> String,
    private val rawResourceNameProvider: (Char) -> String? = AudioAssetCatalog::resourceNameForLetter
) : AudioPlayer {

    private val appContext = context.applicationContext
    private var activePlayer: MediaPlayer? = null

    override fun playLetterSound(letter: Char) {
        val fallback = { tts.speak(spokenNameProvider(letter)) }
        playRawOrFallback(rawResourceNameProvider(letter), fallback)
    }

    override fun playFeedback(isCorrect: Boolean) {
        val resourceName = if (isCorrect) AudioAssetCatalog.CORRECT_FEEDBACK else AudioAssetCatalog.INCORRECT_FEEDBACK
        val fallback = { tts.speak(if (isCorrect) "Верно" else "Попробуй ещё") }
        playRawOrFallback(resourceName, fallback)
    }

    override fun playLevelComplete() {
        playRawOrFallback(AudioAssetCatalog.LEVEL_COMPLETE) {
            tts.speak("Уровень завершён")
        }
    }

    override fun stop() {
        releaseActivePlayer()
        tts.stop()
    }

    override fun release() {
        releaseActivePlayer()
        tts.release()
    }

    private fun playRawOrFallback(resourceName: String?, fallback: () -> Unit) {
        releaseActivePlayer()

        val resourceId = if (resourceName.isNullOrBlank()) {
            0
        } else {
            appContext.resources.getIdentifier(resourceName, "raw", appContext.packageName)
        }

        if (AudioFallbackPolicy.shouldFallback(resourceName, resourceId)) {
            fallback()
            return
        }

        try {
            val player = MediaPlayer.create(appContext, resourceId)
            if (player == null) {
                fallback()
                return
            }

            activePlayer = player
            player.setOnCompletionListener { completed ->
                if (activePlayer === completed) activePlayer = null
                completed.release()
            }
            player.setOnErrorListener { failed, what, extra ->
                Log.e(TAG, "Local audio playback failed: what=$what extra=$extra")
                if (activePlayer === failed) activePlayer = null
                failed.release()
                fallback()
                true
            }
            player.start()
        } catch (error: RuntimeException) {
            Log.e(TAG, "Unable to play local audio asset $resourceName", error)
            releaseActivePlayer()
            fallback()
        }
    }

    private fun releaseActivePlayer() {
        val player = activePlayer ?: return
        activePlayer = null
        try {
            if (player.isPlaying) player.stop()
        } catch (_: IllegalStateException) {
            // Player may already have completed or failed.
        }
        player.release()
    }

    private companion object {
        const val TAG = "HybridAudioPlayer"
    }
}

object AudioAssetCatalog {
    const val CORRECT_FEEDBACK = "sound_correct"
    const val INCORRECT_FEEDBACK = "sound_wrong"
    const val LEVEL_COMPLETE = "sound_level_complete"

    private val letterResources = mapOf(
        'А' to "sound_letter_a",
        'М' to "sound_letter_m",
        'О' to "sound_letter_o",
        'У' to "sound_letter_u",
        'С' to "sound_letter_s",
        'Н' to "sound_letter_n"
    )

    fun resourceNameForLetter(letter: Char): String? = letterResources[letter.uppercaseChar()]
}

/** Pure policy kept separately so the missing-resource fallback is JVM-testable. */
object AudioFallbackPolicy {
    fun shouldFallback(resourceName: String?, resourceId: Int): Boolean =
        resourceName.isNullOrBlank() || resourceId == 0
}

package com.dronrome1245.appabc.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/**
 * D020 audio strategy: local pre-recorded asset first, system TTS fallback.
 *
 * No placeholder audio files are bundled here. Missing resources intentionally fall back to TTS.
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
        val resourceName = rawResourceNameProvider(letter)
        playRawOrFallback(resourceName, fallback)
    }

    override fun playFeedback(isCorrect: Boolean) {
        val resourceName = if (isCorrect) {
            AudioAssetCatalog.CORRECT_FEEDBACK
        } else {
            AudioAssetCatalog.INCORRECT_FEEDBACK
        }
        val fallback = {
            tts.speak(if (isCorrect) "Верно" else "Попробуй ещё")
        }
        playRawOrFallback(resourceName, fallback)
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

        if (resourceName.isNullOrBlank()) {
            fallback()
            return
        }

        val resourceId = appContext.resources.getIdentifier(
            resourceName,
            "raw",
            appContext.packageName
        )
        if (resourceId == 0) {
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
                if (activePlayer === completed) {
                    activePlayer = null
                }
                completed.release()
            }
            player.setOnErrorListener { failed, what, extra ->
                Log.e(TAG, "Local audio playback failed: what=$what extra=$extra")
                if (activePlayer === failed) {
                    activePlayer = null
                }
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

/**
 * Stable resource-name convention. Real WAV/OGG files can be added later without changing callers.
 */
object AudioAssetCatalog {
    const val CORRECT_FEEDBACK = "sound_feedback_correct"
    const val INCORRECT_FEEDBACK = "sound_feedback_incorrect"

    private val letterResources = mapOf(
        'А' to "sound_letter_a",
        'М' to "sound_letter_m"
    )

    fun resourceNameForLetter(letter: Char): String? = letterResources[letter.uppercaseChar()]
}

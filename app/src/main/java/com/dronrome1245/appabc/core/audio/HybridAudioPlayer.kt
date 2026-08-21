package com.dronrome1245.appabc.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log

/** D020 audio strategy: local pre-recorded asset first, system TTS fallback. */
class HybridAudioPlayer(
    context: Context,
    private val tts: TextToSpeechWrapper,
    private val spokenNameProvider: (Char) -> String,
    private val rawResourceCandidatesProvider: (Char) -> List<String> = AudioAssetCatalog::resourceCandidatesForLetter,
    private val settingsSource: AudioSettingsSource = AlwaysEnabledAudioSettings
) : AudioPlayer {
    private val appContext = context.applicationContext
    private val rawResourceResolver = RawAudioResourceResolver(appContext)
    private var activePlayer: MediaPlayer? = null
    private var released = false

    override fun playLetterSound(letter: Char) {
        if (released || !settingsSource.isVoiceoverEnabledNow) return
        val fallback = { tts.speak(spokenNameProvider(letter)) }
        playRawOrFallback(rawResourceCandidatesProvider(letter), fallback)
    }

    override fun playFeedback(isCorrect: Boolean) {
        if (released || !settingsSource.isSoundEffectsEnabledNow) return
        val resourceName = if (isCorrect) AudioAssetCatalog.CORRECT_FEEDBACK else AudioAssetCatalog.INCORRECT_FEEDBACK
        val fallback = { tts.speak(if (isCorrect) "Верно" else "Попробуй ещё") }
        playRawOrFallback(listOf(resourceName), fallback)
    }

    override fun playLevelComplete() {
        if (released || !settingsSource.isSoundEffectsEnabledNow) return
        playRawOrFallback(listOf(AudioAssetCatalog.LEVEL_COMPLETE)) { tts.speak("Уровень завершён") }
    }

    override fun stop() {
        if (released) return
        releaseActivePlayer()
        tts.stop()
    }

    override fun release() {
        if (released) return
        released = true
        releaseActivePlayer()
        tts.release()
    }

    private fun playRawOrFallback(resourceNames: List<String>, fallback: () -> Unit) {
        if (released) return
        releaseActivePlayer()

        val resolved = rawResourceResolver.resolveFirst(resourceNames)
        if (resolved == null) {
            if (resourceNames.isNotEmpty()) {
                Log.w(TAG, "No bundled raw audio found for candidates=${resourceNames.joinToString()}; using TTS fallback")
            }
            fallback()
            return
        }

        try {
            val player = MediaPlayer.create(appContext, resolved.resourceId)
            if (player == null) {
                Log.w(TAG, "MediaPlayer.create returned null for ${resolved.resourceName}; using TTS fallback")
                fallback()
                return
            }
            activePlayer = player
            player.setOnCompletionListener { completed ->
                if (activePlayer === completed) activePlayer = null
                completed.release()
            }
            player.setOnErrorListener { failed, what, extra ->
                Log.e(TAG, "Local audio playback failed for ${resolved.resourceName}: what=$what extra=$extra")
                if (activePlayer === failed) activePlayer = null
                failed.release()
                fallback()
                true
            }
            player.start()
        } catch (error: RuntimeException) {
            Log.e(TAG, "Unable to play local audio asset ${resolved.resourceName}", error)
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

    private companion object { const val TAG = "HybridAudioPlayer" }
}

object AudioAssetCatalog {
    const val CORRECT_FEEDBACK = "sound_correct"
    const val INCORRECT_FEEDBACK = "sound_wrong"
    const val LEVEL_COMPLETE = "sound_level_complete"
    const val V2_SUFFIX = "_v2"

    private val legacyLetterResources = mapOf(
        'А' to "sound_letter_a", 'Б' to "sound_letter_b", 'В' to "sound_letter_v",
        'Г' to "sound_letter_g", 'Д' to "sound_letter_d", 'Е' to "sound_letter_e",
        'Ё' to "sound_letter_yo", 'Ж' to "sound_letter_zh", 'З' to "sound_letter_z",
        'И' to "sound_letter_i", 'Й' to "sound_letter_short_i", 'К' to "sound_letter_k",
        'Л' to "sound_letter_l", 'М' to "sound_letter_m", 'Н' to "sound_letter_n",
        'О' to "sound_letter_o", 'П' to "sound_letter_p", 'Р' to "sound_letter_r",
        'С' to "sound_letter_s", 'Т' to "sound_letter_t", 'У' to "sound_letter_u",
        'Ф' to "sound_letter_f", 'Х' to "sound_letter_h", 'Ц' to "sound_letter_ts",
        'Ч' to "sound_letter_ch", 'Ш' to "sound_letter_sh", 'Щ' to "sound_letter_shch",
        'Ъ' to "sound_letter_hard", 'Ы' to "sound_letter_y", 'Ь' to "sound_letter_soft",
        'Э' to "sound_letter_eh", 'Ю' to "sound_letter_yu", 'Я' to "sound_letter_ya"
    )

    /** Legacy v1 name kept for compatibility and as the second local fallback. */
    fun resourceNameForLetter(letter: Char): String? = legacyLetterResources[letter.uppercaseChar()]

    /**
     * D027 Audio Pack v2 convention. New studio assets are expected as
     * `sound_letter_<token>_v2.ogg` in res/raw.
     */
    fun v2ResourceNameForLetter(letter: Char): String? =
        resourceNameForLetter(letter)?.plus(V2_SUFFIX)

    /** D027 playback order: studio v2 first, bundled v1 second, then TTS in HybridAudioPlayer. */
    fun resourceCandidatesForLetter(letter: Char): List<String> {
        val legacy = resourceNameForLetter(letter) ?: return emptyList()
        return listOf("$legacy$V2_SUFFIX", legacy)
    }

    fun mappedLetters(): Set<Char> = legacyLetterResources.keys

    fun isValidRawResourceName(resourceName: String): Boolean = RAW_RESOURCE_NAME.matches(resourceName)

    private val RAW_RESOURCE_NAME = Regex("^[a-z][a-z0-9_]*$")
}

data class ResolvedRawAudioResource(
    val resourceName: String,
    val resourceId: Int
)

/** Resolves the first bundled Android raw resource from an ordered candidate list. */
class RawAudioResourceResolver(context: Context) {
    private val appContext = context.applicationContext

    fun resolveFirst(resourceNames: Iterable<String>): ResolvedRawAudioResource? {
        resourceNames.forEach { resourceName ->
            if (!AudioAssetCatalog.isValidRawResourceName(resourceName)) return@forEach
            val resourceId = appContext.resources.getIdentifier(resourceName, "raw", appContext.packageName)
            if (resourceId != 0) return ResolvedRawAudioResource(resourceName, resourceId)
        }
        return null
    }
}

object AudioFallbackPolicy {
    fun shouldFallback(resourceName: String?, resourceId: Int): Boolean =
        resourceName.isNullOrBlank() || resourceId == 0
}

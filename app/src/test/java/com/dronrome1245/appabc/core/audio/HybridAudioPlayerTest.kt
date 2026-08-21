package com.dronrome1245.appabc.core.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HybridAudioPlayerTest {
    @Test
    fun voiceoverDisabledSuppressesLetterPlayback() {
        val tts = RecordingTts()
        val settings = MutableAudioSettings(
            isVoiceoverEnabledNow = false,
            isSoundEffectsEnabledNow = true
        )
        val player = createPlayer(tts, settings)

        player.playLetterSound('А')

        assertEquals(emptyList<String>(), tts.spoken)
        player.release()
    }

    @Test
    fun soundEffectsDisabledSuppressesFeedbackAndLevelComplete() {
        val tts = RecordingTts()
        val settings = MutableAudioSettings(
            isVoiceoverEnabledNow = true,
            isSoundEffectsEnabledNow = false
        )
        val player = createPlayer(tts, settings)

        player.playFeedback(isCorrect = true)
        player.playFeedback(isCorrect = false)
        player.playLevelComplete()

        assertEquals(emptyList<String>(), tts.spoken)
        player.release()
    }

    @Test
    fun enabledChannelsStillUseExistingFallbackPath() {
        val tts = RecordingTts()
        val settings = MutableAudioSettings(
            isVoiceoverEnabledNow = true,
            isSoundEffectsEnabledNow = true
        )
        val player = createPlayer(tts, settings)

        player.playLetterSound('А')
        player.playFeedback(isCorrect = true)
        player.playLevelComplete()

        assertEquals(listOf("а", "Верно", "Уровень завершён"), tts.spoken)
        player.release()
    }

    @Test
    fun releaseIsIdempotentAndBlocksPlaybackAfterRelease() {
        val tts = RecordingTts()
        val settings = MutableAudioSettings(
            isVoiceoverEnabledNow = true,
            isSoundEffectsEnabledNow = true
        )
        val player = createPlayer(tts, settings)

        player.release()
        player.release()
        player.playLetterSound('А')
        player.playFeedback(isCorrect = true)
        player.playLevelComplete()

        assertEquals(1, tts.releaseCount)
        assertEquals(emptyList<String>(), tts.spoken)
    }

    private fun createPlayer(tts: RecordingTts, settings: AudioSettingsSource): HybridAudioPlayer {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return HybridAudioPlayer(
            context = context,
            tts = tts,
            spokenNameProvider = { "а" },
            rawResourceCandidatesProvider = { emptyList() },
            settingsSource = settings
        )
    }

    private class MutableAudioSettings(
        override var isVoiceoverEnabledNow: Boolean,
        override var isSoundEffectsEnabledNow: Boolean
    ) : AudioSettingsSource

    private class RecordingTts : TextToSpeechWrapper {
        val spoken = mutableListOf<String>()
        var releaseCount = 0
        override fun speak(text: String) { spoken += text }
        override fun stop() = Unit
        override fun release() { releaseCount += 1 }
    }
}

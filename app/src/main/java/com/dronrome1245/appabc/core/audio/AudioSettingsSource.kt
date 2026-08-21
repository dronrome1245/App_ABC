package com.dronrome1245.appabc.core.audio

/** Synchronous audio-settings boundary used by [HybridAudioPlayer]. */
interface AudioSettingsSource {
    val isSoundEffectsEnabledNow: Boolean
    val isVoiceoverEnabledNow: Boolean
}

object AlwaysEnabledAudioSettings : AudioSettingsSource {
    override val isSoundEffectsEnabledNow: Boolean = true
    override val isVoiceoverEnabledNow: Boolean = true
}

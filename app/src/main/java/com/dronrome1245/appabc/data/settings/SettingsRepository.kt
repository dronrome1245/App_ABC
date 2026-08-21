package com.dronrome1245.appabc.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.dronrome1245.appabc.core.audio.AudioSettingsSource
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

private data class SoundSettings(
    val soundEffectsEnabled: Boolean = true,
    val voiceoverEnabled: Boolean = true
)

class SettingsRepository internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val collectorScope: CoroutineScope
) : AudioSettingsSource {
    private object Keys {
        val soundEffectsEnabled = booleanPreferencesKey("sound_effects_enabled")
        val voiceoverEnabled = booleanPreferencesKey("voiceover_enabled")
    }

    @Volatile
    private var currentSettings = SoundSettings()

    private val settings: Flow<SoundSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            SoundSettings(
                soundEffectsEnabled = preferences[Keys.soundEffectsEnabled] ?: true,
                voiceoverEnabled = preferences[Keys.voiceoverEnabled] ?: true
            )
        }
        .distinctUntilChanged()

    val isSoundEffectsEnabled: Flow<Boolean> = settings
        .map { it.soundEffectsEnabled }
        .distinctUntilChanged()

    val isVoiceoverEnabled: Flow<Boolean> = settings
        .map { it.voiceoverEnabled }
        .distinctUntilChanged()

    override val isSoundEffectsEnabledNow: Boolean
        get() = currentSettings.soundEffectsEnabled

    override val isVoiceoverEnabledNow: Boolean
        get() = currentSettings.voiceoverEnabled

    constructor(context: Context) : this(
        dataStore = context.applicationContext.appSettingsDataStore,
        collectorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    )

    init {
        collectorScope.launch {
            settings.collect { currentSettings = it }
        }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[Keys.soundEffectsEnabled] = enabled }
        currentSettings = currentSettings.copy(soundEffectsEnabled = enabled)
    }

    suspend fun setVoiceoverEnabled(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[Keys.voiceoverEnabled] = enabled }
        currentSettings = currentSettings.copy(voiceoverEnabled = enabled)
    }

    fun close() {
        collectorScope.cancel()
    }
}

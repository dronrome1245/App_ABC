package com.dronrome1245.appabc.data.progression

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.levelProgressionDataStore by preferencesDataStore(name = "level_progression")

data class LevelProgression(
    val highestUnlockedLevel: Int = 1,
    val selectedLevel: Int = 1
)

class LevelProgressionStore(private val context: Context) {
    private object Keys {
        val highestUnlockedLevel = intPreferencesKey("highest_unlocked_level")
        val selectedLevel = intPreferencesKey("selected_level")
    }

    val state: Flow<LevelProgression> = context.levelProgressionDataStore.data.map { preferences ->
        val highest = preferences[Keys.highestUnlockedLevel]?.coerceAtLeast(1) ?: 1
        val selected = (preferences[Keys.selectedLevel] ?: highest).coerceIn(1, highest)
        LevelProgression(
            highestUnlockedLevel = highest,
            selectedLevel = selected
        )
    }

    suspend fun selectLevel(levelId: Int) {
        context.levelProgressionDataStore.edit { preferences ->
            val highest = preferences[Keys.highestUnlockedLevel]?.coerceAtLeast(1) ?: 1
            preferences[Keys.selectedLevel] = levelId.coerceIn(1, highest)
        }
    }

    suspend fun unlockAndSelect(levelId: Int) {
        context.levelProgressionDataStore.edit { preferences ->
            val currentHighest = preferences[Keys.highestUnlockedLevel]?.coerceAtLeast(1) ?: 1
            val newHighest = maxOf(currentHighest, levelId)
            preferences[Keys.highestUnlockedLevel] = newHighest
            preferences[Keys.selectedLevel] = levelId.coerceIn(1, newHighest)
        }
    }
}

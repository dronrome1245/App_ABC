package com.dronrome1245.appabc.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class AppSettings(private val context: Context) {
    companion object {
        val CURRENT_LEVEL = intPreferencesKey("current_level")
    }

    val currentLevel: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_LEVEL] ?: 1
    }

    suspend fun setCurrentLevel(level: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_LEVEL] = level
        }
    }
}

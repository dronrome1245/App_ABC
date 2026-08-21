package com.dronrome1245.appabc.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SettingsRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var collectorScope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        collectorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(temporaryFolder.root, "settings.preferences_pb") }
        )
        repository = SettingsRepository(dataStore, collectorScope)
    }

    @After
    fun tearDown() {
        repository.close()
        dataStoreScope.cancel()
    }

    @Test
    fun defaultsAreEnabled() = runBlocking {
        assertTrue(repository.isVoiceoverEnabled.first())
        assertTrue(repository.isSoundEffectsEnabled.first())
    }

    @Test
    fun valuesPersistInDataStoreAndAreVisibleToNewRepositoryInstance() = runBlocking {
        repository.setVoiceoverEnabled(false)
        repository.setSoundEffectsEnabled(false)

        assertFalse(repository.isVoiceoverEnabled.first())
        assertFalse(repository.isSoundEffectsEnabled.first())

        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val secondRepository = SettingsRepository(dataStore, secondScope)
        try {
            assertFalse(secondRepository.isVoiceoverEnabled.first())
            assertFalse(secondRepository.isSoundEffectsEnabled.first())
        } finally {
            secondRepository.close()
        }
    }
}

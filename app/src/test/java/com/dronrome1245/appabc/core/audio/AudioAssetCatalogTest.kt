package com.dronrome1245.appabc.core.audio

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dronrome1245.appabc.domain.curriculum.RussianAlphabet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AudioAssetCatalogTest {
    @Test
    fun mapsAll33RussianLettersIgnoringCase() {
        assertEquals(33, AudioAssetCatalog.mappedLetters().size)
        assertEquals(RussianAlphabet.symbols.map { it.single() }.toSet(), AudioAssetCatalog.mappedLetters())
        RussianAlphabet.symbols.forEach { symbol ->
            val upper = symbol.single()
            val resourceName = AudioAssetCatalog.resourceNameForLetter(upper)
            assertTrue(resourceName?.startsWith("sound_letter_") == true)
            assertEquals(resourceName, AudioAssetCatalog.resourceNameForLetter(upper.lowercaseChar()))
        }
    }

    @Test
    fun all33MappingsResolveToBundledRawResources() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        RussianAlphabet.symbols.forEach { symbol ->
            val resourceName = AudioAssetCatalog.resourceNameForLetter(symbol.single())
            val resourceId = context.resources.getIdentifier(resourceName, "raw", context.packageName)
            assertNotEquals("Missing raw resource for $symbol: $resourceName", 0, resourceId)
        }
    }

    @Test
    fun unknownLetterStillUsesTtsFallback() {
        assertNull(AudioAssetCatalog.resourceNameForLetter('A'))
        assertTrue(AudioFallbackPolicy.shouldFallback(null, 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("", 0))
        assertTrue(AudioFallbackPolicy.shouldFallback("sound_letter_a", 0))
        assertFalse(AudioFallbackPolicy.shouldFallback("sound_letter_a", 42))
    }

    @Test
    fun feedbackAndCompletionResourceNamesRemainStable() {
        assertEquals("sound_correct", AudioAssetCatalog.CORRECT_FEEDBACK)
        assertEquals("sound_wrong", AudioAssetCatalog.INCORRECT_FEEDBACK)
        assertEquals("sound_level_complete", AudioAssetCatalog.LEVEL_COMPLETE)
    }
}
